package com.onethefull.dasomcontents.base

import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.onethefull.dasomcontents.App
import com.onethefull.dasomcontents.utils.logger.DWLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

open class BaseActivity : AppCompatActivity() {

    @Volatile
    protected var isNeedProcessKill = true
    private var isStartActivity = false
//    private lateinit var mAnimationDialog: AnimationDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.instance.getLocale()
        App.instance.currentActivity = this
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        mAnimationDialog = AnimationDialog(this)
    }

    override fun onResume() {
        super.onResume()
        isStartActivity = false
    }

    override fun onPause() {
        super.onPause()
        App.instance.currentActivity = null
    }

    override fun startActivity(intent: Intent?) {
        isStartActivity = true
        intent?.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        super.startActivity(intent)
    }

    fun showLoading(isShow: Boolean) {
//        mAnimationDialog.showDialog(isShow)
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.currentFocus?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }

    protected fun startActivity(intent:Intent, isNeedKillProcess:Boolean){
        this.isNeedProcessKill = isNeedKillProcess
        startActivity(intent)
    }


    protected suspend fun killProcess() {
        delay(2000)
        withContext(Dispatchers.Main) {
            DWLog.d("${this::class.java.simpleName} : killProcess")
            Process.killProcess(Process.myPid())
        }
    }
}