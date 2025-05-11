package com.example.korset.ui.dialogs // Или твой пакет

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels // Для MovieDetailViewModel
import androidx.lifecycle.ViewModelProvider // Для MovieDetailViewModel (альтернатива)
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.korset.databinding.BottomSheetAddToCollectionBinding // ViewBinding
import com.example.korset.ui.adapters.CollectionListAdapter // Адаптер коллекций
import com.example.korset.ui.details.MovieDetailViewModel // ViewModel с логикой
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.viewModels // <<<=== Для делегата viewModels
import com.example.korset.util.EventObserver // <<<=== Для нашего EventObserver
import com.example.korset.util.Event // Убедись, что Event тоже импортирован
import com.example.korset.data.local.CollectionWithMovies // Импорт новой модели
import com.example.korset.data.local.CollectionEntity


class AddToCollectionBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAddToCollectionBinding? = null
    private val binding get() = _binding!!

    // Получаем MovieDetailViewModel от родительского фрагмента (MovieDetailFragment)
    // Так как именно там есть информация о текущем фильме
    private val viewModel: MovieDetailViewModel by viewModels({ requireParentFragment() })

    private lateinit var collectionAdapter: CollectionListAdapter

    companion object {
        const val TAG = "AddToCollectionBottomSheet"
        // newInstance не нужен, так как вся нужная информация есть в ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAddToCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        println("AddToCollectionBottomSheet: onViewCreated")

        setupRecyclerView()
        setupCreateButton()
        observeCollections()
        setupRecyclerView()
        observeOperationStatus() // Наблюдаем за статусом операций

        // Запрашиваем список коллекций (хотя он должен загружаться при инициализации ViewModel)
        // Если LiveData не обновляется, можно попробовать вызвать что-то вроде viewModel.refreshCollections()
    }

    // Настройка RecyclerView для существующих коллекций
    private fun setupRecyclerView() {
        // Создаем адаптер, передавая ОБЕ лямбды
        collectionAdapter = CollectionListAdapter(
            onCollectionClick = { collectionWithMovies -> // Клик по элементу
                // Получаем саму коллекцию из объекта-обертки
                val collectionEntity = collectionWithMovies.collection // <<<=== ИЗМЕНЕНО
                println("AddToCollectionBottomSheet: Existing collection clicked: ${collectionEntity.name}")
                viewModel.addCurrentMovieToCollection(collectionEntity.collectionId) // <<<=== Используем ID из collectionEntity
            },
            onDeleteClick = { collectionEntity -> // Клик по кнопке удаления
                // В ЭТОМ ДИАЛОГЕ УДАЛЕНИЕ НЕ НУЖНО, оставляем пустым или показываем сообщение
                println("AddToCollectionBottomSheet: Delete clicked for ${collectionEntity.name}, but deletion is not handled here.")
                Toast.makeText(context, "Удалить коллекцию можно на экране 'Избранное'", Toast.LENGTH_SHORT).show()
            }
        )
        // --- КОНЕЦ ИЗМЕНЕНИЯ ПРИ СОЗДАНИИ АДАПТЕРА ---

        binding.rvCollections.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = collectionAdapter
            layoutParams.height = resources.displayMetrics.heightPixels / 3
        }
        println("AddToCollectionBottomSheet: Collections RecyclerView setup done.")
    }
    // ---------------------------------

    // --- КОНЕЦ ИЗМЕНЕНИЯ ---


    // Настройка кнопки "Создать" новую коллекцию
    private fun setupCreateButton() {
        binding.buttonCreateCollection.setOnClickListener {
            val collectionName = binding.etNewCollectionName.text.toString().trim()
            if (collectionName.isNotEmpty()) {
                println("AddToCollectionBottomSheet: Create button clicked. Name: $collectionName")
                // Вызываем метод ViewModel для создания И добавления фильма в новую коллекцию
                // TODO: Доработать ViewModel, чтобы createNewCollection возвращал ID
                //       и потом вызывать addCurrentSMovieToCollection с этим ID.
                //       Или сделать одну функцию createAndAddMovieToCollection.
                // Пока просто создаем коллекцию и показываем сообщение.
                viewModel.createNewCollection(collectionName)
                binding.etNewCollectionName.text.clear() // Очищаем поле ввода
                // Можно закрыть диалог после создания, или подождать observeOperationStatus
            } else {
                Toast.makeText(context, "Введите название коллекции", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Наблюдение за списком коллекций из ViewModel
    private fun observeCollections() {
        // Наблюдаем за userCollectionsWithMovies
        viewModel.userCollectionsWithMovies.observe(viewLifecycleOwner) { collectionsWithMovies ->
            println("AddToCollectionBottomSheet: Observed ${collectionsWithMovies?.size ?: "null"} collections.")
            // Проверяем список CollectionWithMovies на null или пустоту
            val hasCollections = !collectionsWithMovies.isNullOrEmpty() // <<<=== Используем правильную переменную
            binding.rvCollections.isVisible = hasCollections
            binding.tvNoCollections.isVisible = !hasCollections
            if (hasCollections) {
                // Передаем список CollectionWithMovies в адаптер
                collectionAdapter.submitList(collectionsWithMovies) // <<<=== Используем правильную переменную
            } else {
                collectionAdapter.submitList(emptyList()) // Очищаем, если null
            }
        }
    }
    // --- КОНЕЦ ИЗМЕНЕНИЯ ---


    // Наблюдение за статусом операций (создание/добавление)
    private fun observeOperationStatus() {
        // Убедись, что EventObserver импортирован правильно
        viewModel.collectionOperationStatus.observe(viewLifecycleOwner, EventObserver { message ->
            println("AddToCollectionBottomSheet: Operation status received: $message")
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            if (!message.startsWith("Ошибка")) {
                dismiss()
            }
        })
    }
    // --- КОНЕЦ ПРОВЕРКИ ---


    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCollections.adapter = null // Очищаем адаптер
        _binding = null
        println("AddToCollectionBottomSheet: onDestroyView")
    }
}