package com.example.korset.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow // Для реактивного получения данных

@Dao
interface TicketDao {

    // Вставить один билет. Если билет с таким ID уже есть, он будет заменен.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: TicketEntity) // suspend - для корутин

    // Для простоты, пока один метод для всех, отсортированных по дате СЕАНСА (сначала будущие)
    @Query("SELECT * FROM tickets ORDER BY sessionTimestamp DESC, purchaseTimestamp DESC")
    fun getAllTicketsSortedBySessionFlow(): Flow<List<TicketEntity>>

    // Отдельно для активных (где время сеанса > текущего)
    @Query("SELECT * FROM tickets WHERE sessionTimestamp >= :currentTimestamp ORDER BY sessionTimestamp ASC")
    fun getActiveTicketsFlow(currentTimestamp: Long): Flow<List<TicketEntity>>

    // Отдельно для истории (где время сеанса < текущего)
    @Query("SELECT * FROM tickets WHERE sessionTimestamp < :currentTimestamp ORDER BY sessionTimestamp DESC")
    fun getHistoryTicketsFlow(currentTimestamp: Long): Flow<List<TicketEntity>>

    // TODO: getTicketById, deleteTicketById

    // TODO: Добавить методы для получения активных/прошедших билетов, если добавишь дату сеанса
    // @Query("SELECT * FROM tickets WHERE sessionDate >= :currentTimestamp ORDER BY sessionDate ASC")
    // fun getActiveTicketsFlow(currentTimestamp: Long): Flow<List<TicketEntity>>
    //
    // @Query("SELECT * FROM tickets WHERE sessionDate < :currentTimestamp ORDER BY sessionDate DESC")
    // fun getHistoryTicketsFlow(currentTimestamp: Long): Flow<List<TicketEntity>>

    // TODO: Добавить метод для получения билета по ID (для экрана деталей билета)
    // @Query("SELECT * FROM tickets WHERE id = :ticketId")
    // suspend fun getTicketById(ticketId: Int): TicketEntity?

    // TODO: Добавить метод для удаления билета (если нужно)
    // @Query("DELETE FROM tickets WHERE id = :ticketId")
    // suspend fun deleteTicketById(ticketId: Int)
}