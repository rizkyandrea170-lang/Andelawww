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
import com.example.data.model.AttendanceEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkshopViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: WorkshopViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val todayAttendance by viewModel.todayAttendance.collectAsState()
    val allAttendance by viewModel.allAttendance.collectAsState()

    // GPS Status simulation states
    var simulatedGpsLatitude by remember { mutableStateOf("-6.2146") }
    var simulatedGpsLongitude by remember { mutableStateOf("106.8451") }
    var isCheckingGps by remember { mutableStateOf(false) }

    // Live Clock timer
    var currentTimeString by remember { mutableStateOf("") }
    val currentDateString = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()) }

    // Selfie snap simulation
    var isSelfieSnapped by remember { mutableStateOf(false) }
    var snappedPhotoName by remember { mutableStateOf("") }
    var showCameraView by remember { mutableStateOf(false) }

    // Update time clock
    LaunchedEffect(Unit) {
        viewModel.loadTodayAttendance(user?.nik ?: "")
        while (true) {
            currentTimeString = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Filter user's list
    val userAttendanceHistory = allAttendance.filter { it.userNik == user?.nik }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ABSENSI LAPANGAN KARYAWAN", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. DIGITAL CLOCK WIDGET
            Surface(
                color = CharcoalLight,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDateString.uppercase(Locale.getDefault()),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ProfessionalAccent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentTimeString,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = ColorApproved, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mulai Kerja: 08:00 AM WIB", fontSize = 11.sp, color = Color.LightGray)
                    }
                }
            }

            // 2. SELFIE & GPS CAPTURE PREVIEW BOX
            Surface(
                color = CharcoalLight,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (snappedPhotoName.isNotEmpty()) ColorApproved else Color.Gray.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("KAMERA SELFIE & GPS STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 0.5.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Selfie mockup box
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF263238))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp))
                                .clickable { showCameraView = true },
                            contentAlignment = Alignment.Center
                        ) {
                            if (snappedPhotoName.isNotEmpty()) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Face, contentDescription = null, tint = ColorApproved, modifier = Modifier.size(36.dp))
                                    Text("Snapped", fontSize = 10.sp, color = ColorApproved, fontWeight = FontWeight.Bold)
                                    Text(snappedPhotoName.take(12), fontSize = 8.sp, color = Color.LightGray, maxLines = 1)
                                }
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                Icon(Icons.Default.Portrait, contentDescription = null, tint = ProfessionalSubtle, modifier = Modifier.size(32.dp))
                                    Text("Ambil Selfie", fontSize = 10.sp, color = ProfessionalSubtle, textAlign = TextAlign.Center)
                                }
                            }
                        }

                        // GPS Status tracker box
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = ProfessionalAccent, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Akurasi GPS Tinggi", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                text = "Lokasi: Lat $simulatedGpsLatitude, Lng $simulatedGpsLongitude",
                                fontSize = 11.sp,
                                color = ProfessionalSubtle
                            )
                            Text(
                                text = "Site: Indonesia Core Coal Workshop Site A",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )

                            // Refresh coordinates
                            TextButton(
                                onClick = {
                                    isCheckingGps = true
                                    // Simulated random site coordinates
                                    val lat = (-6.21 + (Random().nextDouble() - 0.5) * 0.1)
                                    val lng = (106.84 + (Random().nextDouble() - 0.5) * 0.1)
                                    simulatedGpsLatitude = String.format("%.4f", lat)
                                    simulatedGpsLongitude = String.format("%.4f", lng)
                                    isCheckingGps = false
                                },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(if (isCheckingGps) "Mencari Satelit..." else "Perbarui Lokasi GPS", fontSize = 11.sp, color = ProfessionalAccent)
                            }
                        }
                    }
                }
            }

            // 3. ACTION BUTTONS: CLOCK IN & CLOCK OUT
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (todayAttendance == null) {
                    // Check in
                    Button(
                        onClick = {
                            if (snappedPhotoName.isNotEmpty()) {
                                viewModel.checkIn(
                                    gpsLatLng = "Lat: $simulatedGpsLatitude, Lng: $simulatedGpsLongitude",
                                    photoMock = snappedPhotoName,
                                    onShowMessage = { msg ->
                                        snappedPhotoName = "" // reset selfie camera state
                                    }
                                )
                            }
                        },
                        enabled = snappedPhotoName.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("clock_in_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalAccent,
                            contentColor = ProfessionalOnAccent,
                            disabledContainerColor = ProfessionalAccent.copy(alpha = 0.3f),
                            disabledContentColor = ProfessionalOnAccent.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ABSEN MASUK (CLOCK IN)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    if (snappedPhotoName.isEmpty()) {
                        Text(
                            text = "*Ambil foto selfie di atas terlebih dahulu untuk aktifasi Check-In.",
                            fontSize = 11.sp,
                            color = ColorRejected,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else if (todayAttendance?.checkOutTime == null) {
                    // Check out
                    Button(
                        onClick = {
                            if (snappedPhotoName.isNotEmpty()) {
                                viewModel.checkOut(
                                    gpsLatLng = "Lat: $simulatedGpsLatitude, Lng: $simulatedGpsLongitude",
                                    photoMock = snappedPhotoName,
                                    onShowMessage = { msg ->
                                        snappedPhotoName = ""
                                    }
                                )
                            }
                        },
                        enabled = snappedPhotoName.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("clock_out_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalAccent,
                            contentColor = ProfessionalOnAccent,
                            disabledContainerColor = ProfessionalAccent.copy(alpha = 0.3f),
                            disabledContentColor = ProfessionalOnAccent.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ABSEN KELUAR (CLOCK OUT)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    if (snappedPhotoName.isEmpty()) {
                        Text(
                            text = "*Ambil foto selfie penutup terlebih dahulu untuk aktifasi Check-Out.",
                            fontSize = 11.sp,
                            color = ColorRejected,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Today Complete alert
                    Surface(
                        color = ColorApproved.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, ColorApproved),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TaskAlt, contentDescription = null, tint = ColorApproved)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Absensi Hari Ini Lengkap. Selamat Beristirahat!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorApproved)
                        }
                    }
                }
            }

            // 4. PREVIOUS ATTENDANCE REKAP HISTORY
            Text("REKAP RIWAYAT ABSENSI ANDA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)

            if (userAttendanceHistory.isNotEmpty()) {
                userAttendanceHistory.forEach { item ->
                    AttendanceHistoryRow(item = item)
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(CharcoalLight, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Belum ada riwayat absensi terekam.", color = Color.LightGray, fontSize = 11.sp)
                }
            }
        }
    }

    // Capture simulated camera view modal/alert
    if (showCameraView) {
        val faceMocks = listOf(
            "Mekanik_Budi_Selfie_ShiftPagi.png",
            "Mekanik_Ahmad_Selfie_Sore.jpg",
            "HendraW_Selfie_SiteWest.jpg",
            "Mekanik_Budi_CheckOut.png"
        )

        AlertDialog(
            onDismissRequest = { showCameraView = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = IndustrialYellow)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Selfie Cam Simulator", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Arahkan kamera ke wajah Anda & tekan Jepret untuk validasi biometrik:", fontSize = 12.sp)
                    
                    // Large camera view mock box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black)
                            .border(2.dp, IndustrialYellow),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Portrait, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(80.dp))
                            Text("[ DETEKSI WAJAH SISTEM: OK ]", fontSize = 10.sp, color = ColorApproved, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        snappedPhotoName = faceMocks.random()
                        showCameraView = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorApproved)
                ) {
                    Icon(Icons.Default.Camera, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("JEPRET SELFIE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraView = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun AttendanceHistoryRow(item: AttendanceEntity) {
    Surface(
        color = CharcoalLight,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.date, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                
                // Attendance Status indicator
                Box(
                    modifier = Modifier
                        .background(
                            if (item.status == "HADIR") ColorApproved.copy(alpha = 0.15f) else ColorWaiting.copy(alpha = 0.15f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(1.dp, if (item.status == "HADIR") ColorApproved else ColorWaiting, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = item.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (item.status == "HADIR") ColorApproved else ColorWaiting)
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.2f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Check In:", fontSize = 10.sp, color = Color.LightGray)
                    Text(item.checkInTime ?: "--:--", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Check Out:", fontSize = 10.sp, color = Color.LightGray)
                    Text(item.checkOutTime ?: "--:--", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.checkInGps?.take(28) ?: "Location error", fontSize = 10.sp, color = Color.LightGray)
                }

                if (item.checkInPhoto != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ColorApproved, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Selfie Verified", fontSize = 10.sp, color = ColorApproved)
                    }
                }
            }
        }
    }
}
