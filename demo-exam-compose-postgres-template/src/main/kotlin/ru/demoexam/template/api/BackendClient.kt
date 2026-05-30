package ru.demoexam.template.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import ru.demoexam.template.config.BackendApiConfig
import ru.demoexam.template.model.OrderSummary
import ru.demoexam.template.model.ProductDraft
import ru.demoexam.template.model.ProductFilter
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.model.UserRole
import ru.demoexam.template.model.UserSession
import java.io.File
import java.math.BigDecimal
import java.time.LocalDateTime

@Serializable
data class LoginRequestDto(val login: String, val password: String)

@Serializable
data class LoginResponseDto(
    val token: String,
    val userId: Int,
    val fullName: String,
    val role: String,
)

@Serializable
data class ProductOptionsDto(
    val categories: List<String> = emptyList(),
    val manufacturers: List<String> = emptyList(),
    val suppliers: List<String> = emptyList(),
    val units: List<String> = emptyList(),
)

@Serializable
data class ProductDraftDto(
    val id: Int,
    val name: String,
    val category: String? = null,
    val description: String = "",
    val manufacturer: String? = null,
    val supplier: String? = null,
    val unit: String? = null,
    val price: Double? = null,
    val stockQuantity: Int? = null,
    val discountPercent: Double? = null,
    val imagePath: String? = null,
)

@Serializable
data class ProductListItemDto(
    val id: Int,
    val name: String,
    val categoryName: String,
    val description: String,
    val manufacturerName: String,
    val supplierName: String,
    val unitName: String,
    val price: Double,
    val stockQuantity: Int,
    val discountPercent: Double,
    val imagePath: String? = null,
)

@Serializable
data class OrderSummaryDto(
    val id: Int,
    val customerName: String,
    val managerName: String,
    val statusName: String,
    val orderDate: String,
    val comment: String,
    val itemsCount: Int,
    val totalAmount: Double,
)

@Serializable
data class DeleteErrorDto(val reason: String? = null)

class BackendClient(
    private val apiConfig: BackendApiConfig,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    var authToken: String? = null
        private set

    suspend fun checkHealth() {
        getProducts(ProductFilter())
    }

    suspend fun login(login: String, password: String): UserSession {
        val response = httpClient.post(apiConfig.authLoginUrl) {
            contentType(ContentType.Application.Json)
            setBody(LoginRequestDto(login, password))
        }.body<LoginResponseDto>()

        authToken = response.token
        return UserSession(
            userId = response.userId,
            fullName = response.fullName,
            role = UserRole.fromCode(response.role),
        )
    }

    suspend fun getProducts(filter: ProductFilter): List<ProductListItem> {
        return httpClient.get(apiConfig.productsUrl) {
            url {
                parameters.append("search", filter.searchText)
                parameters.append("discountFilter", filter.discountFilter.name)
                parameters.append("sortField", filter.sortField.name)
                parameters.append("sortDirection", filter.sortDirection.name)
            }
        }.body<List<ProductListItemDto>>().map { it.toModel() }
    }

    suspend fun getProduct(productId: Int): ProductDraft? {
        val response = httpClient.get(apiConfig.productUrl(productId)) {
            authHeader()
        }
        return if (response.status.isSuccess()) {
            response.body<ProductDraftDto>().toModel()
        } else {
            null
        }
    }

    suspend fun nextProductId(): Int {
        return httpClient.get(apiConfig.productsNextIdUrl) {
            authHeader()
        }.body<Int>()
    }

    suspend fun saveProduct(draft: ProductDraft, isNew: Boolean): ProductDraft {
        val payload = draft.toDto()
        val saved = if (isNew) {
            httpClient.post(apiConfig.productsUrl) {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(payload)
            }.body<ProductDraftDto>()
        } else {
            httpClient.put(apiConfig.productUrl(draft.id)) {
                authHeader()
                contentType(ContentType.Application.Json)
                setBody(payload)
            }.body<ProductDraftDto>()
        }
        return saved.toModel()
    }

    suspend fun uploadProductImage(productId: Int, sourcePath: String): ProductDraft {
        val bytes = File(sourcePath).readBytes()
        val fileName = File(sourcePath).name

        val response = httpClient.submitFormWithBinaryData(
            url = apiConfig.productImageUploadUrl(productId),
            formData = formData {
                append(
                    "file",
                    bytes,
                    Headers.build {
                        append(HttpHeaders.ContentType, "application/octet-stream")
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    },
                )
            },
        ) {
            authHeader()
        }

        return response.body<ProductDraftDto>().toModel()
    }

    suspend fun deleteProduct(productId: Int): String? {
        val response = httpClient.delete(apiConfig.productUrl(productId)) {
            authHeader()
        }
        return when (response.status.value) {
            409 -> response.body<DeleteErrorDto>().reason ?: "LINKED_TO_ORDER"
            404 -> "NOT_FOUND"
            else -> null
        }
    }

    suspend fun getOrders(): List<OrderSummary> {
        return httpClient.get(apiConfig.ordersUrl) {
            authHeader()
        }.body<List<OrderSummaryDto>>().map { it.toModel() }
    }

    suspend fun getProductOptions(): ProductOptionsDto {
        return httpClient.get(apiConfig.productsOptionsUrl) {
            authHeader()
        }.body<ProductOptionsDto>()
    }

    suspend fun downloadImage(relativePath: String): ByteArray? {
        return runCatching {
            httpClient.get(apiConfig.fileImageUrl(relativePath)).bodyAsBytes()
        }.getOrNull()
    }

    fun imageUrl(relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) {
            return null
        }
        return apiConfig.fileImageUrl(relativePath)
    }

    fun clearAuth() {
        authToken = null
    }

    fun close() {
        httpClient.close()
    }

    private fun io.ktor.client.request.HttpRequestBuilder.authHeader() {
        authToken?.let { header("Authorization", "Bearer $it") }
    }

    private fun ProductListItemDto.toModel() = ProductListItem(
        id = id,
        name = name,
        categoryName = categoryName,
        description = description,
        manufacturerName = manufacturerName,
        supplierName = supplierName,
        unitName = unitName,
        price = BigDecimal.valueOf(price),
        stockQuantity = stockQuantity,
        discountPercent = BigDecimal.valueOf(discountPercent),
        imagePath = imagePath,
    )

    private fun ProductDraftDto.toModel() = ProductDraft(
        id = id,
        name = name,
        category = category.orEmpty(),
        description = description,
        manufacturer = manufacturer.orEmpty(),
        supplier = supplier.orEmpty(),
        unit = unit.orEmpty(),
        price = price?.let { BigDecimal.valueOf(it) },
        stockQuantity = stockQuantity,
        discountPercent = discountPercent?.let { BigDecimal.valueOf(it) },
        imagePath = imagePath,
    )

    private fun ProductDraft.toDto() = ProductDraftDto(
        id = id,
        name = name,
        category = category.ifBlank { null },
        description = description,
        manufacturer = manufacturer.ifBlank { null },
        supplier = supplier.ifBlank { null },
        unit = unit.ifBlank { null },
        price = price?.toDouble(),
        stockQuantity = stockQuantity,
        discountPercent = discountPercent?.toDouble(),
        imagePath = imagePath,
    )

    private fun OrderSummaryDto.toModel() = OrderSummary(
        id = id,
        customerName = customerName,
        managerName = managerName,
        statusName = statusName,
        orderDate = LocalDateTime.parse(orderDate),
        comment = comment,
        itemsCount = itemsCount,
        totalAmount = BigDecimal.valueOf(totalAmount),
    )
}
