package com.luscii.stepbar

interface Indicator {
    var stepCount: Int
    var currentStep: Int
    fun nextStep()
    fun previousStep()
    fun update()
}
