package database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.date

// Tabella per salvare i dati "complessi" calcolati dagli script Python
object TabellaRiepilogoGiornalieroTable : IntIdTable("daily_summary") {
    val userId = integer("user_id").references(TabellaUserTable.id)
    val data = date("data")

    // metriche base
    val hrv = integer("hrv_rmssd").nullable()
    val stress = integer("stress_score").nullable()
    val passi = integer("passi_totali").nullable()
    val recupero = integer("recupero_score").nullable()
    val vo2max = integer("vo2max").nullable()

    // sonno e recupero
    val rhr_a_riposo = integer("rhr_a_riposo").nullable()
    val sonno_totale_minuti = integer("sonno_totale_minuti").nullable()
    val sonno_profondo_minuti = integer("sonno_profondo_minuti").nullable()
    val sonno_leggero_minuti = integer("sonno_leggero_minuti").nullable()
    val sonno_rem_minuti = integer("sonno_rem_minuti").nullable()
    val sonno_sveglio_minuti = integer("sonno_sveglio_minuti").nullable()

    // sattivita diurna
    val attivita_sedentaria_minuti = integer("attivita_sedentaria_minuti").nullable()
    val attivita_leggera_minuti = integer("attivita_leggera_minuti").nullable()
    val attivita_moderata_minuti = integer("attivita_moderata_minuti").nullable()
    val attivita_intensa_minuti = integer("attivita_intensa_minuti").nullable()

    // stress diurno
    val stress_calmo_minuti = integer("stress_calmo_minuti").nullable()
    val stress_medio_minuti = integer("stress_medio_minuti").nullable()
    val stress_alto_minuti = integer("stress_alto_minuti").nullable()

    init {
        uniqueIndex(userId, data)
    }
}