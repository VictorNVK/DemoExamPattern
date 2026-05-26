package ru.demoexam.template.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import javax.imageio.ImageIO

object ImageStorage {
    fun storeProductImage(
        sourcePath: String,
        existingStoredPath: String?,
        productId: Int,
    ): String {
        AppDirectories.ensureCreated()

        val sourceFile = File(sourcePath)
        val sourceImage = ImageIO.read(sourceFile)
        val targetFileName = if (sourceImage != null) {
            "product_$productId.png"
        } else {
            val extension = sourceFile.extension.ifBlank { "img" }
            "product_$productId.$extension"
        }

        if (!existingStoredPath.isNullOrBlank() && existingStoredPath != targetFileName) {
            deleteStoredImage(existingStoredPath)
        }

        val targetFile = AppDirectories.imagesDirectory.resolve(targetFileName).toFile()

        if (sourceImage == null) {
            Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            return targetFileName
        }

        val resizedImage = BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()

        try {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
            graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            graphics.drawImage(sourceImage, 0, 0, 300, 200, null)
        } finally {
            graphics.dispose()
        }

        ImageIO.write(resizedImage, "png", targetFile)
        return targetFileName
    }

    fun deleteStoredImage(storedPath: String?) {
        if (storedPath.isNullOrBlank()) {
            return
        }

        resolveStoredImage(storedPath)?.takeIf(File::exists)?.delete()
    }

    fun resolveStoredImage(storedPath: String?): File? {
        if (storedPath.isNullOrBlank()) {
            return null
        }

        return AppDirectories.imagesDirectory.resolve(storedPath).toFile()
    }

    fun loadStoredBitmap(storedPath: String?): ImageBitmap? {
        val file = resolveStoredImage(storedPath) ?: return null
        return loadBitmapFromFile(file)
    }

    fun loadBitmapFromAbsolutePath(path: String?): ImageBitmap? {
        if (path.isNullOrBlank()) {
            return null
        }

        return loadBitmapFromFile(File(path))
    }

    private fun loadBitmapFromFile(file: File): ImageBitmap? {
        if (!file.exists()) {
            return null
        }

        return runCatching {
            Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap()
        }.getOrNull()
    }
}
