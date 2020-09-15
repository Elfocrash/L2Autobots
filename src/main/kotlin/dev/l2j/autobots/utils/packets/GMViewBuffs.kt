package dev.l2j.autobots.utils.packets

import dev.l2j.autobots.Autobot
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket

internal class GMViewBuffs(val bot: Autobot) : L2GameServerPacket() {
    
    override fun writeImpl() {
        writeC(0x91)
        writeS(bot.name)
        writeD(bot.allEffects!!.size)
        for (skill in bot.allEffects) {
            writeD(0)
            writeD(skill.level)
            writeD(skill.skill.id)
            writeC(0)
        }
    }
}