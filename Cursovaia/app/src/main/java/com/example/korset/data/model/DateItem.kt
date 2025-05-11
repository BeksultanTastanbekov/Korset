package com.example.korset.data.model // или другой пакет
import java.util.Date
data class DateItem(val date: Date, val dayOfWeek: String, val dayOfMonth: String, var isSelected: Boolean = false)