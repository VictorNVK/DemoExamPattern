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
                categories = options.categories,
                manufacturers = options.manufacturers,
                suppliers = options.suppliers,
                units = options.units,
            )
        }
    }
}
