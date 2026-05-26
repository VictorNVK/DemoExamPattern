package ru.demoexam.template.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ru.demoexam.template.config.AppConfig
import ru.demoexam.template.config.AppConfigLoader
import ru.demoexam.template.data.AppDatabaseProvider
import ru.demoexam.template.data.AuthRepository
import ru.demoexam.template.data.OrderRepository
import ru.demoexam.template.data.ProductRepository
import ru.demoexam.template.data.ReferenceRepository
import ru.demoexam.template.model.DeleteProductResult
import ru.demoexam.template.model.MessageKind
import ru.demoexam.template.model.OrderSummary
import ru.demoexam.template.model.ProductDraft
import ru.demoexam.template.model.ProductEditorPayload
import ru.demoexam.template.model.ProductFilter
import ru.demoexam.template.model.ProductFormModel
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.model.UiMessage
import ru.demoexam.template.model.UserSession
import ru.demoexam.template.navigation.AppScreen
import ru.demoexam.template.util.ImageStorage
import ru.demoexam.template.util.ProductValidation
import ru.demoexam.template.util.AppDirectories

class AppState(
    private val authRepository: AuthRepository = AuthRepository(),
    private val referenceRepository: ReferenceRepository = ReferenceRepository(),
    private val productRepository: ProductRepository = ProductRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
) {
    private val backStack = mutableStateListOf<AppScreen>()
    private var appConfig: AppConfig? = null

    var isInitialized by mutableStateOf(false)
        private set

    var startupError by mutableStateOf<String?>(null)
        private set

    var currentSession by mutableStateOf(UserSession.guest())
        private set

    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Login)
        private set

    var products by mutableStateOf<List<ProductListItem>>(emptyList())
        private set

    var orders by mutableStateOf<List<OrderSummary>>(emptyList())
        private set

    var productFilter by mutableStateOf(ProductFilter())
        private set

    var editorPayload by mutableStateOf<ProductEditorPayload?>(null)
        private set

    var uiMessage by mutableStateOf<UiMessage?>(null)
        private set

    val canGoBack: Boolean
        get() = backStack.isNotEmpty()

    val screenTitle: String
        get() = when (currentScreen) {
            AppScreen.Login -> "Вход"
            AppScreen.Products -> "Товары"
            AppScreen.Orders -> "Заказы"
            is AppScreen.ProductEditor -> "Товар"
        }

    val windowTitle: String
        get() = "${appConfig?.applicationTitle ?: "Demo Exam Template"} - $screenTitle"

    fun initialize() {
        if (isInitialized) {
            return
        }

        startupError = null

        runCatching {
            appConfig = AppConfigLoader.load()
            AppDirectories.ensureCreated()
            AppDatabaseProvider.initialize(requireNotNull(appConfig))
            products = productRepository.findAll(ProductFilter())
            isInitialized = true
        }.onFailure { throwable ->
            AppDatabaseProvider.close()
            startupError = throwable.message ?: "Не удалось инициализировать приложение."
        }
    }

    fun retryInitialization() {
        isInitialized = false
        initialize()
    }

    fun dismissMessage() {
        uiMessage = null
    }

    fun login(login: String, password: String) {
        if (login.isBlank() || password.isBlank()) {
            showMessage(
                title = "Ошибка авторизации",
                text = "Введите логин и пароль.",
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            authRepository.authenticate(login.trim(), password)
        }.onSuccess { user ->
            if (user == null) {
                showMessage(
                    title = "Ошибка авторизации",
                    text = "Пользователь не найден или пароль введен неверно.",
                    kind = MessageKind.ERROR,
                )
                return@onSuccess
            }

            currentSession = user
            backStack.clear()
            currentScreen = AppScreen.Products
            productFilter = ProductFilter()
            refreshProducts()

            if (user.role.canViewOrders) {
                refreshOrders()
            }
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка авторизации",
                text = throwable.message ?: "Не удалось выполнить вход.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun continueAsGuest() {
        currentSession = UserSession.guest()
        backStack.clear()
        currentScreen = AppScreen.Products
        productFilter = ProductFilter()
        refreshProducts()
    }

    fun returnToLogin() {
        currentSession = UserSession.guest()
        currentScreen = AppScreen.Login
        backStack.clear()
        editorPayload = null
        productFilter = ProductFilter()
    }

    fun goBack() {
        if (currentScreen is AppScreen.ProductEditor) {
            editorPayload = null
        }

        if (backStack.isEmpty()) {
            returnToLogin()
            return
        }

        currentScreen = backStack.removeLast()
    }

    fun updateProductFilter(filter: ProductFilter) {
        productFilter = filter
        refreshProducts()
    }

    fun refreshProducts() {
        runCatching {
            products = productRepository.findAll(
                if (currentSession.role.canSearchProducts) {
                    productFilter
                } else {
                    ProductFilter()
                },
            )
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка загрузки товаров",
                text = throwable.message ?: "Не удалось получить список товаров.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun openOrders() {
        if (!currentSession.role.canViewOrders) {
            showMessage(
                title = "Недостаточно прав",
                text = "Просмотр заказов доступен только менеджеру и администратору.",
                kind = MessageKind.WARNING,
            )
            return
        }

        refreshOrders()
        navigateTo(AppScreen.Orders)
    }

    fun openProductEditor(productId: Int?) {
        if (!currentSession.role.canManageProducts) {
            showMessage(
                title = "Недостаточно прав",
                text = "Редактирование и добавление товаров доступно только администратору.",
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            val draft = if (productId == null) {
                ProductDraft(id = productRepository.nextId())
            } else {
                productRepository.findById(productId) ?: error("Товар не найден.")
            }

            editorPayload = ProductEditorPayload(
                draft = draft,
                categories = referenceRepository.loadCategories(),
                manufacturers = referenceRepository.loadManufacturers(),
                suppliers = referenceRepository.loadSuppliers(),
                units = referenceRepository.loadUnits(),
            )
            navigateTo(AppScreen.ProductEditor(productId))
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка открытия формы",
                text = throwable.message ?: "Не удалось открыть форму товара.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun saveProduct(form: ProductFormModel) {
        val validationErrors = ProductValidation.validate(form)
        if (validationErrors.isNotEmpty()) {
            showMessage(
                title = "Проверьте данные",
                text = validationErrors.joinToString("\n"),
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            val price = ProductValidation.parseDecimal(form.priceText) ?: error("Цена указана неверно.")
            val discount = ProductValidation.parseDecimal(form.discountText) ?: error("Скидка указана неверно.")
            val stock = form.stockQuantityText.trim().toIntOrNull() ?: error("Количество указано неверно.")

            val storedImagePath = if (form.selectedImageSourcePath != null) {
                ImageStorage.storeProductImage(
                    sourcePath = form.selectedImageSourcePath,
                    existingStoredPath = form.existingImagePath,
                    productId = form.id,
                )
            } else {
                form.existingImagePath
            }

            val draft = ProductDraft(
                id = form.id,
                name = form.name.trim(),
                categoryId = form.categoryId,
                description = form.description.trim(),
                manufacturerId = form.manufacturerId,
                supplierId = form.supplierId,
                unitId = form.unitId,
                price = price,
                stockQuantity = stock,
                discountPercent = discount,
                imagePath = storedImagePath,
            )

            productRepository.save(draft)
            editorPayload = null
            refreshProducts()
            goBack()
            showMessage(
                title = "Успешно",
                text = "Данные товара сохранены.",
                kind = MessageKind.INFO,
            )
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка сохранения",
                text = throwable.message ?: "Не удалось сохранить товар.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun deleteProduct(product: ProductListItem) {
        if (!currentSession.role.canManageProducts) {
            showMessage(
                title = "Недостаточно прав",
                text = "Удаление товаров доступно только администратору.",
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            when (productRepository.delete(product.id)) {
                DeleteProductResult.DELETED -> {
                    ImageStorage.deleteStoredImage(product.imagePath)
                    refreshProducts()
                    showMessage(
                        title = "Удаление выполнено",
                        text = "Товар удален.",
                        kind = MessageKind.INFO,
                    )
                }

                DeleteProductResult.LINKED_TO_ORDER -> {
                    showMessage(
                        title = "Удаление запрещено",
                        text = "Товар нельзя удалить, потому что он уже присутствует в заказе.",
                        kind = MessageKind.WARNING,
                    )
                }

                DeleteProductResult.NOT_FOUND -> {
                    showMessage(
                        title = "Товар не найден",
                        text = "Запись уже была удалена или отсутствует в базе данных.",
                        kind = MessageKind.WARNING,
                    )
                }
            }
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка удаления",
                text = throwable.message ?: "Не удалось удалить товар.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun shutdown() {
        AppDatabaseProvider.close()
    }

    private fun refreshOrders() {
        runCatching {
            orders = orderRepository.findAll()
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка загрузки заказов",
                text = throwable.message ?: "Не удалось получить список заказов.",
                kind = MessageKind.ERROR,
            )
        }
    }

    private fun navigateTo(screen: AppScreen) {
        backStack += currentScreen
        currentScreen = screen
    }

    private fun showMessage(title: String, text: String, kind: MessageKind) {
        uiMessage = UiMessage(
            title = title,
            text = text,
            kind = kind,
        )
    }
}
