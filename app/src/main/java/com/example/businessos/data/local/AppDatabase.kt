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
        ItemEntity::class,
        SaleEntity::class,
        CustomerEntity::class,
        AuditLogEntity::class,
        ExpenseEntity::class,
        AccountEntity::class,
        JournalEntryEntity::class,
        InvoiceEntity::class,
        SupplierEntity::class,
        BillEntity::class,
        PaymentEntity::class,
        AssetEntity::class,
        LiabilityEntity::class,
        WagePaymentEntity::class,
        LayawayEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun businessDao(): BusinessDao
    abstract fun businessMemberDao(): BusinessMemberDao
    abstract fun jobCardDao(): JobCardDao
    abstract fun itemDao(): ItemDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun accountDao(): AccountDao
    abstract fun journalEntryDao(): JournalEntryDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun supplierDao(): SupplierDao
    abstract fun billDao(): BillDao
    abstract fun paymentDao(): PaymentDao
    abstract fun assetDao(): AssetDao
    abstract fun liabilityDao(): LiabilityDao
    abstract fun wagePaymentDao(): WagePaymentDao
    abstract fun layawayDao(): LayawayDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "business_os_v4.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}