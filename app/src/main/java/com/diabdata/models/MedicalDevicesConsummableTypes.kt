package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.diabdata.shared.R as shared

enum class MedicalDevicesConsumableTypes(
    @param:StringRes
    val displayNameRes: Int,
    val baseColor: Color,
    @param:DrawableRes val iconRes: Int
) {
    WIRELESS_PATCH(
        displayNameRes = shared.string.device_type_wireless_patch,
        iconRes = shared.drawable.wireless_patch_icon_vector,
        baseColor = Color(0xFFDEA32D),
    ),
    WIRED_PATCH(
        displayNameRes = shared.string.device_type_wired_patch,
        iconRes = shared.drawable.wired_patch_icon_vector,
        baseColor = Color(0xFFBEDE2D),
    ),
    CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR(
        displayNameRes = shared.string.device_type_cgm_sensor,
        iconRes = shared.drawable.continuous_glucose_monitoring_system_sensor_icon_vector,
        baseColor = Color(0xFFF55128),
    ),
    CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER(
        displayNameRes = shared.string.device_type_cgm_transmitter,
        iconRes = shared.drawable.continuous_glucose_monitoring_system_transmitter_icon_vector,
        baseColor = Color(0xFF458DEA),
    );

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}