package com.example.korset.ui.details // Убедись, что пакет правильный

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// Адаптер для ViewPager2 на экране деталей
// Используем конструктор FragmentActivity, так как адаптер создается во Фрагменте
class MovieDetailPagerAdapter(fragment: Fragment, private val movieId: Int) : FragmentStateAdapter(fragment) {

    // Массив заголовков для вкладок
    val tabTitles = arrayOf("Билеты", "О фильме", "Отзывы")

    // Количество вкладок
    override fun getItemCount(): Int = tabTitles.size

    // Создание нужного Фрагмента для каждой позиции
    override fun createFragment(position: Int): Fragment {
        // Передаем movieId во фрагменты, если он им нужен
        val args = Bundle().apply {
            putInt("movieId", movieId) // Используем ключ "movieId"
        }

        return when (position) {
            0 -> MovieSessionsFragment().apply { arguments = args } // Вкладка "Билеты"
            1 -> MovieInfoFragment().apply { arguments = args }   // Вкладка "О фильме"
            2 -> MovieReviewsFragment().apply { arguments = args } // Вкладка "Отзывы"
            else -> throw IllegalStateException("Неверная позиция для вкладки: $position")
        }
    }
}