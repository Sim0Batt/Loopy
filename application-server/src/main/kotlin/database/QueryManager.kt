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
                it[TabellaAccelerometroTable.movimento] = input.movement
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
                .map { AccelerometerData(
                    it[TabellaAccelerometroTable.movimento],
                    it[TabellaAccelerometroTable.timestamp]
                )}

            ReturnDataJson(
                heartRates = ppgDatas.map { it.heartRate }.reversed().joinToString(", "),
                oxygens = ppgDatas.map { it.oxygen }.reversed().joinToString(", "),
                timestampsPPG = ppgDatas.map { it.timestamp }.reversed().joinToString(", "),
                sweatings = electrodesDatas.map { it.sweating }.reversed().joinToString(", "),
                timestampsElectrodes = electrodesDatas.map { it.timestamp }.reversed().joinToString(", "),
                temperatures = termometerDatas.map { it.temperature }.reversed().joinToString(", "),
                timestampsTermometer = termometerDatas.map { it.timestamp }.reversed().joinToString(", "),
                movements = accelerometerData.map { it.movement }.reversed().joinToString(", "),
                timestampsAccelerometer = accelerometerData.map { it.timestamp }.reversed().joinToString(", ")
            )
        }
    }


    fun getDailySummary(connection: Database, id: Int): SummaryDataJson {

        val riepilogo = transaction(connection) {
            TabellaRiepilogoGiornalieroTable.selectAll()
                .where { TabellaRiepilogoGiornalieroTable.userId eq id }
                .orderBy(TabellaRiepilogoGiornalieroTable.data to SortOrder.DESC) // Prende il più recente
                .firstOrNull() // Prende solo l'ultima riga calcolata
        }

        if (riepilogo == null) {
            return SummaryDataJson(hrv = null, stress = null, passi = null, recupero = null, vo2max = null)
        }

        return SummaryDataJson(
            hrv = riepilogo[TabellaRiepilogoGiornalieroTable.hrv],
            stress = riepilogo[TabellaRiepilogoGiornalieroTable.stress],
            passi = riepilogo[TabellaRiepilogoGiornalieroTable.passi],
            recupero = riepilogo[TabellaRiepilogoGiornalieroTable.recupero],
            vo2max = riepilogo[TabellaRiepilogoGiornalieroTable.vo2max]
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