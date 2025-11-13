package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date
// tabella per salvare i dati "complessi"
object TabellaRiepilogoGiornalieroTable : IntIdTable("daily_summary") {
    val userId = integer("user_id").references(TabellaUserTable.id)
    val data = date("data")

    val hrv = integer("hrv_rmssd").nullable()
    val stress = integer("stress_score").nullable()
    val passi = integer("passi_totali").nullable() // o qualsiasi altra cosa con acc. e giroscopio
    val recupero = integer("recupero_score").nullable()
    val vo2max = integer("vo2max").nullable()
    //TODO: da aggiungere tutti i vari cosi che si calcolano effettivamente

    init {
        uniqueIndex(userId, data)
    }
}