package dev.l2j.autobots.behaviors.preferences

import com.fasterxml.jackson.annotation.JsonProperty
import dev.l2j.autobots.AutobotScheduler
import java.time.Clock
import java.time.LocalDateTime

internal data class ActivityPreferences(@JsonProperty("activityType")var activityType: ActivityType = ActivityType.None,
                                        @JsonProperty("uptimeMinutes")var uptimeMinutes: Int = 60,
                                        @JsonProperty("loginTime")var loginTime: String = "09:00",
                                        @JsonProperty("logoutTime")var logoutTime: String = "20:00"){
    fun logoutTimeIsInThePast() : Boolean{
        val dateTimeNow = LocalDateTime.now(Clock.systemUTC())
        val logoutTime = LocalDateTime.parse("${dateTimeNow.year}-${if (dateTimeNow.monthValue < 10) "0${dateTimeNow.monthValue}" else dateTimeNow.monthValue}-${if (dateTimeNow.dayOfMonth < 10) "0${dateTimeNow.dayOfMonth}" else dateTimeNow.dayOfMonth} $logoutTime", AutobotScheduler.formatter)
        return dateTimeNow > logoutTime
    }
}

enum class ActivityType {
    None,
    Uptime,
    Schedule
}