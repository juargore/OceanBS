package com.glass.oceanbs.models

data class History(
    val id: Int,
    val title: String,
    val subtitle: String,
    val hexColor: String,
    val unityCode: String,
    val date: String
)

data class HistorySpinner(
    val id: Int,
    val name: String
)