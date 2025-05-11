package com.example.korset.ui.dialogs // Убедись, что пакет правильный

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener // Для отслеживания текста в EditText
import androidx.fragment.app.activityViewModels // Для получения общей ViewModel
import androidx.recyclerview.widget.LinearLayoutManager // Для RecyclerView
import com.example.korset.data.local.CitiesDataSource // Импорт источника данных
import com.example.korset.databinding.BottomSheetCitySelectionBinding // Импорт ViewBinding для макета шторки
import com.example.korset.ui.adapters.CityAdapter // Импорт нашего адаптера
import com.example.korset.ui.main.MainViewModel // Импорт общей ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment // Наследуемся от него

// Наследуемся от BottomSheetDialogFragment, чтобы получить поведение шторки
class CitySelectionBottomSheetFragment : BottomSheetDialogFragment() {

    // ViewBinding для доступа к элементам макета bottom_sheet_city_selection.xml
    private var _binding: BottomSheetCitySelectionBinding? = null
    // Эта переменная доступна только между onCreateView и onDestroyView
    private val binding get() = _binding!!

    // Получаем экземпляр MainViewModel, который используется совместно с MainActivity и другими фрагментами
    private val mainViewModel: MainViewModel by activityViewModels()

    // Экземпляр нашего адаптера для списка городов
    private lateinit var cityAdapter: CityAdapter
    // Экземпляр источника данных для получения списка городов
    private val citiesDataSource = CitiesDataSource()

    // Вызывается для создания View фрагмента
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // "Надуваем" макет шторки с помощью ViewBinding
        _binding = BottomSheetCitySelectionBinding.inflate(inflater, container, false)
        // Возвращаем корневой элемент макета
        return binding.root
    }

    // Вызывается после того, как View фрагмента создано
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Настраиваем RecyclerView, поиск и кнопку закрытия
        setupRecyclerView()
        setupSearch()
        setupCloseButton()
    }

    // Функция для настройки RecyclerView и Адаптера
    private fun setupRecyclerView() {
        // Получаем текущий выбранный город из ViewModel (или город по умолчанию, если ViewModel еще не инициализирована)
        val currentSelectedCityId = mainViewModel.selectedCity.value?.id ?: citiesDataSource.getDefaultCity().id

        // Создаем адаптер:
        cityAdapter = CityAdapter(
            allCities = citiesDataSource.getCities(), // Передаем полный список городов
            selectedCityId = currentSelectedCityId,   // Передаем ID текущего выбранного города
            onCityClicked = { selectedCity ->         // Передаем лямбду, которая выполнится при клике
                mainViewModel.setSelectedCity(selectedCity) // Обновляем город в ViewModel
                dismiss() // Закрываем шторку (BottomSheet)
            }
        )

        // Настраиваем RecyclerView:
        binding.recyclerViewCities.apply {
            // Используем LinearLayoutManager для вертикального списка
            layoutManager = LinearLayoutManager(context)
            // Устанавливаем наш созданный адаптер
            adapter = cityAdapter
        }
    }

    // Функция для настройки поля поиска
    private fun setupSearch() {
        binding.editTextSearchCity.addTextChangedListener { text ->
            // При любом изменении текста в поле поиска, вызываем метод filter у адаптера
            cityAdapter.filter(text?.toString())
        }
    }

    // Функция для настройки кнопки закрытия
    private fun setupCloseButton() {
        binding.buttonCloseSheet.setOnClickListener {
            dismiss() // Просто закрываем шторку при клике на крестик
        }
    }

    // Вызывается при уничтожении View фрагмента (например, когда шторка закрывается)
    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ссылку на binding, чтобы избежать утечек памяти
        _binding = null
    }

    // --- Компаньон для создания экземпляра фрагмента (хорошая практика) ---
    companion object {
        // Тег для идентификации фрагмента (например, в FragmentManager)
        const val TAG = "CitySelectionBottomSheet"

        // Фабричный метод для создания нового экземпляра фрагмента
        fun newInstance(): CitySelectionBottomSheetFragment {
            return CitySelectionBottomSheetFragment()
        }
    }
}