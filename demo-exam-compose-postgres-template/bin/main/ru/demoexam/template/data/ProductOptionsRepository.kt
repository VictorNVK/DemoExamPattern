package ru.demoexam.template.data

import kotlinx.coroutines.runBlocking
import ru.demoexam.template.api.BackendClientProvider
import ru.demoexam.template.model.ProductDraft
import ru.demoexam.template.model.ProductEditorPayload

class ProductOptionsRepository {
    fun loadEditorPayload(draft: ProductDraft): ProductEditorPayload {
        return runBlocking {
            val options = BackendClientProvider.getClient().getProductOptions()
            ProductEditorPayload(
                draft = draft,
                categories = mergeOptions(options.categories, draft.category),
                manufacturers = mergeOptions(options.manufacturers, draft.manufacturer),
                suppliers = mergeOptions(options.suppliers, draft.supplier),
                units = mergeOptions(options.units, draft.unit),
            )
        }
    }

    private fun mergeOptions(options: List<String>, currentValue: String): List<String> {
        val normalized = options.filter { it.isNotBlank() }.distinct()
        if (currentValue.isBlank()) {
            return normalized
        }
        return (listOf(currentValue) + normalized).distinct()
    }
}
