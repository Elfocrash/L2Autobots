package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class SpectralDancerBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(arrest) { _, _, target -> target is Player && !target.isRooted },
            BotSkill(judgement),
            BotSkill(demonicBladeDance){ player, _, target -> target is Player && distance(player, target) < 100 && Rnd.get(1, 10) < 4 }
            
    )

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(ultimateDefense) { player, _, _ -> player.getHealthPercentage() < 20 }
    )

    override val conditionalSkills: List<Int> = emptyList()
}