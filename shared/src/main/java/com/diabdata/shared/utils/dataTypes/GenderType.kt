package com.diabdata.shared.utils.dataTypes

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.diabdata.shared.R as R

enum class Gender(
    @param:StringRes
    val displayNameRes: Int,
    @param:DrawableRes val iconRes: Int
) {
    MALE(
        R.string.gender_male,
        R.drawable.male_icon_vector
    ),
    FEMALE(
        R.string.gender_female,
        R.drawable.female_icon_vector
    ),
    OTHER(
        R.string.gender_other,
        R.drawable.question_mark_icon_vector
    ),
    PREFER_NOT_TO_SAY(
        R.string.gender_prefer_not_to_say,
        R.drawable.back_hand_icon_vector
    );

    fun displayName(context: Context): String = context.getString(displayNameRes)
}