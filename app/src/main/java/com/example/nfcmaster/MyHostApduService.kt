package com.example.nfcmaster

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class MyHostApduService :  HostApduService(){
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val response = "Hello from NFC!".toByteArray()
        Log.d("MyHostApduService", "Received APDU: " )
        return response
    }

    override fun onDeactivated(reason: Int) {
        // Handle deactivation if needed
        Log.d("MyHostApduService", "Deactivated: ")
    }
}