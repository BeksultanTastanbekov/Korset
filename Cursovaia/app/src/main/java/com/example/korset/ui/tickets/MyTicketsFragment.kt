package com.example.korset.ui.tickets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels // Для BookingViewModel
import androidx.navigation.fragment.findNavController
import com.example.korset.databinding.FragmentMyTicketsBinding // ViewBinding
import com.example.korset.ui.details.BookingViewModel // ViewModel с данными о билетах
import com.google.android.material.tabs.TabLayoutMediator

class MyTicketsFragment : Fragment() {

    private var _binding: FragmentMyTicketsBinding? = null
    private val binding get() = _binding!!

    // Получаем BookingViewModel, так как он содержит логику для билетов
    private val bookingViewModel: BookingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("MyTicketsFragment: onViewCreated")

        setupToolbar()
        setupTabsAndViewPager()

        // Запрашиваем данные для билетов (ViewModel сама решит, нужно ли грузить снова)
        // Это вызовет загрузку, если данные еще не были загружены
        bookingViewModel.loadActiveTickets()
        bookingViewModel.loadHistoryTickets()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarMyTickets)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarMyTickets.setNavigationOnClickListener {
            findNavController().popBackStack() // Возврат на предыдущий экран
        }
    }

    private fun setupTabsAndViewPager() {
        val pagerAdapter = MyTicketsPagerAdapter(this) // Передаем текущий фрагмент
        binding.viewPagerMyTickets.adapter = pagerAdapter

        // Связываем TabLayout и ViewPager2
        TabLayoutMediator(binding.tabLayoutMyTickets, binding.viewPagerMyTickets) { tab, position ->
            tab.text = pagerAdapter.tabTitles[position] // Устанавливаем текст для каждой вкладки
        }.attach()

        // Опционально: чтобы фрагменты вкладок не пересоздавались часто
        binding.viewPagerMyTickets.offscreenPageLimit = pagerAdapter.itemCount
        println("MyTicketsFragment: Tabs and ViewPager setup complete.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Важно очищать адаптер ViewPager2, чтобы избежать утечек во вложенных фрагментах
        binding.viewPagerMyTickets.adapter = null
        _binding = null
        println("MyTicketsFragment: onDestroyView")
    }
}