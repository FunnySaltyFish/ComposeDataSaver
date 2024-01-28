package com.funny.data_saver.ui

import com.funny.data_saver.AppConfig
import com.funny.data_saver.core.mutableDataSaverStateOf
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

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