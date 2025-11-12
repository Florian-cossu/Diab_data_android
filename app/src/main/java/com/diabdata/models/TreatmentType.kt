package com.diabdata.models

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.R
import com.diabdata.shared.R as shared

enum class TreatmentType(
    @param:StringRes val displayNameRes: Int, @param:DrawableRes val iconRes: Int
) {
    FAST_ACTING_INSULIN_CARTRIDGE(
        displayNameRes = R.string.fast_acting_insulin_cartridge,
        iconRes = shared.drawable.fast_acting_insulin_cartridge_icon_vector
    ),
    FAST_ACTING_INSULIN_SYRINGE(
        displayNameRes = R.string.fast_acting_insulin_syringe,
        iconRes = shared.drawable.fast_acting_insulin_syringe_icon_vector
    ),
    FAST_ACTING_INSULIN_VIAL(
        displayNameRes = R.string.fast_acting_insulin_vial,
        iconRes = shared.drawable.fast_acting_insulin_vial_icon_vector
    ),
    SLOW_ACTING_INSULIN_CARTRIDGE(
        displayNameRes = R.string.slow_acting_insulin_cartridge,
        iconRes = shared.drawable.slow_acting_insulin_cartridge_icon_vector
    ),
    SLOW_ACTING_INSULIN_SYRINGE(
        displayNameRes = R.string.slow_acting_insulin_syringe,
        iconRes = shared.drawable.slow_acting_insulin_syringe_icon_vector
    ),
    SLOW_ACTING_INSULIN_VIAL(
        displayNameRes = R.string.slow_acting_insulin_vial,
        iconRes = shared.drawable.slow_acting_insulin_vial_icon_vector
    ),
    GLUCAGON_SYRINGE(
        displayNameRes = R.string.glucagon_syringe,
        iconRes = shared.drawable.syringe_icon_vector
    ),
    GLUCAGON_SPRAY(
        displayNameRes = R.string.glucagon_spray,
        iconRes = shared.drawable.nasal_spray_icon_vector
    ),

    B_KETONE_TEST_STRIP(
        displayNameRes = R.string.ketone_test_strip,
        iconRes = shared.drawable.b_ketone_test_icon_vector
    ),

    BLOOD_GLUCOSE_TEST_STRIP(
        displayNameRes = R.string.glucose_test_strip,
        iconRes = shared.drawable.glucose_test_icon_vector
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}
