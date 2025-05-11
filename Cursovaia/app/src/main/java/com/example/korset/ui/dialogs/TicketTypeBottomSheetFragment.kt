package com.example.korset.ui.dialogs

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.CompoundButtonCompat
// УДАЛЯЕМ: import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels // Для получения ViewModel
import com.example.korset.R
import com.example.korset.data.model.Seat
import com.example.korset.data.model.SessionItem
import com.example.korset.data.model.TicketType
import com.example.korset.databinding.BottomSheetTicketTypeBinding
import com.example.korset.ui.details.SeatSelectionViewModel // Импорт ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.radiobutton.MaterialRadioButton // Используем MaterialRadioButton

class TicketTypeBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetTicketTypeBinding? = null
    private val binding get() = _binding!!

    private var selectedSeats: List<Seat>? = null
    private var sessionInfo: SessionItem? = null
    private var ticketTypes = mutableListOf<TicketType>()
    private var selectedTicketType: TicketType? = null

    // --- ПОЛУЧАЕМ ОБЩИЙ ViewModel ---
    private val viewModel: SeatSelectionViewModel by viewModels({ requireParentFragment() })
    // -----------------------------

    companion object {
        const val TAG = "TicketTypeBottomSheet"
        // REQUEST_KEY и RESULT_KEY_TYPE больше не нужны

        private const val ARG_SEATS = "arg_seats"
        private const val ARG_SESSION = "arg_session"

        fun newInstance(seats: List<Seat>, session: SessionItem): TicketTypeBottomSheetFragment {
            val fragment = TicketTypeBottomSheetFragment()
            val seatArrayList = ArrayList(seats) // Используем ArrayList для Parcelable
            fragment.arguments = bundleOf(
                ARG_SEATS to seatArrayList,
                ARG_SESSION to session
            )
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем аргументы
        arguments?.let {
            selectedSeats = it.getParcelableArrayList(ARG_SEATS)
            sessionInfo = it.getParcelable(ARG_SESSION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetTicketTypeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (selectedSeats.isNullOrEmpty() || sessionInfo == null) {
            println("TicketTypeBottomSheet: Error - Missing seats or session info")
            dismiss()
            return
        }

        displaySelectedSeatsInfo()
        prepareTicketTypes()
        populateRadioGroup()
        setupConfirmButton() // Используем ИСПРАВЛЕННУЮ версию ниже
    }

    // Отображение информации о выбранных местах (увеличим текст)
    private fun displaySelectedSeatsInfo() {
        val seatsText = selectedSeats?.joinToString { "Р${it.row} М${it.number}" } ?: "Места не выбраны"
        val count = selectedSeats?.size ?: 0
        binding.tvSelectedSeatsInfo.text = "Места ($count): $seatsText"
        // binding.tvSelectedSeatsInfo.textSize = 16f // Если нужно программно
    }

    // Подготовка списка типов билетов (без изменений)
    private fun prepareTicketTypes() {
        ticketTypes.clear()
        sessionInfo?.let { session ->
            // Взрослый (всегда есть)
            ticketTypes.add(TicketType("Взрослый", null, session.priceAdult))
            // Студенческий (если цена есть и > 0)
            session.priceStudent?.let { price ->
                if (price > 0) ticketTypes.add(TicketType("Студенческий", null, price))
            }
            // Детский (если цена есть и > 0)
            session.priceChild?.let { price ->
                if (price > 0) ticketTypes.add(TicketType("Детский", "с 5 до 16 лет", price))
            }
        }
        // Выбираем первый тип по умолчанию
        if (ticketTypes.isNotEmpty()) {
            ticketTypes[0].isSelected = true
            selectedTicketType = ticketTypes[0]
        }
    }

    // Динамическое добавление RadioButton (без изменений)
    private fun populateRadioGroup() {
        binding.radioGroupTicketTypes.removeAllViews() // Очищаем старые кнопки

        ticketTypes.forEachIndexed { index, ticketType ->
            val radioButton = MaterialRadioButton(requireContext())
            radioButton.id = View.generateViewId()
            radioButton.text = "${ticketType.name} • ${ticketType.price} ₸ ${ticketType.description?.let { "($it)" } ?: ""}"
            radioButton.tag = ticketType
            radioButton.isChecked = ticketType.isSelected
            radioButton.setPadding(0, 16, 0, 16)

            // --- УСТАНАВЛИВАЕМ РАЗМЕР ТЕКСТА ПРОГРАММНО ---
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // <<<=== УСТАНОВИ НУЖНЫЙ РАЗМЕР (например, 16sp)
            // ---------------------------------------------



            binding.radioGroupTicketTypes.addView(radioButton)

            // Устанавливаем слушатель выбора для этой кнопки
            radioButton.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    // Снимаем выбор со всех остальных и обновляем selectedTicketType
                    ticketTypes.forEach { it.isSelected = false } // Сбрасываем флаги в модели
                    val selectedType = buttonView.tag as? TicketType
                    selectedType?.isSelected = true
                    selectedTicketType = selectedType
                    println("TicketType selected: ${selectedTicketType?.name}")
                    // Обновляем вид RadioGroup (хотя он сам должен справиться)
                    // populateRadioGroup() // Не нужно пересоздавать все
                }
            }
        }
        // Убедимся, что первая кнопка выбрана (если она есть)
        if (binding.radioGroupTicketTypes.childCount > 0) {
            (binding.radioGroupTicketTypes.getChildAt(0) as? RadioButton)?.isChecked = true
        }
    }


    // --- ИЗМЕНЕННАЯ Настройка кнопки подтверждения ---
    private fun setupConfirmButton() {
        binding.buttonConfirmTicketType.setOnClickListener {
            selectedTicketType?.let {
                // --- ВЫЗЫВАЕМ МЕТОД ViewModel ---
                viewModel.selectTicketType(it)
                println("TicketTypeBottomSheet: ViewModel updated with: ${it.name}")
                // -------------------------------
            } ?: println("TicketTypeBottomSheet: No ticket type selected!")
            dismiss() // Закрываем диалог
        }
    }
    // --------------------------------------------

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


