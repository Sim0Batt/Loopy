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

object GraphsManagement {
    fun generateStressGraph(userId: Int){

        val map = QueryManager.getStressData(userId)
        val hours = (0..24).map {
            if(it < 10) "0$it" else it.toString()
        }

        val stressMap: MutableMap<String, MutableList<Int>> = mutableMapOf()
        hours.forEach { stressMap[it] = mutableListOf() }

        map.forEach { (key, value) ->
            val hour = key.substring(11,13)
            stressMap[hour]!!.add(value)
        }

        val stressLevels = stressMap.values.map {
            if (it.isNotEmpty()) it.average() else 0.0
        }

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
                x(stressMap.keys.toList().map { "$it:00" }, "Hours") {
                    scale = categorical()
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
            }

            layout {
                title = "Stress"
                size = 1000 to 600
            }
        }.save("/home/simone/stress_graph.png")
    }

    fun generateActivityGraph(userId: Int){
        val map = QueryManager.getActivityData(userId)
        val hours = (0..24).map {
            if(it < 10) "0$it" else it.toString()
        }

        val stressMap: MutableMap<String, MutableList<Int>> = mutableMapOf()
        hours.forEach { stressMap[it] = mutableListOf() }

        map.forEach { (key, value) ->
            val hour = key.substring(11,13)
            stressMap[hour]!!.add(value)
        }

        val stressLevels = stressMap.values.map {
            if (it.isNotEmpty()) it.average() else 0.0
        }

        val colors = mutableListOf<String>()
        stressLevels.forEach {
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
                x(stressMap.keys.toList().map { "$it:00" }, "Hours") {
                    scale = categorical()
                }
                y(stressLevels.toList(), "Activity Level"){
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
                }
                borderLine.width = 0.0
            }

            layout {
                title = "Activity"
                size = 1000 to 600
            }
        }.save("/home/simone/activity_graph.png")
    }


    fun generateSleepGraph(userId: Int){
        val map = QueryManager.getSleepData(userId)
        val hours = (0..24).map {
            if(it < 10) "0$it" else it.toString()
        }
        


        val stressMap: MutableMap<String, MutableList<Int>> = mutableMapOf()
        hours.forEach { stressMap[it] = mutableListOf() }

        map.forEach { (key, value) ->
            val hour = key.substring(11,13)
            stressMap[hour]!!.add(value)
        }

        val stressLevels = stressMap.values.map {
            if (it.isNotEmpty()) it.average() else 0.0
        }

        val colors = mutableListOf<String>()
        stressLevels.forEach {
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
                x(stressMap.keys.toList().map { "$it:00" }, "Hours") {
                    scale = categorical()
                }
                y(stressLevels.toList(), "Activity Level"){
                    axis{
                        breaksLabeled(
                            listOf(0.0, 20.0, 50.0, 80.0),
                            listOf("Woke Up", "Light", "REM", "Deep")
                        )
                    }
                }

                fillColor(colors, "Level") {
                    scale = categorical(
                        listOf(Color.BLUE, Color.GREEN, Color.ORANGE, Color.RED),
                        listOf("blue", "green", "orange", "red"),
                    )
                    legend.type = LegendType.None
                }
                borderLine.width = 0.0
            }

            layout {
                title = "Activity"
                size = 1000 to 600
            }
        }.save("/home/simone/sleep_graph.png")
    }
}