package ru.demoexam.template.model

import java.math.BigDecimal

enum class DiscountFilter(val title: String) {
    ALL("Все диапазоны"),
    UP_TO_12_99("0-12,99%"),
    FROM_13_TO_16_99("13-16,99%"),
    FROM_17("17% и более"),
}

enum class SortField(val title: String) {
    NONE("Без сортировки"),
    PRICE("Цена"),
    STOCK("Остаток"),
}

enum class SortDirection(val title: String) {
    ASC("По возрастанию"),
    DESC("По убыванию"),
}

data class ProductFilter(
    val searchText: String = "",
    val supplierFilter: String = "ALL",
    val discountFilter: DiscountFilter = DiscountFilter.ALL,
    val sortField: SortField = SortField.NONE,
    val sortDirection: SortDirection = SortDirection.ASC,
)

data class ProductListItem(
    val id: Int,
    val article: String,
    val name: String,
    val categoryName: String,
    val description: String,
    val manufacturerName: String,
    val supplierName: String,
    val unitName: String,
    val price: BigDecimal,
    val stockQuantity: Int,
    val discountPercent: BigDecimal,
    val imagePath: String?,
) {
    val finalPrice: BigDecimal
        get() = price.multiply(BigDecimal.ONE.subtract(discountPercent.divide(BigDecimal(100))))
}

data class ProductDraft(
    val id: Int,
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val manufacturer: String = "",
    val supplier: String = "",
    val unit: String = "",
    val price: BigDecimal? = null,
    val stockQuantity: Int? = null,
    val discountPercent: BigDecimal? = null,
    val imagePath: String? = null,
)

data class ProductFormModel(
    val id: Int,
    val name: String = "",
    val category: String = "",
    val description: String = "",
    val manufacturer: String = "",
    val supplier: String = "",
    val unit: String = "",
    val priceText: String = "",
    val stockQuantityText: String = "",
    val discountText: String = "",
    val existingImagePath: String? = null,
    val selectedImageSourcePath: String? = null,
)

data class ProductEditorPayload(
    val draft: ProductDraft,
    val categories: List<String>,
    val manufacturers: List<String>,
    val suppliers: List<String>,
    val units: List<String>,
)

enum class DeleteProductResult {
    DELETED,
    LINKED_TO_ORDER,
    NOT_FOUND,
}
