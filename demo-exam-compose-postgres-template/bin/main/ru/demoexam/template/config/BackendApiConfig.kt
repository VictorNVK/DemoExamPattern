package ru.demoexam.template.config

import java.util.Properties

/**
 * Адреса REST API Spring backend.
 * Базовый URL и пути задаются в application.properties (префикс api.*).
 */
data class BackendApiConfig(
    val baseUrl: String,
    val authLoginPath: String,
    val productsPath: String,
    val productsOptionsPath: String,
    val productsFilterOptionsPath: String,
    val productsNextIdPath: String,
    val ordersPath: String,
    val ordersOptionsPath: String,
    val ordersNextIdPath: String,
    val filesImagesPath: String,
) {
    val authLoginUrl: String
        get() = absolute(authLoginPath)

    val productsUrl: String
        get() = absolute(productsPath)

    val productsOptionsUrl: String
        get() = absolute(productsOptionsPath)

    val productsFilterOptionsUrl: String
        get() = absolute(productsFilterOptionsPath)

    val productsNextIdUrl: String
        get() = absolute(productsNextIdPath)

    val ordersUrl: String
        get() = absolute(ordersPath)

    val ordersOptionsUrl: String
        get() = absolute(ordersOptionsPath)

    val ordersNextIdUrl: String
        get() = absolute(ordersNextIdPath)

    fun orderUrl(orderId: Int): String = absolute("$ordersPath/$orderId")

    fun productUrl(productId: Int): String = absolute("$productsPath/$productId")

    fun productImageUploadUrl(productId: Int): String = absolute("$productsPath/$productId/image")

    fun fileImageUrl(relativePath: String): String = absolute("$filesImagesPath/$relativePath")

    private fun absolute(path: String): String {
        val normalizedPath = if (path.startsWith("/")) path else "/$path"
        return baseUrl.trimEnd('/') + normalizedPath
    }

    companion object {
        fun fromProperties(properties: Properties): BackendApiConfig {
            return BackendApiConfig(
                baseUrl = ConfigProperties.readValue(
                    properties,
                    "api.base_url",
                    "API_BASE_URL",
                    "http://localhost:8082",
                ).trimEnd('/'),
                authLoginPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.auth.login",
                    "API_ENDPOINT_AUTH_LOGIN",
                    "/api/auth/login",
                ),
                productsPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.products",
                    "API_ENDPOINT_PRODUCTS",
                    "/api/products",
                ),
                productsOptionsPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.products.options",
                    "API_ENDPOINT_PRODUCTS_OPTIONS",
                    "/api/products/options",
                ),
                productsFilterOptionsPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.products.filter_options",
                    "API_ENDPOINT_PRODUCTS_FILTER_OPTIONS",
                    "/api/products/filter-options",
                ),
                productsNextIdPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.products.next_id",
                    "API_ENDPOINT_PRODUCTS_NEXT_ID",
                    "/api/products/next-id",
                ),
                ordersPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.orders",
                    "API_ENDPOINT_ORDERS",
                    "/api/orders",
                ),
                ordersOptionsPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.orders.options",
                    "API_ENDPOINT_ORDERS_OPTIONS",
                    "/api/orders/options",
                ),
                ordersNextIdPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.orders.next_id",
                    "API_ENDPOINT_ORDERS_NEXT_ID",
                    "/api/orders/next-id",
                ),
                filesImagesPath = ConfigProperties.readValue(
                    properties,
                    "api.endpoint.files.images",
                    "API_ENDPOINT_FILES_IMAGES",
                    "/api/files/images",
                ),
            )
        }
    }
}
