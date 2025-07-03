package io.github.msaggik.home.presentation.view_model

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.github.msaggik.home.data.mappers.HomeMappers
import io.github.msaggik.home.domain.model.device.LightingModePoly
import io.github.msaggik.home.domain.use_case.InteractorDatabaseLightingMode
import io.github.msaggik.home.presentation.view_model.state.ColorsImageState
import io.github.msaggik.home.presentation.view_model.state.LightingModeState
import io.github.msaggik.util.extractColorsByUriImage

private const val TAG = "NewLightingModeViewModel"

class NewLightingModeViewModel(
    private val application: Application,
    private val interactorDatabaseLightingMode: InteractorDatabaseLightingMode
): AndroidViewModel(application) {

    // image uri lighting mode
    var imageCompositionURI: Uri? = null

    private val colorsImageLiveData = MutableLiveData<ColorsImageState>()
    fun getColorsImageLiveData(): LiveData<ColorsImageState> = colorsImageLiveData

    fun setColorsImageLiveData() {
        viewModelScope.launch(Dispatchers.IO) {
            colorsImageLiveData.postValue(ColorsImageState.Loading)
            try {
                imageCompositionURI?.let { imageURI ->
                    val colors = application.extractColorsByUriImage(imageURI)
                    colorsImageLiveData.postValue(ColorsImageState.Content(colors.map { HomeMappers.map(it) }))
                } ?: run {
                    colorsImageLiveData.postValue(ColorsImageState.Empty)
                }
            } catch (e: Exception) {
                colorsImageLiveData.postValue(ColorsImageState.Error(e.message.toString()))
                Log.e(TAG, "[setColorsImageLiveData()] error: ${e.message.toString()}")
            }
        }
    }

    // db
    private val lightingModeLiveData = MutableLiveData<LightingModeState>()
    fun getLightingModeLiveData(): LiveData<LightingModeState> = lightingModeLiveData

    fun getLightingMode(idLightingMode: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            lightingModeLiveData.postValue(LightingModeState.Loading)
            interactorDatabaseLightingMode.getLightingMode(idLightingMode)
                .collect { lightingMode ->
                    lightingModeLiveData.postValue(LightingModeState.Content(lightingMode))
                }
        }
    }

    fun setLightingMode(lightingModePoly: LightingModePoly) {
        viewModelScope.launch(Dispatchers.IO) {
            interactorDatabaseLightingMode.setLightingMode(lightingModePoly)
        }
    }
}