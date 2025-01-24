package com.onethefull.dasomautobiography.utils

import android.content.Context
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.repository.DiaryRepository
import com.onethefull.dasomautobiography.ui.diary.DiaryViewModelFactory
/**
 * Created by sjw on 2021/11/10
 */
object InjectorUtils {
    private fun getDiaryRepository(context : Context) : DiaryRepository {
        return DiaryRepository.getInstance(context.applicationContext)
    }

    fun provideDiaryViewModelFactory(
        context : Context
    ) : DiaryViewModelFactory {
        return DiaryViewModelFactory(context as MainActivity, getDiaryRepository(context))
    }
}