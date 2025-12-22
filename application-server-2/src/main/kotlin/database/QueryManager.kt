package database

import com.example.loopy.data.models.input.ReturnSSAGDataJson
import database.tables.TabellaActivityTable
import database.tables.TabellaGlucosioTable
import database.tables.TabellaGlucosioTable.glicemia
import database.tables.TabellaSleepTable
import database.tables.TabellaStressTable
import org.example.database.DatabaseConfig
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import kotlin.compareTo

object QueryManager {
    fun getGlucosePredict(userId: Int): String {
        var predict: String = ""
        transaction(DatabaseConfig.getConfig()) {
            predict = TabellaGlucosioTable.selectAll().where {
                TabellaGlucosioTable.userId eq userId
            }.orderBy(TabellaGlucosioTable.id to SortOrder.DESC).firstOrNull()?.get(glicemia).toString()
        }
        return predict
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

    fun getSSGAData(userId: Int): ReturnSSAGDataJson{
        return transaction(DatabaseConfig.getConfig()){
            val activityData = TabellaActivityTable.selectAll().where{
                TabellaActivityTable.userId eq userId and (TabellaActivityTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaActivityTable.activityLevel] }.toList()

            val sleepData = TabellaSleepTable.selectAll().where{
                TabellaSleepTable.userId eq userId and (TabellaSleepTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaSleepTable.sleepLevel] }.toList()

            val stressData = TabellaStressTable.selectAll().where{
                TabellaStressTable.userId eq userId and (TabellaStressTable.timestamp like "${LocalDate.now()}%")
            }.map { it[TabellaStressTable.stressLevel] }.toList()

            val glucoseData = TabellaGlucosioTable.selectAll().where{
                TabellaGlucosioTable.userId eq userId and (TabellaGlucosioTable.timestamp like "${LocalDate.now()}%")
            }.map { it[glicemia] }.toList()

            ReturnSSAGDataJson(
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
}