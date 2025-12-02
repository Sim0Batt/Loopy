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

        val map = QueryManager.getStressData(userId)
        val hours = (0..24).map {
            if(it < 10) "0$it" else it.toString()
        }

        val stressMap: MutableMap<String, MutableList<Int>> = mutableMapOf()
        hours.forEach { stressMap[it] = mutableListOf() }

        map.forEach { (key, value) ->
            val hour = key.substring(11,13)
            println("$hour: $key")
            stressMap[hour]!!.add(value)
        }

        println("Livelli: ${map.values.toList().joinToString(", ")}")
        println("Orario: ${map.keys.toList().joinToString(", ")}")

        val meanActivities = stressMap.values.map {
            if(it.isNotEmpty()) it.sum() / it.size else 0
        }
        println(meanActivities.toList().joinToString(", "))

        println("Final map keys: ${stressMap.keys.toList().joinToString(", ")}; values: ${stressMap.values.toList().joinToString(", ")}")

        val dataset = mapOf(
            "fascia_oraria" to stressMap.keys.toList().map {
                "$it:00"
            },
            "livello attività" to stressMap.values.toList()
        )


        plot (dataset) {
            x("fascia_oraria")
            y("livello attività")

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