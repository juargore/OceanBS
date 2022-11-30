package com.glass.oceanbs.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Unity(
    val id: String,
    val code: String,
    val name: String
): Parcelable
