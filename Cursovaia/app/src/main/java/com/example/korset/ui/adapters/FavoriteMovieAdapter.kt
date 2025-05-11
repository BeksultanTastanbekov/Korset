package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.View // Импорт для View.VISIBLE/GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.local.FavoriteMovieEntity // Используем Entity из БД
import com.example.korset.databinding.ListItemMovieBinding // Используем тот же макет элемента
import com.example.korset.data.network.ApiService // Для URL постера
import java.util.Locale // Для String.format

class FavoriteMovieAdapter(
    private val onItemClick: (FavoriteMovieEntity) -> Unit
) : ListAdapter<FavoriteMovieEntity, FavoriteMovieAdapter.FavoriteMovieViewHolder>(FavoriteMovieDiffCallback) {

    // Получаем базовый URL и размер из ApiService
    private val posterBaseUrl = ApiService.IMAGE_BASE_URL
    private val posterSize = ApiService.POSTER_SIZE_W500

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteMovieViewHolder {
        val binding = ListItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteMovieViewHolder(binding, onItemClick, posterBaseUrl, posterSize) // Передаем URL и размер
    }

    override fun onBindViewHolder(holder: FavoriteMovieViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder для избранного фильма
    class FavoriteMovieViewHolder(
        private val binding: ListItemMovieBinding,
        private val onItemClick: (FavoriteMovieEntity) -> Unit,
        private val posterBaseUrl: String, // Получаем базовый URL
        private val posterSize: String     // Получаем размер постера
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentFavoriteMovie: FavoriteMovieEntity? = null

        init {
            binding.root.setOnClickListener {
                currentFavoriteMovie?.let(onItemClick)
            }
        }

        fun bind(item: FavoriteMovieEntity) {
            currentFavoriteMovie = item

            binding.tvTitle.text = item.title ?: "Нет названия"

            // Форматируем метаданные (год и рейтинг)
            val year = item.releaseDate?.substringBefore("-") ?: "----"
            // Используем Locale.US для точки в качестве десятичного разделителя
            val rating = item.voteAverage?.let { String.format(Locale.US, "%.1f ★", it) } ?: "-.- ★"
            binding.tvMetadata.text = "$year • $rating"

            // Отображаем описание
            binding.tvDescription.text = item.overview ?: ""
            // Управляем видимостью описания (если оно пустое)
            binding.tvDescription.visibility = if (item.overview.isNullOrBlank()) View.GONE else View.VISIBLE

            // Формируем URL постера
            val posterUrl = item.posterPath?.let { path -> posterBaseUrl + posterSize + path }

            // Загружаем постер с помощью Glide
            Glide.with(binding.ivPoster.context)
                .load(posterUrl)
                .placeholder(R.drawable.logo_icon) // Заглушка во время загрузки
                .error(R.drawable.logo_icon)       // Картинка при ошибке
                .centerCrop()
                .into(binding.ivPoster)

            // Скрываем ненужные элементы из макета list_item_movie (если они есть)
        }
    }

    // DiffUtil для сравнения FavoriteMovieEntity
    companion object {
        val FavoriteMovieDiffCallback = object : DiffUtil.ItemCallback<FavoriteMovieEntity>() {
            override fun areItemsTheSame(oldItem: FavoriteMovieEntity, newItem: FavoriteMovieEntity): Boolean {
                return oldItem.movieId == newItem.movieId // Сравниваем по ID
            }

            override fun areContentsTheSame(oldItem: FavoriteMovieEntity, newItem: FavoriteMovieEntity): Boolean {
                // Сравниваем все поля, чтобы отловить любые изменения
                return oldItem == newItem
            }
        }
    }
}