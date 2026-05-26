package ru.demoexam.template.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import ru.demoexam.template.config.AppConfig
import ru.demoexam.template.data.local.AppDatabase
import ru.demoexam.template.util.AppDirectories

object AppDatabaseProvider {
    private var database: AppDatabase? = null

    fun initialize(config: AppConfig) {
        if (database != null) {
            return
        }

        AppDirectories.ensureCreated()
        val databaseFile = AppDirectories.databaseDirectory.resolve(config.databaseFileName).toFile()

        database = Room.databaseBuilder<AppDatabase>(
            name = databaseFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

        DatabaseSeeder.seedIfNeeded(requireNotNull(database))
    }

    fun getDatabase(): AppDatabase {
        return requireNotNull(database) {
            "Локальная база данных Room не инициализирована."
        }
    }

    fun close() {
        database?.close()
        database = null
    }
}
