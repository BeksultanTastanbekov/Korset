package com.example.korset.data.model

import com.google.gson.annotations.SerializedName

data class MovieListResponse(
    // @SerializedName("page") val page: Int, // Пока не используем, можно закомментировать
    @SerializedName("results") val results: List<Movie>?, // Список фильмов
    @SerializedName("page") val page: Int,
    @SerializedName("total_pages") val totalPages: Int,   // <<<=== УБЕДИСЬ, ЧТО ЭТО ПОЛЕ ЕСТЬ И ТАК НАЗВАНО
    @SerializedName("total_results") val totalResults: Int
    // @SerializedName("total_pages") val totalPages: Int, // Пока не используем
    // @SerializedName("total_results") val totalResults: Int // Пока не используем
)