package graph

import database.DatabaseConfig
import database.QueryManager
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.x
import org.jetbrains.kotlinx.kandy.letsplot.y
import org.jetbrains.kotlinx.kandy.util.color.Color
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object GraphsManagement {
    fun generateGraph(userId: Int){
        val data = QueryManager.getDayDatas(DatabaseConfig.getConfig(), userId)
        println("Data: $data")
        val hr = data.heartRates.split(",").map { it.toInt() }
        val oxigen = data.oxygens.split(",").map { it.toDouble() }
        val ppgTimestamps = data.timestampsPPG.split(",").map {
            val tmp = Instant.parse(it)
            LocalDateTime.ofInstant(tmp, ZoneId.systemDefault())
        }
        val hrMap = hr.zip(ppgTimestamps).toMap()
        val oxigenMap = oxigen.zip(ppgTimestamps).toMap()


        val sweatings = data.sweatings.split(",").map { it.toDouble() }
        val electrodesTimestamp = data.timestampsElectrodes.split(",").map {
            val tmp = Instant.parse(it)
            LocalDateTime.ofInstant(tmp, ZoneId.systemDefault())
        }
        val sweatingMap = sweatings.zip(electrodesTimestamp).toMap()


        val temperatures = data.temperatures.split(",").map { it.toDouble() }
        val termometerTimestamps = data.timestampsTermometer.split(",").map {
            val tmp = Instant.parse(it)
            LocalDateTime.ofInstant(tmp, ZoneId.systemDefault())
        }
        val temperatureMap = temperatures.zip(termometerTimestamps).toMap()

        val movements = data.movements.split(",").map {
            it == "true"
        }
        val accelerometerTimestamps = data.timestampsAccelerometer.split(",").map {
            val tmp = Instant.parse(it.trim()).minusSeconds(3600)
            LocalDateTime.ofInstant(tmp, ZoneId.systemDefault())
        }
        val tmpMovementMap = accelerometerTimestamps.zip(movements).toMap()

        val movementMap: MutableMap<String, Int> = mutableMapOf()
        (0..24).forEach { hour ->
            if(hour < 10){
                movementMap["0$hour"] = 0
            }else{
                movementMap[hour.toString()] = 0
            }
        }
        println(movementMap)
        println(tmpMovementMap)
        println("Count: ${tmpMovementMap.count()}")

         tmpMovementMap.forEach { (key, value) ->
            val hour = key.toString().substring(11,13)
            println("$key -> $hour")
            movementMap[hour] = movementMap[hour]!! + 1
        }
        println(movementMap)


        val dataset = mapOf(
            "fascia_oraria" to movementMap.keys.map {
                "$it:00"
            },
            "passi" to movementMap.values.toList()
        )


        plot (dataset) {
            x("fascia_oraria")
            y("passi")

            bars {
                fillColor = Color.hex("#4CAF50")
                width = 0.5
            }

            layout {
                title = "Passi Giornalieri"
                size = 600 to 400
            }
        }.save("/home/simone/grafico_passi.png")
    }
}