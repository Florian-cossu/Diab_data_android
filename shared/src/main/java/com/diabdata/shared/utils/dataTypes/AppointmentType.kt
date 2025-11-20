package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.shared.R

enum class AppointmentType(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    ANNUAL_CHECKUP(
        displayNameRes = R.string.appointment_annual_checkup,
        iconRes = R.drawable.recurring_event_icon_vector
    ),
    APPOINTMENT(
        displayNameRes = R.string.appointment_one_time,
        iconRes = R.drawable.stethoscope_icon_vector
    );

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}