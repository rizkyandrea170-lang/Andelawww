package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_requests")
data class PurchaseRequestEntity(
    @PrimaryKey val prNumber: String,
    val mrNumber: String,
    val date: String,
    val status: String, // "WAITING_PR", "PR_PROCESS", "PR_COMPLETE"
    val notes: String
)
