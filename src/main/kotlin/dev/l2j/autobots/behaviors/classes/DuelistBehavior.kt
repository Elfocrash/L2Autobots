package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.DuelistCombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.DuelistSkillPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.Monster


internal class DuelistBehavior(player: Player, combatPreferences: DuelistCombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = DuelistSkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(sonicRage) {player, _, target -> shouldUseSkillOnTarget(target, player.getCombatBehavior().typedSkillPreferences()) && player.charges < 7 },
            BotSkill(tripleSonicSlash) {player, _, target -> shouldUseSkillOnTarget(target, player.getCombatBehavior().typedSkillPreferences())},
            BotSkill(doubleSonicSlash){player, _, target -> shouldUseSkillOnTarget(target, player.getCombatBehavior().typedSkillPreferences())},
            BotSkill(sonicBlaster){player, _, target -> shouldUseSkillOnTarget(target, player.getCombatBehavior().typedSkillPreferences())},
            BotSkill(tripleSlash){player, _, target -> shouldUseSkillOnTarget(target, player.getCombatBehavior().typedSkillPreferences())})

    private fun shouldUseSkillOnTarget(target: Creature?, skillPreferences: DuelistSkillPreferences) =
            ((target is Monster && skillPreferences.useSkillsOnMobs) || target !is Monster)

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(sonicFocus){player, skill, _ ->  player.getSkill(345) == null && player.charges < skill.maxCharges },
            BotSkill(duelistSpirit, isConditionalSkill = false, isTogglableSkill = false, useWhenEffectIsNotPresent = true),
            BotSkill(braveheart, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(battleroar, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(sonicBarrier, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(sonicMove, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = true)
    )

    override val conditionalSkills: List<Int> = listOf(
            braveheart,
            battleroar,
            sonicBarrier,
            sonicMove
    )
}