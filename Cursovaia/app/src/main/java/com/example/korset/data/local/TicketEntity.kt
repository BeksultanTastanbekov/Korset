package com.example.korset.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.korset.data.model.Seat // Импорт модели Seat

// Определяем таблицу tickets в базе данных
@Entity(tableName = "tickets")
data class TicketEntity(
    // Уникальный ID билета, генерируется автоматически
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // Информация о фильме (из аргументов OrderConfirmationFragment)
    val movieId: Int,
    val movieTitle: String?,
    val moviePosterPath: String?,

    // Информация о сеансе (из аргументов OrderConfirmationFragment)
    val cinemaName: String,
    val hallName: String, // Название зала
    val sessionTime: String, // Время сеанса (например, "10:30")
    // Добавь дату сеанса, если она есть в SessionItem!
    // val sessionDate: Long?, // Дату лучше хранить как Long (timestamp)
    val sessionTimestamp: Long,

    // Информация о местах (из аргументов OrderConfirmationFragment)
    // Сохраним как строку "Ряд X Место Y, Ряд A Место B"
    val seatsInfo: String,

    // Информация о типе билета и цене (из аргументов OrderConfirmationFragment)
    val ticketTypeName: String,
    val ticketPrice: Int, // Цена одного билета этого типа
    val ticketCount: Int, // Количество купленных билетов
    val totalPrice: Int, // Общая стоимость

    // Дата и время покупки билета
    val purchaseTimestamp: Long = System.currentTimeMillis() // Текущее время в мс
)

// Вспомогательная функция для преобразования списка мест в строку
fun List<Seat>.toSeatsString(): String {
    return this.joinToString { "Р${it.row} М${it.number}" }
}