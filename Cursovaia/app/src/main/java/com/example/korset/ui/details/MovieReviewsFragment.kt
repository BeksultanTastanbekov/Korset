package com.example.korset.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels // Используем общий ViewModel
import androidx.lifecycle.ViewModelProvider // Для получения ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.data.model.Review // Модель отзыва
import com.example.korset.databinding.FragmentMovieReviewsBinding // ViewBinding
import com.google.firebase.auth.FirebaseAuth // <<<=== Импорт


class MovieReviewsFragment : Fragment() {

    private var _binding: FragmentMovieReviewsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    // Получаем ОБЩИЙ ViewModel от родителя
    private val viewModel: MovieDetailViewModel by lazy {
        ViewModelProvider(requireParentFragment()).get(MovieDetailViewModel::class.java)
    }

    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieReviewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // --- ПРОВЕРКА ПОЛЬЗОВАТЕЛЯ ПРИ ВХОДЕ ---
        if (auth.currentUser == null) {
            // Пользователь не вошел
            binding.buttonWriteReview.isEnabled = false // Делаем кнопку неактивной
            binding.buttonWriteReview.text = "Войдите, чтобы оставить отзыв"
            // Можно скрыть и RatingBar
            binding.ratingBarMovie.visibility = View.GONE
        } else {
            // Пользователь вошел - все кнопки доступны
            binding.buttonWriteReview.isEnabled = true
            binding.ratingBarMovie.visibility = View.VISIBLE
        }
        // ----------------------------------------

        setupReviewRecyclerView()
        setupWriteReviewButton() // Логика показа/скрытия поля ввода остается
        setupPublishButton()     // Проверка на currentUser теперь внутри
        observeReviews()
        loadMockReviews()
    }


    private fun setupReviewRecyclerView() {
        reviewAdapter = ReviewAdapter()
        binding.rvReviews.layoutManager = LinearLayoutManager(context)
        binding.rvReviews.adapter = reviewAdapter
        binding.rvReviews.isNestedScrollingEnabled = false // Отключаем свою прокрутку
    }

    // Показываем/скрываем поле ввода отзыва
    private fun setupWriteReviewButton() {
        binding.buttonWriteReview.setOnClickListener {
            val isInputVisible = binding.inputLayoutReview.isVisible
            binding.inputLayoutReview.isVisible = !isInputVisible
            binding.buttonPublishReview.isVisible = !isInputVisible
            binding.buttonWriteReview.text = if (isInputVisible) "Написать рецензию" else "Отмена"
        }
    }

    // Обработка кнопки Опубликовать
    private fun setupPublishButton() {
        binding.editTextReview.addTextChangedListener { text ->
            binding.buttonPublishReview.isEnabled = (text?.length ?: 0) >= 10
        }

        // В MovieReviewsFragment.kt, внутри setupPublishButton()

        binding.buttonPublishReview.setOnClickListener {
            // --- ПРОВЕРЯЕМ ПОЛЬЗОВАТЕЛЯ ---
            val currentUser = auth.currentUser
            if (currentUser == null) {
                // Пользователь не аутентифицирован!
                Toast.makeText(context, "Для публикации отзыва нужно войти", Toast.LENGTH_LONG).show()
                // Можно перенаправить на логин, но лучше дать пользователю решить
                // startActivity(Intent(activity, LoginActivity::class.java))
                return@setOnClickListener // Выходим из обработчика
            }
            // -------------------------------

            val rating = binding.ratingBarMovie.rating
            val reviewText = binding.editTextReview.text.toString()
            // Теперь безопасно получаем email
            val userEmail = currentUser.email ?: "Аноним" // Используем "Аноним", если email вдруг null

            // TODO: Отправить/сохранить отзыв
            Toast.makeText(context, "Отзыв от $userEmail отправлен! Оценка: $rating", Toast.LENGTH_SHORT).show()

            // Скрываем поле ввода
            binding.inputLayoutReview.isVisible = false
            binding.buttonPublishReview.isVisible = false
            binding.buttonWriteReview.text = "Написать рецензию"
            binding.editTextReview.text = null
            binding.ratingBarMovie.rating = 0f
        }
    }

    // Наблюдение за отзывами (пока не используется, так как отзывы имитируем)
    private fun observeReviews() {
        // viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
        //     reviewAdapter.submitList(reviews)
        //     binding.tvNoReviews.isVisible = reviews.isNullOrEmpty()
        // }
    }

    // Имитация загрузки отзывов
    private fun loadMockReviews() {
        val mockReviews = listOf(
            Review("Пользователь 1", "29 апреля 2025", "Отличный фильм, всем советую!"),
            Review("Критик", "28 апреля 2025", "Сюжет неплох, но игра актеров оставляет желать лучшего."),
            Review("Аноним", "27 апреля 2025", "Не понравилось.")
        )
        reviewAdapter.submitList(mockReviews)
        binding.tvNoReviews.isVisible = mockReviews.isEmpty()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvReviews.adapter = null
        _binding = null
    }
}