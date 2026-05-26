package ru.demoexam.template.model

enum class UserRole(
    val code: String,
    val title: String,
    val canSearchProducts: Boolean,
    val canViewOrders: Boolean,
    val canManageProducts: Boolean,
) {
    GUEST("guest", "Гость", false, false, false),
    CLIENT("client", "Клиент", false, false, false),
    MANAGER("manager", "Менеджер", true, true, false),
    ADMIN("admin", "Администратор", true, true, true),
    ;

    companion object {
        fun fromCode(code: String): UserRole {
            return entries.firstOrNull { it.code == code } ?: CLIENT
        }
    }
}

