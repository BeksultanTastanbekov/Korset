package com.example.korset.ui.tickets // Убедись, что пакет правильный

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

// Адаптер для ViewPager2 на экране "Мои Билеты"
class MyTicketsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    // Названия вкладок
    val tabTitles = arrayOf("Активные билеты", "История")

    // Количество вкладок
    override fun getItemCount(): Int = tabTitles.size

    // Создание фрагмента для каждой позиции/вкладки
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActiveTicketsFragment.newInstance() // Фрагмент для активных билетов
            1 -> HistoryTicketsFragment.newInstance() // Фрагмент для истории
            else -> throw IllegalStateException("Invalid position for MyTicketsPagerAdapter")
        }
    }
}