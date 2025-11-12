package database

import database.tables.TabellaAccelerometroTable
import database.tables.TabellaElettrodiTable
import database.tables.TabellaGlucosioTable
import database.tables.TabellaGlucosioTable.glicemia
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
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

object QueryManager {

    // Dentro QueryManager.kt

    fun saveDatas(connection: Database, input: SaveDataJson, userId: Int) {
        transaction(connection) {

            // PPG
            TabellaPpgTable.insert {
                it[TabellaPpgTable.battito] = input.heartRate
                it[TabellaPpgTable.ossigenazione] = "${input.oxygen}%"
                it[TabellaPpgTable.timestamp] = input.timestampPPG
                it[TabellaPpgTable.userId] = userId
            }

            // Electrodes
            TabellaElettrodiTable.insert {
                it[TabellaElettrodiTable.sudorazione] = input.sweating
                it[TabellaElettrodiTable.timestamp] = input.timestampElectrodes
                it[TabellaElettrodiTable.userId] = userId
            }

            // Accelerometer
            TabellaAccelerometroTable.insert {
                it[TabellaAccelerometroTable.movimento] = input.movement
                it[TabellaAccelerometroTable.timestamp] = input.timestampAccelerometer
                it[TabellaAccelerometroTable.userId] = userId
            }

            // Termometer
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
            val dataLimit = 10 // limite per pigliamento dati recenti

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

    fun getHistoricalDatas(connection: Database, id: Int, dataLimit: Int): ReturnDataJson {

        return transaction(connection) {

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