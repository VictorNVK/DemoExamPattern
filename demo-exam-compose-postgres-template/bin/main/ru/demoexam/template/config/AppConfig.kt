package ru.demoexam.template.config

import java.util.Properties

data class AppConfig(
    val applicationTitle: String,
    val companyName: String,
    val backendApi: BackendApiConfig,
)

object AppConfigLoader {
    fun load(): AppConfig {
        val properties = Properties()
        AppConfigLoader::class.java.getResourceAsStream("/application.properties")?.use(properties::load)

        return AppConfig(
            applicationTitle = ConfigProperties.readValue(
                properties,
                "app.title",
                "APP_TITLE",
                "Book Store Demo Template",
            ),
            companyName = ConfigProperties.readValue(
                properties,
                "app.company_name",
                "APP_COMPANY_NAME",
                "Book Store",
            ),
            backendApi = BackendApiConfig.fromProperties(properties),
        )
    }
}
