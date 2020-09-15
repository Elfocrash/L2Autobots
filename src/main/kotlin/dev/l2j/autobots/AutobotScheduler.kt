package dev.l2j.autobots

import dev.l2j.autobots.behaviors.preferences.ActivityType
import dev.l2j.autobots.dao.AutobotsDao
import dev.l2j.autobots.models.ScheduledSpawnInfo
import dev.l2j.autobots.utils.Klock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ConcurrentSkipListMap

internal object AutobotScheduler {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    
    private val scheduledBots = AutobotsDao.loadScheduledSpawns().map { it.botName to it }.toMap(ConcurrentSkipListMap<String, ScheduledSpawnInfo>(String.CASE_INSENSITIVE_ORDER))
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
    
    init {
        Klock.start("scheduledBots", 1000, 30000, CoScopes.generalScope, action = {
            val dateTimeNow = LocalDateTime.now(Clock.systemUTC())
            scheduledBots.values.forEach {
                handleScheduledBotSpawn(dateTimeNow, it)
            }
        })
    }

    private fun handleScheduledBotSpawn(dateTimeNow: LocalDateTime, it: ScheduledSpawnInfo) {
        val loginDateTime = LocalDateTime.parse("${dateTimeNow.year}-${if (dateTimeNow.monthValue < 10) "0${dateTimeNow.monthValue}" else dateTimeNow.monthValue}-${if (dateTimeNow.dayOfMonth < 10) "0${dateTimeNow.dayOfMonth}" else dateTimeNow.dayOfMonth} ${it.loginTime}", formatter)
        val logoutDateTime = LocalDateTime.parse("${dateTimeNow.year}-${if (dateTimeNow.monthValue < 10) "0${dateTimeNow.monthValue}" else dateTimeNow.monthValue}-${if (dateTimeNow.dayOfMonth < 10) "0${dateTimeNow.dayOfMonth}" else dateTimeNow.dayOfMonth} ${it.logoutTime}", formatter)

        if (dateTimeNow >= loginDateTime && dateTimeNow < logoutDateTime && !AutobotsManager.activeBots.containsKey(it.botName)) {
            scope.launch {
                val autobot = AutobotsDao.loadByName(it.botName) ?: return@launch
                AutobotsManager.spawnAutobot(autobot)
            }
        }

        if (dateTimeNow >= logoutDateTime && AutobotsManager.activeBots.containsKey(it.botName)) {
            scope.launch { AutobotsManager.activeBots[it.botName]!!.despawn() }
        }
    }

    internal fun addBot(autobot: Autobot){
        if(autobot.combatBehavior.activityPreferences.activityType == ActivityType.Schedule) {
            val spawnInfo = ScheduledSpawnInfo(autobot.name, autobot.combatBehavior.activityPreferences.loginTime, autobot.combatBehavior.activityPreferences.logoutTime)
            scheduledBots[autobot.name] = ScheduledSpawnInfo(autobot.name, autobot.combatBehavior.activityPreferences.loginTime, autobot.combatBehavior.activityPreferences.logoutTime)
            val dateTimeNow = LocalDateTime.now(Clock.systemUTC())
            handleScheduledBotSpawn(dateTimeNow, spawnInfo)
        }        
    }

    internal fun removeBot(autobot: Autobot){
        scheduledBots.remove(autobot.name)
    }
}