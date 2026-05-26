package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.model.UserRole
import ru.demoexam.template.model.UserSession

class AuthRepository {
    fun authenticate(login: String, password: String): UserSession? {
        return runBlocking {
            AppDatabaseProvider.getDatabase()
                .authDao()
                .findAuthorizedUser(login = login, password = password)
                ?.let { authorizedUser ->
                    UserSession(
                        userId = authorizedUser.user.id,
                        fullName = authorizedUser.user.fullName,
                        role = UserRole.fromCode(authorizedUser.role.code),
                    )
                }
        }
    }
}

