package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.InvoiceItemEntity

@Dao
interface InvoiceItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItem(item: InvoiceItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :invoiceId ORDER BY rowid ASC")
    suspend fun getByInvoice(invoiceId: String): List<InvoiceItemEntity>

    @Query("DELETE FROM invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteByInvoice(invoiceId: String)
}
