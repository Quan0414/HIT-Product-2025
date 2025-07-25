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
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val strokePaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val fillPaint = TextPaint(TextPaint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var strokeColor  = currentTextColor
    private var strokeWidth  = 4f
    private var fillColor    = currentTextColor

    init {
        // đọc custom attrs
        attrs?.let {
            val a = context.obtainStyledAttributes(
                it, R.styleable.OutlinedTextView, defStyleAttr, 0
            )
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

        // thiết lập paint
        strokePaint.color       = strokeColor
        strokePaint.strokeWidth = strokeWidth
        fillPaint.color         = fillColor

        // Ẩn text gốc: ta tự vẽ fill bằng fillPaint
        super.setTextColor(Color.TRANSPARENT)
    }

    override fun onDraw(canvas: Canvas) {
        // 1) vẽ outline + fill
        val content = if (text.isNullOrEmpty()) hint?.toString().orEmpty()
        else text.toString()
        val isHint = text.isNullOrEmpty()

        if (content.isNotEmpty()) {
            drawOutlinedText(canvas, content, isHint)
        }

        // 2) rồi super vẽ background, cursor, selection chứ không vẽ text (vì transparent)
        super.onDraw(canvas)
    }

    private fun drawOutlinedText(canvas: Canvas, content: String, isHint: Boolean) {
        val layout = layout ?: return

        // cập nhật paint mỗi lần vẽ
        strokePaint.apply {
            textSize      = this@OutlinedEditText.textSize
            typeface      = this@OutlinedEditText.typeface
            letterSpacing = this@OutlinedEditText.letterSpacing
            color         = strokeColor
            strokeWidth   = strokeWidth
            alpha         = if (isHint) 100 else 255
        }
        fillPaint.apply {
            textSize      = this@OutlinedEditText.textSize
            typeface      = this@OutlinedEditText.typeface
            letterSpacing = this@OutlinedEditText.letterSpacing
            color         = fillColor
            alpha         = if (isHint) 100 else 255
        }

        val padL = compoundPaddingLeft
        val padT = compoundPaddingTop
        canvas.save()
        canvas.translate(padL - scrollX.toFloat(), padT - scrollY.toFloat())

        val offset = strokeWidth / 2f
        val diag   = offset * 0.707f

        for (i in 0 until layout.lineCount) {
            val start = layout.getLineStart(i)
            val end   = layout.getLineEnd(i)
            val line  = content.substring(start, end)
            val x     = layout.getLineLeft(i)
            val y     = layout.getLineBaseline(i).toFloat()

            // vẽ outline 8 hướng
            listOf(
                -offset to  0f,  offset to  0f,
                0f     to -offset, 0f     to  offset,
                -diag   to -diag,   diag   to -diag,
                -diag   to  diag,   diag   to  diag
            ).forEach { (dx, dy) ->
                canvas.drawText(line, x + dx, y + dy, strokePaint)
            }

            // vẽ fill chính giữa
            canvas.drawText(line, x, y, fillPaint)
        }

        canvas.restore()
    }

    // override để fillColor theo setTextColor()
    override fun setTextColor(color: Int) {
        fillColor = color
        fillPaint.color = color
        super.setTextColor(Color.TRANSPARENT)
        invalidate()
    }

    /** Programmatic API **/
    fun setStrokeColor(color: Int) {
        strokeColor = color; strokePaint.color = color; invalidate()
    }
    fun setStrokeWidth(width: Float) {
        strokeWidth = width; strokePaint.strokeWidth = width; invalidate()
    }
    fun setFillColor(color: Int) {
        fillColor = color; fillPaint.color = color; invalidate()
    }

    fun getStrokeColor(): Int  = strokeColor
    fun getStrokeWidth(): Float = strokeWidth
    fun getFillColor(): Int    = fillColor
}
