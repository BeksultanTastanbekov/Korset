package com.example.korset.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient // Опционально, для логгирования или Interceptor
import okhttp3.logging.HttpLoggingInterceptor // Опционально, для логгирования

object RetrofitClient {

    // Опционально: Настройка логгирования сетевых запросов (очень полезно для отладки)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Логгировать тело запроса/ответа
    }

    // Опционально: Создание OkHttpClient с логгированием
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Добавляем логгер
        // Здесь можно добавить Interceptor для автоматического добавления API ключа ко всем запросам
        // .addInterceptor { chain -> ... }
        .build()

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(ApiService.BASE_URL) // Используем базовый URL из интерфейса
            .client(okHttpClient) // Используем настроенный OkHttpClient (опционально)
            .addConverterFactory(GsonConverterFactory.create()) // Конвертер JSON
            .build()

        retrofit.create(ApiService::class.java) // Создаем реализацию ApiService
    }
}