package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.DreadnoughtSkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class DreadnoughtBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = DreadnoughtSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(361),
            BotSkill(347){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 },
            BotSkill(48){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 },
            BotSkill(452){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 },
            BotSkill(36){player, _, target -> target != null && !target.isDead && distance(player, target) < 100 })

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(braveheart, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(revival, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(battleroar, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(fellSwoop, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(viciousStance, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(warFrenzy, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(thrillFight, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
            
    )

    override val conditionalSkills: List<Int> = listOf(braveheart,revival,battleroar)
}