package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.R // Для доступа к ресурсам
import com.example.korset.data.model.Seat
import com.example.korset.data.model.SeatType
import com.example.korset.databinding.ListItemSeatBinding // ViewBinding для макета места

// Адаптер для ОДНОГО МЕСТА
class SeatAdapter(
    private val onSeatClick: (Seat) -> Unit
// Теперь передаем SeatDiffCallback ИЗ companion object
) : ListAdapter<Seat, SeatAdapter.SeatViewHolder>(SeatDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        // Используем binding для list_item_seat.xml
        val binding = ListItemSeatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeatViewHolder(binding, onSeatClick)
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SeatViewHolder(
        private val binding: ListItemSeatBinding,
        private val onSeatClick: (Seat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentSeat: Seat? = null

        init {
            binding.root.setOnClickListener {
                currentSeat?.let { seat ->
                    // Разрешаем клик только если место не пустое и не занято
                    if (seat.type != SeatType.EMPTY && !seat.isOccupied) {
                        onSeatClick(seat)
                    }
                }
            }
        }

        // --- ИЗМЕНЯЕМ bind ---
        fun bind(seat: Seat) { // Параметр seat все еще нужен для ID, номера, типа
            currentSeat = seat
            val context = binding.root.context

            if (seat.type == SeatType.EMPTY) {
                binding.root.visibility = View.INVISIBLE
                binding.root.isClickable = false
            } else {
                binding.root.visibility = View.VISIBLE
                binding.tvSeatNumber.text = seat.number.toString()
                binding.root.isClickable = !seat.isOccupied

                // --- ПОЛУЧАЕМ АКТУАЛЬНОЕ СОСТОЯНИЕ ВЫБОРА ---
                // Найдем этот seat ID в списке выбранных во фрагменте
                // Это требует доступа к selectedSeats из фрагмента. Передадим его.
                // Пока сделаем заглушку - будем использовать isSelected из объекта seat,
                // но добавим лог, чтобы проверить его значение.
                val isCurrentlySelected = seat.isSelected // << ПОКА ОСТАВИМ ТАК, НО ПРОВЕРИМ
                println("Binding seat ${seat.row}-${seat.number}: isSelected from object = $isCurrentlySelected")
                // --------------------------------------------

                val backgroundDrawable: Int
                val textColor: Int
                when {
                    // Используем isCurrentlySelected
                    isCurrentlySelected -> {
                        backgroundDrawable = R.drawable.seat_background_selected
                        textColor = ContextCompat.getColor(context, R.color.white)
                    }
                    seat.isOccupied -> {
                        backgroundDrawable = R.drawable.seat_background_occupied
                        textColor = ContextCompat.getColor(context, R.color.seat_occupied_text)
                    }
                    seat.type == SeatType.VIP -> {
                        backgroundDrawable = R.drawable.seat_background_vip
                        textColor = ContextCompat.getColor(context, R.color.white)
                    }
                    else -> { // STANDARD
                        backgroundDrawable = R.drawable.seat_background_available
                        textColor = ContextCompat.getColor(context, R.color.white)
                    }
                }
                binding.seatContainer.background = ContextCompat.getDrawable(context, backgroundDrawable)
                binding.tvSeatNumber.setTextColor(textColor)
            }
        }
        // --- КОНЕЦ bind ---
    }

    // --- ДОБАВЛЯЕМ COMPANION OBJECT С DIFFUTIL ---
    companion object {
        val SeatDiffCallback = object : DiffUtil.ItemCallback<Seat>() {
            override fun areItemsTheSame(oldItem: Seat, newItem: Seat): Boolean {
                return oldItem.id == newItem.id
            }
            override fun areContentsTheSame(oldItem: Seat, newItem: Seat): Boolean {
                return oldItem == newItem // Сравниваем все поля data class
            }
        }
    }
    // ---------------------------------------------
}