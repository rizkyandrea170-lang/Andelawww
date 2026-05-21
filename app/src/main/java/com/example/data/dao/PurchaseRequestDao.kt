package com.example.data.dao

import androidx.room.*
import com.example.data.model.PurchaseRequestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseRequestDao {
    @Query("SELECT * FROM purchase_requests ORDER BY prNumber DESC")
    fun getAllPurchaseRequests(): Flow<List<PurchaseRequestEntity>>

    @Query("SELECT * FROM purchase_requests WHERE prNumber = :prNumber LIMIT 1")
    suspend fun getPurchaseRequestByNumber(prNumber: String): PurchaseRequestEntity?

    @Query("SELECT * FROM purchase_requests WHERE mrNumber = :mrNumber LIMIT 1")
    suspend fun getPurchaseRequestByMr(mrNumber: String): PurchaseRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseRequest(pr: PurchaseRequestEntity)

    @Update
    suspend fun updatePurchaseRequest(pr: PurchaseRequestEntity)

    @Query("SELECT COUNT(*) FROM purchase_requests")
    suspend fun getPurchaseRequestsCount(): Int
}
