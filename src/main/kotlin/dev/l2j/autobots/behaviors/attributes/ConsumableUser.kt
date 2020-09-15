package dev.l2j.autobots.behaviors.attributes

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.extensions.useItem
import net.sf.l2j.gameserver.model.actor.Player

internal interface ConsumableUser {
    val consumables: List<Consumable>
    
    fun handleConsumables(player: Player){
        if(consumables.isEmpty()) return

        consumables.forEach {
            
            if(!it.condition(player)) return@forEach
            
            val items = player.inventory.getItemsByItemId(it.consumableId)
            
            if(items.isEmpty() && player is Autobot) {
                player.addItem("AubotoItem", it.consumableId, 200, player, false)
            }
            
            player.useItem(it.consumableId)
        }
    }
}
internal data class Consumable(val consumableId: Int, val condition: (Player) -> Boolean = { true })