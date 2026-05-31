package ru.demoexam.template.model

enum class UserRole(
    val code: String,
    val title: String,
    val canSearchProducts: Boolean,
    val canViewOrders: Boolean,
    val canManageProducts: Boolean,
    val canManageOrders: Boolean,
) {
    GUEST("guest", "Гость", false, false, false, false),
    CLIENT("client", "Клиент", false, false, false, false),
    MANAGER("manager", "Менеджер", true, true, false, false),
    ADMIN("admin", "Администратор", true, true, true, true),
    ;

    companion object {
        fun fromCode(code: String): UserRole {
            return entries.firstOrNull { it.code == code } ?: CLIENT
        }
    }
}

