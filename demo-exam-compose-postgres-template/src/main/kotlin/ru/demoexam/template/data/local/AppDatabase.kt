package ru.demoexam.template.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        RoleEntity::class,
        UserEntity::class,
        CategoryEntity::class,
        ManufacturerEntity::class,
        SupplierEntity::class,
        UnitEntity::class,
        ProductEntity::class,
        CustomerEntity::class,
        PickupPointEntity::class,
        OrderStatusEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun authDao(): AuthDao
    abstract fun lookupDao(): LookupDao
    abstract fun productDao(): ProductDao
    abstract fun orderDao(): OrderDao
    abstract fun maintenanceDao(): MaintenanceDao
}
