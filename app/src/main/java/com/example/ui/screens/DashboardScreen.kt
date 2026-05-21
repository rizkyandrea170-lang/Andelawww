package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.MaterialRequestEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.NotificationItem
import com.example.ui.viewmodel.WorkshopViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: WorkshopViewModel,
    onNavigateToCreateMr: () -> Unit,
    onNavigateToMrList: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val allMrs by viewModel.allMaterialRequests.collectAsState()
    val prs by viewModel.allPurchaseRequests.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    val todayDateString = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // Aggregate statistics
    val todayMrs = allMrs.filter { it.date.startsWith(todayDateString) }
    val totalTodayMr = todayMrs.size
    
    val countWaiting = allMrs.count { it.status == "WAITING_APPROVAL_PIC" }
    val countApproved = allMrs.count { it.status == "APPROVED" && it.prNumber == null }
    val countRejected = allMrs.count { it.status == "REJECTED" }
    val countPrDone = allMrs.count { it.prNumber != null }

    // Attendance stats
    val attendanceToday = allAttendance.filter { it.date == todayDateString }
    val totalHadir = attendanceToday.count { it.status == "HADIR" }
    val totalTerlambat = attendanceToday.count { it.status == "TERLAMBAT" }
    // Simulated employee directory has 6 people
    val totalEmployee = 6
    val totalTidakHadir = (totalEmployee - (totalHadir + totalTerlambat)).coerceAtLeast(0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- HEADER (Status Bar & Safe Area Friendly) ---
        Surface(
            color = MaterialTheme.colorScheme.background,
            modifier = Modifier.fillMaxWidth().statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WORKSHOP MR SYSTEM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user?.nama ?: "Guest User",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${user?.jabatan ?: "Senior Mekanik"} • ${user?.departemen ?: "Plant A"}",
                        fontSize = 12.sp,
                        color = ProfessionalSubtle
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Profile Initials Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B4454))
                            .border(2.dp, ProfessionalAccent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.nama?.take(2)?.uppercase(Locale.getDefault()) ?: "BS",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    // Logout Icon (elegant modern touch)
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CharcoalLight)
                            .border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- DASHBOARD ACTIVE BOARD ---
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Stats Grid
            if (user?.hasFullAccess() == true) {
                item {
                    Text(
                        text = "RINGKASAN REQ SPAREPART",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatsCard(
                                title = "Total MR",
                                value = totalTodayMr.toString(),
                                icon = Icons.Default.DateRange,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.clearFilters()
                                    onNavigateToMrList()
                                }
                            )
                            StatsCard(
                                title = "Waiting Approval",
                                value = countWaiting.toString(),
                                icon = Icons.Default.PendingActions,
                                color = ColorWaiting,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    viewModel.setFilters("", "WAITING_APPROVAL_PIC", "", "")
                                    onNavigateToMrList()
                                }
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatsCard(
                                title = "Approved MR",
                                value = countApproved.toString(),
                                icon = Icons.Default.CheckCircle,
                                color = ColorApproved,
                                modifier = Modifier.weight(1.2f),
                                onClick = {
                                    viewModel.setFilters("", "APPROVED", "", "")
                                    onNavigateToMrList()
                                }
                            )
                            StatsCard(
                                title = "Rejected",
                                value = countRejected.toString(),
                                icon = Icons.Default.Cancel,
                                color = ColorRejected,
                                modifier = Modifier.weight(0.9f),
                                onClick = {
                                    viewModel.setFilters("", "REJECTED", "", "")
                                    onNavigateToMrList()
                                }
                            )
                            StatsCard(
                                title = "PR Selesai",
                                value = countPrDone.toString(),
                                icon = Icons.Default.ShoppingCart,
                                color = ColorPrDone,
                                modifier = Modifier.weight(1.1f),
                                onClick = {
                                    viewModel.setFilters("", "PR_DONE", "", "")
                                    onNavigateToMrList()
                                }
                            )
                        }
                    }
                }

                // --- ATTENDANCE OVERVIEW PANEL ---
                item {
                    AttendanceStatusPanel(
                        attendance = todayAttendance,
                        onNavigate = onNavigateToAttendance,
                        totalHadir = totalHadir,
                        totalTerlambat = totalTerlambat,
                        totalTidakHadir = totalTidakHadir
                    )
                }
            }

            // --- MENU WORKSHOP ---
            item {
                Text(
                    text = "MENU WORKSHOP",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ProfessionalSubtle,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Create MR - Available to everyone
                    Button(
                        onClick = onNavigateToCreateMr,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("create_mr_menu_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalAccent,
                            contentColor = ProfessionalOnAccent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            text = "+",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ProfessionalOnAccent
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CREATE MATERIAL REQUEST",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 0.5.sp,
                            color = ProfessionalOnAccent
                        )
                    }

                    // Approved materials status list logic - only for full access
                    if (user?.hasFullAccess() == true) {
                        MenuButtonLarge(
                            title = "Monitoring MR & Tracking Sparepart",
                            subtitle = "Lihat status request, filter, download PDF & Excel",
                            icon = Icons.Default.ListAlt,
                            accentColor = ColorPrDone,
                            onClick = onNavigateToMrList,
                            tag = "view_mr_list_btn"
                        )

                        // Attendance logging
                        MenuButtonLarge(
                            title = "Absensi Lapangan (Clock In / Out)",
                            subtitle = "Check-in selfie dengan koordinat lokasi otomatis",
                            icon = Icons.Default.LocationOn,
                            accentColor = ColorApproved,
                            onClick = onNavigateToAttendance,
                            tag = "attendance_menu_btn"
                        )
                    }

                    // Admin full stats monitoring - ADMIN and ADMIN_PLANT
                    if (user?.role == "ADMIN" || user?.role == "ADMIN_PLANT") {
                        MenuButtonLarge(
                            title = "Workshop Dashboard & User Approval",
                            subtitle = "Statistik breakdown, unit kritis & management admin",
                            icon = Icons.Default.Analytics,
                            accentColor = IndustrialYellow,
                            onClick = onNavigateToAdmin,
                            tag = "admin_dashboard_btn"
                        )
                    }

                    // Guideline card for non-full-access users
                    if (user?.hasFullAccess() == false) {
                        Surface(
                            color = CharcoalLight,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info Access",
                                        tint = ProfessionalAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Akses Standar Aktif",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 13.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Akun Anda terdaftar dengan hak akses standard (${user?.role}). Anda dipersilakan untuk mengajukan permintaan sparepart baru (Material Request) melalui tombol di atas.",
                                    color = ProfessionalSubtle,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            // --- REALTIME NOTIFICATIONS --- only for full access
            if (user?.hasFullAccess() == true) {
                item {
                    Text(
                        text = "NOTIFIKASI REALTIME WORKSHOP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalSubtle,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(notifications.take(4)) { item ->
                    NotificationRow(notification = item)
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val (cardBg, borderStrokeColor, textColor) = when (title) {
        "Waiting Approval" -> Triple(Color(0xFF2D2618), ColorWaiting.copy(alpha = 0.2f), ColorWaiting)
        "Approved MR" -> Triple(Color(0xFF1C291E), ColorApproved.copy(alpha = 0.2f), ColorApproved)
        "Rejected" -> Triple(Color(0xFF2D1C1C), ColorRejected.copy(alpha = 0.2f), ColorRejected)
        "PR Selesai" -> Triple(Color(0xFF1B242C), ColorPrDone.copy(alpha = 0.2f), ColorPrDone)
        else -> Triple(CharcoalLight, Color.White.copy(alpha = 0.05f), Color.White)
    }

    Surface(
        onClick = onClick,
        modifier = modifier,
        color = cardBg,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderStrokeColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title, 
                    fontSize = 10.sp, 
                    color = if (textColor == Color.White) ProfessionalSubtle else textColor.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = textColor, 
                    modifier = Modifier.size(14.dp)
                )
            }
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun AttendanceStatusPanel(
    attendance: AttendanceEntity?,
    onNavigate: () -> Unit,
    totalHadir: Int,
    totalTerlambat: Int,
    totalTidakHadir: Int
) {
    val indicatorColor = if (attendance != null) Color(0xFF81C784) else ColorWaiting
    
    Surface(
        color = CharcoalLight,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Soft green/orange vertical bar on left edge matching HTML border-l-4
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(indicatorColor)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "ATTENDANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ProfessionalSubtle,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (attendance != null) {
                                "${attendance.checkInTime} - In Office"
                            } else {
                                "Anda belum Check-In hari ini."
                            },
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    
                    // Live pulsing indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(indicatorColor)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ringkasan Absensi Hari Ini:", 
                        fontSize = 11.sp, 
                        color = ProfessionalSubtle
                    )
                    TextButton(
                        onClick = onNavigate,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Text("KELOLA", color = ProfessionalAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttendanceIndicator(title = "Hadir", count = totalHadir, color = Color(0xFF81C784))
                    AttendanceIndicator(title = "Terlambat", count = totalTerlambat, color = ColorWaiting)
                    AttendanceIndicator(title = "Absen", count = totalTidakHadir, color = ColorRejected)
                }
            }
        }
    }
}

@Composable
fun AttendanceIndicator(title: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = title, fontSize = 11.sp, color = ProfessionalSubtle)
    }
}

@Composable
fun MenuButtonLarge(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(tag),
        color = CharcoalLight,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)
                Text(text = subtitle, fontSize = 11.sp, color = ProfessionalSubtle)
            }

            Icon(
                imageVector = Icons.Default.ArrowForwardIos, 
                contentDescription = null, 
                tint = ProfessionalSubtle.copy(alpha = 0.5f), 
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
fun NotificationRow(notification: NotificationItem) {
    val indicatorColor = when (notification.type) {
        "waiting" -> ColorWaiting
        "approved" -> ColorApproved
        "rejected" -> ColorRejected
        "pr" -> ColorPrDone
        else -> Color.Gray
    }

    Surface(
        color = CharcoalLight,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f))
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = notification.title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color.White)
                Text(text = notification.message, fontSize = 11.sp, color = ProfessionalSubtle)
            }
            Text(text = notification.timestamp, fontSize = 10.sp, color = ProfessionalSubtle)
        }
    }
}
