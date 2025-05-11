package com.example.korset.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible // Для управления видимостью
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.R
import com.example.korset.databinding.FragmentMovieSessionsBinding // <-- ИМПОРТ ViewBinding
import com.example.korset.data.model.DateItem
import com.example.korset.data.model.SessionItem
import com.example.korset.ui.adapters.DateAdapter // <-- ИМПОРТ Адаптера
import com.example.korset.ui.adapters.SessionAdapter // <-- ИМПОРТ Адаптера
import com.google.android.material.chip.Chip // Для работы с чипами
import com.google.android.material.tabs.TabLayout // Для работы с табами
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random
import androidx.navigation.fragment.findNavController
import android.util.Log
import com.example.korset.ui.details.MovieDetailFragmentDirections
import androidx.fragment.app.activityViewModels // Для получения ViewModel от родителя
import androidx.lifecycle.ViewModelProvider // Можно и так получить родительскую VM
import com.example.korset.ui.main.MainViewModel
import com.example.korset.ui.details.MovieDetailViewModel

class MovieSessionsFragment : Fragment() {

    // ViewBinding
    private var _binding: FragmentMovieSessionsBinding? = null
    private val binding get() = _binding!!

    // Способ 2: Получаем ViewModel родительского фрагмента
    private val movieDetailViewModel: MovieDetailViewModel by lazy {
        ViewModelProvider(requireParentFragment()).get(MovieDetailViewModel::class.java)
    }

    // Адаптеры
    private lateinit var dateAdapter: DateAdapter
    private lateinit var sessionAdapter: SessionAdapter

    // Списки данных
    private var dateList: MutableList<DateItem> = mutableListOf() // Изменяемый список дат
    private var allSessions: List<SessionItem> = emptyList() // Полный список сгенерированных сеансов

    // Текущие выбранные фильтры
    private var selectedDate: Date? = null
    private var selectedTimeFilter: String = "Все" // "Все", "Дневные", "Вечерние"
    private var selectedFormatFilter: String? = null // Например, "IMAX" или null для всех


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieSessionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Генерируем данные
        dateList = generateDates(10).toMutableList() // Генерируем и делаем изменяемым
        allSessions = generateMockSessions(dateList) // Генерируем сеансы

        // Находим выбранную дату по умолчанию (первую)
        selectedDate = dateList.find { it.isSelected }?.date

        // Настраиваем UI
        setupDateRecyclerView()
        setupSessionRecyclerView()
        setupFilterToggleButtons() // Используем новый метод
        setupFilterChips()

        // Отображаем отфильтрованные сеансы
        filterAndDisplaySessions()
    }

    // --- Настройка RecyclerView для Дат ---
    private fun setupDateRecyclerView() {
        dateAdapter = DateAdapter { clickedDateItem, position ->
            handleDateClick(clickedDateItem, position)
        }
        binding.rvDates.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }
        dateAdapter.submitList(dateList) // Передаем сгенерированный список дат
    }

    // --- Изменяем Настройку RecyclerView для Сеансов ---
    private fun setupSessionRecyclerView() {
        sessionAdapter = SessionAdapter { session ->
            // Обработка клика по сеансу
            Toast.makeText(context, "Переход к выбору мест для ${session.time}", Toast.LENGTH_SHORT).show()

            // --- НАВИГАЦИЯ К ВЫБОРУ МЕСТ ---
            try {
                // Получаем текущие детали фильма из ViewModel родителя
                val currentDetails = movieDetailViewModel.movieDetails.value
                if (currentDetails == null) {
                    Log.e("MovieSessionsFragment", "Movie details are null, cannot navigate")
                    Toast.makeText(context, "Не удалось получить детали фильма", Toast.LENGTH_SHORT).show()
                    return@SessionAdapter // Выходим из лямбды, если деталей нет
                }

                // Создаем Action, передавая ВСЕ необходимые аргументы
                val action = MovieDetailFragmentDirections
                    .actionMovieDetailFragmentToSeatSelectionFragment(
                        sessionItem = session, // Передаем выбранный сеанс
                        movieId = currentDetails.id, // <<<=== Передаем ID из деталей
                        movieTitle = currentDetails.title, // <<<=== Передаем Название
                        moviePosterPath = currentDetails.posterPath // <<<=== Передаем Постер
                    )
                // Ищем NavController у родительского фрагмента
                parentFragment?.findNavController()?.navigate(action)
                    ?: Log.e("MovieSessionsFragment", "Parent NavController not found!")

            } catch (e: Exception) {
                Log.e("MovieSessionsFragment", "Navigation failed", e)
                Toast.makeText(context, "Не удалось открыть выбор мест", Toast.LENGTH_SHORT).show()
            }
            // ------------------------------------
        }
        binding.rvSessions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sessionAdapter
        }
        println("MovieSessionsFragment: Session RecyclerView setup done.")
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // --- НОВАЯ ФУНКЦИЯ ДЛЯ КНОПОК-ПЕРЕКЛЮЧАТЕЛЕЙ ---
    private fun setupFilterToggleButtons() {
        // Используем НОВЫЙ ID группы кнопок
        binding.toggleButtonGroupFilterType.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_filter_by_time -> {
                        println("Filter Type: By Time Selected")
                        binding.chipGroupSessionFilters.isVisible = true
                        filterAndDisplaySessions()
                    }
                    R.id.button_filter_by_cinema -> {
                        println("Filter Type: By Cinema Selected")
                        binding.chipGroupSessionFilters.isVisible = false
                        sessionAdapter.submitList(emptyList())
                        binding.rvSessions.isVisible = false
                        binding.tvNoSessions.isVisible = true
                        binding.tvNoSessions.text = "Фильтрация по кинотеатрам пока не реализована"
                    }
                }
            }
        }
        // Устанавливаем начальное состояние
        binding.chipGroupSessionFilters.isVisible = true
        // Убедимся, что кнопка "По времени" выбрана по умолчанию (как в XML)
        // binding.toggleButtonGroupFilterType.check(R.id.button_filter_by_time) // Обычно не нужно, если есть app:checkedButton в XML
    }
