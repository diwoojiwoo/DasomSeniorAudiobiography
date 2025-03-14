package com.onethefull.dasomautobiography.ui.speech

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.SpeechRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class SpeechViewModelFactory (
    private val context: Activity,
    private val repository: SpeechRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SpeechViewModel::class.java)) {
            SpeechViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}