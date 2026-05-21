package com.example.data.dao

import androidx.room.*
import com.example.data.model.MaterialRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialRequestDao {
    @Query("SELECT * FROM material_requests ORDER BY mrNumber DESC")
    fun getAllMaterialRequests(): Flow<List<MaterialRequestEntity>>

    @Query("SELECT * FROM material_requests WHERE mechanicNik = :nik ORDER BY mrNumber DESC")
    fun getMaterialRequestsByMechanic(nik: String): Flow<List<MaterialRequestEntity>>

    @Query("SELECT * FROM material_requests WHERE mrNumber = :mrNumber LIMIT 1")
    suspend fun getMaterialRequestByNumber(mrNumber: String): MaterialRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterialRequest(mr: MaterialRequestEntity)

    @Update
    suspend fun updateMaterialRequest(mr: MaterialRequestEntity)

    @Query("SELECT COUNT(*) FROM material_requests")
    suspend fun getMaterialRequestsCount(): Int
}
