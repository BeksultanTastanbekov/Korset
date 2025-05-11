package com.example.korset.data.local

import com.example.korset.data.model.City // Убедись, что импорт есть

class CitiesDataSource {

    // Функция, которая возвращает список всех городов
    fun getCities(): List<City> {
        return listOf(
            City(1, "Алматы"), City(2, "Астана"), City(3, "Шымкент"),
            City(4, "Актау"), City(5, "Актобе"), City(6, "Атырау"),
            City(7, "Караганда"), City(8, "Костанай"), City(9, "Кызылорда"),
            City(10, "Павлодар"), City(11, "Петропавловск"), City(12, "Семей"),
            City(13, "Талдыкорган"), City(14, "Тараз"), City(15, "Туркестан"),
            City(16, "Уральск"), City(17, "Усть-Каменогорск"), City(18, "Рудный"),
            City(19, "Сатпаев"), City(20, "Степногорск"), City(21, "Талгар"),
            City(22, "Темиртау"), City(23, "Жезказган"), City(24, "Жанаозен"),
            City(25, "Бейнеу"), City(26, "Балхаш"), City(27, "Алаколь"),
            City(28, "Аксай")
        ).sortedBy { it.name } // Сортируем по имени для удобства
    }

    // Функция, возвращающая город по умолчанию
    fun getDefaultCity(): City {
        return City(1, "Алматы") // Алматы - город по умолчанию
    }
}