package com.example.businessos.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.businessos.data.local.converters.Converters
import com.example.businessos.data.local.dao.*
import com.example.businessos.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        BusinessEntity::class,
        BusinessMemberEntity::class,
        JobCardEntity::class,
        JobPartEntity::class,
        JobStageTransitionEntity::class,
        ItemEntity::class,
        SaleEntity::class,
        SaleItemEntity::class,
        StockMovementEntity::class,
        DocumentCounterEntity::class,
        CustomerEntity::class,
        AuditLogEntity::class,
        ExpenseEntity::class,
        AccountEntity::class,
        JournalEntryEntity::class,
        JournalLineEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        SupplierEntity::class,
        PaymentEntity::class,
        WagePaymentEntity::class,
        LayawayEntity::class
    ],
    version = AppDatabase.SCHEMA_VERSION,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun businessDao(): BusinessDao
    abstract fun businessMemberDao(): BusinessMemberDao
    abstract fun jobCardDao(): JobCardDao
    abstract fun jobPartDao(): JobPartDao
    abstract fun jobStageTransitionDao(): JobStageTransitionDao
    abstract fun itemDao(): ItemDao
    abstract fun saleDao(): SaleDao
    abstract fun saleItemDao(): SaleItemDao
    abstract fun stockMovementDao(): StockMovementDao
    abstract fun documentCounterDao(): DocumentCounterDao
    abstract fun customerDao(): CustomerDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountDao(): AccountDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun journalLineDao(): JournalLineDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun invoiceItemDao(): InvoiceItemDao
    abstract fun supplierDao(): SupplierDao
    abstract fun paymentDao(): PaymentDao
    abstract fun wagePaymentDao(): WagePaymentDao
    abstract fun layawayDao(): LayawayDao

    companion object {
        const val DB_NAME = "business_os.db"
        const val SCHEMA_VERSION = 1

        /** Closes the open database so its file can be replaced (restore). */
        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    // No fallbackToDestructiveMigration — explicit migrations required from v1 onward.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
