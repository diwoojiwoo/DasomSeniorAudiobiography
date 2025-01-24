package com.onethefull.dasomautobiography.utils.robot.kebbi

import com.onethefull.dasomautobiography.utils.robot.action.RobotAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

/**
 * Created by Douner on 2023/11/28.
 */
class RobotActionKebbi : RobotAction {

    private var robotActionJob : Job? = null
    private var robotLedJob : Job? = null
    override fun startAction(action: String) {
        releaseAction()
//        robotActionJob = KebbiRobotCommand.noJamAnimation()
//        robotLedJob = KebbiRobotCommand.noJamLedJob()
    }
    override fun releaseAction() {
        robotActionJob?.cancel(CancellationException(ROBOT_CANCALE_EXCEPTION))
        robotLedJob?.cancel(CancellationException(ROBOT_CANCALE_EXCEPTION))
    }
    companion object{
        const val ROBOT_CANCALE_EXCEPTION = "release Robot Action"
    }
}