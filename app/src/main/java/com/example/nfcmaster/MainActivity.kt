package com.example.nfcmaster

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nfcmaster.ui.theme.NFCMasterTheme

//Doc: https://developer.android.com/develop/connectivity/nfc/hce?hl=es-419
//Supported NFC cards and protocols APDU
//ISO/IEC 14443-4 (ISO-DEP): Es un estándar utilizado para la comunicación de tarjetas de proximidad, como las tarjetas de crédito y transporte. HCE puede emular este tipo de tarjetas para interactuar con lectores NFC compatibles.
//NFC-A y NFC-B: Son subtipos dentro del estándar ISO/IEC 14443, utilizados en diferentes regiones del mundo.


/**
 * La mayoría de los dispositivos Android no pueden actuar como etiquetas NFC pasivas sin usar HCE (Host Card Emulation), y HCE está limitado a emular tarjetas específicas (como tarjetas de crédito).
 * Emitir una etiqueta NFC con un mensaje personalizado puede no ser posible.
 * --------------------------------------------------------------------------
 *
 * Emitir NFC es una funcionalidad limitada y depende del hardware del dispositivo.
 *
 * Android no está diseñado para replicar completamente el comportamiento de los tags pasivos como los que se encuentran en etiquetas físicas NFC. En su lugar, Android soporta HCE (Host-based Card Emulation) para emular tarjetas de proximidad activas.
 *
 * Está limitado por Android y el hardware del dispositivo. No todos los dispositivos Android pueden emitir etiquetas NFC.
 *
 */


class MainActivity : ComponentActivity() {
    private var nfcAdapter: NfcAdapter? = null
    private lateinit var pendingIntent: PendingIntent
    private lateinit var intentFiltersArray: Array<IntentFilter>
    private lateinit var techListsArray: Array<Array<String>>
    private var ndefMessageToWrite: NdefMessage? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NFCMasterTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                    NFCAppScreen()
                }
            }

        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC no está disponible en este dispositivo", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val ndef = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        try {
            ndef.addDataType("*/*")
        } catch (e: IntentFilter.MalformedMimeTypeException) {
            throw RuntimeException("Error al agregar tipo MIME", e)
        }
        intentFiltersArray = arrayOf(ndef)
        techListsArray = arrayOf(arrayOf(Ndef::class.java.name, NdefFormatable::class.java.name))

    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            tag?.let {
                if (ndefMessageToWrite != null) {
                    writeToTag(it, ndefMessageToWrite!!)
                } else {
                    readFromTag(it)
                }
            }
        }
    }

    private fun readFromTag(tag: Tag) {
        val ndef = Ndef.get(tag)
        ndef?.connect()
        val ndefMessage = ndef?.ndefMessage
        ndef?.close()
        ndefMessage?.let {
            for (record in it.records) {
                val payload = String(record.payload)
                Toast.makeText(this, "Leído del tag: $payload", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun writeToTag(tag: Tag, message: NdefMessage) {
        try {
            val ndef = Ndef.get(tag)
            ndef?.connect()
            if (ndef != null) {
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(message)
                    Toast.makeText(this, "Tag escrito correctamente", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Tag no es escribible", Toast.LENGTH_LONG).show()
                }
                ndef.close()
            } else {
                val formatable = NdefFormatable.get(tag)
                if (formatable != null) {
                    formatable.connect()
                    formatable.format(message)
                    Toast.makeText(this, "Tag formateado y escrito correctamente", Toast.LENGTH_LONG).show()
                    formatable.close()
                } else {
                    Toast.makeText(this, "Tag no soporta NDEF", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al escribir el tag NFC", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNdefMessage(message: String): NdefMessage {
        val ndefRecord = NdefRecord.createTextRecord("en", message)
        return NdefMessage(arrayOf(ndefRecord))
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NFCMasterTheme {
        Greeting("Android")
    }
}

@Composable
fun NFCAppScreen(onWriteNfc: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Ingrese texto para escribir en NFC") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onWriteNfc(text) }) {
            Text("Escribir en Tag NFC")
        }
    }
}
@Composable
fun NFCAppScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Your device is ready to act as an NFC tag.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}