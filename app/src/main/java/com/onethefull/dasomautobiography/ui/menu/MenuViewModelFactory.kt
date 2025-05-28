package com.onethefull.dasomautobiography.ui.menu

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.onethefull.dasomautobiography.repository.MenuRepository

/**
 * Created by sjw on 2025. 1. 9.
 */
class MenuViewModelFactory (
    private val context: Activity,
    private val repository: MenuRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            MenuViewModel(context, repository) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}