package com.onethefull.dasomautobiography.utils

import android.content.Context
import com.onethefull.dasomautobiography.MainActivity
import com.onethefull.dasomautobiography.repository.QuestionListRepository
import com.onethefull.dasomautobiography.repository.DiaryRepository
import com.onethefull.dasomautobiography.repository.MenuRepository
import com.onethefull.dasomautobiography.repository.QuestionDetailRepository
import com.onethefull.dasomautobiography.repository.SpeechRepository
import com.onethefull.dasomautobiography.ui.diary.DiaryViewModelFactory
import com.onethefull.dasomautobiography.ui.menu.MenuViewModelFactory
import com.onethefull.dasomautobiography.ui.question.QuestionListViewModel
import com.onethefull.dasomautobiography.ui.question.QuestionListViewModelFactory
import com.onethefull.dasomautobiography.ui.questiondetail.QuestionDetailViewModel
import com.onethefull.dasomautobiography.ui.questiondetail.QuestionDetailViewModelFactory
import com.onethefull.dasomautobiography.ui.speech.SpeechViewModelFactory

/**
 * Created by sjw on 2021/11/10
 */
object InjectorUtils {
    private fun getSpeechRepository(context: Context) : SpeechRepository {
        return SpeechRepository.getInstance(context.applicationContext)
    }

    fun provideSpeechViewModelFactory(
        context : Context
    ) : SpeechViewModelFactory {
        return SpeechViewModelFactory(context as MainActivity, getSpeechRepository(context))
    }

    private fun getQuestionListRepository(context: Context) : QuestionListRepository {
        return QuestionListRepository.getInstance(context.applicationContext)
    }

    fun provideQuestionListViewModelFactory(
        context : Context
    ) : QuestionListViewModelFactory {
        return QuestionListViewModelFactory(context as MainActivity, getQuestionListRepository(context))
    }

    private fun getMenuRepository(context: Context) : MenuRepository {
        return MenuRepository.getInstance(context.applicationContext)
    }

    fun provideMenuViewModelFactory(
        context : Context
    ) : MenuViewModelFactory {
        return MenuViewModelFactory(context as MainActivity, getMenuRepository(context))
    }

    private fun getDiaryRepository(context : Context) : DiaryRepository {
        return DiaryRepository.getInstance(context.applicationContext)
    }

    fun provideDiaryViewModelFactory(
        context : Context
    ) : DiaryViewModelFactory {
        return DiaryViewModelFactory(context as MainActivity, getDiaryRepository(context))
    }

    private fun getQuestionDetailRepository(context: Context) : QuestionDetailRepository {
        return QuestionDetailRepository.getInstance(context.applicationContext)
    }

    fun provideQuestionDetailModelFactory(
        context : Context
    ) : QuestionDetailViewModelFactory {
        return QuestionDetailViewModelFactory(context as MainActivity, getQuestionDetailRepository(context))
    }
 }