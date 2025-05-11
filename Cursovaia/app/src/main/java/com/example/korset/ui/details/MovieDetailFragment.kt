package com.example.korset.ui.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible // Для isVisible
import androidx.lifecycle.ViewModelProvider // Для создания ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.korset.R // Для ресурсов
import com.example.korset.data.model.Genre // Для жанров
import com.example.korset.data.model.MovieDetails // Используем новую модель
import com.example.korset.data.network.ApiService // Для URL
import com.example.korset.databinding.FragmentMovieDetailBinding // ViewBinding
import androidx.fragment.app.DialogFragment
import com.google.android.material.chip.Chip
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.example.korset.data.local.FavoriteMovieEntity // Может понадобиться, если будешь явно создавать
import com.example.korset.util.EventObserver // Если используешь Event для статуса (пока не нужно для isFavorite)
import com.example.korset.ui.dialogs.AddToCollectionBottomSheetFragment

class MovieDetailFragment : Fragment() {

    private var _binding: FragmentMovieDetailBinding? = null
    private val binding get() = _binding!!

    private val args: MovieDetailFragmentArgs by navArgs()

    // --- ДОБАВЛЯЕМ ViewModel ---
    private lateinit var viewModel: MovieDetailViewModel
    // -------------------------

    private var youtubeTrailerKey: String? = null // Для хранения ключа трейлера

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieDetailBinding.inflate(inflater, container, false)
        // --- ИЗМЕНЕНИЕ: СОЗДАЕМ ViewModel, ПЕРЕДАВАЯ Application ---
        // ViewModelProvider требует фабрику для AndroidViewModel, если конструктор не пустой.
        // Стандартная AndroidViewModelFactory позаботится об Application.
        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        viewModel = ViewModelProvider(this, factory).get(MovieDetailViewModel::class.java)
        // ----------------------------------------------------------
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movieId = args.movieId
        println("MovieDetailFragment: Received movie ID: $movieId")

        setupToolbar()
        setupPlayButton() // Настройка клика по кнопке Play
        observeViewModel() // Начинаем наблюдать за ViewModel

