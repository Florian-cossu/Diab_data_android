package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.diabdata.R

enum class MedicalDevicesTypes(
    @param:StringRes
    val displayNameRes: Int,
    val baseColor: Color,
    @param:DrawableRes val iconRes: Int
) {
    WIRELESS_PATCH_REMOTE(
        displayNameRes = R.string.annual_checkup,
        iconRes = R.drawable.wireless_patch_remote_icon_vector,
        baseColor = Color(0xFF2DDE7D),
    ),
    WIRED_PUMP(
        displayNameRes = R.string.annual_checkup,
        iconRes = R.drawable.wired_pump_icon_vector,
        baseColor = Color(0xFF62DE2D),
    );

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}