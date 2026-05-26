package ru.demoexam.template.model

data class UserSession(
    val userId: Int?,
    val fullName: String,
    val role: UserRole,
) {
    companion object {
        fun guest(): UserSession {
            return UserSession(
                userId = null,
                fullName = "Гость",
                role = UserRole.GUEST,
            )
        }
    }
}

