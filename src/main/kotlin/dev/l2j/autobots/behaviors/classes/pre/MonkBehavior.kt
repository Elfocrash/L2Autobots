package dev.l2j.autobots.behaviors.classes.pre

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.SecretManaRegen
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.skills.BotSkill
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class MonkBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), SecretManaRegen {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)

    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(120){ _, _, target -> target is Player && !target.isStunned},
            BotSkill(54){ player, skill, _ -> player.charges < skill.maxCharges},
            BotSkill(284){ player, skill, _ -> player.charges < skill.maxCharges})

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(50){player, skill, _ ->  player.charges < skill.maxCharges }
    )

    override val conditionalSkills: List<Int> = emptyList()
}