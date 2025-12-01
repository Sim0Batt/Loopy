package database.dao

import database.tables.graph.TabellaSleepTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaSleepEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaSleepEntity>(TabellaSleepTable)

    var rhr_on_sleep by TabellaSleepTable.rhr_on_sleep
    var total_sleep_min by TabellaSleepTable.total_sleep_min
    var deep_sleep_min by TabellaSleepTable.deep_sleep_min
    var light_sleep_min by TabellaSleepTable.light_sleep_min
    var rem_sleep_min by TabellaSleepTable.rem_sleep_min
    var sleep_graph_json by TabellaSleepTable.sleep_graph_json
    var userId by TabellaSleepTable.userId
}