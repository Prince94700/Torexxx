package com.example.data

import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class TorexRepository(
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao,
    private val landParcelDao: LandParcelDao,
    private val auditLogDao: AuditLogDao,
    private val maintenanceComponentDao: MaintenanceComponentDao,
    private val citizenGrievanceDao: CitizenGrievanceDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allLandParcels: Flow<List<LandParcelEntity>> = landParcelDao.getAllLandParcels()
    val allLogs: Flow<List<AuditLogEntity>> = auditLogDao.getAllLogs()
    val allComponents: Flow<List<MaintenanceComponentEntity>> = maintenanceComponentDao.getAllComponents()
    val allGrievances: Flow<List<CitizenGrievanceEntity>> = citizenGrievanceDao.getAllGrievances()

    suspend fun insertProject(project: ProjectEntity) = projectDao.insertProject(project)
    suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)
    suspend fun deleteProject(project: ProjectEntity) = projectDao.deleteProject(project)

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun insertLandParcel(parcel: LandParcelEntity) = landParcelDao.insertLandParcel(parcel)
    suspend fun updateLandParcel(parcel: LandParcelEntity) = landParcelDao.updateLandParcel(parcel)
    suspend fun deleteLandParcel(parcel: LandParcelEntity) = landParcelDao.deleteLandParcel(parcel)

    suspend fun insertLog(log: AuditLogEntity) = auditLogDao.insertLog(log)
    suspend fun clearAllLogs() = auditLogDao.clearLogs()

    suspend fun insertComponent(component: MaintenanceComponentEntity) = maintenanceComponentDao.insertComponent(component)
    suspend fun updateComponent(component: MaintenanceComponentEntity) = maintenanceComponentDao.updateComponent(component)
    suspend fun clearComponents() = maintenanceComponentDao.clearComponents()

    suspend fun insertGrievance(grievance: CitizenGrievanceEntity) = citizenGrievanceDao.insertGrievance(grievance)
    suspend fun updateGrievance(grievance: CitizenGrievanceEntity) = citizenGrievanceDao.updateGrievance(grievance)
    suspend fun clearGrievances() = citizenGrievanceDao.clearGrievances()

    // Automatic Escalation Protocol Execution
    suspend fun checkAndTriggerEscalations(virtualTimeMs: Long): Int {
        val activePending = taskDao.getActivePendingTasks()
        var escalationsTriggeredCount = 0

        for (task in activePending) {
            if (task.deadlineTimestamp < virtualTimeMs) {
                // Task is overdue - automatic escalation!
                val nextCount = task.escalationCount + 1
                val updatedTask = task.copy(
                    status = "ESCALATED",
                    isEscalated = true,
                    escalationCount = nextCount,
                    lastActionTime = virtualTimeMs
                )
                taskDao.updateTask(updatedTask)
                escalationsTriggeredCount++

                // Determine escalation targets
                val escalationTarget = when (nextCount) {
                    1 -> "State-Level Infrastructure Committee"
                    2 -> "National Escalation Board & PMO"
                    else -> "National Core Cabinet Secretariat"
                }

                // 2. Fetch holding project info
                val project = projectDao.getProjectById(task.projectId)
                val projName = project?.name ?: "Unknown Project"

                // 3. Update project status to Blocked/Delayed if it was active
                if (project != null && project.status != "Blocked" && project.status != "Completed") {
                    projectDao.updateProject(
                        project.copy(
                            status = "Blocked",
                            lastUpdated = virtualTimeMs
                        )
                    )
                }

                // 4. Record Immutable Audit Log with Simulated cryptographic hash
                val logDetails = "Task '${task.name}' for Project '$projName' exceeded deadline of ${formatTime(task.deadlineTimestamp)}. Escalated automatically to: $escalationTarget."
                val auditLog = AuditLogEntity(
                    timestamp = virtualTimeMs,
                    entityType = "TASK",
                    entityId = task.id,
                    action = "AUTOMATIC_ESCALATION",
                    details = logDetails
                )
                auditLogDao.insertLog(auditLog)
            }
        }
        return escalationsTriggeredCount
    }

    private fun formatTime(ms: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(ms))
    }

    // Insert mock seeds if database is empty
    suspend fun prepopulateSeeds() {
        // Since we'll listen to flow, we can check if table is empty elsewhere or query first.
    }
}
