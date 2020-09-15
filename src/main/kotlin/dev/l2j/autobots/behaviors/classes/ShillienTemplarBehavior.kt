package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class ShillienTemplarBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val miscItems = listOf(MiscItem({ 1458 }, 100, 50))
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(shieldBash) { _, _, target -> target is Player && target.target == player },
            BotSkill(arrest) { player, _, target -> target is Player && !target.isRooted && distance(player, target) > 300 },
            BotSkill(touchOfDeath) { _, _, target -> target is Player && player.getHealthPercentage() < 75 },
            BotSkill(judgement)
    //TODO hex
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(summonVampiricCubic) { player, _, _ -> !player.isInCombat && player.getCubic(summonVampiricCubicNpcId) == null },
            BotSkill(summonPhantomCubic) { player, _, _ -> !player.isInCombat && player.getCubic(summonPhantomCubicNpcId) == null },
            BotSkill(summonViperCubic) { player, _, _ -> !player.isInCombat && player.getCubic(summonViperCubicNpcId) == null },
            BotSkill(magicalMirror) { player, _, _ -> player.getHealthPercentage() < 60 },
            BotSkill(ultimateDefense) { player, _, _ -> player.getHealthPercentage() < 20 }
    )

    override val conditionalSkills: List<Int> = emptyList()
}