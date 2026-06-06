package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.security.MessageDigest

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val sector: String, // Highway, Rail, Metro, Water, Power
    val layer: String, // National, State, Municipal
    val executingAgency: String, // NHAI, RVNL, BMRCL, Municipal Corp
    val budgetCr: Double, // in Crores
    val progress: Float, // 0.0f to 1.0f
    val status: String, // Active, Delayed, Blocked, Completed
    val description: String,
    val location: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val projectId: Int,
    val name: String, // e.g., "Forest land clearance Approval", "Environmental Impact Assessment", "Utility Mapping Verification"
    val assignedOfficer: String,
    val deadlineTimestamp: Long, // Absolute epoch ms
    val status: String, // PENDING, APPROVED, REJECTED, ESCALATED
    val isEscalated: Boolean = false,
    val escalationCount: Int = 0, // 0 = None, 1 = Municipal->State, 2 = State->National
    val lastActionTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "land_parcels")
data class LandParcelEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val surveyNumber: String,
    val ownerName: String,
    val districtState: String, // e.g., "Raigad, Maharashtra", "Noida, UP"
    val areaAcres: Double,
    val baseCircleRate: Double, // INR per Acre
    val distanceToHighwayKm: Double, // closer = higher dynamic valuation proximity bonus
    val economicActivityScore: Float, // 1 to 10 scale
    val structuralValueINR: Double, // Value of buildings/properties on the land
    val verifiedStatus: String, // Clean, Overlap Dispute, Verification Pending, Settled
    val compensationPaid: Boolean = false,
    val notes: String = ""
) {
    // Dynamic Valuation Algorithmic Logic
    // Proximity multiplier: closer to transport corridor = higher value
    // economic multiplier: dynamic weight
    // Structural value added
    fun calculateDynamicValue(): Double {
        val proximityMultiplier = when {
            distanceToHighwayKm <= 1.0 -> 2.5
            distanceToHighwayKm <= 5.0 -> 1.8
            else -> 1.2
        }
        val economicMultiplier = 1.0 + (economicActivityScore / 10.0)
        
        val baseLandValue = areaAcres * baseCircleRate
        val dynamicLandValue = baseLandValue * proximityMultiplier * economicMultiplier
        
        return dynamicLandValue + structuralValueINR
    }

    // Standard multiplier of 4x circle rate under LARR Act (typically 2x to 4x urban/rural)
    fun calculateFairCompensation(): Double {
        val dynamicValue = calculateDynamicValue()
        return dynamicValue * 2.0 // Dynamic pricing settlement compensation logic
    }
}

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val entityType: String, // PROJECT, TASK, LAND
    val entityId: Int,
    val action: String, // e.g., "AUTOMATIC_ESCALATION", "COMPENSATION_SETTLED", "VALUATION_RECALCULATED"
    val details: String,
    val blockchainHash: String = generateHash(timestamp, entityType, entityId, action, details)
)

private fun generateHash(time: Long, type: String, id: Int, action: String, details: String): String {
    val input = "$time|$type|$id|$action|$details|TOREX_INTEGRITY_SALT_2026"
    return try {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        bytes.fold("") { str, it -> str + "%02x".format(it) }.take(16).uppercase()
    } catch (e: Exception) {
        "TX_GEN_HASH_FAIL"
    }
}

@Entity(tableName = "maintenance_components")
data class MaintenanceComponentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Water Conduit Pipe", "High-Tension Power Grid", "Concrete Highway Overpass"
    val sensorMetrics: String, // e.g. "Core Pressure: 145 PSI, Temperature: 42°C"
    val ageYears: Int,
    val historicalIncidents: Int,
    val urbanPlanConflict: String, // e.g., "Conflict with High-Density Phase III subway line routing"
    val failureProbability: Float, // 0.0f to 1.0f (predicted by ML / heuristics)
    val alertLevel: String, // "CRITICAL", "HIGH", "WARNING", "HEALTHY"
    val estRepairCostCr: Double, // in Crores
    val estRepairDays: Int, // days
    val lastAuditTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "citizen_grievances")
data class CitizenGrievanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trackingId: String, // e.g. GRV-2026-XF82
    val citizenName: String,
    val location: String,
    val complaintText: String,
    val category: String, // e.g. "Pipeline Leak", "Power Line Sag", "Bridge Structural Cracks"
    val proposedAction: String, // Machine learning / Gemini suggested preliminary action course
    val escalationPath: String, // Agency / Department path e.g. "BWSSB Regional Water Inspector"
    val status: String, // "Ingested", "Under Investigation", "Action Scheduled", "Resolved"
    val timestamp: Long = System.currentTimeMillis()
)

