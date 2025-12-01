package database

import database.tables.TabellaAccelerometroTable
import database.tables.TabellaElettrodiTable
import database.tables.TabellaGlucosioTable
import database.tables.TabellaGlucosioTable.glicemia
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
import database.tables.TabellaRiepilogoGiornalieroTable // <-- Importa la nuova tabella
import models.AccelerometerData
import models.ElectrodeData
import models.PPGData
import models.TermometerData
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import server.jsonModels.inputJsons.SaveDataJson
import server.jsonModels.outputJsons.ReturnDataJson
import server.jsonModels.outputJsons.SummaryDataJson // <-- Importa il nuovo JSON

object QueryManager {


    fun saveDatas(connection: Database, input: SaveDataJson, userId: Int) {
        transaction(connection) {
            TabellaPpgTable.insert {
                it[TabellaPpgTable.battito] = input.heartRate
                it[TabellaPpgTable.ossigenazione] = "${input.oxygen}%"
                it[TabellaPpgTable.timestamp] = input.timestampPPG
                it[TabellaPpgTable.userId] = userId
            }
            TabellaElettrodiTable.insert {
                it[TabellaElettrodiTable.sudorazione] = input.sweating
                it[TabellaElettrodiTable.timestamp] = input.timestampElectrodes
                it[TabellaElettrodiTable.userId] = userId
            }
            TabellaAccelerometroTable.insert {
                it[TabellaAccelerometroTable.acc_x] = input.acc_x
                it[TabellaAccelerometroTable.acc_y] = input.acc_y
                it[TabellaAccelerometroTable.acc_z] = input.acc_z
                it[TabellaAccelerometroTable.timestamp] = input.timestampAccelerometer
                it[TabellaAccelerometroTable.userId] = userId
            }

            TabellaTermometroTable.insert {
                it[TabellaTermometroTable.temperatura] = input.temperature
                it[TabellaTermometroTable.timestamp] = input.timestampTermometer
                it[TabellaTermometroTable.userId] = userId
            }
        }
        println("All data saved for user $userId")
    }

    fun getDatas(connection: Database, id: Int): ReturnDataJson {
        return transaction(connection) {
            val dataLimit = 10 // Limite fisso per i dati "recenti"

            val ppgDatas = TabellaPpgTable.selectAll()
                .where { TabellaPpgTable.userId eq id }
                .orderBy(TabellaPpgTable.id to SortOrder.DESC)
                .limit(dataLimit)
                .map { PPGData(
                    it[TabellaPpgTable.battito],
                    it[TabellaPpgTable.ossigenazione].replace("%", "").toDouble(),
                    it[TabellaPpgTable.timestamp]
                )}

            val electrodesDatas = TabellaElettrodiTable.selectAll()
                .where { TabellaElettrodiTable.userId eq id }
                .orderBy(TabellaElettrodiTable.id to SortOrder.DESC)
                .limit(dataLimit)
                .map { ElectrodeData(
                    it[TabellaElettrodiTable.sudorazione],
                    it[TabellaElettrodiTable.timestamp]
                )}

            val termometerDatas = TabellaTermometroTable.selectAll()
                .where { TabellaTermometroTable.userId eq id }
                .orderBy(TabellaTermometroTable.id to SortOrder.DESC)
                .limit(dataLimit)
                .map { TermometerData(
                    it[TabellaTermometroTable.temperatura],
                    it[TabellaTermometroTable.timestamp]
                )}


            val accelerometerData = TabellaAccelerometroTable.selectAll()
                .where { TabellaAccelerometroTable.userId eq id }
                .orderBy(TabellaAccelerometroTable.id to SortOrder.DESC)
                .limit(dataLimit)
                .map {
                    AccelerometerData(
                        acc_x = it[TabellaAccelerometroTable.acc_x],
                        acc_y = it[TabellaAccelerometroTable.acc_y],
                        acc_z = it[TabellaAccelerometroTable.acc_z],
                        timestamp = it[TabellaAccelerometroTable.timestamp]
                    )
                }


            ReturnDataJson(
                heartRates = ppgDatas.map { it.heartRate }.reversed().joinToString(", "),
                oxygens = ppgDatas.map { it.oxygen }.reversed().joinToString(", "),
                timestampsPPG = ppgDatas.map { it.timestamp }.reversed().joinToString(", "),
                sweatings = electrodesDatas.map { it.sweating }.reversed().joinToString(", "),
                timestampsElectrodes = electrodesDatas.map { it.timestamp }.reversed().joinToString(", "),
                temperatures = termometerDatas.map { it.temperature }.reversed().joinToString(", "),
                timestampsTermometer = termometerDatas.map { it.timestamp }.reversed().joinToString(", "),
                movements = accelerometerData
                    .map { data ->
                        // modulo del vettore (x,y,z) come indicatore di intensità movimento
                        kotlin.math.sqrt(
                            data.acc_x * data.acc_x +
                                    data.acc_y * data.acc_y +
                                    data.acc_z * data.acc_z
                        )
                    }
                    .reversed()
                    .joinToString(", "),

                timestampsAccelerometer = accelerometerData.map { it.timestamp }.reversed().joinToString(", ")
            )
        }
    }


    fun getDailySummary(connection: Database, id: Int): SummaryDataJson {
        val riepilogo = transaction(connection) {
            TabellaRiepilogoGiornalieroTable.selectAll()
                .where { TabellaRiepilogoGiornalieroTable.userId eq id }
                .orderBy(TabellaRiepilogoGiornalieroTable.data to SortOrder.DESC)
                .firstOrNull()
        }

        if (riepilogo == null) {
            return SummaryDataJson()
        }

        return SummaryDataJson(
            // Base
            hrv = riepilogo[TabellaRiepilogoGiornalieroTable.hrv],
            rhr_a_riposo = riepilogo[TabellaRiepilogoGiornalieroTable.rhr_a_riposo],
            recupero = riepilogo[TabellaRiepilogoGiornalieroTable.recupero],
            vo2max = riepilogo[TabellaRiepilogoGiornalieroTable.vo2max],

            // Sonno
            sonno_totale_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.sonno_totale_minuti],
            sonno_profondo_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.sonno_profondo_minuti],
            sonno_leggero_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.sonno_leggero_minuti],
            sonno_rem_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.sonno_rem_minuti],
            sonno_sveglio_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.sonno_sveglio_minuti],
            sonno_grafico_json = riepilogo[TabellaRiepilogoGiornalieroTable.sonno_grafico_json],

            // Attività
            attivita_sedentaria_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.attivita_sedentaria_minuti],
            attivita_leggera_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.attivita_leggera_minuti],
            attivita_moderata_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.attivita_moderata_minuti],
            attivita_intensa_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.attivita_intensa_minuti],
            attivita_grafico_json = riepilogo[TabellaRiepilogoGiornalieroTable.attivita_grafico_json],

            // Stress
            stress_calmo_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.stress_calmo_minuti],
            stress_medio_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.stress_medio_minuti],
            stress_alto_minuti = riepilogo[TabellaRiepilogoGiornalieroTable.stress_alto_minuti],
            stress_grafico_json = riepilogo[TabellaRiepilogoGiornalieroTable.stress_grafico_json]
        )
    }

    fun getGlucosePredict(userId: Int): String {
        var predict: String = ""
        transaction(DatabaseConfig.getConfig()) {
            predict = TabellaGlucosioTable.selectAll().where {
                TabellaGlucosioTable.userId eq userId
            }.orderBy(TabellaGlucosioTable.id to SortOrder.DESC).firstOrNull()?.get(glicemia).toString()
        }
        return predict
    }
}