package com.example.businessos.data.local.dao

import androidx.room.*
import com.example.businessos.data.local.entity.AssetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets WHERE businessId = :businessId ORDER BY purchaseDate DESC")
    fun observeAssets(businessId: String): Flow<List<AssetEntity>>

    @Query("SELECT * FROM assets WHERE id = :id AND businessId = :businessId LIMIT 1")
    suspend fun getAssetById(id: String, businessId: String): AssetEntity?

    @Query("SELECT SUM(currentValue) FROM assets WHERE businessId = :businessId")
    fun observeTotalAssetValue(businessId: String): Flow<Double?>

    @Query("SELECT COUNT(*) FROM assets WHERE businessId = :businessId")
    suspend fun countAssets(businessId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)

    @Update
    suspend fun updateAsset(asset: AssetEntity)

    @Query("DELETE FROM assets WHERE id = :id AND businessId = :businessId")
    suspend fun deleteAsset(id: String, businessId: String)
}