        if (args.movieId != -1) { // Используем args.movieId
            viewModel.fetchMovieDetails(args.movieId)
            setupActionButtons() // Настраиваем кнопки "Избранное" и "Коллекция"
            setupTabsAndViewPager(args.movieId)
        } else {
            handleInvalidId()
        }

    }

    // --- ИЗМЕНЕНИЕ: Настройка кнопки "В избранное" ---
    private fun setupActionButtons() {
        // Кнопка "В избранное" (остается без изменений)
        binding.buttonFavorite.setOnClickListener { viewModel.toggleFavoriteStatus() }

        // --- ИЗМЕНЕНИЕ: Кнопка "+ Коллекция" ---
        binding.buttonAddToCollection.setOnClickListener {
            println("MovieDetailFragment: Add to Collection button clicked")
            // Показываем наш BottomSheet
            AddToCollectionBottomSheetFragment() // Используем конструктор по умолчанию
                .show(childFragmentManager, AddToCollectionBottomSheetFragment.TAG)
        }
        // --- КОНЕЦ ИЗМЕНЕНИЯ ---
    }


    // --- НОВАЯ ФУНКЦИЯ ДЛЯ НАСТРОЙКИ ВКЛАДОК ---
    private fun setupTabsAndViewPager(currentMovieId: Int) {
        println("MovieDetailFragment: Setting up Tabs and ViewPager for ID: $currentMovieId")
        // Создаем адаптер, передавая текущий фрагмент и movieId
        val pagerAdapter = MovieDetailPagerAdapter(this, currentMovieId)
        // Устанавливаем адаптер для ViewPager2
        binding.viewPagerDetail.adapter = pagerAdapter

        // Связываем TabLayout и ViewPager2
        TabLayoutMediator(binding.tabLayoutDetail, binding.viewPagerDetail) { tab, position ->
            // Устанавливаем текст для каждой вкладки из массива в адаптере
            tab.text = pagerAdapter.tabTitles[position]
        }.attach() // Не забудь вызвать attach()!

        // Опционально: Предотвращаем пересоздание фрагментов при скролле ViewPager
        binding.viewPagerDetail.offscreenPageLimit = pagerAdapter.itemCount
    }
    // --- КОНЕЦ НОВОЙ ФУНКЦИИ ---

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarDetail)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.toolbarDetail.title = " "
        binding.toolbarDetail.setNavigationOnClickListener { findNavController().popBackStack() }
    }

    // Настройка клика по кнопке Play
    private fun setupPlayButton() {
        binding.ivPlayTrailer.setOnClickListener {
            youtubeTrailerKey?.let { key ->
                // Показываем наш диалог с плеером
                YoutubePlayerDialogFragment.newInstance(key)
                    .show(childFragmentManager, YoutubePlayerDialogFragment.TAG)
            } ?: Toast.makeText(context, "Трейлер не найден", Toast.LENGTH_SHORT).show()
        }
    }


    // --- ИЗМЕНЕНИЕ: Наблюдение за ViewModel, включая isFavorite ---
    private fun observeViewModel() {
        viewModel.movieDetails.observe(viewLifecycleOwner) { details ->
            details?.let { bindMovieDetails(it) }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // binding.progressBarDetail.isVisible = isLoading // Если есть ProgressBar
            binding.ivPlayTrailer.isVisible = !isLoading && youtubeTrailerKey != null
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { Toast.makeText(context, "Ошибка: $it", Toast.LENGTH_LONG).show() }
        }

        // --- НОВОЕ: Наблюдаем за статусом "В избранном" ---
        viewModel.isFavorite.observe(viewLifecycleOwner) { isFavorite ->
            updateFavoriteButton(isFavorite) // Обновляем вид кнопки
            println("MovieDetailFragment: isFavorite status updated to: $isFavorite")
        }
        // ----------------------------------------------
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // --- НОВАЯ ФУНКЦИЯ: Обновление вида кнопки "В избранное" ---
    private fun updateFavoriteButton(isFavorite: Boolean) {
        val favoriteIconRes = if (isFavorite) {
            R.drawable.ic_baseline_bookmark_24 // Заполненная иконка
        } else {
            R.drawable.ic_baseline_bookmark_border_24 // Пустая иконка
        }
        binding.buttonFavorite.setIconResource(favoriteIconRes)
        // Текст кнопки не меняем
        // binding.buttonFavorite.text = if (isFavorite) "В избранном" else "В избранное"
        println("MovieDetailFragment: Favorite button icon updated. IsFavorite: $isFavorite")
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---

    // Отображение данных фильма в UI
    private fun bindMovieDetails(details: MovieDetails) {
        println("MovieDetailFragment: Binding movie details: ${details.title}")
        binding.toolbarDetail.title = details.title

        binding.tvMovieTitle.text = details.title

        // --- Обновляем метаданные ---
        val year = details.releaseDate?.substringBefore("-") ?: "----"
        binding.tvMovieYear.text = year

        val runtime = details.runtime?.let { "$it мин." } ?: ""
        binding.tvMovieRuntime.text = runtime
        // Показываем или скрываем TextView времени, если оно есть
        binding.tvMovieRuntime.isVisible = details.runtime != null && details.runtime > 0

        val rating = details.voteAverage?.let { String.format("%.1f", it) } ?: ""
        binding.tvMovieRatingMeta.text = rating
        // Показываем или скрываем рейтинг, если он есть
        binding.tvMovieRatingMeta.isVisible = details.voteAverage != null && details.voteAverage > 0
        // --- Конец обновления метаданных ---


        // --- Динамически добавляем Chip'ы для жанров ---
        binding.chipGroupGenres.removeAllViews() // Очищаем старые Chip'ы
        details.genres?.take(3)?.forEach { genre -> // Берем не больше 3 жанров
            if (!genre.name.isNullOrBlank()) {
                val chip = Chip(context) // Создаем Chip программно
                chip.text = genre.name
                // Можно добавить стиль для Chip из темы
                // chip.setTextAppearance(R.style.ChipTextStyle)
                // chip.setChipBackgroundColorResource(R.color.chip_background_color)
                binding.chipGroupGenres.addView(chip) // Добавляем в ChipGroup
            }
        }
        // Скрываем ChipGroup, если жанров нет
        binding.chipGroupGenres.isVisible = !details.genres.isNullOrEmpty()
        // --- Конец добавления Chip'ов ---

        // Загружаем бэкдроп
        val backdropUrl = details.backdropPath?.let { ApiService.IMAGE_BASE_URL + "w780" + it }
        Glide.with(requireContext())
            .load(backdropUrl)
            .placeholder(R.drawable.logo_icon)
            .error(R.drawable.logo_icon)
            .into(binding.ivMovieBackdrop)

        // Кнопка Play трейлера
        val trailer = details.videos?.results?.find { it.site == "YouTube" && it.type == "Trailer" }
        youtubeTrailerKey = trailer?.key
        binding.ivPlayTrailer.isVisible = youtubeTrailerKey != null

        // TODO: Настроить TabLayout и ViewPager2
        // setupTabsAndViewPager(details.id)
    }


    // Обработка ошибки неверного ID
    private fun handleInvalidId() {
        println("MovieDetailFragment: Error - Invalid movie ID received")
        Toast.makeText(context, "Ошибка: Неверный ID фильма", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }
    

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем binding
        println("MovieDetailFragment: onDestroyView")
    }
}