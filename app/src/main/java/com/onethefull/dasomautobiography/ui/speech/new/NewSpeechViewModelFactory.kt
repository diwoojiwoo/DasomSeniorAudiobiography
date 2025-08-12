package com.onethefull.dasomautobiography.ui.speech.new

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.NewSpeechRepository
import com.onethefull.dasomautobiography.repository.SpeechRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class NewSpeechViewModelFactory (
    private val context: Activity,
    private val repository: NewSpeechRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NewSpeechViewModel::class.java)) {
            NewSpeechViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}