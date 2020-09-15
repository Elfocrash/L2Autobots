package dev.l2j.autobots.behaviors.preferences

import com.fasterxml.jackson.annotation.JsonProperty

internal open class SkillPreferences(
        @JsonProperty("isSkillsOnly")var isSkillsOnly: Boolean,
        @JsonProperty("skillUsageConditions")open val skillUsageConditions : MutableList<SkillUsageCondition> = mutableListOf(),
        @JsonProperty("togglableSkills")open val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf()
)