package com.luscii.stepbar

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class TextIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr), Indicator {
    override var stepCount: Int = 0
        set(value) {
            field = value
            update()
        }
    override var currentStep: Int = 0

    private var color: Int = Color.GRAY
    private var formatText: String = ""

    init {
        initStyle(attrs, defStyleAttr)
    }

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.TextIndicator,
            defStyleAttr,
            0
        ).apply {
            try {
                color = getColor(R.styleable.TextIndicator_color, Color.GRAY)
                setTextColor(color)
                getString(R.styleable.TextIndicator_formatText)?.let {
                    formatText = it
                }
                stepCount = getInteger(R.styleable.TextIndicator_count, 3)
            } finally {
                recycle()
            }
        }
    }

    override fun update() {
        text = String.format(formatText, currentStep + 1, stepCount)
    }

    override fun nextStep() {
        if (currentStep < stepCount - 1) {
            currentStep++
            update()
        }
    }

    override fun previousStep() {
        if (currentStep > 0) {
            currentStep--
            update()
        }
    }
}
