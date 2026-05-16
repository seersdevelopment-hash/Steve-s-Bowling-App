package com.bowlerspeed.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SessionEntity::class,
        DeliveryEntity::class,
        CalibrationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BowlerSpeedDatabase : RoomDatabase() {
    abstract fun dao(): BowlerSpeedDao

    companion object {
        @Volatile
        private var instance: BowlerSpeedDatabase? = null

        fun get(context: Context): BowlerSpeedDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BowlerSpeedDatabase::class.java,
                    "bowler-speed.db"
                ).build().also { instance = it }
            }
        }
    }
}