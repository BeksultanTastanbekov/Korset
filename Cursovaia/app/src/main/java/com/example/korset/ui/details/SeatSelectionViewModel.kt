package com.example.korset.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.korset.data.model.TicketType
import com.example.korset.util.Event // Импортируем наш Event

// ViewModel для обмена данными между SeatSelection и TicketTypeBottomSheet
class SeatSelectionViewModel : ViewModel() {

    // LiveData для хранения ВЫБРАННОГО типа билета
    private val _selectedTicketTypeEvent = MutableLiveData<Event<TicketType>>()
    val selectedTicketTypeEvent: LiveData<Event<TicketType>> = _selectedTicketTypeEvent

    // Функция, которую вызовет BottomSheet
    fun selectTicketType(ticketType: TicketType) {
        _selectedTicketTypeEvent.value = Event(ticketType) // Оборачиваем в Event
        println("SeatSelectionViewModel: Ticket type selected: ${ticketType.name}")
    }
}