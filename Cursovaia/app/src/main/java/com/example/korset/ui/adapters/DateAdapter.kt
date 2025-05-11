package com.example.korset.ui.adapters // Убедись, что пакет правильный

import android.annotation.SuppressLint
import android.graphics.Color // Для цвета фона
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat // Для получения цвета из ресурсов
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.R // Для доступа к ресурсам (цветам)
import com.example.korset.data.model.DateItem
import com.example.korset.databinding.ListItemDateBinding // ViewBinding

class DateAdapter(
    private val onDateClick: (DateItem, Int) -> Unit // Лямбда для клика, передает элемент и позицию
) : ListAdapter<DateItem, DateAdapter.DateViewHolder>(DateDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder {
        val binding = ListItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateViewHolder(binding, onDateClick)
    }

    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    // ViewHolder для элемента даты
    class DateViewHolder(
        private val binding: ListItemDateBinding,
        private val onDateClick: (DateItem, Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentDateItem: DateItem? = null
        private var currentPosition: Int = -1

        // Устанавливаем клик на контейнер элемента
        init {
            binding.dateItemContainer.setOnClickListener {
                currentDateItem?.let {
                    // Вызываем лямбду при клике, передавая сам элемент и его позицию
                    onDateClick(it, currentPosition)
                }
            }
        }

        // Заполняем View данными и управляем выделением
        fun bind(item: DateItem, position: Int) {
            currentDateItem = item
            currentPosition = position

            binding.tvDayOfWeek.text = item.dayOfWeek
            binding.tvDayOfMonth.text = item.dayOfMonth

            // Устанавливаем фон и цвет текста в зависимости от того, выбрана ли дата
            val context = binding.root.context
            if (item.isSelected) {
                // Выбранный элемент - цветной фон, белый текст
                binding.dateItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary)) // Твой акцентный цвет
                binding.tvDayOfWeek.setTextColor(Color.WHITE)
                binding.tvDayOfMonth.setTextColor(Color.WHITE)
            } else {
                // Невыбранный элемент - прозрачный фон (или фон по умолчанию), стандартный цвет текста
                binding.dateItemContainer.setBackgroundColor(Color.TRANSPARENT) // Или ?attr/selectableItemBackground
                binding.tvDayOfWeek.setTextColor(ContextCompat.getColor(context, android.R.color.secondary_text_light)) // Цвет вторичного текста
                binding.tvDayOfMonth.setTextColor(ContextCompat.getColor(context, android.R.color.primary_text_light)) // Цвет основного текста
            }
        }
    }

    // DiffUtil для сравнения элементов (важно для ListAdapter)
    object DateDiffCallback : DiffUtil.ItemCallback<DateItem>() {
        override fun areItemsTheSame(oldItem: DateItem, newItem: DateItem): Boolean {
            return oldItem.date == newItem.date // Сравниваем по дате
        }

        @SuppressLint("DiffUtilEquals") // Объекты могут быть разными, но контент (дата и isSelected) тот же
        override fun areContentsTheSame(oldItem: DateItem, newItem: DateItem): Boolean {
            return oldItem == newItem // Сравниваем все поля (включая isSelected)
        }
    }
}