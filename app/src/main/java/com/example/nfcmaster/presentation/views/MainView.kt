package com.example.nfcmaster.presentation.views

import com.example.nfcmaster.domain.entities.TagInfo

interface MainView {
    fun showTagInfo(tagInfo: TagInfo)
    fun showError(message: String)
    fun updateNfcStatus(isSupported: Boolean, isEnabled: Boolean)
}
