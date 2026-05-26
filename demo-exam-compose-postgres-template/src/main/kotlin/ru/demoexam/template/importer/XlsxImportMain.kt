package ru.demoexam.template.importer

import kotlinx.coroutines.runBlocking
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import ru.demoexam.template.config.AppConfigLoader
import ru.demoexam.template.data.AppDatabaseProvider
import ru.demoexam.template.data.local.AppDatabase
import ru.demoexam.template.data.local.CategoryEntity
import ru.demoexam.template.data.local.CustomerEntity
import ru.demoexam.template.data.local.ManufacturerEntity
import ru.demoexam.template.data.local.OrderEntity
import ru.demoexam.template.data.local.OrderItemEntity
import ru.demoexam.template.data.local.OrderStatusEntity
import ru.demoexam.template.data.local.PickupPointEntity
import ru.demoexam.template.data.local.ProductEntity
import ru.demoexam.template.data.local.RoleEntity
import ru.demoexam.template.data.local.SupplierEntity
import ru.demoexam.template.data.local.UnitEntity
import ru.demoexam.template.data.local.UserEntity
import ru.demoexam.template.util.AppDirectories
import ru.demoexam.template.util.ImageStorage
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.name
import kotlin.streams.toList

private val dateTimeFormats: List<DateTimeFormatter> = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
    DateTimeFormatter.ofPattern("yyyy-MM-dd H:mm:ss"),
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
)

private val dateFormats: List<DateTimeFormatter> = listOf(
    DateTimeFormatter.ofPattern("yyyy-MM-dd"),
    DateTimeFormatter.ofPattern("dd.MM.yyyy"),
)

fun main(args: Array<String>) {
    val importDirectory = resolveImportDirectory(args)

    println("XLSX import started")
    println("Source directory: $importDirectory")

    AppDatabaseProvider.initialize(AppConfigLoader.load())

    runCatching {
        XlsxImportService(
            importDirectory = importDirectory,
            database = AppDatabaseProvider.getDatabase(),
        ).run()
    }.onSuccess {
        println("XLSX import completed successfully")
    }.onFailure { throwable ->
        System.err.println("XLSX import failed: ${throwable.message}")
        throw throwable
    }.also {
        AppDatabaseProvider.close()
    }
}

private fun resolveImportDirectory(args: Array<String>): Path {
    val fromArgs = args.firstOrNull()?.takeIf { it.isNotBlank() }
    val fromEnvironment = System.getenv("IMPORT_DIR")?.takeIf { it.isNotBlank() }
    val rawPath = fromArgs ?: fromEnvironment ?: System.getProperty("user.dir")
    val resolvedPath = Path.of(rawPath).toAbsolutePath().normalize()

    require(Files.exists(resolvedPath)) {
        "Каталог импорта не найден: $resolvedPath"
    }
    require(Files.isDirectory(resolvedPath)) {
        "Путь импорта должен указывать на каталог: $resolvedPath"
    }

    return resolvedPath
}

