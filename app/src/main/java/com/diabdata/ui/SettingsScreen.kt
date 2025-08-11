package com.diabdata.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen () {
    Scaffold () { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "Données",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.surfaceTint
            )

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {

                }) {
                    Text("Exporter les données")
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {

                }) {
                    Text("Importer des données")
                }
            }

            Spacer(Modifier.height(8.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {

                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,        // fond rouge
                        contentColor = Color.White          // texte blanc
                    )
                ) {
                    Text("Vider la base de données")
                }
            }
        }
    }
}