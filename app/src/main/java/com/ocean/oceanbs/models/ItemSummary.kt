package com.ocean.oceanbs.models

data class ItemSummary(
    val title: String,
    val subtitle: String,
    val hexColor: String,
    val urlImgLeft: String,
    val isDone: Boolean,
    val percentage: String,
    val urlImgDone: String = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c6/Sign-check-icon.png/800px-Sign-check-icon.png"
)
