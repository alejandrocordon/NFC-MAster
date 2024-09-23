package com.example.nfcmaster

import com.example.nfcmaster.domain.entities.TagInfo

interface ReadTagUseCase {
    fun readTag(): TagInfo?
}
