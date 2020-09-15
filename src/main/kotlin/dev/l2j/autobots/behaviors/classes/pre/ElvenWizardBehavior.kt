package dev.l2j.autobots.behaviors.classes.pre

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.distance
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class ElvenWizardBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), SecretManaRegen {

    override var skillPreferences: SkillPreferences = SkillPreferences(true)

    override fun getShotType(): ShotType {
        return ShotType.BLESSED_SPIRITSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(1264),
            BotSkill(1172){player, _, target -> target != null && distance(player, target) < 200 },
            BotSkill(1175),
            BotSkill(1274),
            BotSkill(1181))

    override val conditionalSkills: List<Int> = emptyList()
}