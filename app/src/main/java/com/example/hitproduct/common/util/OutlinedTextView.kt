package com.example.hitproduct.common.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.hitproduct.R

class OutlinedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

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
    }

    override fun onDraw(canvas: Canvas) {
        val x = compoundPaddingLeft.toFloat()
        val y = baseline.toFloat()

        strokePaint.textSize = textSize
        strokePaint.typeface = typeface
        strokePaint.letterSpacing = letterSpacing

        fillPaint.textSize = textSize
        fillPaint.typeface = typeface
        fillPaint.letterSpacing = letterSpacing

        canvas.drawText(text.toString(), x, y, strokePaint)
        canvas.drawText(text.toString(), x, y, fillPaint)
    }
}
