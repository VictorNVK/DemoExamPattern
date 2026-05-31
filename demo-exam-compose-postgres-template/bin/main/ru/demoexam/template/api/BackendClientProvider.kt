package ru.demoexam.template.api

import ru.demoexam.template.config.BackendApiConfig

object BackendClientProvider {
    private var client: BackendClient? = null

    fun initialize(apiConfig: BackendApiConfig) {
        close()
        client = BackendClient(apiConfig)
    }

    fun getClient(): BackendClient {
        return requireNotNull(client) { "Backend client is not initialized." }
    }

    fun close() {
        client?.close()
        client = null
    }
}
