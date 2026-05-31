package ru.demoexam.template.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.skia.Image
import ru.demoexam.template.api.BackendClientProvider
import java.io.File

object ImageStorage {
    suspend fun loadStoredBitmapAsync(storedPath: String?): ImageBitmap? {
        if (storedPath.isNullOrBlank()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            val bytes = runCatching {
                BackendClientProvider.getClient().downloadImage(storedPath)
            }.getOrNull() ?: return@withContext null

            runCatching {
                Image.makeFromEncoded(bytes).toComposeImageBitmap()
            }.getOrNull()
        }
    }

    fun loadStoredBitmap(storedPath: String?): ImageBitmap? {
        if (storedPath.isNullOrBlank()) {
            return null
        }

        return runCatching {
            val bytes = kotlinx.coroutines.runBlocking {
                BackendClientProvider.getClient().downloadImage(storedPath)
            } ?: return null

            Image.makeFromEncoded(bytes).toComposeImageBitmap()
        }.getOrNull()
    }

    fun loadBitmapFromAbsolutePath(path: String?): ImageBitmap? {
        if (path.isNullOrBlank()) {
            return null
        }

        return runCatching {
            Image.makeFromEncoded(File(path).readBytes()).toComposeImageBitmap()
        }.getOrNull()
    }
}
