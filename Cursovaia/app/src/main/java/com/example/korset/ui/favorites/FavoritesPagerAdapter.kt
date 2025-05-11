package com.example.korset.ui.favorites

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FavoritesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    val tabTitles = arrayOf("Избранное", "Коллекции")

    override fun getItemCount(): Int = tabTitles.size

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FavoriteMoviesFragment.newInstance() // Используем пустой newInstance
            1 -> CollectionsFragment.newInstance()   // Используем пустой newInstance
            else -> throw IllegalStateException("Invalid position for FavoritesPagerAdapter")
        }
    }
}