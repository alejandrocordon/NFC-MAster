package com.example.nfcmaster

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var nfcAdapter: NfcAdapter? = null
    private lateinit var textViewSerialNumber: TextView
    private lateinit var buttonRead: Button

    private var isReading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        setContentView(R.layout.activity_main)

        textViewSerialNumber = findViewById(R.id.textViewSerialNumber)
        buttonRead = findViewById(R.id.buttonRead)

        // Obtener el NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Log.e(TAG, "El dispositivo no soporta NFC")
            textViewSerialNumber.text = "El dispositivo no soporta NFC"
            buttonRead.isEnabled = false
        } else if (!nfcAdapter!!.isEnabled) {
            Log.e(TAG, "NFC está desactivado")
            textViewSerialNumber.text = "NFC está desactivado"
            val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            startActivity(intent)
        }

        buttonRead.setOnClickListener {
            Log.d(TAG, "Botón Leer presionado")
            isReading = true
            textViewSerialNumber.text = "Acerque un tag NFC al dispositivo"
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        val techList = arrayOf(arrayOf(NfcA::class.java.name))
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, filters, techList)
        Log.d(TAG, "Foreground dispatch activado")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        nfcAdapter?.disableForegroundDispatch(this)
        Log.d(TAG, "Foreground dispatch desactivado")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent called with action: ${intent.action}")

        if (isReading && NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                Log.d(TAG, "Tag detectado")
                readFromTag(tag)
            } else {
                Log.e(TAG, "No se pudo obtener el Tag")
                textViewSerialNumber.text = "Error: No se pudo obtener el Tag"
            }
            isReading = false
        }
    }

    private fun readFromTag(tag: Tag) {
        // Obtener el número de serie (ID) del tag
        val id = tag.id
        val serialNumber = id.joinToString("") { "%02X".format(it) }
        Log.d(TAG, "Número de serie del Tag: $serialNumber")
        textViewSerialNumber.text = "Número de Serie: $serialNumber"

        // Opcional: Leer más información del tag

        // Registrar las tecnologías soportadas por el tag
        val techList = tag.techList
        Log.d(TAG, "Tecnologías soportadas por el Tag:")
        techList.forEach { tech ->
            Log.d(TAG, tech)
        }

        // Si el tag es de tipo NfcA (ISO 14443-3A), puedes obtener más detalles
        if (techList.contains(NfcA::class.java.name)) {
            val nfcATag = NfcA.get(tag)
            try {
                nfcATag.connect()
                val atqa = nfcATag.atqa
                val sak = nfcATag.sak
                Log.d(TAG, "ATQA: ${atqa.joinToString("") { "%02X".format(it) }}")
                Log.d(TAG, "SAK: $sak")
                nfcATag.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error al leer datos adicionales del Tag", e)
            }
        }
    }
}
