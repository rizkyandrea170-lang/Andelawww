package com.example.data.dao

import androidx.room.*
import com.example.data.model.AttendanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance ORDER BY date DESC, id DESC")
    fun getAllAttendanceFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE userNik = :nik ORDER BY date DESC, id DESC")
    fun getAttendanceByNikFlow(nik: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE userNik = :nik AND date = :date LIMIT 1")
    suspend fun getAttendanceByNikAndDate(nik: String, date: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)
}
