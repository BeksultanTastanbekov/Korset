// CinemaAdapter.kt
package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.model.Cinema
import com.example.korset.databinding.ListItemCinemaBinding

class CinemaAdapter(
    private val onItemClick: (Cinema) -> Unit
) : ListAdapter<Cinema, CinemaAdapter.CinemaViewHolder>(CinemaDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CinemaViewHolder {
        val binding = ListItemCinemaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CinemaViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: CinemaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CinemaViewHolder(
        private val binding: ListItemCinemaBinding,
        private val onItemClick: (Cinema) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentCinema: Cinema? = null

        init {
            binding.root.setOnClickListener {
                currentCinema?.let(onItemClick)
            }
        }

        fun bind(cinema: Cinema) {
            currentCinema = cinema
            binding.tvCinemaName.text = cinema.name
            binding.tvCinemaAddress.text = cinema.address

            // Загрузка логотипа (если есть URL и ImageView)
            if (binding.ivCinemaLogo != null && cinema.logoUrl != null) {
                Glide.with(binding.ivCinemaLogo.context)
                    .load(cinema.logoUrl)
                    .placeholder(R.drawable.logo_icon) // Замените на свой плейсхолдер
                    .error(R.drawable.logo_icon)       // Картинка при ошибке
                    .into(binding.ivCinemaLogo)
            } else if (binding.ivCinemaLogo != null) {
                // Если URL нет, можно скрыть ImageView или поставить дефолтное изображение
                binding.ivCinemaLogo.setImageResource(R.drawable.logo_icon) // Или View.GONE
            }
        }
    }

    companion object {
        private val CinemaDiffCallback = object : DiffUtil.ItemCallback<Cinema>() {
            override fun areItemsTheSame(oldItem: Cinema, newItem: Cinema): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Cinema, newItem: Cinema): Boolean {
                return oldItem == newItem
            }
        }
    }
}