package com.diabdata.models

import android.content.Context
import androidx.annotation.StringRes
import com.diabdata.R

enum class AddableType(
    val tableName: String, @param:StringRes val displayNameRes: Int
) {
    WEIGHT("weight_entries", R.string.addable_weight),
    HBA1C("hba1c_entries", R.string.addable_hba1c),
    APPOINTMENT("appointments", R.string.addable_appointment),
    TREATMENT("treatments", R.string.addable_treatment),
    IMPORTANT_DATE("important_date_entries", R.string.addable_important_date);

    fun getDisplayName(context: Context): String =
        context.getString(displayNameRes)
}
