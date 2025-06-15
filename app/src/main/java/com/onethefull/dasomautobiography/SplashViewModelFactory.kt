package com.onethefull.dasomautobiography

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.SplashRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class SplashViewModelFactory (
    private val context: Activity,
    private val repository: SplashRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
            SplashViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}