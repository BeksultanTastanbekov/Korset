package com.example.korset.ui.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels // Для общего ViewModel
import androidx.lifecycle.ViewModelProvider // Если нужна своя ViewModel
import androidx.navigation.fragment.findNavController // Для навигации
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.databinding.FragmentFavoriteMoviesBinding // ViewBinding
import com.example.korset.ui.adapters.FavoriteMovieAdapter // Наш новый адаптер
import com.example.korset.ui.details.MovieDetailViewModel // ViewModel с LiveData
import com.example.korset.ui.favorites.FavoritesFragmentDirections // <<<=== УБЕДИСЬ, ЧТО ОН ЕСТЬ

class FavoriteMoviesFragment : Fragment() {

    private var _binding: FragmentFavoriteMoviesBinding? = null
    private val binding get() = _binding!!

    // Получаем MovieDetailViewModel (так как LiveData favoriteMovies там)
    // Используем activityViewModels, предполагая, что ViewModel привязана к MainActivity
    private val viewModel: MovieDetailViewModel by activityViewModels()

    // Адаптер для списка избранных
    private lateinit var favoriteAdapter: FavoriteMovieAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteMoviesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("FavoriteMoviesFragment: onViewCreated")

        setupRecyclerView()
        observeFavoriteMovies()

        // Данные загружаются автоматически при изменении в БД благодаря Flow/LiveData
    }

    private fun setupRecyclerView() {
        // Создаем наш новый FavoriteMovieAdapter
        favoriteAdapter = FavoriteMovieAdapter { favoriteMovie ->
            // Обработка клика по избранному фильму
            Toast.makeText(context, "Клик: ${favoriteMovie.title}", Toast.LENGTH_SHORT).show()
            navigateToMovieDetails(favoriteMovie.movieId) // Вызываем функцию навигации
        }

        binding.rvFavoriteMovies.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = favoriteAdapter // Устанавливаем адаптер
        }
        println("FavoriteMoviesFragment: RecyclerView setup done.")
    }

    // Функция для выполнения навигации
    private fun navigateToMovieDetails(movieId: Int) {
        try {
            // Используем NavController РОДИТЕЛЬСКОГО фрагмента (FavoritesFragment)
            // и Action, который мы добавили в nav_graph.xml
            val action = FavoritesFragmentDirections
                .actionNavFavoritesToMovieDetailFragment(movieId = movieId)
            // Ищем NavController у родителя и переходим
            requireParentFragment().findNavController().navigate(action)
        } catch (e: IllegalStateException) {
            // Ошибка может возникнуть, если фрагмент еще не присоединен к графу
            Log.e("FavoriteMoviesNav", "Navigation failed: Fragment not attached?", e)
            Toast.makeText(context, "Не удалось открыть детали", Toast.LENGTH_SHORT).show()
        } catch (e: IllegalArgumentException) {
            // Ошибка может возникнуть, если action не найден в текущем destination
            Log.e("FavoriteMoviesNav", "Navigation failed: Action not found?", e)
            Toast.makeText(context, "Ошибка навигации", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FavoriteMoviesNav", "Navigation failed: Unknown error", e)
            Toast.makeText(context, "Неизвестная ошибка", Toast.LENGTH_SHORT).show()
        }
    }


    private fun observeFavoriteMovies() {
        // Наблюдаем за LiveData<List<FavoriteMovieEntity>> из ViewModel
        viewModel.favoriteMovies.observe(viewLifecycleOwner) { favMovies ->
            println("FavoriteMoviesFragment: Observed ${favMovies?.size ?: "null"} favorite movies.")
            val hasFavorites = !favMovies.isNullOrEmpty()
            binding.tvNoFavorites.isVisible = !hasFavorites // Показываем заглушку, если список пуст
            binding.rvFavoriteMovies.isVisible = hasFavorites // Показываем список, если не пуст

            // Передаем список в адаптер (DiffUtil сам разберется с изменениями)
            favoriteAdapter.submitList(favMovies)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFavoriteMovies.adapter = null // Очищаем адаптер у RecyclerView
        _binding = null
        println("FavoriteMoviesFragment: onDestroyView")
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteMoviesFragment()
    }
}