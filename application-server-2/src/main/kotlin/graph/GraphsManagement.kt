package graph

import database.QueryManager
import org.jetbrains.kotlinx.kandy.dsl.categorical
import org.jetbrains.kotlinx.kandy.dsl.plot
import org.jetbrains.kotlinx.kandy.letsplot.export.save
import org.jetbrains.kotlinx.kandy.letsplot.feature.layout
import org.jetbrains.kotlinx.kandy.letsplot.layers.bars
import org.jetbrains.kotlinx.kandy.letsplot.scales.guide.LegendType
import org.jetbrains.kotlinx.kandy.util.color.Color
import org.jetbrains.kotlinx.kandy.util.context.invoke
import java.io.File

object GraphsManagement {
    fun generateStressGraph(userId: Int, path: String){
        if(!File(path).exists()){
            File(path).mkdirs()
        }

        val map = QueryManager.getStressData(userId)
        val hours = (0..23).map {
            if(it < 10) "0$it" else it.toString()
        }

        val stressMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        val hourInts = hours.map { it.toInt() }
        hourInts.forEach { stressMap[it] = mutableListOf() }

        map.forEach { (key, value) ->
            val hour = key.substring(11,13).toInt()
            if (hour in 0..23) stressMap[hour]!!.add(value)
        }

        val stressLevels = hourInts.map { h ->
            val v = stressMap[h]!!
            if (v.isNotEmpty()) v.average() else 0.0
        }

        val xPos = hourInts.map { it + 0.5 }



        val colors = mutableListOf<String>()
        stressLevels.forEach {
            when{
                it < 33.3 ->{
                    colors.add("green")
                }
                it < 66.6 ->{
                    colors.add("orange")
                }
                it >= 66.6 ->{
                    colors.add("red")
                }
            }
        }

        plot {
            bars {
                x(xPos, "Hours") {
                    axis {
                        breaksLabeled(
                            (0..24).map { it.toDouble() },
                            (0..24).map { h -> "%02d:00".format(h) }
                        )
                    }
                }
                y(stressLevels.toList(), "Stress Level"){
                    axis{
                        breaksLabeled(
                            listOf(30.0, 60.0, 90.0),
                            listOf("Calm", "Medium", "High")
                        )
                    }
                }

                fillColor(colors, "Level") {
                    scale = categorical(
                        listOf(Color.GREEN, Color.ORANGE, Color.RED),
                        listOf("green", "orange", "red"),
                    )
                    legend.type = LegendType.None
                }
                borderLine.width = 0.0
                width = 1.0
            }

            layout {
                title = "Stress"
                size = 1000 to 600
            }
        }.save("$path/stress_graph.png")
    }

    fun generateActivityGraph(userId: Int, path: String){
        val map = QueryManager.getActivityData(userId)
        val hours = (0..23).map {
            if(it < 10) "0$it" else it.toString()
        }

        val activityMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        val hourInts = hours.map { it.toInt() }
        hourInts.forEach { activityMap[it] = mutableListOf() }

        map.forEach { (key, value) ->
            val hour = key.substring(11,13).toInt()
            if (hour in 0..23) activityMap[hour]!!.add(value)
        }

        val activityLevels = hourInts.map { h ->
            val v = activityMap[h]!!
            if (v.isNotEmpty()) v.average() else 0.0
        }

        val xPos = hourInts.map { it + 0.5 }

        val colors = mutableListOf<String>()
        activityLevels.forEach {
            when{
                it < 10 ->{
                    colors.add("blue")
                }
                it < 20 ->{
                    colors.add("green")
                }
                it < 50 ->{
                    colors.add("orange")
                }
                it >= 50 ->{
                    colors.add("red")
                }
            }
        }

        plot {
            bars {
                x(activityMap.keys.toList().map { "$it:00" }, "Hours") {
                    scale = categorical()
                }
                y(activityLevels.toList(), "Activity Level"){
                    axis{
                        breaksLabeled(
                            listOf(0.0, 20.0, 50.0, 80.0),
                            listOf("Sedentary", "Light", "Moderate", "Intense")
                        )
                    }
                }

                fillColor(colors, "Level") {
                    scale = categorical(
                        listOf(Color.BLUE, Color.GREEN, Color.ORANGE, Color.RED),
                        listOf("blue", "green", "orange", "red"),
                    )
                    legend.type = LegendType.None
                    width = 1.0
                }
                borderLine.width = 0.0
            }

            layout {
                title = "Activity"
                size = 1000 to 600
            }
        }.save("$path/activity_graph.png")
    }


    fun generateSleepGraph(userId: Int, path: String){
        val yesterdayMap = QueryManager.getYesterdaySleepData(userId)
        val todayMap = QueryManager.getTodaySleepData(userId)

        val hourInts = (0..23).toList()

        // --- Build maps with Int keys (0..23)
        val yesterdaySleepMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        hourInts.forEach { yesterdaySleepMap[it] = mutableListOf() }

        yesterdayMap.forEach { (key, value) ->
            val hour = key.substring(11,13).toInt()
            if (hour in 0..23) yesterdaySleepMap[hour]!!.add(value)
        }

        val yesterdaySleepLevels = hourInts.map { h ->
            val v = yesterdaySleepMap[h]!!
            if (v.isNotEmpty()) v.average() else 0.0
        }

        val todaySleepMap: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        hourInts.forEach { todaySleepMap[it] = mutableListOf() }

        todayMap.forEach { (key, value) ->
            val hour = key.substring(11,13).toInt()
            if (hour in 0..23) todaySleepMap[hour]!!.add(value)
        }

        val todaySleepLevels = hourInts.map { h ->
            val v = todaySleepMap[h]!!
            if (v.isNotEmpty()) v.average() else 0.0
        }

        val xYesterday = hourInts.map { (it + 0.5) }
        val xToday = hourInts.map { (it + 24 + 0.5) }
        val xPos = xYesterday + xToday

        val sleepLevels = yesterdaySleepLevels + todaySleepLevels

        val colors = mutableListOf<String>()
        sleepLevels.forEach {
            when {
                it < 20 -> colors.add("blue")
                it < 50 -> colors.add("green")
                it < 80 -> colors.add("purple")
                else -> colors.add("dark blue")
            }
        }

        plot {
            bars {
                x(xPos, "Hours") {
                    axis {
                        val breaks = (0..48).map { it.toDouble() }
                        val labels = (0..48).map { h ->
                            when {
                                h <= 24 -> "Y%02d:00".format(h)
                                else -> "T%02d:00".format(h - 24)
                            }
                        }
                        breaksLabeled(breaks, labels)
                    }
                }

                y(sleepLevels, "Sleep Level") {
                    axis {
                        breaksLabeled(
                            listOf(0.0, 20.0, 50.0, 80.0),
                            listOf("Woke Up", "Light", "REM", "Deep")
                        )
                    }
                }

                fillColor(colors, "Level") {
                    scale = categorical(
                        listOf(Color.BLUE, Color.GREEN, Color.PURPLE, Color.hex("#042a66")),
                        listOf("blue", "green", "purple", "dark blue"),
                    )
                    legend.type = LegendType.None
                }

                borderLine.width = 0.0
                width = 1.0
            }

            layout {
                title = "Sleep"
                size = 1000 to 600
            }
        }.save("$path/sleep_graph.png")
    }
}