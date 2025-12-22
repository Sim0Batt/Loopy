package database

import database.tables.TabellaAccelerometroTable
import database.tables.TabellaActivityTable
import database.tables.TabellaStressTable
import database.tables.TabellaElettrodiTable
import database.tables.TabellaGlucosioTable
import database.tables.TabellaGlucosioTable.glicemia
import database.tables.TabellaPpgTable
import database.tables.TabellaTermometroTable
import database.tables.TabellaSleepTable
import database.tables.TabellaUserTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import server.jsonModels.inputJsons.RegisterJson
import server.jsonModels.inputJsons.SaveDataJson
import server.jsonModels.outputJsons.CsvDataJson
import server.jsonModels.outputJsons.ReturnDataJson
import server.jsonModels.outputJsons.UserDataJson
import java.time.LocalDate

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


    fun getCsvData(userId: Int): CsvDataJson {
        var returnJson = CsvDataJson("", "", "")
        transaction(DatabaseConfig.getConfig()) {
            returnJson =  CsvDataJson(
                hrs = TabellaPpgTable.selectAll().where{ TabellaPpgTable.userId eq userId}.joinToString(separator = ",") { it[TabellaPpgTable.battito].toString() },
                sweatings = TabellaElettrodiTable.selectAll().where{ TabellaElettrodiTable.userId eq userId}.joinToString(separator = ",") { it[TabellaElettrodiTable.sudorazione].toString() },
                temperatures = TabellaTermometroTable.selectAll().where{ TabellaTermometroTable.userId eq userId}.joinToString(separator = ",") { it[TabellaTermometroTable.temperatura].toString() }
            )
        }
        return returnJson
    }


    fun getDatas(connection: Database, id: Int): ReturnDataJson {
        return transaction(connection) {

            val ppgData = TabellaPpgTable.selectAll()
                .where { TabellaPpgTable.userId eq id and (TabellaPpgTable.timestamp like "${LocalDate.now()}%") }
                .map {
                    it[TabellaPpgTable.battito]
                }

            val oxygenData = TabellaPpgTable.selectAll()
            .where { TabellaPpgTable.userId eq id and (TabellaPpgTable.timestamp like "${LocalDate.now()}%") }
            .map {
                it[TabellaPpgTable.ossigenazione].toDouble()
            }

            val electrodesData = TabellaElettrodiTable.selectAll()
                .where { TabellaElettrodiTable.userId eq id and (TabellaElettrodiTable.timestamp like "${LocalDate.now()}%") }
                .map {
                    it[TabellaElettrodiTable.sudorazione]
                }

            val thermometerData = TabellaTermometroTable.selectAll()
                .where { TabellaTermometroTable.userId eq id and (TabellaTermometroTable.timestamp like "${LocalDate.now()}%") }
                .map { it[TabellaTermometroTable.temperatura]}


            val activityData = TabellaActivityTable.selectAll().where{
                TabellaActivityTable.userId eq id and (TabellaActivityTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaActivityTable.activityLevel] }.toList()

            val sleepData = TabellaSleepTable.selectAll().where{
                TabellaSleepTable.userId eq id and (TabellaSleepTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaSleepTable.sleepLevel] }.toList()

            val stressData = TabellaStressTable.selectAll().where{
                TabellaStressTable.id eq id and (TabellaStressTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaStressTable.stressLevel] }.toList()

            val glucoseData = TabellaGlucosioTable.selectAll().where{
                TabellaGlucosioTable.userId eq id and (TabellaGlucosioTable.timestamp like "${LocalDate.now()}%")
            }.map { it[glicemia] }.toList()



            ReturnDataJson(
                heartRate = ppgData.average(),
                oxygen = oxygenData.average(),
                sweating = electrodesData.average(),
                temperature = thermometerData.average(),
                glucose = glucoseData.average(),
                activity = when{
                    activityData.average() < 10 -> "Sedentary"
                    activityData.average() < 20 -> "Light"
                    activityData.average() < 50 -> "Moderate"
                    else -> "Intense"
                },
                sleep = when{
                    sleepData.average() < 20 -> "Woke Up"
                    sleepData.average() < 50 -> "Light"
                    sleepData.average() < 80 -> "REM"
                    else -> "Deep"
                },
                stress = when{
                    stressData.count() == 0 -> "No Stress"
                    stressData.average() < 33.3 -> "Calm"
                    stressData.average() < 66.6 -> "Medium"
                    else -> "High"
                }
            )
        }
    }

    fun getStressData(userId: Int): Map<String, Int>{
        var timestampList = listOf<String>()
        var stressLevelList = listOf<Int>()
        transaction(DatabaseConfig.getConfig()) {
            timestampList = TabellaStressTable.selectAll().where{
                TabellaStressTable.userId eq userId and (TabellaStressTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaStressTable.timestamp] }.toList()

            stressLevelList = TabellaStressTable.selectAll().where{
                TabellaStressTable.userId eq userId and (TabellaStressTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaStressTable.stressLevel] }.toList()
        }
        return timestampList.zip(stressLevelList).toMap()
    }

    fun getActivityData(userId: Int): Map<String, Int>{
        var timestampList = listOf<String>()
        var activityLevelsList = listOf<Int>()
        transaction(DatabaseConfig.getConfig()) {
            timestampList = TabellaActivityTable.selectAll().where{
                TabellaActivityTable.userId eq userId and (TabellaActivityTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaActivityTable.timestamp] }.toList()

            activityLevelsList = TabellaActivityTable.selectAll().where{
                TabellaActivityTable.userId eq userId and (TabellaActivityTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaActivityTable.activityLevel] }.toList()
        }
        return timestampList.zip(activityLevelsList).toMap()
    }

    fun getTodaySleepData(userId: Int): Map<String, Int>{
        var timestampList = listOf<String>()
        var sleepLevelsList = listOf<Int>()
        transaction(DatabaseConfig.getConfig()) {
            timestampList = TabellaSleepTable.selectAll().where{
                TabellaSleepTable.userId eq userId and (TabellaSleepTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaSleepTable.timestamp] }.toList()

            sleepLevelsList = TabellaSleepTable.selectAll().where{
                TabellaSleepTable.userId eq userId and (TabellaSleepTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaSleepTable.sleepLevel] }.toList()
        }
        return timestampList.zip(sleepLevelsList).toMap()
    }

    fun getYesterdaySleepData(userId: Int): Map<String, Int>{
        var timestampList = listOf<String>()
        var sleepLevelsList = listOf<Int>()
        transaction(DatabaseConfig.getConfig()) {
            timestampList = TabellaSleepTable.selectAll().where{
                TabellaSleepTable.userId eq userId and (TabellaSleepTable.timestamp like "${LocalDate.now().minusDays(1)}%")
            }.map { it[TabellaSleepTable.timestamp] }.toList()

            sleepLevelsList = TabellaSleepTable.selectAll().where{
                TabellaSleepTable.userId eq userId and (TabellaSleepTable.timestamp like "${LocalDate.now().minusDays(1)}%")
            }.map { it[TabellaSleepTable.sleepLevel] }.toList()
        }
        return timestampList.zip(sleepLevelsList).toMap()
    }


    fun getUserInformation(userId: Int): UserDataJson {
        var userDatajson = UserDataJson("", "", "", "", "", "")
        transaction(DatabaseConfig.getConfig()) {
            val username = TabellaUserTable.selectAll().where{
                TabellaUserTable.id eq userId
            }.first()[TabellaUserTable.username]

            val email = TabellaUserTable.selectAll().where{
                TabellaUserTable.id eq userId
            }.first()[TabellaUserTable.email]

            val age = TabellaUserTable.selectAll().where{
                TabellaUserTable.id eq userId
            }.first()[TabellaUserTable.age].toString()

            val gender = TabellaUserTable.selectAll().where{
                TabellaUserTable.id eq userId
            }.first()[TabellaUserTable.sex]

            val weight = TabellaUserTable.selectAll().where{
                TabellaUserTable.id eq userId
            }.first()[TabellaUserTable.weight].toString()

            val height = TabellaUserTable.selectAll().where{
                TabellaUserTable.id eq userId
            }.first()[TabellaUserTable.height].toString()

            userDatajson = UserDataJson(
                username,
                email,
                age,
                gender,
                weight,
                height
            )
        }

        return userDatajson
    }

    fun updateUserInformation(userId: Int, registerJson: RegisterJson): Boolean{
        var isLoaded = false
        transaction(DatabaseConfig.getConfig()) {
            try{
                if(registerJson.password != ""){
                    TabellaUserTable.update({ TabellaUserTable.id eq userId }) {
                        it[username] = registerJson.username
                        it[email] = registerJson.email
                        it[password] = registerJson.password
                        it[age] = registerJson.age
                        it[weight] = registerJson.weight
                        it[height] = registerJson.height
                        it[sex] = registerJson.sex
                    }
                }else{
                    TabellaUserTable.update({ TabellaUserTable.id eq userId }) {
                        it[username] = registerJson.username
                        it[email] = registerJson.email
                        it[age] = registerJson.age
                        it[weight] = registerJson.weight
                        it[height] = registerJson.height
                        it[sex] = registerJson.sex
                    }
                }
                isLoaded = true
            }catch (e: Exception){
                isLoaded = false
            }
        }
        return isLoaded
    }


}