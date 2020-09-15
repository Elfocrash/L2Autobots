package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.PetOwner
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.SoultakerSkillPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class SoultakerBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem, PetOwner {

    override var skillPreferences: SkillPreferences = SoultakerSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }

    override val miscItems = listOf(MiscItem({ 2508 }))
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(curseGloom){ player, _, target -> player.getCombatBehavior().skillPreferences.togglableSkills[curseGloom]!! && (/*(target is Monster) || */(target is Player && (target.pvpFlag > 0 || target.karma > 0)) && !target.hasEffect(curseGloom))},
            BotSkill(anchor){ player, _, target -> player.getCombatBehavior().skillPreferences.togglableSkills[anchor]!! && target is Player && (target.pvpFlag > 0 || target.karma > 0) && !target.hasEffect(anchor)},
            BotSkill(curseOfAbyss){ player, _, target -> player.getCombatBehavior().skillPreferences.togglableSkills[curseOfAbyss]!! && target is Player && (target.pvpFlag > 0 || target.karma > 0) && !target.hasEffect(curseOfAbyss)},
            BotSkill(curseOfDoom){ player, _, target -> player.getCombatBehavior().skillPreferences.togglableSkills[curseOfDoom]!! && target is Player && (target.pvpFlag > 0 || target.karma > 0) && !target.hasEffect(curseOfDoom)},
            BotSkill(silence){ player, _, target -> player.getCombatBehavior().skillPreferences.togglableSkills[silence]!! && target is Player && (target.pvpFlag > 0 || target.karma > 0) && !target.hasEffect(silence)},
            BotSkill(slow) { player, skill, target -> target != null && !target.hasEffect(slow) && player.getCombatBehavior().validateConditionalSkill(skill)},
            BotSkill(darkVortex),
            BotSkill(vampiricClaw){ player, skill, _ -> player.getCombatBehavior().validateConditionalSkill(skill)},
            BotSkill(deathSpike))

    override val summonInfo: Pair<BotSkill, MiscItem?> = Pair(BotSkill(1129) { _, _, target -> target != null && target.isDead }, MiscItem({ 1459 }, 50, 10))

    override val petBuffs: Array<IntArray> = emptyArray()

    override val getSelfSupportSkills: List<BotSkill> =
            listOf(
                    BotSkill(arcanePower, { player, _, _ -> player.getCombatBehavior().skillPreferences.togglableSkills[arcanePower]!! && !player.hasEffect(arcanePower) && player.isInCombat }, true),
                    BotSkill(transferPain, { player, _, _ -> player.getCombatBehavior().skillPreferences.togglableSkills[transferPain]!! && !player.hasEffect(transferPain) && player.hasServitor() }, true)
            )
    
    override val conditionalSkills: List<Int> = listOf(slow, vampiricClaw)
}