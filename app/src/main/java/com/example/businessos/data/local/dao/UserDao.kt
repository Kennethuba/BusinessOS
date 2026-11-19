package com.example.businessos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.businessos.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun observeUserById(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET failedAttempts = :attempts, lockoutUntil = :lockoutUntil WHERE id = :userId")
    suspend fun updateLockout(userId: String, attempts: Int, lockoutUntil: Long?)

    @Query("UPDATE users SET failedAttempts = 0, lockoutUntil = NULL WHERE id = :userId")
    suspend fun clearLockout(userId: String)
}
