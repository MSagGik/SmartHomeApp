package io.github.msaggik.util

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.EditText

object ViewUtils {}

fun View.showAndHideOthers(hiddenViews: Array<View>?) {
    hiddenViews?.let { views ->
        views.forEach {
            it.visibility = View.GONE
        }
    }
    visibility = View.VISIBLE
}

fun Context.convertDpToPx(dp: Float): Int =
    TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        resources.displayMetrics
    ).toInt()

fun EditText.onIsNullOrEmptyChange(callback: (Boolean) -> Unit): TextWatcher {
    callback(text.isNullOrEmpty())

    return addTextChangedListenerAfter { editable ->
        callback(editable.isNullOrEmpty())
    }
}

inline fun EditText.addTextChangedListenerAfter(
    crossinline afterTextChanged: (Editable?) -> Unit
): TextWatcher {
    val watcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) = afterTextChanged(s)
    }
    addTextChangedListener(watcher)
    return watcher
}