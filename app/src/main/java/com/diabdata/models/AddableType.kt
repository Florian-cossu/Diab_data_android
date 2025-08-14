package com.diabdata.models

enum class AddableType(val tableName: String) {
    WEIGHT("weight_entries"),
    HBA1C("hba1c_entries"),
    APPOINTMENT("appointments"),
    TREATMENT("treatments"),
    DIAGNOSIS("diagnosis_date_entries")
}