package com.example.korset.ui.details // ИЛИ com.example.korset.ui.booking, если ты его туда переместил

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.korset.KorsetApp
import com.example.korset.data.local.TicketDao
import com.example.korset.data.local.TicketEntity
import com.example.korset.util.Event // Убедись, что импорт правильный
import kotlinx.coroutines.launch
import java.lang.Exception

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val ticketDao: TicketDao

    // --- ЭТИ ДВЕ СТРОКИ КРИТИЧНЫ ---
    private val _saveStatus = MutableLiveData<Event<Boolean>>() // Приватная изменяемая
    val saveStatus: LiveData<Event<Boolean>> = _saveStatus    // Публичная неизменяемая
    // ---------------------------------

    // LiveData для загрузки билетов (для экрана "Мои билеты")
    private val _activeTickets = MutableLiveData<List<TicketEntity>>()
    val activeTickets: LiveData<List<TicketEntity>> = _activeTickets

    private val _historyTickets = MutableLiveData<List<TicketEntity>>()
    val historyTickets: LiveData<List<TicketEntity>> = _historyTickets

    init {
        val database = (application as KorsetApp).database
        ticketDao = database.ticketDao()
    }

    fun saveTicket(ticket: TicketEntity) {
        viewModelScope.launch {
            try {
                ticketDao.insertTicket(ticket)
                println("BookingViewModel: Ticket saved successfully. Details: $ticket")
                _saveStatus.value = Event(true)
            } catch (e: Exception) {
                println("BookingViewModel: Error saving ticket: ${e.message}")
                _saveStatus.value = Event(false)
            }
        }
    }

    fun loadActiveTickets() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            ticketDao.getActiveTicketsFlow(currentTime).collect { tickets ->
                _activeTickets.value = tickets
                println("BookingViewModel: Loaded ${tickets.size} active tickets.")
            }
        }
    }

    fun loadHistoryTickets() {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            ticketDao.getHistoryTicketsFlow(currentTime).collect { tickets ->
                _historyTickets.value = tickets
                println("BookingViewModel: Loaded ${tickets.size} history tickets.")
            }
        }
    }
}