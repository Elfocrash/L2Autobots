package dev.l2j.autobots.autofarm

import dev.l2j.autobots.ui.AutofarmUi
import net.sf.l2j.gameserver.model.actor.Player

internal object AutofarmCommandHandler {
    
    fun onBypass(player: Player, command: String) {
        if(!command.startsWith("autofarm")) {
            return
        }
        
        if(command == "autofarm") {
            AutofarmUi.index(player)
            return
        }
        
        val splitCommand = command.removePrefix("autofarm ").split(" ")
        
        when(splitCommand[0]){
            "start" -> {
                AutofarmManager.startFarm(player)
                AutofarmUi.index(player)
            }
            "stop" -> {
                AutofarmManager.stopFarm(player)
                AutofarmUi.index(player)
            }
            "close" -> {
                AutofarmUi.closeWindow(player)
            }
            "todo" -> {
                player.sendMessage("Not implemented yet")
            }
        }
    }
    
}