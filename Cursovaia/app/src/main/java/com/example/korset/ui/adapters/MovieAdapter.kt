package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter // <<<=== ВОЗВРАЩАЕМ PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.model.Movie
import com.example.korset.databinding.ListItemMovieBinding
import com.example.korset.data.network.ApiService

// Наследуемся от PagingDataAdapter<Movie, MovieViewHolder>
class MovieAdapter(
    private val onItemClick: (Movie) -> Unit
) : PagingDataAdapter<Movie, MovieAdapter.MovieViewHolder>(MovieDiffCallback()) { // DiffCallback тот же

    private val posterBaseUrl = ApiService.IMAGE_BASE_URL
    private val posterSize = ApiService.POSTER_SIZE_W500

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ListItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding, onItemClick, posterBaseUrl, posterSize)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        // Используем getItem(position) из PagingDataAdapter
        val movie = getItem(position)
        movie?.let { holder.bind(it) } // getItem может вернуть null
    }

    // ViewHolder остается таким же
    class MovieViewHolder(
        private val binding: ListItemMovieBinding,
        private val onItemClick: (Movie) -> Unit,
        private val posterBaseUrl: String,
        private val posterSize: String
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentMovie: Movie? = null
        init { binding.root.setOnClickListener { currentMovie?.let(onItemClick) } }

        fun bind(item: Movie) {
            currentMovie = item
            binding.tvTitle.text = item.title ?: item.name ?: "Нет названия"
            val dateString = item.releaseDate ?: item.firstAirDate
            val year = dateString?.substringBefore("-") ?: "----"
            val rating = item.voteAverage?.let { String.format("%.1f ★", it) } ?: "-.- ★"
            binding.tvMetadata.text = "$year • $rating"
            binding.tvDescription.text = item.overview ?: ""

            val posterUrl = item.posterPath?.let { path -> posterBaseUrl + posterSize + path }

            Glide.with(binding.ivPoster.context)
                .load(posterUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .error(R.drawable.ic_placeholder_image)
                .centerCrop()
                .into(binding.ivPoster)
        }
    }

    // DiffUtil остается таким же
    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }
}