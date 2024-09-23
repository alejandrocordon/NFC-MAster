package com.example.nfcmaster.data

import android.nfc.Tag
import android.nfc.tech.*
import android.util.Log
import com.example.nfcmaster.NFCRepository
import com.example.nfcmaster.domain.entities.TagInfo

class NFCRepositoryImpl : NFCRepository {

    override fun getTagInfo(tag: Tag): TagInfo {
        Log.d("NFCRepositoryImpl", "getTagInfo called")
        val serialNumber = tag.id.joinToString("") { "%02x".format(it) }
        Log.d("NFCRepositoryImpl", "Serial Number: $serialNumber")
        val techList = tag.techList.toList()
        Log.d("NFCRepositoryImpl", "Tech List: $techList")
        val additionalData = mutableMapOf<String, String>()

        if (techList.contains(Ndef::class.java.name)) {
            val ndef = Ndef.get(tag)
            ndef?.let {
                additionalData["Type"] = it.type
                additionalData["Max Size"] = "${it.maxSize} bytes"
                additionalData["Writable"] = it.isWritable.toString()
                Log.d("NFCRepositoryImpl", "NDEF Data: $additionalData")
            }
        }

        // Puedes añadir más extracciones de información según las tecnologías disponibles
        // y agregar logs correspondientes

        return TagInfo(
            serialNumber = serialNumber,
            techList = techList,
            additionalData = additionalData
        )
    }
}
