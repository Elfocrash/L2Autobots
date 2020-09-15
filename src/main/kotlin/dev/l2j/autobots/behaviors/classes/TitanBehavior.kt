package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.getHealthPercentage
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class TitanBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(362),
            BotSkill(315) { _, _, _ -> Rnd.nextDouble() < 0.8 },
            BotSkill(190) { _, _, _ -> Rnd.nextDouble() < 0.2 }            )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(139) { player, _, _ -> player.getHealthPercentage() < 30 },
            BotSkill(176) { player, _, _ -> player.getHealthPercentage() < 15 }
    )

    override val conditionalSkills: List<Int> = emptyList()
}