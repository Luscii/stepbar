package com.luscii.stepbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import androidx.core.animation.addListener
import com.luscii.stepbar.Utils.dp

class DotIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), Indicator {
    override var stepCount: Int = 0
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }
    override var currentStep: Int = 0

    private var previousStep: Int = 0
        set(value) {
            field = value
            val centerY = height / 2f
            ValueAnimator.ofFloat(centerY, centerY - dotRadius * 3, centerY).apply {
                duration = 700
                interpolator = AnticipateOvershootInterpolator()
                addUpdateListener {
                    selectedY = it.animatedValue as Float
                    invalidate()
                }
                addListener {
                    previousStep = currentStep
                }
                start()
            }
            val drawWidth = stepCount * dotRadius + (stepCount - 1) * dotDistance
            val startX = (width - drawWidth) / 2 + previousStep * (dotRadius * 2 + dotDistance)
            ValueAnimator.ofFloat(
                startX,
                startX + (currentStep - previousStep) * (dotDistance + dotRadius * 2)
            ).apply {
                startDelay = 250
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    selectedX = it.animatedValue as Float
                }
                start()
            }
        }

    private var selectedX: Float = 0f
    private var selectedY: Float = 0f
    private var selectedColor: Int = Color.GREEN
    private var deselectedColor: Int = Color.GRAY
    private var dotRadius: Float = 0f
    private var dotDistance: Float = 0f

    private val selectedPaint by lazy {
        Paint(ANTI_ALIAS_FLAG).apply {
            color = selectedColor
            style = Paint.Style.FILL
        }
    }

    private val deselectedPaint by lazy {
        Paint(ANTI_ALIAS_FLAG).apply {
            color = deselectedColor
            style = Paint.Style.FILL
        }
    }

    init {
        initView()
        initStyle(attrs, defStyleAttr)
    }

    private fun initView() {
    }

    private fun initStyle(attrs: AttributeSet?, defStyleAttr: Int) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.DotIndicator,
            defStyleAttr,
            0
        ).apply {

            try {
                selectedColor = getColor(R.styleable.DotIndicator_selectedColor, Color.RED)
                deselectedColor = getColor(R.styleable.DotIndicator_deselectedColor, Color.BLUE)
                stepCount = getInteger(R.styleable.DotIndicator_count, 3)
                dotRadius =
                    getDimensionPixelSize(R.styleable.DotIndicator_dotRadius, 4.dp).toFloat()
                dotDistance =
                    getDimensionPixelSize(R.styleable.DotIndicator_dotDistance, 8.dp).toFloat()
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = (stepCount + 2) * dotRadius * 2 + stepCount * dotDistance +
            paddingLeft + paddingRight
        val desiredHeight = dotRadius * 8 + paddingTop + paddingBottom

        setMeasuredDimension(
            measureDimension(desiredWidth.toInt(), widthMeasureSpec),
            measureDimension(desiredHeight.toInt(), heightMeasureSpec)
        )
    }

    private fun measureDimension(desiredSize: Int, measureSpec: Int): Int {
        var result: Int
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        if (specMode == MeasureSpec.EXACTLY) {
            result = specSize
        } else {
            result = desiredSize
            if (specMode == MeasureSpec.AT_MOST) {
                result = result.coerceAtMost(specSize)
            }
        }

        if (result < desiredSize) {
            Log.e("DotIndicator", "The view is too small, the content might get cut")
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val drawWidth = stepCount * dotRadius + (stepCount - 1) * dotDistance
        selectedX = (width - drawWidth) / 2 + currentStep * (dotRadius * 2 + dotDistance)
        selectedY = height / 2f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            val verticalCenter = (height / 2).toFloat()
            val drawWidth = stepCount * dotRadius + (stepCount - 1) * dotDistance
            var startX = (width - drawWidth) / 2
            for (i in 0 until stepCount) {
                drawCircle(startX, verticalCenter, dotRadius, deselectedPaint)
                startX += dotRadius * 2 + dotDistance
            }
            drawCircle(selectedX, selectedY, dotRadius, selectedPaint)
        }
    }

    override fun nextStep() {
        if (currentStep < stepCount - 1) {
            currentStep++
            previousStep = currentStep - 1
        }
    }

    override fun previousStep() {
        if (currentStep > 0) {
            currentStep--
            previousStep = currentStep + 1
        }
    }

    override fun update() {
    }
}
