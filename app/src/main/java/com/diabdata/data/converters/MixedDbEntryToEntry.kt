package com.diabdata.data.converters

import com.diabdata.data.DataViewModel
import com.diabdata.models.Appointment
import com.diabdata.models.DiagnosisDate
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

// Fonction d'extension pour convertir n'importe quel MixedDbEntry
fun DataViewModel.MixedDbEntry.toEntity(): Any = when (this) {
    is DataViewModel.MixedDbEntry.AppointmentEntry -> Appointment(
        id = id,
        date = date,
        doctor = doctor,
        type = type,
        notes = notes,
        isArchived = isArchived,
        createdAt = createdAt
    )

    is DataViewModel.MixedDbEntry.DiagnosisEntry -> DiagnosisDate(
        id = id,
        date = date,
        diagnosis = diagnosis,
        isArchived = isArchived,
        createdAt = createdAt
    )

    is DataViewModel.MixedDbEntry.Hba1cEntry -> HBA1CEntry(
        id = id,
        date = date,
        value = value,
        isArchived = isArchived,
        createdAt = createdAt
    )

    is DataViewModel.MixedDbEntry.TreatmentEntry -> Treatment(
        id = id,
        expirationDate = date,
        name = name,
        type = treatmentType,
        isArchived = isArchived,
        createdAt = createdAt
    )

    is DataViewModel.MixedDbEntry.WeightEntry -> WeightEntry(
        id = id,
        date = date,
        value = value,
        isArchived = isArchived,
        createdAt = createdAt
    )
}