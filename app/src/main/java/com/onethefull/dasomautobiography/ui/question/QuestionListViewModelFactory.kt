package com.onethefull.dasomautobiography.ui.question

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.QuestionListRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class QuestionListViewModelFactory (
    private val context: Activity,
    private val repository: QuestionListRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(QuestionListViewModel::class.java)) {
            QuestionListViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}