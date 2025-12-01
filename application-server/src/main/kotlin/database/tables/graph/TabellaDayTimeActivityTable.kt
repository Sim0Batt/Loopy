package database.tables.graph

import org.jetbrains.exposed.dao.id.IntIdTable

object TabellaDayTimeActivityTable : IntIdTable("DayTimeActivity") {
    val sedentary_activity_min = integer("sedentary_activity_min")
    val light_activity_min = integer("light_activity_min")
    val moderate_activity_min = integer("moderate_activity_min")
    val intense_activity_min = integer("intense_activity_min")
    val activity_graph_json = text("activity_graph_json")
    val userId = integer("userId")
}