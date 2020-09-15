package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.EvasTemplarSkillPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class EvasTemplarBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem {

    override var skillPreferences: SkillPreferences = EvasTemplarSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val miscItems = listOf(MiscItem({1458}, 100, 50))

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(tribunal),
            BotSkill(shieldBash) { _, _, target -> target is Player && target.target == player },
            BotSkill(arrest) { player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && !target!!.isRooted && player.getCombatBehavior().validateConditionalSkill(skill) },
            BotSkill(songOfSilence) { player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && !target!!.isRooted && player.getCombatBehavior().validateConditionalSkill(skill) }
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(summonStormCubic) { player, _, _ -> player.getCombatBehavior().skillPreferences.togglableSkills[summonStormCubic]!! && !player.isInCombat && player.getCubic(summonStormCubicNpcId) == null },
            //BotSkill(summonLifeCubic) { player, _, _ -> !player.isInCombat && player.getCubic(summonLifeCubicNpcId) == null },
            BotSkill(summonAttractiveCubic) { player, _, _ -> player.getCombatBehavior().skillPreferences.togglableSkills[summonAttractiveCubic]!! && !player.isInCombat && player.getCubic(summonAttractiveCubicNpcId) == null },
            BotSkill(touchOfLife, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false, forceTargetSelf = true),
            BotSkill(magicalMirror, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = true),
            BotSkill(ultimateDefense, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(vengeance, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(shieldFortress, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(fortitude, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(deflectArrow, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(guardStance, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(holyArmor, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
    )

    override val conditionalSkills: List<Int> = listOf(arrest, ultimateDefense, touchOfLife, magicalMirror, vengeance)
}