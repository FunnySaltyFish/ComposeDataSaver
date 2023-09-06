package com.funny.composedatasaver.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.funny.composedatasaver.AppConfig
import com.funny.data_saver.core.mutableDataSaverStateOf

class MainViewModel: ViewModel() {
    private val dataSaver get() = AppConfig.dataSaver

    var username by dataSaverState(key = "username", initialValue = "FunnySaltyFish")
    var password by dataSaverState(key = "password", initialValue = "password")

    private inline fun <reified T> dataSaverState(key: String, initialValue: T) =
        // we pass dataSaver and custom coroutineScope(viewModelScope)
        // so that if the viewModel is cleared, the un-finished coroutine will be cancelled as well
        mutableDataSaverStateOf(dataSaver, key, initialValue, coroutineScope = viewModelScope)

    override fun onCleared() {
        super.onCleared()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}