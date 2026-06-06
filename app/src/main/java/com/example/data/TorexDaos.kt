package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastUpdated DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: Int): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Delete
    suspend fun deleteProject(project: ProjectEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY deadlineTimestamp ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY deadlineTimestamp ASC")
    fun getTasksByProject(projectId: Int): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = 'PENDING'")
    suspend fun getActivePendingTasks(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)
}

@Dao
interface LandParcelDao {
    @Query("SELECT * FROM land_parcels ORDER BY surveyNumber ASC")
    fun getAllLandParcels(): Flow<List<LandParcelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandParcel(parcel: LandParcelEntity)

    @Update
    suspend fun updateLandParcel(parcel: LandParcelEntity)

    @Delete
    suspend fun deleteLandParcel(parcel: LandParcelEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
    
    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}

@Dao
interface MaintenanceComponentDao {
    @Query("SELECT * FROM maintenance_components ORDER BY failureProbability DESC")
    fun getAllComponents(): Flow<List<MaintenanceComponentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: MaintenanceComponentEntity)

    @Update
    suspend fun updateComponent(component: MaintenanceComponentEntity)

    @Query("DELETE FROM maintenance_components")
    suspend fun clearComponents()
}

@Dao
interface CitizenGrievanceDao {
    @Query("SELECT * FROM citizen_grievances ORDER BY timestamp DESC")
    fun getAllGrievances(): Flow<List<CitizenGrievanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrievance(grievance: CitizenGrievanceEntity)

    @Update
    suspend fun updateGrievance(grievance: CitizenGrievanceEntity)

    @Query("DELETE FROM citizen_grievances")
    suspend fun clearGrievances()
}

