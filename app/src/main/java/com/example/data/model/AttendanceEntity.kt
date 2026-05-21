package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userNik: String,
    val userName: String,
    val date: String, // format YYYY-MM-DD
    val checkInTime: String? = null,
    val checkOutTime: String? = null,
    val checkInGps: String? = null,
    val checkOutGps: String? = null,
    val checkInPhoto: String? = null,
    val checkOutPhoto: String? = null,
    val status: String // "HADIR", "TERLAMBAT", "TIDAK_HADIR"
)
