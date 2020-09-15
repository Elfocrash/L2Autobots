package dev.l2j.autobots.behaviors.preferences

import com.fasterxml.jackson.annotation.JsonProperty
import dev.l2j.autobots.utils.distance
import dev.l2j.autobots.utils.getCpPercentage
import dev.l2j.autobots.utils.getHealthPercentage
import dev.l2j.autobots.utils.getManaPercentage
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.Monster

internal enum class StatusCondition {
    Hp,
    Cp,
    Mp,
    Distance,
    Level
}

internal enum class TargetCondition {
    My,
    Target,
    MobTarget,
    PlayerTarget
}

internal enum class ComparisonCondition(val operator: String) {
    Equals("="),
    LessThan("<"),
    LessOrEqualThan("<="),
    MoreThan(">"),
    MoreOrEqualThan(">=")
}

internal enum class ConditionValueType {
    Percentage,
    MissingAmount,
    Amount
}

internal data class SkillUsageCondition(
        @JsonProperty("skillId") var skillId: Int,
        @JsonProperty("statusCondition")var statusCondition: StatusCondition,
        @JsonProperty("comparisonCondition")var comparisonCondition: ComparisonCondition,
        @JsonProperty("conditionValueType")var conditionValueType: ConditionValueType,
        @JsonProperty("targetCondition")var targetCondition: TargetCondition,
        @JsonProperty("value")var value: Int) {
    
    fun isValid(autobot: Player): Boolean{
        val skill = autobot.getSkill(skillId) ?: return false
        
        val target = when(targetCondition){
            TargetCondition.My -> autobot
            TargetCondition.Target -> autobot.target
            TargetCondition.PlayerTarget -> if(autobot.target is Player) autobot.target else null
            TargetCondition.MobTarget -> if(autobot.target is Monster) autobot.target else null
        } as Creature? ?: return false
        
        val valueToCompare = when(conditionValueType){
            ConditionValueType.Percentage -> {
                when(statusCondition) {
                    StatusCondition.Hp -> target.getHealthPercentage().toInt()
                    StatusCondition.Cp -> target.getCpPercentage().toInt()
                    StatusCondition.Mp -> target.getManaPercentage().toInt()
                    StatusCondition.Distance -> 0
                    StatusCondition.Level -> 0
                }
            }
            ConditionValueType.Amount -> {
                when(statusCondition) {
                    StatusCondition.Hp -> target.currentHp.toInt()
                    StatusCondition.Cp -> target.currentCp.toInt()
                    StatusCondition.Mp -> target.currentMp.toInt()
                    StatusCondition.Distance -> distance(autobot, autobot.target as Creature).toInt()
                    StatusCondition.Level -> target.level
                }
            }
            ConditionValueType.MissingAmount -> {
                when(statusCondition) {
                    StatusCondition.Hp -> target.maxHp - target.currentHp.toInt()
                    StatusCondition.Cp -> target.maxCp - target.currentCp.toInt()
                    StatusCondition.Mp -> target.maxMp - target.currentMp.toInt()
                    StatusCondition.Distance -> 0
                    StatusCondition.Level -> 0
                }
            }
        }
        
        return when(comparisonCondition){
            ComparisonCondition.Equals -> valueToCompare == value
            ComparisonCondition.MoreOrEqualThan -> valueToCompare >= value
            ComparisonCondition.MoreThan -> valueToCompare > value
            ComparisonCondition.LessOrEqualThan -> valueToCompare <= value
            ComparisonCondition.LessThan -> valueToCompare < value
        }
    }
    
    fun getConditionText(): String{
        val sb = StringBuilder()
        
        when(targetCondition){
            TargetCondition.My -> sb.append("My ")
            TargetCondition.Target -> sb.append("My target's ")
            TargetCondition.MobTarget -> sb.append("My monster target's ")
            TargetCondition.PlayerTarget -> sb.append("My player target's ")
        }
        
        when(statusCondition){
            StatusCondition.Hp -> sb.append("HP ")
            StatusCondition.Cp -> sb.append("CP ")
            StatusCondition.Mp -> sb.append("MP ")
            StatusCondition.Distance -> sb.append("distance ")
            StatusCondition.Level -> sb.append("level ")
        }
        
        when(comparisonCondition){
            ComparisonCondition.Equals -> sb.append("is equal to ")
            ComparisonCondition.MoreOrEqualThan -> sb.append("is more or equal than ")
            ComparisonCondition.MoreThan -> sb.append("is more than ")
            ComparisonCondition.LessOrEqualThan -> sb.append("is less or equal than ")
            ComparisonCondition.LessThan -> sb.append("is less than ")
        }
        
        when(conditionValueType){
            ConditionValueType.Percentage -> sb.append("$value%")
            ConditionValueType.Amount -> sb.append("$value")
            ConditionValueType.MissingAmount -> sb.append("$value of the missing max amount")
        }
        return sb.toString()
    }
}