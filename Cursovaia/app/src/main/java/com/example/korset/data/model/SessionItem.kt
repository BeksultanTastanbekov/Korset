package com.example.korset.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class SessionItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val date: Date,
    val time: String,
    val cinemaName: String,
    val hall: String,
    val format: String,
    val priceAdult: Int,
    val priceStudent: Int?,
    val priceChild: Int?
) : Parcelable