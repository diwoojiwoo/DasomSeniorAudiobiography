package com.onethefull.dasomautobiography.ui.questiondetail

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.QuestionDetailRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class QuestionDetailViewModelFactory (
    private val context: Activity,
    private val repository: QuestionDetailRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(QuestionDetailViewModel::class.java)) {
            QuestionDetailViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}