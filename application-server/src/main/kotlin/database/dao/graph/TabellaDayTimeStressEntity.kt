package database.dao

import database.tables.graph.TabellaDayTimeStressTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TabellaDayTimeStressEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TabellaDayTimeStressEntity>(TabellaDayTimeStressTable)

    var calm_stress_min by TabellaDayTimeStressTable.calm_stress_min
    var medium_stress_min by TabellaDayTimeStressTable.medium_stress_min
    var high_stress_min by TabellaDayTimeStressTable.high_stress_min
    var stress_graph_json by TabellaDayTimeStressTable.stress_graph_json
    var userId by TabellaDayTimeStressTable.userId
}