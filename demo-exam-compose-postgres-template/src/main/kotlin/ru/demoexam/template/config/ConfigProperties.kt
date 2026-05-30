package ru.demoexam.template.config

import java.util.Properties

internal object ConfigProperties {
    fun readValue(
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
