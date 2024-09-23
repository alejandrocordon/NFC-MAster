package com.example.nfcmaster


import android.nfc.Tag
import com.example.nfcmaster.domain.entities.TagInfo

interface NFCRepository {
    fun getTagInfo(tag: Tag): TagInfo
}
