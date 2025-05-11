package com.example.korset.ui.tickets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels // Для BookingViewModel
import androidx.lifecycle.ViewModelProvider // Если ViewModel нужна только этому фрагменту
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.local.TicketEntity
import com.example.korset.data.network.ApiService
import com.example.korset.databinding.FragmentTicketDetailBinding // ViewBinding
import com.example.korset.ui.details.BookingViewModel // ViewModel с логикой билетов
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TicketDetailFragment : Fragment() {

    private var _binding: FragmentTicketDetailBinding? = null
    private val binding get() = _binding!!

    // Получаем аргументы
    private val args: TicketDetailFragmentArgs by navArgs()

    // Используем BookingViewModel (общий с Activity)
    private val bookingViewModel: BookingViewModel by activityViewModels()

    // Форматы для даты и времени
    private val outputDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("ru")) // "13 апреля 2025"
    private val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault()) // "12:15"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTicketDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("TicketDetailFragment: onViewCreated, ticketId: ${args.ticketId}")

        setupToolbar()
        observeTicketDetails()

        // Запрашиваем детали билета (если ViewModel этого еще не делает)
        // bookingViewModel.loadTicketDetails(args.ticketId) // Нужна такая функция в ViewModel
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarTicketDetail)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarTicketDetail.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeTicketDetails() {
        // TODO: Нужен LiveData в BookingViewModel для ОДНОГО билета
        // bookingViewModel.selectedTicketDetails.observe(viewLifecycleOwner) { ticket ->
        //     ticket?.let { bindTicketDetails(it) }
        //         ?: Toast.makeText(context, "Не удалось загрузить детали билета", Toast.LENGTH_SHORT).show()
        // }

        // ВРЕМЕННАЯ ЗАГЛУШКА: Пока нет загрузки одного билета, возьмем из списка (если есть)
        // Это не очень хорошо, но для отображения пойдет.
        // Лучше добавить getTicketById в DAO и ViewModel.
        val ticket = bookingViewModel.activeTickets.value?.find { it.id == args.ticketId }
            ?: bookingViewModel.historyTickets.value?.find { it.id == args.ticketId }

        if (ticket != null) {
            bindTicketDetails(ticket)
        } else {
            println("TicketDetailFragment: Ticket with ID ${args.ticketId} not found in ViewModel lists.")
            Toast.makeText(context, "Детали билета не найдены", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindTicketDetails(ticket: TicketEntity) {
        println("TicketDetailFragment: Binding details for ticket ID ${ticket.id}")
        binding.toolbarTicketDetail.title = ticket.movieTitle ?: "Детали билета" // Заголовок Toolbar

        // Верхняя часть с постером
        binding.tvTicketDetailMovieTitle.text = ticket.movieTitle ?: "Фильм"
        binding.tvTicketDetailCinemaInfo.text = "${ticket.cinemaName}, ${ticket.hallName}"

        val sessionDate = Date(ticket.sessionTimestamp)
        val formattedDate = outputDateFormat.format(sessionDate) // "13 апреля 2025"
        binding.tvTicketDetailDatetime.text = "Дата и время\n$formattedDate • ${ticket.sessionTime}"

        val posterUrl = ticket.moviePosterPath?.let { ApiService.IMAGE_BASE_URL + ApiService.POSTER_SIZE_W500 + it }
        Glide.with(requireContext())
            .load(posterUrl)
            .placeholder(R.drawable.logo_icon)
            .error(R.drawable.logo_icon)
            .into(binding.ivTicketDetailPoster)

        // Детали мест и цены
        binding.tvTicketDetailSeats.text = ticket.seatsInfo
        // Формируем строку для "Детали" (количество и тип)
        binding.tvTicketDetailCountType.text = "${ticket.ticketCount} ${ticket.ticketTypeName}"
        binding.tvTicketDetailTotalPrice.text = "${ticket.totalPrice} ₸"

        // Номер заказа и Access Code (пока заглушки, если их нет в TicketEntity)
        binding.tvTicketDetailOrderNumber.text = ticket.id.toString().padStart(12, '0') // Пример номера заказа
        binding.tvTicketDetailAccessCode.text = "EXT_${ticket.id.toString().padStart(12, '0')}"

        // TODO: Реализовать генерацию QR-кода (можно использовать библиотеку)
        // Пока просто показываем заглушку
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}