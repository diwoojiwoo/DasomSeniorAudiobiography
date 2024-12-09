package com.onethefull.dasomcontents.utils

import android.content.Context
import com.onethefull.dasomcontents.MainActivity
import com.onethefull.dasomcontents.repository.MemoryContentRepository
import com.onethefull.dasomcontents.ui.memoryquiz.MemoryContentViewModelFactory

/**
 * Created by sjw on 2021/11/10
 */
object InjectorUtils {

    private fun getMemoryContentRepository(context: Context): MemoryContentRepository {
        return MemoryContentRepository.getInstance(context.applicationContext)
    }
    fun provideMemoryContentViewModelFactory(
        context: Context
    ): MemoryContentViewModelFactory {
        return MemoryContentViewModelFactory(context as MainActivity, getMemoryContentRepository(context))
    }
}