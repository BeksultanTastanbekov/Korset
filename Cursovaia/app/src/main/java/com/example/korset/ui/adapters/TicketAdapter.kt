package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.local.TicketEntity // Твоя модель билета из БД
import com.example.korset.data.network.ApiService // Для URL постера
import com.example.korset.databinding.ListItemTicketBinding // ViewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketAdapter(
    private val onItemClick: (TicketEntity) -> Unit
) : ListAdapter<TicketEntity, TicketAdapter.TicketViewHolder>(TicketDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val binding = ListItemTicketBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TicketViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TicketViewHolder(
        private val binding: ListItemTicketBinding,
        private val onItemClick: (TicketEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentTicket: TicketEntity? = null
        // Форматы для отображения даты и времени
        private val outputDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        init {
            binding.root.setOnClickListener {
                currentTicket?.let { ticket ->
                    onItemClick(ticket)
                }
            }
        }

        fun bind(ticket: TicketEntity) {
            currentTicket = ticket

            binding.tvTicketMovieTitle.text = ticket.movieTitle ?: "Фильм"
            binding.tvTicketCinemaInfo.text = "${ticket.cinemaName} • ${ticket.hallName}" // Совмещаем кинотеатр и зал

            // Форматируем дату и время
            val sessionDate = Date(ticket.sessionTimestamp) // Преобразуем Long в Date
            val formattedDate = outputDateFormat.format(sessionDate)
            binding.tvTicketSessionDatetime.text = "$formattedDate • ${ticket.sessionTime}"

            // Загружаем постер
            val posterUrl = ticket.moviePosterPath?.let { ApiService.IMAGE_BASE_URL + ApiService.POSTER_SIZE_W500 + it }
            Glide.with(binding.ivTicketMoviePoster.context)
                .load(posterUrl)
                .placeholder(R.drawable.logo_icon) // Заглушка
                .error(R.drawable.logo_icon)       // При ошибке
                .centerCrop()
                .into(binding.ivTicketMoviePoster)
        }
    }

    object TicketDiffCallback : DiffUtil.ItemCallback<TicketEntity>() {
        override fun areItemsTheSame(oldItem: TicketEntity, newItem: TicketEntity): Boolean {
            return oldItem.id == newItem.id // Сравниваем по ID из БД
        }
        override fun areContentsTheSame(oldItem: TicketEntity, newItem: TicketEntity): Boolean {
            return oldItem == newItem // Сравнение всех полей data class
        }
    }
}