// --- КОНЕЦ НОВОЙ ФУНКЦИИ ---

    // --- Настройка Фильтров (ChipGroup) ---
    private fun setupFilterChips() {
        binding.chipGroupSessionFilters.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                // Если ничего не выбрано (маловероятно при singleSelection=true), выбираем "Все"
                group.check(R.id.chip_all)
                return@setOnCheckedStateChangeListener
            }

            val selectedChipId = checkedIds.first() // Берем первый (и единственный) ID
            val chip = group.findViewById<Chip>(selectedChipId)

            // Обновляем текущие фильтры в зависимости от выбранного чипа
            when (chip.id) {
                R.id.chip_day -> selectedTimeFilter = "Дневные"
                R.id.chip_evening -> selectedTimeFilter = "Вечерние"
                R.id.chip_imax -> selectedFormatFilter = "IMAX"
                // Добавь другие форматы
                else -> { // По умолчанию (или R.id.chip_all)
                    selectedTimeFilter = "Все"
                    selectedFormatFilter = null
                }
            }
            println("Filters updated: Time='${selectedTimeFilter}', Format='${selectedFormatFilter}'")
            filterAndDisplaySessions() // Перефильтровываем и отображаем
        }
    }

    // --- Обработка клика по Дате ---
    private fun handleDateClick(clickedDateItem: DateItem, position: Int) {
        // Снимаем выделение со старой даты
        val previousSelectedPosition = dateList.indexOfFirst { it.isSelected }
        if (previousSelectedPosition != -1) {
            dateList[previousSelectedPosition].isSelected = false
            dateAdapter.notifyItemChanged(previousSelectedPosition) // Обновляем старый элемент
        }

        // Устанавливаем выделение для новой даты
        clickedDateItem.isSelected = true
        selectedDate = clickedDateItem.date
        dateAdapter.notifyItemChanged(position) // Обновляем новый элемент

        println("Date selected: ${selectedDate}")
        filterAndDisplaySessions() // Перефильтровываем и отображаем
    }

    // --- Фильтрация и отображение Сеансов ---
    private fun filterAndDisplaySessions() {
        // Фильтруем ПОЛНЫЙ список сеансов по текущим выбранным фильтрам
        val filteredList = allSessions.filter { session ->
            // TODO: Фильтрация по ДАТЕ (если бы она была в SessionItem)
            // val sessionCalendar = Calendar.getInstance().apply { time = session.date }
            // val selectedCalendar = Calendar.getInstance().apply { time = selectedDate }
            // val dateMatches = sessionCalendar.get(Calendar.DAY_OF_YEAR) == selectedCalendar.get(Calendar.DAY_OF_YEAR) &&
            //                   sessionCalendar.get(Calendar.YEAR) == selectedCalendar.get(Calendar.YEAR)
            // if (!dateMatches) return@filter false // Пропускаем, если дата не совпадает

            // Фильтрация по ВРЕМЕНИ (Дневные/Вечерние)
            val hour = session.time.substringBefore(":").toIntOrNull() ?: 0
            val timeMatches = when (selectedTimeFilter) {
                "Дневные" -> hour < 18 // До 18:00
                "Вечерние" -> hour >= 18 // С 18:00
                else -> true // "Все"
            }
            if (!timeMatches) return@filter false

            // Фильтрация по ФОРМАТУ
            val formatMatches = selectedFormatFilter == null || session.format.contains(selectedFormatFilter!!, ignoreCase = true)
            if (!formatMatches) return@filter false

            // Если все фильтры пройдены
            true
        }

        println("Filtered sessions (${filteredList.size}):")
        filteredList.take(5).forEach { println(it) }

        // Передаем отфильтрованный список в адаптер
        sessionAdapter.submitList(filteredList)

        // Показываем/скрываем сообщение "Нет сеансов"
        binding.rvSessions.isVisible = filteredList.isNotEmpty()
        binding.tvNoSessions.isVisible = filteredList.isEmpty()
    }


    // --- Функции генерации данных (остаются без изменений) ---
    private fun generateDates(count: Int): List<DateItem> {
        val dates = ArrayList<DateItem>()
        val calendar = Calendar.getInstance() // Начинаем с текущей даты
        val dayOfWeekFormat = SimpleDateFormat("EE", Locale("ru")) // Формат для дня недели (Пн, Вт...)
        val dayOfMonthFormat = SimpleDateFormat("d", Locale.getDefault()) // Формат для числа

        // Добавляем сегодняшнюю дату первой и делаем ее выбранной
        dates.add(
            DateItem(
                date = calendar.time,
                dayOfWeek = "Сегодня", // Или dayOfWeekFormat.format(calendar.time).uppercase(),
                dayOfMonth = dayOfMonthFormat.format(calendar.time),
                isSelected = true // Первая дата выбрана по умолчанию
            )
        )

        // Генерируем остальные даты
        for (i in 1 until count) {
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Переходим на следующий день
            dates.add(
                DateItem(
                    date = calendar.time,
                    dayOfWeek = dayOfWeekFormat.format(calendar.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }, // "Пн", "Вт"
                    dayOfMonth = dayOfMonthFormat.format(calendar.time),
                    isSelected = false
                )
            )
        }
        return dates
    }
    private fun generateMockSessions(availableDates: List<DateItem>): List<SessionItem> {
        val sessions = ArrayList<SessionItem>()
        val cinemaNames = listOf(
            "Kinopark 8 Moskva",
            "Halyk IMAX Kinopark 16",
            "Kinopark 6 (Спутник)",
            "Chaplin ADK",
            "Lumiera Cinema (ЦУМ)"
        )
        val halls = listOf("Зал 1", "Зал 2", "Зал 3", "Зал 4", "Зал 5", "IMAX Зал", "VIP Зал")
        val formats = listOf("2D", "3D", "IMAX", "2D Atmos")
        val times = listOf(
            "10:30", "11:00", "12:15", "13:00", "14:00", "14:45", "15:30",
            "16:15", "17:00", "18:00", "19:00", "19:40", "20:30", "21:10", "22:00", "23:30"
        )
        val basePrice = 1500 // Базовая цена взрослого билета

        // Для каждой доступной даты генерируем несколько сеансов
        availableDates.forEach { dateItem ->
            // Генерируем случайное количество сеансов для этой даты (от 5 до 15)
            repeat(Random.nextInt(5, 16)) {
                val timeStr = times.random()
                val cinema = cinemaNames.random()
                val hallName = halls.random()
                val formatType = if (hallName.contains("IMAX")) "IMAX" else formats.random()
                val adultPrice = basePrice + Random.nextInt(-2, 5) * 100 // +/- разброс цены
                val studentPrice = adultPrice - Random.nextInt(1, 4) * 100
                val childPrice = studentPrice - Random.nextInt(1, 3) * 100

                sessions.add(
                    SessionItem(
                        date = dateItem.date, // Если бы SessionItem хранил Date
                        time = timeStr,
                        cinemaName = cinema,
                        hall = hallName,
                        format = formatType,
                        priceAdult = adultPrice,
                        priceStudent = if (studentPrice > 500) studentPrice else adultPrice, // Студ. не дешевле 500
                        priceChild = if (childPrice > 400) childPrice else studentPrice // Детский не дешевле 400
                    )
                )
            }
        }

        // Сортируем сеансы по времени для удобства (если SessionItem хранил бы Date)
        // sessions.sortBy { it.time }
        // Пока сортируем строки времени (не идеально, но для имитации сойдет)
        sessions.sortWith(compareBy({ it.date }, { it.time }))

        return sessions
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding
    }
}