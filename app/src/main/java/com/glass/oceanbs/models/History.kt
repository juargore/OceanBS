package com.glass.oceanbs.models

data class History(
    val id: Int,
    val title: String,
    val subtitle: String,
    val hexColor: String,
    val unityCode: String,
    val creationDate: String,
    val estimatedDate: String,

    val additionalInfo: String,
    val progress: Int,
    val phase: Int,
    val photo1: String,
    val photo2: String,
    val photo3: String
)

data class HistorySpinner(
    val id: Int,
    val name: String
)