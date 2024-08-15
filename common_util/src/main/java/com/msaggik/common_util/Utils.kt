package com.msaggik.common_util

import android.view.View

object Utils {

    fun visibilityView(views: Array<View>? = emptyArray(), v: View) {
        if (views != null) {
            for(view in views) view.visibility = View.GONE
        }
        v.visibility = View.VISIBLE
    }
}