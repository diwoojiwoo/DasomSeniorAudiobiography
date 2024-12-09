package com.onethefull.dasomcontents.contents.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

/**
 * Created by Douner on 2023/11/28.
 */
abstract class BaseDialog<T:ViewBinding>(context: Context) : Dialog(context){

    protected lateinit var _binding : T
    protected val binding get() = _binding
    protected inline fun <reified T : ViewBinding> setViewBinding(layoutInflater: LayoutInflater): T {
        val binding = T::class.java.getMethod("inflate", LayoutInflater::class.java)
            .invoke(null, layoutInflater) as T
        setContentView(binding.root)
        return binding
    }
}