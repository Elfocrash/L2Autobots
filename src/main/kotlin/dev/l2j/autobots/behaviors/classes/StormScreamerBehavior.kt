package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class StormScreamerBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = SkillPreferences(true)
    
    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(1341),
            BotSkill(1343),
            BotSkill(1234),
            BotSkill(1239))

    override val conditionalSkills: List<Int> = emptyList()
}