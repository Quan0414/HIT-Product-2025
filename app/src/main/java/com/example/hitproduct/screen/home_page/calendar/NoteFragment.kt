package com.example.hitproduct.screen.home_page.calendar

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.constants.AuthPrefersConstants
import com.example.hitproduct.common.state.UiState
import com.example.hitproduct.common.util.OutlinedTextView
import com.example.hitproduct.data.api.NetworkClient
import com.example.hitproduct.data.model.calendar.Note
import com.example.hitproduct.data.repository.AuthRepository
import com.example.hitproduct.databinding.FragmentNoteBinding
import com.example.hitproduct.screen.dialog.note.create.DialogCreateNote
import com.example.hitproduct.screen.dialog.note.get.DialogNote
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class NoteFragment : BaseFragment<FragmentNoteBinding>() {

    private val prefs by lazy {
        requireContext().getSharedPreferences(AuthPrefersConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val authRepo by lazy {
        AuthRepository(
            NetworkClient.provideApiService(requireContext()),
            prefs
        )
    }

    private val viewModel by activityViewModels<NoteViewModel> {
        NoteViewModelFactory(authRepo)
    }

    private var allNotes: List<Note> = emptyList()

    private var noteDates: Set<LocalDate> = emptySet()
    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchNotes()
        }
    }

    override fun initView() {
        setupCalendar()
        requireActivity().supportFragmentManager.setFragmentResultListener(
            "refresh_notes",
            viewLifecycleOwner
        ) { _, _ ->
            Log.d("NoteFragment", "🔔 refresh_notes received — reloading notes")
            viewModel.fetchNotes()
        }

        viewModel.fetchNotes()
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY)

        binding.cvCalendar.setup(startMonth, endMonth, daysOfWeek.first())
        binding.cvCalendar.scrollToMonth(currentMonth)

        // Setup day binder
        binding.cvCalendar.dayBinder = object : MonthDayBinder<DayContainer> {
            override fun create(view: View) = DayContainer(view)

            override fun bind(container: DayContainer, data: CalendarDay) {
                container.day = data
                container.tvDay.text = String.format("%02d", data.date.dayOfMonth)

                container.ivNote.visibility = if (
                    data.position == DayPosition.MonthDate
                    && noteDates.contains(data.date)
                ) View.VISIBLE else View.INVISIBLE

                // Set callback for date clicks
                container.onDateClickListener = { date ->
                    onDateClicked(date)
                }

                when (data.position) {
                    DayPosition.MonthDate -> {
                        // Ngày trong tháng hiện tại
                        container.tvDay.apply {
                            visibility = View.VISIBLE
                            val bgRes = when {
                                data.date == selectedDate -> R.drawable.bg_item_day_selector
                                data.date == today -> R.drawable.bg_item_day_selector
                                else -> R.drawable.bg_item_day
                            }
                            setBackgroundResource(bgRes)
                        }
                    }

                    DayPosition.InDate -> {
                        container.itemDay.visibility = View.INVISIBLE
                    }

                    DayPosition.OutDate -> {
                        container.itemDay.visibility = View.INVISIBLE
                    }
                }

                // TODO: Hiển thị note indicator nếu có note cho ngày này
                // container.ivNote.visibility = if (hasNoteForDate(data.date)) View.VISIBLE else View.GONE
            }
        }

        // Setup month header binder
        binding.cvCalendar.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthHeaderContainer> {
                override fun create(view: View) = MonthHeaderContainer(view)

                override fun bind(container: MonthHeaderContainer, data: CalendarMonth) {
                    val monthTitle = "Tháng ${
                        data.yearMonth.month.value
                    } ${data.yearMonth.year}"
                    container.tvMonth.text = monthTitle

                    container.btnPrev.setOnClickListener {
                        binding.cvCalendar.smoothScrollToMonth(data.yearMonth.minusMonths(1))
                    }

                    container.btnNext.setOnClickListener {
                        binding.cvCalendar.smoothScrollToMonth(data.yearMonth.plusMonths(1))
                    }
                }
            }
    }

    class MonthHeaderContainer(view: View) : ViewContainer(view) {
        val btnPrev = view.findViewById<ImageView>(R.id.btnPrev)
        val tvMonth = view.findViewById<TextView>(R.id.tvMonth)
        val btnNext = view.findViewById<ImageView>(R.id.btnNext)
    }

    class DayContainer(view: View) : ViewContainer(view) {
        val itemDay = view.findViewById<View>(R.id.itemCalendarDay)
        val tvDay = view.findViewById<OutlinedTextView>(R.id.tvDay)
        val ivNote = view.findViewById<ImageView>(R.id.ivNote)

        // Lưu reference đến CalendarDay để sử dụng trong click listener
        lateinit var day: CalendarDay

        // Callback để handle date click
        var onDateClickListener: ((LocalDate) -> Unit)? = null

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    // Handle day click thông qua callback
                    onDateClickListener?.invoke(day.date)
                }
            }
        }
    }

    private fun onDateClicked(date: LocalDate) {
        val notesForDate = allNotes.filter { note ->
            Instant.parse(note.date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == date
        }
        if (notesForDate.isEmpty()) {
            DialogCreateNote
                .newInstance(date.toString())
                .show(requireActivity().supportFragmentManager, "dialog_create_note")

        } else {
            DialogNote.newInstance(notesForDate)
                .show(requireActivity().supportFragmentManager, "dialog_note")
        }

        // Handle date selection
        handleDateSelection(date)
    }

    private fun handleDateSelection(date: LocalDate) {
        //mo dialog
    }

    override fun initListener() {
        // Các listener khác nếu cần
    }

    override fun initData() {
        // Load data nếu cần
    }

    override fun handleEvent() {
        // Handle events nếu cần
    }

    override fun bindData() {
        viewModel.notes.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Error -> {
                    Log.e("NoteFragment", "Error fetching notes: ${state.error}")
                }

                UiState.Idle -> {}
                UiState.Loading -> {}
                is UiState.Success -> {
                    allNotes = state.data
                    noteDates = state.data.map { note ->
                        Instant.parse(note.date)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                    }.toSet()
                    binding.cvCalendar.notifyCalendarChanged()
                }
            }
        }


    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNoteBinding {
        return FragmentNoteBinding.inflate(inflater, container, false)
    }
}