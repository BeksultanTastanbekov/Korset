package com.example.korset.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Делаем Parcelable для возможной передачи
data class TicketType(
    val name: String, // "Взрослый", "Студенческий", "Детский"
    val description: String?, // Доп. описание (например, "с 5 до 16 лет")
    val price: Int, // Цена для этого типа
    var isSelected: Boolean = false // Флаг для RadioGroup/Адаптера
) : Parcelable