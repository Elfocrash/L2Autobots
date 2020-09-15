package dev.l2j.autobots.utils

import dev.l2j.autobots.AutobotsNameService
import dev.l2j.autobots.utils.packets.ServerSideImage
import net.sf.l2j.Config
import net.sf.l2j.gameserver.model.actor.Player

internal fun imageTag(id: Int, width: Int, height: Int) : String{
    return "<img src=\"Crest.crest_${Config.SERVER_ID}_$id\" width=$width height=$height>"
}

internal fun sendImagePacket(player: Player, imageId: Int, imageName: String){
    val buffer = DDSConverter.convertToDDS(AutobotsNameService.javaClass.classLoader.getResource("images/$imageName")!!.openStream())!!
    buffer.position(0)
    val arr = ByteArray(buffer.remaining())
    buffer.get(arr)
    val packet = ServerSideImage(imageId, arr)
    player.sendPacket(packet)
}