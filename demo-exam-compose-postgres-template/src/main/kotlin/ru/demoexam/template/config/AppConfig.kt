package ru.demoexam.template.config

import java.util.Properties

data class AppConfig(
    val applicationTitle: String,
    val companyName: String,
    val databaseFileName: String,
)

object AppConfigLoader {
    fun load(): AppConfig {
        val properties = Properties()
        AppConfigLoader::class.java.getResourceAsStream("/application.properties")?.use(properties::load)

        return AppConfig(
            applicationTitle = readValue(properties, "app.title", "APP_TITLE", "Book Store Demo Template"),
            companyName = readValue(properties, "app.company_name", "APP_COMPANY_NAME", "Book Store"),
            databaseFileName = readValue(
                properties,
                "db.file_name",
                "DB_FILE_NAME",
                "book_store_exam.db",
            ).ifBlank { "book_store_exam.db" },
        )
    }

    private fun readValue(
        properties: Properties,
        propertyName: String,
        environmentName: String,
        defaultValue: String,
    ): String {
        val environmentValue = System.getenv(environmentName)
        if (!environmentValue.isNullOrBlank()) {
            return environmentValue
        }

        return properties.getProperty(propertyName)?.takeIf { it.isNotBlank() } ?: defaultValue
    }
}
