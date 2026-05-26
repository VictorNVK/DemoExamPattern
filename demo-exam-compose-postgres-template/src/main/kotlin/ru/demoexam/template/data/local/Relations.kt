package ru.demoexam.template.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class UserWithRole(
    @Embedded
    val user: UserEntity,
    @Relation(parentColumn = "roleId", entityColumn = "id")
    val role: RoleEntity,
)

data class ProductWithDetails(
    @Embedded
    val product: ProductEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity,
    @Relation(parentColumn = "manufacturerId", entityColumn = "id")
    val manufacturer: ManufacturerEntity,
    @Relation(parentColumn = "supplierId", entityColumn = "id")
    val supplier: SupplierEntity,
    @Relation(parentColumn = "unitId", entityColumn = "id")
    val unit: UnitEntity,
)

data class OrderWithDetails(
    @Embedded
    val order: OrderEntity,
    @Relation(parentColumn = "customerId", entityColumn = "id")
    val customer: CustomerEntity?,
    @Relation(parentColumn = "managerId", entityColumn = "id")
    val manager: UserEntity?,
    @Relation(parentColumn = "statusId", entityColumn = "id")
    val status: OrderStatusEntity,
    @Relation(parentColumn = "pickupPointId", entityColumn = "id")
    val pickupPoint: PickupPointEntity?,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<OrderItemEntity>,
)

