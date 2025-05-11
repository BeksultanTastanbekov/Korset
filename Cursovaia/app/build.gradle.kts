import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("androidx.navigation.safeargs.kotlin") version "2.7.7"
    id("kotlin-parcelize")
    kotlin("kapt")
}

// --- НАЧАЛО: Чтение local.properties ---
val localProperties = Properties()
// Получаем путь к файлу local.properties в корне проекта
val localPropertiesFile = rootProject.file("local.properties")

// Проверяем, существует ли файл, и загружаем его
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    // Используем .inputStream().use { ... } для автоматического закрытия потока
    localPropertiesFile.inputStream().use { fileInputStream ->
        localProperties.load(fileInputStream)
    }
}
// --- КОНЕЦ: Чтение local.properties ---

android {
    namespace = "com.example.korset"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.korset"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- НАЧАЛО: Добавление поля в BuildConfig ---
        // Получаем значение ключа из local.properties или ставим заглушку "YOUR_API_KEY"
        val tmdbApiKey = localProperties.getProperty("tmdb.api.key", "local.properties")
        // Добавляем поле TMDB_API_KEY типа String в BuildConfig
        // Значение обязательно должно быть обернуто в кавычки (экранированные)
        buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
        // --- КОНЕЦ: Добавление поля в BuildConfig ---
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.8.0")
    // Room Persistence Library
    val room_version = "2.7.1" // Используй актуальную версию
    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Navigation Component (Kotlin)
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")

    // Paging
    implementation("androidx.paging:paging-runtime-ktx:3.2.1")

    // Стандартные зависимости AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Firebase
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-auth:23.0.0") // Добавлено из предоставленного файла

    // Тестирование
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Retrofit & Gson Converter
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ViewModel and LiveData (Lifecycle KTX)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    // Glide (для загрузки изображений)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Зависимости из libs
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}