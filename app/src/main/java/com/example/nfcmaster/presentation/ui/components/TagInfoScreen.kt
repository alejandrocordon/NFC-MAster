package com.example.nfcmaster.presentation.ui.components


import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.nfcmaster.domain.entities.TagInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagInfoScreen(
    tagInfo: TagInfo?,
    isNfcSupported: Boolean,
    isNfcEnabled: Boolean,
    onReadButtonClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Información del Tag NFC") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Button(
                onClick = {
                    Log.d("TagInfoScreen", "Read Button clicked")
                    onReadButtonClick()
                },
                enabled = isNfcSupported && isNfcEnabled
            ) {
                Text(text = "Leer")
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (tagInfo != null) {
                TagInfoContent(tagInfo = tagInfo)
            } else {
                EmptyState(isNfcSupported, isNfcEnabled)
            }
        }
    }
}

@Composable
fun TagInfoContent(tagInfo: TagInfo) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            Text(
                text = "Número de Serie:",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = tagInfo.serialNumber,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tecnologías Soportadas:",
                style = MaterialTheme.typography.headlineLarge
            )
            tagInfo.techList.forEach { tech ->
                Text(
                    text = tech.substringAfterLast('.'),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (tagInfo.additionalData.isNotEmpty()) {
                Text(
                    text = "Información Adicional:",
                    style = MaterialTheme.typography.headlineSmall
                )
                tagInfo.additionalData.forEach { (key, value) ->
                    Text(
                        text = "$key: $value",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(isNfcSupported: Boolean, isNfcEnabled: Boolean) {
    val message = when {
        !isNfcSupported -> "El dispositivo no soporta NFC."
        !isNfcEnabled -> "El NFC está desactivado. Por favor, actívalo."
        else -> "Acerque un tag NFC al dispositivo para leer su información."
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
