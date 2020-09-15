package dev.l2j.autobots.behaviors.preferences

import com.fasterxml.jackson.annotation.JsonProperty
import dev.l2j.autobots.AutobotData

internal open class DefaultCombatPreferences(
        @JsonProperty("targetingRadius")override var targetingRadius: Int = AutobotData.settings.targetingRange,
        @JsonProperty("attackPlayerType")override var attackPlayerType: AttackPlayerType = AutobotData.settings.attackPlayerType,
        @JsonProperty("buffs") override var buffs: Array<IntArray> = emptyArray(),
        @JsonProperty("targetingPreference")override var targetingPreference: TargetingPreference = AutobotData.settings.targetingPreference,
        @JsonProperty("useManaPots")override var useManaPots: Boolean = AutobotData.settings.useManaPots,
        @JsonProperty("useQuickHealingPots")override var useQuickHealingPots: Boolean = AutobotData.settings.useQuickHealingPots,
        @JsonProperty("useGreaterHealingPots")override var useGreaterHealingPots: Boolean = AutobotData.settings.useGreaterHealingPots,
        @JsonProperty("useGreaterCpPots")override var useGreaterCpPots: Boolean = AutobotData.settings.useGreaterCpPots
) : CombatPreferences

internal open class ArcherCombatPreferences(
        @JsonProperty("kiteRadius")var kiteRadius: Int = 500,
        @JsonProperty("isKiting")var isKiting: Boolean = true,
        @JsonProperty("kitingDelay")var kitingDelay: Long = 700,
        @JsonProperty("targetingRadius")override var targetingRadius: Int = AutobotData.settings.targetingRange,
        @JsonProperty("attackPlayerType")override var attackPlayerType: AttackPlayerType = AutobotData.settings.attackPlayerType,
        @JsonProperty("buffs")override var buffs: Array<IntArray> = emptyArray(),
        @JsonProperty("targetingPreference")override var targetingPreference: TargetingPreference = AutobotData.settings.targetingPreference,
        @JsonProperty("useManaPots")override var useManaPots: Boolean = AutobotData.settings.useManaPots,
        @JsonProperty("useQuickHealingPots")override var useQuickHealingPots: Boolean = AutobotData.settings.useQuickHealingPots,
        @JsonProperty("useGreaterHealingPots")override var useGreaterHealingPots: Boolean = AutobotData.settings.useGreaterHealingPots,
        @JsonProperty("useGreaterCpPots")override var useGreaterCpPots: Boolean = AutobotData.settings.useGreaterCpPots) : CombatPreferences

internal open class PetOwnerCombatPreferences(
        @JsonProperty("petAssists")override var petAssists: Boolean = true,
        @JsonProperty("summonPet")override var summonPet: Boolean = true,
        @JsonProperty("petUsesShots")override var petUsesShots: Boolean = false,
        @JsonProperty("petShotId")override var petShotId: Int,
        @JsonProperty("petHasBuffs")override var petHasBuffs: Boolean = true,
        @JsonProperty("targetingRadius")override var targetingRadius: Int = AutobotData.settings.targetingRange,
        @JsonProperty("attackPlayerType")override var attackPlayerType: AttackPlayerType = AutobotData.settings.attackPlayerType,
        @JsonProperty("buffs")override var buffs: Array<IntArray> = emptyArray(),
        @JsonProperty("targetingPreference")override var targetingPreference: TargetingPreference = AutobotData.settings.targetingPreference,
        @JsonProperty("useManaPots")override var useManaPots: Boolean = AutobotData.settings.useManaPots,
        @JsonProperty("useQuickHealingPots")override var useQuickHealingPots: Boolean = AutobotData.settings.useQuickHealingPots,
        @JsonProperty("useGreaterHealingPots")override var useGreaterHealingPots: Boolean = AutobotData.settings.useGreaterHealingPots,
        @JsonProperty("useGreaterCpPots")override var useGreaterCpPots: Boolean = AutobotData.settings.useGreaterCpPots) : CombatPreferences, PetOwnerPreferences

internal open class DuelistCombatPreferences(
        @JsonProperty("targetingRadius")override var targetingRadius: Int = AutobotData.settings.targetingRange,
        @JsonProperty("attackPlayerType")override var attackPlayerType: AttackPlayerType = AutobotData.settings.attackPlayerType,
        @JsonProperty("buffs")override var buffs: Array<IntArray> = emptyArray(),
        @JsonProperty("targetingPreference")override var targetingPreference: TargetingPreference = AutobotData.settings.targetingPreference,
        @JsonProperty("useManaPots")override var useManaPots: Boolean = AutobotData.settings.useManaPots,
        @JsonProperty("useQuickHealingPots")override var useQuickHealingPots: Boolean = AutobotData.settings.useQuickHealingPots,
        @JsonProperty("useGreaterHealingPots")override var useGreaterHealingPots: Boolean = AutobotData.settings.useGreaterHealingPots,
        @JsonProperty("useGreaterCpPots")override var useGreaterCpPots: Boolean = AutobotData.settings.useGreaterCpPots) : CombatPreferences

internal interface CombatPreferences{
    var targetingRadius: Int
    var attackPlayerType: AttackPlayerType
    var buffs: Array<IntArray>
    var targetingPreference: TargetingPreference
    var useManaPots: Boolean
    var useQuickHealingPots: Boolean
    var useGreaterHealingPots: Boolean
    var useGreaterCpPots: Boolean
}

internal interface PetOwnerPreferences {
    var petAssists: Boolean
    var summonPet: Boolean
    var petUsesShots: Boolean
    var petShotId: Int
    var petHasBuffs: Boolean 
}