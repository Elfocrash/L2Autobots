package dev.l2j.autobots.behaviors.classes.pre

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.getArrowId
import dev.l2j.autobots.utils.getHealthPercentage
import dev.l2j.autobots.utils.hasEffect
import dev.l2j.autobots.utils.rapidShot
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class RogueBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem, SecretManaRegen {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)

    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(16),
            BotSkill(96){_, _, target -> target?.getFirstEffect(96) != null},
            BotSkill(101){_, _, target -> target is Player && target.pvpFlag > 0 && !target.isStunned})

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(rapidShot) { player, _, _ -> !player.hasEffect(rapidShot)},
            BotSkill(111) { player, _, _ -> player.getHealthPercentage() < 40}
    )
    
    override val miscItems = listOf(MiscItem({ b -> getArrowId(b) }))

    override val conditionalSkills: List<Int> = emptyList()
}