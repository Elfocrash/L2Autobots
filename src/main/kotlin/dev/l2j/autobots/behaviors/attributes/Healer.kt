package dev.l2j.autobots.behaviors.attributes

import dev.l2j.autobots.utils.getHealthPercentage
import dev.l2j.autobots.utils.isFullHealth
import net.sf.l2j.gameserver.model.actor.Player

internal interface Healer {
    fun tryTargetingLowestHpTargetInRadius(player: Player, radius: Int) {
        val finalTargets = mutableListOf<Player>()
        
        val targets = player.getKnownTypeInRadius(Player::class.java, radius).filter { (it.isInParty && player.isInParty && player.party == it.party) || (it.clan != null && player.clan != null && player.clanId == it.clanId) }.toMutableList()
        finalTargets.add(player)
        
        val partyMembers = targets.filter { it.isInParty && player.isInParty && it.party == player.party && it.getHealthPercentage() < 80 }
        
        if(partyMembers.isEmpty()) {
            val clanMembers = targets.filter { it.clan != null && player.clan != null && player.clanId == it.clanId && it.getHealthPercentage() < 80 }
            
            if(clanMembers.isNotEmpty()) {
                finalTargets.addAll(clanMembers)
            }
        } else {
            finalTargets.addAll(partyMembers)
        }
        
        val target = finalTargets.filter { !it!!.isDead }.minBy { it!!.getHealthPercentage() } ?: return

        //TODO fix this broken shit

        if(!target.isFullHealth() && target.getHealthPercentage() < 80) {
            player.target = target
            return
        }
        
        if(target.isDead) {
            player.target = null
            return
        }
    }
}