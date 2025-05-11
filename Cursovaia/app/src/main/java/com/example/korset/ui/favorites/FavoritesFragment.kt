package com.example.korset.ui.favorites

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.korset.databinding.FragmentFavoritesBinding // ViewBinding
import com.google.android.material.tabs.TabLayoutMediator

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("FavoritesFragment: onViewCreated")
        setupTabsAndViewPager()
    }

    private fun setupTabsAndViewPager() {
        val pagerAdapter = FavoritesPagerAdapter(this) // Передаем текущий фрагмент
        binding.viewPagerFavorites.adapter = pagerAdapter

        // Связываем TabLayout и ViewPager2
        TabLayoutMediator(binding.tabLayoutFavorites, binding.viewPagerFavorites) { tab, position ->
            tab.text = pagerAdapter.tabTitles[position]
        }.attach()
        println("FavoritesFragment: Tabs and ViewPager setup complete.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewPagerFavorites.adapter = null // Очищаем адаптер
        _binding = null
        println("FavoritesFragment: onDestroyView")
    }

    // Добавляем пустой newInstance для единообразия
    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }
}