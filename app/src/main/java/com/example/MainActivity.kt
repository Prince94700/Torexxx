package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.TorexViewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TorexApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TorexApp() {
    val context = LocalContext.current
    val viewModel: TorexViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    // Database state flows
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val landParcels by viewModel.landParcels.collectAsStateWithLifecycle()
    val logs by viewModel.auditLogs.collectAsStateWithLifecycle()
    val virtualTime by viewModel.virtualTime.collectAsStateWithLifecycle()
    val maintenanceComponents by viewModel.maintenanceComponents.collectAsStateWithLifecycle()
    val citizenGrievances by viewModel.citizenGrievances.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf("dashboard") }

    // Floating Panel State for Audit Ledger Log Drawer
    var showLedgerDrawer by remember { mutableStateOf(false) }

    // Dialog state controllers
    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(
                modifier = Modifier
                    .background(TorexSurfaceGlass)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TorexSaffronPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = "TOREX Logo",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "TOREX",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = TorexGold,
                                letterSpacing = (-0.5).sp,
                                modifier = Modifier.testTag("app_title")
                            )
                            Text(
                                text = "ADMIN INTELLIGENCE LAYER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = TorexSaffronPrimary.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Administrative Clock & Federated Sync Badge
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(TorexSurfaceGlassSelected)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(TorexDharmaGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "SYNC ${formatTime(virtualTime)}",
                            color = TorexTextPrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                HorizontalDivider(color = TorexBorderColor, thickness = 1.dp)

                // Virtual Clock fast-forward controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TorexSurfaceGlass)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SIMULATOR SPEEDUP:",
                        fontSize = 10.sp,
                        color = TorexTextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.weight(1.0f))
                    Button(
                        onClick = { viewModel.advanceClockByMinutes(1) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("btn_ff_1m"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TorexSurfaceGlassSelected,
                            contentColor = TorexSaffronPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+1 min", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.advanceClockByMinutes(5) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("btn_ff_5m"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TorexSurfaceGlassSelected,
                            contentColor = TorexSaffronPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+5 min", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.advanceClockByMinutes(120) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(26.dp)
                            .testTag("btn_ff_2h"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TorexSurfaceGlassSelected,
                            contentColor = TorexSaffronPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("+2 hrs", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = TorexBorderColor, thickness = 1.dp)
            }
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = TorexBorderColor, thickness = 1.dp)
                NavigationBar(
                    containerColor = TorexSurfaceGlass,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets.navigationBars
                ) {
                    NavigationBarItem(
                        selected = activeTab == "dashboard",
                        onClick = { activeTab = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Systems", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TorexSaffronPrimary,
                            selectedTextColor = TorexSaffronPrimary,
                            unselectedIconColor = TorexTextSecondary,
                            unselectedTextColor = TorexTextSecondary,
                            indicatorColor = TorexSurfaceGlassSelected
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )
                    NavigationBarItem(
                        selected = activeTab == "pipeline",
                        onClick = { activeTab = "pipeline" },
                        icon = {
                            BadgedBox(badge = {
                                val activeCount = tasks.count { it.status == "PENDING" }
                                if (activeCount > 0) {
                                    Badge(
                                        containerColor = TorexAlertRed,
                                        contentColor = Color.White
                                    ) {
                                        Text(activeCount.toString(), fontSize = 9.sp)
                                    }
                                }
                            }) {
                                Icon(Icons.Default.PendingActions, contentDescription = "Pipeline")
                            }
                        },
                        label = { Text("Pipeline", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TorexSaffronPrimary,
                            selectedTextColor = TorexSaffronPrimary,
                            unselectedIconColor = TorexTextSecondary,
                            unselectedTextColor = TorexTextSecondary,
                            indicatorColor = TorexSurfaceGlassSelected
                        ),
                        modifier = Modifier.testTag("tab_pipeline")
                    )
                    NavigationBarItem(
                        selected = activeTab == "valuation",
                        onClick = { activeTab = "valuation" },
                        icon = { Icon(Icons.Default.Map, contentDescription = "Valuation") },
                        label = { Text("Settlement", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TorexSaffronPrimary,
                            selectedTextColor = TorexSaffronPrimary,
                            unselectedIconColor = TorexTextSecondary,
                            unselectedTextColor = TorexTextSecondary,
                            indicatorColor = TorexSurfaceGlassSelected
                        ),
                        modifier = Modifier.testTag("tab_valuation")
                    )
                    NavigationBarItem(
                        selected = activeTab == "ai_assist",
                        onClick = { activeTab = "ai_assist" },
                        icon = { Icon(Icons.Default.Psychology, contentDescription = "AI Assist") },
                        label = { Text("Audit", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TorexSaffronPrimary,
                            selectedTextColor = TorexSaffronPrimary,
                            unselectedIconColor = TorexTextSecondary,
                            unselectedTextColor = TorexTextSecondary,
                            indicatorColor = TorexSurfaceGlassSelected
                        ),
                        modifier = Modifier.testTag("tab_ai_assist")
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == "dashboard") {
                FloatingActionButton(
                    onClick = { showAddProjectDialog = true },
                    containerColor = TorexSaffronPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_project")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Project File")
                }
            } else if (activeTab == "pipeline") {
                FloatingActionButton(
                    onClick = { showAddTaskDialog = true },
                    containerColor = TorexSaffronPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(Icons.Default.PlaylistAdd, contentDescription = "Add Clearance File")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(TorexCanvasBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // Top level metric widgets
                MetricsBand(projects, tasks, landParcels, onVerifyLogClicked = { showLedgerDrawer = true })

                // Selected Tab view representation
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = activeTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "tab_transition"
                    ) { tab ->
                        when (tab) {
                            "dashboard" -> DashboardScreen(
                                projects = projects,
                                tasks = tasks,
                                maintenanceComponents = maintenanceComponents,
                                citizenGrievances = citizenGrievances,
                                onAddProjectClick = { showAddProjectDialog = true },
                                onRunPredictiveAssessment = { viewModel.runPredictiveAssessment(it) },
                                onSubmitGrievance = { name, loc, text, cat -> viewModel.submitCitizenGrievance(name, loc, text, cat) },
                                onResolveGrievance = { viewModel.resolveGrievance(it) },
                                onRepairComponent = { viewModel.triggerMaintenanceRepair(it) },
                                aiLoading = viewModel.aiLoading.collectAsStateWithLifecycle().value,
                                aiResponse = viewModel.aiResponse.collectAsStateWithLifecycle().value,
                                aiError = viewModel.aiError.collectAsStateWithLifecycle().value,
                                onClearAIResult = { viewModel.clearAIState() }
                            )
                            "pipeline" -> PipelineScreen(
                                tasks = tasks,
                                projects = projects,
                                virtualTime = virtualTime,
                                onApprove = { viewModel.approveTask(it) },
                                onReject = { task, reason -> viewModel.rejectTask(task, reason) },
                                onManualEscalate = { viewModel.forceEscalateTask(it) }
                            )
                            "valuation" -> LandValuationScreen(
                                parcels = landParcels,
                                onSettle = { viewModel.settleLandCompensation(it) },
                                onUpdateParcel = { viewModel.updateLandParcel(it) }
                            )
                            "ai_assist" -> AIAssistantScreen(
                                viewModel = viewModel,
                                activeProjectsList = projects
                            )
                        }
                    }
                }
            }

            // Expanding Drawer Panel for Immutable Verification Ledger Log (Simulated PostGIS / Cryptographic state)
            if (showLedgerDrawer) {
                LedgerLogDrawer(
                    logs = logs,
                    onDismiss = { showLedgerDrawer = false },
                    onClearLogs = { coroutineScope.launch { viewModel.repository.clearAllLogs() } }
                )
            }
        }
    }

    // Add Project Dialog Form
    if (showAddProjectDialog) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onSave = { entity ->
                coroutineScope.launch {
                    viewModel.repository.insertProject(entity)
                    Toast.makeText(context, "New Project File registered under TOREX National Hub.", Toast.LENGTH_SHORT).show()
                }
                showAddProjectDialog = false
            }
        )
    }

    // Add Clearance Task Dialog Form
    if (showAddTaskDialog) {
        AddTaskDialog(
            projectsList = projects,
            onDismiss = { showAddTaskDialog = false },
            onSave = { entity ->
                coroutineScope.launch {
                    viewModel.repository.insertTask(entity)
                    Toast.makeText(context, "Clearance milestone timeline registered & armed.", Toast.LENGTH_SHORT).show()
                }
                showAddTaskDialog = false
            }
        )
    }
}

// Global Metric Summary Board
@Composable
fun MetricsBand(
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    landParcels: List<LandParcelEntity>,
    onVerifyLogClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
        border = BorderStroke(1.dp, TorexBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                MetricItem("Files", projects.size.toString(), TorexAshokaBlue)
                
                val escCount = tasks.count { it.status == "ESCALATED" }
                MetricItem(
                    label = "Escalated",
                    value = escCount.toString(),
                    color = if (escCount > 0) TorexAlertRed else TorexTextSecondary
                )

                val activeVal = landParcels.sumOf { if (it.compensationPaid) 0.0 else it.calculateFairCompensation() }
                val croreVal = activeVal / 10_000_000.0
                MetricItem("Pending Payouts", "₹%.1f Cr".format(croreVal), TorexSaffronPrimary)
            }

            IconButton(
                onClick = onVerifyLogClicked,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(TorexSurfaceGlassSelected)
                    .size(40.dp)
                    .testTag("btn_audit_ledger")
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = "View Integrity Log",
                    tint = TorexGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RowScope.MetricItem(label: String, value: String, color: Color) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TorexTextSecondary, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

// 1. DASHBOARD COMPONENT WITH MULTI-MODULE SUB-TABS
@Composable
fun DashboardScreen(
    projects: List<ProjectEntity>,
    tasks: List<TaskEntity>,
    maintenanceComponents: List<MaintenanceComponentEntity>,
    citizenGrievances: List<CitizenGrievanceEntity>,
    onAddProjectClick: () -> Unit,
    onRunPredictiveAssessment: (MaintenanceComponentEntity) -> Unit,
    onSubmitGrievance: (String, String, String, String) -> Unit,
    onResolveGrievance: (CitizenGrievanceEntity) -> Unit,
    onRepairComponent: (MaintenanceComponentEntity) -> Unit,
    aiLoading: Boolean,
    aiResponse: String?,
    aiError: String?,
    onClearAIResult: () -> Unit
) {
    var selectedSubTab by remember { mutableStateOf("projects") }

    Column(modifier = Modifier.fillMaxSize()) {
        // High-Fidelity Subsegment Navigation Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(TorexSurfaceGlassSelected)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(
                "projects" to "🏛️ Projects",
                "maintenance" to "⚡ Maintenance",
                "grievances" to "📣 Grievances"
            ).forEach { (tabId, label) ->
                val isSelected = selectedSubTab == tabId
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) TorexSaffronPrimary else Color.Transparent)
                        .clickable { selectedSubTab = tabId }
                        .padding(vertical = 10.dp)
                        .testTag("subtab_$tabId"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.White else TorexTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Dynamic Subsegment Content Areas
        when (selectedSubTab) {
            "projects" -> {
                if (projects.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = "No project",
                                tint = TorexTextSecondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Registered Infrastructure Projects", color = TorexTextPrimary, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Pre-population seed files are booting up, or click addition icon to start.", color = TorexTextSecondary, textAlign = TextAlign.Center, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onAddProjectClick, colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary)) {
                                Text("Register Project File", color = Color.White)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val approvedCount = tasks.count { it.status == "APPROVED" }
                                val totalTasks = tasks.size
                                val velocityPct = if (totalTasks == 0) 84 else (approvedCount * 100) / totalTasks

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFFD0BCFF))
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Speed,
                                            contentDescription = "Speed",
                                            tint = Color(0xFF21005D),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "$velocityPct%",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Light,
                                                color = Color(0xFF21005D),
                                                fontFamily = FontFamily.SansSerif
                                            )
                                            Text(
                                                text = "Execution Velocity",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF21005D).copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1.2f)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(Color(0xFFEADDFF))
                                        .padding(14.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timeline,
                                            contentDescription = "Timeline",
                                            tint = Color(0xFF21005D),
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(
                                                text = "14d",
                                                fontSize = 28.sp,
                                                fontWeight = FontWeight.Light,
                                                color = Color(0xFF21005D),
                                                fontFamily = FontFamily.SansSerif
                                            )
                                            Text(
                                                text = "Avg. Cycle Time",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF21005D).copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "FEDERATED PROJECT FILES",
                                fontSize = 12.sp,
                                color = TorexTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        items(projects, key = { it.id }) { project ->
                            val projectTasks = tasks.filter { it.projectId == project.id }
                            ProjectCard(project = project, relevantTasks = projectTasks)
                        }
                    }
                }
            }

            "maintenance" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlassSelected),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, TorexBorderColor.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(TorexSaffronPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Engineering,
                                        contentDescription = "Engineering Info",
                                        tint = TorexSaffronPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Predictive Infrastructure Maintenance Hub actively monitors telemetry inputs, age metrics, and GIS layouts to model risks and mitigate failures proactively.",
                                    color = TorexTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Selected AI Intelligence Insight expander
                    if (aiLoading || aiResponse != null || aiError != null) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
                                border = BorderStroke(1.dp, TorexSaffronPrimary),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "⚙️ TOREX REAL-TIME AI OUTCOME",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TorexGold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        IconButton(
                                            onClick = onClearAIResult,
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear",
                                                tint = TorexTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    if (aiLoading) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                color = TorexSaffronPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "Saffron ML evaluation running...",
                                                color = TorexTextSecondary,
                                                fontSize = 13.sp
                                            )
                                        }
                                    } else if (aiError != null) {
                                        Text(
                                            text = aiError,
                                            color = TorexAlertRed,
                                            fontSize = 13.sp
                                        )
                                    } else if (aiResponse != null) {
                                        Text(
                                            text = aiResponse,
                                            color = TorexTextPrimary,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "CRITICAL COG ALERTS & FAILURE PREDICTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TorexTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Render maintenance components list
                    items(maintenanceComponents, key = { it.id }) { component ->
                        MaintenanceComponentCard(
                            component = component,
                            onRunPrediction = onRunPredictiveAssessment,
                            onRepair = onRepairComponent,
                            aiLoading = aiLoading
                        )
                    }
                }
            }

            "grievances" -> {
                var citizenNameInput by remember { mutableStateOf("") }
                var locationInput by remember { mutableStateOf("") }
                var complaintTextInput by remember { mutableStateOf("") }
                var categoryInput by remember { mutableStateOf("Water") }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
                            border = BorderStroke(1.dp, TorexBorderColor),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "🏛️ INGEST CITIZEN COMPLAINT (API NODE GATEWAY)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = TorexGold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = citizenNameInput,
                                    onValueChange = { citizenNameInput = it },
                                    label = { Text("Citizen Full Name", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TorexTextPrimary,
                                        unfocusedTextColor = TorexTextPrimary,
                                        focusedBorderColor = TorexSaffronPrimary,
                                        unfocusedBorderColor = TorexBorderColor,
                                        focusedLabelColor = TorexSaffronPrimary,
                                        unfocusedLabelColor = TorexTextSecondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_citizen_name"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = locationInput,
                                    onValueChange = { locationInput = it },
                                    label = { Text("Ward / Location (District, State)", fontSize = 12.sp) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TorexTextPrimary,
                                        unfocusedTextColor = TorexTextPrimary,
                                        focusedBorderColor = TorexSaffronPrimary,
                                        unfocusedBorderColor = TorexBorderColor,
                                        focusedLabelColor = TorexSaffronPrimary,
                                        unfocusedLabelColor = TorexTextSecondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_grievance_location"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = complaintTextInput,
                                    onValueChange = { complaintTextInput = it },
                                    label = { Text("Describe Infrastructure Blockage / Defect", fontSize = 12.sp) },
                                    minLines = 3,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = TorexTextPrimary,
                                        unfocusedTextColor = TorexTextPrimary,
                                        focusedBorderColor = TorexSaffronPrimary,
                                        unfocusedBorderColor = TorexBorderColor,
                                        focusedLabelColor = TorexSaffronPrimary,
                                        unfocusedLabelColor = TorexTextSecondary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_grievance_text"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "SELECT PRIMARY SECTOR CLASSIFICATION:",
                                    fontSize = 10.sp,
                                    color = TorexTextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("Water", "Power", "Transit").forEach { cat ->
                                        val isChosen = categoryInput == cat
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .border(
                                                    1.dp,
                                                    if (isChosen) TorexSaffronPrimary else TorexBorderColor,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .background(if (isChosen) TorexSaffronPrimary.copy(alpha = 0.15f) else Color.Transparent)
                                                .clickable { categoryInput = cat }
                                                .padding(vertical = 8.dp)
                                                .testTag("cat_button_$cat"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = cat,
                                                color = if (isChosen) TorexSaffronPrimary else TorexTextSecondary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (citizenNameInput.isNotEmpty() && locationInput.isNotEmpty() && complaintTextInput.isNotEmpty()) {
                                            onSubmitGrievance(citizenNameInput, locationInput, complaintTextInput, categoryInput)
                                            citizenNameInput = ""
                                            locationInput = ""
                                            complaintTextInput = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("submit_citizen_grievance"),
                                    colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary),
                                    shape = RoundedCornerShape(14.dp),
                                    enabled = !aiLoading
                                ) {
                                    if (aiLoading) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                                    } else {
                                        Icon(imageVector = Icons.Default.Campaign, contentDescription = "Submit", tint = Color.White)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Submit & Auto-Categorize (AI Deep Ingest)", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "PUBLIC REDRESSAL TRACKING BOARD (ANONYMISED UPDATE FEED)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = TorexTextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Render active grievances list
                    items(citizenGrievances, key = { it.id }) { grievance ->
                        GrievanceTrackingCard(
                            grievance = grievance,
                            onResolve = onResolveGrievance
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MaintenanceComponentCard(
    component: MaintenanceComponentEntity,
    onRunPrediction: (MaintenanceComponentEntity) -> Unit,
    onRepair: (MaintenanceComponentEntity) -> Unit,
    aiLoading: Boolean
) {
    val severityColor = when (component.alertLevel) {
        "CRITICAL" -> TorexAlertRed
        "HIGH" -> TorexGold
        "WARNING" -> Color(0xFFE28413) // Orange warning fallback
        else -> TorexDharmaGreen
    }

    val typeIcon = when (component.type) {
        "High-Tension Power Grid" -> Icons.Default.ElectricBolt
        "Water Conduit Pipe" -> Icons.Default.WaterDrop
        else -> Icons.Default.Architecture // Standard default bridge/highway icon fallback or architecture
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("maintenance_card_${component.id}"),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
        border = BorderStroke(1.dp, TorexBorderColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TorexSaffronPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (component.type == "High-Tension Power Grid") Icons.Default.ElectricBolt else if (component.type == "Water Conduit Pipe") Icons.Default.WaterDrop else Icons.Default.Architecture,
                            contentDescription = component.type,
                            tint = TorexSaffronPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = component.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TorexTextPrimary
                        )
                        Text(
                            text = component.type,
                            fontSize = 10.sp,
                            color = TorexTextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Alert Badge Status
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(severityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = component.alertLevel,
                        color = severityColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Telemeters Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TorexSurfaceGlassSelected)
                    .padding(12.dp)
            ) {
                Text(
                    text = "📡 ACTIVE SENSOR NETWORK STREAM:",
                    fontSize = 9.sp,
                    color = TorexTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = component.sensorMetrics,
                    color = TorexTextPrimary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 14.sp
                )
                if (component.urbanPlanConflict.isNotEmpty() && component.urbanPlanConflict != "None") {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Conflict",
                            tint = TorexGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "GIS Conflict: ${component.urbanPlanConflict}",
                            color = TorexGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Failure likelihood progress bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Predicted Failure Probability",
                        fontSize = 11.sp,
                        color = TorexTextSecondary
                    )
                    Text(
                        text = "${(component.failureProbability * 100).toInt()}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = severityColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { component.failureProbability },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = severityColor,
                    trackColor = TorexBorderColor
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Estimates row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("EST. REPAIR COST", fontSize = 8.sp, color = TorexTextSecondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("₹${"%.2f".format(component.estRepairCostCr)} Cr", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TorexTextPrimary)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("EST. TIMELINE", fontSize = 8.sp, color = TorexTextSecondary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Text("${component.estRepairDays} Days", fontSize = 13.sp, fontWeight = FontWeight.Black, color = TorexTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action triggers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onRunPrediction(component) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_ai_predict_${component.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TorexSurfaceGlassSelected),
                    border = BorderStroke(1.dp, TorexSaffronPrimary),
                    contentPadding = PaddingValues(8.dp),
                    enabled = !aiLoading
                ) {
                    Icon(imageVector = Icons.Default.Psychology, contentDescription = "AI Assessment", tint = TorexSaffronPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Assessment", fontSize = 11.sp, color = TorexSaffronPrimary, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onRepair(component) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_repair_infra_${component.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Build, contentDescription = "Manual Override Bypass", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Arm Repair Bypass", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GrievanceTrackingCard(
    grievance: CitizenGrievanceEntity,
    onResolve: (CitizenGrievanceEntity) -> Unit
) {
    val statusColor = when (grievance.status) {
        "Resolved" -> TorexDharmaGreen
        "Action Scheduled" -> TorexGold
        "Under Investigation" -> TorexSaffronPrimary
        else -> TorexTextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("grievance_card_${grievance.id}"),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
        border = BorderStroke(1.dp, TorexBorderColor),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = grievance.trackingId,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = TorexGold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Location: ${grievance.location}",
                        fontSize = 11.sp,
                        color = TorexTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = grievance.status,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Complaint original text
            Text(
                text = "“${grievance.complaintText}”",
                fontSize = 12.sp,
                color = TorexTextPrimary,
                fontFamily = FontFamily.SansSerif,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Action resolution suggestion
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(TorexSurfaceGlassSelected)
                    .padding(12.dp)
            ) {
                Text(
                    text = "🔧 AUTOMATED G2G AI PROPOSED ACTION:",
                    fontSize = 9.sp,
                    color = TorexSaffronPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = grievance.proposedAction,
                    color = TorexTextPrimary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "📌 G2G PUBLIC ESCALATION CHANNEL PATHWAY:",
                    fontSize = 9.sp,
                    color = TorexTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = grievance.escalationPath,
                    color = TorexGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (grievance.status != "Resolved") {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = { onResolve(grievance) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .testTag("btn_resolve_grievance_${grievance.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TorexDharmaGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Authorize Resolution", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Execute Resolution & Sync Ledger", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
fun ProjectCard(project: ProjectEntity, relevantTasks: List<TaskEntity>) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (project.status) {
        "Active" -> TorexDharmaGreen
        "Delayed" -> TorexGold
        "Blocked" -> TorexAlertRed
        else -> TorexTextSecondary
    }

    val layerColor = when (project.layer) {
        "National" -> TorexSaffronPrimary
        "State" -> TorexAshokaBlue
        else -> TorexTextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .testTag("project_card_${project.id}"),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, TorexBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(layerColor.copy(alpha = 0.2f))
                            .border(1.dp, layerColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = project.layer.uppercase(),
                            fontSize = 9.sp,
                            color = layerColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = project.sector,
                        fontSize = 11.sp,
                        color = TorexTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(statusColor.copy(alpha = 0.1f))
                        .border(1.dp, statusColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = project.status.uppercase(),
                        fontSize = 9.sp,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = project.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TorexTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Agency: ${project.executingAgency}",
                    fontSize = 12.sp,
                    color = TorexTextSecondary
                )
                Text(
                    text = "Budget: ₹${project.budgetCr.toInt()} Cr",
                    fontSize = 12.sp,
                    color = TorexTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Custom styled progress bar representing completion index
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cumulative Clearance Progress", fontSize = 11.sp, color = TorexTextSecondary)
                    Text("${(project.progress * 100).toInt()}%", fontSize = 11.sp, color = TorexDharmaGreen, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                LinearProgressIndicator(
                    progress = { project.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = TorexDharmaGreen,
                    trackColor = TorexBorderColor
                )
            }

            // Expanded detail section
            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = TorexBorderColor)
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Description:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TorexTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = project.description,
                    fontSize = 13.sp,
                    color = TorexTextPrimary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Location:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = TorexTextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = project.location,
                        fontSize = 13.sp,
                        color = TorexTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Timeline Milestones Checklist (${relevantTasks.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = TorexTextSecondary,
                    fontFamily = FontFamily.Monospace
                )

                if (relevantTasks.isEmpty()) {
                    Text(
                        text = "No pending milestones armed. Ready for execution.",
                        fontSize = 12.sp,
                        color = TorexDharmaGreen,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                    relevantTasks.forEach { task ->
                        val taskBg = when (task.status) {
                            "APPROVED" -> TorexDharmaGreen.copy(alpha = 0.1f)
                            "ESCALATED" -> TorexAlertRed.copy(alpha = 0.1f)
                            "REJECTED" -> TorexAlertRed.copy(alpha = 0.05f)
                            else -> TorexSurfaceGlassSelected
                        }
                        val borderCol = when (task.status) {
                            "APPROVED" -> TorexDharmaGreen
                            "ESCALATED" -> TorexAlertRed
                            else -> TorexBorderColor
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(taskBg)
                                .border(1.dp, borderCol, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(0.7f)) {
                                Text(task.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TorexTextPrimary)
                                Text(
                                    text = "Officer: ${task.assignedOfficer}",
                                    fontSize = 10.sp,
                                    color = TorexTextSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(borderCol.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.status,
                                    fontSize = 8.sp,
                                    color = if (task.status == "PENDING") TorexTextPrimary else borderCol,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 2. TIMELINE ENFORCED EXECUTION PIPELINE
@Composable
fun PipelineScreen(
    tasks: List<TaskEntity>,
    projects: List<ProjectEntity>,
    virtualTime: Long,
    onApprove: (TaskEntity) -> Unit,
    onReject: (TaskEntity, String) -> Unit,
    onManualEscalate: (TaskEntity) -> Unit
) {
    var rejectReasonDialogTask by remember { mutableStateOf<TaskEntity?>(null) }
    var rejectReasonText by remember { mutableStateOf("") }
    
    val pendingTasks = tasks.filter { it.status == "PENDING" }
    val escalatedTasks = tasks.filter { it.status == "ESCALATED" }
    val otherTasks = tasks.filter { it.status == "APPROVED" || it.status == "REJECTED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = TorexSaffronPrimary.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, TorexSaffronPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = "Deadlines", tint = TorexSaffronPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTOMATIC ESCALATION ENGINE",
                            fontWeight = FontWeight.Bold,
                            color = TorexSaffronPrimary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tasks pending clearance have dynamic real-time countdown meters. If countdown expires, TOREX instantly escalates file responsibility, updates logs, and flags bottlenecks.",
                        fontSize = 11.sp,
                        color = TorexTextPrimary
                    )
                }
            }
        }

        // 1. ESCALATED SECTOR (CRITICAL IN RED)
        if (escalatedTasks.isNotEmpty()) {
            item {
                Text(
                    text = "⚠️ ESCALATED CRITICAL BOTTLENECK DOSSIERS",
                    fontSize = 12.sp,
                    color = TorexAlertRed,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(escalatedTasks, key = { it.id }) { task ->
                val linkedProj = projects.find { it.id == task.projectId }
                EscalatedTaskCard(task = task, project = linkedProj)
            }
        }

        // 2. ACTIVE PENDING CHANNELS
        item {
            Text(
                text = "⏳ ACTIVE MONITORING CHANNELS (${pendingTasks.size})",
                fontSize = 12.sp,
                color = TorexTextSecondary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        if (pendingTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
                    border = BorderStroke(1.dp, TorexBorderColor)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No active clear files pending. Clean pipeline execution.", color = TorexDharmaGreen, textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                }
            }
        } else {
            items(pendingTasks, key = { it.id }) { task ->
                val linkedProj = projects.find { it.id == task.projectId }
                PendingTaskCard(
                    task = task,
                    project = linkedProj,
                    currentVirtualTime = virtualTime,
                    onApprove = { onApprove(task) },
                    onReject = { rejectReasonDialogTask = task },
                    onManualEscalate = { onManualEscalate(task) }
                )
            }
        }

        // 3. SECURED RESOLUTIONS (ARCHIVE)
        if (otherTasks.isNotEmpty()) {
            item {
                Text(
                    text = "🔒 SECURED RESOLUTIONS (HISTORIC AUDIT)",
                    fontSize = 12.sp,
                    color = TorexTextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            items(otherTasks, key = { it.id }) { task ->
                val linkedProj = projects.find { it.id == task.projectId }
                CompletedTaskCard(task, linkedProj)
            }
        }
    }

    // Reject Reason Input Dialog
    if (rejectReasonDialogTask != null) {
        Dialog(onDismissRequest = { rejectReasonDialogTask = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TorexSurfaceGlass,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TorexBorderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "REJECTION DIRECTIVE FILE",
                        fontWeight = FontWeight.Bold,
                        color = TorexSaffronPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Specify legal or technical correction protocol reference to return file '${rejectReasonDialogTask?.name}' back directly to officer.",
                        fontSize = 12.sp,
                        color = TorexTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reject_reason_input"),
                        placeholder = { Text("e.g., Circular Section 14B Overlap Error", fontSize = 12.sp, color = TorexTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TorexTextPrimary,
                            unfocusedTextColor = TorexTextPrimary,
                            focusedBorderColor = TorexSaffronPrimary,
                            unfocusedBorderColor = TorexBorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { rejectReasonDialogTask = null; rejectReasonText = "" }) {
                            Text("Cancel", color = TorexTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val reason = if (rejectReasonText.trim().isEmpty()) "Unspecified Protocol Clearance Issue" else rejectReasonText
                                rejectReasonDialogTask?.let { onReject(it, reason) }
                                rejectReasonDialogTask = null
                                rejectReasonText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TorexAlertRed)
                        ) {
                            Text("Reject File", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PendingTaskCard(
    task: TaskEntity,
    project: ProjectEntity?,
    currentVirtualTime: Long,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onManualEscalate: () -> Unit
) {
    val countdownText = getCountdownText(task.deadlineTimestamp, currentVirtualTime)
    val criticalThreshold = (task.deadlineTimestamp - currentVirtualTime) < 60000L // Under 1 minute is flashing red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_task_card_${task.id}"),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
        border = BorderStroke(1.dp, if (criticalThreshold) TorexAlertRed else TorexBorderColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Project: ${project?.name ?: "TOREX Node Hub"}",
                    fontSize = 11.sp,
                    color = TorexTextSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.6f)
                )

                // Flash Countdown Counter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (criticalThreshold) TorexAlertRed.copy(alpha = 0.15f) else TorexSaffronPrimary.copy(alpha = 0.10f))
                        .border(1.dp, if (criticalThreshold) TorexAlertRed else TorexSaffronPrimary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "LOCKS IN: $countdownText",
                        fontSize = 10.sp,
                        color = if (criticalThreshold) TorexAlertRed else TorexSaffronPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = task.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TorexTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SupervisedUserCircle,
                    contentDescription = "Officer icon",
                    tint = TorexTextSecondary,
                    modifier = Modifier.size(16.dp)

                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Assigned: ${task.assignedOfficer}",
                    fontSize = 12.sp,
                    color = TorexTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Admin Command Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1.0f)
                        .height(36.dp)
                        .testTag("btn_approve_${task.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = TorexDharmaGreen),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Approve", modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Approve", fontSize = 11.sp, color = Color.White)
                }

                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier
                        .weight(1.0f)
                        .height(36.dp)
                        .testTag("btn_reject_${task.id}"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TorexAlertRed),
                    border = BorderStroke(1.dp, TorexAlertRed),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(16.dp), tint = TorexAlertRed)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reject", fontSize = 11.sp, color = TorexAlertRed)
                }

                IconButton(
                    onClick = onManualEscalate,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(TorexSaffronPrimary.copy(alpha = 0.15f))
                        .border(1.dp, TorexSaffronPrimary, RoundedCornerShape(6.dp))
                        .testTag("btn_escalate_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PriorityHigh,
                        contentDescription = "Escalate Directly",
                        tint = TorexSaffronPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EscalatedTaskCard(task: TaskEntity, project: ProjectEntity?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TorexAlertRed.copy(alpha = 0.04f)),
        border = BorderStroke(1.dp, TorexAlertRed)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Project: ${project?.name ?: "TOREX Central Block"}",
                    fontSize = 11.sp,
                    color = TorexTextSecondary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.6f)
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(TorexAlertRed.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    val targetLevel = when (task.escalationCount) {
                        1 -> "STATE-LEVEL COMM"
                        2 -> "PMO NATIONAL"
                        else -> "CABINET"
                    }
                    Text(
                        text = "ESCALATED TO: $targetLevel",
                        fontSize = 9.sp,
                        color = TorexAlertRed,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = task.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TorexTextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned Offender: ${task.assignedOfficer}",
                    fontSize = 11.sp,
                    color = TorexTextSecondary
                )
                Text(
                    text = "L${task.escalationCount} Escalation",
                    fontSize = 11.sp,
                    color = TorexSaffronPrimary,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = TorexBorderColor)
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "info",
                    tint = TorexAlertRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Auto-Escalation Protocol armed. Accountability reports exported.",
                    fontSize = 11.sp,
                    color = TorexTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun CompletedTaskCard(task: TaskEntity, project: ProjectEntity?) {
    val isApproved = task.status == "APPROVED"
    val accentColor = if (isApproved) TorexDharmaGreen else TorexTextSecondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass.copy(alpha = 0.40f)),
        border = BorderStroke(1.dp, TorexBorderColor.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(0.7f)) {
                Text(
                    text = task.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TorexTextSecondary
                )
                Text(
                    text = "Project: ${project?.name ?: "Cleared Block"}",
                    fontSize = 11.sp,
                    color = TorexTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.1f))
                    .border(1.dp, accentColor, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = task.status,
                    fontSize = 9.sp,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// 3. LAND INTELLIGENCE AND DYNAMIC VALUATION VIEW
@Composable
fun LandValuationScreen(
    parcels: List<LandParcelEntity>,
    onSettle: (LandParcelEntity) -> Unit,
    onUpdateParcel: (LandParcelEntity) -> Unit
) {
    var expandedParcelId by remember { mutableStateOf<Int?>(null) }
    var showEditNotesParcel by remember { mutableStateOf<LandParcelEntity?>(null) }
    var notesText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = TorexAshokaBlue.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, TorexAshokaBlue)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = "LARR Compensation", tint = TorexAshokaBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "LARR DYNAMIC COMPENSATION MULTIPLIER",
                            fontWeight = FontWeight.Bold,
                            color = TorexAshokaBlue,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Static Circle Rates are bypassed. TOREX calculates compensation algorithmically based on transport distance, localized commercial velocity multipliers, and physical structures with Direct Bank settlement.",
                        fontSize = 11.sp,
                        color = TorexTextPrimary
                    )
                }
            }
        }

        item {
            Text(
                text = "🗺️ GEOGRAPHIC PLOT SURVEYS",
                fontSize = 12.sp,
                color = TorexTextSecondary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        items(parcels, key = { it.id }) { parcel ->
            val isExpanded = expandedParcelId == parcel.id

            val verColor = when (parcel.verifiedStatus) {
                "Settled" -> TorexDharmaGreen
                "Clean" -> TorexAshokaBlue
                "Overlap Dispute" -> TorexAlertRed
                else -> TorexGold
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedParcelId = if (isExpanded) null else parcel.id }
                    .testTag("land_parcel_card_${parcel.id}"),
                colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
                border = BorderStroke(1.dp, TorexBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = parcel.surveyNumber,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TorexTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.6f)
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(verColor.copy(alpha = 0.15f))
                                .border(1.dp, verColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = parcel.verifiedStatus.uppercase(),
                                fontSize = 9.sp,
                                color = verColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Register Owner: ${parcel.ownerName}",
                            fontSize = 12.sp,
                            color = TorexTextSecondary
                        )
                        Text(
                            text = "${parcel.areaAcres} Acres",
                            fontSize = 12.sp,
                            color = TorexTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = TorexBorderColor)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Algorithm valuation breakdown
                        Text(
                            text = "DYNAMIC VALUATION LEDGER MULTIPLIERS",
                            fontSize = 11.sp,
                            color = TorexGold,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Multiplier items
                        ValuationRow("Base Circle Rate per Acre", "₹${formatCurrency(parcel.baseCircleRate)}")
                        
                        val proxFactor = when {
                            parcel.distanceToHighwayKm <= 1.0 -> "2.5x (Immediate Edge)"
                            parcel.distanceToHighwayKm <= 5.0 -> "1.8x (Secondary Corridor)"
                            else -> "1.2x"
                        }
                        ValuationRow("Distance to Corridor (${parcel.distanceToHighwayKm}km)", proxFactor)
                        ValuationRow("Economic Velocity Score (${parcel.economicActivityScore}/10)", "${1.0 + (parcel.economicActivityScore / 10.0)}x multiplier")
                        ValuationRow("Validated Structures Value Added", "₹${formatCurrency(parcel.structuralValueINR)}")
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = TorexBorderColor, modifier = Modifier.alpha(0.5f))
                        Spacer(modifier = Modifier.height(8.dp))

                        val dynamicCalc = parcel.calculateDynamicValue()
                        val payoutValue = parcel.calculateFairCompensation()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Standard Dynamic Base Land Value", fontSize = 11.sp, color = TorexTextSecondary)
                            Text("₹${formatCurrency(dynamicCalc)}", fontSize = 12.sp, color = TorexTextPrimary, fontFamily = FontFamily.Monospace)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Net compensation payout (2.0x LARR Bonus)", fontSize = 12.sp, color = TorexSaffronPrimary, fontWeight = FontWeight.Bold)
                            Text("₹${formatCurrency(payoutValue)}", fontSize = 14.sp, color = TorexSaffronPrimary, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (parcel.notes.isNotEmpty()) {
                            Text(
                                text = "Verification Registry Notes:",
                                fontSize = 10.sp,
                                color = TorexTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = parcel.notes,
                                fontSize = 12.sp,
                                color = TorexTextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Settlement Direct Benefit Transfer action row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!parcel.compensationPaid) {
                                Button(
                                    onClick = { onSettle(parcel) },
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .height(38.dp)
                                        .testTag("btn_settle_parcel_${parcel.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = TorexDharmaGreen)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = "Bank Transfer", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disburse DBT Payment", fontSize = 11.sp)
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TorexDharmaGreen.copy(alpha = 0.15f))
                                        .border(2.dp, TorexDharmaGreen, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = "paid", tint = TorexDharmaGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("SETTLED DIRECT BENEFIT TRANSFER", color = TorexDharmaGreen, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    showEditNotesParcel = parcel
                                    notesText = parcel.notes
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("btn_edit_notes_${parcel.id}"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TorexTextPrimary),
                                border = BorderStroke(1.dp, TorexBorderColor)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "notes", modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Notes Dialog
    if (showEditNotesParcel != null) {
        Dialog(onDismissRequest = { showEditNotesParcel = null }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TorexSurfaceGlass,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, TorexBorderColor, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "EDIT PLOT REGISTRY NOTES",
                        fontWeight = FontWeight.Bold,
                        color = TorexSaffronPrimary,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Plot verification updates will automatically recalculate matching metrics.",
                        fontSize = 12.sp,
                        color = TorexTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TorexTextPrimary,
                            unfocusedTextColor = TorexTextPrimary,
                            focusedBorderColor = TorexSaffronPrimary,
                            unfocusedBorderColor = TorexBorderColor
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showEditNotesParcel = null }) {
                            Text("Cancel", color = TorexTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                showEditNotesParcel?.let { parcel ->
                                    val updated = parcel.copy(notes = notesText)
                                    onUpdateParcel(updated)
                                }
                                showEditNotesParcel = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary)
                        ) {
                            Text("Save Notes", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ValuationRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = TorexTextSecondary)
        Text(text = value, fontSize = 11.sp, color = TorexTextPrimary, fontFamily = FontFamily.Monospace)
    }
}

// 4. GEMINI ADMINISTRATIVE AI ASSISTANT WORKSPACE
@Composable
fun AIAssistantScreen(
    viewModel: TorexViewModel,
    activeProjectsList: List<ProjectEntity>
) {
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()
    val aiResponse by viewModel.aiResponse.collectAsStateWithLifecycle()
    val aiError by viewModel.aiError.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    
    // Default context selection
    var activeContextType by remember { mutableStateOf("DEBOTTLENECK") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            colors = CardDefaults.cardColors(containerColor = TorexSaffronPrimary.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, TorexSaffronPrimary)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = "AI Assitance", tint = TorexSaffronPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOREX INTEL ADMINISTRATIVE LAYER",
                        fontWeight = FontWeight.Bold,
                        color = TorexSaffronPrimary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bypassing bureaucratic delays. Call Gemini AI intelligence on project files to draft legal letters, synthesize disputes settlement formulas or solve environmental roadblocks.",
                    fontSize = 11.sp,
                    color = TorexTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "SELECT INTELLIGENCE TASK CONTEXT",
            fontSize = 12.sp,
            color = TorexTextSecondary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Tab Row for Context Selection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val ctxOptions = listOf(
                Pair("DEBOTTLENECK", "Debottleneck Roadblock"),
                Pair("ESCALATION_LETTER", "Escalate Brief"),
                Pair("LAND_RESOLVE", "Resolve Land Dispute")
            )

            ctxOptions.forEach { opt ->
                val selected = activeContextType == opt.first
                Box(
                    modifier = Modifier
                        .weight(1.0f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) TorexSaffronPrimary else TorexSurfaceGlass)
                        .clickable { activeContextType = opt.first }
                        .border(1.dp, if (selected) TorexSaffronPrimary else TorexBorderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = opt.second,
                        fontSize = 10.sp,
                        color = if (selected) Color.Black else TorexTextPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "INPUT ADMINISTRATIVE COMPROMISE DETAILS",
            fontSize = 11.sp,
            color = TorexTextSecondary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_prompt_field"),
            placeholder = {
                val ph = when (activeContextType) {
                    "DEBOTTLENECK" -> "Specify blockade, e.g. 'Noida Section Solar panel land overlap boundary conflict with State Forest boundary...'"
                    "ESCALATION_LETTER" -> "e.g. 'Forest clearance delay on Project AKIC Highway, Officer Ramesh G has exceeded priority deadline 4 times...'"
                    else -> "e.g. 'Plot survey S.No 89-C Yamuna Expressway overlapping municipal water grid corridor claims...'"
                }
                Text(ph, fontSize = 12.sp, color = TorexTextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TorexTextPrimary,
                unfocusedTextColor = TorexTextPrimary,
                focusedBorderColor = TorexSaffronPrimary,
                unfocusedBorderColor = TorexBorderColor
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Prompt templates pill selectors
        Text(
            text = "QUICK TEMPLATE DIRECTIVES (TAP TO SEED)",
            fontSize = 10.sp,
            color = TorexTextSecondary,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val templateOptions = when (activeContextType) {
                "DEBOTTLENECK" -> listOf(
                    "Highway Corridor MoRTH Joint Survey Soil roadblock",
                    "Metro Phase 3 electrical utility pipeline clearance mapping conflict"
                )
                "ESCALATION_LETTER" -> listOf(
                    "IFS Officer Verma clearance delays on Highway Plot 124B",
                    "Swaminathan Telecom Physical Utility verification timeline breach"
                )
                else -> listOf(
                    "Survey 89-C Noida Yamuna Expressway overlap claims dispute",
                    "Bengaluru Swamy Devanahalli plot structure asset valuation audit"
                )
            }

            templateOptions.forEach { text ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(32.dp))
                        .background(TorexSurfaceGlassSelected)
                        .border(1.dp, TorexBorderColor, RoundedCornerShape(32.dp))
                        .clickable { searchQuery = text }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text, fontSize = 11.sp, color = TorexSaffronPrimary, maxLines = 1)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (searchQuery.trim().isNotEmpty()) {
                    viewModel.askTorexAI(searchQuery, activeContextType)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_ask_ai"),
            colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary),
            enabled = !aiLoading
        ) {
            if (aiLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.Black, strokeWidth = 2.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Analyzing Federal Databases...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Icon(Icons.Default.PlayArrow, contentDescription = "submit", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Run Administrative Intel", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Output Result Section
        if (aiResponse != null || aiError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 80.dp),
                colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlass),
                border = BorderStroke(1.dp, if (aiError != null) TorexAlertRed else TorexBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (aiError != null) "OFFLINE SECURITY ADVISORY PROTOCOL" else "RESOLUTION DIRECTIVE PRINTOUT",
                            fontWeight = FontWeight.Bold,
                            color = if (aiError != null) TorexAlertRed else TorexDharmaGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        IconButton(onClick = { viewModel.clearAIState() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "clear text", tint = TorexTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (aiError != null) {
                        Text(
                            text = aiError ?: "",
                            color = TorexAlertRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Text(
                        text = aiResponse ?: "",
                        fontSize = 13.sp,
                        color = TorexTextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

// 5. IMMUTABLE INTEGRITY AUDIT LEDGER DRAWER PANEL
@Composable
fun LedgerLogDrawer(
    logs: List<AuditLogEntity>,
    onDismiss: () -> Unit,
    onClearLogs: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            color = TorexSurfaceGlass,
            border = BorderStroke(1.dp, TorexBorderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HistoryEdu, contentDescription = "Ledger", tint = TorexGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "INTEGRITY JOURNAL LEDGER",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TorexGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "close ledger", tint = TorexTextPrimary)
                    }
                }

                Text(
                    text = "A decentralized, high-integrity administrative audit log tracking real-time automatic escalations and DBT payment disbursals.",
                    fontSize = 11.sp,
                    color = TorexTextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1.0f)) {
                    if (logs.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Journal is empty.", color = TorexTextSecondary)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(logs, key = { it.id }) { log ->
                                LogItemCard(log)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClearLogs,
                        modifier = Modifier.weight(1.0f),
                        border = BorderStroke(1.dp, TorexAlertRed.copy(alpha = 0.5f))
                    ) {
                        Text("Reset Journal Database", color = TorexAlertRed, fontSize = 11.sp)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1.0f),
                        colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary)
                    ) {
                        Text("Secure Close", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LogItemCard(log: AuditLogEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TorexSurfaceGlassSelected),
        border = BorderStroke(1.dp, TorexBorderColor)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(TorexSaffronPrimary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.action,
                        color = TorexSaffronPrimary,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Text(
                    text = formatTime(log.timestamp),
                    fontSize = 10.sp,
                    color = TorexTextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.details,
                fontSize = 11.sp,
                color = TorexTextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Signature block
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Fingerprint, contentDescription = "crypto index", tint = TorexGold, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "BLOCKCHAIN LEDGER HASH SIGNATURE: ${log.blockchainHash}",
                    fontSize = 8.sp,
                    color = TorexGold,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Dialog helper forms
@Composable
fun AddProjectDialog(onDismiss: () -> Unit, onSave: (ProjectEntity) -> Unit) {
    var name by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf("Highway") }
    var layer by remember { mutableStateOf("National") }
    var executingAgency by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    val sectors = listOf("Highway", "Metro", "Water", "Power", "Airport")
    val layers = listOf("National", "State", "Municipal")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TorexSurfaceGlass,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TorexBorderColor, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "REGISTER NEW PROJECT FILE",
                    fontWeight = FontWeight.Bold,
                    color = TorexSaffronPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("field_name")
                )

                OutlinedTextField(
                    value = executingAgency,
                    onValueChange = { executingAgency = it },
                    label = { Text("Executing Agency (e.g. NHAI)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("field_agency")
                )

                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it },
                    label = { Text("Budget Allocation (Crores)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("field_budget")
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Physical Location") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("field_location")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Milestones Overview / Scope") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("field_desc")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TorexTextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && budgetText.toDoubleOrNull() != null) {
                                val proj = ProjectEntity(
                                    name = name,
                                    sector = sector,
                                    layer = layer,
                                    executingAgency = executingAgency,
                                    budgetCr = budgetText.toDoubleOrNull() ?: 100.0,
                                    progress = 0.0f,
                                    status = "Active",
                                    description = desc,
                                    location = location
                                )
                                onSave(proj)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary)
                    ) {
                        Text("Save Project", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    projectsList: List<ProjectEntity>,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var officer by remember { mutableStateOf("") }
    var durationMinutesStr by remember { mutableStateOf("2") } // default 2 minutes for quick escalations preview!
    var selectedProject by remember { mutableStateOf<ProjectEntity?>(projectsList.firstOrNull()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = TorexSurfaceGlass,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, TorexBorderColor, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "REGISTER ARMED CLEARANCE DOSSIER",
                    fontWeight = FontWeight.Bold,
                    color = TorexSaffronPrimary,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Milestone Clearance Name (e.g. Forest Approval)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_task_field_name")
                )

                OutlinedTextField(
                    value = officer,
                    onValueChange = { officer = it },
                    label = { Text("Executing Field Officer Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_task_field_officer")
                )

                OutlinedTextField(
                    value = durationMinutesStr,
                    onValueChange = { durationMinutesStr = it },
                    label = { Text("System Enforced Deadline (Minutes)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TorexTextPrimary,
                        unfocusedTextColor = TorexTextPrimary,
                        focusedBorderColor = TorexSaffronPrimary,
                        unfocusedBorderColor = TorexBorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("add_task_field_duration")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel", color = TorexTextSecondary) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val durationMultiplier = durationMinutesStr.toIntOrNull() ?: 2
                            val selectedProjId = selectedProject?.id ?: 1
                            if (name.isNotEmpty() && officer.isNotEmpty()) {
                                val task = TaskEntity(
                                    projectId = selectedProjId,
                                    name = name,
                                    assignedOfficer = officer,
                                    deadlineTimestamp = System.currentTimeMillis() + (durationMultiplier * 60 * 1000L),
                                    status = "PENDING"
                                )
                                onSave(task)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TorexSaffronPrimary)
                    ) {
                        Text("Arm Milestones", color = Color.White)
                    }
                }
            }
        }
    }
}

// Global Currency Formatter
fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,##,###")
    return formatter.format(amount)
}

// Global Date Formatter
fun formatTime(milliseconds: Long): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(milliseconds))
}

// Global Time Remaining Meter Countdown
fun getCountdownText(deadlineMs: Long, currentMs: Long): String {
    val difference = deadlineMs - currentMs
    if (difference <= 0) return "ESCALATED AUTO-PROTOCOL"
    val totalSecs = difference / 1000
    val secs = totalSecs % 60
    val mins = (totalSecs / 60) % 60
    val hrs = (totalSecs / 3600)
    return String.format("%02d:%02d:%02d", hrs, mins, secs)
}
