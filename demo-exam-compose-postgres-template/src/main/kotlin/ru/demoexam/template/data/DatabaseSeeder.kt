package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.data.local.AppDatabase
import ru.demoexam.template.data.local.CategoryEntity
import ru.demoexam.template.data.local.CustomerEntity
import ru.demoexam.template.data.local.ManufacturerEntity
import ru.demoexam.template.data.local.OrderEntity
import ru.demoexam.template.data.local.OrderItemEntity
import ru.demoexam.template.data.local.OrderStatusEntity
import ru.demoexam.template.data.local.ProductEntity
import ru.demoexam.template.data.local.RoleEntity
import ru.demoexam.template.data.local.SupplierEntity
import ru.demoexam.template.data.local.UnitEntity
import ru.demoexam.template.data.local.UserEntity
import java.math.BigDecimal
import java.time.LocalDateTime

object DatabaseSeeder {
    fun seedIfNeeded(database: AppDatabase) {
        if (runBlocking { database.maintenanceDao().countRoles() } > 0) {
            return
        }

        runBlocking {
            val maintenanceDao = database.maintenanceDao()

            maintenanceDao.insertRole(RoleEntity(id = 1, code = "client", name = "Клиент"))
            maintenanceDao.insertRole(RoleEntity(id = 2, code = "manager", name = "Менеджер"))
            maintenanceDao.insertRole(RoleEntity(id = 3, code = "admin", name = "Администратор"))

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

            val fictionId = maintenanceDao.insertCategory(CategoryEntity(name = "Художественная литература")).toInt()
            val educationId = maintenanceDao.insertCategory(CategoryEntity(name = "Учебная литература")).toInt()
            val comicsId = maintenanceDao.insertCategory(CategoryEntity(name = "Комиксы")).toInt()
            val kidsId = maintenanceDao.insertCategory(CategoryEntity(name = "Детские книги")).toInt()

            val eksmoId = maintenanceDao.insertManufacturer(ManufacturerEntity(name = "Эксмо")).toInt()
            val astId = maintenanceDao.insertManufacturer(ManufacturerEntity(name = "АСТ")).toInt()
            val piterId = maintenanceDao.insertManufacturer(ManufacturerEntity(name = "Питер")).toInt()

            val supplier1Id = maintenanceDao.insertSupplier(SupplierEntity(name = "Поставщик 1")).toInt()
            val supplier2Id = maintenanceDao.insertSupplier(SupplierEntity(name = "Поставщик 2")).toInt()
            val supplier3Id = maintenanceDao.insertSupplier(SupplierEntity(name = "Книжный склад Урал")).toInt()

            val unitId = maintenanceDao.insertUnit(UnitEntity(name = "шт.")).toInt()

            val mariaCustomerId = maintenanceDao.insertCustomer(
                CustomerEntity(
                    fullName = "Кузнецова Мария Андреевна",
                    phone = "+7 900 000-00-01",
                    email = "maria@example.com",
                ),
            ).toInt()
            val sergeyCustomerId = maintenanceDao.insertCustomer(
                CustomerEntity(
                    fullName = "Орлов Сергей Петрович",
                    phone = "+7 900 000-00-02",
                    email = "sergey@example.com",
                ),
            ).toInt()

            val newStatusId = maintenanceDao.insertOrderStatus(OrderStatusEntity(name = "Новый")).toInt()
            val inProgressStatusId = maintenanceDao.insertOrderStatus(OrderStatusEntity(name = "В работе")).toInt()
            maintenanceDao.insertOrderStatus(OrderStatusEntity(name = "Завершен"))

            maintenanceDao.insertProduct(
                ProductEntity(
                    id = 1001,
                    article = "A1001",
                    name = "451 градус по Фаренгейту",
                    categoryId = fictionId,
                    description = "Роман-антиутопия Рэя Брэдбери.",
                    manufacturerId = eksmoId,
                    supplierId = supplier1Id,
                    unitId = unitId,
                    price = BigDecimal("650.00"),
                    stockQuantity = 18,
                    discountPercent = BigDecimal("10.00"),
                ),
            )
            maintenanceDao.insertProduct(
                ProductEntity(
                    id = 1002,
                    article = "A1002",
                    name = "Чистый код",
                    categoryId = educationId,
                    description = "Книга по написанию поддерживаемого кода.",
                    manufacturerId = piterId,
                    supplierId = supplier3Id,
                    unitId = unitId,
                    price = BigDecimal("1450.00"),
                    stockQuantity = 4,
                    discountPercent = BigDecimal("27.00"),
                ),
            )
            maintenanceDao.insertProduct(
                ProductEntity(
                    id = 1003,
                    article = "A1003",
                    name = "Котенок Шмяк",
                    categoryId = kidsId,
                    description = "Детская книга с иллюстрациями.",
                    manufacturerId = astId,
                    supplierId = supplier2Id,
                    unitId = unitId,
                    price = BigDecimal("420.00"),
                    stockQuantity = 0,
                    discountPercent = BigDecimal("0.00"),
                ),
            )
            maintenanceDao.insertProduct(
                ProductEntity(
                    id = 1004,
                    article = "A1004",
                    name = "Бэтмен: Год первый",
                    categoryId = comicsId,
                    description = "Комикс о начале истории Бэтмена.",
                    manufacturerId = astId,
                    supplierId = supplier2Id,
                    unitId = unitId,
                    price = BigDecimal("990.00"),
                    stockQuantity = 7,
                    discountPercent = BigDecimal("15.00"),
                ),
            )

            maintenanceDao.insertOrder(
                OrderEntity(
                    id = 1,
                    customerId = mariaCustomerId,
                    managerId = 2,
                    statusId = newStatusId,
                    orderDate = LocalDateTime.now().minusDays(1),
                    comment = "Срочная доставка",
                ),
            )
            maintenanceDao.insertOrder(
                OrderEntity(
                    id = 2,
                    customerId = sergeyCustomerId,
                    managerId = 2,
                    statusId = inProgressStatusId,
                    orderDate = LocalDateTime.now().minusDays(2),
                    comment = "Самовывоз",
                ),
            )

            maintenanceDao.insertOrderItem(
                OrderItemEntity(
                    orderId = 1,
                    productId = 1001,
                    quantity = 1,
                    unitPrice = BigDecimal("650.00"),
                    discountPercent = BigDecimal("10.00"),
                ),
            )
            maintenanceDao.insertOrderItem(
                OrderItemEntity(
                    orderId = 2,
                    productId = 1002,
                    quantity = 2,
                    unitPrice = BigDecimal("1450.00"),
                    discountPercent = BigDecimal("27.00"),
                ),
            )
        }
    }
}