private class XlsxImportService(
    private val importDirectory: Path,
    private val database: AppDatabase,
) {
    private val formatter = DataFormatter()

    fun run() = runBlocking {
        val productsFile = findOptionalFile("Tovar.xlsx", "Товар.xlsx")
            ?: error("Не найден файл товаров `Tovar.xlsx`.")
        val usersFile = findOptionalFile("user_import.xlsx")
        val pickupPointsFile = findOptionalFile("Пункты выдачи_import.xlsx", "ПунктыВыдачи_import.xlsx")
        val ordersFile = findOptionalFile("Заказ_import.xlsx", "Orders_import.xlsx")

        clearBusinessData()
        insertBaseRoles()

        val productIndex = importProducts(productsFile)
        importUsers(usersFile)
        val pickupPointIndex = importPickupPoints(pickupPointsFile)
        importOrders(ordersFile, productIndex, pickupPointIndex)
    }

    private suspend fun clearBusinessData() {
        val maintenanceDao = database.maintenanceDao()
        maintenanceDao.clearOrderItems()
        maintenanceDao.clearOrders()
        maintenanceDao.clearCustomers()
        maintenanceDao.clearPickupPoints()
        maintenanceDao.clearProducts()
        maintenanceDao.clearCategories()
        maintenanceDao.clearManufacturers()
        maintenanceDao.clearSuppliers()
        maintenanceDao.clearUnits()
        maintenanceDao.clearUsers()
        maintenanceDao.clearOrderStatuses()
        maintenanceDao.clearRoles()

        AppDirectories.ensureCreated()
        Files.list(AppDirectories.imagesDirectory).use { paths ->
            paths.forEach(Files::deleteIfExists)
        }
    }

    private suspend fun insertBaseRoles() {
        val maintenanceDao = database.maintenanceDao()
        maintenanceDao.insertRole(RoleEntity(id = 1, code = "client", name = "Клиент"))
        maintenanceDao.insertRole(RoleEntity(id = 2, code = "manager", name = "Менеджер"))
        maintenanceDao.insertRole(RoleEntity(id = 3, code = "admin", name = "Администратор"))
    }

    private suspend fun importProducts(file: Path): Map<String, ImportedProduct> {
        val maintenanceDao = database.maintenanceDao()
        val result = linkedMapOf<String, ImportedProduct>()
        var nextProductId = 1

        withFirstSheet(file) { sheet ->
            val header = sheet.readHeaderMap()

            sheet.forEachDataRow(startRowIndex = 1) { row ->
                val article = row.stringValue(header, "Артикул")
                if (article.isBlank()) {
                    return@forEachDataRow
                }

                val productId = nextProductId++
                val categoryId = getOrCreateCategoryId(row.stringValue(header, "Категория товара"))
                val manufacturerId = getOrCreateManufacturerId(row.stringValue(header, "Производитель"))
                val supplierId = getOrCreateSupplierId(row.stringValue(header, "Поставщик"))
                val unitId = getOrCreateUnitId(row.stringValue(header, "Единица измерения"))
                val price = row.decimalValue(header, "Цена")
                val discountPercent = row.decimalValue(header, "Действующая скидка")
                val imagePath = importProductImage(
                    productId = productId,
                    imageFileName = row.stringValue(header, "Фото"),
                )

                maintenanceDao.insertProduct(
                    ProductEntity(
                        id = productId,
                        article = article,
                        name = row.stringValue(header, "Наименование товара"),
                        categoryId = categoryId,
                        description = row.stringValue(header, "Описание товара"),
                        manufacturerId = manufacturerId,
                        supplierId = supplierId,
                        unitId = unitId,
                        price = price,
                        stockQuantity = row.intValue(header, "Кол-во на складе"),
                        discountPercent = discountPercent,
                        imagePath = imagePath,
                    ),
                )

                result[article] = ImportedProduct(
                    id = productId,
                    price = price,
                    discountPercent = discountPercent,
                )
            }
        }

        println("Imported products: ${result.size}")
        return result
    }

    private suspend fun importUsers(file: Path?) {
        if (file == null) {
            insertDefaultUsers()
            println("Users file not found, default users created")
            return
        }

        val maintenanceDao = database.maintenanceDao()
        var importedCount = 0

        withFirstSheet(file) { sheet ->
            val header = sheet.readHeaderMap()

            sheet.forEachDataRow(startRowIndex = 1) { row ->
                val login = row.stringValue(header, "Логин")
                if (login.isBlank()) {
                    return@forEachDataRow
                }

                val roleId = parseRoleId(row.stringValue(header, "Роль сотрудника"))

                maintenanceDao.insertUser(
                    UserEntity(
                        fullName = row.stringValue(header, "ФИО"),
                        login = login,
                        password = row.stringValue(header, "Пароль"),
                        roleId = roleId,
                        isActive = true,
                    ),
                )
                importedCount++
            }
        }

        println("Imported users: $importedCount")
    }

    private suspend fun insertDefaultUsers() {
        val maintenanceDao = database.maintenanceDao()
        maintenanceDao.insertUser(
            UserEntity(
                fullName = "Иванов Иван Иванович",
                login = "client",
                password = "client",
                roleId = 1,
            ),
        )
        maintenanceDao.insertUser(
            UserEntity(
                fullName = "Петрова Анна Сергеевна",
                login = "manager",
                password = "manager",
                roleId = 2,
            ),
        )
        maintenanceDao.insertUser(
            UserEntity(
                fullName = "Смирнов Дмитрий Олегович",
                login = "admin",
                password = "admin",
                roleId = 3,
            ),
        )
    }

    private suspend fun importPickupPoints(file: Path?): Map<Int, Int> {
        if (file == null) {
            println("Pickup points file not found, skipped")
            return emptyMap()
        }

        val result = linkedMapOf<Int, Int>()

        withFirstSheet(file) { sheet ->
            sheet.forEachDataRow(startRowIndex = 0) { row ->
                val address = row.getCell(0).asTrimmedText()
                if (address.isBlank()) {
                    return@forEachDataRow
                }

                val pickupPointId = getOrCreatePickupPointId(address)
                result[row.rowNum + 1] = pickupPointId
            }
        }

        println("Imported pickup points: ${result.size}")
        return result
    }

    private suspend fun importOrders(
        file: Path?,
        productsByArticle: Map<String, ImportedProduct>,
        pickupPointsBySourceNumber: Map<Int, Int>,
    ) {
        if (file == null) {
            println("Orders file not found, skipped")
            return
        }

        val maintenanceDao = database.maintenanceDao()
        var importedCount = 0

        withFirstSheet(file) { sheet ->
            val header = sheet.readHeaderMap()

            sheet.forEachDataRow(startRowIndex = 1) { row ->
                val orderId = row.intValue(header, "Номер заказа")
                val itemsRaw = row.stringValue(header, "Артикул заказа")
                if (itemsRaw.isBlank()) {
                    return@forEachDataRow
                }

                val customerId = getOrCreateCustomerId(row.stringValue(header, "ФИО авторизированного клиента"))
                val statusId = getOrCreateOrderStatusId(row.stringValue(header, "Статус заказа"))
                val pickupPointId = resolvePickupPointId(
                    cellText = row.stringValue(header, "Адрес пункта выдачи"),
                    pickupPointsBySourceNumber = pickupPointsBySourceNumber,
                )

                maintenanceDao.insertOrder(
                    OrderEntity(
                        id = orderId,
                        customerId = customerId,
                        managerId = null,
                        statusId = statusId,
                        orderDate = row.dateTimeValue(header, "Дата заказа"),
                        deliveryDate = row.dateTimeValue(header, "Дата доставки"),
                        pickupPointId = pickupPointId,
                        pickupCode = row.stringValue(header, "Код для получения"),
                        comment = "",
                    ),
                )

                parseOrderItems(itemsRaw).forEach { item ->
                    val product = productsByArticle[item.article]
                        ?: error("В заказе #$orderId найден неизвестный артикул `${item.article}`.")

                    maintenanceDao.insertOrderItem(
                        OrderItemEntity(
                            orderId = orderId,
                            productId = product.id,
                            quantity = item.quantity,
                            unitPrice = product.price,
                            discountPercent = product.discountPercent,
                        ),
                    )
                }

                importedCount++
            }
        }

        println("Imported orders: $importedCount")
    }

    private suspend fun getOrCreateCategoryId(name: String): Int {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) {
            "Не указана категория товара."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findCategoryByName(normalizedName)?.id
            ?: maintenanceDao.insertCategory(CategoryEntity(name = normalizedName)).toInt()
    }

    private suspend fun getOrCreateManufacturerId(name: String): Int {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) {
            "Не указан производитель."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findManufacturerByName(normalizedName)?.id
            ?: maintenanceDao.insertManufacturer(ManufacturerEntity(name = normalizedName)).toInt()
    }

    private suspend fun getOrCreateSupplierId(name: String): Int {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) {
            "Не указан поставщик."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findSupplierByName(normalizedName)?.id
            ?: maintenanceDao.insertSupplier(SupplierEntity(name = normalizedName)).toInt()
    }

    private suspend fun getOrCreateUnitId(name: String): Int {
        val normalizedName = name.trim()
        require(normalizedName.isNotBlank()) {
            "Не указана единица измерения."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findUnitByName(normalizedName)?.id
            ?: maintenanceDao.insertUnit(UnitEntity(name = normalizedName)).toInt()
    }

    private suspend fun getOrCreateCustomerId(fullName: String): Int {
        val normalizedValue = fullName.trim()
        require(normalizedValue.isNotBlank()) {
            "У заказа отсутствует ФИО клиента."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findCustomerByFullName(normalizedValue)?.id
            ?: maintenanceDao.insertCustomer(CustomerEntity(fullName = normalizedValue)).toInt()
    }

    private suspend fun getOrCreatePickupPointId(address: String): Int {
        val normalizedValue = address.trim()
        require(normalizedValue.isNotBlank()) {
            "Не указан адрес пункта выдачи."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findPickupPointByAddress(normalizedValue)?.id
            ?: maintenanceDao.insertPickupPoint(PickupPointEntity(address = normalizedValue)).toInt()
    }

    private suspend fun getOrCreateOrderStatusId(statusName: String): Int {
        val normalizedValue = statusName.trim()
        require(normalizedValue.isNotBlank()) {
            "У заказа отсутствует статус."
        }

        val maintenanceDao = database.maintenanceDao()
        return maintenanceDao.findOrderStatusByName(normalizedValue)?.id
            ?: maintenanceDao.insertOrderStatus(OrderStatusEntity(name = normalizedValue)).toInt()
    }

    private suspend fun resolvePickupPointId(
        cellText: String,
        pickupPointsBySourceNumber: Map<Int, Int>,
    ): Int? {
        val normalizedValue = cellText.trim()
        if (normalizedValue.isBlank()) {
            return null
        }

        normalizedValue.toIntOrNull()?.let { sourceNumber ->
            return pickupPointsBySourceNumber[sourceNumber]
                ?: error("Пункт выдачи с номером $sourceNumber отсутствует в файле пунктов выдачи.")
        }

        return getOrCreatePickupPointId(normalizedValue)
    }

    private fun importProductImage(
        productId: Int,
        imageFileName: String,
    ): String? {
        if (imageFileName.isBlank()) {
            return null
        }

        val sourceFile = importDirectory.resolve(imageFileName)
        if (!Files.exists(sourceFile)) {
            println("Image not found, skipped: $imageFileName")
            return null
        }

        return ImageStorage.storeProductImage(
            sourcePath = sourceFile.toString(),
            existingStoredPath = null,
            productId = productId,
        )
    }

    private fun parseOrderItems(source: String): List<OrderItemInput> {
        val tokens = source.split(",")
            .map(String::trim)
            .filter(String::isNotBlank)

        require(tokens.size % 2 == 0) {
            "Строка заказа имеет некорректную структуру: `$source`."
        }

        return buildList {
            var index = 0
            while (index < tokens.size) {
                add(
                    OrderItemInput(
                        article = tokens[index],
                        quantity = tokens[index + 1].toIntOrNull()
                            ?: error("Количество товара `${tokens[index]}` в строке `$source` не является целым числом."),
                    ),
                )
                index += 2
            }
        }
    }

    private fun findOptionalFile(vararg names: String): Path? {
        val fileNames = importDirectory.fileNamesLowercase()
        return names.firstNotNullOfOrNull { candidate ->
            val exactMatch = fileNames[candidate.lowercase()]
            if (exactMatch != null) {
                return@firstNotNullOfOrNull exactMatch
            }

            fileNames.entries.firstOrNull { (name, _) ->
                name.contains(candidate.lowercase())
            }?.value
        }
    }

    private fun parseRoleId(roleName: String): Int {
        val normalizedRole = roleName.trim().lowercase()
        return when {
            "администратор" in normalizedRole -> 3
            "менеджер" in normalizedRole -> 2
            "клиент" in normalizedRole -> 1
            else -> error("Неизвестная роль сотрудника: `$roleName`.")
        }
    }

    private fun Path.fileNamesLowercase(): Map<String, Path> {
        return Files.list(this).use { paths ->
            paths
                .filter(Files::isRegularFile)
                .toList()
                .associateBy { it.name.lowercase() }
        }
    }

    private suspend fun <T> withFirstSheet(file: Path, block: suspend (AutoCloseableSheet) -> T): T {
        val workbook = WorkbookFactory.create(file.toFile())
        val sheet = AutoCloseableSheet(workbook, workbook.getSheetAt(0))
        try {
            return block(sheet)
        } finally {
            sheet.close()
        }
    }

    private inner class AutoCloseableSheet(
        private val workbook: Workbook,
        val sheet: Sheet,
    ) : AutoCloseable {
        fun readHeaderMap(): Map<String, Int> {
            val headerRow = sheet.getRow(0) ?: error("В файле отсутствует строка заголовков.")
            return buildMap {
                headerRow.forEachIndexed { index, cell ->
                    val value = formatter.formatCellValue(cell).trim()
                    if (value.isNotBlank()) {
                        put(value, index)
                    }
                }
            }
        }

        suspend fun forEachDataRow(
            startRowIndex: Int,
            block: suspend (Row) -> Unit,
        ) {
            val lastRowIndex = sheet.lastRowNum
            for (rowIndex in startRowIndex..lastRowIndex) {
                val row = sheet.getRow(rowIndex) ?: continue
                if (row.isBlank()) {
                    continue
                }
                block(row)
            }
        }

        override fun close() {
            workbook.close()
        }
    }

    private fun Row.stringValue(header: Map<String, Int>, columnName: String): String {
        val columnIndex = header[columnName]
            ?: error("В файле не найдена колонка `$columnName`.")
        return getCell(columnIndex).asTrimmedText()
    }

    private fun Row.decimalValue(header: Map<String, Int>, columnName: String): BigDecimal {
        val rawValue = stringValue(header, columnName)
        return rawValue.replace(" ", "").replace(",", ".").toBigDecimalOrNull()
            ?: error("Значение `$rawValue` в колонке `$columnName` не является числом.")
    }

    private fun Row.intValue(header: Map<String, Int>, columnName: String): Int {
        val rawValue = stringValue(header, columnName)
        return rawValue.toIntOrNull()
            ?: rawValue.toBigDecimalOrNull()?.toInt()
            ?: error("Значение `$rawValue` в колонке `$columnName` не является целым числом.")
    }

    private fun Row.dateTimeValue(header: Map<String, Int>, columnName: String): LocalDateTime {
        val columnIndex = header[columnName]
            ?: error("В файле не найдена колонка `$columnName`.")
        val cell = getCell(columnIndex)

        if (cell != null && cell.cellType == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.localDateTimeCellValue
        }

        val rawValue = cell.asTrimmedText()
        dateTimeFormats.forEach { format ->
            runCatching {
                return LocalDateTime.parse(rawValue, format)
            }
        }
        dateFormats.forEach { format ->
            runCatching {
                return LocalDate.parse(rawValue, format).atStartOfDay()
            }
        }

        return rawValue.toBigDecimalOrNull()?.let {
            DateUtil.getLocalDateTime(it.toDouble())
        } ?: error("Не удалось распознать дату `$rawValue` в колонке `$columnName`.")
    }

    private fun Row.isBlank(): Boolean {
        for (cellIndex in firstCellNum until lastCellNum) {
            if (getCell(cellIndex)?.asTrimmedText().orEmpty().isNotBlank()) {
                return false
            }
        }
        return true
    }

    private fun Cell?.asTrimmedText(): String {
        if (this == null) {
            return ""
        }
        return formatter.formatCellValue(this).trim()
    }
}

private data class ImportedProduct(
    val id: Int,
    val price: BigDecimal,
    val discountPercent: BigDecimal,
)

private data class OrderItemInput(
    val article: String,
    val quantity: Int,
)
