// Cinema.kt
package com.example.korset.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Cinema(
    val id: Int,
    val name: String,
    val address: String,
    val logoUrl: String? = null // URL для логотипа, если будете добавлять
) : Parcelable