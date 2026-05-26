package ru.demoexam.template.navigation

sealed interface AppScreen {
    data object Login : AppScreen
    data object Products : AppScreen
    data object Orders : AppScreen
    data class ProductEditor(val productId: Int?) : AppScreen
}

