package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.data.local.ProductEntity
import ru.demoexam.template.data.local.ProductWithDetails
import ru.demoexam.template.model.DeleteProductResult
import ru.demoexam.template.model.DiscountFilter
import ru.demoexam.template.model.ProductDraft
import ru.demoexam.template.model.ProductFilter
import ru.demoexam.template.model.ProductListItem
import ru.demoexam.template.model.SortDirection
import ru.demoexam.template.model.SortField
import java.math.BigDecimal
import java.time.LocalDateTime

class ProductRepository {
    fun findAll(filter: ProductFilter): List<ProductListItem> {
        return runBlocking {
            AppDatabaseProvider.getDatabase()
                .productDao()
                .getAllProductsWithDetails()
        }
            .map { it.toListItem() }
            .filterBySearch(filter.searchText)
            .filterByDiscount(filter.discountFilter)
            .sortByFilter(filter)
    }

    fun findById(productId: Int): ProductDraft? {
        return runBlocking {
            AppDatabaseProvider.getDatabase()
                .productDao()
                .getProductById(productId)
                ?.toDraft()
        }
    }

    fun nextId(): Int {
        return runBlocking {
            (AppDatabaseProvider.getDatabase().productDao().findMaxProductId() ?: 1000) + 1
        }
    }

    fun save(product: ProductDraft) {
        require(product.categoryId != null)
        require(product.manufacturerId != null)
        require(product.supplierId != null)
        require(product.unitId != null)
        require(product.price != null)
        require(product.stockQuantity != null)
        require(product.discountPercent != null)

        runBlocking {
            val productDao = AppDatabaseProvider.getDatabase().productDao()
            val now = LocalDateTime.now()
            val existingProduct = productDao.getProductById(product.id)

            val entity = ProductEntity(
                id = product.id,
                article = existingProduct?.article,
                name = product.name,
                categoryId = product.categoryId,
                description = product.description,
                manufacturerId = product.manufacturerId,
                supplierId = product.supplierId,
                unitId = product.unitId,
                price = product.price,
                stockQuantity = product.stockQuantity,
                discountPercent = product.discountPercent,
                imagePath = product.imagePath,
                createdAt = existingProduct?.createdAt ?: now,
                updatedAt = now,
            )

            if (existingProduct == null) {
                productDao.insertProduct(entity)
            } else {
                productDao.updateProduct(entity)
            }
        }
    }

    fun delete(productId: Int): DeleteProductResult {
        return runBlocking {
            val productDao = AppDatabaseProvider.getDatabase().productDao()
            if (productDao.countOrderItemsByProductId(productId) > 0) {
                return@runBlocking DeleteProductResult.LINKED_TO_ORDER
            }

            val existingProduct = productDao.getProductById(productId)
                ?: return@runBlocking DeleteProductResult.NOT_FOUND

            productDao.deleteProduct(existingProduct)
            DeleteProductResult.DELETED
        }
    }

    private fun ProductWithDetails.toListItem(): ProductListItem {
        return ProductListItem(
            id = product.id,
            name = product.name,
            categoryName = category.name,
            description = product.description,
            manufacturerName = manufacturer.name,
            supplierName = supplier.name,
            unitName = unit.name,
            price = product.price,
            stockQuantity = product.stockQuantity,
            discountPercent = product.discountPercent,
            imagePath = product.imagePath,
        )
    }

    private fun ProductEntity.toDraft(): ProductDraft {
        return ProductDraft(
            id = id,
            name = name,
            categoryId = categoryId,
            description = description,
            manufacturerId = manufacturerId,
            supplierId = supplierId,
            unitId = unitId,
            price = price,
            stockQuantity = stockQuantity,
            discountPercent = discountPercent,
            imagePath = imagePath,
        )
    }

    private fun List<ProductListItem>.filterBySearch(searchText: String): List<ProductListItem> {
        val normalizedSearch = searchText.trim()
        if (normalizedSearch.isBlank()) {
            return this
        }

        return filter { product ->
            listOf(
                product.name,
                product.categoryName,
                product.description,
                product.manufacturerName,
                product.supplierName,
                product.unitName,
            ).any { fieldValue ->
                fieldValue.contains(normalizedSearch, ignoreCase = true)
            }
        }
    }

    private fun List<ProductListItem>.filterByDiscount(discountFilter: DiscountFilter): List<ProductListItem> {
        return when (discountFilter) {
            DiscountFilter.ALL -> this
            DiscountFilter.UP_TO_12_99 -> filter { it.discountPercent >= BigDecimal.ZERO && it.discountPercent < BigDecimal("13") }
            DiscountFilter.FROM_13_TO_16_99 -> filter { it.discountPercent >= BigDecimal("13") && it.discountPercent < BigDecimal("17") }
            DiscountFilter.FROM_17 -> filter { it.discountPercent >= BigDecimal("17") }
        }
    }

    private fun List<ProductListItem>.sortByFilter(filter: ProductFilter): List<ProductListItem> {
        val comparator = when (filter.sortField) {
            SortField.NONE -> compareBy<ProductListItem> { it.id }.thenBy { it.name.lowercase() }
            SortField.PRICE -> compareBy<ProductListItem> { it.price }.thenBy { it.name.lowercase() }
            SortField.STOCK -> compareBy<ProductListItem> { it.stockQuantity }.thenBy { it.name.lowercase() }
        }

        val sortedItems = sortedWith(comparator)
        return if (filter.sortField == SortField.NONE || filter.sortDirection == SortDirection.ASC) {
            sortedItems
        } else {
            sortedItems.reversed()
        }
    }
}
