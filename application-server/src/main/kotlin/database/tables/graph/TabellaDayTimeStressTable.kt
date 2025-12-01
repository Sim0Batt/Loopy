package database.tables.graph

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaDayTimeStressTable : IntIdTable("DayTimeStress") {
    val calm_stress_min = integer("calm_stress_min")
    val medium_stress_min = integer("medium_stress_min")
    val high_stress_min = integer("high_stress_min")
    val stress_graph_json = text("stress_graph_json")
    val userId = integer("userId")
}