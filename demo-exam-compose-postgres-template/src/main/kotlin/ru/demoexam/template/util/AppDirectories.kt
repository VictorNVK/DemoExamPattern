package ru.demoexam.template.util

import java.nio.file.Files
import java.nio.file.Path

object AppDirectories {
    private val applicationRoot: Path = Path.of(System.getProperty("user.dir"))
    val storageDirectory: Path = applicationRoot.resolve("storage")
    val databaseDirectory: Path = storageDirectory.resolve("database")
    val imagesDirectory: Path = storageDirectory.resolve("images")

    fun ensureCreated() {
        Files.createDirectories(databaseDirectory)
        Files.createDirectories(imagesDirectory)
    }
}
