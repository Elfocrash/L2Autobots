package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.Kiter
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.getArrowId
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class MoonlightSentinelBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem, Kiter {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val miscItems = listOf(MiscItem({b -> getArrowId(b)}))

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(101){_, _, target -> target is Player && target.pvpFlag > 0 },
            BotSkill(343){_, _, target -> target is Player },
            BotSkill(354){_, _, target -> target is Player },
            BotSkill(369){_, _, target -> target is Player })

    override val conditionalSkills: List<Int> = emptyList()
}