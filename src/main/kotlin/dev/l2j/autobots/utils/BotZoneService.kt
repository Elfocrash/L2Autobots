package dev.l2j.autobots.utils

import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive
import java.awt.Color

internal object BotZoneService {
    var player: Player? = null    
    var graph: BotGraph = BotGraph()
    
    
    fun sendZone(player: Player){
        val packet = ExServerPrimitive(player.name + "_", player.enterWorldLocation.x, player.enterWorldLocation.y, -65535)
        if(!graph.points.any()) {
            return
        }
        
        for (i in 0 until graph.points.size) {
            val point = graph.points[i]            
            packet.addPoint(i.toString(), point.color, point.isNameColored, point.x, point.y, point.z)
            
            if(i + 1 < graph.points.size) {
                val nextPoint = graph.points[i+1]
                packet.addLine(point.color, point.x, point.y, point.z, nextPoint.x, nextPoint.y, nextPoint.z)
            }            
        }    
        
        player.sendPacket(packet)
    }
}

internal data class BotGraph(val points: MutableList<BotZonePoint> = mutableListOf())

internal data class BotZonePoint(val x: Int, val y: Int, val z: Int, val color: Color = Color.GREEN, val isNameColored: Boolean = true)