package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val nik: String,
    val nama: String,
    val jabatan: String,
    val departemen: String,
    val noHp: String,
    val password: String,
    val role: String, // "ADMIN", "PIC", "MEKANIK", "LOGISTIK", "ADMIN_PLANT"
    val isApproved: Boolean = false
) {
    fun hasFullAccess(): Boolean {
        val r = role.uppercase().trim()
        return r == "ADMIN" || r == "LOGISTIK" || r == "ADMIN_PLANT" || r == "ADMIN PLANT"
    }
}
