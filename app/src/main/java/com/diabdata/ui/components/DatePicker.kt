import android.app.DatePickerDialog
import android.view.ContextThemeWrapper
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.diabdata.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DateSelector(
    initialDate: LocalDate = LocalDate.now(),
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    var selectedDate by remember { mutableStateOf(initialDate) }

    val year = selectedDate.year
    val month = selectedDate.monthValue - 1
    val day = selectedDate.dayOfMonth

    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    val themedContext = ContextThemeWrapper(context, R.style.Theme_DiabData)

    // Get default text field height and padding
    val paddingValues = OutlinedTextFieldDefaults.contentPadding()

    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp), // No outer padding, leave layout control to parent
        onClick = {
            DatePickerDialog(
                themedContext,
                { _: DatePicker, selYear: Int, selMonth: Int, selDay: Int ->
                    val newDate = LocalDate.of(selYear, selMonth + 1, selDay)
                    selectedDate = newDate
                    onDateSelected(newDate)
                },
                year,
                month,
                day
            ).show()
        },
        shape = OutlinedTextFieldDefaults.shape, // same shape
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.surfaceTint.copy(0.5f)
        ),
        contentPadding = paddingValues,
        elevation = null
    ) {
        Text(
            text = selectedDate.format(formatter),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
