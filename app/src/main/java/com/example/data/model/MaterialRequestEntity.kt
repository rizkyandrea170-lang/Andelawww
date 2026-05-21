package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "material_requests")
data class MaterialRequestEntity(
    @PrimaryKey val mrNumber: String,
    val date: String,
    val mechanicNik: String,
    val mechanicName: String,
    val unitEquipment: String,
    val hmKmUnit: String,
    val lokasiUnit: String,
    val breakdownDescription: String,
    val partNumber: String,
    val partName: String,
    val quantity: Int,
    val priority: String, // "Urgent", "Normal", "Schedule"
    val photos: String, // Comma-separated photo paths/URIs
    val catatan: String,
    val status: String, // "DRAFT", "WAITING_APPROVAL_PIC", "APPROVED", "REJECTED", "REVISI"
    val alasanReject: String = "",
    val prNumber: String? = null,
    val isSynced: Boolean = true,
    
    // PDF FORM EXTENSIONS
    val areaKerja: String = "",
    val jenisUnit: String = "",
    val modelUnit: String = "",
    val serialNumberUnit: String = "",
    val noUnit: String = "",
    val tanggalBreakdown: String = "",
    val tanggalInspeksi: String = "",
    val timMekanik: String = "",
    
    val kondisiMesinStatus: String = "BAIK",
    val kondisiMesinDesc: String = "",
    val kondisiTransmisiStatus: String = "BAIK",
    val kondisiTransmisiDesc: String = "",
    val kondisiHydraulicStatus: String = "BAIK",
    val kondisiHydraulicDesc: String = "",
    val kondisiPendinginStatus: String = "BAIK",
    val kondisiPendinginDesc: String = "",
    val kondisiRodaStatus: String = "BAIK",
    val kondisiRodaDesc: String = "",
    val kondisiBrakeStatus: String = "BAIK",
    val kondisiBrakeDesc: String = "",
    val kondisiElectricalStatus: String = "BAIK",
    val kondisiElectricalDesc: String = "",
    val kondisiBodyStatus: String = "BAIK",
    val kondisiBodyDesc: String = "",
    
    val kondisiCustom9Label: String = "",
    val kondisiCustom9Status: String = "BAIK",
    val kondisiCustom9Desc: String = "",
    val kondisiCustom10Label: String = "",
    val kondisiCustom10Status: String = "BAIK",
    val kondisiCustom10Desc: String = "",
    
    val kategoriKerusakan: String = "", // "BERAT", "SEDANG", "RINGAN"
    val tindakanDibutuhkan: String = "", // "SERVICE", "REPLACE"
    
    val readyTypeChecked: String = "", // "PART_READY", "ORDER_PART", "LAINNYA" or comma-separated
    val readyLainnyaText: String = "", // text for BUBUT, PABRIKASI, etc.
    
    val partsTableSerialized: String = "" // Serialized list of up to 12 parts requested
)
