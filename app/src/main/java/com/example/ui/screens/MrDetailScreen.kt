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
import androidx.compose.ui.platform.LocalContext
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
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
import com.example.data.model.MaterialRequestEntity
import com.example.data.model.PurchaseRequestEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkshopViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MrListScreen(
    viewModel: WorkshopViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val mrs by viewModel.filteredMaterialRequests.collectAsState()
    val user by viewModel.currentUser.collectAsState()
    val query by viewModel.searchQuery.collectAsState()

    val currentFilterUnit by viewModel.filterUnit.collectAsState()
    val currentFilterStatus by viewModel.filterStatus.collectAsState()
    val currentFilterMechanic by viewModel.filterMechanic.collectAsState()

    // Filters UI selection helpers
    var filterUnit by remember(currentFilterUnit) { mutableStateOf(currentFilterUnit) }
    var filterStatus by remember(currentFilterStatus) { mutableStateOf(currentFilterStatus) }
    var filterMech by remember(currentFilterMechanic) { mutableStateOf(currentFilterMechanic) }
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MONITORING MR & PR SPAREPART", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter", tint = ProfessionalAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
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
            
            // Search Bar Component
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchMr(it) },
                label = { Text("Cari Berdasarkan No. MR, Unit, atau Part") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("mr_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            // Dynamic filter descriptions
            if (filterUnit.isNotEmpty() || filterStatus.isNotEmpty() || filterMech.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Filter:", fontSize = 11.sp, color = Color.Gray)
                    if (filterUnit.isNotEmpty()) {
                        FilterPill(label = filterUnit) {
                            filterUnit = ""
                            viewModel.setFilters(filterUnit, filterStatus, filterMech, "")
                        }
                    }
                    if (filterStatus.isNotEmpty()) {
                        FilterPill(label = filterStatus) {
                            filterStatus = ""
                            viewModel.setFilters(filterUnit, filterStatus, filterMech, "")
                        }
                    }
                    if (filterMech.isNotEmpty()) {
                        FilterPill(label = filterMech) {
                            filterMech = ""
                            viewModel.setFilters(filterUnit, filterStatus, filterMech, "")
                        }
                    }
                }
            }

            // Material Request List View
            if (mrs.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("mr_items_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(mrs) { mr ->
                        MrItemCard(mr = mr) {
                            onNavigateToDetail(mr.mrNumber)
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Empty",
                            tint = Color.Gray,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak Ada Material Request Ditemukan",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Coba sesuaikan pencarian atau filter Anda.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Filter selection bottom-sheet mock dialog (easy & responsive)
    if (showFilterSheet) {
        val uniqueUnits = listOf("Excavator PC200", "Dump Truck HD785", "Dozer D85ESS", "GD511A Grader")
        val uniqueStatuses = listOf("WAITING_APPROVAL_PIC", "APPROVED", "REJECTED", "REVISI", "DRAFT", "PR_DONE")

        AlertDialog(
            onDismissRequest = { showFilterSheet = false },
            title = { Text("Filter Laporan MR", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Filter Unit
                    Column {
                        Text("Berdasarkan Unit / Alat:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uniqueUnits.forEach { unit ->
                                FilterOptionChip(
                                    label = unit,
                                    isSelected = filterUnit == unit,
                                    onClick = { filterUnit = if (filterUnit == unit) "" else unit }
                                )
                            }
                        }
                    }

                    // Filter Status
                    Column {
                        Text("Berdasarkan Status Flow:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            uniqueStatuses.forEach { status ->
                                FilterOptionChip(
                                    label = status,
                                    isSelected = filterStatus == status,
                                    onClick = { filterStatus = if (filterStatus == status) "" else status }
                                )
                            }
                        }
                    }

                    // Filter Mekanik name input
                    Column {
                        Text("Cari Nama Mekanik:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                        OutlinedTextField(
                            value = filterMech,
                            onValueChange = { filterMech = it },
                            placeholder = { Text("Masukkan rincian nama Mekanik") },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextButton(onClick = {
                        filterUnit = ""
                        filterStatus = ""
                        filterMech = ""
                        viewModel.clearFilters()
                        showFilterSheet = false
                    }) {
                        Text("RESET FILTERS", color = Color.Red, fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            viewModel.setFilters(filterUnit, filterStatus, filterMech, "")
                            showFilterSheet = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ProfessionalAccent,
                            contentColor = ProfessionalOnAccent
                        )
                    ) {
                        Text("TERAPKAN", fontSize = 12.sp)
                    }
                }
            }
        )
    }
}

@Composable
fun FilterPill(label: String, onRemove: () -> Unit) {
    Surface(
        color = CharcoalLight,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, ProfessionalAccent.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}

@Composable
fun FilterOptionChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() }.padding(vertical = 4.dp),
        color = if (isSelected) ProfessionalAccent else CharcoalLight,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun MrItemCard(mr: MaterialRequestEntity, onClick: () -> Unit) {
    val statusColor = when (mr.status) {
        "DRAFT" -> ColorDraft
        "WAITING_APPROVAL_PIC" -> ColorWaiting
        "APPROVED" -> if (mr.prNumber != null) ColorPrDone else ColorApproved
        "REJECTED" -> ColorRejected
        "REVISI" -> ColorWaiting
        else -> Color.Gray
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("mr_item_card_${mr.mrNumber}"),
        color = CharcoalLight,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = mr.mrNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    Text(text = mr.date, fontSize = 10.sp, color = Color.Gray)
                }

                // Priority & Status Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Priority Indicator
                    if (mr.priority == "Urgent") {
                        Box(
                            modifier = Modifier
                                .background(ColorRejected.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, ColorRejected, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("URGENT", color = ColorRejected, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Status pill
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.5.dp, statusColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (mr.status == "APPROVED" && mr.prNumber != null) "PR ARIVED (COMPLETE)" else mr.status,
                            color = statusColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Divider(color = Color.Gray.copy(alpha = 0.3f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Unit / Equipment:", fontSize = 10.sp, color = Color.LightGray)
                    Text(mr.unitEquipment, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Request Suku Cadang:", fontSize = 10.sp, color = Color.LightGray)
                    Text("${mr.partName} x${mr.quantity}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ProfessionalAccent)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Oleh: ${mr.mechanicName}", fontSize = 11.sp, color = Color.Gray)
                
                if (mr.prNumber != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = ColorPrDone, modifier = Modifier.size(12.dp))
                        Text(text = mr.prNumber ?: "", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorPrDone)
                    }
                }
            }
        }
    }
}


// --- DETAIL SCREEN & FLOW WORKFLOWS ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MrDetailScreen(
    viewModel: WorkshopViewModel,
    mrNumber: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val allMrs by viewModel.allMaterialRequests.collectAsState()
    val prs by viewModel.allPurchaseRequests.collectAsState()

    val mr = allMrs.find { it.mrNumber == mrNumber }
    val linkedPr = prs.find { it.mrNumber == mrNumber }

    var rejectReasonText by remember { mutableStateOf("") }
    var inputPrNotesText by remember { mutableStateOf("") }

    var showRejectDialog by remember { mutableStateOf(false) }
    var showRevisionDialog by remember { mutableStateOf(false) }
    var showCreatePrDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportFormatSelected by remember { mutableStateOf("") } // "PDF" or "EXCEL"

    if (mr == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Request nomor $mrNumber tidak ditemukan.")
        }
        return
    }

    val statusColor = when (mr.status) {
        "DRAFT" -> ColorDraft
        "WAITING_APPROVAL_PIC" -> ColorWaiting
        "APPROVED" -> if (mr.prNumber != null) ColorPrDone else ColorApproved
        "REJECTED" -> ColorRejected
        "REVISI" -> ColorWaiting
        else -> Color.Gray
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DETAIL MATERIAL REQUEST", fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Export Document", tint = ProfessionalAccent)
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
            
            // STATUS BANNER HEADER
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(2.dp, statusColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (mr.prNumber != null) "SUDAH MENJADI PR" else "STATUS REQUEST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (mr.prNumber != null) "PR Process / Complete" else mr.status,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = when (mr.status) {
                            "APPROVED" -> Icons.Default.CheckCircle
                            "REJECTED" -> Icons.Default.Cancel
                            "REVISI" -> Icons.Default.EditCalendar
                            else -> Icons.Default.Pending
                        },
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            // REJECT / REVISION ADVISORY WARNING
            if (mr.status == "REJECTED" || mr.status == "REVISI") {
                Surface(
                    color = ColorRejected.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ColorRejected)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = ColorRejected, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pesan Masukan PIC/Supervisor:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorRejected)
                        }
                        Text(text = mr.alasanReject, fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            val isComprehensiveMr = mr.noUnit.isNotEmpty()

            if (!isComprehensiveMr) {
                // PANEL 1: MECHANICAL/METADATA DETAILS (Standard fallback)
                CardDetailSection(title = "INFORMASI REQUEST DATA") {
                    DetailRowItem(label = "Nomor MR", value = mr.mrNumber, isHighlight = true)
                    DetailRowItem(label = "Tanggal Dibuat", value = mr.date)
                    DetailRowItem(label = "Kategori Prioritas", value = mr.priority, customColor = if (mr.priority == "Urgent") ColorRejected else Color.White)
                    DetailRowItem(label = "Mekanik Pelapor", value = mr.mechanicName)
                    DetailRowItem(label = "NIK Mekanik", value = mr.mechanicNik)
                }

                // PANEL 2: ASSET & TECHNICAL BREAKDOWN INFO (Standard fallback)
                CardDetailSection(title = "DETAIL EQUIPMENT & MASALAH") {
                    DetailRowItem(label = "Unit Code / Equipment", value = mr.unitEquipment)
                    DetailRowItem(label = "HM / KM Unit", value = mr.hmKmUnit)
                    DetailRowItem(label = "Lokasi Pengerjaan / Unit", value = mr.lokasiUnit)
                    DetailRowItem(label = "Deskripsi Breakdown", value = mr.breakdownDescription, isMultiLine = true)
                }

                // PANEL 3: PART REQ & QUANTITY (Standard fallback)
                CardDetailSection(title = "SUKU CADANG YANG DIMINTA") {
                    DetailRowItem(label = "Part Number", value = mr.partNumber)
                    DetailRowItem(label = "Nama Sparepart", value = mr.partName, isHighlight = true)
                    DetailRowItem(label = "Kebutuhan Quantity", value = "${mr.quantity} Pcs")
                    DetailRowItem(label = "Catatan Tambahan", value = mr.catatan.ifEmpty { "Tidak ada catatan tambahan." }, isMultiLine = true)
                }
            } else {
                // PANEL 1: COMPREHENSIVE PDF UNIT DATA (Section A)
                CardDetailSection(title = "A. KETERANGAN UNIT") {
                    DetailRowItem(label = "Nomor MR (Otomatis)", value = mr.mrNumber, isHighlight = true)
                    DetailRowItem(label = "CODE / NO. UNIT", value = mr.noUnit, isHighlight = true)
                    DetailRowItem(label = "Tanggal Mulai Breakdown", value = mr.tanggalBreakdown)
                    DetailRowItem(label = "Tanggal Inspeksi", value = mr.tanggalInspeksi)
                    DetailRowItem(label = "Area Kerja", value = mr.areaKerja)
                    DetailRowItem(label = "Jenis Unit", value = mr.jenisUnit)
                    DetailRowItem(label = "Model Unit", value = mr.unitEquipment)
                    DetailRowItem(label = "Serial Number Unit", value = mr.serialNumberUnit)
                    DetailRowItem(label = "HM / KM Unit", value = mr.hmKmUnit)
                    DetailRowItem(label = "Lokasi Pengerjaan", value = mr.lokasiUnit)
                    DetailRowItem(label = "Diagnosis Gejala Breakdown", value = mr.breakdownDescription, isMultiLine = true)
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("TIM MEKANIK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                    val meks = mr.timMekanik.split(",").filter { it.isNotEmpty() }
                    meks.forEachIndexed { iNum, name ->
                        DetailRowItem(label = "${iNum + 1}. Mekanik", value = name)
                    }
                }

                // PANEL 2: PDF COMPONENT INSPECTIONS (Section B)
                CardDetailSection(title = "B. HASIL INSPEKSI KOMPONEN") {
                    val compInsps = listOf(
                        Triple("MESIN / ENGINE", mr.kondisiMesinStatus, mr.kondisiMesinDesc),
                        Triple("TRANSMISI / TRANSMISSION", mr.kondisiTransmisiStatus, mr.kondisiTransmisiDesc),
                        Triple("SISTEM HYDRAULIC", mr.kondisiHydraulicStatus, mr.kondisiHydraulicDesc),
                        Triple("SISTEM PENDINGIN", mr.kondisiPendinginStatus, mr.kondisiPendinginDesc),
                        Triple("RODA / TRACK", mr.kondisiRodaStatus, mr.kondisiRodaDesc),
                        Triple("SISTEM BRAKE / REM", mr.kondisiBrakeStatus, mr.kondisiBrakeDesc),
                        Triple("SISTEM ELECTRICAL", mr.kondisiElectricalStatus, mr.kondisiElectricalDesc),
                        Triple("BODY", mr.kondisiBodyStatus, mr.kondisiBodyDesc)
                    )

                    compInsps.forEach { (compLabel, status, desc) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(compLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                if (status == "TIDAK BAIK" && desc.isNotEmpty()) {
                                    Text("Uraian lengkap: $desc", fontSize = 11.sp, color = ColorRejected)
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (status == "BAIK") ColorApproved.copy(alpha = 0.15f) else ColorRejected.copy(alpha = 0.15f))
                                    .border(1.dp, if (status == "BAIK") ColorApproved else ColorRejected, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = status,
                                    fontSize = 10.sp,
                                    color = if (status == "BAIK") ColorApproved else ColorRejected,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Render Custom 9 and 10 if labels exist
                    if (mr.kondisiCustom9Label.isNotEmpty()) {
                        DetailInspectionCustomView(mr.kondisiCustom9Label, mr.kondisiCustom9Status, mr.kondisiCustom9Desc)
                    }
                    if (mr.kondisiCustom10Label.isNotEmpty()) {
                        DetailInspectionCustomView(mr.kondisiCustom10Label, mr.kondisiCustom10Status, mr.kondisiCustom10Desc)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("KATEGORI KERUSAKAN", fontSize = 10.sp, color = ProfessionalSubtle)
                            Text(mr.kategoriKerusakan, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorWaiting)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TINDAKAN DIBUTUHKAN", fontSize = 10.sp, color = ProfessionalSubtle)
                            Text(mr.tindakanDibutuhkan, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                        }
                    }
                }

                // PANEL 3: PDF MATERIAL REQUEST TABLE & PERSISTENCE CATEGORY (Section C)
                CardDetailSection(title = "C. MATERIAL REQUEST & READY CHECKLIST") {
                    val readyLabel = when (mr.readyTypeChecked) {
                        "PART_READY" -> "1. PART READY (Suku Cadang Tersedia di Gudang)"
                        "ORDER_PART" -> "2. ORDER PART (Butuh Pembelian Suku Cadang)"
                        else -> "3. LAINNYA (${mr.readyLainnyaText})"
                    }
                    DetailRowItem(label = "Tipe Persediaan", value = readyLabel, isHighlight = true)

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("TABEL DAFTAR SPAREPARTS YANG DIMINTA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                    
                    val pTable = parsePartsTableInDetail(mr.partsTableSerialized)
                    if (pTable.isNotEmpty()) {
                        pTable.forEach { row ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CharcoalLight),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.03f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("No. ${row.no}: ${row.namePart}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                        Text("Qty: ${row.qty} ${row.uom}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ProfessionalAccent)
                                    }
                                    Text("Part Number: ${row.partNumber}", fontSize = 11.sp, color = Color.LightGray)
                                    if (row.keterangan.isNotEmpty()) {
                                        Text("Keterangan: ${row.keterangan}", fontSize = 11.sp, color = ProfessionalSubtle)
                                    }
                                }
                            }
                        }
                    } else {
                        DetailRowItem(label = "Nama Sparepart", value = mr.partName, isHighlight = true)
                        DetailRowItem(label = "Part Number", value = mr.partNumber)
                        DetailRowItem(label = "Kebutuhan Quantity", value = "${mr.quantity} Pcs")
                    }

                    DetailRowItem(label = "Catatan Tambahan", value = mr.catatan.ifEmpty { "Tidak ada catatan tambahan." }, isMultiLine = true)
                }

                // PANEL 4: INTERACTIVE AUTHORIZATION SIGNATURES (Section D)
                CardDetailSection(title = "D. AUTHORIZED SIGNATURE SYSTEM") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("REQUESTED BY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ProfessionalSubtle)
                            Text("MEKANIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Box(modifier = Modifier.height(55.dp), contentAlignment = Alignment.Center) {
                                Text("Budi S.", color = ProfessionalAccent, fontSize = 20.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                            Text(mr.mechanicName, fontSize = 11.sp, color = Color.White, textAlign = TextAlign.Center)
                        }

                        Column(
                            modifier = Modifier.weight(1.1f).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("CHECKED BY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ProfessionalSubtle)
                            Text("PLANNER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Box(modifier = Modifier.height(55.dp), contentAlignment = Alignment.Center) {
                                Text("Anh", color = ColorApproved, fontSize = 24.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontWeight = FontWeight.Bold)
                            }
                            Text("RIZKY ANDREA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                        }

                        Column(
                            modifier = Modifier.weight(1.1f).border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp)).padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("APPROVED BY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ProfessionalSubtle)
                            Text("PIC AREA KERJA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Box(modifier = Modifier.height(55.dp), contentAlignment = Alignment.Center) {
                                Text("Asfor.", color = ColorWaiting, fontSize = 22.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                            }
                            Text("UNTUNG SYAHRUDIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // PANEL 5: ATTACHED PHOTOS PREVIEW
            CardDetailSection(title = "LAMPIRAN FOTO DARI LAPANGAN") {
                if (mr.photos.isNotEmpty()) {
                    val photos = mr.photos.split(",")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        photos.forEach { name ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CharcoalLight)
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Image, contentDescription = null, tint = ProfessionalAccent, modifier = Modifier.size(28.dp))
                                    Text(name, fontSize = 9.sp, color = Color.White, modifier = Modifier.padding(top = 4.dp), maxLines = 1)
                                }
                            }
                        }
                    }
                } else {
                    Text("Tidak ada foto terlampir pada request ini.", fontSize = 12.sp, color = Color.LightGray)
                }
            }

            // PANEL 5: PURCHASE REQUEST DETAILS IF PROCESSED
            if (mr.prNumber != null || linkedPr != null) {
                CardDetailSection(title = "MONITORING TAHAP PEMBELIAN (PR)") {
                    DetailRowItem(label = "Nomor PR", value = mr.prNumber ?: linkedPr?.prNumber ?: "Generating...", isHighlight = true, customColor = ColorPrDone)
                    DetailRowItem(label = "Status Progress PR", value = linkedPr?.status ?: "PR_PROCESS", customColor = ColorPrDone)
                    DetailRowItem(label = "Rincian Logistik", value = linkedPr?.notes ?: "PR sedang dalam proses pembelian di pusat vendor.")

                    if (user?.role == "LOGISTIK" || user?.role == "ADMIN" || user?.role == "ADMIN_PLANT" || user?.role == "ADMIN PLANT") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.setPrStatus(mr.prNumber ?: linkedPr?.prNumber ?: "", "PR_PROCESS") },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorWaiting),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("PROSES PO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }
                            Button(
                                onClick = { viewModel.setPrStatus(mr.prNumber ?: linkedPr?.prNumber ?: "", "PR_COMPLETE") },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorApproved),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("PR SELESAI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- ROLE ACTION PANELS ---
            
            // 1. PIC / Foreman Approval Buttons
            if (user?.role == "PIC" || user?.role == "ADMIN" || user?.role == "ADMIN_PLANT" || user?.role == "ADMIN PLANT") {
                if (mr.status == "WAITING_APPROVAL_PIC") {
                    Text("LEMBAR PERSETUJUAN PIC / FOREMAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 0.5.sp)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showRejectDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorRejected),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("pic_reject_btn")
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("REJECT", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { showRevisionDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorWaiting),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.EditCalendar, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("BUTUH REVISI", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        }

                        Button(
                            onClick = {
                                viewModel.picApproveMr(mr.mrNumber) {
                                    onNavigateBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorApproved),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                                .testTag("pic_approve_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("APPROVE MR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 2. Logistics PR Generation Buttons
            if (user?.role == "LOGISTIK" || user?.role == "ADMIN" || user?.role == "ADMIN_PLANT" || user?.role == "ADMIN PLANT") {
                if (mr.status == "APPROVED" && mr.prNumber == null) {
                    Text("TINDAKAN LOGISTIK - PEMBUATAN PR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 0.5.sp)
                    
                    Button(
                        onClick = { showCreatePrDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorPrDone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("logistics_create_pr_btn")
                    ) {
                        Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BUAT PURHASE REQUEST (PR) SEKARANG", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // 3. Mechanic Revision Submissions
            if (user?.role == "MEKANIK" && mr.status == "REVISI") {
                Button(
                    onClick = {
                        viewModel.updateMaterialRequestAfterRevision(mr) {
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfessionalAccent,
                        contentColor = ProfessionalOnAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.SendAndArchive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AJUKAN REVISI KEMBALI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }


    // --- DIALOG MODALS ---

    // PIC Reject input reason Dialog
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("Tolak MR (Reject)") },
            text = {
                Column {
                    Text("Masukkan alasan penolakan secara mendetail (wajib):", fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))
                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        label = { Text("Alasan Penolakan") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReasonText.trim().isNotEmpty()) {
                            viewModel.picRejectMr(mr.mrNumber, rejectReasonText) {
                                showRejectDialog = false
                                onNavigateBack()
                            }
                        }
                    },
                    enabled = rejectReasonText.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorRejected)
                ) {
                    Text("TOLAK SEKARANG")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) { Text("Batal") }
            }
        )
    }

    // PIC Revision input reason Dialog
    if (showRevisionDialog) {
        AlertDialog(
            onDismissRequest = { showRevisionDialog = false },
            title = { Text("Minta Revisi MR") },
            text = {
                Column {
                    Text("Masukkan instruksi bagian yang wajib direvisi oleh Mekanik:", fontSize = 13.sp, modifier = Modifier.padding(bottom = 10.dp))
                    OutlinedTextField(
                        value = rejectReasonText,
                        onValueChange = { rejectReasonText = it },
                        label = { Text("Instruksi Revisi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (rejectReasonText.trim().isNotEmpty()) {
                            viewModel.picRequestRevisionMr(mr.mrNumber, rejectReasonText) {
                                showRevisionDialog = false
                                onNavigateBack()
                            }
                        }
                    },
                    enabled = rejectReasonText.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorWaiting)
                ) {
                    Text("MINTA REVISI", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevisionDialog = false }) { Text("Batal") }
            }
        )
    }

    // Logistics Generate PR Dialog
    if (showCreatePrDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePrDialog = false },
            title = { Text("Form Purchase Request (PR)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Menggenerate surat PR otomatis dari MR ${mr.mrNumber}:", fontSize = 13.sp)
                    Text("Item: ${mr.partName} x${mr.quantity}", fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                    Text("Untuk Unit: ${mr.unitEquipment}", fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = inputPrNotesText,
                        onValueChange = { inputPrNotesText = it },
                        label = { Text("Catatan Pengadaan Logistik") },
                        placeholder = { Text("Contoh: PO ready, supplier Jakarta") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitPurchaseRequest(mr.mrNumber, inputPrNotesText) {
                            showCreatePrDialog = false
                            onNavigateBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorPrDone)
                ) {
                    Text("GENERATE PR")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePrDialog = false }) { Text("Batal") }
            }
        )
    }

    // EXPORT PDF / EXCEL / SHARE OVERLAY SIMULATOR
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = ProfessionalAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Export Laporan Data", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Pilih format berkas untuk dibagikan atau disimpan ke perangkat Anda:", fontSize = 13.sp)
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ExportFormatButton(
                            title = "Preview PDF",
                            icon = Icons.Default.PictureAsPdf,
                            isSelected = exportFormatSelected == "PDF",
                            color = ColorRejected,
                            modifier = Modifier.weight(1f)
                        ) {
                            exportFormatSelected = "PDF"
                        }

                        ExportFormatButton(
                            title = "Preview Excel",
                            icon = Icons.Default.PivotTableChart,
                            isSelected = exportFormatSelected == "EXCEL",
                            color = ColorApproved,
                            modifier = Modifier.weight(1f)
                        ) {
                            exportFormatSelected = "EXCEL"
                        }
                    }

                    if (exportFormatSelected.isNotEmpty()) {
                        Divider(color = Color.Gray.copy(alpha = 0.5f))

                        if (exportFormatSelected == "PDF") {
                            // BEAUTIFULLY DRAWN PDF WORKSHOP WORKFLOW FORMAT LAYOUT
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.LightGray),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // PDF HEADER
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Column {
                                            Text("PT INDO MINING PLANT", color = Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            Text("Workshop & Site Block 9", color = Color.Gray, fontSize = 8.sp)
                                        }
                                        Icon(Icons.Default.BuildCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
                                    }

                                    Divider(color = Color.Black)

                                    Text(
                                        text = "MATERIAL REQUEST (MR)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        PdfFieldRow(label = "Nomor MR:", value = mr.mrNumber)
                                        PdfFieldRow(label = "Tanggal:", value = mr.date)
                                        PdfFieldRow(label = "Unit Model:", value = mr.unitEquipment)
                                        PdfFieldRow(label = "HM/KM Unit:", value = mr.hmKmUnit)
                                        PdfFieldRow(label = "Lokasi Unit:", value = mr.lokasiUnit)
                                        PdfFieldRow(label = "Prioritas:", value = mr.priority)
                                        PdfFieldRow(label = "Part Number:", value = mr.partNumber)
                                        PdfFieldRow(label = "Sparepart:", value = "${mr.partName} x${mr.quantity}")
                                    }

                                    Divider(color = Color.LightGray)

                                    Text("FOTO LAMPIRAN DAMAGE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        mr.photos.split(",").filter { it.isNotEmpty() }.forEach { ph ->
                                            Box(modifier = Modifier
                                                .size(34.dp)
                                                .background(Color.LightGray, RoundedCornerShape(4.dp)), contentAlignment = Alignment.Center) {
                                                Text(ph.takeLast(6), fontSize = 7.sp, color = Color.Black)
                                            }
                                        }
                                    }

                                    Divider(color = Color.LightGray)

                                    // QR Code and Digital Signatures
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        // Fake QR code using symbols box
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .border(2.dp, Color.Black)
                                                .padding(2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.QrCode, contentDescription = "QR MR", tint = Color.Black, modifier = Modifier.size(38.dp))
                                        }

                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Digital Signatures Verified:", fontSize = 7.sp, color = Color.Gray)
                                            Text("Mechanic: ${mr.mechanicName} [Signed]", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                            if (mr.status == "APPROVED") {
                                                Text("PIC Approval: Hendra W. [Approved]", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // EXCEL PREVIEW TABULAR DATA
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ColorApproved),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PivotTableChart, contentDescription = null, tint = ColorApproved)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Microsoft Excel Sheet View (.xlsx)", fontSize = 12.sp, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold)
                                    }

                                    Divider(color = ColorApproved)

                                    // Mock CSV/Grid cells
                                    Text(
                                        text = "A1: MR_NO | B1: DATE | C1: EQUIP | D1: PART | E1: QTY | F1: STATUS",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "A2: ${mr.mrNumber} | B2: ${mr.date.take(10)} | C2: ${mr.unitEquipment} | D2: ${mr.partName} | E2: ${mr.quantity} | F2: ${mr.status}",
                                        fontSize = 11.sp,
                                        color = Color.Black
                                    )
                                    if (mr.prNumber != null) {
                                        Text(
                                            text = "A3: PR_NO: ${mr.prNumber} | B3: LINKED_MR: ${mr.mrNumber}",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("Row data completely formatted for audit sync.", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val file = if (exportFormatSelected == "PDF") {
                                generatePdfForMr(context, mr)
                            } else {
                                generateExcelForMr(context, mr)
                            }
                            
                            val authority = "${context.packageName}.fileprovider"
                            val uri: android.net.Uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                            
                            val isPdf = exportFormatSelected == "PDF"
                            val mimeType = if (isPdf) "application/pdf" else "text/comma-separated-values"
                            
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = mimeType
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "Laporan MR ${mr.mrNumber}")
                                putExtra(android.content.Intent.EXTRA_TEXT, "Berikut dilampirkan berkas MR ${mr.mrNumber} yang sudah di-approve.")
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            
                            val chooserIntent = android.content.Intent.createChooser(intent, "Kirim Laporan Melalui")
                            val activity = findActivity(context)
                            if (activity != null) {
                                activity.startActivity(chooserIntent)
                            } else {
                                chooserIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(chooserIntent)
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Error export file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                            e.printStackTrace()
                        }
                        showExportDialog = false
                        exportFormatSelected = ""
                    },
                    enabled = exportFormatSelected.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ProfessionalAccent,
                        contentColor = ProfessionalOnAccent,
                        disabledContainerColor = ProfessionalAccent.copy(alpha = 0.3f),
                        disabledContentColor = ProfessionalOnAccent.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(if (exportFormatSelected == "PDF") Icons.Default.PictureAsPdf else Icons.Default.PivotTableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (exportFormatSelected == "PDF") "SHARE PDF DOCUMENT" else "SHARE EXCEL CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                    exportFormatSelected = ""
                }) { Text("Tutup") }
            }
        )
    }
}

@Composable
fun CardDetailSection(title: String, content: @Composable () -> Unit) {
    Surface(
        color = CharcoalLight,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 0.5.sp)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun DetailRowItem(
    label: String,
    value: String,
    isHighlight: Boolean = false,
    customColor: Color = Color.White,
    isMultiLine: Boolean = false
) {
    if (isMultiLine) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            Text(text = label, fontSize = 11.sp, color = Color.LightGray)
            Text(text = value, fontSize = 13.sp, color = customColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 2.dp))
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 11.sp, color = Color.LightGray)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Normal,
                color = if (isHighlight && customColor == Color.White) ProfessionalAccent else customColor
            )
        }
    }
}

@Composable
fun ExportFormatButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .border(2.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = CharcoalDark,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PdfFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        textForPdf(label, fontWeight = FontWeight.Normal)
        textForPdf(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun textForPdf(text: String, fontWeight: FontWeight = FontWeight.Normal) {
    Text(text = text, fontSize = 9.sp, color = Color.Black, fontWeight = fontWeight)
}

fun parsePartsTableInDetail(serialized: String): List<PartRequestRow> {
    if (serialized.isEmpty()) return emptyList()
    try {
        return serialized.split(";").mapNotNull {
            val tokens = it.split("|")
            if (tokens.size >= 6) {
                PartRequestRow(
                    no = tokens[0].toIntOrNull() ?: 1,
                    namePart = tokens[1],
                    partNumber = tokens[2],
                    qty = tokens[3],
                    uom = tokens[4],
                    keterangan = tokens[5]
                )
            } else null
        }
    } catch (e: Exception) {
        return emptyList()
    }
}

@Composable
fun DetailInspectionCustomView(label: String, status: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            if (status == "TIDAK BAIK" && desc.isNotEmpty()) {
                Text("Uraian lengkap: $desc", fontSize = 11.sp, color = ColorRejected)
            }
        }
        
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(if (status == "BAIK") ColorApproved.copy(alpha = 0.15f) else ColorRejected.copy(alpha = 0.15f))
                .border(1.dp, if (status == "BAIK") ColorApproved else ColorRejected, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = status,
                fontSize = 10.sp,
                color = if (status == "BAIK") ColorApproved else ColorRejected,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// PDF & Excel real file generators for Sharing
fun generatePdfForMr(context: android.content.Context, mr: MaterialRequestEntity): java.io.File {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val androidColorBlack = android.graphics.Color.BLACK
    val paintText = Paint().apply {
        color = androidColorBlack
        textSize = 8.5f
        isAntiAlias = true
    }
    val paintTextBlue = Paint().apply {
        color = android.graphics.Color.rgb(0, 51, 102)
        textSize = 9f
        isAntiAlias = true
    }
    val paintTextGreen = Paint().apply {
        color = android.graphics.Color.rgb(27, 94, 32)
        textSize = 9f
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val paintTextRed = Paint().apply {
        color = android.graphics.Color.rgb(183, 28, 28)
        textSize = 9f
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
    }
    val paintBold = Paint().apply {
        color = androidColorBlack
        textSize = 9f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        isAntiAlias = true
    }
    val paintHeaderBold = Paint().apply {
        color = androidColorBlack
        textSize = 11f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        isAntiAlias = true
    }
    val paintTitle = Paint().apply {
        color = android.graphics.Color.rgb(0, 51, 102)
        textSize = 13f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        isAntiAlias = true
    }
    val paintSectionHeader = Paint().apply {
        color = android.graphics.Color.rgb(0, 51, 102)
        textSize = 10f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        isAntiAlias = true
    }
    val paintBorder = Paint().apply {
        color = android.graphics.Color.rgb(200, 200, 200)
        style = Paint.Style.STROKE
        strokeWidth = 0.5f
        isAntiAlias = true
    }
    val paintDivider = Paint().apply {
        color = androidColorBlack
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
    }
    val paintBgBox = Paint().apply {
        color = android.graphics.Color.rgb(245, 247, 250)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    // DRAW HEADER
    canvas.drawText("PT INDO MINING PLANT", 40f, 45f, paintHeaderBold)
    canvas.drawText("Workshop & Site Block 9 - Fleet Support", 40f, 57f, paintText)
    canvas.drawText("Verified Digital Workplace System", 40f, 67f, paintTextBlue)

    canvas.drawText("FORM MATERIAL REQUEST (MR)", 335f, 45f, paintTitle)
    canvas.drawText("No. MR: ${mr.mrNumber}", 335f, 57f, paintBold)
    canvas.drawText("Tanggal: ${mr.date}", 335f, 67f, paintText)

    canvas.drawLine(40f, 75f, 555f, 75f, paintDivider)

    var y = 92f

    // SECTION A
    canvas.drawText("A. DETAILS UNIT & EQUIPMENT", 40f, y, paintSectionHeader)
    canvas.drawLine(40f, y + 3, 555f, y + 3, paintBorder)
    y += 15f

    // Column 1
    canvas.drawText("No. Unit / Equipment :", 45f, y, paintBold)
    canvas.drawText(mr.noUnit.ifEmpty { mr.unitEquipment }, 145f, y, paintText)
    canvas.drawText("Jenis Unit :", 300f, y, paintBold)
    canvas.drawText(mr.jenisUnit.ifEmpty { "-" }, 400f, y, paintText)
    y += 12f

    canvas.drawText("Model Unit :", 45f, y, paintBold)
    canvas.drawText(mr.unitEquipment, 145f, y, paintText)
    canvas.drawText("Serial Number (S/N) :", 300f, y, paintBold)
    canvas.drawText(mr.serialNumberUnit.ifEmpty { "-" }, 400f, y, paintText)
    y += 12f

    canvas.drawText("Tgl Mulai Breakdown :", 45f, y, paintBold)
    canvas.drawText(mr.tanggalBreakdown.ifEmpty { "-" }, 145f, y, paintText)
    canvas.drawText("Tanggal Inspeksi :", 300f, y, paintBold)
    canvas.drawText(mr.tanggalInspeksi.ifEmpty { "-" }, 400f, y, paintText)
    y += 12f

    canvas.drawText("Area Kerja :", 45f, y, paintBold)
    canvas.drawText(mr.areaKerja.ifEmpty { "HAULING INDUK" }, 145f, y, paintText)
    canvas.drawText("HM / KM Unit :", 300f, y, paintBold)
    canvas.drawText(mr.hmKmUnit.ifEmpty { "-" }, 400f, y, paintText)
    y += 12f

    canvas.drawText("Lokasi Unit :", 45f, y, paintBold)
    canvas.drawText(mr.lokasiUnit.ifEmpty { "-" }, 145f, y, paintText)
    canvas.drawText("Mekanik Pelapor :", 300f, y, paintBold)
    canvas.drawText("${mr.mechanicName} (NIK: ${mr.mechanicNik})", 400f, y, paintText)
    y += 12f

    canvas.drawText("Diagnosis Gejala :", 45f, y, paintBold)
    canvas.drawText(mr.breakdownDescription.ifEmpty { "Keluhan breakdown belum diuraikan." }, 145f, y, paintText)
    
    // Draw box border for Section A unit details
    canvas.drawRect(40f, 82f, 555f, y + 8f, paintBorder)
    y += 24f

    // SECTION B: INSPEKSI
    canvas.drawText("B. HASIL INSPEKSI KOMPONEN UTAMA", 40f, y, paintSectionHeader)
    canvas.drawLine(40f, y + 3, 555f, y + 3, paintBorder)
    y += 12f

    // Draw checklist table header
    canvas.drawRect(40f, y, 555f, y + 12f, paintBgBox)
    canvas.drawRect(40f, y, 555f, y + 12f, paintBorder)
    canvas.drawText("No", 45f, y + 9f, paintBold)
    canvas.drawText("Komponen Utama Alat", 65f, y + 9f, paintBold)
    canvas.drawText("Kondisi", 250f, y + 9f, paintBold)
    canvas.drawText("Temuan Kerusakan / Uraian Detail", 310f, y + 9f, paintBold)
    y += 12f

    val compRows = listOf(
        Triple("1", "MESIN / ENGINE STATUS", Pair(mr.kondisiMesinStatus, mr.kondisiMesinDesc)),
        Triple("2", "TRANSMISI / TRANSMISSION", Pair(mr.kondisiTransmisiStatus, mr.kondisiTransmisiDesc)),
        Triple("3", "SISTEM HYDRAULIC", Pair(mr.kondisiHydraulicStatus, mr.kondisiHydraulicDesc)),
        Triple("4", "SISTEM PENDINGIN / COOLING", Pair(mr.kondisiPendinginStatus, mr.kondisiPendinginDesc)),
        Triple("5", "RODA / UNDERCARRIAGE TRACK", Pair(mr.kondisiRodaStatus, mr.kondisiRodaDesc)),
        Triple("6", "SISTEM BRAKE / REM", Pair(mr.kondisiBrakeStatus, mr.kondisiBrakeDesc)),
        Triple("7", "SISTEM ELECTRICAL_UNIT", Pair(mr.kondisiElectricalStatus, mr.kondisiElectricalDesc)),
        Triple("8", "BODY & CHASSIS FRAME", Pair(mr.kondisiBodyStatus, mr.kondisiBodyDesc))
    ).toMutableList()

    if (mr.kondisiCustom9Label.isNotEmpty()) {
        compRows.add(Triple("9", mr.kondisiCustom9Label.uppercase(), Pair(mr.kondisiCustom9Status, mr.kondisiCustom9Desc)))
    }
    if (mr.kondisiCustom10Label.isNotEmpty()) {
        compRows.add(Triple("10", mr.kondisiCustom10Label.uppercase(), Pair(mr.kondisiCustom10Status, mr.kondisiCustom10Desc)))
    }

    compRows.forEach { (no, name, data) ->
        val status = data.first
        val desc = data.second
        
        canvas.drawRect(40f, y, 555f, y + 12f, paintBorder)
        canvas.drawText(no, 45f, y + 9f, paintText)
        canvas.drawText(name, 65f, y + 9f, paintText)
        
        if (status == "BAIK") {
            canvas.drawText("BAIK", 250f, y + 9f, paintTextGreen)
        } else {
            canvas.drawText("TIDAK BAIK", 250f, y + 9f, paintTextRed)
        }
        
        canvas.drawText(desc.ifEmpty { "Kondisi terperiksa OK/Normal" }, 310f, y + 9f, paintText)
        y += 12f
    }

    y += 6f
    canvas.drawText("Kategori Kerusakan :  ${mr.kategoriKerusakan.ifEmpty { "RINGAN" }}", 45f, y, paintBold)
    canvas.drawText("Tindakan Diperlukan :  ${mr.tindakanDibutuhkan.ifEmpty { "SERVICE" }}", 300f, y, paintBold)
    y += 15f

    // SECTION C: SPAREPART REQUEST TABLE
    canvas.drawText("C. MATERIAL REQUEST & READY CHECKLIST", 40f, y, paintSectionHeader)
    canvas.drawLine(40f, y + 3, 555f, y + 3, paintBorder)
    y += 12f

    val readyOption = when (mr.readyTypeChecked) {
        "PART_READY" -> "✓  PART READY (Suku Cadang Tersedia di Gudang)"
        "ORDER_PART" -> "✓  ORDER PART (Butuh Pembelian Suku Cadang)"
        else -> "✓  LAINNYA: ${mr.readyLainnyaText.ifEmpty { "BUBUT / PABRIKASI" }}"
    }
    canvas.drawText("Kategori Penanganan Suku Cadang:  $readyOption", 45f, y, paintBold)
    y += 14f

    // Draw spareparts table header
    canvas.drawRect(40f, y, 555f, y + 12f, paintBgBox)
    canvas.drawRect(40f, y, 555f, y + 12f, paintBorder)
    canvas.drawText("No", 45f, y + 9f, paintBold)
    canvas.drawText("Nama Suku Cadang (Part Name)", 65f, y + 9f, paintBold)
    canvas.drawText("Catalog Part Number", 240f, y + 9f, paintBold)
    canvas.drawText("Qty Req", 400f, y + 9f, paintBold)
    canvas.drawText("Keterangan Tambahan", 450f, y + 9f, paintBold)
    y += 12f

    val pTable = parsePartsTableInDetail(mr.partsTableSerialized)
    if (pTable.isNotEmpty()) {
        pTable.forEachIndexed { index, row ->
            canvas.drawRect(40f, y, 555f, y + 12f, paintBorder)
            canvas.drawText((index + 1).toString(), 45f, y + 9f, paintText)
            canvas.drawText(row.namePart, 65f, y + 9f, paintText)
            canvas.drawText(row.partNumber, 240f, y + 9f, paintText)
            canvas.drawText("${row.qty} ${row.uom}", 400f, y + 9f, paintText)
            canvas.drawText(row.keterangan.ifEmpty { "-" }, 450f, y + 9f, paintText)
            y += 12f
        }
    } else {
        canvas.drawRect(40f, y, 555f, y + 12f, paintBorder)
        canvas.drawText("1", 45f, y + 9f, paintText)
        canvas.drawText(mr.partName, 65f, y + 9f, paintText)
        canvas.drawText(mr.partNumber, 240f, y + 9f, paintText)
        canvas.drawText("${mr.quantity} Pcs", 400f, y + 9f, paintText)
        canvas.drawText(mr.catatan.ifEmpty { "Kebutuhan penggantian segera." }, 450f, y + 9f, paintText)
        y += 12f
    }

    y += 15f

    // SECTION D: AUTHORIZED DIGITAL SIGNATURES & VERIFICATION SYSTEM
    canvas.drawText("D. SYSTEM DIGITAL AUTHORIZATION & OFFICIAL VERIFICATION", 40f, y, paintSectionHeader)
    canvas.drawLine(40f, y + 3, 555f, y + 3, paintBorder)
    y += 10f

    val boxWidth = 165f
    val boxHeight = 70f
    
    // Column 1: Requested By
    canvas.drawRect(40f, y, 40f + boxWidth, y + boxHeight, paintBgBox)
    canvas.drawRect(40f, y, 40f + boxWidth, y + boxHeight, paintBorder)
    canvas.drawText("REQUESTED BY:", 45f, y + 12f, paintBold)
    canvas.drawText("Mekanik Pembuat", 45f, y + 22f, paintTextBlue)
    
    val italicPaintSign = Paint().apply {
        color = android.graphics.Color.rgb(20, 80, 160)
        textSize = 14f
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD_ITALIC)
        isAntiAlias = true
    }
    
    canvas.drawText("Budi.S", 65f, y + 42f, italicPaintSign)
    canvas.drawText(mr.mechanicName, 45f, y + 54f, paintBold)
    canvas.drawText("[Signed Digital]", 45f, y + 64f, paintTextGreen)

    // Column 2: Checked By
    canvas.drawRect(210f, y, 210f + boxWidth, y + boxHeight, paintBgBox)
    canvas.drawRect(210f, y, 210f + boxWidth, y + boxHeight, paintBorder)
    canvas.drawText("CHECKED BY:", 215f, y + 12f, paintBold)
    canvas.drawText("Planner Suku Cadang", 215f, y + 22f, paintTextBlue)
    canvas.drawText("Anh", 235f, y + 42f, italicPaintSign)
    canvas.drawText("RIZKY ANDREA", 215f, y + 54f, paintBold)
    
    if (mr.status == "APPROVED" || mr.prNumber != null) {
        canvas.drawText("[Verified: PLANNER]", 215f, y + 64f, paintTextGreen)
    } else {
        canvas.drawText("[WAITING VERIFICATION]", 215f, y + 64f, paintTextRed)
    }

    // Column 3: Approved By
    canvas.drawRect(380f, y, 380f + boxWidth, y + boxHeight, paintBgBox)
    canvas.drawRect(380f, y, 380f + boxWidth, y + boxHeight, paintBorder)
    canvas.drawText("APPROVED BY:", 385f, y + 12f, paintBold)
    canvas.drawText("PIC Area / Supervisor", 385f, y + 22f, paintTextBlue)
    canvas.drawText("Asfor.", 405f, y + 42f, italicPaintSign)
    canvas.drawText("UNTUNG SYAHRUDIN", 385f, y + 54f, paintBold)
    
    if (mr.status == "APPROVED" || mr.prNumber != null) {
        canvas.drawText("[Approved: PIC AREA]", 385f, y + 64f, paintTextGreen)
    } else {
        canvas.drawText("[WAITING APPROVAL]", 385f, y + 64f, paintTextRed)
    }

    y += boxHeight + 15f
    // Draw QR Badge / Hash verified at bottom margin for legitimacy
    canvas.drawRect(40f, y, 555f, y + 22f, paintBorder)
    canvas.drawText("✓ DIGITAL SECURITY TOKEN:", 45f, y + 14f, paintBold)
    val securityHash = "HSA-${mr.mrNumber.hashCode().coerceAtLeast(0).toString(16).uppercase()}"
    canvas.drawText("$securityHash | GUDANG & WORKSHOP VERIFIED STATUS: OFFICIAL APPROVED DOCUMENT.", 175f, y + 14f, paintTextBlue)

    pdfDocument.finishPage(page)

    val file = java.io.File(context.cacheDir, "MR_${mr.mrNumber}.pdf")
    val fos = java.io.FileOutputStream(file)
    pdfDocument.writeTo(fos)
    pdfDocument.close()
    fos.close()

    return file
}

fun generateExcelForMr(context: android.content.Context, mr: MaterialRequestEntity): java.io.File {
    val file = java.io.File(context.cacheDir, "MR_${mr.mrNumber}.csv")
    val fos = java.io.FileOutputStream(file)
    val sb = java.lang.StringBuilder()
    
    // UTF-8 BOM to ensure Indonesian and regional characters open correctly in Excel
    fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
    
    sb.append("PT INDO MINING PLANT;WORKSHOP & SITE BLOCK 9;;;;\n")
    sb.append("FORM MATERIAL REQUEST (MR);;;;;\n")
    sb.append("Nomor MR;${mr.mrNumber};;;;\n")
    sb.append("Tanggal;${mr.date};;;;\n")
    sb.append("\n")
    sb.append("A. KETERANGAN UNIT;;;;;\n")
    sb.append("Code Unit;${mr.noUnit};;;;\n")
    sb.append("Jenis Unit;${mr.jenisUnit};;;;\n")
    sb.append("Model Unit;${mr.unitEquipment};;;;\n")
    sb.append("Serial Number;${mr.serialNumberUnit};;;;\n")
    sb.append("Tanggal Breakdown;${mr.tanggalBreakdown};;;;\n")
    sb.append("Tanggal Inspeksi;${mr.tanggalInspeksi};;;;\n")
    sb.append("Area Kerja;${mr.areaKerja};;;;\n")
    sb.append("HM / KM Unit;${mr.hmKmUnit};;;;\n")
    sb.append("Lokasi Unit;${mr.lokasiUnit};;;;\n")
    sb.append("Deskripsi Breakdown;${mr.breakdownDescription.replace("\n", " ")};;;;\n")
    sb.append("Tim Mekanik;${mr.timMekanik.replace(",", " | ")};;;;\n")
    sb.append("\n")
    sb.append("B. HASIL INSPEKSI KOMPONEN;;;;;\n")
    sb.append("No;Nama Komponen;Status;Uraian / Keterangan;;\n")
    sb.append("1;MESIN / ENGINE;${mr.kondisiMesinStatus};${mr.kondisiMesinDesc};;\n")
    sb.append("2;TRANSMISI / TRANSMISSION;${mr.kondisiTransmisiStatus};${mr.kondisiTransmisiDesc};;\n")
    sb.append("3;SISTEM HYDRAULIC;${mr.kondisiHydraulicStatus};${mr.kondisiHydraulicDesc};;\n")
    sb.append("4;SISTEM PENDINGIN;${mr.kondisiPendinginStatus};${mr.kondisiPendinginDesc};;\n")
    sb.append("5;RODA / TRACK;${mr.kondisiRodaStatus};${mr.kondisiRodaDesc};;\n")
    sb.append("6;SISTEM BRAKE / REM;${mr.kondisiBrakeStatus};${mr.kondisiBrakeDesc};;\n")
    sb.append("7;SISTEM ELECTRICAL;${mr.kondisiElectricalStatus};${mr.kondisiElectricalDesc};;\n")
    sb.append("8;BODY;${mr.kondisiBodyStatus};${mr.kondisiBodyDesc};;\n")
    
    if (mr.kondisiCustom9Label.isNotEmpty()) {
        sb.append("9;${mr.kondisiCustom9Label};${mr.kondisiCustom9Status};${mr.kondisiCustom9Desc};;\n")
    }
    if (mr.kondisiCustom10Label.isNotEmpty()) {
        sb.append("10;${mr.kondisiCustom10Label};${mr.kondisiCustom10Status};${mr.kondisiCustom10Desc};;\n")
    }
    
    sb.append("Kategori Kerusakan;${mr.kategoriKerusakan};;;;\n")
    sb.append("Tindakan Dibutuhkan;${mr.tindakanDibutuhkan};;;;\n")
    sb.append("\n")
    sb.append("C. DAFTAR PERMINTAAN SPAREPART;;;;;\n")
    sb.append("Tipe Ready;${mr.readyTypeChecked} (${mr.readyLainnyaText});;;;\n")
    sb.append("No;Nama Part;Part Number;QTY;UOM;Keterangan\n")
    
    val parts = parsePartsTableInDetail(mr.partsTableSerialized)
    if (parts.isNotEmpty()) {
        parts.forEach { row ->
            sb.append("${row.no};${row.namePart};${row.partNumber};${row.qty};${row.uom};${row.keterangan}\n")
        }
    } else {
        sb.append("1;${mr.partName};${mr.partNumber};${mr.quantity};Pcs;${mr.catatan}\n")
    }
    
    sb.append("\n")
    sb.append("D. DIGITAL SIGNATURES AUTHORIZATION;;;;;\n")
    sb.append("Mekanik (Requested By);${mr.mechanicName};[Signed Digital];;;\n")
    sb.append("Planner (Checked By);RIZKY ANDREA;[Verified Digital];;;\n")
    sb.append("PIC Area Kerja (Approved By);UNTUNG SYAHRUDIN;[Approved Digital];;;\n")
    
    fos.write(sb.toString().toByteArray(Charsets.UTF_8))
    fos.close()
    return file
}

fun findActivity(context: android.content.Context): android.app.Activity? {
    var cur = context
    while (cur is android.content.ContextWrapper) {
        if (cur is android.app.Activity) {
            return cur
        }
        cur = cur.baseContext
    }
    return null
}

