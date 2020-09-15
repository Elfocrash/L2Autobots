package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class GhostHunterBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(bluff) { _, _, target -> target !is Player || (target.pvpFlag > 0 || target.karma > 0)  },
            BotSkill(backstab) { player, _, _ -> player.isBehindTarget },
            BotSkill(lethalBlow),
            BotSkill(deadlyBlow),
            BotSkill(mortalStrike),
            BotSkill(blindingBlow))

    override val conditionalSkills: List<Int> = emptyList()
}