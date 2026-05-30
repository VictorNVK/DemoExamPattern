package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.api.BackendClientProvider
import ru.demoexam.template.model.DeleteProductResult
import ru.demoexam.template.model.ProductDraft
import ru.demoexam.template.model.ProductFilter
import ru.demoexam.template.model.ProductListItem

class ProductRepository {
    fun findAll(filter: ProductFilter): List<ProductListItem> {
        return runBlocking {
            BackendClientProvider.getClient().getProducts(filter)
        }
    }

    fun findById(productId: Int): ProductDraft? {
        return runBlocking {
            BackendClientProvider.getClient().getProduct(productId)
        }
    }

    fun nextId(): Int {
        return runBlocking {
            BackendClientProvider.getClient().nextProductId()
        }
    }

    fun save(product: ProductDraft, isNew: Boolean) {
        runBlocking {
            BackendClientProvider.getClient().saveProduct(product, isNew)
        }
    }

    fun saveWithImage(product: ProductDraft, isNew: Boolean, imageSourcePath: String?): ProductDraft {
        return runBlocking {
            val client = BackendClientProvider.getClient()
            var saved = client.saveProduct(product, isNew)
            if (!imageSourcePath.isNullOrBlank()) {
                saved = client.uploadProductImage(saved.id, imageSourcePath)
            }
            saved
        }
    }

    fun delete(productId: Int): DeleteProductResult {
        return runBlocking {
            when (BackendClientProvider.getClient().deleteProduct(productId)) {
                "LINKED_TO_ORDER" -> DeleteProductResult.LINKED_TO_ORDER
                "NOT_FOUND" -> DeleteProductResult.NOT_FOUND
                else -> DeleteProductResult.DELETED
            }
        }
    }
}
