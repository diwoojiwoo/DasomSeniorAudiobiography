package com.onethefull.dasomautobiography

import android.os.Bundle
import android.os.Process
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.onethefull.dasomautobiography.base.BaseActivity
import com.onethefull.dasomautobiography.databinding.ActivityMainBinding
import com.onethefull.dasomautobiography.utils.Constant
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
    private var isGraphSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DWLog.d("MainActivity - onCreate ")
        binding = setContentView(this, R.layout.activity_main)
        navController = Navigation.findNavController(this, R.id.nav_host)
        setupViewModel()
    }

    override fun onResume() {
        super.onResume()
        DWLog.d("MainActivity - onResume ")
        GCTextToSpeech.getInstance()?.start(this)
        App.instance.isRunning = true

        if (!isGraphSet) {
            val isMotionDetected = intent.getBooleanExtra(Constant.PARAM_MOTION_DETECTED, false)
            val isEffectOn = intent.getBooleanExtra(Constant.PARAM_EFFECT_ON, false)
            val bundle = Bundle().apply {
                putBoolean(Constant.PARAM_MOTION_DETECTED, isMotionDetected)
                putBoolean(Constant.PARAM_EFFECT_ON, isEffectOn)
            }
            navController.setGraph(R.navigation.nav_graph, bundle)
            isGraphSet = true
        }

        viewModel.start()
    }

    fun navigateToSpeechFragment(reason: String? = null, motionDetected: Boolean = false) {
        val bundle = bundleOf(
            Constant.KEY_MOVE_REASON to reason,
            Constant.PARAM_MOTION_DETECTED to motionDetected
        )
        supportFragmentManager.findFragmentById(R.id.nav_host)
            ?.findNavController()
            ?.navigate(R.id.action_global_speechFragment, bundle)
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
//        Process.killProcess(Process.myPid())
    }

    private fun setupViewModel() {
        viewModel = ViewModelProviders.of(
            this,
            MainViewModelFactory()
        ).get(MainViewModel::class.java)
    }

    companion object {}
}