package dev.l2j.autobots.utils

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


internal object IconsTable {
    private val mapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)}).registerKotlinModule().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    var icons = mapOf<Int, String>()

    init {
        icons = mapper.readValue<List<L2Icon>>(IconsTable.javaClass.classLoader.getResource("xml/icons.xml")!!).map { it.id to it.value }.toMap()
    }
    
    private data class L2Icon(@JsonProperty("Id")val id: Int, @JsonProperty("value")val value: String)
    
    fun getSkillIcon(skillId: Int) : String {
        val skillIdAsText = when(skillId.toString().length){
            1 -> "000$skillId"
            2 -> "00$skillId"
            3 -> "0$skillId"
            else -> skillId.toString()
        }
        
        return "Icon.skill${skillIdAsText}"
    }
    
    fun getItemIcon(itemId: Int) : String {
        return icons.getOrDefault(itemId, "")
    }
}