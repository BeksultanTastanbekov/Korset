package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.data.model.Seat
import com.example.korset.databinding.ListItemRowBinding // ViewBinding для ряда

// Модель данных для одного ряда
data class RowData(
    val rowNumber: Int,
    val seats: List<Seat> // Список мест ТОЛЬКО для этого ряда
)

// Адаптер для списка РЯДОВ
class RowAdapter(
    private val onSeatClick: (Seat) -> Unit // Лямбда для клика по месту (пробрасываем дальше)
) : ListAdapter<RowData, RowAdapter.RowViewHolder>(RowDiffCallback) {

    // Создаем View Holder для ряда
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowViewHolder {
        val binding = ListItemRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Передаем обработчик клика по месту в конструктор ViewHolder'а
        return RowViewHolder(binding, onSeatClick)
    }

    // Привязываем данные ряда к ViewHolder'у
    override fun onBindViewHolder(holder: RowViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // View Holder для ОДНОГО РЯДА
    class RowViewHolder(
        private val binding: ListItemRowBinding,
        private val onSeatClick: (Seat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val seatAdapter = SeatAdapter(onSeatClick)
        // --- ДОБАВЛЯЕМ ПОЛЕ ДЛЯ ХРАНЕНИЯ ТЕКУЩИХ МЕСТ РЯДА ---
        var currentSeats: List<Seat> = emptyList()
            private set // Делаем сеттер приватным
        // --------------------------------------------------

        init {
            binding.rvSeatsInRow.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = seatAdapter
            }
        }

        fun bind(rowData: RowData) {
            binding.tvRowLabel.text = rowData.rowNumber.toString()
            // Сохраняем текущий список мест
            currentSeats = rowData.seats
            // Передаем список во вложенный адаптер
            seatAdapter.submitList(currentSeats)
        }

        // --- ДОБАВЛЯЕМ МЕТОД ДЛЯ ПОЛУЧЕНИЯ SeatViewHolder ---
        fun getSeatViewHolder(position: Int): SeatAdapter.SeatViewHolder? {
            return binding.rvSeatsInRow.findViewHolderForAdapterPosition(position) as? SeatAdapter.SeatViewHolder
        }
        // -------------------------------------------------
    }

    // DiffUtil для сравнения рядов (например, по номеру)
    // В RowAdapter.kt
    object RowDiffCallback : DiffUtil.ItemCallback<RowData>() {
        override fun areItemsTheSame(oldItem: RowData, newItem: RowData): Boolean {
            return oldItem.rowNumber == newItem.rowNumber
        }
        override fun areContentsTheSame(oldItem: RowData, newItem: RowData): Boolean {
            // Сравниваем сами объекты RowData. Так как это data class,
            // сравнение будет по всем полям (rowNumber и seats).
            // Если список seats изменился (из-за изменения Seat внутри него),
            // объекты RowData будут не равны.
            return oldItem == newItem // <<<=== ИСПРАВЛЕНО
        }
    }
}