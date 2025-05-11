package com.example.korset.data.model

import com.google.gson.annotations.SerializedName

// Модель для детальной информации о фильме
data class MovieDetails(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("overview") val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("runtime") val runtime: Int?, // Продолжительность в минутах
    @SerializedName("genres") val genres: List<Genre>?, // Список жанров
    @SerializedName("credits") val credits: Credits?, // Информация об актерах/команде
    @SerializedName("videos") val videos: VideoResponse? // Информация о трейлерах
    // Добавь другие поля по необходимости (бюджет, сборы, статус и т.д.)
)

// Вспомогательные классы для вложенных данных

data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?
)

data class Credits(
    @SerializedName("cast") val cast: List<CastMember>? // Актеры
    // Можно добавить crew, если нужна съемочная группа
)

data class CastMember(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("character") val character: String?,
    @SerializedName("profile_path") val profilePath: String? // Путь к фото актера
)

data class VideoResponse(
    @SerializedName("results") val results: List<Video>?
)

data class Video(
    @SerializedName("id") val id: String?,
    @SerializedName("key") val key: String?, // Ключ для YouTube
    @SerializedName("name") val name: String?,
    @SerializedName("site") val site: String?, // Обычно "YouTube"
    @SerializedName("type") val type: String? // Например, "Trailer"
)