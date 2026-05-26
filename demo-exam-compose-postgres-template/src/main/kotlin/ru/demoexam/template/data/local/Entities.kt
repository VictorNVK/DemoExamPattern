package ru.demoexam.template.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "roles",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["name"], unique = true),
    ],
)
data class RoleEntity(
    @PrimaryKey
    val id: Int,
    val code: String,
    val name: String,
)

@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = RoleEntity::class,
            parentColumns = ["id"],
            childColumns = ["roleId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["login"], unique = true),
        Index(value = ["roleId"]),
    ],
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val login: String,
    val password: String,
    val roleId: Int,
    val isActive: Boolean = true,
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
)

@Entity(
    tableName = "manufacturers",
    indices = [Index(value = ["name"], unique = true)],
)
data class ManufacturerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
)

@Entity(
    tableName = "suppliers",
    indices = [Index(value = ["name"], unique = true)],
)
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
)

@Entity(
    tableName = "units",
    indices = [Index(value = ["name"], unique = true)],
)
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = ManufacturerEntity::class,
            parentColumns = ["id"],
            childColumns = ["manufacturerId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["article"], unique = true),
        Index(value = ["categoryId"]),
        Index(value = ["manufacturerId"]),
        Index(value = ["supplierId"]),
        Index(value = ["unitId"]),
    ],
)
data class ProductEntity(
    @PrimaryKey
    val id: Int,
    val article: String? = null,
    val name: String,
    val categoryId: Int,
    val description: String = "",
    val manufacturerId: Int,
    val supplierId: Int,
    val unitId: Int,
    val price: BigDecimal,
    val stockQuantity: Int,
    val discountPercent: BigDecimal = BigDecimal.ZERO,
    val imagePath: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fullName: String,
    val phone: String? = null,
    val email: String? = null,
)

@Entity(
    tableName = "pickup_points",
    indices = [Index(value = ["address"], unique = true)],
)
data class PickupPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val address: String,
)

@Entity(
    tableName = "order_statuses",
    indices = [Index(value = ["name"], unique = true)],
)
data class OrderStatusEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
)

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["managerId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = OrderStatusEntity::class,
            parentColumns = ["id"],
            childColumns = ["statusId"],
            onDelete = ForeignKey.RESTRICT,
        ),
        ForeignKey(
            entity = PickupPointEntity::class,
            parentColumns = ["id"],
            childColumns = ["pickupPointId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["managerId"]),
        Index(value = ["statusId"]),
        Index(value = ["pickupPointId"]),
    ],
)
data class OrderEntity(
    @PrimaryKey
    val id: Int,
    val customerId: Int? = null,
    val managerId: Int? = null,
    val statusId: Int,
    val orderDate: LocalDateTime = LocalDateTime.now(),
    val deliveryDate: LocalDateTime? = null,
    val pickupPointId: Int? = null,
    val pickupCode: String? = null,
    val comment: String = "",
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["productId"]),
    ],
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val discountPercent: BigDecimal = BigDecimal.ZERO,
)

