package com.example.korset.ui.details

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer // <<<=== НОВЫЙ ИМПОРТ
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.ui.adapters.RowData
import com.example.korset.data.model.Seat
import com.example.korset.data.model.SeatType
import com.example.korset.data.model.SessionItem
import com.example.korset.databinding.FragmentSeatSelectionBinding
import com.example.korset.ui.adapters.RowAdapter
import kotlin.random.Random
// УДАЛЯЕМ: import androidx.fragment.app.setFragmentResultListener
import com.example.korset.ui.dialogs.TicketTypeBottomSheetFragment
import com.example.korset.data.model.TicketType
import com.example.korset.util.Event // <<<=== НОВЫЙ ИМПОРТ
import com.example.korset.util.EventObserver // <<<=== НОВЫЙ ИМПОРТ


class SeatSelectionFragment : Fragment() {

    private var _binding: FragmentSeatSelectionBinding? = null
    private val binding get() = _binding!!

    private val args: SeatSelectionFragmentArgs by navArgs()
    private lateinit var selectedSession: SessionItem

    // --- ViewModel для обмена данными с BottomSheet ---
    private lateinit var seatSelectionViewModel: SeatSelectionViewModel
    // ------------------------------------------------

    private lateinit var rowAdapter: RowAdapter
    private var allSeatsMap = mutableMapOf<String, Seat>()
    private val selectedSeats = mutableSetOf<Seat>()
    private val HALL_COLUMNS = 12 // Уменьшили количество

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Создаем ViewModel с областью видимости этого фрагмента
        seatSelectionViewModel = ViewModelProvider(this).get(SeatSelectionViewModel::class.java)
        println("SeatSelectionFragment: SeatSelectionViewModel created")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSeatSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        selectedSession = args.sessionItem
        println("SeatSelectionFragment: Received session for ${selectedSession.cinemaName} at ${selectedSession.time}")

