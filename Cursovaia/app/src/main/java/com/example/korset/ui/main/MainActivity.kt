package com.example.korset.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment // <<<=== НОВЫЙ ИМПОРТ
import androidx.navigation.ui.setupWithNavController // <<<=== НОВЫЙ ИМПОРТ
import com.example.korset.R // R нужен для ID контейнера
import com.example.korset.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- НАСТРОЙКА NAVIGATION COMPONENT ---
        // Находим NavHostFragment (контейнер из activity_main.xml)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Получаем NavController, который управляет фрагментами внутри NavHostFragment
        val navController = navHostFragment.navController

        // Связываем BottomNavigationView с NavController
        // Теперь клики по нижнему меню будут автоматически переключать
        // фрагменты в nav_host_fragment согласно ID в меню и графе
        binding.bottomNavigationView.setupWithNavController(navController)
        // -------------------------------------

        // Старый код с replaceFragment и setOnItemSelectedListener больше не нужен
    }

    // Функция replaceFragment больше не нужна
}