package com.diabdata.data.converters

import com.diabdata.data.DataViewModel
import com.diabdata.models.Appointment
import com.diabdata.models.HBA1CEntry
import com.diabdata.models.ImportantDate
import com.diabdata.models.Treatment
import com.diabdata.models.WeightEntry

// Fonction d'extension pour convertir n'importe quel MixedDbEntry
fun DataViewModel.MixedDbEntry.toEntity(): Any = when (this) {
    is DataViewModel.MixedDbEntry.AppointmentEntry -> Appointment(
        id = id,
        date = date,
        doctor = doctor,
        type = type,
        createdAt = createdAt,
        isArchived = isArchived,
        notes = notes,
        updatedAt = updatedAt
    )

    is DataViewModel.MixedDbEntry.ImportantDateEntry -> ImportantDate(
        id = id,
        date = date,
        createdAt = createdAt,
        isArchived = isArchived,
        importantDate = importantDate,
        updatedAt = updatedAt
    )

    is DataViewModel.MixedDbEntry.Hba1cEntry -> HBA1CEntry(
        id = id,
        date = date,
        createdAt = createdAt,
        isArchived = isArchived,
        value = value,
        updatedAt = updatedAt
    )

    is DataViewModel.MixedDbEntry.TreatmentEntry -> Treatment(
        id = id,
        expirationDate = date,
        name = name,
        createdAt = createdAt,
        isArchived = isArchived,
        type = treatmentType,
        updatedAt = updatedAt
    )

    is DataViewModel.MixedDbEntry.WeightEntry -> WeightEntry(
        id = id,
        date = date,
        createdAt = createdAt,
        isArchived = isArchived,
        value = value,
        updatedAt = updatedAt
    )
}