package com.onethefull.dasomautobiography

import android.os.Bundle
import android.os.Process
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.onethefull.dasomautobiography.base.BaseActivity
import com.onethefull.dasomautobiography.base.OnethefullBase
import com.onethefull.dasomautobiography.utils.speech.GCTextToSpeech
import com.onethefull.dasomautobiography.databinding.ActivityMainBinding
import com.onethefull.dasomautobiography.utils.logger.DWLog

/**
 * Created by sjw on 2021/11/10
 */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var navController: NavController
    private var resId: Int? = null
    private lateinit var viewModel: MainViewModel
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
        when {
            intent.hasExtra(OnethefullBase.PARAM_ACTION_NAME) -> {
                val action = intent.getStringExtra(OnethefullBase.PARAM_ACTION_NAME)
                startDiary()
            }

            else -> {
                startDiary()
            }
        }
    }

    private fun startDiary() {
        if (navController.currentDestination?.id == R.id.main_fragment) {
            navController.navigate(R.id.diary_fragment, Bundle().apply {
            })
        }
    }

    private fun navigateFragment(resId: Int) {
        navController.navigate(resId)
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

    companion object {
        const val MIN_CLICK_INTERVAL = 10 * 1000L
    }
}