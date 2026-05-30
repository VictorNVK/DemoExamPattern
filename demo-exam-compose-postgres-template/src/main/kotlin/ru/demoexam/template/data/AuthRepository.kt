package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.api.BackendClientProvider
import ru.demoexam.template.model.UserSession

class AuthRepository {
    fun authenticate(login: String, password: String): UserSession? {
        return runBlocking {
            runCatching {
                BackendClientProvider.getClient().login(login, password)
            }.getOrNull()
        }
    }
}
