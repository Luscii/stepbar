package com.luscii.stepbar

import android.content.res.Resources

object Utils {
    val Int.dp: Int
        get() = (this / Resources.getSystem().displayMetrics.density).toInt()
}
