package com.glass.oceanbs.models

data class ItemSummary(
    val id: Int,
    val title: String,
    val subtitle: String,
    val hexColor: String,
    val urlImgLeft: String,
    val isDone: Boolean,
    val percentage: String,
    val urlImgDone: String
)
