package com.example.nfcmaster.presentation.presenters

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.util.Log
import androidx.activity.ComponentActivity
import com.example.nfcmaster.data.NFCRepositoryImpl
import com.example.nfcmaster.presentation.views.MainView

class MainPresenter(private val view: MainView, context: Context) {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
    private val nfcRepository = NFCRepositoryImpl()

    init {
        Log.d("MainPresenter", "nfcAdapter is $nfcAdapter")
    }

    fun enableForegroundDispatch(activity: ComponentActivity) {
        Log.d("MainPresenter", "enableForegroundDispatch called")
        val intent = Intent(activity.applicationContext, activity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
        val filters = arrayOf(IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED))
        nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, filters, null)
    }

    fun disableForegroundDispatch(activity: ComponentActivity) {
        Log.d("MainPresenter", "disableForegroundDispatch called")
        nfcAdapter?.disableForegroundDispatch(activity)
    }

    fun handleIntent(intent: Intent) {
        Log.d("MainPresenter", "handleIntent called with action: ${intent.action}")

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.also { rawMessages ->
                val messages: List<NdefMessage> = rawMessages.map { it as NdefMessage }
                // Process the messages array.
                Log.d("MainActivity", "NDEF Messages: $messages")
            }
        }

        val action = intent.action

        if (action == NfcAdapter.ACTION_TECH_DISCOVERED || action == NfcAdapter.ACTION_TAG_DISCOVERED) {
            Log.d("MainPresenter", "NFC tag detected")
            val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
            tag?.let {
                Log.d("MainPresenter", "NFC tag detected")
                val tagInfo = nfcRepository.getTagInfo(it)
                view.showTagInfo(tagInfo)
            } ?: run {
                Log.e("MainPresenter", "No NFC tag found in intent")
                view.showError("No se pudo leer el tag NFC")
            }
        }
    }

    fun checkNfcAvailability() {
        Log.d("MainPresenter", "checkNfcAvailability called")
        val isSupported = nfcAdapter != null
        val isEnabled = nfcAdapter?.isEnabled == true

        view.updateNfcStatus(isSupported, isEnabled)

        if (isSupported && isEnabled) {
            Log.d("MainPresenter", "NFC is supported and enabled , Esperando a que se acerque un tag NFC")
            // Esperar a que se acerque un tag NFC
        } else {
            Log.e("MainPresenter", "NFC is not available or not enabled")
        }
    }
}
