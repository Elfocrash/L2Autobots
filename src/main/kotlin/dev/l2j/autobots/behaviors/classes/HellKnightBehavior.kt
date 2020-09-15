package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.PetOwner
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.HellKnightSkillPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class HellKnightBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), PetOwner {

    override var skillPreferences: SkillPreferences = HellKnightSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(shieldStun) { _, _, target -> target is Player && !target.isStunned },
            BotSkill(shieldSlam) { _, _, target -> target is Player && !target.isMuted },            
            BotSkill(touchOfDeath) { player, skill, target -> target is Player && player.getCombatBehavior().validateConditionalSkill(skill) },
            BotSkill(shackle) { player, skill, target -> (target !is Player || ((target.pvpFlag > 0 || target.karma > 0) )) && !target!!.isRooted && player.getCombatBehavior().validateConditionalSkill(skill) },
            BotSkill(judgement)
            
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(ultimateDefense, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(physicalMirror, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(vengeance, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(shieldFortress, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(fortitude, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(deflectArrow, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
    )
    
    override val summonInfo: Pair<BotSkill, MiscItem?> = Pair(BotSkill(summonDarkPanther) { player, _, _ -> !player.isInCombat }, MiscItem({ 1459 }, 50, 10))
    
    override val petBuffs: Array<IntArray> = getDefaultFighterBuffs()

    override val conditionalSkills: List<Int> = listOf(shackle, ultimateDefense, touchOfDeath, physicalMirror, vengeance, shieldOfRevenge)
}