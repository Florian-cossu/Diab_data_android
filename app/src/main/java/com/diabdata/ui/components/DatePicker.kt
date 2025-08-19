import android.app.DatePickerDialog
import android.view.ContextThemeWrapper
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.diabdata.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

@Composable
fun DateSelector(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val themedContext = ContextThemeWrapper(context, R.style.Theme_DiabData)

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())

    var selectedDate by remember { mutableStateOf(initialDate) }
    var dateText by remember { mutableStateOf(TextFieldValue(selectedDate.format(formatter))) }

    fun openDatePicker() {
        DatePickerDialog(
            themedContext,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val newDate = LocalDate.of(year, month + 1, day)
                selectedDate = newDate
                dateText = TextFieldValue(newDate.format(formatter))
                onDateSelected(newDate)
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    OutlinedTextField(
        value = dateText,
        onValueChange = { newValue ->
            dateText = newValue
            try {
                val parsedDate = LocalDate.parse(newValue.text, formatter)
                selectedDate = parsedDate
                onDateSelected(parsedDate)
            } catch (_: DateTimeParseException) {
                // Ignore parsing errors until valid date is entered
            }
        },
        label = { Text("Date") },
        singleLine = true,
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = "Sélectionner une date",
                modifier = Modifier.clickable { openDatePicker() }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { openDatePicker() }, // Champ entier cliquable
        shape = MaterialTheme.shapes.small
    )
}