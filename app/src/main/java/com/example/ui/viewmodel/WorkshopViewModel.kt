package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.MaterialRequestEntity
import com.example.data.model.PurchaseRequestEntity
import com.example.data.model.UserEntity
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String,
    val type: String // "waiting", "approved", "rejected", "pr", "new_user"
)

class WorkshopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WorkshopRepository
    
    // Auth state
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    private val _registerStatus = MutableStateFlow<String?>(null)
    val registerStatus: StateFlow<String?> = _registerStatus.asStateFlow()

    // Filters and search for MR
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterUnit = MutableStateFlow("")
    val filterUnit: StateFlow<String> = _filterUnit.asStateFlow()

    private val _filterStatus = MutableStateFlow("")
    val filterStatus: StateFlow<String> = _filterStatus.asStateFlow()

    private val _filterMechanic = MutableStateFlow("")
    val filterMechanic: StateFlow<String> = _filterMechanic.asStateFlow()

    private val _filterDate = MutableStateFlow("")
    val filterDate: StateFlow<String> = _filterDate.asStateFlow()

    // Realtime-like Notification list
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Attendance state
    private val _todayAttendance = MutableStateFlow<AttendanceEntity?>(null)
    val todayAttendance: StateFlow<AttendanceEntity?> = _todayAttendance.asStateFlow()

    // Draft State
    private val _draftMr = MutableStateFlow<MaterialRequestEntity?>(null)
    val draftMr: StateFlow<MaterialRequestEntity?> = _draftMr.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = WorkshopRepository(db)
        
        // Add default notifications
        _notifications.value = listOf(
            NotificationItem(title = "MR Baru Terdaftar", message = "Mekanik Budi membuat MR-2026-0002", timestamp = "10 Mins Ago", type = "waiting"),
            NotificationItem(title = "PR Selesai", message = "PR-2026-0001 untuk MR-2026-0001 selesai diproses", timestamp = "1 Hour Ago", type = "pr"),
            NotificationItem(title = "User Baru Mendaftar", message = "Doni Prasetyo mendaftar sebagai Mekanik", timestamp = "2 Hours Ago", type = "new_user")
        )
    }

    // Streams from repository
    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingUsers: StateFlow<List<UserEntity>> = repository.pendingUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMaterialRequests: StateFlow<List<MaterialRequestEntity>> = repository.allMaterialRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPurchaseRequests: StateFlow<List<PurchaseRequestEntity>> = repository.allPurchaseRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAttendance: StateFlow<List<AttendanceEntity>> = repository.allAttendance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // FilterState Helper Class
    data class FilterState(
        val search: String,
        val unit: String,
        val status: String,
        val mechanic: String,
        val date: String
    )

    private val filterStateFlow = combine(
        _searchQuery,
        _filterUnit,
        _filterStatus,
        _filterMechanic,
        _filterDate
    ) { search, unit, status, mech, date ->
        FilterState(search, unit, status, mech, date)
    }

    // Filtered MR
    val filteredMaterialRequests: StateFlow<List<MaterialRequestEntity>> = combine(
        allMaterialRequests,
        filterStateFlow,
        _currentUser
    ) { mrs, filter, user ->
        mrs.filter { mr ->
            // If user is Mekanik, only see their own MRs unless they search
            val matchesRoleVisibility = if (user?.role == "MEKANIK") mr.mechanicNik == user.nik else true
            val matchesSearch = filter.search.isEmpty() || mr.mrNumber.contains(filter.search, ignoreCase = true) || 
                    mr.partName.contains(filter.search, ignoreCase = true) || 
                    mr.unitEquipment.contains(filter.search, ignoreCase = true)
            val matchesUnit = filter.unit.isEmpty() || mr.unitEquipment.equals(filter.unit, ignoreCase = true)
            val matchesStatus = if (filter.status.isEmpty()) {
                true
            } else if (filter.status.equals("PR_DONE", ignoreCase = true)) {
                mr.prNumber != null
            } else if (filter.status.equals("APPROVED", ignoreCase = true)) {
                mr.status.equals("APPROVED", ignoreCase = true) && mr.prNumber == null
            } else {
                mr.status.equals(filter.status, ignoreCase = true)
            }
            val matchesMech = filter.mechanic.isEmpty() || mr.mechanicName.contains(filter.mechanic, ignoreCase = true)
            val matchesDate = filter.date.isEmpty() || mr.date.startsWith(filter.date)

            matchesRoleVisibility && matchesSearch && matchesUnit && matchesStatus && matchesMech && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- ACTIONS ---

    fun searchMr(query: String) {
        _searchQuery.value = query
    }

    fun setFilters(unit: String, status: String, mech: String, date: String) {
        _filterUnit.value = unit
        _filterStatus.value = status
        _filterMechanic.value = mech
        _filterDate.value = date
    }

    fun clearFilters() {
        _filterUnit.value = ""
        _filterStatus.value = ""
        _filterMechanic.value = ""
        _filterDate.value = ""
        _searchQuery.value = ""
    }

    fun login(nik: String, oPass: String, onSuccess: (UserEntity) -> Unit) {
        viewModelScope.launch {
            _loginError.value = null
            val cleanedNik = nik.trim().lowercase(Locale.getDefault())
            val cleanedPass = oPass.trim()
            val user = repository.getUserByNik(cleanedNik)
            if (user == null) {
                _loginError.value = "User NIK tidak ditemukan!"
                return@launch
            }
            if (user.password.trim() != cleanedPass) {
                _loginError.value = "Password salah!"
                return@launch
            }
            if (!user.isApproved) {
                _loginError.value = "Akun Anda berstatus: Waiting Approval Admin!"
                return@launch
            }
            // Success
            _currentUser.value = user
            onSuccess(user)
            loadTodayAttendance(user.nik)
        }
    }

    fun logout() {
        _currentUser.value = null
        _todayAttendance.value = null
    }

    fun register(user: UserEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _registerStatus.value = null
            val sanitizedUser = user.copy(
                nik = user.nik.trim().lowercase(Locale.getDefault()),
                password = user.password.trim()
            )
            val success = repository.registerUser(sanitizedUser)
            if (success) {
                _registerStatus.value = "Registrasi Berhasil! Hubungi Admin Utama untuk persetujuan akun."
                // Add admin notification
                addNotification(
                    title = "New User Registration",
                    message = "${user.nama} (${user.role}) mendaftar di sistem.",
                    type = "new_user"
                )
                onSuccess()
            } else {
                _registerStatus.value = "Akun NIK sudah terdaftar!"
            }
        }
    }

    fun approveNewUser(nik: String) {
        viewModelScope.launch {
            repository.approveUser(nik)
            addNotification(
                title = "User Approved",
                message = "NIK $nik telah disetujui untuk login.",
                type = "approved"
            )
        }
    }

    fun rejectNewUser(nik: String) {
        viewModelScope.launch {
            repository.rejectUser(nik)
        }
    }

    // --- MR ACTION CREATORS ---
    
    suspend fun getNextMrNumber(): String {
        return repository.generateNextMrNumber()
    }

    fun submitMaterialRequest(
        unit: String,
        hmKm: String,
        lokasi: String,
        desc: String,
        partNo: String,
        partName: String,
        qty: Int,
        priority: String,
        photos: List<String>,
        catatan: String,
        jenisUnit: String,
        modelUnit: String,
        serialNumberUnit: String,
        noUnit: String,
        tanggalBreakdown: String,
        tanggalInspeksi: String,
        timMekanik: String,
        kondisiMesinStatus: String, kondisiMesinDesc: String,
        kondisiTransmisiStatus: String, kondisiTransmisiDesc: String,
        kondisiHydraulicStatus: String, kondisiHydraulicDesc: String,
        kondisiPendinginStatus: String, kondisiPendinginDesc: String,
        kondisiRodaStatus: String, kondisiRodaDesc: String,
        kondisiBrakeStatus: String, kondisiBrakeDesc: String,
        kondisiElectricalStatus: String, kondisiElectricalDesc: String,
        kondisiBodyStatus: String, kondisiBodyDesc: String,
        kondisiCustom9Label: String, kondisiCustom9Status: String, kondisiCustom9Desc: String,
        kondisiCustom10Label: String, kondisiCustom10Status: String, kondisiCustom10Desc: String,
        kategoriKerusakan: String,
        tindakanDibutuhkan: String,
        readyTypeChecked: String,
        readyLainnyaText: String,
        partsTableSerialized: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val mrNum = repository.generateNextMrNumber()
            val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            
            val mr = MaterialRequestEntity(
                mrNumber = mrNum,
                date = dateString,
                mechanicNik = user.nik,
                mechanicName = user.nama,
                unitEquipment = unit,
                hmKmUnit = hmKm,
                lokasiUnit = lokasi,
                breakdownDescription = desc,
                partNumber = partNo,
                partName = partName,
                quantity = qty,
                priority = priority,
                photos = photos.joinToString(","),
                catatan = catatan,
                status = "WAITING_APPROVAL_PIC",
                jenisUnit = jenisUnit,
                modelUnit = modelUnit,
                serialNumberUnit = serialNumberUnit,
                noUnit = noUnit,
                tanggalBreakdown = tanggalBreakdown,
                tanggalInspeksi = tanggalInspeksi,
                timMekanik = timMekanik,
                kondisiMesinStatus = kondisiMesinStatus, kondisiMesinDesc = kondisiMesinDesc,
                kondisiTransmisiStatus = kondisiTransmisiStatus, kondisiTransmisiDesc = kondisiTransmisiDesc,
                kondisiHydraulicStatus = kondisiHydraulicStatus, kondisiHydraulicDesc = kondisiHydraulicDesc,
                kondisiPendinginStatus = kondisiPendinginStatus, kondisiPendinginDesc = kondisiPendinginDesc,
                kondisiRodaStatus = kondisiRodaStatus, kondisiRodaDesc = kondisiRodaDesc,
                kondisiBrakeStatus = kondisiBrakeStatus, kondisiBrakeDesc = kondisiBrakeDesc,
                kondisiElectricalStatus = kondisiElectricalStatus, kondisiElectricalDesc = kondisiElectricalDesc,
                kondisiBodyStatus = kondisiBodyStatus, kondisiBodyDesc = kondisiBodyDesc,
                kondisiCustom9Label = kondisiCustom9Label, kondisiCustom9Status = kondisiCustom9Status, kondisiCustom9Desc = kondisiCustom9Desc,
                kondisiCustom10Label = kondisiCustom10Label, kondisiCustom10Status = kondisiCustom10Status, kondisiCustom10Desc = kondisiCustom10Desc,
                kategoriKerusakan = kategoriKerusakan,
                tindakanDibutuhkan = tindakanDibutuhkan,
                readyTypeChecked = readyTypeChecked,
                readyLainnyaText = readyLainnyaText,
                partsTableSerialized = partsTableSerialized
            )
            repository.saveMaterialRequest(mr)
            
            // Delete draft if it was a draft
            _draftMr.value = null

            addNotification(
                title = "MR Baru Terbuat",
                message = "${user.nama} membuat request $mrNum ($partName)",
                type = "waiting"
            )
            onSuccess()
        }
    }

    // Draft management
    fun saveDraftMr(
        unit: String,
        hmKm: String,
        lokasi: String,
        desc: String,
        partNo: String,
        partName: String,
        qty: Int,
        priority: String,
        photos: List<String>,
        catatan: String,
        jenisUnit: String = "",
        modelUnit: String = "",
        serialNumberUnit: String = "",
        noUnit: String = "",
        tanggalBreakdown: String = "",
        tanggalInspeksi: String = "",
        timMekanik: String = "",
        kondisiMesinStatus: String = "BAIK", kondisiMesinDesc: String = "",
        kondisiTransmisiStatus: String = "BAIK", kondisiTransmisiDesc: String = "",
        kondisiHydraulicStatus: String = "BAIK", kondisiHydraulicDesc: String = "",
        kondisiPendinginStatus: String = "BAIK", kondisiPendinginDesc: String = "",
        kondisiRodaStatus: String = "BAIK", kondisiRodaDesc: String = "",
        kondisiBrakeStatus: String = "BAIK", kondisiBrakeDesc: String = "",
        kondisiElectricalStatus: String = "BAIK", kondisiElectricalDesc: String = "",
        kondisiBodyStatus: String = "BAIK", kondisiBodyDesc: String = "",
        kondisiCustom9Label: String = "", kondisiCustom9Status: String = "BAIK", kondisiCustom9Desc: String = "",
        kondisiCustom10Label: String = "", kondisiCustom10Status: String = "BAIK", kondisiCustom10Desc: String = "",
        kategoriKerusakan: String = "",
        tindakanDibutuhkan: String = "",
        readyTypeChecked: String = "",
        readyLainnyaText: String = "",
        partsTableSerialized: String = ""
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val tempMr = MaterialRequestEntity(
                mrNumber = "DRAFT-${UUID.randomUUID().toString().take(4)}",
                date = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                mechanicNik = user.nik,
                mechanicName = user.nama,
                unitEquipment = unit,
                hmKmUnit = hmKm,
                lokasiUnit = lokasi,
                breakdownDescription = desc,
                partNumber = partNo,
                partName = partName,
                quantity = qty,
                priority = priority,
                photos = photos.joinToString(","),
                catatan = catatan,
                status = "DRAFT",
                jenisUnit = jenisUnit,
                modelUnit = modelUnit,
                serialNumberUnit = serialNumberUnit,
                noUnit = noUnit,
                tanggalBreakdown = tanggalBreakdown,
                tanggalInspeksi = tanggalInspeksi,
                timMekanik = timMekanik,
                kondisiMesinStatus = kondisiMesinStatus, kondisiMesinDesc = kondisiMesinDesc,
                kondisiTransmisiStatus = kondisiTransmisiStatus, kondisiTransmisiDesc = kondisiTransmisiDesc,
                kondisiHydraulicStatus = kondisiHydraulicStatus, kondisiHydraulicDesc = kondisiHydraulicDesc,
                kondisiPendinginStatus = kondisiPendinginStatus, kondisiPendinginDesc = kondisiPendinginDesc,
                kondisiRodaStatus = kondisiRodaStatus, kondisiRodaDesc = kondisiRodaDesc,
                kondisiBrakeStatus = kondisiBrakeStatus, kondisiBrakeDesc = kondisiBrakeDesc,
                kondisiElectricalStatus = kondisiElectricalStatus, kondisiElectricalDesc = kondisiElectricalDesc,
                kondisiBodyStatus = kondisiBodyStatus, kondisiBodyDesc = kondisiBodyDesc,
                kondisiCustom9Label = kondisiCustom9Label, kondisiCustom9Status = kondisiCustom9Status, kondisiCustom9Desc = kondisiCustom9Desc,
                kondisiCustom10Label = kondisiCustom10Label, kondisiCustom10Status = kondisiCustom10Status, kondisiCustom10Desc = kondisiCustom10Desc,
                kategoriKerusakan = kategoriKerusakan,
                tindakanDibutuhkan = tindakanDibutuhkan,
                readyTypeChecked = readyTypeChecked,
                readyLainnyaText = readyLainnyaText,
                partsTableSerialized = partsTableSerialized
            )
            _draftMr.value = tempMr
        }
    }

    fun clearDraft() {
        _draftMr.value = null
    }

    // Edit before approve or revisions
    fun updateMaterialRequestAfterRevision(mr: MaterialRequestEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.updateMaterialRequest(mr.copy(status = "WAITING_APPROVAL_PIC"))
            addNotification(
                title = "MR Direvisi Mekanik",
                message = "${mr.mrNumber} telah diajukan kembali.",
                type = "waiting"
            )
            onSuccess()
        }
    }

    // PIC/Foreman actions
    fun picApproveMr(mrNumber: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.getMaterialRequestByNumber(mrNumber)?.let { mr ->
                repository.updateMaterialRequest(mr.copy(status = "APPROVED"))
                
                // Automatically create raw logistics notice
                addNotification(
                    title = "MR Approved",
                    message = "$mrNumber disetujui PIC. Siap dibuat PR.",
                    type = "approved"
                )
                onSuccess()
            }
        }
    }

    fun picRejectMr(mrNumber: String, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.getMaterialRequestByNumber(mrNumber)?.let { mr ->
                repository.updateMaterialRequest(mr.copy(status = "REJECTED", alasanReject = reason))
                addNotification(
                    title = "MR Rejected",
                    message = "$mrNumber ditolak PIC: $reason",
                    type = "rejected"
                )
                onSuccess()
            }
        }
    }

    fun picRequestRevisionMr(mrNumber: String, reason: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.getMaterialRequestByNumber(mrNumber)?.let { mr ->
                repository.updateMaterialRequest(mr.copy(status = "REVISI", alasanReject = reason))
                addNotification(
                    title = "MR Butuh Revisi",
                    message = "$mrNumber memerlukan perbaikan: $reason",
                    type = "waiting"
                )
                onSuccess()
            }
        }
    }


    // --- PURCHASING / LOGISTICS ACTION ---
    fun submitPurchaseRequest(mrNumber: String, notes: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val prNum = repository.createPrFromMr(mrNumber, user.nik, notes)
            addNotification(
                title = "PR Terbuat",
                message = "$prNum terbuat untuk $mrNumber",
                type = "pr"
            )
            onSuccess(prNum)
        }
    }

    fun setPrStatus(prNumber: String, status: String) {
        viewModelScope.launch {
            // "WAITING_PR", "PR_PROCESS", "PR_COMPLETE"
            repository.updatePrStatus(prNumber, status)
            
            // Check original MR and link
            val pr = repository.getPurchaseRequestByNumber(prNumber)
            if (pr != null) {
                if (status == "PR_COMPLETE") {
                     addNotification(
                         title = "Sparepart Tiba (Complete)",
                         message = "Suku cadang $prNumber telah sampai dan siap diambil.",
                         type = "pr"
                     )
                } else if (status == "PR_PROCESS") {
                     addNotification(
                         title = "Sparepart Diproses",
                         message = "PR $prNumber dalam tahap pembelian vendor.",
                         type = "pr"
                     )
                }
            }
        }
    }


    // --- ATTENDANCE SYSTEM ---
    fun loadTodayAttendance(nik: String) {
        viewModelScope.launch {
            val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            _todayAttendance.value = repository.getTodayAttendance(nik, todayDate)
        }
    }

    fun checkIn(gpsLatLng: String, photoMock: String, onShowMessage: (String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            
            // Late logic: e.g. check in after 08:00 is late
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val isLate = hour > 8 || (hour == 8 && minute > 0)

            repository.recordCheckIn(user.nik, user.nama, dateStr, timeStr, gpsLatLng, photoMock, isLate)
            _todayAttendance.value = repository.getTodayAttendance(user.nik, dateStr)
            onShowMessage(if (isLate) "Check-In Berhasil! (Terlambat)" else "Check-In Berhasil Tepat Waktu!")
        }
    }

    fun checkOut(gpsLatLng: String, photoMock: String, onShowMessage: (String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            repository.recordCheckOut(user.nik, dateStr, timeStr, gpsLatLng, photoMock)
            _todayAttendance.value = repository.getTodayAttendance(user.nik, dateStr)
            onShowMessage("Check-Out Berhasil!")
        }
    }

    // --- HELPER WRAPPERS ---
    private fun addNotification(title: String, message: String, type: String) {
        val list = _notifications.value.toMutableList()
        val timeNow = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        list.add(0, NotificationItem(title = title, message = message, timestamp = timeNow, type = type))
        _notifications.value = list
    }
}
