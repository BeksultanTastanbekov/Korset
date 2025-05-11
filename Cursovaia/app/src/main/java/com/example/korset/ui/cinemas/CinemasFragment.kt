// CinemasFragment.kt
package com.example.korset.ui.cinemas

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView // <<<=== ДОБАВИТЬ ИМПОРТ
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController // <<<=== ДОБАВИТЬ ИМПОРТ ДЛЯ НАВИГАЦИИ
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.R // <<<=== УБЕДИТЕСЬ, ЧТО ЭТОТ ИМПОРТ ЕСТЬ
import com.example.korset.data.model.Cinema
import com.example.korset.databinding.FragmentCinemasBinding
import com.example.korset.ui.adapters.CinemaAdapter
import com.example.korset.ui.dialogs.CitySelectionBottomSheetFragment // <<<=== ДОБАВИТЬ ИМПОРТ
import com.example.korset.ui.main.MainViewModel

class CinemasFragment : Fragment() {

    private var _binding: FragmentCinemasBinding? = null
    private val binding get() = _binding!!

    private var tvCurrentCityInHeader: TextView? = null
    // --- ИЗМЕНЕНИЕ: Используем конкретный тип MaterialButton, если знаем его ---
    // Или оставим View, если citySelectorButton_header - это ConstraintLayout
    private var citySelectorButtonInHeader: com.google.android.material.button.MaterialButton? = null
    private var ivMyTicketsInHeader: ImageView? = null
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    private lateinit var cinemaAdapter: CinemaAdapter
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCinemasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Находим View из включенного макета
        // ID берем из layout_main_header.xml
        val headerRootView = binding.mainHeaderCinemas.root
        tvCurrentCityInHeader = headerRootView.findViewById(R.id.citySelectorButton_header)
        citySelectorButtonInHeader = headerRootView.findViewById(R.id.citySelectorButton_header) // Это MaterialButton
        ivMyTicketsInHeader = headerRootView.findViewById(R.id.iv_my_tickets_header)

        setupRecyclerView()
        setupHeader() // Теперь эта функция будет работать с правильными View
        loadCinemas()

        mainViewModel.selectedCity.observe(viewLifecycleOwner) { city ->
            tvCurrentCityInHeader?.text = city.name
        }
    }

    private fun setupHeader() {
        tvCurrentCityInHeader?.text = mainViewModel.selectedCity.value?.name ?: "Город"

        // Устанавливаем слушатель на кнопку выбора города
        citySelectorButtonInHeader?.setOnClickListener {
            // Открываем диалог выбора города (такой же, как в HomeFragment)
            CitySelectionBottomSheetFragment.newInstance().show(childFragmentManager, CitySelectionBottomSheetFragment.TAG)
            // Если у вас другая логика или другой диалог, адаптируйте
        }

        // Устанавливаем слушатель на иконку "Мои билеты"
        ivMyTicketsInHeader?.setOnClickListener {
            // Переход на экран "Мои билеты"
            // Убедитесь, что action_global_to_myTicketsFragment существует в вашем nav_graph.xml
            try {
                findNavController().navigate(R.id.action_global_to_myTicketsFragment)
            } catch (e: Exception) {
                Toast.makeText(context, "Не удалось открыть Мои билеты", Toast.LENGTH_SHORT).show()
                // Логирование ошибки e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerView() {
        cinemaAdapter = CinemaAdapter { cinema ->
            Toast.makeText(context, "Выбран кинотеатр: ${cinema.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvCinemasList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cinemaAdapter
        }
    }

    private fun loadCinemas() {
        val dummyCinemas = listOf(
            Cinema(1, "Kinopark 11 (Esentai) IMAX", "пр. Аль-Фараби, 77/8, ТЦ «Esentai Mall»", logoUrl = "ваша_статическая_картинка_если_используете_ее_в_Cinema_модели"),
            Cinema(2, "Kinopark 8 Moskva", "пр. Достык, уг. ул. Сатпаева, ТРЦ «Moskva Metropolitan»"),
            Cinema(3, "Chaplin MEGA Alma-Ata", "ул. Розыбакиева, 247А, ТРЦ «MEGA Alma-Ata»"),
            Cinema(4, "Kinoforum 10 (ТРЦ Forum)", "ул. Сейфуллина, 617, ТРЦ «Forum Almaty»"),
            Cinema(5, "Lumiera Cinema (ЦУМ)", "пр. Абылай хана, 62, ЦУМ «Зангар»")
        )

        if (dummyCinemas.isNotEmpty()) {
            cinemaAdapter.submitList(dummyCinemas)
            binding.rvCinemasList.isVisible = true
            binding.tvNoCinemasPlaceholder.isVisible = false
        } else {
            binding.rvCinemasList.isVisible = false
            binding.tvNoCinemasPlaceholder.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCinemasList.adapter = null
        tvCurrentCityInHeader = null
        citySelectorButtonInHeader = null
        ivMyTicketsInHeader = null
        _binding = null
    }
}