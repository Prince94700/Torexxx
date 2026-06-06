package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ProjectEntity::class,
        TaskEntity::class,
        LandParcelEntity::class,
        AuditLogEntity::class,
        MaintenanceComponentEntity::class,
        CitizenGrievanceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TorexDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun landParcelDao(): LandParcelDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun maintenanceComponentDao(): MaintenanceComponentDao
    abstract fun citizenGrievanceDao(): CitizenGrievanceDao

    companion object {
        @Volatile
        private var INSTANCE: TorexDatabase? = null

        fun getDatabase(context: Context): TorexDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TorexDatabase::class.java,
                    "torex_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
