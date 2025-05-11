package com.example.korset.ui.tickets

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast // Для теста клика
import androidx.core.view.isVisible // Для управления видимостью
import androidx.fragment.app.activityViewModels // Для BookingViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.databinding.FragmentActiveTicketsBinding // <<<=== ViewBinding
import com.example.korset.ui.adapters.TicketAdapter // <<<=== Адаптер билетов
import com.example.korset.ui.details.BookingViewModel // ViewModel с билетами
import androidx.navigation.fragment.findNavController
import com.example.korset.ui.tickets.MyTicketsFragmentDirections

class ActiveTicketsFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentActiveTicketsBinding? = null
    private val binding get() = _binding!!

    // Получаем BookingViewModel от родительской Activity (или родительского фрагмента MyTicketsFragment)
    private val bookingViewModel: BookingViewModel by activityViewModels()
    // ИЛИ если MyTicketsFragment создает свой BookingViewModel, то:
    // private val bookingViewModel: BookingViewModel by viewModels({requireParentFragment()})


    private lateinit var ticketAdapter: TicketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("ActiveTicketsFragment: onViewCreated")

        setupRecyclerView()
        observeActiveTickets()

        // Загружаем активные билеты при создании (ViewModel сам решит, нужно ли грузить снова)
        // bookingViewModel.loadActiveTickets() // Вызов перенесен в MyTicketsFragment
    }

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter { ticket ->
            // --- ПЕРЕХОД К ДЕТАЛЯМ БИЛЕТА ---
            try {
                // Используем NavController от РОДИТЕЛЬСКОГО MyTicketsFragment
                val action =
                    MyTicketsFragmentDirections.actionMyTicketsFragmentToTicketDetailFragment(
                        ticketId = ticket.id
                    )
                requireParentFragment().findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("ActiveTicketsFragment", "Navigation to TicketDetail failed", e)
                Toast.makeText(context, "Не удалось открыть детали билета", Toast.LENGTH_SHORT)
                    .show()
            }
            // ---------------------------------
        }
        binding.rvActiveTickets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ticketAdapter
        }
        println("ActiveTicketsFragment: RecyclerView setup done.")
    }


    private fun observeActiveTickets() {
        bookingViewModel.activeTickets.observe(viewLifecycleOwner) { tickets ->
            println("ActiveTicketsFragment: Observed ${tickets?.size ?: "null"} active tickets.")
            ticketAdapter.submitList(tickets)
            // Показываем/скрываем сообщение "Нет билетов"
            binding.tvNoActiveTickets.isVisible = tickets.isNullOrEmpty()
            binding.rvActiveTickets.isVisible = !tickets.isNullOrEmpty()
        }

        // Опционально: Наблюдение за состоянием загрузки (если оно есть в BookingViewModel для списка)
        // bookingViewModel.isLoadingActiveTickets.observe(viewLifecycleOwner) { isLoading ->
        //     binding.progressBarActiveTickets.isVisible = isLoading
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvActiveTickets.adapter = null // Очищаем адаптер
        _binding = null
        println("ActiveTicketsFragment: onDestroyView")
    }

    companion object {
        @JvmStatic
        fun newInstance() = ActiveTicketsFragment()
    }
}