package com.example.korset.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
// Модель для представления одного места в зале
data class Seat(
    val id: String = java.util.UUID.randomUUID().toString(), // Уникальный ID для DiffUtil
    val row: Int,           // Номер ряда
    val number: Int,        // Номер места в ряду
    var type: SeatType = SeatType.STANDARD, // Тип места (стандарт, VIP, пустое)
    var isOccupied: Boolean = false, // Занято ли место (например, уже куплено)
    var isSelected: Boolean = false,  // Выбрано ли место пользователем сейчас
    var isFirstInRow: Boolean = false
) : Parcelable

// Перечисление для типов мест (можно расширить)
enum class SeatType {
    EMPTY,      // Пустое место (проход)
    STANDARD,   // Обычное место
    VIP         // VIP место
}