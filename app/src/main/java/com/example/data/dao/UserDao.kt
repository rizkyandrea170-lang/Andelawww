package com.example.data.dao

import androidx.room.*
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE nik = :nik LIMIT 1")
    suspend fun getUserByNik(nik: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY nama ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
    
    @Query("SELECT * FROM users WHERE isApproved = 0")
    fun getPendingApprovalUsers(): Flow<List<UserEntity>>
}
