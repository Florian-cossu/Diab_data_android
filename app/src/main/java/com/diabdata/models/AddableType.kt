package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.diabdata.R

enum class AddableType(
    val tableName: String,
    @param:StringRes val displayNameRes: Int,
    val baseColor: Color,
    @param:DrawableRes val iconRes: Int
) {
    WEIGHT(
        tableName = "weight_entries",
        displayNameRes = R.string.addable_weight,
        baseColor = Color(0xFF4CAF50),
        iconRes = R.drawable.weight_icon_vector
    ),
    HBA1C(
        tableName = "hba1c_entries",
        displayNameRes = R.string.addable_hba1c,
        baseColor = Color(0xFF4DB3EA),
        iconRes = R.drawable.hba1c_icon_vector
    ),
    APPOINTMENT(
        tableName = "appointments",
        displayNameRes = R.string.addable_appointment,
        baseColor = Color(0xFFF637C5),
        iconRes = R.drawable.event_icon_vector
    ),
    TREATMENT(
        tableName = "treatments", displayNameRes = R.string.addable_treatment,
        baseColor = Color(0xFF4ADCC4),
        iconRes = R.drawable.medication_icon_vector
    ),
    IMPORTANT_DATE(
        tableName = "important_date_entries",
        displayNameRes = R.string.addable_important_date,
        baseColor = Color(0xFFFCB227),
        iconRes = R.drawable.important_date_icon_vector
    ),
    DEVICE(
        tableName = "medical_devices",
        displayNameRes = R.string.addable_device,
        baseColor = Color(0xFF4DB3EA),
        iconRes = R.drawable.devices_icon_vector
    );

    fun getDisplayName(context: Context): String =
        context.getString(displayNameRes)
}