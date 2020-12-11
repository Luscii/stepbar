package com.luscii.stepbar

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.View.OnClickListener
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import com.google.android.material.button.MaterialButton

class StepBar @kotlin.jvm.JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    var stepCount: Int = 0
        set(value) {
            field = value
            indicator.stepCount = value
            stepChanged()
        }

    var leftButtonEnabled: Boolean
        set(value) {
            leftButton.isEnabled = value
        }
        get() = leftButton.isEnabled

    var leftButtonVisible: Boolean
        set(value) {
            leftButton.visibility = if (value) View.VISIBLE else View.GONE
        }
        get() = leftButton.visibility == View.VISIBLE

    var rightButtonVisible: Boolean
        set(value) {
            rightButton.visibility = if (value) View.VISIBLE else View.GONE
        }
        get() = rightButton.visibility == View.VISIBLE

    var rightButtonEnabled: Boolean
        set(value) {
            rightButton.isEnabled = value
        }
        get() = rightButton.isEnabled

    var indicatorVisibility: Boolean
        set(value) {
            indicatorContainer.visibility = if (value) View.VISIBLE else View.GONE
        }
        get() = indicatorContainer.visibility == View.VISIBLE

    private val indicatorContainer by lazy {
        findViewById<FrameLayout>(R.id.indicatorContainer)
    }

    var autoHideButtons: Boolean = true

    private lateinit var leftButton: MaterialButton
    private lateinit var rightButton: MaterialButton
    private lateinit var indicator: Indicator
    var currentStep: Int = 0
        set(value) {
            field = value
            indicator.currentStep = value
            stepChanged()
        }

    private var listener: StepBarListener? = null
    var leftButtonClickListener: OnClickListener? = null
    var rightButtonClickListener: OnClickListener? = null
    fun setListener(listener: StepBarListener) {
        this.listener = listener
    }

    private val _leftButtonClickListener = OnClickListener {
        previousStep()
        leftButtonClickListener?.onClick(it)
    }

    private val _rightButtonClickListener = OnClickListener {
        nextStep()
        rightButtonClickListener?.onClick(it)
    }

    init {
        initView()
        initStyle(attrs)
        isSaveEnabled = true
    }

    private fun initView() {
        inflate(context, R.layout.step_bar, this)
    }

    private fun initStyle(attrs: AttributeSet?) {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.StepBar,
            0,
            0
        ).apply {

            try {
                val leftButtonType = getInt(R.styleable.StepBar_leftButtonType, 0)
                leftButton = MaterialButton(
                    ContextThemeWrapper(
                        context,
                        getButtonStyle((leftButtonType))
                    )
                ).apply {
                    layoutParams = LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                }
                findViewById<FrameLayout>(R.id.leftButtonContainer).addView(leftButton)
                getString(R.styleable.StepBar_leftButtonText)?.let {
                    setLeftButtonText(it)
                }
                setLeftButtonIcon(getDrawable(R.styleable.StepBar_leftButtonIcon))
                leftButton.isEnabled = getBoolean(R.styleable.StepBar_leftButtonEnabled, true)
                leftButton.setOnClickListener(_leftButtonClickListener)

                val rightButtonType = getInt(R.styleable.StepBar_rightButtonType, 0)
                rightButton = MaterialButton(
                    ContextThemeWrapper(
                        context,
                        getButtonStyle(rightButtonType)
                    )
                ).apply {
                    layoutParams = LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT,
                        Gravity.CENTER
                    )
                }
                findViewById<FrameLayout>(R.id.rightButtonContainer).addView(rightButton)
                getString(R.styleable.StepBar_rightButtonText)?.let {
                    setRightButtonText(it)
                }
                setRightButtonIcon(getDrawable(R.styleable.StepBar_rightButtonIcon))
                rightButton.isEnabled = getBoolean(R.styleable.StepBar_rightButtonEnabled, true)
                rightButton.setOnClickListener(_rightButtonClickListener)

                val indicatorType = getInt(R.styleable.StepBar_indicatorType, 0)
                val indicatorContainer = findViewById<FrameLayout>(R.id.indicatorContainer)
                indicator = when (indicatorType) {
                    0 ->
                        DotIndicator(
                            ContextThemeWrapper(context, R.style.StepBar_DotIndicator)
                        ).apply {
                            indicatorContainer.addView(this)
                        }
                    1 ->
                        TextIndicator(
                            ContextThemeWrapper(context, R.style.StepBar_TextIndicator)
                        ).apply {
                            layoutParams = LayoutParams(
                                LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT,
                                Gravity.CENTER
                            )
                            indicatorContainer.addView(this)
                        }
                    else -> NoIndicator()
                }

                autoHideButtons = getBoolean(R.styleable.StepBar_autoHideButtons, true)
                stepCount = getInteger(R.styleable.StepBar_stepCount, 0)
            } finally {
                recycle()
            }
        }
    }

    private fun getButtonStyle(buttonType: Int): Int {
        return when (buttonType) {
            1 -> R.style.StepBar_OutlinedButton
            2 -> R.style.StepBar_TextButton
            else -> R.style.StepBar_PrimaryButton
        }
    }

    private fun setLeftButtonIcon(drawable: Drawable?) {
        leftButton.setCompoundDrawablesWithIntrinsicBounds(
            drawable,
            null,
            null,
            null
        )
    }

    private fun setRightButtonIcon(drawable: Drawable?) {
        rightButton.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
    }

    fun setLeftButtonText(text: String) {
        leftButton.text = text
    }

    fun setRightButtonText(text: String) {
        rightButton.text = text
    }

    private fun nextStep() {
        indicator.nextStep()
        currentStep = indicator.currentStep
        stepChanged()
    }

    private fun previousStep() {
        indicator.previousStep()
        currentStep = indicator.currentStep
        stepChanged()
    }

    private fun stepChanged() {
        if (autoHideButtons) {
            leftButton.visibility = if (currentStep == 0) View.GONE else View.VISIBLE
            rightButton.visibility = if (currentStep == stepCount - 1) View.GONE else View.VISIBLE
        }
        indicator.update()
        listener?.onStepChanged(currentStep)
    }

    override fun onSaveInstanceState(): Parcelable? {
        val savedState = SavedState(super.onSaveInstanceState())
        savedState.currentStep = currentStep
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            currentStep = state.currentStep
            indicator.currentStep = currentStep
            stepChanged()
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    internal class SavedState : BaseSavedState {
        var currentStep: Int = 0

        constructor(source: Parcel) : super(source) {
            currentStep = source.readByte().toInt()
        }

        constructor(superState: Parcelable?) : super(superState)

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(currentStep)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }
}
