package com.luscii.stepbar

class NoIndicator : Indicator {

    override var stepCount: Int
        get() = 0
        set(value) {}
    override var currentStep: Int
        get() = 0
        set(value) {}

    override fun nextStep() {}

    override fun previousStep() {}

    override fun update() {}
}
