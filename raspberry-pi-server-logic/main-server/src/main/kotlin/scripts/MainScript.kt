package scripts

import java.util.concurrent.TimeUnit

object MainScript {
    fun executeAllSensors(): String{
        var process: Process? = null
        try {
            val termometerScriptPath = "/home/loopy/sensors-logic/termometer_sensor.py"
            val termometerProcessBuilder = ProcessBuilder(
                "/usr/bin/python3",
                termometerScriptPath
            )
            termometerProcessBuilder.redirectErrorStream(false)
            process = termometerProcessBuilder.start()
            process.waitFor(30, TimeUnit.SECONDS)
            if(process.exitValue() != 0) return "failure"

            val electrodesScriptPath = "/home/loopy/sensors-logic/electrode_sensor.py"
            val electrodesProcessBuilder = ProcessBuilder(
                "/usr/bin/python3",
                electrodesScriptPath
            )
            electrodesProcessBuilder.redirectErrorStream(false)
            process = electrodesProcessBuilder.start()
            process.waitFor(30, TimeUnit.SECONDS)
            if(process.exitValue() != 0) return "failure"

            val PPGScriptPath = "/home/loopy/sensors-logic/ppg_sensor.py"
            val PPGProcessBuilder = ProcessBuilder(
                "/usr/bin/python3",
                PPGScriptPath
            )
            PPGProcessBuilder.redirectErrorStream(false)
            process = PPGProcessBuilder.start()
            process.waitFor(30, TimeUnit.SECONDS)
            if(process.exitValue() != 0) return "failure"

            val accelerometerScriptPath = "/home/loopy/sensors-logic/accelerometer_sensor.py"
            val accelerometerProcessBuilder = ProcessBuilder(
                "/usr/bin/python3",
                accelerometerScriptPath
            )
            accelerometerProcessBuilder.redirectErrorStream(false)
            process = accelerometerProcessBuilder.start()
            process.waitFor(30, TimeUnit.SECONDS)
            if(process.exitValue() != 0) return "failure"

            return "success"

        } catch (e: Exception) {
            println("Python execution failed: ${e.message}")
            e.printStackTrace()
            return "failure"
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