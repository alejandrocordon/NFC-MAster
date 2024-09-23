package com.example.nfcmaster.domain.entities

data class TagInfo(
    val serialNumber: String,
    val techList: List<String>,
    val additionalData: Map<String, String>
)
