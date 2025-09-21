package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.R

enum class AppointmentType(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    ANNUAL_CHECKUP(
        displayNameRes = R.string.annual_checkup,
        iconRes = R.drawable.recurring_event_icon_vector
    ),
    APPOINTMENT(
        displayNameRes = R.string.appointment,
        iconRes = R.drawable.stethoscope_icon_vector
    );

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}