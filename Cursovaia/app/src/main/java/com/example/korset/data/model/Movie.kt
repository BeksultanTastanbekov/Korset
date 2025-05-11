package com.example.korset.data.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id") val id: Int,

    // Название (для фильмов И сериалов, API использует разные ключи)
    @SerializedName("title") val title: String?,
    @SerializedName("name") val name: String?, // <-- ДОБАВЛЕНО для имени сериала
    @SerializedName("backdrop_path") val backdropPath: String?, // <<<=== ЭТО ПОЛЕ ДОЛЖНО БЫТЬ
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("vote_count") val voteCount: Int?, // <-- ДОБАВЛЕНО для фильтрации

    // Дата релиза (для фильмов И сериалов)
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String? // <-- ДОБАВЛЕНО для даты сериала
    // Добавьте другие поля, если они понадобятся (например, genre_ids)
)