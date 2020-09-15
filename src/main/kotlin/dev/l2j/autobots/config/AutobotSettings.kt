package dev.l2j.autobots.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import dev.l2j.autobots.behaviors.preferences.AttackPlayerType
import dev.l2j.autobots.behaviors.preferences.TargetingPreference
import java.io.File
import java.nio.charset.Charset

@JacksonXmlRootElement(namespace = "", localName = "settings")
internal data class AutobotSettings(
        @JsonProperty("thinkIteration") var iterationDelay: Long,
        @JsonProperty("defaultTitle") var defaultTitle: String = "",
        @JsonProperty("defaultTargetingRange") var targetingRange: Int,
        @JsonProperty("defaultAttackPlayerType") var attackPlayerType: AttackPlayerType,
        @JsonProperty("defaultTargetingPreference") var targetingPreference: TargetingPreference,
        @JsonProperty("useManaPots") var useManaPots: Boolean = true,
        @JsonProperty("useQuickHealingPots") var useQuickHealingPots: Boolean = false,
        @JsonProperty("useGreaterHealingPots") var useGreaterHealingPots: Boolean = true,
        @JsonProperty("useGreaterCpPots") var useGreaterCpPots: Boolean = true
){
    @JsonIgnore private val mapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)}).registerKotlinModule().configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).also { it.enable(SerializationFeature.INDENT_OUTPUT) }
    
    internal fun save(){
        val content = mapper.writeValueAsString(this)
        val header = "<?xml version='1.0' encoding='utf-8'?>\r\n"
        File("./data/autobots/config.xml").writeText(header + content, Charset.forName("UTF-8"))
    }
}