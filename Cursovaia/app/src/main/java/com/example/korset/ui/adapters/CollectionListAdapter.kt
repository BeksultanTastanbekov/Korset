package com.example.korset.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.R
import com.example.korset.data.local.CollectionEntity // Модель коллекции из БД
import com.example.korset.databinding.ListItemCollectionBinding // ViewBinding
import com.example.korset.data.local.CollectionWithMovies // <<<=== ИЗМЕНЕНИЕ
import android.widget.ImageButton

class CollectionListAdapter(
    private val onCollectionClick: (CollectionWithMovies) -> Unit,
    private val onDeleteClick: (CollectionEntity) -> Unit // <<<=== НОВАЯ ЛЯМБДА
) : ListAdapter<CollectionWithMovies, CollectionListAdapter.CollectionViewHolder>(CollectionDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ListItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        // Передаем ОБА обработчика
        return CollectionViewHolder(binding, onCollectionClick, onDeleteClick) // <<<=== ИЗМЕНЕНО
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CollectionViewHolder(
        private val binding: ListItemCollectionBinding,
        private val onCollectionClick: (CollectionWithMovies) -> Unit,
        private val onDeleteClick: (CollectionEntity) -> Unit // <<<=== НОВАЯ ЛЯМБДА
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentCollectionWithMovies: CollectionWithMovies? = null // <<<=== ИЗМЕНЕНИЕ

        init {
            // Клик по всему элементу
            binding.root.setOnClickListener {
                currentCollectionWithMovies?.let { onCollectionClick(it) }
            }
            // Клик по кнопке удаления
            binding.buttonDeleteCollection.setOnClickListener { // <<<=== НОВЫЙ СЛУШАТЕЛЬ
                currentCollectionWithMovies?.collection?.let { collection ->
                    onDeleteClick(collection)
                }
            }
        }

        fun bind(collectionWithMovies: CollectionWithMovies) { // <<<=== ИЗМЕНЕНИЕ
            currentCollectionWithMovies = collectionWithMovies
            val collection = collectionWithMovies.collection // Получаем саму коллекцию
            val movieCount = collectionWithMovies.movies.size // Получаем количество фильмов

            binding.tvCollectionName.text = collection.name
            // Отображаем количество фильмов
            binding.tvCollectionMovieCount.text = itemView.resources.getQuantityString(
                R.plurals.movies_count, // <<<=== НУЖНО СОЗДАТЬ plurals
                movieCount,
                movieCount
            )
        }
    }

    // DiffUtil теперь сравнивает CollectionWithMovies
    companion object { // Оборачиваем в companion object
        val CollectionDiffCallback = object : DiffUtil.ItemCallback<CollectionWithMovies>() {
            override fun areItemsTheSame(oldItem: CollectionWithMovies, newItem: CollectionWithMovies): Boolean {
                return oldItem.collection.collectionId == newItem.collection.collectionId // Сравниваем по ID коллекции
            }
            override fun areContentsTheSame(oldItem: CollectionWithMovies, newItem: CollectionWithMovies): Boolean {
                return oldItem == newItem // Сравниваем все поля data class
            }
        }
    }
}