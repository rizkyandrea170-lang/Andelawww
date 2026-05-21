package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.AttendanceEntity
import com.example.data.model.MaterialRequestEntity
import com.example.data.model.PurchaseRequestEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class WorkshopRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val mrDao = db.materialRequestDao()
    private val prDao = db.purchaseRequestDao()
    private val attendanceDao = db.attendanceDao()

    init {
        // Run database seeding on start
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabase()
        }
    }

    // --- USER MANAGEMENT ---
    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsersFlow()
    val pendingUsers: Flow<List<UserEntity>> = userDao.getPendingApprovalUsers()

    suspend fun getUserByNik(nik: String): UserEntity? = withContext(Dispatchers.IO) {
        userDao.getUserByNik(nik)
    }

    suspend fun registerUser(user: UserEntity): Boolean = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByNik(user.nik)
        if (existing != null) {
            false // already exists
        } else {
            userDao.insertUser(user)
            true
        }
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    suspend fun approveUser(nik: String) = withContext(Dispatchers.IO) {
        userDao.getUserByNik(nik)?.let {
            userDao.updateUser(it.copy(isApproved = true))
        }
    }

    suspend fun rejectUser(nik: String) = withContext(Dispatchers.IO) {
        userDao.getUserByNik(nik)?.let {
            userDao.deleteUser(it)
        }
    }


    // --- MATERIAL REQUEST (MR) ---
    val allMaterialRequests: Flow<List<MaterialRequestEntity>> = mrDao.getAllMaterialRequests()

    fun getMaterialRequestsByMechanic(nik: String): Flow<List<MaterialRequestEntity>> {
        return mrDao.getMaterialRequestsByMechanic(nik)
    }

    suspend fun getMaterialRequestByNumber(mrNumber: String): MaterialRequestEntity? = withContext(Dispatchers.IO) {
        mrDao.getMaterialRequestByNumber(mrNumber)
    }

    private fun getRomanMonth(month: Int): String {
        return when (month) {
            1 -> "I"
            2 -> "II"
            3 -> "III"
            4 -> "IV"
            5 -> "V"
            6 -> "VI"
            7 -> "VII"
            8 -> "VIII"
            9 -> "IX"
            10 -> "X"
            11 -> "XI"
            12 -> "XII"
            else -> month.toString()
        }
    }

    suspend fun generateNextMrNumber(): String = withContext(Dispatchers.IO) {
        val calendar = Calendar.getInstance()
        val count = mrDao.getMaterialRequestsCount()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val romanMonth = getRomanMonth(month)
        String.format("%04d/MR/80/%s/%d", count + 1, romanMonth, year)
    }

    suspend fun saveMaterialRequest(mr: MaterialRequestEntity) = withContext(Dispatchers.IO) {
        mrDao.insertMaterialRequest(mr)
    }

    suspend fun updateMaterialRequest(mr: MaterialRequestEntity) = withContext(Dispatchers.IO) {
        mrDao.updateMaterialRequest(mr)
    }


    // --- PURCHASE REQUEST (PR) ---
    val allPurchaseRequests: Flow<List<PurchaseRequestEntity>> = prDao.getAllPurchaseRequests()

    suspend fun getPurchaseRequestByNumber(prNumber: String): PurchaseRequestEntity? = withContext(Dispatchers.IO) {
        prDao.getPurchaseRequestByNumber(prNumber)
    }

    suspend fun getPurchaseRequestByMr(mrNumber: String): PurchaseRequestEntity? = withContext(Dispatchers.IO) {
        prDao.getPurchaseRequestByMr(mrNumber)
    }

    suspend fun generateNextPrNumber(): String = withContext(Dispatchers.IO) {
        val count = prDao.getPurchaseRequestsCount()
        val year = Calendar.getInstance().get(Calendar.YEAR)
        String.format("PR-%d-%04d", year, count + 1)
    }

    suspend fun createPrFromMr(mrNumber: String, logisticNik: String, notes: String): String = withContext(Dispatchers.IO) {
        val prNum = generateNextPrNumber()
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        // Create PR
        val pr = PurchaseRequestEntity(
            prNumber = prNum,
            mrNumber = mrNumber,
            date = dateString,
            status = "PR_PROCESS", // Status: Waiting PR, PR Process, PR Complete
            notes = notes
        )
        prDao.insertPurchaseRequest(pr)

        // Update original MR with prNumber and status
        mrDao.getMaterialRequestByNumber(mrNumber)?.let { mr ->
            mrDao.updateMaterialRequest(mr.copy(
                prNumber = prNum,
                status = "APPROVED" // or retain approved, but linkage is established
            ))
        }

        prNum
    }

    suspend fun updatePrStatus(prNumber: String, status: String) = withContext(Dispatchers.IO) {
        prDao.getPurchaseRequestByNumber(prNumber)?.let { pr ->
            prDao.updatePurchaseRequest(pr.copy(status = status))
        }
    }


    // --- ATTENDANCE ---
    val allAttendance: Flow<List<AttendanceEntity>> = attendanceDao.getAllAttendanceFlow()

    fun getAttendanceByNik(nik: String): Flow<List<AttendanceEntity>> {
        return attendanceDao.getAttendanceByNikFlow(nik)
    }

    suspend fun getTodayAttendance(nik: String, date: String): AttendanceEntity? = withContext(Dispatchers.IO) {
        attendanceDao.getAttendanceByNikAndDate(nik, date)
    }

    suspend fun recordCheckIn(nik: String, name: String, date: String, time: String, gps: String, photo: String, isLate: Boolean) = withContext(Dispatchers.IO) {
        val attendance = AttendanceEntity(
            userNik = nik,
            userName = name,
            date = date,
            checkInTime = time,
            checkInGps = gps,
            checkInPhoto = photo,
            status = if (isLate) "TERLAMBAT" else "HADIR"
        )
        attendanceDao.insertAttendance(attendance)
    }

    suspend fun recordCheckOut(nik: String, date: String, time: String, gps: String, photo: String) = withContext(Dispatchers.IO) {
        attendanceDao.getAttendanceByNikAndDate(nik, date)?.let { existing ->
            attendanceDao.updateAttendance(existing.copy(
                checkOutTime = time,
                checkOutGps = gps,
                checkOutPhoto = photo
            ))
        }
    }


    // --- DATABASE SEEDING ---
    private suspend fun seedDatabase() {
        // 1. Seed Users (Admin, Mechanic, PIC/Foreman, Logistik) if empty
        if (userDao.getUserByNik("admin") == null) {
            userDao.insertUser(UserEntity("admin", "Admin Utama", "Supervisor", "Main Office", "08123456789", "admin", "ADMIN", true))
        }
        if (userDao.getUserByNik("adminall") == null) {
            userDao.insertUser(UserEntity("adminall", "Admin All Akses", "Super Admin", "Main Office", "08123451111", "adminall", "ADMIN", true))
        }
        if (userDao.getUserByNik("mech1") == null) {
            userDao.insertUser(UserEntity("mech1", "Budi Santoso", "Mekanik Senior", "Workshop Unit A", "08234567890", "mech1", "MEKANIK", true))
        }
        if (userDao.getUserByNik("mech2") == null) {
            userDao.insertUser(UserEntity("mech2", "Ahmad Fauzi", "Mekanik Helper", "Workshop Unit B", "08234567891", "mech2", "MEKANIK", true))
        }
        if (userDao.getUserByNik("foreman1") == null) {
            userDao.insertUser(UserEntity("foreman1", "Hendra Wijaya", "Foreman", "Workshop Unit A", "08345678901", "foreman1", "PIC", true))
        }
        if (userDao.getUserByNik("log1") == null) {
            userDao.insertUser(UserEntity("log1", "Siti Aminah", "Logistics Officer", "Warehouse main", "08456789012", "log1", "LOGISTIK", true))
        }
        if (userDao.getUserByNik("plant1") == null) {
            userDao.insertUser(UserEntity("plant1", "Plant Supervisor", "Admin Plant Supervisor", "Plant Room", "08123456711", "plant1", "ADMIN_PLANT", true))
        }
        if (userDao.getUserByNik("waiting1") == null) {
            userDao.insertUser(UserEntity("waiting1", "Doni Prasetyo", "Mekanik Apprentice", "Workshop Unit A", "08567890123", "password", "MEKANIK", false))
        }

        // 2. Seed MR if empty
        if (mrDao.getMaterialRequestsCount() == 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val cal = Calendar.getInstance()

            val mr1 = MaterialRequestEntity(
                mrNumber = "0001/MR/80/V/2026",
                date = "2026-05-18 09:30:00",
                mechanicNik = "mech1",
                mechanicName = "Budi Santoso",
                unitEquipment = "Excavator PC200",
                hmKmUnit = "12450 HM",
                lokasiUnit = "Pit Utara Blk C",
                breakdownDescription = "Engine overhaul - Piston ring cracked and leaking compression.",
                partNumber = "207-01-71210",
                partName = "Piston Ring Set Komatsu",
                quantity = 6,
                priority = "Urgent",
                photos = "sim_excavator_piston.jpg",
                catatan = "Butuh cepat karena unit mengganggu jalan angkut utama.",
                status = "APPROVED",
                prNumber = "PR-2026-0001"
            )
            mrDao.insertMaterialRequest(mr1)
            prDao.insertPurchaseRequest(PurchaseRequestEntity(
                prNumber = "PR-2026-0001",
                mrNumber = "0001/MR/80/V/2026",
                date = "2026-05-18 11:30:00",
                status = "PR_COMPLETE",
                notes = "Sparepart arrived on warehouse, pending mechanic pickup."
            ))

            val mr2 = MaterialRequestEntity(
                mrNumber = "0002/MR/80/V/2026",
                date = "2026-05-19 14:15:00",
                mechanicNik = "mech2",
                mechanicName = "Ahmad Fauzi",
                unitEquipment = "Dump Truck HD785",
                hmKmUnit = "31500 KM",
                lokasiUnit = "Workshop B Lap 1",
                breakdownDescription = "Brake pad thin & warning light flashing on dashboard.",
                partNumber = "561-09-12340",
                partName = "Brake Lining Plate Front",
                quantity = 4,
                priority = "Normal",
                photos = "sim_brake_lining.jpg",
                catatan = "Periodic replacement scheduled.",
                status = "WAITING_APPROVAL_PIC"
            )
            mrDao.insertMaterialRequest(mr2)

            val mr3 = MaterialRequestEntity(
                mrNumber = "0003/MR/80/V/2026",
                date = "2026-05-20 08:10:00",
                mechanicNik = "mech1",
                mechanicName = "Budi Santoso",
                unitEquipment = "Dozer D85ESS",
                hmKmUnit = "18700 HM",
                lokasiUnit = "Pit Selatan Blk F",
                breakdownDescription = "Track link cracked - Needs link segment assembly replacement.",
                partNumber = "14X-30-00110",
                partName = "Track Link Assy 41L",
                quantity = 2,
                priority = "Urgent",
                photos = "sim_dozer_track.jpg",
                catatan = "Breakdown on the field.",
                status = "REJECTED",
                alasanReject = "Spare dalam stok lokal masih ada, tidak perlu impor MR baru. Tolong cek loker warehouse lantai 2."
            )
            mrDao.insertMaterialRequest(mr3)
        }
    }
}
