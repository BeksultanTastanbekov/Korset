package com.example.korset.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.korset.R
import com.example.korset.data.network.ApiService
import com.example.korset.databinding.FragmentOrderConfirmationBinding // ViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.korset.data.local.TicketEntity // Импорт TicketEntity
import com.example.korset.data.local.toSeatsString // Импорт функции расширения
import java.util.concurrent.TimeUnit // Для форматирования времени (если понадобится)
import java.util.Calendar
import com.example.korset.util.EventObserver
import com.example.korset.ui.details.BookingViewModel
import androidx.fragment.app.activityViewModels

class OrderConfirmationFragment : Fragment() {

    private var _binding: FragmentOrderConfirmationBinding? = null
    private val binding get() = _binding!!

    private val args: OrderConfirmationFragmentArgs by navArgs()

    private val bookingViewModel: BookingViewModel by activityViewModels()

    // --- ДОБАВЛЯЕМ BookingViewModel ---
//    private lateinit var bookingViewModel: BookingViewModel

    // Переменные для промокода
    private var currentTotalPrice: Int = 0
    private var promoApplied: Boolean = false

    // TODO: Добавить ViewModel для сохранения билета

//    override fun onCreate(savedInstanceState: Bundle?) { // Создаем ViewModel в onCreate
//        super.onCreate(savedInstanceState)
//        bookingViewModel = ViewModelProvider(this).get(BookingViewModel::class.java)
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("OrderConfirmationFragment: onViewCreated")

        setupToolbar()
        displayOrderDetails() // Отображаем начальные данные