        setupToolbar()
        displaySessionInfo()
        setupSeatRecyclerView()
        generateAndDisplaySeats()
        setupNextButton()
        // УДАЛЯЕМ: setupFragmentResultListener()
        observeViewModelEvents() // <<<=== НАЧИНАЕМ СЛУШАТЬ ViewModel
    }

    // --- Функции настройки UI и генерации данных (БЕЗ ИЗМЕНЕНИЙ) ---
    // Настройка Toolbar (без изменений)
    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarSeatSelection)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarSeatSelection.title = "Выберите места"
        binding.toolbarSeatSelection.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    // Отображение информации о сеансе (без изменений)
    private fun displaySessionInfo() {
        binding.tvSessionInfo.text = "${selectedSession.cinemaName}, ${selectedSession.hall}\n${selectedSession.time}"
    }

    // --- ИЗМЕНЕННАЯ Настройка RecyclerView ---
    // Теперь настраиваем RecyclerView для отображения РЯДОВ
    private fun setupSeatRecyclerView() {
        // Создаем RowAdapter, передавая лямбду handleSeatClick
        rowAdapter = RowAdapter { seat ->
            handleSeatClick(seat)
        }
        binding.rvSeats.apply {
            // Используем LinearLayoutManager для ВЕРТИКАЛЬНОГО списка рядов
            layoutManager = LinearLayoutManager(context)
            adapter = rowAdapter // Устанавливаем RowAdapter
            itemAnimator = null // Убираем анимацию
        }
        println("SeatSelectionFragment: Row RecyclerView setup done.")
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // --- ИЗМЕНЕННАЯ Генерация мест ---
    private fun generateAndDisplaySeats() {
        allSeatsMap.clear()
        selectedSeats.clear()
        val totalRows = 10 // Можно тоже уменьшить до 8, например
        val seatsInRow = HALL_COLUMNS
        val rowDataList = mutableListOf<RowData>()

        for (row in 1..totalRows) {
            val seatsForRow = mutableListOf<Seat>()
            for (number in 1..seatsInRow) {
                val seat = Seat(row = row, number = number)

                // --- ЛОГИКА ПРОХОДОВ: Только справа (последняя колонка) ---
                if (number == seatsInRow) { // Последнее место - пустое
                    seat.type = SeatType.EMPTY
                }
                // --------------------------------------------------------
                else {
                    // Логика VIP (например, центр рядов)
                    val centerStart = seatsInRow / 2 - 1 // Например, 6 для 14
                    val centerEnd = seatsInRow / 2 + 2   // Например, 9 для 14
                    if (row >= totalRows - 1 && number >= centerStart && number <= centerEnd) {
                        seat.type = SeatType.VIP
                        if (Random.nextInt(100) < 15) seat.isOccupied = true
                    } else {
                        seat.type = SeatType.STANDARD
                        if (Random.nextInt(100) < 25) seat.isOccupied = true
                    }
                }
                seatsForRow.add(seat)
                allSeatsMap[seat.id] = seat
            }
            rowDataList.add(RowData(rowNumber = row, seats = seatsForRow))
        }
        rowAdapter.submitList(rowDataList)
        updateNextButtonState()
        println("SeatSelectionFragment: Generated ${rowDataList.size} rows.")
    }

    // --- ИЗМЕНЕННАЯ Обработка клика по месту ---
    private fun handleSeatClick(clickedSeat: Seat) {
        val seatInMap = allSeatsMap[clickedSeat.id] ?: return

        // --- Важно: Создаем КОПИЮ места перед изменением ---
        // Это гарантирует, что объект, который видит DiffUtil, изменится
        val updatedSeat = seatInMap.copy(isSelected = !seatInMap.isSelected)
        // -------------------------------------------------

        // Обновляем место в нашей ОСНОВНОЙ карте
        allSeatsMap[clickedSeat.id] = updatedSeat

        // Обновляем Set выбранных мест
        if (updatedSeat.isSelected) {
            selectedSeats.add(updatedSeat)
        } else {
            // Нужно удалить старый объект из Set, если он там был
            selectedSeats.removeIf { it.id == updatedSeat.id }
        }

        // --- ВОЗВРАЩАЕМ ОБНОВЛЕНИЕ ВСЕГО СПИСКА ---
        // Создаем НОВЫЙ список RowData на основе обновленной allSeatsMap
        val updatedRowDataList = allSeatsMap.values // Берем обновленные Seat из карты
            .groupBy { it.row }
            .map { entry -> RowData(entry.key, entry.value.sortedBy { it.number }) }
            .sortedBy { it.rowNumber }

        // Передаем НОВЫЙ список в адаптер рядов. DiffUtil сравнит объекты Seat.
        rowAdapter.submitList(updatedRowDataList)
        println("SeatSelectionFragment: Submitted updated row list after click.")
        // --------------------------------------

        updateNextButtonState()
        println("SeatSelectionFragment: Seat clicked: ${updatedSeat.row}-${updatedSeat.number}, Selected: ${updatedSeat.isSelected}, Count: ${selectedSeats.size}")
    }

    // Настройка кнопки "Далее" (без изменений)
    private fun setupNextButton() {
        updateNextButtonState()
        binding.buttonNextStep.setOnClickListener {
            if (selectedSeats.isNotEmpty()) {
                showTicketTypeBottomSheet()
            }
        }
    }
    // Обновление состояния кнопки "Далее" (без изменений)
    private fun updateNextButtonState() {
        binding.buttonNextStep.isEnabled = selectedSeats.isNotEmpty()
    }


    // --- Показ BottomSheet (БЕЗ ИЗМЕНЕНИЙ) ---
    private fun showTicketTypeBottomSheet() {
        if (selectedSeats.isEmpty()) { /* ... */ return }
        val seatArrayList = ArrayList(selectedSeats)
        TicketTypeBottomSheetFragment.newInstance(seatArrayList, selectedSession)
            .show(childFragmentManager, TicketTypeBottomSheetFragment.TAG)
        println("SeatSelectionFragment: Showing TicketTypeBottomSheet...")
    }
    // ---------------------------------------

    // --- УДАЛЯЕМ setupFragmentResultListener ---
    /*
    private fun setupFragmentResultListener() { ... }
    */
    // ---------------------------------------

    // --- НОВАЯ ФУНКЦИЯ: Наблюдение за событиями от ViewModel ---
    private fun observeViewModelEvents() {
        println("SeatSelectionFragment: Setting up ViewModel observer")
        seatSelectionViewModel.selectedTicketTypeEvent.observe(viewLifecycleOwner, EventObserver { selectedType ->
            // Этот код выполнится ОДИН РАЗ при получении события
            println("SeatSelectionFragment: Event received from ViewModel: ${selectedType.name} for ${selectedSeats.size} seats")
            Toast.makeText(context, "Выбран тип: ${selectedType.name}", Toast.LENGTH_SHORT).show()

            // --- ПЕРЕХОД НА ЭКРАН ПОДТВЕРЖДЕНИЯ ---
            try {
                val currentMovieId = args.movieId // Получаем из аргументов фрагмента
                val currentMovieTitle = args.movieTitle
                val currentMoviePosterPath = args.moviePosterPath
                val totalPrice = selectedSeats.size * selectedType.price

                println("SeatSelectionFragment: Navigating to Confirmation. Price: $totalPrice")

                val action = SeatSelectionFragmentDirections
                    .actionSeatSelectionFragmentToOrderConfirmationFragment(
                        session = selectedSession,
                        seats = selectedSeats.toTypedArray(),
                        ticketType = selectedType,
                        totalPrice = totalPrice,
                        movieId = currentMovieId,
                        movieTitle = currentMovieTitle,
                        moviePosterPath = currentMoviePosterPath
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("SeatSelectionNav", "Navigation to Confirmation failed", e)
                Toast.makeText(context, "Не удалось перейти к подтверждению", Toast.LENGTH_SHORT).show()
            }
            // -----------------------------------------
        })
    }
    // --- КОНЕЦ НОВОЙ ФУНКЦИИ ---

    // --- Очистка в onDestroyView (БЕЗ ИЗМЕНЕНИЙ) ---
    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSeats.adapter = null
        _binding = null
        println("SeatSelectionFragment: onDestroyView")
    }
}

// --- Вспомогательный класс EventObserver ---
// Помести его в отдельный файл util/EventObserver.kt или в конец этого файла
class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) { // Изменено имя параметра
        value.getContentIfNotHandled()?.let { content -> // Используем content
            onEventUnhandledContent(content)
        }
    }
}
// ------------------------------------------



