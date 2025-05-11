package com.example.korset.ui.tickets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.databinding.FragmentHistoryTicketsBinding // <<<=== ViewBinding
import com.example.korset.ui.adapters.TicketAdapter
import com.example.korset.ui.details.BookingViewModel
import android.util.Log
import androidx.navigation.fragment.findNavController
import com.example.korset.ui.tickets.MyTicketsFragmentDirections

class HistoryTicketsFragment : Fragment() {

    private var _binding: FragmentHistoryTicketsBinding? = null
    private val binding get() = _binding!!

    private val bookingViewModel: BookingViewModel by activityViewModels()
    private lateinit var ticketAdapter: TicketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryTicketsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("HistoryTicketsFragment: onViewCreated")

        setupRecyclerView()
        observeHistoryTickets()

        // bookingViewModel.loadHistoryTickets() // Вызов перенесен в MyTicketsFragment
    }

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter { ticket ->
            // --- ПЕРЕХОД К ДЕТАЛЯМ БИЛЕТА ---
            try {
                // Используем NavController от РОДИТЕЛЬСКОГО MyTicketsFragment
                val action = MyTicketsFragmentDirections.actionMyTicketsFragmentToTicketDetailFragment(ticketId = ticket.id)
                requireParentFragment().findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("ActiveTicketsFragment", "Navigation to TicketDetail failed", e)
                Toast.makeText(context, "Не удалось открыть детали билета", Toast.LENGTH_SHORT).show()
            }
            // ---------------------------------
        }
        binding.rvHistoryTickets.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ticketAdapter
        }
        println("HistoryTicketsFragment: RecyclerView setup done.")
    }

    private fun observeHistoryTickets() {
        bookingViewModel.historyTickets.observe(viewLifecycleOwner) { tickets ->
            println("HistoryTicketsFragment: Observed ${tickets?.size ?: "null"} history tickets.")
            ticketAdapter.submitList(tickets)
            binding.tvNoHistoryTickets.isVisible = tickets.isNullOrEmpty()
            binding.rvHistoryTickets.isVisible = !tickets.isNullOrEmpty()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvHistoryTickets.adapter = null
        _binding = null
        println("HistoryTicketsFragment: onDestroyView")
    }

    companion object {
        @JvmStatic
        fun newInstance() = HistoryTicketsFragment()
    }
}