package com.diabdata.ui.components.addDataPopup.iaSummary

import android.content.Context
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.classes.HealthSummary
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


fun DataViewModel.generateIaSummary(
    summary: HealthSummary,
    context: Context
): String {
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())

    val weightText = if (summary.weightData.isNotEmpty()) {
        summary.weightData.joinToString(separator = "\n") { "- ${it.date.format(formatter)}: ${it.value} kg" }
    } else context.getString(R.string.ai_no_weight_data)

    val hba1cText = if (summary.hba1cData.isNotEmpty()) {
        summary.hba1cData.joinToString(separator = "\n") { "- ${it.date.format(formatter)}: ${it.value}%" }
    } else context.getString(R.string.ai_no_hba1c_data)

    val appointmentsText = if (summary.appointments.isNotEmpty()) {
        summary.appointments.joinToString(separator = "\n") {
            var notes = ""

            if (it.notes.isNotBlank()) notes = it.notes

            "- ${it.date.format(formatter)} ${it.doctor} (${it.type.name}): $notes"
        }
    } else context.getString(R.string.ai_no_appointments)

    val promptTemplate = context.getString(R.string.ai_prompt_summary_text)

    return String.format(promptTemplate, weightText, hba1cText, appointmentsText)
}

fun buildGemmaPrompt(
    context: Context,
    healthSummary: HealthSummary,
    formatter: DateTimeFormatter
): String {
    val weightSection = if (healthSummary.weightData.isNotEmpty()) {
        healthSummary.weightData.joinToString("\n") {
            "- ${it.date.format(formatter)} : ${it.value} kg"
        }
    } else context.getString(R.string.ai_no_weight_data)

    val hba1cSection = if (healthSummary.hba1cData.isNotEmpty()) {
        healthSummary.hba1cData.joinToString("\n") {
            "- ${it.date.format(formatter)} : ${it.value}%"
        }
    } else context.getString(R.string.ai_no_hba1c_data)

    val appointmentSection = if (healthSummary.appointments.isNotEmpty()) {
        healthSummary.appointments.joinToString("\n") {
            val notesPart = if (!it.notes.isNullOrBlank()) " – Notes: ${it.notes}" else ""
            "- ${it.date.format(formatter)} ${it.doctor} (${it.type.name})$notesPart"
        }
    } else context.getString(R.string.ai_no_appointments)

    val formattedContent = context.getString(
        R.string.ai_local_llm_prompt,
        weightSection,
        hba1cSection,
        appointmentSection
    )

    return """
        <start_of_turn>user
        $formattedContent
        <end_of_turn>
        <start_of_turn>model
    """.trimIndent()
}