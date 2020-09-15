package dev.l2j.autobots.behaviors.preferences.skills

import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.utils.*

internal data class DreadnoughtSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(braveheart, StatusCondition.Cp, ComparisonCondition.MoreOrEqualThan, ConditionValueType.MissingAmount, TargetCondition.My, 1000),
                SkillUsageCondition(revival, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 5),
                SkillUsageCondition(battleroar, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 80)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(fellSwoop, false), Pair(viciousStance, true), Pair(warFrenzy, false), Pair(thrillFight, false)
        )) : SkillPreferences(false)

internal data class DuelistSkillPreferences(
        var useSkillsOnMobs: Boolean = false,
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(braveheart, StatusCondition.Cp, ComparisonCondition.MoreOrEqualThan, ConditionValueType.MissingAmount, TargetCondition.My, 1000),
                SkillUsageCondition(battleroar, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 80)
        )) : SkillPreferences(false)

internal data class PhoenixKnightSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(shackle, StatusCondition.Distance, ComparisonCondition.MoreOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 300),
                SkillUsageCondition(ultimateDefense, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 50),
                SkillUsageCondition(touchOfLife, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 40),
                SkillUsageCondition(angelicIcon, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 30)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(deflectArrow, false), Pair(aegisStance, false), Pair(shieldFortress, false), Pair(fortitude, false), Pair(holyBlade, false), Pair(holyArmor, false)
        )) : SkillPreferences(false)

internal data class HellKnightSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(shackle, StatusCondition.Distance, ComparisonCondition.MoreOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 300),
                SkillUsageCondition(ultimateDefense, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 40),
                SkillUsageCondition(touchOfDeath, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.Target, 75),
                SkillUsageCondition(shieldOfRevenge, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 50)                
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(deflectArrow, false), Pair(shieldFortress, false), Pair(fortitude, false)
        )) : SkillPreferences(false)

internal data class AdventurerSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(dash, StatusCondition.Distance, ComparisonCondition.MoreOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 300),
                SkillUsageCondition(ultimateEvasion, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 60),
                SkillUsageCondition(mirage, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 40)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(focusSkillMastery, true), Pair(focusPower, true), Pair(trick, true), Pair(switch, true), Pair(viciousStance, true)
        )) : SkillPreferences(false)

internal data class SagittariusSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(dash, StatusCondition.Distance, ComparisonCondition.MoreOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 1000),
                SkillUsageCondition(ultimateEvasion, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 60)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(hawkEye, false), Pair(viciousStance, true), Pair(accuracy, true)
        )) : SkillPreferences(false)

internal data class ArchmageSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(surrenderToFire, true), Pair(cancellation, false), Pair(arcanePower, true)
        )) : SkillPreferences(true)

internal data class SoultakerSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(vampiricClaw, StatusCondition.Hp, ComparisonCondition.LessThan, ConditionValueType.Percentage, TargetCondition.My, 100)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(curseGloom, true), Pair(anchor, true), Pair(curseOfAbyss, true), Pair(curseOfDoom, true), Pair(silence, true), Pair(anchor, true), Pair(transferPain, true), Pair(arcanePower, true)
        )
) : SkillPreferences(true)

internal data class EvasTemplarSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(arrest, StatusCondition.Distance, ComparisonCondition.MoreOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 300),
                SkillUsageCondition(ultimateDefense, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 50),
                SkillUsageCondition(touchOfLife, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 30),
                SkillUsageCondition(magicalMirror, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 40)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(summonStormCubic, true), Pair(summonAttractiveCubic, true), Pair(deflectArrow, false), Pair(guardStance, true), Pair(holyArmor, false), Pair(shieldFortress, false), Pair(fortitude, false)
        )) : SkillPreferences(false)

internal data class SwordMuseSkillPreferences(
        override val skillUsageConditions: MutableList<SkillUsageCondition> = mutableListOf(
                SkillUsageCondition(arrest, StatusCondition.Distance, ComparisonCondition.MoreOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 300),
                SkillUsageCondition(ultimateDefense, StatusCondition.Hp, ComparisonCondition.LessOrEqualThan, ConditionValueType.Percentage, TargetCondition.My, 50),
                SkillUsageCondition(songOfSilence, StatusCondition.Distance, ComparisonCondition.LessOrEqualThan, ConditionValueType.Amount, TargetCondition.PlayerTarget, 100)
        ),
        override val togglableSkills: MutableMap<Int, Boolean> = mutableMapOf(
                Pair(deflectArrow, false), Pair(holyBlade, true)
        )) : SkillPreferences(false)