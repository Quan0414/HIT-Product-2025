package com.example.hitproduct.common.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.hitproduct.R

class OutlinedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var strokeColor: Int = Color.BLACK
    private var strokeWidth: Float = 4f
    private var fillColor: Int = currentTextColor

    init {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.OutlinedTextView)
            strokeColor = a.getColor(
                R.styleable.OutlinedTextView_strokeColor,
                strokeColor
            )
            strokeWidth = a.getDimension(
                R.styleable.OutlinedTextView_strokeWidth,
                strokeWidth
            )
            fillColor = a.getColor(
                R.styleable.OutlinedTextView_fillColor,
                fillColor
            )
            a.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // lưu lại trạng thái paint và color gốc
        val originalPaint = paint
        val originalStyle = originalPaint.style
        val originalStrokeWidth = originalPaint.strokeWidth
        val originalTextColor = currentTextColor

        // Vẽ outline
        originalPaint.style = Paint.Style.STROKE
        originalPaint.strokeWidth = strokeWidth
        setTextColor(strokeColor)
        super.onDraw(canvas)

        // Vẽ fill
        originalPaint.style = Paint.Style.FILL
        originalPaint.strokeWidth = originalStrokeWidth
        setTextColor(fillColor)
        super.onDraw(canvas)

        // Khôi phục paint và color ban đầu
//        originalPaint.style = originalStyle
//        originalPaint.strokeWidth = originalStrokeWidth
        setTextColor(originalTextColor)
    }
}