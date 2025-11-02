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
    fun saveDatas(connection: Database, input: SaveDataJson) {
        transaction(connection) {
            TabellaPpgTable.insert {
                TabellaPpgTable.battito to input.heartRate
                TabellaPpgTable.ossigenazione to input.oxygen
                TabellaPpgTable.timestamp to input.timestampPPG
            }
        }
        println("Saved Heart Data:\n $input")

        transaction(connection) {
            TabellaElettrodiTable.insert {
                TabellaElettrodiTable.sudorazione to input.sweating
                TabellaElettrodiTable.timestamp to input.timestampElectrodes
            }
        }
        println("Saved Heart Data:\n $input")

        transaction(connection) {
            TabellaAccelerometroTable.insert {
                TabellaAccelerometroTable.movimento to input.movement
                TabellaAccelerometroTable.timestamp to input.timestampAccelerometer
            }
        }
        println("Saved Heart Data:\n $input")

        transaction(connection) {
            TabellaTermometroTable.insert {
                TabellaTermometroTable.temperatura to input.temperature
                TabellaTermometroTable.timestamp to input.timestampTermometer
            }
        }
        println("Saved Heart Data:\n $input")
    }

    fun getDatas(connection: Database, id:Int): ReturnDataJson {
        val ppgDatas = mutableListOf<PPGData>()
        val electrodesDatas = mutableListOf<ElectrodeData>()
        val termometerDatas = mutableListOf<TermometerData>()
        val accelerometerData = mutableListOf<AccelerometerData>()

        transaction(connection) {
            //PPG
            val hrTmp = TabellaPpgTable.selectAll().where {
                TabellaPpgTable.userId eq id.toString().toInt()
            }.orderBy(TabellaPpgTable.id to SortOrder.DESC).map{
                it[TabellaPpgTable.battito]
            }.take(10)
            val oxTmp = TabellaPpgTable.selectAll().where {
                TabellaPpgTable.userId eq id.toString().toInt()
            }.orderBy(TabellaPpgTable.id to SortOrder.DESC).map{
                it[TabellaPpgTable.ossigenazione].replace("%", "").toDouble()
            }.take(10)
            var tsTmp = TabellaPpgTable.selectAll().where {
                TabellaPpgTable.userId eq id.toString().toInt()
            }.orderBy(TabellaPpgTable.id to SortOrder.DESC).map{
                it[TabellaPpgTable.timestamp]
            }.take(10)

            for(i in 0..9){
                ppgDatas.add(PPGData(hrTmp[i], oxTmp[i], tsTmp[i]))
            }

            //Electrodes
            val swTmp = TabellaElettrodiTable.selectAll().where {
                TabellaElettrodiTable.userId eq id.toString().toInt()
            }.orderBy(TabellaElettrodiTable.id to SortOrder.DESC).map{
                it[TabellaElettrodiTable.sudorazione]
            }.take(10)

            tsTmp = TabellaElettrodiTable.selectAll().where {
                TabellaElettrodiTable.userId eq id.toString().toInt()
            }.orderBy(TabellaElettrodiTable.id to SortOrder.DESC).map{
                it[TabellaElettrodiTable.timestamp]
            }.take(10)

            for(i in 0..9){
                electrodesDatas.add(ElectrodeData(swTmp[i],tsTmp[i]))
            }

            //Termometer
            val trTmp = TabellaTermometroTable.selectAll().where {
                TabellaTermometroTable.userId eq id.toString().toInt()
            }.orderBy(TabellaTermometroTable.id to SortOrder.DESC).map{
                it[TabellaTermometroTable.temperatura]
            }.take(10)

            tsTmp = TabellaTermometroTable.selectAll().where {
                TabellaTermometroTable.userId eq id.toString().toInt()
            }.orderBy(TabellaTermometroTable.id to SortOrder.DESC).map{
                it[TabellaTermometroTable.timestamp]
            }.take(10)
            for(i in 0..9){
                termometerDatas.add(TermometerData(trTmp[i],tsTmp[i]))
            }

            //Accelerometer
            val mvTmp = TabellaAccelerometroTable.selectAll().where {
                TabellaAccelerometroTable.userId eq id.toString().toInt()
            }.orderBy(TabellaAccelerometroTable.id to SortOrder.DESC).map{
                it[TabellaAccelerometroTable.movimento]
            }.take(10)
            tsTmp = TabellaAccelerometroTable.selectAll().where {
                TabellaAccelerometroTable.userId eq id.toString().toInt()
            }.orderBy(TabellaAccelerometroTable.id to SortOrder.DESC).map{
                it[TabellaAccelerometroTable.timestamp]
            }
            for(i in 0..9){
                accelerometerData.add(AccelerometerData(mvTmp[i],tsTmp[i]))
            }
        }
        return ReturnDataJson(
            heartRates = ppgDatas.joinToString{"${it.heartRate}, "},
            oxygens = ppgDatas.joinToString{"${it.oxygen}, "},
            timestampsPPG = ppgDatas.joinToString{"${it.timestamp}, "},
            sweatings = electrodesDatas.joinToString { "${it.sweating}, " },
            timestampsElectrodes = electrodesDatas.joinToString { "${it.timestamp}, " },
            temperatures = termometerDatas.joinToString { "${it.temperature}, " },
            timestampsTermometer = termometerDatas.joinToString { "${it.timestamp}, " },
            movements = accelerometerData.joinToString { "${it.movement}, " },
            timestampsAccelerometer = accelerometerData.joinToString { "${it.timestamp}, " }
        )
    }


    fun getGlucosePredict(userId: Int): String{
        var predict: String = ""
        transaction (DatabaseConfig.getConfig()) {
            predict = TabellaGlucosioTable.selectAll().where{
                TabellaGlucosioTable.userId eq userId
            }.orderBy(TabellaGlucosioTable.id to SortOrder.DESC).firstOrNull()?.get(glicemia).toString()
        }
        return predict
    }
}