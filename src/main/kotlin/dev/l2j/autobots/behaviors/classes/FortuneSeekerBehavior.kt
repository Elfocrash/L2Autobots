package dev.l2j.autobots.behaviors.classes

import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.CombatPreferences
import dev.l2j.autobots.behaviors.preferences.SkillPreferences
import dev.l2j.autobots.extensions.getClosestEntityInRadius
import dev.l2j.autobots.extensions.useMagicSkill
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import kotlinx.coroutines.delay
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.Monster


internal class FortuneSeekerBehavior(player: Player, combatPreferences: CombatPreferences) : CombatBehavior(player, combatPreferences) {

    override var skillPreferences: SkillPreferences = SkillPreferences(false)
    
    override fun getShotType(): ShotType {
        return ShotType.SOULSHOT
    }
    
    override val getOffensiveSkills: List<BotSkill> = listOf(            
            BotSkill(spoilCrush){ player, _, target -> target is Monster && target.spoilerId != player.objectId && target.spoilerId == 0},
            BotSkill(spoil){ player, _, target -> target is Monster && target.spoilerId != player.objectId && target.spoilerId == 0},
            BotSkill(hammerCrush){_, _, target -> target is Player && !target.isStunned},
            BotSkill(stunAttack){_, _, target -> target is Player && !target.isStunned},
            BotSkill(armorCrush)
    )

    override suspend fun afterAttack() {
        if(player.target == null || player.target !is Monster)
            return
        
        val sweeperSkill = player.getSkill(42) ?: return
        
        val mob = (player.target as Monster)
        if(mob.getHealthPercentage() < 10 && mob.spoilerId == player.objectId)
            delay(2000)
        
        if(mob.isDead && mob.spoilerId == player.objectId) {
            player.useMagicSkill(sweeperSkill)
            delay(1000)
        }
        
        val sweepable = player.getClosestEntityInRadius<Monster>(300) { it is Monster && it.isDead && it.spoilerId == player.objectId}
        
        if(sweepable != null) {
            val previousTarget = player.target
            player.target = sweepable
            player.useMagicSkill(sweeperSkill)
            delay(1500)
            player.target = previousTarget
        }
    }

    override val conditionalSkills: List<Int> = emptyList()
}