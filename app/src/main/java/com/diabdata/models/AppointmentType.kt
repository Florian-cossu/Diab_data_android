package com.diabdata.models

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.R

enum class AppointmentType(@param:StringRes val displayNameRes: Int) {
    ANNUAL_CHECKUP(R.string.annual_checkup),
    APPOINTMENT(R.string.appointment);

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}
