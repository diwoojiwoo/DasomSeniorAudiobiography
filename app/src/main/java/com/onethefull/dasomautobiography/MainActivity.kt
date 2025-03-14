package com.onethefull.dasomautobiography

import android.os.Bundle
import android.os.Process
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.onethefull.dasomautobiography.base.BaseActivity
import com.onethefull.dasomautobiography.databinding.ActivityMainBinding
import com.onethefull.dasomautobiography.utils.logger.DWLog
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech

/**
 * Created by sjw on 2021/11/10
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private var resId: Int? = null
    lateinit var viewModel: MainViewModel

    // 웨이크업 연속어 문장 수신
    var mSentence: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host)
        setupViewModel()
        startFragment()
    }

    override fun onResume() {
        super.onResume()
        DWLog.d("MainActivity - onResume ")
        GCTextToSpeech.getInstance()?.start(this)
        App.instance.isRunning = true
        viewModel.start()
    }

    /**
     * Fragment 분리
     */
    fun startFragment() {
        DWLog.d("MainActivity - startFragment")
        start()
    }

    private fun start() {
        navController.navigate(R.id.splash_fragment, Bundle().apply {})
    }

    fun back() {
        navController.navigateUp()
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
        GCTextToSpeech.getInstance()?.release()
        App.instance.isRunning = false
        viewModel.release()
        Process.killProcess(Process.myPid())
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            MainViewModelFactory()
        ).get(MainViewModel::class.java)
    }

    companion object {}
}