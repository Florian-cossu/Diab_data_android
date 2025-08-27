package com.diabdata.models

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.R

enum class TreatmentType(@param:StringRes val displayNameRes: Int) {
    FAST_ACTING_INSULIN_CARTRIDGE(R.string.fast_acting_insulin_cartridge),
    FAST_ACTING_INSULIN_SYRINGE(R.string.fast_acting_insulin_syringe),
    FAST_ACTING_INSULIN_VIAL(R.string.fast_acting_insulin_vial),
    SLOW_ACTING_INSULIN_SYRINGE(R.string.slow_acting_insulin_syringe),
    SLOW_ACTING_INSULIN_CARTRIDGE(R.string.slow_acting_insulin_cartridge),
    SLOW_ACTING_INSULIN_VIAL(R.string.slow_acting_insulin_vial),
    GLUCAGON_SYRINGE(R.string.glucagon_syringe),
    GLUCAGON_SPRAY(R.string.glucagon_spray);

    fun displayName(context: Context): String =
        context.getString(displayNameRes)
}
