package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TorexViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TorexDatabase.getDatabase(application)
    val repository = TorexRepository(
        database.projectDao(),
        database.taskDao(),
        database.landParcelDao(),
        database.auditLogDao(),
        database.maintenanceComponentDao(),
        database.citizenGrievanceDao()
    )

    // Virtual Administrative Timeline Settings
    private val _virtualTime = MutableStateFlow(System.currentTimeMillis())
    val virtualTime: StateFlow<Long> = _virtualTime.asStateFlow()

    // Base database flows
    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val landParcels: StateFlow<List<LandParcelEntity>> = repository.allLandParcels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenanceComponents: StateFlow<List<MaintenanceComponentEntity>> = repository.allComponents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val citizenGrievances: StateFlow<List<CitizenGrievanceEntity>> = repository.allGrievances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gemini Administrative AI Assistant UX States
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    private val _aiResponse = MutableStateFlow<String?>(null)
    val aiResponse: StateFlow<String?> = _aiResponse.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    init {
        // Run seed check
        viewModelScope.launch {
            repository.allProjects.first().let { currentList ->
                if (currentList.isEmpty()) {
                    seedDatabase()
                }
            }
        }

        // Ticker loop executing the "Automatic Escalation Protocol" checking system
        viewModelScope.launch {
            while (true) {
                delay(1000)
                // Advance virtual clock standard 1 second in real time (unless manual fast-forward triggers)
                _virtualTime.update { it + 1000 }
                // Execute pipeline deadline check
                repository.checkAndTriggerEscalations(_virtualTime.value)
            }
        }
    }

    // Advance clock helper (e.g. fast forward time to trigger real-time escalation preview)
    fun advanceClockByMinutes(minutes: Int) {
        viewModelScope.launch {
            val offsetMs = minutes * 60 * 1000L
            _virtualTime.update { it + offsetMs }
            repository.checkAndTriggerEscalations(_virtualTime.value)
        }
    }

    // Core task operations
    fun approveTask(task: TaskEntity) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = "APPROVED", lastActionTime = _virtualTime.value)
            repository.updateTask(updatedTask)
            
            val project = projects.value.firstOrNull { it.id == task.projectId }
            // Record Audit Log block
            repository.insertLog(
                AuditLogEntity(
                    timestamp = _virtualTime.value,
                    entityType = "TASK",
                    entityId = task.id,
                    action = "TASK_APPROVED",
                    details = "Task '${task.name}' for Project '${project?.name ?: "ID ${task.projectId}"}' has been approved with final administrative clearance by designated field officer."
                )
            )
            updateProjectProgress(task.projectId)
        }
    }

    fun rejectTask(task: TaskEntity, reason: String) {
        viewModelScope.launch {
            val updatedTask = task.copy(status = "REJECTED", lastActionTime = _virtualTime.value)
            repository.updateTask(updatedTask)

            val project = projects.value.firstOrNull { it.id == task.projectId }
            repository.insertLog(
                AuditLogEntity(
                    timestamp = _virtualTime.value,
                    entityType = "TASK",
                    entityId = task.id,
                    action = "TASK_REJECTED",
                    details = "Task '${task.name}' rejected. Protocol Action Ref: $reason. Returned to executing officer for immediate revision."
                )
            )
        }
    }

    fun forceEscalateTask(task: TaskEntity) {
        viewModelScope.launch {
            val nextEsc = task.escalationCount + 1
            val updated = task.copy(
                status = "ESCALATED",
                isEscalated = true,
                escalationCount = nextEsc,
                lastActionTime = _virtualTime.value
            )
            repository.updateTask(updated)

            val project = projects.value.firstOrNull { it.id == task.projectId }
            val escTarget = when (nextEsc) {
                1 -> "State-Level Infrastructure Committee"
                2 -> "National Escalation Board & PMO"
                else -> "National Core Cabinet Secretariat"
            }
            
            project?.let {
                if (it.status != "Blocked") {
                    repository.updateProject(it.copy(status = "Blocked", lastUpdated = _virtualTime.value))
                }
            }

            repository.insertLog(
                AuditLogEntity(
                    timestamp = _virtualTime.value,
                    entityType = "TASK",
                    entityId = task.id,
                    action = "MANUAL_ESCALATION",
                    details = "Task '${task.name}' manually escalated. Transferred file responsibility status upwards to: $escTarget. Related Project '${project?.name ?: "ID"}' flagged as Bottlenecked/Blocked."
                )
            )
        }
    }

    // Recalculates matching progress based on completed tasks
    private suspend fun updateProjectProgress(projectId: Int) {
        val allProjectTasks = tasks.value.filter { it.projectId == projectId }
        if (allProjectTasks.isEmpty()) return

        val approved = allProjectTasks.count { it.status == "APPROVED" }
        val newProgress = approved.toFloat() / allProjectTasks.size.toFloat()
        val isCompleted = approved == allProjectTasks.size

        val project = projects.value.firstOrNull { it.id == projectId }
        if (project != null) {
            val updatedState = project.copy(
                progress = newProgress,
                status = if (isCompleted) "Completed" else "Active",
                lastUpdated = _virtualTime.value
            )
            repository.updateProject(updatedState)
        }
    }

    // Core land parcel operations
    fun updateLandParcel(parcel: LandParcelEntity) {
        viewModelScope.launch {
            repository.updateLandParcel(parcel)
            repository.insertLog(AuditLogEntity(
                timestamp = _virtualTime.value,
                entityType = "LAND",
                entityId = parcel.id,
                action = "VALUATION_RECALCULATED",
                details = "Land parcel survey '${parcel.surveyNumber}' registry details updated. Dynamic compensation algorithm value reset."
            ))
        }
    }

    fun settleLandCompensation(parcel: LandParcelEntity) {
        viewModelScope.launch {
            val updated = parcel.copy(compensationPaid = true, verifiedStatus = "Settled")
            repository.updateLandParcel(updated)
            
            val doubleValue = updated.calculateFairCompensation()
            val croreStr = "%.2f Cr".format(doubleValue / 10000000.0)
            
            repository.insertLog(AuditLogEntity(
                timestamp = _virtualTime.value,
                entityType = "LAND",
                entityId = parcel.id,
                action = "COMPENSATION_SETTLED",
                details = "Middleman-free Direct Bank Deposit settlement processed for parcel '${parcel.surveyNumber}'. Disbursed: ₹${croreStr} to direct beneficiary account. Registry cleared."
            ))
        }
    }

    // AI Intelligence layer interaction - Powered by Gemini
    fun askTorexAI(prompt: String, contextType: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiResponse.value = null

            val systemPrompt = """
                You are TOREX: AI-Integrated Governance & Infrastructure Execution Framework for India.
                Your purpose is to eliminate administrative delays, execution paralysis, and data fragmentation.
                You are helping administrative officers debottleneck infrastructure, resolve land disputes, and generate high-level policy resolutions.
                
                Always formulate answers that reflect:
                1. Standardised legal protocols, inter-agency APIs, and G2G linkages.
                2. Saffron & green technology integration (e.g. PM GatiShakti GIS layers, central registries).
                3. High-quality precision, referencing the LARR Act (2013) for land acquisition, municipal codes, and state policy priorities.
                4. Professional, action-oriented tone to solve blockages. Avoid passive bureaucracy. Offer clear immediate resolutions.
            """.trimIndent()

            val customPrompt = when (contextType) {
                "DEBOTTLENECK" -> "Review the following roadblock and provide a strict technical & legal pathway to resolve the clearance: $prompt"
                "ESCALATION_LETTER" -> "Generate an official, highly polished, time-bound Escalation Letter Brief under the Public Accountability Act for: $prompt. Include deadlines, recipient minister or department, and automatic log reference."
                "LAND_RESOLVE" -> "Synthesize a resolution plan for land overlap/demarcation dispute details: $prompt. Highlight registration parameters and middleman-free LARR compensation formulas."
                else -> prompt
            }

            val result = GeminiBrain.askGemini(customPrompt, systemPrompt)
            if (result == "API_KEY_MISSING_ERROR") {
                _aiError.value = "Gemini API key is not configured in the AI Studio environment. Please set up GEMINI_API_KEY in the Secrets panel."
                _aiResponse.value = "API Key Unavailable. Showing offline fallback advisory resolution:\n\n1. Check GIS mapping database layers.\n2. Auto-schedule high-priority joint site inspection.\n3. Escalate file manually to the State Infrastructure Secretary."
            } else {
                _aiResponse.value = result
            }
            _aiLoading.value = false
        }
    }

    fun clearAIState() {
        _aiResponse.value = null
        _aiError.value = null
        _aiLoading.value = false
    }

    // Populate Initial Mock Seeds
    private suspend fun seedDatabase() {
        // Initial Project Seeds
        val p1 = ProjectEntity(
            name = "Amritsar-Kolkata Industrial Highway Corridors",
            sector = "Highway",
            layer = "National",
            executingAgency = "NHAI / DMICDC Corporation",
            budgetCr = 42500.0,
            progress = 0.0f,
            status = "Active",
            description = "High-priority economic corridor traversing six major northern Indian states. Integrates industrial freight clusters, greenfield nodes, and logistics parks.",
            location = "UP, Bihar, WB Segment corridor"
        )
        val p2 = ProjectEntity(
            name = "Bengaluru Namma Metro Expansion Phase 3A",
            sector = "Metro",
            layer = "State",
            executingAgency = "BMRCL / Government of Karnataka",
            budgetCr = 15600.0,
            progress = 0.0f,
            status = "Delayed",
            description = "Expanded rapid transit network connectivity loops across Bengaluru commercial belts and suburban tech parks.",
            location = "Bengaluru City & Outer Ring Road Area"
        )
        val p3 = ProjectEntity(
            name = "Cauvery Municipal Tap-Water Pipeline Phase V",
            sector = "Water",
            layer = "Municipal",
            executingAgency = "BWSSB / BBMP municipal network",
            budgetCr = 5500.0,
            progress = 0.0f,
            status = "Active",
            description = "Crucial drinking water delivery optimization pipeline to feed expanding outskirts, with dual utility-mapping safeguards.",
            location = "Bengaluru Rural/Urban transition zones"
        )
        val p4 = ProjectEntity(
            name = "Noida Solid Waste Resource Solar Grid",
            sector = "Power",
            layer = "State",
            executingAgency = "UPNEDA / Noida Authority",
            budgetCr = 2800.0,
            progress = 0.0f,
            status = "Blocked",
            description = "Decentralized high-voltage solar feed integrating municipal solid-state recycling and electricity supply optimization.",
            location = "Greater Noida SEZ Sector 4A"
        )

        repository.insertProject(p1)
        repository.insertProject(p2)
        repository.insertProject(p3)
        repository.insertProject(p4)

        // Fetch freshly generated project IDs to assign tasks cleanly
        // Because fields are inserted concurrently, we'll use manually assigned IDs that map to the 1-based default sqlite indexes
        val t1 = TaskEntity(
            projectId = 1,
            name = "Forest Conservation clearance Approval Certificate",
            assignedOfficer = "S. K. Verma (IFS Officer - MoEFCC Liaison)",
            deadlineTimestamp = System.currentTimeMillis() + 120_000L, // 2 Minutes from now (extremely close!)
            status = "PENDING"
        )
        val t2 = TaskEntity(
            projectId = 1,
            name = "MoRTH National Joint Highway Boundary Alignment Audit",
            assignedOfficer = "Amit Shah (Executive NHAI Engineer - Lucknow)",
            deadlineTimestamp = System.currentTimeMillis() + 600_000L, // 10 Minutes from now
            status = "PENDING"
        )
        val t3 = TaskEntity(
            projectId = 2,
            name = "Telecom Fiber Utilities Physical Mapping & Cross-Matching",
            assignedOfficer = "A. Swaminathan (Junior Municipal Planning Liaison)",
            deadlineTimestamp = System.currentTimeMillis() + 180_000L, // 3 Minutes from now (fast-escalation target)
            status = "PENDING"
        )
        val t4 = TaskEntity(
            projectId = 3,
            name = "BWSSB Leakage Pressure Test Verification & Signoff",
            assignedOfficer = "M. N. Krishna (BWSSB Chief Engineer)",
            deadlineTimestamp = System.currentTimeMillis() + 3_600_000L, // 1 Hour from now
            status = "PENDING"
        )
        val t5 = TaskEntity(
            projectId = 4,
            name = "Geotechnical Ground Suitability Clearance Certificate",
            assignedOfficer = "Priyanka Sen (Joint Director - State Geol. Dept.)",
            deadlineTimestamp = System.currentTimeMillis() + 90_000L, // 1.5 Minutes from now
            status = "PENDING"
        )

        repository.insertTask(t1)
        repository.insertTask(t2)
        repository.insertTask(t3)
        repository.insertTask(t4)
        repository.insertTask(t5)

        // National Land Intelligence / Dynamic Valuation seeds
        val l1 = LandParcelEntity(
            surveyNumber = "S.No. 124B/P - Mumbai Alignment",
            ownerName = "Baldev Singh Sandhu",
            districtState = "Raigad, Maharashtra",
            areaAcres = 4.8,
            baseCircleRate = 12500000.0, // ₹1.25 Cr per acre base circle rate
            distanceToHighwayKm = 0.2, // Very close! (Proximity bonus)
            economicActivityScore = 8.5f,
            structuralValueINR = 4200000.0, // Existing warehouse valuation
            verifiedStatus = "Clean",
            notes = "Survey aligned directly to Panvel highway container corridor. Owner verified via AADHAAR registry links."
        )
        val l2 = LandParcelEntity(
            surveyNumber = "S.No. 89-C - Noida Extension Segment",
            ownerName = "Rampal Gopinath Yadav",
            districtState = "Gautam Buddha Nagar, UP",
            areaAcres = 12.5,
            baseCircleRate = 9500000.0,
            distanceToHighwayKm = 1.1,
            economicActivityScore = 9.0f,
            structuralValueINR = 1500000.0,
            verifiedStatus = "Overlap Dispute",
            notes = "Overlaps with municipal parkway plan. Requires AI dispute validation and physical alignment shift proposal."
        )
        val l3 = LandParcelEntity(
            surveyNumber = "S.No. 402/1 - Jewar Airport Extension Area",
            ownerName = "Surendra Balaji Mahto",
            districtState = "Jewar Suburban, UP",
            areaAcres = 2.2,
            baseCircleRate = 8500000.0,
            distanceToHighwayKm = 0.4,
            economicActivityScore = 7.8f,
            structuralValueINR = 250000.0,
            verifiedStatus = "Verification Pending",
            notes = "Pending field verification of residential structure. Scheduled within 9-12 month cleansing window."
        )
        val l4 = LandParcelEntity(
            surveyNumber = "S.No. 77A - Bengaluru Ring Road Sector",
            ownerName = "G. Venkatesh Swamy",
            districtState = "Devanahalli, Karnataka",
            areaAcres = 3.5,
            baseCircleRate = 18000000.0,
            distanceToHighwayKm = 4.5,
            economicActivityScore = 7.0f,
            structuralValueINR = 0.0,
            verifiedStatus = "Settled",
            compensationPaid = true,
            notes = "Settled directly using DBT payment interface. Zero complaints registered."
        )

        repository.insertLandParcel(l1)
        repository.insertLandParcel(l2)
        repository.insertLandParcel(l3)
        repository.insertLandParcel(l4)

        // Add Predictive Infrastructure Maintenance component seeds
        val mc1 = MaintenanceComponentEntity(
            name = "Vrishabhavathi Sewer Trunk Conduit Line",
            type = "Water Conduit Pipe",
            sensorMetrics = "Internal Silt Depth: 1.4m, Flow Backpressure: 185 PSI, Delta pH: 5.2 (Acidic)",
            ageYears = 24,
            historicalIncidents = 6,
            urbanPlanConflict = "Underground intersection conflict with upcoming Metro Phase 3B cross-tunnelling corridor.",
            failureProbability = 0.85f,
            alertLevel = "CRITICAL",
            estRepairCostCr = 42.50,
            estRepairDays = 45
        )
        val mc2 = MaintenanceComponentEntity(
            name = "Greater Noida Substation Trans-Grid T-4",
            type = "High-Tension Power Grid",
            sensorMetrics = "Transformer 3 Core Temperature: 94°C, Oil Level: 72%, Transmission Load Factor: 94%",
            ageYears = 18,
            historicalIncidents = 3,
            urbanPlanConflict = "Encroached by the greenfield Expressway buffer extension corridor mapping grids.",
            failureProbability = 0.62f,
            alertLevel = "HIGH",
            estRepairCostCr = 12.80,
            estRepairDays = 14
        )
        val mc3 = MaintenanceComponentEntity(
            name = "Bandra-Worli Sea Link Auxiliary Cable Anchor 24",
            type = "Concrete Highway Overpass",
            sensorMetrics = "Auxiliary Cable Tension: 6200 kN, Delta Expansion Deflection: 1.8cm, Concrete Saline Intrusion: Active",
            ageYears = 16,
            historicalIncidents = 0,
            urbanPlanConflict = "Unauthorised sub-sea marine pipe routing alignment near deep anchorage points.",
            failureProbability = 0.38f,
            alertLevel = "WARNING",
            estRepairCostCr = 85.00,
            estRepairDays = 120
        )
        repository.insertComponent(mc1)
        repository.insertComponent(mc2)
        repository.insertComponent(mc3)

        // Add Citizen Grievance seeds
        val cg1 = CitizenGrievanceEntity(
            trackingId = "GRV-2026-N2D4",
            citizenName = "Rajesh S. Gowda",
            location = "Kasturi Nagar, Bengaluru",
            complaintText = "A critical underground main water channel is leaking. It is causing the road surface above to sink, forming a 4-foot deep pothole under the street intersection.",
            category = "Water Conduit Pipe Pipeline Leak",
            proposedAction = "Instruct municipal BWSSB area controls to throttle local supply pressure valve 14, dispatch emergency water pumping tenders, and alert road reconstruction engineers.",
            escalationPath = "BWSSB Area Assistant Executive Engineer & BBMP Ward Planner",
            status = "Under Investigation"
        )
        val cg2 = CitizenGrievanceEntity(
            trackingId = "GRV-2026-F9W1",
            citizenName = "Ananya Nair",
            location = "Mayur Vihar Phase I, New Delhi",
            complaintText = "A high-tension electrical distributor line has sagged within 3 meters of residential third-floor balconies, shooting sparks during rain showers. Extreme risk to children.",
            category = "High-Tension Power Grid Power Line Sag",
            proposedAction = "Coordinate with BSES Yamuna Power control room to temporarily bypass line routing load to grid T-2 and deploy physical field technician teams to re-tension cables.",
            escalationPath = "BSES Yamuna Power Zonal Maintenance Supervisor / Secretary",
            status = "Ingested"
        )
        repository.insertGrievance(cg1)
        repository.insertGrievance(cg2)

        // Seed audit log blocks
        repository.insertLog(AuditLogEntity(
            timestamp = System.currentTimeMillis() - 600000L,
            entityType = "SYSTEM",
            entityId = 0,
            action = "SYSTEM_INITIALIZED",
            details = "TOREX India Governance Platform initialized. National, State, and Municipal registry nodes synced successfully."
        ))
    }

    // Helper functions for parsing simple fields from Gemini model outputs
    private fun extractFloat(text: String, field: String, fallback: Float): Float {
        val pattern = "\"$field\"\\s*:\\s*([0-9.]+)"
        val match = Regex(pattern).find(text)
        return match?.groupValues?.get(1)?.toFloatOrNull() ?: fallback
    }

    private fun extractString(text: String, field: String, fallback: String): String {
        val pattern = "\"$field\"\\s*:\\s*\"([^\"]+)\""
        val match = Regex(pattern).find(text)
        return match?.groupValues?.get(1) ?: fallback
    }

    // Run Predictive Assessment Module execution block
    fun runPredictiveAssessment(component: MaintenanceComponentEntity) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiResponse.value = null

            val systemPrompt = """
                You are TOREX Predictive Infrastructure Maintenance Intelligence.
                Your goal is to parse sensor metrics, historical incidents, age, and urban plan conflicts for a critical infrastructure component (e.g. pipe, grid, bridge) and calculate/predict:
                1. A failure probability rating between 0% and 100% (decimal format, e.g. 0.85).
                2. An alert prioritization level (CRITICAL, HIGH, WARNING, HEALTHY).
                3. Estimated repair costs (in Crores INR) and timelines (in days).
                4. Actionable recommendations.
                
                You MUST output raw JSON in this format ONLY:
                {
                  "failureProbability": 0.XX,
                  "alertLevel": "LEVEL",
                  "estimatedCostCr": X.XX,
                  "estimatedDays": XX,
                  "recommendations": "Actionable instructions bullet points"
                }
            """.trimIndent()

            val prompt = """
                Analyze this critical infrastructure component for failure likelihood:
                Name: ${component.name}
                Type: ${component.type}
                Sensor Metrics: ${component.sensorMetrics}
                Age: ${component.ageYears} years
                Historical Incidents: ${component.historicalIncidents}
                Urban Plan Conflict: ${component.urbanPlanConflict}
            """.trimIndent()

            val result = GeminiBrain.askGemini(prompt, systemPrompt)
            if (result == "API_KEY_MISSING_ERROR") {
                // Heuristics fallback calculation
                var baseProb = 0.15f
                if (component.ageYears > 15) baseProb += 0.25f
                if (component.historicalIncidents > 2) baseProb += 0.20f
                if (component.sensorMetrics.lowercase().contains("backpressure") || component.sensorMetrics.lowercase().contains("94°c")) {
                    baseProb += 0.30f
                }
                if (component.urbanPlanConflict.isNotEmpty() && !component.urbanPlanConflict.lowercase().contains("none")) {
                    baseProb += 0.15f
                }
                baseProb = baseProb.coerceIn(0.05f, 0.95f)

                val calculatedAlert = when {
                    baseProb >= 0.75f -> "CRITICAL"
                    baseProb >= 0.50f -> "HIGH"
                    baseProb >= 0.25f -> "WARNING"
                    else -> "HEALTHY"
                }
                val estCost = (component.ageYears * 1.8 + component.historicalIncidents * 2.5).coerceIn(1.0, 120.0)
                val estDays = (component.ageYears * 3 + 10).coerceIn(7, 180)

                val updatedComp = component.copy(
                    failureProbability = baseProb,
                    alertLevel = calculatedAlert,
                    estRepairCostCr = estCost,
                    estRepairDays = estDays,
                    lastAuditTime = _virtualTime.value
                )
                repository.updateComponent(updatedComp)

                _aiResponse.value = """
                    📢 OFFLINE PREDICTIVE FALLBACK ASSESSMENT:
                    - **Predicted Probability**: ${(baseProb * 100).toInt()}% likelihood of failure.
                    - **Prioritized Threshold Alert**: $calculatedAlert
                    - **Estimated Expenditure**: ₹${"%.2f".format(estCost)} Cr
                    - **Estimated Mitigation Timeline**: $estDays Business Days
                    
                    🔧 Immediate G2G Tactical Plan:
                    1. Dispatch physical validation teams to install piezometers or ultrasound sensors.
                    2. Check PM GatiShakti GIS layers to verify physical utility overlap with conflicting urban developments.
                    3. Auto-notify localized administrative offices (BBMP/BWSSB/BSES) to initiate a bypass.
                """.trimIndent()

                repository.insertLog(AuditLogEntity(
                    timestamp = _virtualTime.value,
                    entityType = "INFRASTRUCTURE",
                    entityId = component.id,
                    action = "PREDICTIVE_MAINTENANCE_LOG",
                    details = "Heuristic ML modeling complete for '${component.name}'. Failure Likelihood: ${(baseProb*100).toInt()}%."
                ))
            } else {
                try {
                    val parsedProb = extractFloat(result, "failureProbability", 0.5f)
                    val parsedAlert = extractString(result, "alertLevel", "HIGH")
                    val parsedCost = extractFloat(result, "estimatedCostCr", 10.0f).toDouble()
                    val parsedDays = extractFloat(result, "estimatedDays", 30f).toInt()
                    val parsedRecs = extractString(result, "recommendations", "Establish physical structural inspection timeline.")

                    val updatedComp = component.copy(
                        failureProbability = parsedProb,
                        alertLevel = parsedAlert,
                        estRepairCostCr = parsedCost,
                        estRepairDays = parsedDays,
                        lastAuditTime = _virtualTime.value
                    )
                    repository.updateComponent(updatedComp)

                    _aiResponse.value = """
                        🤖 TOREX CENTRAL AI PREDICTIVE OUTCOME:
                        - **Status Level**: $parsedAlert
                        - **Failure Probability**: ${(parsedProb * 100).toInt()}%
                        - **Expenditure Budget**: ₹$parsedCost Cr
                        - **Mitigation Speed**: $parsedDays Days
                        
                        📋 Technical Action Roadmap:
                        $parsedRecs
                    """.trimIndent()

                    repository.insertLog(AuditLogEntity(
                        timestamp = _virtualTime.value,
                        entityType = "INFRASTRUCTURE",
                        entityId = component.id,
                        action = "PREDICTIVE_MAINTENANCE_LOG",
                        details = "AI Gemini analytics complete for '${component.name}'. Predicted likelihood: ${(parsedProb*100).toInt()}%. Repair budget: ₹$parsedCost Cr."
                    ))
                } catch (e: Exception) {
                    _aiError.value = "Failed parsing response: ${e.message}"
                }
            }
            _aiLoading.value = false
        }
    }

    // Citizen Grievance Redressal Ingestion API Simulation block
    fun submitCitizenGrievance(citizenName: String, location: String, complaintText: String, category: String) {
        viewModelScope.launch {
            _aiLoading.value = true
            _aiError.value = null
            _aiResponse.value = null

            val randomHex = (1000..9999).random().toString()
            val trackingId = "GRV-2026-X$randomHex"

            val systemPrompt = """
                You are TOREX: Citizen Grievance Redressal Intelligence Layer.
                Your purpose is to ingest a citizen complaint, categorize it precisely (e.g., Water Pipe Pipeline Leak, Power Line Sag, Highway Overpass Structural Crack),
                suggest a high-priority preliminary course of action (G2G linkage) and identify the appropriate escalation authority department.
                
                Provide response fields in this raw JSON format ONLY:
                {
                  "category": "PRECISE_CATEGORY",
                  "proposedAction": "Action details",
                  "escalationPath": "Ministry / Department Zonal head name"
                }
            """.trimIndent()

            val prompt = "Ingest Complaint Info:\nCitizen: $citizenName\nLocation: $location\nComplaint: $complaintText\nInitial Sector: $category"

            val result = GeminiBrain.askGemini(prompt, systemPrompt)
            if (result == "API_KEY_MISSING_ERROR") {
                // Heuristic mapping fallback
                val suggestedCat = when {
                    complaintText.lowercase().contains("water") || complaintText.lowercase().contains("pipe") || complaintText.lowercase().contains("leak") || complaintText.lowercase().contains("drain") -> "Water Conduit Pipe Pipeline Leak"
                    complaintText.lowercase().contains("power") || complaintText.lowercase().contains("line") || complaintText.lowercase().contains("wire") || complaintText.lowercase().contains("spark") || complaintText.lowercase().contains("electricity") -> "High-Tension Power Grid Power Line Sag"
                    complaintText.lowercase().contains("bridge") || complaintText.lowercase().contains("overpass") || complaintText.lowercase().contains("crack") || complaintText.lowercase().contains("pillar") -> "Concrete Highway Overpass Structural Defect"
                    else -> "General Municipal Services Issue"
                }
                val act = when (suggestedCat) {
                    "Water Conduit Pipe Pipeline Leak" -> "Instruct municipal water BWSSB field controls to throttle water supply valve 14, dispatch physical water vacuum tenders, and alert local sewage engineers."
                    "High-Tension Power Grid Power Line Sag" -> "Trigger substation trans-grid emergency bypass protocols, insulate the overhanging lines, and schedule technical team for wire re-tensioning."
                    "Concrete Highway Overpass Structural Defect" -> "Deploy rapid non-destructive physical stress monitoring devices and alert State Road Transport authorities for heavy-vehicle diversion."
                    else -> "Forward complaint file details to municipal grievance desk for visual physical onsite auditing."
                }
                val esc = when (suggestedCat) {
                    "Water Conduit Pipe Pipeline Leak" -> "BWSSB Area Assistant Executive Engineer & BBMP Ward Planner"
                    "High-Tension Power Grid Power Line Sag" -> "BSES Yamuna Power Zonal Maintenance Supervisor / Secretary"
                    "Concrete Highway Overpass Structural Defect" -> "NHAI State Bridge Integrity Commission Inspector"
                    else -> "City Municipal Grievance Commissioner Desk"
                }

                val grievance = CitizenGrievanceEntity(
                    trackingId = trackingId,
                    citizenName = citizenName,
                    location = location,
                    complaintText = complaintText,
                    category = suggestedCat,
                    proposedAction = act,
                    escalationPath = esc,
                    status = "Ingested",
                    timestamp = _virtualTime.value
                )
                repository.insertGrievance(grievance)

                repository.insertLog(AuditLogEntity(
                    timestamp = _virtualTime.value,
                    entityType = "CITIZEN_GRIEVANCE",
                    entityId = 0,
                    action = "GRIEVANCE_AUTO_INGEST",
                    details = "Registering Grievance '$trackingId' offline. Auto-assigned to '$esc' with local heuristics."
                ))
            } else {
                try {
                    val parsedCat = extractString(result, "category", category.ifEmpty { "General Municipal Issue" })
                    val parsedAction = extractString(result, "proposedAction", "Review filed complaint details and allocate state inspector.")
                    val parsedEscalation = extractString(result, "escalationPath", "City Municipal Joint Planner Office")

                    val grievance = CitizenGrievanceEntity(
                        trackingId = trackingId,
                        citizenName = citizenName,
                        location = location,
                        complaintText = complaintText,
                        category = parsedCat,
                        proposedAction = parsedAction,
                        escalationPath = parsedEscalation,
                        status = "Ingested",
                        timestamp = _virtualTime.value
                    )
                    repository.insertGrievance(grievance)

                    repository.insertLog(AuditLogEntity(
                        timestamp = _virtualTime.value,
                        entityType = "CITIZEN_GRIEVANCE",
                        entityId = 0,
                        action = "GRIEVANCE_AUTO_INGEST",
                        details = "Citizen grievance '$trackingId' successfully analyzed & categorized via AI Gemini. Escrued escalation route: $parsedEscalation."
                    ))
                } catch (e: Exception) {
                    _aiError.value = "Failed parsing response: ${e.message}"
                }
            }
            _aiLoading.value = false
        }
    }

    // Resolve a Citizen Grievance
    fun resolveGrievance(grievance: CitizenGrievanceEntity) {
        viewModelScope.launch {
            val updated = grievance.copy(status = "Resolved")
            repository.updateGrievance(updated)

            repository.insertLog(AuditLogEntity(
                timestamp = _virtualTime.value,
                entityType = "CITIZEN_GRIEVANCE",
                entityId = grievance.id,
                action = "GRIEVANCE_RESOLVED",
                details = "Grievance ID '${grievance.trackingId}' marked as RESOLVED. Settlement recorded on public tracking dashboard."
            ))
        }
    }

    // Trigger physical maintenance repair bypass
    fun triggerMaintenanceRepair(component: MaintenanceComponentEntity) {
        viewModelScope.launch {
            val updated = component.copy(
                failureProbability = 0.05f,
                alertLevel = "HEALTHY",
                lastAuditTime = _virtualTime.value
            )
            repository.updateComponent(updated)

            repository.insertLog(AuditLogEntity(
                timestamp = _virtualTime.value,
                entityType = "INFRASTRUCTURE",
                entityId = component.id,
                action = "REPAIR_SCHEDULED",
                details = "Physical emergency repair pathway successfully triggered for '${component.name}'. Components set to target HEALTHY status."
            ))
        }
    }
}

