package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.PhoenixKnightSkillPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class PhoenixKnightBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem {

    override var skillPreferences: SkillPreferences = PhoenixKnightSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val miscItems = listOf(MiscItem({ 1459 }, 50, 10))
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(shieldSlam) { _, _, target -> target is Player && !target.isMuted },
            BotSkill(shieldStun) { _, _, target -> target is Player && !target.isStunned },
            BotSkill(shackle) { player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && !target!!.isRooted && player.getCombatBehavior().validateConditionalSkill(skill) },
            BotSkill(tribunal)
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(angelicIcon, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(touchOfLife, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(ultimateDefense, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(holyBlessing, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(deflectArrow, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(aegisStance, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(shieldFortress, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(fortitude, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(holyBlade, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(holyArmor, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(physicalMirror, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(vengeance, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false)
    )

    override val conditionalSkills: List<Int> = listOf(shackle, ultimateDefense, holyBlessing, physicalMirror, vengeance, touchOfLife, angelicIcon)
}