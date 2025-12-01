package database.dao

import database.tables.graph.TabellaDayTimeActivityTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaDayTimeActivityEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaDayTimeActivityEntity>(TabellaDayTimeActivityTable)

    var sedentary_activity_min by TabellaDayTimeActivityTable.sedentary_activity_min
    var light_activity_min by TabellaDayTimeActivityTable.light_activity_min
    var moderate_activity_min by TabellaDayTimeActivityTable.moderate_activity_min
    var intense_activity_min by TabellaDayTimeActivityTable.intense_activity_min
    var activity_graph_json by TabellaDayTimeActivityTable.activity_graph_json
    var userId by TabellaDayTimeActivityTable.userId
}