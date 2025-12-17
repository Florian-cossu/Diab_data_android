package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.shared.R

enum class TreatmentType(
    @param:StringRes val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val iconFilledRes: Int
) {
    FAST_ACTING_INSULIN_CARTRIDGE(
        displayNameRes = R.string.medication_fast_acting_insulin_cartridge,
        iconRes = R.drawable.fast_acting_insulin_cartridge_icon_vector,
        iconFilledRes = R.drawable.fast_acting_insulin_cartridge_filled_icon_vector
    ),
    FAST_ACTING_INSULIN_SYRINGE(
        displayNameRes = R.string.medication_fast_acting_insulin_syringe,
        iconRes = R.drawable.fast_acting_insulin_syringe_icon_vector,
        iconFilledRes = R.drawable.fast_acting_insulin_syringe_filled_icon_vector
    ),
    FAST_ACTING_INSULIN_VIAL(
        displayNameRes = R.string.medication_fast_acting_insulin_vial,
        iconRes = R.drawable.fast_acting_insulin_vial_icon_vector,
        iconFilledRes = R.drawable.fast_acting_insulin_vial_filled_icon_vector
    ),
    SLOW_ACTING_INSULIN_CARTRIDGE(
        displayNameRes = R.string.medication_slow_acting_insulin_cartridge,
        iconRes = R.drawable.slow_acting_insulin_cartridge_icon_vector,
        iconFilledRes = R.drawable.slow_acting_insulin_cartridge_filled_icon_vector
    ),
    SLOW_ACTING_INSULIN_SYRINGE(
        displayNameRes = R.string.medication_slow_acting_insulin_syringe,
        iconRes = R.drawable.slow_acting_insulin_syringe_icon_vector,
        iconFilledRes = R.drawable.slow_acting_insulin_syringe_filled_icon_vector
    ),
    SLOW_ACTING_INSULIN_VIAL(
        displayNameRes = R.string.medication_slow_acting_insulin_vial,
        iconRes = R.drawable.slow_acting_insulin_vial_icon_vector,
        iconFilledRes = R.drawable.slow_acting_insulin_vial_filled_icon_vector
    ),
    GLUCAGON_SYRINGE(
        displayNameRes = R.string.medication_glucagon_syringe,
        iconRes = R.drawable.syringe_icon_vector,
        iconFilledRes = R.drawable.syringe_filled_icon_vector
    ),
    GLUCAGON_SPRAY(
        displayNameRes = R.string.medication_glucagon_spray,
        iconRes = R.drawable.nasal_spray_icon_vector,
        iconFilledRes = R.drawable.nasal_spray_filled_icon_vector
    ),

    B_KETONE_TEST_STRIP(
        displayNameRes = R.string.medication_ketone_test_strip,
        iconRes = R.drawable.b_ketone_test_icon_vector,
        iconFilledRes = R.drawable.b_ketone_test_filled_icon_vector
    ),

    BLOOD_GLUCOSE_TEST_STRIP(
        displayNameRes = R.string.medication_glucose_test_strip,
        iconRes = R.drawable.glucose_test_icon_vector,
        iconFilledRes = R.drawable.glucose_test_filled_icon_vector
    ),

    UNKNOWN(
        displayNameRes = R.string.medication_unknown,
        iconRes = R.drawable.syringe_icon_vector,
        iconFilledRes = R.drawable.syringe_filled_icon_vector
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}