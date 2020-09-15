package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.attributes.Kiter
import dev.l2j.autobots.behaviors.attributes.MiscItem
import dev.l2j.autobots.behaviors.attributes.RequiresMiscItem
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.behaviors.preferences.skills.SagittariusSkillPreferences
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player


internal class SagittariusBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences), RequiresMiscItem, Kiter {

    override var skillPreferences: SkillPreferences = SagittariusSkillPreferences()
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }

    override val getOffensiveSkills: List<BotSkill> = listOf(
            BotSkill(stunningShot){_, _, target -> target is Player && (target.pvpFlag > 0 || target.karma > 0) && !target.isStunned},
            BotSkill(lethalShot){_, _, target -> target is Player },
            BotSkill(hamstringShot){_, _, target -> target is Player && !target.hasEffect(hamstringShot) })

    override val getSelfSupportSkills: List<BotSkill> = listOf(
            BotSkill(rapidShot, isConditionalSkill = false, isTogglableSkill = false, useWhenEffectIsNotPresent = true),
            BotSkill(dash, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = true),
            BotSkill(ultimateEvasion, isConditionalSkill = true, isTogglableSkill = false, useWhenEffectIsNotPresent = false),
            BotSkill(hawkEye, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(viciousStance, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true),
            BotSkill(accuracy, isConditionalSkill = false, isTogglableSkill = true, useWhenEffectIsNotPresent = true)
    )
    
    override val miscItems = listOf(MiscItem({ b -> getArrowId(b)}))

    override val conditionalSkills: List<Int> = listOf(dash, ultimateEvasion)
}