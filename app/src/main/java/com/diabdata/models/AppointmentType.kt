package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.R
import com.diabdata.shared.R as shared

enum class AppointmentType(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    ANNUAL_CHECKUP(
        displayNameRes = R.string.annual_checkup,
        iconRes = shared.drawable.recurring_event_icon_vector
    ),
    APPOINTMENT(
        displayNameRes = R.string.appointment,
        iconRes = shared.drawable.stethoscope_icon_vector
    );

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}