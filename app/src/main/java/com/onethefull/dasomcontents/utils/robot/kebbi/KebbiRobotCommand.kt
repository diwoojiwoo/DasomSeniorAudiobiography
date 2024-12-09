package com.onethefull.dasomcontents.utils.robot.kebbi

import com.google.gson.Gson
import com.onethefull.dasomcontents.utils.logger.DWLog
import com.onethefull.wonderfulrobotmodule.data.LedData
import com.onethefull.wonderfulrobotmodule.robot.BaseRobotController
import com.onethefull.wonderfulrobotmodule.robot.IRobotService
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay
import org.json.JSONObject

/**
 * Created by Douner on 2023/04/25.
 */
@Suppress("ObjectPropertyName")
object KebbiRobotCommand {

    /**
     * neck_y:1 20,-20
     * neck_z:2 40,-40
     * right_shoulder_z:3   5, -85
     * right_shoulder_y:4   70, -200
     * right_shoulder_x:5   100,-3
     * right_bow_y:6    0,-80
     * left_shoulder_z:7    5, -85
     * left_shoulder_y:8    70, -200
     * left_shoulder_x:9    100,-3
     * left_bow_y:10    0,-80
     *
     */

    /*  const val 오른쪽_어깨_앞뒤 = 3
        const val 오른쪽_어깨_위아래 = 4
        const val 오른쪽_어깨_측면 = 5
        const val 오른쪽_어깨_손목 = 6
        const val 왼쪽_어깨_앞뒤 = 7
        const val 왼쪽_어깨_위아래 = 8
        const val 왼쪽_어깨_측면 = 9
        const val 왼쪽_어깨_손목 = 10*/

    private fun makeRobotCommand(position: Int, degree: Int, speed: Int): String {
        return try {
            JSONObject().apply {
                this.put("position", position)
                this.put("degree", degree)
                this.put("speed", speed)
            }.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return String()
        }
    }

    fun makeLedCommand(r: Int, g: Int, b: Int) {
        Gson().toJson(
            LedData(
                redId = 0,
                green = g,
                blue = b,
                red = r,
                period = 0,
                count = 0,
                type = -0x00001,
                direction = 0
            )
        ).toString().apply {
            BaseRobotController.robotService?.setLed(this)
        }
    }

    fun testLookU(service: IRobotService) {
        while (true)  {
            service.setMotor(makeRobotCommand(2, -35, 100))
        }
    }

    suspend fun loadingAction01(service: IRobotService) {
        while (true) {
            service.setMotor(makeRobotCommand(3, -35, 100))
            service.setMotor(makeRobotCommand(4, -160, 100))
            service.setMotor(makeRobotCommand(5, -3, 100))
            service.setMotor(makeRobotCommand(6, -35, 100))
            delay(2000)
            service.robotMotor?.down(20)
            delay(1000)
            service.robotMotor?.up(20)
            delay(1000)
            service.robotMotor?.turnRight(20)
            delay(1000)
            service.robotMotor?.turnLeft(20)
            delay(1000)
            service.robotMotor.reset()
            delay(1500)
            service.setMotor(makeRobotCommand(7, -35, 100))
            service.setMotor(makeRobotCommand(8, -160, 100))
            service.setMotor(makeRobotCommand(9, -3, 100))
            service.setMotor(makeRobotCommand(10, -35, 100))
            delay(2000)
            service.robotMotor.reset()
            delay(1500)
        }
    }

    suspend fun loadingAction02(service: IRobotService) {
        while (true) {
            service.setMotor(makeRobotCommand(7, -35, 100))
            service.setMotor(makeRobotCommand(8, -160, 100))
            service.setMotor(makeRobotCommand(9, -3, 100))
            service.setMotor(makeRobotCommand(10, -35, 100))
            delay(2000)
            service.robotMotor?.down(20)
            delay(1000)
            service.robotMotor?.up(20)
            delay(1000)
            service.robotMotor?.turnRight(20)
            delay(1000)
            service.robotMotor?.turnLeft(20)
            delay(1000)
            service.robotMotor.reset()
            delay(1500)
            service.setMotor(makeRobotCommand(3, -35, 100))
            service.setMotor(makeRobotCommand(4, -160, 100))
            service.setMotor(makeRobotCommand(5, -3, 100))
            service.setMotor(makeRobotCommand(6, -35, 100))
            delay(2000)
            service.robotMotor.reset()
            delay(1500)
        }
    }

    suspend fun loadingAction03(service: IRobotService) {
        while (true) {
            delay(2000)
            service.robotMotor?.down(20)
            delay(1000)
            service.robotMotor?.up(20)
            delay(1000)
            service.robotMotor?.turnRight(20)
            delay(1000)
            service.robotMotor?.turnLeft(20)
            delay(1000)
            service.robotMotor.reset()
            delay(1500)
            service.setMotor(makeRobotCommand(7, -35, 100))
            service.setMotor(makeRobotCommand(8, -160, 100))
            service.setMotor(makeRobotCommand(9, -3, 100))
            service.setMotor(makeRobotCommand(10, -35, 100))
            delay(2000)
            service.robotMotor.reset()
            delay(1000)
            service.setMotor(makeRobotCommand(3, -35, 100))
            service.setMotor(makeRobotCommand(4, -160, 100))
            service.setMotor(makeRobotCommand(5, -3, 100))
            service.setMotor(makeRobotCommand(6, -35, 100))
            delay(2000)
            service.robotMotor.reset()
            delay(1500)
        }
    }

    val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        DWLog.d("CoroutineException :: ${throwable.message}")
    }
}