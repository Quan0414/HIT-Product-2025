package com.example.hitproduct.common.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import com.example.hitproduct.R

class OutlinedEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val strokePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val fillPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.OutlinedTextView)
            strokePaint.color = a.getColor(
                R.styleable.OutlinedTextView_strokeColor,
                Color.BLACK
            )
            strokePaint.strokeWidth = a.getDimension(
                R.styleable.OutlinedTextView_strokeWidth,
                4f
            )
            fillPaint.color = a.getColor(
                R.styleable.OutlinedTextView_fillColor,
                currentTextColor
            )
            a.recycle()
        }

        // Tắt background mặc định để tránh conflict với custom drawing
        background = null
    }

    override fun onDraw(canvas: Canvas) {
        // Vẽ outline text trước
        drawOutlinedText(canvas)

        // Tạm thời làm trong suốt text color để không vẽ đè lên outline
        val originalColor = currentTextColor
        setTextColor(Color.TRANSPARENT)

        // Vẽ EditText bình thường (cursor, selection, etc.)
        super.onDraw(canvas)

        // Khôi phục màu text
        setTextColor(originalColor)
    }

    private fun drawOutlinedText(canvas: Canvas) {
        val text = text?.toString() ?: return
        if (text.isEmpty()) return

        val x = compoundPaddingLeft.toFloat()
        val y = baseline.toFloat()

        // Cấu hình paint cho stroke
        strokePaint.textSize = textSize
        strokePaint.typeface = typeface
        strokePaint.letterSpacing = letterSpacing

        // Cấu hình paint cho fill
        fillPaint.textSize = textSize
        fillPaint.typeface = typeface
        fillPaint.letterSpacing = letterSpacing

        // Phương pháp 1: Vẽ outline bằng cách offset theo 4 hướng chính
        val strokeWidth = strokePaint.strokeWidth
        val offset = strokeWidth / 2

        // Vẽ outline ở 4 hướng chính
        canvas.drawText(text, x - offset, y, strokePaint) // Trái
        canvas.drawText(text, x + offset, y, strokePaint) // Phải
        canvas.drawText(text, x, y - offset, strokePaint) // Trên
        canvas.drawText(text, x, y + offset, strokePaint) // Dưới

        // Vẽ outline ở 4 hướng chéo để làm mượt hơn
        val diagonalOffset = offset * 0.7f // Giảm offset cho hướng chéo
        canvas.drawText(text, x - diagonalOffset, y - diagonalOffset, strokePaint) // Trái-trên
        canvas.drawText(text, x + diagonalOffset, y - diagonalOffset, strokePaint) // Phải-trên
        canvas.drawText(text, x - diagonalOffset, y + diagonalOffset, strokePaint) // Trái-dưới
        canvas.drawText(text, x + diagonalOffset, y + diagonalOffset, strokePaint) // Phải-dưới

        // Vẽ fill text lên trên
        canvas.drawText(text, x, y, fillPaint)
    }

    // Cập nhật màu text khi thay đổi
    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        fillPaint.color = color
        invalidate()
    }

    // Các phương thức để set màu stroke và fill programmatically
    fun setStrokeColor(color: Int) {
        strokePaint.color = color
        invalidate()
    }

    fun setStrokeWidth(width: Float) {
        strokePaint.strokeWidth = width
        invalidate()
    }

    fun setFillColor(color: Int) {
        fillPaint.color = color
        invalidate()
    }
}