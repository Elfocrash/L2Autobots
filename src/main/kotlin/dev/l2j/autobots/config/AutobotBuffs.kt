package dev.l2j.autobots.config

import com.fasterxml.jackson.annotation.JsonProperty
import net.sf.l2j.gameserver.enums.actors.ClassId

data class AutobotBuffs(@JsonProperty("classid") val classId: ClassId, @JsonProperty("buffs")val buffsAsString: String){
    val buffsContent : Array<IntArray> = buffsAsString.split(";").map { intArrayOf(it.split(",")[0].toInt(), it.split(",")[1].toInt()) }.toTypedArray()
}