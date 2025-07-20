package com.example.hitproduct.screen.home_page.note

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.hitproduct.R
import com.example.hitproduct.base.BaseFragment
import com.example.hitproduct.common.util.OutlinedTextView
import com.example.hitproduct.databinding.FragmentNoteBinding
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class NoteFragment : BaseFragment<FragmentNoteBinding>() {

    private var selectedDate: LocalDate? = null

    override fun initView() {
        setupCalendar()
    }

    private fun setupCalendar() {
        // Setup calendar range
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

                // Set callback for date clicks
                container.onDateClickListener = { date ->
                    onDateClicked(date)
                }

                when (data.position) {
                    DayPosition.MonthDate -> {
                        // Ngày trong tháng hiện tại
                        container.tvDay.visibility = View.VISIBLE

                        // Hiển thị selection nếu là ngày được chọn
//                        if (data.date == selectedDate) {
//                            container.ivCalendarIcon.setBackgroundResource(R.drawable.bg_item_day_selector)
//                        } else {
//                            container.ivCalendarIcon.setBackgroundResource(R.drawable.bg_item_day)
//                        }
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
        // Cập nhật selected date
        val currentSelection = selectedDate
        if (currentSelection == date) {
            // Nếu click vào ngày đã chọn thì bỏ chọn
            selectedDate = null
            binding.cvCalendar.notifyDateChanged(currentSelection)
        } else {
            selectedDate = date
            binding.cvCalendar.notifyDateChanged(date)
            if (currentSelection != null) {
                binding.cvCalendar.notifyDateChanged(currentSelection)
            }
        }

        // Handle date selection
        handleDateSelection(date)
    }

    private fun handleDateSelection(date: LocalDate) {
        // Implement logic khi user chọn một ngày
        // Ví dụ: hiển thị notes cho ngày đó, mở editor, etc.
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
        // Bind data nếu cần
    }

    override fun inflateLayout(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentNoteBinding {
        return FragmentNoteBinding.inflate(inflater, container, false)
    }
}