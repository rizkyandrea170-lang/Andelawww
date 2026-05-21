package com.example.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.WorkshopViewModel
import java.text.SimpleDateFormat
import java.util.*

data class PartRequestRow(
    val no: Int,
    val namePart: String = "",
    val partNumber: String = "",
    val qty: String = "",
    val uom: String = "",
    val keterangan: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MrFormScreen(
    viewModel: WorkshopViewModel,
    onNavigateBack: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val draft by viewModel.draftMr.collectAsState()
    val context = LocalContext.current

    var mrNumber by remember { mutableStateOf("Generating...") }
    val currentDate = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    // --- FORM STATES ---
    // A. KETERANGAN UNIT
    var noUnit by remember { mutableStateOf("") }
    var jenisUnit by remember { mutableStateOf("") }
    var modelUnit by remember { mutableStateOf("") }
    var serialNumberUnit by remember { mutableStateOf("") }
    var tanggalBreakdown by remember { mutableStateOf("") }
    var tanggalInspeksi by remember { mutableStateOf(currentDate) }
    var areaKerja by remember { mutableStateOf("HAULING INDUK") }
    var hmKmUnit by remember { mutableStateOf("") }
    var lokasiUnit by remember { mutableStateOf("") }
    var breakdownDescription by remember { mutableStateOf("") }

    // B. HASIL INSPEKSI
    var timMekanik1 by remember { mutableStateOf("") }
    var timMekanik2 by remember { mutableStateOf("") }
    var timMekanik3 by remember { mutableStateOf("") }
    var timMekanik4 by remember { mutableStateOf("") }

    // Component conditions with Status (BAIK/TIDAK BAIK) and description (Uraian)
    var mesinStatus by remember { mutableStateOf("BAIK") }
    var mesinDesc by remember { mutableStateOf("") }
    var transmisiStatus by remember { mutableStateOf("BAIK") }
    var transmisiDesc by remember { mutableStateOf("") }
    var hydraulicStatus by remember { mutableStateOf("BAIK") }
    var hydraulicDesc by remember { mutableStateOf("") }
    var pendinginStatus by remember { mutableStateOf("BAIK") }
    var pendinginDesc by remember { mutableStateOf("") }
    var rodaStatus by remember { mutableStateOf("BAIK") }
    var rodaDesc by remember { mutableStateOf("") }
    var brakeStatus by remember { mutableStateOf("BAIK") }
    var brakeDesc by remember { mutableStateOf("") }
    var electricalStatus by remember { mutableStateOf("BAIK") }
    var electricalDesc by remember { mutableStateOf("") }
    var bodyStatus by remember { mutableStateOf("BAIK") }
    var bodyDesc by remember { mutableStateOf("") }

    // Custom Components 9 and 10 in the list
    var custom9Label by remember { mutableStateOf("") }
    var custom9Status by remember { mutableStateOf("BAIK") }
    var custom9Desc by remember { mutableStateOf("") }
    var custom10Label by remember { mutableStateOf("") }
    var custom10Status by remember { mutableStateOf("BAIK") }
    var custom10Desc by remember { mutableStateOf("") }

    var kategoriKerusakan by remember { mutableStateOf("") } // "BERAT", "SEDANG", "RINGAN"
    var tindakanDibutuhkan by remember { mutableStateOf("") } // "SERVICE", "REPLACE"

    // C. MATERIAL REQUEST CATEGORY & TABLE
    var readyTypeChecked by remember { mutableStateOf("") } // "PART_READY", "ORDER_PART", "LAINNYA"
    var readyLainnyaText by remember { mutableStateOf("") } // e.g. BUBUT, PABRIKASI, KALIBRASI, DSB

    // Dynamic requested parts list (up to 12 rows)
    var partsList by remember {
        mutableStateOf(
            listOf(
                PartRequestRow(1),
                PartRequestRow(2)
            )
        )
    }

    var photosList by remember { mutableStateOf(listOf<String>()) }
    var catatan by remember { mutableStateOf("") }
    var showScanQrDialog by remember { mutableStateOf(false) }

    // Navigation and validation help states
    var activeTab by remember { mutableStateOf(0) } // 0: Unit, 1: Inspeksi, 2: Sparepart
    var validationTriggered by remember { mutableStateOf(false) }

    // Date Pickers setup
    val calendar = Calendar.getInstance()
    val bkDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            tanggalBreakdown = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val inspDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            tanggalInspeksi = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Helper functions to serialize and deserialize custom 12-parts table
    fun serializePartsTable(list: List<PartRequestRow>): String {
        return list.filter { it.namePart.isNotEmpty() || it.partNumber.isNotEmpty() }
            .joinToString(";") { row ->
                "${row.no}|${row.namePart}|${row.partNumber}|${row.qty}|${row.uom}|${row.keterangan}"
            }
    }

    fun deserializePartsTable(serialized: String): List<PartRequestRow> {
        if (serialized.isEmpty()) return listOf(PartRequestRow(1), PartRequestRow(2))
        try {
            val list = serialized.split(";").mapNotNull {
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
            return if (list.isNotEmpty()) list else listOf(PartRequestRow(1), PartRequestRow(2))
        } catch (e: Exception) {
            return listOf(PartRequestRow(1), PartRequestRow(2))
        }
    }

    // Load next number and draft configuration
    LaunchedEffect(Unit) {
        mrNumber = viewModel.getNextMrNumber()
        
        draft?.let { d ->
            noUnit = d.noUnit
            jenisUnit = d.jenisUnit
            modelUnit = d.modelUnit
            serialNumberUnit = d.serialNumberUnit
            tanggalBreakdown = d.tanggalBreakdown
            tanggalInspeksi = d.tanggalInspeksi
            areaKerja = if (d.areaKerja.isNotEmpty()) d.areaKerja else "HAULING INDUK"
            hmKmUnit = d.hmKmUnit
            lokasiUnit = d.lokasiUnit
            breakdownDescription = d.breakdownDescription

            // B
            val splitMek = d.timMekanik.split(",")
            timMekanik1 = splitMek.getOrElse(0) { "" }
            timMekanik2 = splitMek.getOrElse(1) { "" }
            timMekanik3 = splitMek.getOrElse(2) { "" }
            timMekanik4 = splitMek.getOrElse(3) { "" }

            mesinStatus = d.kondisiMesinStatus
            mesinDesc = d.kondisiMesinDesc
            transmisiStatus = d.kondisiTransmisiStatus
            transmisiDesc = d.kondisiTransmisiDesc
            hydraulicStatus = d.kondisiHydraulicStatus
            hydraulicDesc = d.kondisiHydraulicDesc
            pendinginStatus = d.kondisiPendinginStatus
            pendinginDesc = d.kondisiPendinginDesc
            rodaStatus = d.kondisiRodaStatus
            rodaDesc = d.kondisiRodaDesc
            brakeStatus = d.kondisiBrakeStatus
            brakeDesc = d.kondisiBrakeDesc
            electricalStatus = d.kondisiElectricalStatus
            electricalDesc = d.kondisiElectricalDesc
            bodyStatus = d.kondisiBodyStatus
            bodyDesc = d.kondisiBodyDesc

            custom9Label = d.kondisiCustom9Label
            custom9Status = d.kondisiCustom9Status
            custom9Desc = d.kondisiCustom9Desc
            custom10Label = d.kondisiCustom10Label
            custom10Status = d.kondisiCustom10Status
            custom10Desc = d.kondisiCustom10Desc

            kategoriKerusakan = d.kategoriKerusakan
            tindakanDibutuhkan = d.tindakanDibutuhkan

            // C
            readyTypeChecked = d.readyTypeChecked
            readyLainnyaText = d.readyLainnyaText
            partsList = deserializePartsTable(d.partsTableSerialized)
            catatan = d.catatan
            if (d.photos.isNotEmpty()) {
                photosList = d.photos.split(",")
            }
        }
    }

    // --- VALIDATION LOGIC ---
    // Section A validations
    val isUnitValid = noUnit.isNotEmpty() &&
            jenisUnit.isNotEmpty() &&
            modelUnit.isNotEmpty() &&
            serialNumberUnit.isNotEmpty() &&
            tanggalBreakdown.isNotEmpty() &&
            tanggalInspeksi.isNotEmpty() &&
            hmKmUnit.isNotEmpty()

    // Section B validations
    val isInspeksiValid = timMekanik1.isNotEmpty() &&
            kategoriKerusakan.isNotEmpty() &&
            tindakanDibutuhkan.isNotEmpty() &&
            (mesinStatus == "BAIK" || mesinDesc.isNotEmpty()) &&
            (transmisiStatus == "BAIK" || transmisiDesc.isNotEmpty()) &&
            (hydraulicStatus == "BAIK" || hydraulicDesc.isNotEmpty()) &&
            (pendinginStatus == "BAIK" || pendinginDesc.isNotEmpty()) &&
            (rodaStatus == "BAIK" || rodaDesc.isNotEmpty()) &&
            (brakeStatus == "BAIK" || brakeDesc.isNotEmpty()) &&
            (electricalStatus == "BAIK" || electricalDesc.isNotEmpty()) &&
            (bodyStatus == "BAIK" || bodyDesc.isNotEmpty()) &&
            (custom9Label.isEmpty() || custom9Status == "BAIK" || custom9Desc.isNotEmpty()) &&
            (custom10Label.isEmpty() || custom10Status == "BAIK" || custom10Desc.isNotEmpty())

    // Section C validations
    val isCategoryReadyValid = readyTypeChecked.isNotEmpty() && 
            (readyTypeChecked != "LAINNYA" || readyLainnyaText.isNotEmpty())

    // Check custom parts table rows are completely filled
    val populatedParts = partsList.filter { 
        it.namePart.isNotEmpty() || it.partNumber.isNotEmpty() || it.qty.isNotEmpty() || it.uom.isNotEmpty() 
    }
    val atLeastOnePartFilled = populatedParts.isNotEmpty()
    val allPopulatedPartsComplete = populatedParts.all { 
        it.namePart.isNotEmpty() && it.partNumber.isNotEmpty() && it.qty.isNotEmpty() && it.uom.isNotEmpty() 
    }
    val isSparepartsValid = isCategoryReadyValid && atLeastOnePartFilled && allPopulatedPartsComplete

    val isFormCompleteAndValid = isUnitValid && isInspeksiValid && isSparepartsValid

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MATERIAL REQUEST & FORM INSPEKSI", fontSize = 14.sp, fontWeight = FontWeight.Bold) },
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
            // STEP PROGRESS TABS
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CharcoalLight,
                contentColor = ProfessionalAccent,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = ProfessionalAccent
                    )
                }
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("1. UNIT & MEKANIK", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (validationTriggered && !isUnitValid) {
                                BaseErrorIndicator()
                            }
                        }
                    }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("2. HASIL INSPEKSI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (validationTriggered && !isInspeksiValid) {
                                BaseErrorIndicator()
                            }
                        }
                    }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("3. PERMINTAAN PART", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (validationTriggered && !isSparepartsValid) {
                                BaseErrorIndicator()
                            }
                        }
                    }
                )
            }

            // FORM BODY CONTAINERS
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Warning if submit was triggered but invalid
                if (validationTriggered && !isFormCompleteAndValid) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ColorRejected.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, ColorRejected),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ColorRejected)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Terdapat kolom wajib (*) atau baris part yang belum lengkap!", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Pastikan data unit, hasil inspeksi, dan minimal 1 baris part beserta checklist (Ready tipe) terisi lengkap.", fontSize = 11.sp, color = Color.LightGray)
                            }
                        }
                    }
                }

                // Load and show draft message
                if (draft != null) {
                    Surface(
                        color = CharcoalLight,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, ProfessionalAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Drafts, contentDescription = null, tint = ProfessionalAccent, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Melanjutkan dari draft tersimpan", fontSize = 11.sp, color = Color.White)
                            }
                            TextButton(onClick = {
                                viewModel.clearDraft()
                                noUnit = ""; jenisUnit = ""; modelUnit = ""; serialNumberUnit = ""; tanggalBreakdown = ""; tanggalInspeksi = currentDate; areaKerja = "HAULING INDUK"; hmKmUnit = ""; lokasiUnit = ""; breakdownDescription = ""
                                timMekanik1 = ""; timMekanik2 = ""; timMekanik3 = ""; timMekanik4 = ""
                                mesinStatus = "BAIK"; mesinDesc = ""; transmisiStatus = "BAIK"; transmisiDesc = ""; hydraulicStatus = "BAIK"; hydraulicDesc = ""; pendinginStatus = "BAIK"; pendinginDesc = ""; rodaStatus = "BAIK"; rodaDesc = ""; brakeStatus = "BAIK"; brakeDesc = ""; electricalStatus = "BAIK"; electricalDesc = ""; bodyStatus = "BAIK"; bodyDesc = ""
                                custom9Label = ""; custom9Status = "BAIK"; custom9Desc = ""; custom10Label = ""; custom10Status = "BAIK"; custom10Desc = ""
                                kategoriKerusakan = ""; tindakanDibutuhkan = ""
                                readyTypeChecked = ""; readyLainnyaText = ""
                                partsList = listOf(PartRequestRow(1), PartRequestRow(2))
                                photosList = emptyList(); catatan = ""
                            }, contentPadding = PaddingValues(0.dp)) {
                                Text("Reset Form", color = ColorRejected, fontSize = 10.sp)
                            }
                        }
                    }
                }

                when (activeTab) {
                    0 -> {
                        // TAB 1: KETERANGAN UNIT & TIM MEKANIK
                        Text("A. KETERANGAN UNIT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)

                        // NO MR & NO UNIT Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = mrNumber,
                                onValueChange = {},
                                label = { Text("No. MR (Otomatis)") },
                                readOnly = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Gray, unfocusedBorderColor = Color.Gray)
                            )

                            // NO UNIT BOX (Top Right in original form)
                            OutlinedTextField(
                                value = noUnit,
                                onValueChange = { noUnit = it },
                                label = { Text("CODE / NO. UNIT *") },
                                placeholder = { Text("Contoh: EXCA-201") },
                                modifier = Modifier.weight(1f).testTag("mr_no_unit_input"),
                                isError = validationTriggered && noUnit.isEmpty(),
                                leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = ProfessionalAccent) },
                                singleLine = true
                            )
                        }

                        // Date Pickers Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Breakdown Date Field
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = tanggalBreakdown,
                                    onValueChange = { tanggalBreakdown = it },
                                    label = { Text("Tgl Mulai Breakdown *") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    isError = validationTriggered && tanggalBreakdown.isEmpty(),
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { bkDatePickerDialog.show() },
                                    trailingIcon = {
                                        IconButton(onClick = { bkDatePickerDialog.show() }) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Pilih Tanggal")
                                        }
                                    }
                                )
                            }

                            // Inspection Date Field
                            Column(modifier = Modifier.weight(1f)) {
                                OutlinedTextField(
                                    value = tanggalInspeksi,
                                    onValueChange = { tanggalInspeksi = it },
                                    label = { Text("Tanggal Inspeksi *") },
                                    placeholder = { Text("YYYY-MM-DD") },
                                    isError = validationTriggered && tanggalInspeksi.isEmpty(),
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth().clickable { inspDatePickerDialog.show() },
                                    trailingIcon = {
                                        IconButton(onClick = { inspDatePickerDialog.show() }) {
                                            Icon(Icons.Default.CalendarToday, contentDescription = "Pilih Tanggal")
                                        }
                                    }
                                )
                            }
                        }

                        // Jenis Unit & Model Unit Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = jenisUnit,
                                onValueChange = { jenisUnit = it },
                                label = { Text("Jenis Unit *") },
                                placeholder = { Text("Contoh: Excavator") },
                                modifier = Modifier.weight(1.1f),
                                isError = validationTriggered && jenisUnit.isEmpty(),
                                singleLine = true
                            )

                            // Quick QR scanning simulator button for auto filling
                            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                                IconButton(
                                    onClick = { showScanQrDialog = true },
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CharcoalLight)
                                        .border(1.dp, ProfessionalAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = ProfessionalAccent)
                                }
                            }

                            OutlinedTextField(
                                value = modelUnit,
                                onValueChange = { modelUnit = it },
                                label = { Text("Model Unit *") },
                                placeholder = { Text("Contoh: PC200-8") },
                                modifier = Modifier.weight(1f).testTag("mr_unit_input"),
                                isError = validationTriggered && modelUnit.isEmpty(),
                                singleLine = true
                            )
                        }

                        // Serial Number & Area Kerja
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = serialNumberUnit,
                                onValueChange = { serialNumberUnit = it },
                                label = { Text("Serial Number (S/N) *") },
                                placeholder = { Text("Contoh: SN988029") },
                                modifier = Modifier.weight(1f),
                                isError = validationTriggered && serialNumberUnit.isEmpty(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = areaKerja,
                                onValueChange = { areaKerja = it },
                                label = { Text("Area Kerja") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // HM/KM unit & Lokasi Unit
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = hmKmUnit,
                                onValueChange = { hmKmUnit = it },
                                label = { Text("HM / KM Unit *") },
                                placeholder = { Text("Contoh: 15400 HM") },
                                modifier = Modifier.weight(1.1f).testTag("mr_hm_input"),
                                isError = validationTriggered && hmKmUnit.isEmpty(),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = lokasiUnit,
                                onValueChange = { lokasiUnit = it },
                                label = { Text("Lokasi Unit") },
                                placeholder = { Text("Contoh: Pit Utara Blok A") },
                                modifier = Modifier.weight(1f).testTag("mr_lokasi_input"),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = breakdownDescription,
                            onValueChange = { breakdownDescription = it },
                            label = { Text("Diagnosis Gejala Breakdown") },
                            placeholder = { Text("Masukkan keluhan awal unit saat breakdown terjadi...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .testTag("mr_desc_input")
                        )

                        // TIM MEKANIK INPUT SPACES
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("TIM MEKANIK (Min. 1 Mekanik Terisi *)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = timMekanik1,
                                onValueChange = { timMekanik1 = it },
                                label = { Text("1. Mekanik Utama *") },
                                placeholder = { Text("Ketik nama mekanik utama...") },
                                modifier = Modifier.fillMaxWidth(),
                                isError = validationTriggered && timMekanik1.isEmpty(),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ProfessionalAccent) },
                                singleLine = true
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = timMekanik2,
                                    onValueChange = { timMekanik2 = it },
                                    label = { Text("2. Anggota Tim") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = timMekanik3,
                                    onValueChange = { timMekanik3 = it },
                                    label = { Text("3. Anggota Tim") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            OutlinedTextField(
                                value = timMekanik4,
                                onValueChange = { timMekanik4 = it },
                                label = { Text("4. Anggota Tim") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { activeTab = 1 },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ProfessionalAccent, contentColor = ProfessionalOnAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("LANJUT KE HASIL INSPEKSI", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null)
                        }
                    }

                    1 -> {
                        // TAB 2: HASIL INSPEKSI
                        Text("B. HASIL INSPEKSI", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)
                        Text("Masukkan kondisi unit dan berikan uraian lengkap jika kondisi TIDAK BAIK *", fontSize = 11.sp, color = ProfessionalSubtle)

                        val listComponentsComp = listOf(
                            Triple("1. MESIN / ENGINE", mesinStatus, mesinDesc),
                            Triple("2. TRANSMISI / TRANSMISSION", transmisiStatus, transmisiDesc),
                            Triple("3. SISTEM HYDRAULIC", hydraulicStatus, hydraulicDesc),
                            Triple("4. SISTEM PENDINGIN", pendinginStatus, pendinginDesc),
                            Triple("5. RODA / TRACK", rodaStatus, rodaDesc),
                            Triple("6. SISTEM BRAKE / REM", brakeStatus, brakeDesc),
                            Triple("7. SISTEM ELECTRICAL", electricalStatus, electricalDesc),
                            Triple("8. BODY", bodyStatus, bodyDesc),
                        )

                        listComponentsComp.forEach { (label, status, desc) ->
                            ComponentInspectionRow(
                                label = label,
                                status = status,
                                desc = desc,
                                onStatusChange = { newStatus ->
                                    when (label) {
                                        "1. MESIN / ENGINE" -> mesinStatus = newStatus
                                        "2. TRANSMISI / TRANSMISSION" -> transmisiStatus = newStatus
                                        "3. SISTEM HYDRAULIC" -> hydraulicStatus = newStatus
                                        "4. SISTEM PENDINGIN" -> pendinginStatus = newStatus
                                        "5. RODA / TRACK" -> rodaStatus = newStatus
                                        "6. SISTEM BRAKE / REM" -> brakeStatus = newStatus
                                        "7. SISTEM ELECTRICAL" -> electricalStatus = newStatus
                                        "8. BODY" -> bodyStatus = newStatus
                                    }
                                },
                                onDescChange = { newDesc ->
                                    when (label) {
                                        "1. MESIN / ENGINE" -> mesinDesc = newDesc
                                        "2. TRANSMISI / TRANSMISSION" -> transmisiDesc = newDesc
                                        "3. SISTEM HYDRAULIC" -> hydraulicDesc = newDesc
                                        "4. SISTEM PENDINGIN" -> pendinginDesc = newDesc
                                        "5. RODA / TRACK" -> rodaDesc = newDesc
                                        "6. SISTEM BRAKE / REM" -> brakeDesc = newDesc
                                        "7. SISTEM ELECTRICAL" -> electricalDesc = newDesc
                                        "8. BODY" -> bodyDesc = newDesc
                                    }
                                },
                                validationFail = validationTriggered && status == "TIDAK BAIK" && desc.isEmpty()
                            )
                        }

                        // Custom 9 component input
                        ComponentInspectionRowCustom(
                            customLabelVal = custom9Label,
                            onLabelValChange = { custom9Label = it },
                            placeholderLabel = "9. Tambah Komponen Lain...",
                            status = custom9Status,
                            desc = custom9Desc,
                            onStatusChange = { custom9Status = it },
                            onDescChange = { custom9Desc = it },
                            validationFail = validationTriggered && custom9Label.isNotEmpty() && custom9Status == "TIDAK BAIK" && custom9Desc.isEmpty()
                        )

                        // Custom 10 component input
                        ComponentInspectionRowCustom(
                            customLabelVal = custom10Label,
                            onLabelValChange = { custom10Label = it },
                            placeholderLabel = "10. Tambah Komponen Lain...",
                            status = custom10Status,
                            desc = custom10Desc,
                            onStatusChange = { custom10Status = it },
                            onDescChange = { custom10Desc = it },
                            validationFail = validationTriggered && custom10Label.isNotEmpty() && custom10Status == "TIDAK BAIK" && custom10Desc.isEmpty()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // CATEGORIES CRITICAL CHECKS
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // KATEGORI KERUSAKAN Column
                            Card(
                                modifier = Modifier.weight(1.1f),
                                colors = CardDefaults.cardColors(containerColor = CharcoalLight),
                                border = BorderStroke(
                                    1.dp, 
                                    if (validationTriggered && kategoriKerusakan.isEmpty()) ColorRejected else Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("KATEGORI KERUSAKAN *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    listOf("BERAT", "SEDANG", "RINGAN").forEach { k ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { kategoriKerusakan = k },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = kategoriKerusakan == k,
                                                onClick = { kategoriKerusakan = k },
                                                colors = RadioButtonDefaults.colors(selectedColor = ProfessionalAccent)
                                            )
                                            Text(k, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }

                            // TINDAKAN YANG HARUS DILAKUKAN Column
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = CharcoalLight),
                                border = BorderStroke(
                                    1.dp, 
                                    if (validationTriggered && tindakanDibutuhkan.isEmpty()) ColorRejected else Color.White.copy(alpha = 0.05f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("TINDAKAN DIBUTUHKAN *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    listOf("SERVICE", "REPLACE").forEach { t ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().clickable { tindakanDibutuhkan = t },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = tindakanDibutuhkan == t,
                                                onClick = { tindakanDibutuhkan = t },
                                                colors = RadioButtonDefaults.colors(selectedColor = ProfessionalAccent)
                                            )
                                            Text(t, fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { activeTab = 0 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                border = BorderStroke(1.dp, ProfessionalAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = ProfessionalAccent)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("KEMBALI", color = ProfessionalAccent, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { activeTab = 2 },
                                modifier = Modifier.weight(1.3f).height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ProfessionalAccent, contentColor = ProfessionalOnAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("LANJUT KE SPAREPART", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                            }
                        }
                    }

                    2 -> {
                        // TAB 3: SPAREPART DETAILS
                        Text("C. MATERIAL REQUEST & READY CHECKLIST", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)

                        // Ready Tipe Checkboxes
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CharcoalLight),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(
                                1.dp, 
                                if (validationTriggered && readyTypeChecked.isEmpty()) ColorRejected else Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("TIPE PERSEDIAAN READY (✓) *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent)
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { readyTypeChecked = "PART_READY" },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = readyTypeChecked == "PART_READY",
                                        onClick = { readyTypeChecked = "PART_READY" }
                                    )
                                    Text("1. PART READY (Tersedia di Gudang)", fontSize = 11.sp, color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { readyTypeChecked = "ORDER_PART" },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = readyTypeChecked == "ORDER_PART",
                                        onClick = { readyTypeChecked = "ORDER_PART" }
                                    )
                                    Text("2. ORDER PART (Perlu Order)", fontSize = 11.sp, color = Color.White)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { readyTypeChecked = "LAINNYA" },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = readyTypeChecked == "LAINNYA",
                                        onClick = { readyTypeChecked = "LAINNYA" }
                                    )
                                    Text("3. LAINNYA", fontSize = 11.sp, color = Color.White)
                                }

                                AnimatedVisibility(visible = readyTypeChecked == "LAINNYA") {
                                    OutlinedTextField(
                                        value = readyLainnyaText,
                                        onValueChange = { readyLainnyaText = it },
                                        placeholder = { Text("Contoh: BUBUT, PABRIKASI, KALIBRASI, DSB") },
                                        label = { Text("Keterangan Kategori Lainnya *") },
                                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                        isError = validationTriggered && readyLainnyaText.isEmpty()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // REQUESTED SPAREPARTS TABLE DISPLAY
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("DAFTAR SPAREPART REQUEST (Maksimal 12) *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)
                            
                            // Add row button
                            if (partsList.size < 12) {
                                TextButton(
                                    onClick = { 
                                        partsList = partsList + PartRequestRow(partsList.size + 1)
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = ColorApproved)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tambah Baris", color = ColorApproved, fontSize = 11.sp)
                                }
                            }
                        }

                        // Display dynamic parts items with explicit validation alerts
                        partsList.forEachIndexed { idx, row ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CharcoalLight),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("BARIS NO. ${idx + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ProfessionalSubtle)
                                        
                                        // Delete row button
                                        if (partsList.size > 1) {
                                            IconButton(
                                                onClick = {
                                                    partsList = partsList.filterIndexed { i, _ -> i != idx }
                                                        .mapIndexed { itemIndex, item -> item.copy(no = itemIndex + 1) }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus Baris", tint = ColorRejected, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Part Number & Name Part Row
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = row.partNumber,
                                            onValueChange = { newVal ->
                                                partsList = partsList.mapIndexed { i, item ->
                                                    if (i == idx) item.copy(partNumber = newVal) else item
                                                }
                                            },
                                            label = { Text("Part Number *") },
                                            placeholder = { Text("Contoh: 207-01-71210") },
                                            modifier = Modifier.weight(1.1f),
                                            isError = validationTriggered && (row.namePart.isNotEmpty() || row.qty.isNotEmpty() || row.uom.isNotEmpty()) && row.partNumber.isEmpty()
                                        )

                                        OutlinedTextField(
                                            value = row.namePart,
                                            onValueChange = { newVal ->
                                                partsList = partsList.mapIndexed { i, item ->
                                                    if (i == idx) item.copy(namePart = newVal) else item
                                                }
                                            },
                                            label = { Text("Nama Part *") },
                                            placeholder = { Text("Contoh: Piston Ring Set") },
                                            modifier = Modifier.weight(1.3f).testTag("mr_partname_input_row_" + (idx+1)),
                                            isError = validationTriggered && (row.partNumber.isNotEmpty() || row.qty.isNotEmpty() || row.uom.isNotEmpty()) && row.namePart.isEmpty()
                                        )
                                    }

                                    // Qty & UOM & Keterangan Row
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = row.qty,
                                            onValueChange = { newVal ->
                                                partsList = partsList.mapIndexed { i, item ->
                                                    if (i == idx) item.copy(qty = newVal) else item
                                                }
                                            },
                                            label = { Text("Qty *") },
                                            placeholder = { Text("6") },
                                            modifier = Modifier.weight(1f),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            isError = validationTriggered && (row.namePart.isNotEmpty() || row.partNumber.isNotEmpty() || row.uom.isNotEmpty()) && row.qty.isEmpty()
                                        )

                                        OutlinedTextField(
                                            value = row.uom,
                                            onValueChange = { newVal ->
                                                partsList = partsList.mapIndexed { i, item ->
                                                    if (i == idx) item.copy(uom = newVal) else item
                                                }
                                            },
                                            label = { Text("UOM *") },
                                            placeholder = { Text("Pcs / Set") },
                                            modifier = Modifier.weight(1f),
                                            isError = validationTriggered && (row.namePart.isNotEmpty() || row.partNumber.isNotEmpty() || row.qty.isNotEmpty()) && row.uom.isEmpty()
                                        )

                                        OutlinedTextField(
                                            value = row.keterangan,
                                            onValueChange = { newVal ->
                                                partsList = partsList.mapIndexed { i, item ->
                                                    if (i == idx) item.copy(keterangan = newVal) else item
                                                }
                                            },
                                            label = { Text("Keterangan (Opsi)") },
                                            modifier = Modifier.weight(1.5f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // PHOTOS SECTION
                        Text("DOKUMENTASI FOTO UNIT ATAU KERUSAKAN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ProfessionalAccent, letterSpacing = 1.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val cameraShots = listOf("img_excav_piston.jpg", "img_leak.jpg", "img_gear_leveled.jpg")
                                    photosList = photosList + cameraShots.random()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ambil Kamera", fontSize = 11.sp)
                            }

                            Button(
                                onClick = {
                                    val galleryPhotos = listOf("gallery_broken.png", "diff_gears.png", "wire_electrical.jpg")
                                    photosList = photosList + galleryPhotos.random()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F))
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Galeri", fontSize = 11.sp)
                            }
                        }

                        // Attached photos row
                        if (photosList.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                photosList.forEachIndexed { iIndex, name ->
                                    Box(
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)).background(CharcoalLight).border(1.dp, Color.White.copy(alpha = 0.05f))
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize().padding(4.dp)) {
                                            Icon(Icons.Default.Image, contentDescription = null, tint = ProfessionalAccent, modifier = Modifier.size(20.dp))
                                            Text(name, fontSize = 8.sp, color = Color.White, maxLines = 1)
                                        }
                                        IconButton(
                                            onClick = { photosList = photosList.filterIndexed { pi, _ -> pi != iIndex } },
                                            modifier = Modifier.align(Alignment.TopEnd).size(20.dp).background(Color.Red, CircleShape)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(10.dp))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = catatan,
                            onValueChange = { catatan = it },
                            label = { Text("Catatan Tambahan (Opsi)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Back
                            OutlinedButton(
                                onClick = { activeTab = 1 },
                                modifier = Modifier.weight(1f).height(50.dp),
                                border = BorderStroke(1.dp, ProfessionalAccent),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("UP INSPEKSI", color = ProfessionalAccent, fontWeight = FontWeight.Bold)
                            }

                            // Save draft
                            OutlinedButton(
                                onClick = {
                                    val tMekanik = listOf(timMekanik1, timMekanik2, timMekanik3, timMekanik4).filter { it.isNotEmpty() }.joinToString(",")
                                    viewModel.saveDraftMr(
                                        unit = modelUnit, hmKm = hmKmUnit, lokasi = lokasiUnit, desc = breakdownDescription,
                                        partNo = partsList.firstOrNull()?.partNumber ?: "", partName = partsList.firstOrNull()?.namePart ?: "",
                                        qty = partsList.firstOrNull()?.qty?.toIntOrNull() ?: 1, priority = "Normal",
                                        photos = photosList, catatan = catatan,
                                        jenisUnit = jenisUnit, modelUnit = modelUnit, serialNumberUnit = serialNumberUnit, noUnit = noUnit,
                                        tanggalBreakdown = tanggalBreakdown, tanggalInspeksi = tanggalInspeksi, timMekanik = tMekanik,
                                        kondisiMesinStatus = mesinStatus, kondisiMesinDesc = mesinDesc,
                                        kondisiTransmisiStatus = transmisiStatus, kondisiTransmisiDesc = transmisiDesc,
                                        kondisiHydraulicStatus = hydraulicStatus, kondisiHydraulicDesc = hydraulicDesc,
                                        kondisiPendinginStatus = pendinginStatus, kondisiPendinginDesc = pendinginDesc,
                                        kondisiRodaStatus = rodaStatus, kondisiRodaDesc = rodaDesc,
                                        kondisiBrakeStatus = brakeStatus, kondisiBrakeDesc = brakeDesc,
                                        kondisiElectricalStatus = electricalStatus, kondisiElectricalDesc = electricalDesc,
                                        kondisiBodyStatus = bodyStatus, kondisiBodyDesc = bodyDesc,
                                        kondisiCustom9Label = custom9Label, kondisiCustom9Status = custom9Status, kondisiCustom9Desc = custom9Desc,
                                        kondisiCustom10Label = custom10Label, kondisiCustom10Status = custom10Status, kondisiCustom10Desc = custom10Desc,
                                        kategoriKerusakan = kategoriKerusakan, tindakanDibutuhkan = tindakanDibutuhkan,
                                        readyTypeChecked = readyTypeChecked, readyLainnyaText = readyLainnyaText,
                                        partsTableSerialized = serializePartsTable(partsList)
                                    )
                                    Toast.makeText(context, "DRAFT berhasil disimpan secara lokal!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f).height(50.dp),
                                border = BorderStroke(1.dp, ColorWaiting),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorWaiting),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("DRAFT", fontWeight = FontWeight.Bold)
                            }

                            // Submit request button with mandatory requirements check
                            Button(
                                onClick = {
                                    validationTriggered = true
                                    if (isFormCompleteAndValid) {
                                        val firstPart = partsList.first { it.namePart.isNotEmpty() && it.partNumber.isNotEmpty() }
                                        val tMekanik = listOf(timMekanik1, timMekanik2, timMekanik3, timMekanik4).filter { it.isNotEmpty() }.joinToString(",")
                                        
                                        viewModel.submitMaterialRequest(
                                            unit = modelUnit, hmKm = hmKmUnit, lokasi = lokasiUnit, desc = breakdownDescription,
                                            partNo = firstPart.partNumber, partName = firstPart.namePart,
                                            qty = firstPart.qty.toIntOrNull() ?: 1, priority = "Normal",
                                            photos = photosList, catatan = catatan,
                                            jenisUnit = jenisUnit, modelUnit = modelUnit, serialNumberUnit = serialNumberUnit, noUnit = noUnit,
                                            tanggalBreakdown = tanggalBreakdown, tanggalInspeksi = tanggalInspeksi, timMekanik = tMekanik,
                                            kondisiMesinStatus = mesinStatus, kondisiMesinDesc = mesinDesc,
                                            kondisiTransmisiStatus = transmisiStatus, kondisiTransmisiDesc = transmisiDesc,
                                            kondisiHydraulicStatus = hydraulicStatus, kondisiHydraulicDesc = hydraulicDesc,
                                            kondisiPendinginStatus = pendinginStatus, kondisiPendinginDesc = pendinginDesc,
                                            kondisiRodaStatus = rodaStatus, kondisiRodaDesc = rodaDesc,
                                            kondisiBrakeStatus = brakeStatus, kondisiBrakeDesc = brakeDesc,
                                            kondisiElectricalStatus = electricalStatus, kondisiElectricalDesc = electricalDesc,
                                            kondisiBodyStatus = bodyStatus, kondisiBodyDesc = bodyDesc,
                                            kondisiCustom9Label = custom9Label, kondisiCustom9Status = custom9Status, kondisiCustom9Desc = custom9Desc,
                                            kondisiCustom10Label = custom10Label, kondisiCustom10Status = custom10Status, kondisiCustom10Desc = custom10Desc,
                                            kategoriKerusakan = kategoriKerusakan, tindakanDibutuhkan = tindakanDibutuhkan,
                                            readyTypeChecked = readyTypeChecked, readyLainnyaText = readyLainnyaText,
                                            partsTableSerialized = serializePartsTable(partsList)
                                        ) {
                                            Toast.makeText(context, "MR berhasil dikirim ke Foreman PIC!", Toast.LENGTH_LONG).show()
                                            onNavigateBack()
                                        }
                                    } else {
                                        Toast.makeText(context, "Form masih belum lengkap! Silakan periksa tab lainnya yang ditandai merah.", Toast.LENGTH_LONG).show()
                                        
                                        // Auto focus correct tab that has errors
                                        if (!isUnitValid) activeTab = 0
                                        else if (!isInspeksiValid) activeTab = 1
                                    }
                                },
                                modifier = Modifier.weight(1.3f).height(50.dp).testTag("submit_mr_form_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorApproved, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SEND MR", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Scan QR Unit simulation Dialog
    if (showScanQrDialog) {
        val qrCodesList = listOf(
            Triple("EXCAVATOR KOMATSU PC200-8 (Code: EXCA-201)", "PC200-8", "Excavator|EXCA-201|SNKOM9882|Loc: Pit Area Block 4A"),
            Triple("HEAVY DUMP TRUCK HD785-7 (Code: TRUCK-108)", "HD785-7", "Dump Truck|TRUCK-108|SNKOM77712|Loc: Stockpile Lane 2"),
            Triple("BULLDOZER D85ESS-2A (Code: DOZER-052)", "D85ESS-2A", "Bulldozer|DOZER-052|SNKOM12455|Loc: Pit Area West"),
            Triple("MOTOR GRADER GD511A (Code: GRAD-030)", "GD511A", "Motor Grader|GRAD-030|SNKOM3031|Loc: Access Road KM11")
        )

        AlertDialog(
            onDismissRequest = { showScanQrDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ProfessionalAccent)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("QR Scanner Lapangan")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Pilih kode barcode / QR logam unit di lokasi untuk simulasi scan otomatis:", fontSize = 12.sp, color = Color.LightGray)
                    
                    qrCodesList.forEach { (label, model, payload) ->
                        Surface(
                            color = CharcoalLight,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val tokens = payload.split("|")
                                    jenisUnit = tokens[0]
                                    noUnit = tokens[1]
                                    serialNumberUnit = tokens[2]
                                    lokasiUnit = tokens[3]
                                    modelUnit = model
                                    showScanQrDialog = false
                                    Toast.makeText(context, "Selesai menscan QR Unit: ${tokens[1]}", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text(payload, fontSize = 10.sp, color = Color.LightGray)
                                }
                                Icon(Icons.Default.QrCode, contentDescription = "Scan", tint = ProfessionalAccent)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showScanQrDialog = false }) {
                    Text("Tutup", color = Color.Red)
                }
            }
        )
    }
}

// Sub components decoration
@Composable
fun BaseErrorIndicator() {
    Box(
        modifier = Modifier
            .padding(start = 4.dp)
            .size(8.dp)
            .background(ColorRejected, CircleShape)
    )
}

@Composable
fun ComponentInspectionRow(
    label: String,
    status: String,
    desc: String,
    onStatusChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    validationFail: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalLight),
        border = BorderStroke(1.dp, if (validationFail) ColorRejected else Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                // Segmented status buttons
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF263238))
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (status == "BAIK") ColorApproved else Color.Transparent)
                            .clickable { onStatusChange("BAIK") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("BAIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (status == "BAIK") Color.White else Color.LightGray)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (status == "TIDAK BAIK") ColorRejected else Color.Transparent)
                            .clickable { onStatusChange("TIDAK BAIK") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("TIDAK BAIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (status == "TIDAK BAIK") Color.White else Color.LightGray)
                    }
                }
            }

            AnimatedVisibility(visible = status == "TIDAK BAIK") {
                OutlinedTextField(
                    value = desc,
                    onValueChange = onDescChange,
                    placeholder = { Text("Tulis rincian kerusakan komponen ini...") },
                    label = { Text("Uraian Lengkap Kerusakan *") },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    isError = validationFail
                )
            }
        }
    }
}

@Composable
fun ComponentInspectionRowCustom(
    customLabelVal: String,
    onLabelValChange: (String) -> Unit,
    placeholderLabel: String,
    status: String,
    desc: String,
    onStatusChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    validationFail: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalLight),
        border = BorderStroke(1.dp, if (validationFail) ColorRejected else Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customLabelVal,
                    onValueChange = onLabelValChange,
                    placeholder = { Text(placeholderLabel) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Segmented status buttons
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF263238))
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .background(if (status == "BAIK") ColorApproved else Color.Transparent)
                            .clickable { onStatusChange("BAIK") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("BAIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (status == "BAIK") Color.White else Color.LightGray)
                    }
                    Box(
                        modifier = Modifier
                            .background(if (status == "TIDAK BAIK") ColorRejected else Color.Transparent)
                            .clickable { onStatusChange("TIDAK BAIK") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("TIDAK BAIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (status == "TIDAK BAIK") Color.White else Color.LightGray)
                    }
                }
            }

            AnimatedVisibility(visible = status == "TIDAK BAIK" && customLabelVal.isNotEmpty()) {
                OutlinedTextField(
                    value = desc,
                    onValueChange = onDescChange,
                    placeholder = { Text("Tulis rincian kerusakan...") },
                    label = { Text("Uraian Lengkap Kerusakan *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = validationFail
                )
            }
        }
    }
}
