package io.github.msaggik.settings.presentation.view_model.appdata

import io.github.msaggik.settings.domain.model.SmartDevice

object AppDataSettings {
    val deviceListSaved: MutableList<SmartDevice> = mutableListOf()
    val deviceListSearched: MutableList<SmartDevice> = mutableListOf()
}