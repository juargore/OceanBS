package com.ocean.oceanbs.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Unity(
    val id: String,
    val code: String,
    val name: String
): Parcelable
