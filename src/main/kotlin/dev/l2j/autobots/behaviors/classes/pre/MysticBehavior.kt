package dev.l2j.autobots.behaviors.classes.pre

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class MysticBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), SecretManaRegen {

    override var skillPreferences: SkillPreferences = SkillPreferences(true)

    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(1147),
            BotSkill(1184),
            BotSkill(1177))

    override val conditionalSkills: List<Int> = emptyList()
}