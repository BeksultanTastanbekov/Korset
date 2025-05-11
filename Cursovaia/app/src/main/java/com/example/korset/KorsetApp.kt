package com.example.korset // Убедись, что пакет правильный

import android.app.Application
import com.example.korset.data.local.AppDatabase // Импорт твоей БД

class KorsetApp : Application() {

    // Ленивая инициализация базы данных (создается только при первом доступе)
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    // Можно добавить onCreate для другой инициализации, если нужно
    // override fun onCreate() {
    //     super.onCreate()
    //     // ...
    // }
}