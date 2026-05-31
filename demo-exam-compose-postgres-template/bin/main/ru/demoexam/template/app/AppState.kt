package ru.demoexam.template.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.runBlocking
import ru.demoexam.template.api.BackendClientProvider
import ru.demoexam.template.config.AppConfig
import ru.demoexam.template.config.AppConfigLoader
import ru.demoexam.template.data.AuthRepository
import ru.demoexam.template.data.OrderRepository
import ru.demoexam.template.data.ProductOptionsRepository
import ru.demoexam.template.data.ProductRepository
import ru.demoexam.template.model.DeleteProductResult
import ru.demoexam.template.model.MessageKind
import ru.demoexam.template.model.OrderDetail
import ru.demoexam.template.model.OrderEditorPayload
import ru.demoexam.template.model.OrderFormModel
import ru.demoexam.template.model.OrderSummary
import ru.demoexam.template.model.toOrderDetail
import ru.demoexam.template.model.ProductDraft
import ru.demoexam.template.model.ProductEditorPayload
import ru.demoexam.template.model.ProductFilter
import ru.demoexam.template.model.ProductFormModel
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.model.UiMessage
import ru.demoexam.template.model.UserSession
import ru.demoexam.template.navigation.AppScreen
import ru.demoexam.template.util.OrderValidation
import ru.demoexam.template.util.ProductValidation
import java.math.BigDecimal
import java.time.LocalDateTime

class AppState(
    private val authRepository: AuthRepository = AuthRepository(),
    private val productOptionsRepository: ProductOptionsRepository = ProductOptionsRepository(),
    private val productRepository: ProductRepository = ProductRepository(),
    private val orderRepository: OrderRepository = OrderRepository(),
) {
    private val backStack = mutableStateListOf<AppScreen>()
    private var appConfig: AppConfig? = null
    private var editingExistingProduct = false
    private var editingExistingOrder = false

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

    var orderEditorPayload by mutableStateOf<OrderEditorPayload?>(null)
        private set

    var supplierOptions by mutableStateOf<List<String>>(emptyList())
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
            is AppScreen.OrderEditor -> "Заказ"
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
            BackendClientProvider.initialize(requireNotNull(appConfig).backendApi)
            runBlocking {
                BackendClientProvider.getClient().checkHealth()
            }
            products = productRepository.findAll(ProductFilter())
            isInitialized = true
        }.onFailure { throwable ->
            BackendClientProvider.close()
            startupError = throwable.message ?: "Не удалось подключиться к Spring backend."
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

            if (user.role.canSearchProducts) {
                loadSupplierOptions()
            }
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
        orderEditorPayload = null
        supplierOptions = emptyList()
        productFilter = ProductFilter()
        BackendClientProvider.getClient().clearAuth()
    }

    fun goBack() {
        if (currentScreen is AppScreen.ProductEditor) {
            editorPayload = null
        }
        if (currentScreen is AppScreen.OrderEditor) {
            orderEditorPayload = null
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

    fun loadSupplierOptions() {
        if (!currentSession.role.canSearchProducts) {
            return
        }

        runCatching {
            supplierOptions = productRepository.loadSupplierOptions()
        }.onFailure {
            supplierOptions = emptyList()
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
            editingExistingProduct = productId != null
            val draft = if (productId == null) {
                ProductDraft(id = productRepository.nextId())
            } else {
                productRepository.findById(productId) ?: error("Товар не найден.")
            }

            editorPayload = productOptionsRepository.loadEditorPayload(draft)
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

            val draft = ProductDraft(
                id = form.id,
                name = form.name.trim(),
                category = form.category.trim(),
                description = form.description.trim(),
                manufacturer = form.manufacturer.trim(),
                supplier = form.supplier.trim(),
                unit = form.unit.trim(),
                price = price,
                stockQuantity = stock,
                discountPercent = discount,
                imagePath = form.existingImagePath,
            )

            productRepository.saveWithImage(
                product = draft,
                isNew = !editingExistingProduct,
                imageSourcePath = form.selectedImageSourcePath,
            )

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

    fun openOrderEditor(orderId: Int?) {
        if (!currentSession.role.canManageOrders) {
            showMessage(
                title = "Недостаточно прав",
                text = "Редактирование заказов доступно только администратору.",
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            editingExistingOrder = orderId != null
            val draft = if (orderId == null) {
                OrderDetail(
                    id = orderRepository.nextId(),
                    customerName = "",
                    managerId = 0,
                    managerName = "",
                    status = "Новый",
                    pickupAddress = "",
                    orderDate = LocalDateTime.now(),
                    deliveryDate = null,
                    pickupCode = "",
                    comment = "",
                    productId = 0,
                    productArticle = "",
                    productName = "",
                    quantity = 1,
                    unitPrice = BigDecimal.ZERO,
                    discountPercent = BigDecimal.ZERO,
                    totalAmount = BigDecimal.ZERO,
                )
            } else {
                orderRepository.findById(orderId) ?: error("Заказ не найден.")
            }

            orderEditorPayload = orderRepository.loadEditorPayload(draft)
            navigateTo(AppScreen.OrderEditor(orderId))
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка открытия формы",
                text = throwable.message ?: "Не удалось открыть форму заказа.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun orderProductPrices(productId: Int): Pair<String, String>? {
        return runCatching {
            val product = productRepository.findById(productId) ?: return null
            val price = product.price?.stripTrailingZeros()?.toPlainString().orEmpty()
            val discount = product.discountPercent?.stripTrailingZeros()?.toPlainString() ?: "0"
            price to discount
        }.getOrNull()
    }

    fun saveOrder(form: OrderFormModel) {
        val validationErrors = OrderValidation.validate(form)
        if (validationErrors.isNotEmpty()) {
            showMessage(
                title = "Проверьте данные",
                text = validationErrors.joinToString("\n"),
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            val draft = form.toOrderDetail()
            orderRepository.save(draft, isNew = !editingExistingOrder)
            orderEditorPayload = null
            refreshOrders()
            goBack()
            showMessage(
                title = "Успешно",
                text = "Данные заказа сохранены.",
                kind = MessageKind.INFO,
            )
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка сохранения",
                text = throwable.message ?: "Не удалось сохранить заказ.",
                kind = MessageKind.ERROR,
            )
        }
    }

    fun deleteOrder(order: OrderSummary) {
        if (!currentSession.role.canManageOrders) {
            showMessage(
                title = "Недостаточно прав",
                text = "Удаление заказов доступно только администратору.",
                kind = MessageKind.WARNING,
            )
            return
        }

        runCatching {
            if (orderRepository.delete(order.id)) {
                refreshOrders()
                showMessage(
                    title = "Удаление выполнено",
                    text = "Заказ удален.",
                    kind = MessageKind.INFO,
                )
            } else {
                showMessage(
                    title = "Ошибка удаления",
                    text = "Не удалось удалить заказ.",
                    kind = MessageKind.ERROR,
                )
            }
        }.onFailure { throwable ->
            showMessage(
                title = "Ошибка удаления",
                text = throwable.message ?: "Не удалось удалить заказ.",
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
        BackendClientProvider.close()
    }

    fun refreshOrders() {
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
