package com.example.korset.ui.favorites

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.databinding.FragmentCollectionsBinding // ViewBinding
import com.example.korset.ui.adapters.CollectionListAdapter // Адаптер коллекций
import com.example.korset.ui.details.MovieDetailViewModel // ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder // Для диалога создания
import com.example.korset.util.EventObserver // <<<=== ПРОВЕРЬ ЭТОТ ИМПОРТ
import androidx.navigation.fragment.findNavController // Добавь, если нет
import com.example.korset.ui.favorites.FavoritesFragmentDirections // Импорт от родителя
import com.example.korset.data.local.CollectionEntity


class CollectionsFragment : Fragment() {

    private var _binding: FragmentCollectionsBinding? = null
    private val binding get() = _binding!!

    // Используем MovieDetailViewModel, так как он содержит userCollectionsWithMovies
    private val viewModel: MovieDetailViewModel by activityViewModels()
//    private lateinit var collectionAdapter: CollectionListAdapter
// Инициализируем адаптер позже, в setupRecyclerView
private val collectionAdapter: CollectionListAdapter by lazy {
    CollectionListAdapter (
        onCollectionClick = { collectionWithMovies ->
            // Переход к деталям коллекции
            navigateToCollectionDetail(collectionWithMovies.collection.collectionId)
        },
        onDeleteClick = { collectionEntity ->
            // Показываем диалог подтверждения удаления
            showDeleteConfirmationDialog(collectionEntity)
        }
    )
}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("CollectionsFragment: onViewCreated")

        setupRecyclerView()
        setupCreateButton()
        observeCollections()
        setupRecyclerView()
        observeOperationStatus() // Наблюдаем за статусом создания/добавления
    }

    private fun setupRecyclerView() {
        binding.rvCollectionsList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectionAdapter // Устанавливаем адаптер, созданный через lazy
        }
        println("CollectionsFragment: RecyclerView setup.")
    }

    // --- НОВАЯ ФУНКЦИЯ: Показ диалога подтверждения удаления ---
    private fun showDeleteConfirmationDialog(collection: CollectionEntity) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить коллекцию?")
            .setMessage("Вы уверены, что хотите удалить коллекцию \"${collection.name}\"? Это действие нельзя отменить.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteCollection(collection) // Вызываем метод ViewModel
            }
            .show()
    }
    // ---------------------------------------------------------

    // --- НОВАЯ ФУНКЦИЯ: Навигация к деталям коллекции ---
    private fun navigateToCollectionDetail(collectionId: Int) {
        try {
            val action = FavoritesFragmentDirections
                .actionNavFavoritesToCollectionDetailFragment(collectionId)
            requireParentFragment().findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e("CollectionsFragment", "Navigation to Collection Detail failed", e)
            Toast.makeText(context, "Не удалось открыть коллекцию", Toast.LENGTH_SHORT).show()
        }
    }
    // -------------------------------------------------

    // Настройка кнопки "Создать коллекцию"
    private fun setupCreateButton() {
        binding.buttonCreateNewCollection.setOnClickListener {
            showCreateCollectionDialog()
        }
    }

    // Показ диалога для создания новой коллекции
    private fun showCreateCollectionDialog() {
        val editText = EditText(requireContext())
        editText.hint = "Название новой коллекции"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Создать коллекцию")
            .setView(editText) // Добавляем поле ввода в диалог
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Создать") { _, _ ->
                val name = editText.text.toString().trim()
                if (name.isNotEmpty()) {
                    viewModel.createNewCollection(name) // Вызываем метод ViewModel
                } else {
                    Toast.makeText(context, "Название не может быть пустым", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    // Наблюдение за списком коллекций
    private fun observeCollections() {
        viewModel.userCollectionsWithMovies.observe(viewLifecycleOwner) { collections ->
            println("CollectionsFragment: Observed ${collections?.size ?: "null"} collections.")
            val hasCollections = !collections.isNullOrEmpty()
            binding.rvCollectionsList.isVisible = hasCollections
            binding.tvNoCollectionsPlaceholder.isVisible = !hasCollections
            collectionAdapter.submitList(collections)
        }
    }

    // Наблюдение за статусом операций (чтобы показать Toast)
    private fun observeOperationStatus() {
        viewModel.collectionOperationStatus.observe(viewLifecycleOwner, EventObserver { message ->
            println("CollectionsFragment: Operation status received: $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            // LiveData userCollectionsWithMovies обновится сам после создания коллекции
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCollectionsList.adapter = null
        _binding = null
        println("CollectionsFragment: onDestroyView")
    }

    companion object {
        @JvmStatic
        fun newInstance() = CollectionsFragment()
    }
}