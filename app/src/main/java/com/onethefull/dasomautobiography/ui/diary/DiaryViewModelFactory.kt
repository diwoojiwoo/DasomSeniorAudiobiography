package com.onethefull.dasomautobiography.ui.diary

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.DiaryRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class DiaryViewModelFactory (
    private val context: Activity,
    private val repository: DiaryRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            DiaryViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}