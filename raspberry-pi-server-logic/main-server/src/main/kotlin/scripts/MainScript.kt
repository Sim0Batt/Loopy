package scripts

import kotlinx.serialization.json.Json
import server.inputJsons.SaveDataJson
import java.util.concurrent.TimeUnit

object MainScript {

    private const val PYTHON_BIN = "/usr/bin/python3"
    private const val ORCHESTRATOR_PATH = "/home/loopy/sensors-logic/orchestrator.py"
    private const val SENSOR_TIMEOUT_SECONDS = 60L

    fun executeAllSensors(): SaveDataJson{
        var process: Process? = null
        try {
            val processBuilder = ProcessBuilder(PYTHON_BIN, ORCHESTRATOR_PATH)
            processBuilder.redirectErrorStream(false)
            process = processBuilder.start()
            process.waitFor(SENSOR_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            val output = process.inputStream.bufferedReader().readText()
            return Json.decodeFromString<SaveDataJson>(output)
        } catch (e: Exception) {
            println("Python execution failed: ${e.message}")
            e.printStackTrace()
            throw e
        } finally {
            process?.destroy()
        }
    }


    fun executePPGSensor(): String{
        var process: Process? = null
        val ppgScriptPath = "/home/loopy/sensors-logic/ppg_sensor.py"
        val ppgProcessBuilder = ProcessBuilder(
            "/usr/bin/python3",
            ppgScriptPath
        )
        ppgProcessBuilder.redirectErrorStream(false)
        process = ppgProcessBuilder.start()
        process.waitFor(30, TimeUnit.SECONDS)

        return if(process.exitValue() == 0){
            "success"
        }else{
            "failure"
        }
    }

    fun executeElectrodeSensor(): String{
        var process: Process? = null
        val electroceScriptPath = "/home/loopy/sensors-logic/electroce_sensor.py"
        val electroceProcessBuilder = ProcessBuilder(
            "/usr/bin/python3",
            electroceScriptPath
        )
        electroceProcessBuilder.redirectErrorStream(false)
        process = electroceProcessBuilder.start()
        process.waitFor(30, TimeUnit.SECONDS)

        return if(process.exitValue() == 0){
            "success"
        }else{
            "failure"
        }
    }

    fun executeAccelerometerSensor(): String{
        var process: Process? = null
        val accelerometerScriptPath = "/home/loopy/sensors-logic/accelerometer_sensor.py"
        val accelerometerProcessBuilder = ProcessBuilder(
            "/usr/bin/python3",
            accelerometerScriptPath
        )
        accelerometerProcessBuilder.redirectErrorStream(false)
        process = accelerometerProcessBuilder.start()
        process.waitFor(30, TimeUnit.SECONDS)

        return if(process.exitValue() == 0){
            "success"
        }else{
            "failure"
        }
    }

    fun executeThermometerSensor(): String{
        var process: Process? = null
        val termometerScriptPath = "/home/loopy/sensors-logic/termometer_sensor.py"
        val termometerProcessBuilder = ProcessBuilder(
            "/usr/bin/python3",
            termometerScriptPath
        )
        termometerProcessBuilder.redirectErrorStream(false)
        process = termometerProcessBuilder.start()
        process.waitFor(30, TimeUnit.SECONDS)

        return if(process.exitValue() == 0){
            "success"
        }else{
            "failure"
        }
    }
}