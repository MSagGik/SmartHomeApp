package io.github.msaggik.home.presentation.view_model.pager_view_model

import android.util.Log
import androidx.lifecycle.ViewModel

private const val TAG = "AutoViewModel"

class AutoViewModel () : ViewModel() {

    override fun onCleared() {
        super.onCleared()
        Log.i(TAG, "[onCleared] ViewModel cleared, stopping connection updates")
    }
}