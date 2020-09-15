package dev.l2j.autobots

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.l2j.autobots.config.*
import net.sf.l2j.gameserver.enums.actors.ClassId
import java.io.File

internal object AutobotData {
    private val mapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)}).registerKotlinModule().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    
    var equipment = listOf<AutobotEquipment>()
    var buffs = mapOf<ClassId, Array<IntArray>>()
    var symbols = listOf<AutobotSymbol>()
    var teleportLocations = listOf<AutobotLocation>()
    var settings: AutobotSettings

    init {
        settings = mapper.readValue(File("./data/autobots/config.xml"))
        
        equipment = mapper.readValue(File("./data/autobots/equipment.xml"))
        buffs = mapper.readValue<List<AutobotBuffs>>(File("./data/autobots/buffs.xml")).map { it.classId to it.buffsContent}.toMap()
        teleportLocations = mapper.readValue(File("./data/autobots/teleports.xml"))
        symbols = mapper.readValue(File("./data/autobots/symbols.xml"))
    }
}