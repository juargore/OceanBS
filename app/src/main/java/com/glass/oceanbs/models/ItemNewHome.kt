package com.glass.oceanbs.models

data class ItemNewHome(
    val id: Int,
    val title: String,
    val subtitle: String,
    val hexColor: String,
    val openScreen: Boolean = false,
    val url: String? = null
)
