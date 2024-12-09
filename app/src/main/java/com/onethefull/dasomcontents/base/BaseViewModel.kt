package com.onethefull.dasomcontents.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Created by sjw on 2021/11/12
 */
open class BaseViewModel : ViewModel() {
    private val job = SupervisorJob()
    protected val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }
}