package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkshopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMonitoringScreen(
    viewModel: WorkshopViewModel,
    onNavigateBack: () -> Unit
) {
    val pendingLogins by viewModel.pendingUsers.collectAsState()
    val allMrs by viewModel.allMaterialRequests.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Approval User", "Grafik Breakdown", "Backup & Laporan")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADMIN MONITORING CONSOLE", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Tab Header Bar
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = CharcoalLight,
                contentColor = ProfessionalAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = ProfessionalAccent
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Tab Content Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (selectedTabIndex) {
                    0 -> UserApprovalTab(pendingLogins, viewModel)
                    1 -> ChartsTab(allMrs)
                    2 -> ReportsBackupTab(allMrs, allUsers, pendingLogins)
                }
            }
        }
    }
}

// TAB 1: USER REGISTRATION APPROVAL FLOW
@Composable
fun UserApprovalTab(
    pendingUsers: List<UserEntity>,
    viewModel: WorkshopViewModel
) {
    if (pendingUsers.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("admin_pending_users_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendingUsers) { user ->
                Surface(
                    color = CharcoalLight,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ColorWaiting),
                    modifier = Modifier.fillMaxWidth().testTag("pending_user_card_${user.nik}")
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(user.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                Text("NIK: ${user.nik}", fontSize = 12.sp, color = Color.LightGray)
                            }

                            // Role tag
                            Box(
                                modifier = Modifier
                                    .background(ProfessionalAccent.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .border(1.dp, ProfessionalAccent, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(user.role, color = ProfessionalAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Jabatan: ${user.jabatan}", fontSize = 12.sp, color = Color.LightGray)
                            Text("Departemen: ${user.departemen}", fontSize = 12.sp, color = Color.LightGray)
                            Text("No. HP: ${user.noHp}", fontSize = 12.sp, color = Color.LightGray)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.rejectNewUser(user.nik) },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorRejected),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }

                            Button(
                                onClick = { viewModel.approveNewUser(user.nik) },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorApproved),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("approve_user_btn_${user.nik}")
                            ) {
                                Text("APPROVE", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(54.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Semua Registrasi Sudah Approved", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text("Belum ada antrian karyawan baru mendaftar.", color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// TAB 2: GLOWING BAR CHARTS (most breakdown unit, parts request)
@Composable
fun ChartsTab(mrs: List<com.example.data.model.MaterialRequestEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("STATISTIK WORKSHOP INDOMINING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)

        // 1. UNIT MOST FREQUENT BREAKDOWN
        CardChartSection(title = "Unit Paling Sering Breakdown") {
            // Count breakdowns per unit
            val unitCounts = mrs.groupBy { it.unitEquipment }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
            val maxCount = unitCounts.firstOrNull()?.second ?: 1

            if (unitCounts.isNotEmpty()) {
                unitCounts.take(4).forEach { (unitName, count) ->
                    val percentage = count.toFloat() / maxCount
                    ChartBarRow(label = unitName, valueStr = "$count Breakdown", percentage = percentage, color = ColorRejected)
                }
            } else {
                Text("Belum mendeteksi data unit breakdown.", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        // 2. PARTS MOST REQUESTED
        CardChartSection(title = "Sparepart Paling Sering Di-Request") {
            // Count parts total
            val partCounts = mrs.groupBy { it.partName }.mapValues { list -> list.value.sumOf { it.quantity } }.toList().sortedByDescending { it.second }
            val maxCount = partCounts.firstOrNull()?.second ?: 1

            if (partCounts.isNotEmpty()) {
                partCounts.take(4).forEach { (partName, totalQty) ->
                    val percentage = totalQty.toFloat() / maxCount
                    ChartBarRow(label = partName, valueStr = "$totalQty Pcs", percentage = percentage, color = ProfessionalAccent)
                }
            } else {
                Text("Belum mendeteksi data sparepart req.", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        // 3. MOST ACTIVE MECHANICS
        CardChartSection(title = "Mekanik Teraktif Lapangan") {
            val mechanicCounts = mrs.groupBy { it.mechanicName }.mapValues { it.value.size }.toList().sortedByDescending { it.second }
            val maxCount = mechanicCounts.firstOrNull()?.second ?: 1

            if (mechanicCounts.isNotEmpty()) {
                mechanicCounts.take(3).forEach { (name, count) ->
                    val percentage = count.toFloat() / maxCount
                    ChartBarRow(label = name, valueStr = "$count Selesai", percentage = percentage, color = ColorApproved)
                }
            } else {
                Text("Belum mendeteksi mekanik aktif.", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        // 4. MONTHLY BREAKDOWN BAR CHART MOCK
        CardChartSection(title = "Volume MR Selama 2026") {
            val months = listOf("Jan", "Feb", "Mar", "Apr", "Mei (Today)", "Jun")
            val counts = listOf(14, 21, 18, 35, mrs.size + 12, 0)
            val maxCount = counts.maxOrNull() ?: 1

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                months.forEachIndexed { i, month ->
                    val c = counts[i]
                    val pct = c.toFloat() / maxCount
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = c.toString(), fontSize = 9.sp, color = Color.White)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .fillMaxHeight(pct.coerceAtLeast(0.05f))
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(ColorPrDone)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = month, fontSize = 9.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

// TAB 3: BACKUP CLOUD, GENERAL REKAP STATS
@Composable
fun ReportsBackupTab(
    mrs: List<com.example.data.model.MaterialRequestEntity>,
    users: List<UserEntity>,
    pending: List<UserEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("AUDIT LAPORAN & BACKUP SYSTEM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)

        Surface(
            color = CharcoalLight,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sistem database beroperasi lokal & aman terintegrasi cloud backup.", fontSize = 12.sp, color = Color.LightGray)
                
                Divider(color = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                BackupStatText(label = "Total Material Requests (MR):", value = mrs.size.toString())
                BackupStatText(label = "Total Purchase Requests (PR):", value = mrs.count { it.prNumber != null }.toString())
                BackupStatText(label = "Karyawan Terdaftar Disetujui:", value = (users.size - pending.size).toString())
                BackupStatText(label = "Karyawan Waiting Approval:", value = pending.size.toString())
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large actions of management
        CardChartSection(title = "Pencadangan Cloud & Sync") {
            Text("Lakukan manual sync sebelum melakukan update OS atau pengalihan site.", fontSize = 11.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfessionalAccent,
                    contentColor = ProfessionalOnAccent
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AUTO BACKUP KE CLOUD (SYNC)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {},
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorApproved),
                border = BorderStroke(1.dp, ColorApproved),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.DownloadForOffline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("DOWNLOAD ALL REKAP COAL REPORT", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun CardChartSection(title: String, content: @Composable () -> Unit) {
    Surface(
        color = CharcoalLight,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ChartBarRow(label: String, valueStr: String, percentage: Float, color: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, color = Color.LightGray)
            Text(valueStr, fontSize = 12.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(Color.Gray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage.coerceIn(0.02f, 1f))
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
fun BackupStatText(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.LightGray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}
