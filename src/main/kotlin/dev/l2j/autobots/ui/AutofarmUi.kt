package dev.l2j.autobots.ui

import dev.l2j.autobots.AutobotsNameService
import dev.l2j.autobots.autofarm.AutofarmManager
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.network.serverpackets.TutorialCloseHtml
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowHtml

internal object AutofarmUi {

    private fun readFileText(fileName: String) = AutobotsNameService.javaClass.classLoader.getResource(fileName)!!.readText()
    
    fun index(player: Player) {
        val html = readFileText("views/autofarm_main.htv")
                .replace("{{startstopcmd}}", if(AutofarmManager.isAutoFarming(player)) "stop" else "start")
                .replace("{{startstoptext}}", if(AutofarmManager.isAutoFarming(player)) "Stop" else "Start")
                .replace("{{closebtn}}", if(AutofarmManager.isAutoFarming(player)) "" else "<button action=\"bypass autofarm close\" value=\"Close\" width=74 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"><br>")
        sendQuestWindow(player, html)
    }
    
    fun closeWindow(player: Player){
        player.sendPacket(TutorialCloseHtml.STATIC_PACKET)
    }
    
    private fun sendQuestWindow(player: Player, text: String){
        player.sendPacket(TutorialShowHtml(text))
    }
}