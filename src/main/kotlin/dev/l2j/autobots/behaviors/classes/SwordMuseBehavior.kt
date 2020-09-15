package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.SwordMuseSkillPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class SwordMuseBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {
    
    override var skillPreferences: SkillPreferences = SwordMuseSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(arrest) { player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && !target!!.isRooted && player.getCombatBehavior().validateConditionalSkill(skill) },
            BotSkill(songOfSilence){player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && player.getCombatBehavior().validateConditionalSkill(skill) && target != null && !target.hasEffect(songOfSilence) }
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(ultimateDefense, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(deflectArrow, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(holyBlade, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
    )

    override val conditionalSkills: List<Int> = listOf(ultimateDefense, songOfSilence)
}