package com.onethefull.dasomcontents.ui.memoryquiz

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomcontents.repository.MemoryContentRepository

/**
 * Created by sjw on 2024. 12. 4.
 */
class MemoryContentViewModelFactory (
    private val context: Activity,
    private val repository: MemoryContentRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MemoryContentViewModel::class.java)) {
            MemoryContentViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}