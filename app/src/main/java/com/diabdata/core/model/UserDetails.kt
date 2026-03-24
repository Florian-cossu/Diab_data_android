package com.diabdata.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.diabdata.shared.utils.dataTypes.BloodType
import com.diabdata.shared.utils.dataTypes.DiabetesType
import com.diabdata.shared.utils.dataTypes.Gender
import com.diabdata.shared.utils.dataTypes.GlucoseUnit
import java.time.LocalDate

@Entity(tableName = "user_details")
data class UserDetails(
    @PrimaryKey val id: Int = 0,

    // --- Identity ---
    val firstName: String? = null,
    val lastName: String? = null,
    val profilePhotoPath: String? = null,
    val birthdate: LocalDate? = null,
    val gender: Gender? = null,
    val bloodType: BloodType? = null,

    // --- Diabetes ---
    val diabetesType: DiabetesType? = null,
    val diagnosisDate: LocalDate? = null,

    // --- Medical team ---
    val endocrinologist: String? = null,
    val generalPractitioner: String? = null,
    val ophthalmologist: String? = null,
    val cardiologist: String? = null,
    val nephrologist: String? = null,

    // --- Treatments ---
    val insulinPumpModel: String? = null,
    val cgmModel: String? = null,
    val insulinType: String? = null,
    val basalInsulinType: String? = null,

    // --- Blood sugar levels goals ---
    val targetGlucoseMin: Float? = null,
    val targetGlucoseMax: Float? = null,
    val glucoseUnit: GlucoseUnit? = null,

    // --- Emergency contact ---
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
)