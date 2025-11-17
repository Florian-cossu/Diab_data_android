package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.shared.R as shared

enum class AppointmentType(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    ANNUAL_CHECKUP(
        displayNameRes = shared.string.appointment_annual_checkup,
        iconRes = shared.drawable.recurring_event_icon_vector
    ),
    APPOINTMENT(
        displayNameRes = shared.string.appointment_one_time,
        iconRes = shared.drawable.stethoscope_icon_vector
    );

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}