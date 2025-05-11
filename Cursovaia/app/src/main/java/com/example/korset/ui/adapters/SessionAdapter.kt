package com.example.korset.ui.adapters // Убедись, что пакет правильный

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.data.model.SessionItem
import com.example.korset.databinding.ListItemSessionBinding // ViewBinding

class SessionAdapter(
    private val onSessionClick: (SessionItem) -> Unit // Лямбда для клика
) : ListAdapter<SessionItem, SessionAdapter.SessionViewHolder>(SessionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ListItemSessionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SessionViewHolder(binding, onSessionClick)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SessionViewHolder(
        private val binding: ListItemSessionBinding,
        private val onSessionClick: (SessionItem) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentSession: SessionItem? = null

        init {
            // Клик на всю карточку
            binding.root.setOnClickListener {
                currentSession?.let { session ->
                    onSessionClick(session)
                }
            }
        }

        fun bind(item: SessionItem) {
            currentSession = item
            binding.tvSessionTime.text = item.time
            binding.tvCinemaName.text = item.cinemaName
            // Формируем строку для зала и формата
            binding.tvHallAndFormat.text = "${item.hall} • ${item.format}"
            // Формируем строки цен (добавляем валюту)
            binding.tvPriceAdult.text = "${item.priceAdult} ₸"
            // Показываем вторую цену, если она есть и отличается от взрослой
            val otherPrice = item.priceStudent ?: item.priceChild
            if (otherPrice != null && otherPrice != item.priceAdult) {
                binding.tvPriceOther.text = "${otherPrice} ₸"
                binding.tvPriceOther.visibility = View.VISIBLE
            } else {
                binding.tvPriceOther.visibility = View.GONE // Скрываем, если нет
            }
        }
    }

    object SessionDiffCallback : DiffUtil.ItemCallback<SessionItem>() {
        override fun areItemsTheSame(oldItem: SessionItem, newItem: SessionItem): Boolean {
            return oldItem.id == newItem.id // Сравниваем по уникальному ID
        }
        override fun areContentsTheSame(oldItem: SessionItem, newItem: SessionItem): Boolean {
            return oldItem == newItem // Сравниваем все поля
        }
    }
}