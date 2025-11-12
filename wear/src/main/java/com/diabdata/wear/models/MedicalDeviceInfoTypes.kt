package com.diabdata.wear.models

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.diabdata.shared.R as shared

/**
 * Represents a type of medical device.
 * @param baseColor The base color of the device's icon.
 * @param iconRes The drawable resource ID for the device's icon.
 */

enum class MedicalDeviceInfoType(
    val baseColor: Color,
    @param:DrawableRes val iconRes: Int
) {
    WIRELESS_PATCH(
        iconRes = shared.drawable.wireless_patch_icon_vector,
        baseColor = Color(0xFFDEA32D),
    ),
    WIRED_PATCH(
        iconRes = shared.drawable.wired_patch_icon_vector,
        baseColor = Color(0xFFBEDE2D),
    ),
    CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_SENSOR(
        iconRes = shared.drawable.continuous_glucose_monitoring_system_sensor,
        baseColor = Color(0xFFFA7450),
    ),
    CONTINUOUS_GLUCOSE_MONITORING_SYSTEM_TRANSMITTER(
        iconRes = shared.drawable.continuous_glucose_monitoring_system_transmitter,
        baseColor = Color(0xFF458DEA),
    ),
    WIRELESS_PATCH_REMOTE(
        iconRes = shared.drawable.wireless_patch_remote_icon_vector,
        baseColor = Color(0xFF2DDE7D),
    ),
    WIRED_PUMP(
        iconRes = shared.drawable.wired_pump_icon_vector,
        baseColor = Color(0xFF62DE2D),
    ),
    UNKNOWN(
        iconRes = shared.drawable.no_devices_icon_vector,
        baseColor = Color(0xFF62DE2D),
    );
}