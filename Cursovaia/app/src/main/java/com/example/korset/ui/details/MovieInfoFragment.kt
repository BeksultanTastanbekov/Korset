package com.example.korset.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible // <<<=== ДОБАВЛЕН ИМПОРТ
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.databinding.FragmentMovieInfoBinding // ViewBinding

class MovieInfoFragment : Fragment() {

    private var _binding: FragmentMovieInfoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovieDetailViewModel by lazy {
        if (!isAdded || parentFragment == null) {
            throw IllegalStateException("MovieInfoFragment должен быть внутри другого фрагмента")
        }
        ViewModelProvider(requireParentFragment()).get(MovieDetailViewModel::class.java)
    }

    private lateinit var castAdapter: CastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("MovieInfoFragment: onViewCreated")
        setupCastRecyclerView()
        observeMovieDetails()
    }

    private fun setupCastRecyclerView() {
        castAdapter = CastAdapter()
        binding.rvCast.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCast.adapter = castAdapter
        println("MovieInfoFragment: Cast RecyclerView setup done.")
    }

    private fun observeMovieDetails() {
        viewModel.movieDetails.observe(viewLifecycleOwner) { details ->
            println("MovieInfoFragment: Observed movie details update.")
            details?.let {
                println("MovieInfoFragment: Binding overview and cast.")
                binding.tvInfoOverview.text = it.overview ?: "Описание отсутствует."
                castAdapter.submitList(it.credits?.cast ?: emptyList())
                // Используем isVisible после импорта
                binding.tvInfoOverview.isVisible = !it.overview.isNullOrBlank()
                binding.rvCast.isVisible = !it.credits?.cast.isNullOrEmpty()
                // TODO: Отобразить режиссера и другие детали
            } ?: run {
                println("MovieInfoFragment: Movie details are null.")
                binding.tvInfoOverview.text = "Не удалось загрузить описание."
                castAdapter.submitList(emptyList())
                // Используем isVisible после импорта
                binding.tvInfoOverview.isVisible = true
                binding.rvCast.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCast.adapter = null
        _binding = null
        println("MovieInfoFragment: onDestroyView")
    }
}