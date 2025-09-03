package com.diabdata.ui.components.addDataPopup.iaSummary

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.diabdata.R
import com.diabdata.data.DataViewModel
import com.diabdata.models.classes.HealthSummary
import com.diabdata.utils.SvgIcon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale


@Composable
fun IaSummaryPopup(
    onDismiss: () -> Unit,
    dataViewModel: DataViewModel
) {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
        .withLocale(Locale.getDefault())
    val oneYearAgo = today.minusYears(1)
    val context = LocalContext.current

    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    prefs.getString("ai_api_key", "").toString()

    val healthSummary by dataViewModel.getHealthSummary(oneYearAgo, today)
        .collectAsState(initial = HealthSummary(emptyList(), emptyList(), emptyList()))

    var summaryText by remember { mutableStateOf("Chargement...") }

    stringResource(R.string.ai_prompt_summary_system_prompt)
    dataViewModel.generateIaSummary(healthSummary, context)


//    LaunchedEffect(healthSummary) {
//        if (healthSummary.weightData.isNotEmpty() ||
//            healthSummary.hba1cData.isNotEmpty() ||
//            healthSummary.appointments.isNotEmpty()
//        ) {
//            summaryText = generateGeminiSummary(systemPrompt, prompt, apiKey)
//
//        }
//    }

    LaunchedEffect(healthSummary) {
        if (healthSummary.weightData.isNotEmpty() ||
            healthSummary.hba1cData.isNotEmpty() ||
            healthSummary.appointments.isNotEmpty()
        ) {
            try {
                val llm = initializeGemma(context)

                val fullPrompt = buildGemmaPrompt(context, healthSummary, formatter)

                Log.d("IaSummaryPopup", "Full prompt:\n$fullPrompt")

                val buffer = StringBuilder()

                llm.generateResponseAsync(fullPrompt) { partialResult, done ->
                    if (partialResult.isNotBlank()) {
                        buffer.append(partialResult)
                        summaryText = buffer.toString()
                    }
                    if (done) {
                        summaryText = buffer.toString()
                        Log.d("IaSummaryPopup", "Response:\n$summaryText")
                    }
                }
            } catch (e: Exception) {
                summaryText = "Erreur lors de la génération du résumé."
                e.printStackTrace()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            SvgIcon(
                resId = R.drawable.ai_icon_vector,
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = stringResource(R.string.add_data_fab_ai_insights_popup_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = summaryText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.confirm_button_text),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}