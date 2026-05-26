package ru.demoexam.template.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

@Dao
interface AuthDao {
    @Transaction
    @Query(
        """
        SELECT * FROM users
        WHERE login = :login
          AND password = :password
          AND isActive = 1
        LIMIT 1
        """,
    )
    suspend fun findAuthorizedUser(login: String, password: String): UserWithRole?
}

@Dao
interface LookupDao {
    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM manufacturers ORDER BY name")
    suspend fun getAllManufacturers(): List<ManufacturerEntity>

    @Query("SELECT * FROM suppliers ORDER BY name")
    suspend fun getAllSuppliers(): List<SupplierEntity>

    @Query("SELECT * FROM units ORDER BY name")
    suspend fun getAllUnits(): List<UnitEntity>
}

@Dao
interface ProductDao {
    @Transaction
    @Query("SELECT * FROM products")
    suspend fun getAllProductsWithDetails(): List<ProductWithDetails>

    @Transaction
    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductWithDetailsById(productId: Int): ProductWithDetails?

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: Int): ProductEntity?

    @Query("SELECT MAX(id) FROM products")
    suspend fun findMaxProductId(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    @Query("SELECT COUNT(*) FROM order_items WHERE productId = :productId")
    suspend fun countOrderItemsByProductId(productId: Int): Int
}

@Dao
interface OrderDao {
    @Transaction
    @Query("SELECT * FROM orders ORDER BY orderDate DESC, id DESC")
    suspend fun getAllOrdersWithDetails(): List<OrderWithDetails>
}

@Dao
interface MaintenanceDao {
    @Query("SELECT COUNT(*) FROM roles")
    suspend fun countRoles(): Int

    @Query("DELETE FROM order_items")
    suspend fun clearOrderItems()

    @Query("DELETE FROM orders")
    suspend fun clearOrders()

    @Query("DELETE FROM customers")
    suspend fun clearCustomers()

    @Query("DELETE FROM pickup_points")
    suspend fun clearPickupPoints()

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("DELETE FROM categories")
    suspend fun clearCategories()

    @Query("DELETE FROM manufacturers")
    suspend fun clearManufacturers()

    @Query("DELETE FROM suppliers")
    suspend fun clearSuppliers()

    @Query("DELETE FROM units")
    suspend fun clearUnits()

    @Query("DELETE FROM users")
    suspend fun clearUsers()

    @Query("DELETE FROM roles")
    suspend fun clearRoles()

    @Query("DELETE FROM order_statuses")
    suspend fun clearOrderStatuses()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRole(role: RoleEntity)

    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Insert
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert
    suspend fun insertManufacturer(manufacturer: ManufacturerEntity): Long

    @Insert
    suspend fun insertSupplier(supplier: SupplierEntity): Long

    @Insert
    suspend fun insertUnit(unit: UnitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert
    suspend fun insertPickupPoint(pickupPoint: PickupPointEntity): Long

    @Insert
    suspend fun insertOrderStatus(status: OrderStatusEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Insert
    suspend fun insertOrderItem(orderItem: OrderItemEntity): Long

    @Query("SELECT * FROM roles WHERE code = :code LIMIT 1")
    suspend fun findRoleByCode(code: String): RoleEntity?

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun findCategoryByName(name: String): CategoryEntity?

    @Query("SELECT * FROM manufacturers WHERE name = :name LIMIT 1")
    suspend fun findManufacturerByName(name: String): ManufacturerEntity?

    @Query("SELECT * FROM suppliers WHERE name = :name LIMIT 1")
    suspend fun findSupplierByName(name: String): SupplierEntity?

    @Query("SELECT * FROM units WHERE name = :name LIMIT 1")
    suspend fun findUnitByName(name: String): UnitEntity?

    @Query("SELECT * FROM customers WHERE fullName = :fullName LIMIT 1")
    suspend fun findCustomerByFullName(fullName: String): CustomerEntity?

    @Query("SELECT * FROM pickup_points WHERE address = :address LIMIT 1")
    suspend fun findPickupPointByAddress(address: String): PickupPointEntity?

    @Query("SELECT * FROM order_statuses WHERE lower(name) = lower(:name) LIMIT 1")
    suspend fun findOrderStatusByName(name: String): OrderStatusEntity?
}

