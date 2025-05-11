package com.example.korset.ui.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels // Используем общую ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.R
import com.example.korset.databinding.FragmentCollectionDetailBinding // ViewBinding
import com.example.korset.ui.adapters.FavoriteMovieAdapter // Адаптер для фильмов
import com.example.korset.ui.details.MovieDetailViewModel // ViewModel
import com.example.korset.data.local.FavoriteMovieEntity

class CollectionDetailFragment : Fragment() {

    private var _binding: FragmentCollectionDetailBinding? = null
    private val binding get() = _binding!!

    // Получаем аргументы
    private val args: CollectionDetailFragmentArgs by navArgs()

    // Получаем ViewModel
    private val viewModel: MovieDetailViewModel by activityViewModels()

    // Адаптер для фильмов в коллекции
    private lateinit var movieAdapter: FavoriteMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val collectionId = args.collectionId
        println("CollectionDetailFragment: onViewCreated for collection ID $collectionId")

        setupToolbar()
        setupRecyclerView()
        observeCollectionDetails()

        // Запрашиваем детали коллекции
        viewModel.loadCollectionDetails(collectionId)
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarCollectionDetail)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbarCollectionDetail.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        movieAdapter = FavoriteMovieAdapter { movie ->
            // Клик по фильму внутри коллекции - переход на детали фильма
            try {
                val action = CollectionDetailFragmentDirections
                    .actionCollectionDetailFragmentToMovieDetailFragment(movieId = movie.movieId)
                findNavController().navigate(action)
            } catch (e: Exception) {
                Log.e("CollectionDetailNav", "Navigation to Movie Detail failed", e)
            }
        }
        binding.rvCollectionMovies.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = movieAdapter
        }
    }

    private fun observeCollectionDetails() {
        viewModel.selectedCollectionDetails.observe(viewLifecycleOwner) { collectionWithMovies ->
            if (collectionWithMovies != null) {
                println("CollectionDetailFragment: Observed details for collection '${collectionWithMovies.collection.name}'")
                binding.toolbarCollectionDetail.title = collectionWithMovies.collection.name

                // --- НАЧАЛО ИЗМЕНЕНИЯ ---
                val cachedMovies = collectionWithMovies.movies // Это List<MovieCacheEntity>

                // Преобразуем List<MovieCacheEntity> в List<FavoriteMovieEntity>
                val favoriteMovieEntities = cachedMovies.map { cacheEntity ->
                    FavoriteMovieEntity(
                        movieId = cacheEntity.movieId,
                        title = cacheEntity.title,
                        posterPath = cacheEntity.posterPath,
                        backdropPath = cacheEntity.backdropPath, // Убедитесь, что FavoriteMovieEntity имеет это поле
                        overview = cacheEntity.overview,
                        releaseDate = cacheEntity.releaseDate,
                        voteAverage = cacheEntity.voteAverage
                        // Другие поля, если они есть в FavoriteMovieEntity и нужны адаптеру,
                        // но отсутствуют в MovieCacheEntity, можно оставить по умолчанию или null.
                    )
                }
                movieAdapter.submitList(favoriteMovieEntities) // Передаем преобразованный список
                // --- КОНЕЦ ИЗМЕНЕНИЯ ---

                binding.tvNoMoviesInCollection.isVisible = favoriteMovieEntities.isEmpty()
                binding.rvCollectionMovies.isVisible = favoriteMovieEntities.isNotEmpty()
            } else {
                println("CollectionDetailFragment: Observed null collection details")
                binding.toolbarCollectionDetail.title = "Коллекция не найдена"
                movieAdapter.submitList(emptyList())
                binding.tvNoMoviesInCollection.isVisible = true
                binding.tvNoMoviesInCollection.text = "Не удалось загрузить коллекцию"
                binding.rvCollectionMovies.isVisible = false
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        // Обнуляем LiveData в ViewModel, чтобы при следующем открытии не показать старые данные
        viewModel.clearSelectedCollectionDetails() // <<<=== НУЖНО ДОБАВИТЬ ЭТОТ МЕТОД В ViewModel
        binding.rvCollectionMovies.adapter = null
        _binding = null
        println("CollectionDetailFragment: onDestroyView")
    }

    // Добавляем newInstance для единообразия
    companion object {
        @JvmStatic
        fun newInstance(collectionId: Int): CollectionDetailFragment {
            val fragment = CollectionDetailFragment()
            fragment.arguments = Bundle().apply {
                putInt("collectionId", collectionId)
            }
            return fragment
        }
    }
}