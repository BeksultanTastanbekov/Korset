package com.example.korset.ui.details // Или ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.model.CastMember // Модель актера
import com.example.korset.data.network.ApiService
import com.example.korset.databinding.ListItemCastBinding // ViewBinding

class CastAdapter : ListAdapter<CastMember, CastAdapter.CastViewHolder>(CastDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val binding = ListItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CastViewHolder(private val binding: ListItemCastBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(castMember: CastMember) {
            binding.tvActorName.text = castMember.name ?: "Неизвестно"
            binding.tvCharacterName.text = castMember.character ?: ""

            val photoUrl = castMember.profilePath?.let {
                ApiService.IMAGE_BASE_URL + "w185" + it // Размер поменьше для фото актера
            }

            Glide.with(binding.ivActorPhoto.context)
                .load(photoUrl)
                .placeholder(R.drawable.ic_baseline_person_24) // Заглушка для фото
                .error(R.drawable.ic_baseline_person_24)
                .circleCrop() // Можно использовать Glide для скругления
                .into(binding.ivActorPhoto)
        }
    }

    object CastDiffCallback : DiffUtil.ItemCallback<CastMember>() {
        override fun areItemsTheSame(oldItem: CastMember, newItem: CastMember): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: CastMember, newItem: CastMember): Boolean {
            return oldItem == newItem
        }
    }
}