        setupButtons() // Настраиваем кнопки
        observeBookingStatus() // Наблюдаем за статусом сохранения
    }

    // Настройка Toolbar
    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarConfirmation)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarConfirmation.setNavigationOnClickListener {
            showCancelConfirmationDialog() // Предлагаем отменить при нажатии назад
        }
    }

    // Отображение деталей заказа
    private fun displayOrderDetails() {
        val session = args.session
        val seats = args.seats.toList()
        val ticketType = args.ticketType
        val movieTitle = args.movieTitle ?: "Фильм"
        val moviePosterPath = args.moviePosterPath
        currentTotalPrice = args.totalPrice // Сохраняем начальную цену

        println("OrderConfirmationFragment: Displaying details - Movie: $movieTitle, Seats: ${seats.size}, Price: $currentTotalPrice")

        // --- Отображение информации о фильме и сеансе ---
        binding.tvMovieSessionInfoConfirm.text = """
            $movieTitle
            ${session.time} • ${ticketType.name}
            ${session.cinemaName} • ${session.hall}
        """.trimIndent()

        val posterUrl = moviePosterPath?.let { ApiService.IMAGE_BASE_URL + ApiService.POSTER_SIZE_W500 + it }
        Glide.with(requireContext())
            .load(posterUrl)
            .placeholder(R.drawable.logo_icon)
            .error(R.drawable.logo_icon)
            .into(binding.ivMoviePosterConfirm)

        // --- Отображение информации о билетах ---
        val ticketCount = seats.size
        binding.tvTicketCount.text = resources.getQuantityString(R.plurals.tickets, ticketCount, ticketCount)
        val seatsText = seats.joinToString { "Р${it.row} М${it.number}" }
        binding.tvSelectedSeatsConfirm.text = seatsText

        // --- Отображение стоимости ---
        updatePriceDisplay() // Используем отдельную функцию для обновления цены

        // --- ТАЙМЕР УДАЛЕН ИЗ ЛОГИКИ ---
        // Убедись, что LinearLayout с таймером закомментирован или удален в XML
    }

    // Функция для обновления отображения цены
    private fun updatePriceDisplay() {
        binding.tvTotalPrice.text = "$currentTotalPrice ₸"
        binding.buttonPay.text = "Оплатить $currentTotalPrice ₸"
    }

    // Функция применения промокода
    private fun applyPromoCode(code: String) {
        if (promoApplied) {
            Toast.makeText(context, "Промокод уже применен", Toast.LENGTH_SHORT).show()
            return
        }
        // Если код пустой, ничего не делаем
        if (code.isBlank()) {
            Toast.makeText(context, "Введите промокод", Toast.LENGTH_SHORT).show()
            return
        }

        val validPromoCodes = mapOf(
            "KORSET" to 0.50,
            "DIASPIAZ" to 0.90,
            "VMESTE" to 0.20
        )
        val discountPercent = validPromoCodes[code.uppercase()] // Приводим к верхнему регистру

        if (discountPercent != null) {
            val discountAmount = (args.totalPrice * discountPercent).toInt() // Скидка от НАЧАЛЬНОЙ цены
            currentTotalPrice = args.totalPrice - discountAmount // Пересчитываем от НАЧАЛЬНОЙ
            promoApplied = true

            updatePriceDisplay() // Обновляем текст цены и кнопки
            binding.editTextPromoCode.isEnabled = false
            binding.buttonApplyPromo.isEnabled = false // Делаем кнопку "Применить" неактивной
            binding.buttonApplyPromo.text = "Применен" // Меняем текст

            Toast.makeText(context, "Скидка ${(discountPercent * 100).toInt()}% применена!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Неверный промокод", Toast.LENGTH_SHORT).show()
        }
    }

    // Настройка всех кнопок
    private fun setupButtons() {
        // --- КНОПКА "ПРИМЕНИТЬ" ПРОМОКОД ---
        // Убедись, что у кнопки "Применить" в XML есть ID: android:id="@+id/button_apply_promo"
        binding.buttonApplyPromo.setOnClickListener {
            val promoCode = binding.editTextPromoCode.text.toString()
            applyPromoCode(promoCode)
        }
        // -----------------------------------

        // --- КНОПКА "ОПЛАТИТЬ" ---
        binding.buttonPay.setOnClickListener {
            // TODO: Сохранить билет в БД
            println("OrderConfirmationFragment: 'Pay' button clicked. Saving ticket...")
            createAndSaveTicket()
//            saveTicketToDatabase() // Вызываем новую функцию сохранения
        }
        // -------------------------

        // --- КНОПКА "ОТМЕНИТЬ БРОНЬ" ---
        binding.buttonCancelBooking.setOnClickListener {
            showCancelConfirmationDialog()
        }
        // -----------------------------
    }

    // --- НОВАЯ ФУНКЦИЯ: Сохранение билета (пока заглушка) ---
    private fun saveTicketToDatabase() {
        // Здесь будет логика создания TicketEntity и вызова ViewModel/DAO
        // val ticket = TicketEntity(...)
        // bookingViewModel.saveTicket(ticket)

        // После "сохранения" показываем сообщение и уходим на главный экран
        Toast.makeText(context, "Билет успешно 'оплачен' и сохранен!", Toast.LENGTH_LONG).show()
        findNavController().navigate(R.id.nav_home, null, androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build())
    }
    // --- КОНЕЦ НОВОЙ ФУНКЦИИ ---

    // --- НОВАЯ ФУНКЦИЯ: Создание и сохранение билета ---
    private fun createAndSaveTicket() {
        // Собираем все данные
        val session = args.session
        val seats = args.seats.toList()
        val ticketType = args.ticketType
        // Используем currentTotalPrice на случай применения промокода
        // val totalPrice = args.totalPrice // Не используем начальную цену
        val movieTitle = args.movieTitle
        val moviePosterPath = args.moviePosterPath
        val movieId = args.movieId

        // Преобразуем дату/время сеанса в timestamp (Long)
        // Предполагаем, что session.date это java.util.Date, а session.time это "HH:mm"
        val sessionDateTimeMillis = try {
            val timeParts = session.time.split(":")
            val calendar = Calendar.getInstance().apply {
                time = session.date // Устанавливаем дату
                set(Calendar.HOUR_OF_DAY, timeParts.getOrNull(0)?.toInt() ?: 0)
                set(Calendar.MINUTE, timeParts.getOrNull(1)?.toInt() ?: 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        } catch (e: Exception) {
            println("OrderConfirmationFragment: Error parsing session date/time - ${e.message}")
            System.currentTimeMillis() // Запасной вариант - текущее время
        }


        // Создаем TicketEntity
        val newTicket = TicketEntity(
            movieId = movieId,
            movieTitle = movieTitle,
            moviePosterPath = moviePosterPath,
            cinemaName = session.cinemaName,
            hallName = session.hall,
            sessionTime = session.time,
            sessionTimestamp = sessionDateTimeMillis, // Сохраняем timestamp
            seatsInfo = seats.toSeatsString(), // Используем функцию расширения
            ticketTypeName = ticketType.name,
            ticketPrice = ticketType.price,
            ticketCount = seats.size,
            totalPrice = currentTotalPrice // Используем текущую цену (с учетом скидки)
            // purchaseTimestamp генерируется автоматически
        )

        // Вызываем ViewModel для сохранения
        bookingViewModel.saveTicket(newTicket)
        println("OrderConfirmationFragment: Saving ticket...")
    }
    // --- КОНЕЦ НОВОЙ ФУНКЦИИ ---

    // --- НОВАЯ ФУНКЦИЯ: Наблюдение за статусом сохранения ---
    private fun observeBookingStatus() {
        bookingViewModel.saveStatus.observe(viewLifecycleOwner, EventObserver { success ->
            if (success) {
                println("OrderConfirmationFragment: Ticket saved successfully! Navigating home.")
                Toast.makeText(context, "Билет успешно сохранен!", Toast.LENGTH_LONG).show()
                // Переходим на главный экран
                findNavController().navigate(R.id.nav_home, null, androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build())
            } else {
                println("OrderConfirmationFragment: Failed to save ticket.")
                Toast.makeText(context, "Не удалось сохранить билет. Попробуйте снова.", Toast.LENGTH_SHORT).show()
                // Остаемся на экране, чтобы пользователь мог попробовать еще раз
            }
        })
    }
    // --- КОНЕЦ НОВОЙ ФУНКЦИИ ---


    // Диалог подтверждения отмены (без изменений)
    // Показ диалога подтверждения отмены
    private fun showCancelConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Отмена бронирования")
            .setMessage("Вы уверены, что хотите отменить билет?")
            .setNegativeButton("Нет, оставить") { dialog, _ ->
                dialog.dismiss() // Просто закрываем диалог
            }
            .setPositiveButton("Да, отменить") { dialog, _ ->
                // Возвращаемся на предыдущий экран (выбора мест)
                println("OrderConfirmationFragment: Booking cancelled by user.")
                findNavController().popBackStack()
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        println("OrderConfirmationFragment: onDestroyView")
    }
}


