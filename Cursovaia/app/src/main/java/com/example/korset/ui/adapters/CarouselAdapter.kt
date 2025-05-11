package com.example.korset.ui.adapters // Убедись, что пакет правильный

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.korset.R // Импорт для placeholder/error
import com.example.korset.data.model.Movie // Импорт твоей модели Movie
import com.example.korset.data.network.ApiService // Для базового URL
import com.example.korset.databinding.ItemCarouselMovieBinding // Binding для макета элемента

// Адаптер для ViewPager2, наследуется от ListAdapter
class CarouselAdapter(private val onClick: (Movie) -> Unit) :
    ListAdapter<Movie, CarouselAdapter.CarouselViewHolder>(MovieDiffCallback) {

    // Создание ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val binding = ItemCarouselMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CarouselViewHolder(binding, onClick)
    }

    // Привязка данных к ViewHolder
    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    // ViewHolder для элемента карусели
    class CarouselViewHolder(
        private val binding: ItemCarouselMovieBinding,
        private val onClick: (Movie) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentMovie: Movie? = null

        // Устанавливаем клик на весь элемент
        init {
            binding.root.setOnClickListener {
                currentMovie?.let { movie ->
                    onClick(movie) // Вызываем лямбду при клике
                }
            }
        }

        // Заполняем ImageView данными
        fun bind(movie: Movie) {
            currentMovie = movie

            // --- ИЗМЕНЕНИЕ: Используем backdropPath и размер пошире ---
            val backdropUrl = movie.backdropPath?.let { path ->
                // Формируем URL для backdrop (размер w780 или w1280 обычно хорош)
                ApiService.IMAGE_BASE_URL + "w780" + path
            }
            // --------------------------------------------------------

            Glide.with(binding.ivCarouselPoster.context)
                .load(backdropUrl) // <-- Загружаем ИМЕННО backdropUrl
                .placeholder(R.drawable.logo_icon) // Можно оставить лого или сделать другой placeholder
                .error(R.drawable.logo_icon)
                .centerCrop() // <-- centerCrop хорошо подходит для backdrops
                .into(binding.ivCarouselPoster)
        }
    }

    // DiffUtil для сравнения элементов списка
    object MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            // Сравниваем по важным полям, если просто 'oldItem == newItem' не работает
            return oldItem.id == newItem.id && oldItem.posterPath == newItem.posterPath
        }
    }
}