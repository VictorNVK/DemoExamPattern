package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.model.LookupItem

class ReferenceRepository {
    fun loadCategories(): List<LookupItem> = runBlocking {
        AppDatabaseProvider.getDatabase()
            .lookupDao()
            .getAllCategories()
            .map { LookupItem(id = it.id, name = it.name) }
    }

    fun loadManufacturers(): List<LookupItem> = runBlocking {
        AppDatabaseProvider.getDatabase()
            .lookupDao()
            .getAllManufacturers()
            .map { LookupItem(id = it.id, name = it.name) }
    }

    fun loadSuppliers(): List<LookupItem> = runBlocking {
        AppDatabaseProvider.getDatabase()
            .lookupDao()
            .getAllSuppliers()
            .map { LookupItem(id = it.id, name = it.name) }
    }

    fun loadUnits(): List<LookupItem> = runBlocking {
        AppDatabaseProvider.getDatabase()
            .lookupDao()
            .getAllUnits()
            .map { LookupItem(id = it.id, name = it.name) }
    }
}

