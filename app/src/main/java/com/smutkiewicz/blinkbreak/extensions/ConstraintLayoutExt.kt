package com.smutkiewicz.blinkbreak.extensions

import android.support.constraint.ConstraintLayout
import android.widget.CheckBox

/**
 * Utility function. Sets isEnabled property for all given layout's children,
 * excluding checkboxes. They are used for setting isEnabled layout's property.
 */
fun ConstraintLayout.setIsEnabledForChildren(isEnabled: Boolean) {
    (0 until childCount)
        .map { getChildAt(it) }
        .forEach {
            if (it !is CheckBox) {
                it.isEnabled = isEnabled
            }
        }
}