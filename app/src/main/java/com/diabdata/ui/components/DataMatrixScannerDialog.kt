package com.diabdata.ui.components

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.diabdata.ui.components.latestMeasurements.CameraPreview
import com.diabdata.utils.MedicationInfo
import com.diabdata.utils.parseGS1

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun DataMatrixScannerDialog(
    onDismiss: () -> Unit,
    onResult: (MedicationInfo) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(Color.Black, RoundedCornerShape(16.dp))
        ) {
            CameraPreview(
                modifier = Modifier.matchParentSize(),
                onBarcodeDetected = { rawValue ->
                    rawValue.let { value ->
                        Log.d("Scanner", "Value=${rawValue}")
                        val parsed = parseGS1(rawValue)
                        onResult(parsed)
                        onDismiss()
                    }
                }
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(220.dp)
                    .border(
                        width = 3.dp,
                        color = Color.White.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(24.dp)
                    )
            )
        }
    }
}
