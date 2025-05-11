package com.example.korset.ui.adapters // Убедись, что пакет правильный

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible // Для удобного управления видимостью
import androidx.recyclerview.widget.RecyclerView
import com.example.korset.data.model.City // Импорт модели
import com.example.korset.databinding.ListItemCityBinding // Импорт ViewBinding для макета строки

// Адаптер принимает:
// 1. Изначальный полный список городов (allCities)
// 2. ID текущего выбранного города (чтобы показать галочку)
// 3. Лямбда-функцию (колбэк), которая будет вызвана при клике на город
class CityAdapter(
    private val allCities: List<City>,
    private var selectedCityId: Int,
    private val onCityClicked: (City) -> Unit // Функция, вызываемая при клике
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    // Список, который будет отображаться (может меняться при поиске)
    private var filteredCities: List<City> = allCities

    // ViewHolder: Хранит ссылки на элементы интерфейса одной строки (из list_item_city.xml)
    inner class CityViewHolder(private val binding: ListItemCityBinding) : RecyclerView.ViewHolder(binding.root) {

        // Функция для "привязки" данных города к элементам строки
        fun bind(city: City) {
            binding.textViewCityName.text = city.name // Устанавливаем название города

            // Показываем галочку, если ID этого города совпадает с выбранным ID
            binding.imageViewCheckmark.isVisible = (city.id == selectedCityId)

            // Устанавливаем слушатель клика на всю строку
            binding.root.setOnClickListener {
                // Вызываем переданную нам лямбда-функцию, передавая в нее нажатый город
                onCityClicked(city)
            }
        }
    }

    // Вызывается, когда RecyclerView нужен новый ViewHolder (новая строка)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        // "Надуваем" (инфлейтим) макет list_item_city.xml с помощью ViewBinding
        val binding = ListItemCityBinding.inflate(
            LayoutInflater.from(parent.context), // Берем LayoutInflater из контекста родителя
            parent, // Родительский ViewGroup
            false // Не прикреплять к родителю сразу
        )
        // Создаем и возвращаем ViewHolder, передавая ему binding
        return CityViewHolder(binding)
    }

    // Вызывается, чтобы отобразить данные для конкретной позиции (строки)
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        // Получаем город из отфильтрованного списка по текущей позиции
        val city = filteredCities[position]
        // Вызываем метод bind у ViewHolder'а, чтобы он заполнил строку данными
        holder.bind(city)
    }

    // Возвращает количество элементов в *отфильтрованном* списке
    override fun getItemCount(): Int {
        return filteredCities.size
    }

    // --- Вспомогательные функции ---

    // Функция для обновления ID выбранного города (например, если он изменился извне)
    // и перерисовки списка, чтобы галочка обновилась
    @SuppressLint("NotifyDataSetChanged") // Внимание: не самый эффективный способ обновления
    fun updateSelection(newSelectedCityId: Int) {
        selectedCityId = newSelectedCityId
        // Говорим адаптеру, что все данные изменились (чтобы перерисовался весь список)
        // Для простоты используем notifyDataSetChanged, хотя ListAdapter был бы лучше.
        notifyDataSetChanged()
    }

    // Функция для фильтрации списка по поисковому запросу
    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String?) {
        // Если запрос пустой или null, показываем все города
        if (query.isNullOrBlank()) {
            filteredCities = allCities
        } else {
            // Иначе фильтруем полный список:
            // Приводим запрос к нижнему регистру для поиска без учета регистра
            val lowerCaseQuery = query.lowercase()
            // Оставляем только те города, чье имя (в нижнем регистре) содержит текст запроса
            filteredCities = allCities.filter { city ->
                city.name.lowercase().contains(lowerCaseQuery)
            }
        }
        // Сообщаем адаптеру, что данные изменились, чтобы RecyclerView обновился
        notifyDataSetChanged()
    }
}