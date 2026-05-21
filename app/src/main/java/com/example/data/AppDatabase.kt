package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.AttendanceDao
import com.example.data.dao.MaterialRequestDao
import com.example.data.dao.PurchaseRequestDao
import com.example.data.dao.UserDao
import com.example.data.model.AttendanceEntity
import com.example.data.model.MaterialRequestEntity
import com.example.data.model.PurchaseRequestEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        MaterialRequestEntity::class,
        PurchaseRequestEntity::class,
        AttendanceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun materialRequestDao(): MaterialRequestDao
    abstract fun purchaseRequestDao(): PurchaseRequestDao
    abstract fun attendanceDao(): AttendanceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workshop_mr_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
