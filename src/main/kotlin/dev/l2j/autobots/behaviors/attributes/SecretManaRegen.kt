package dev.l2j.autobots.behaviors.attributes

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.utils.getManaPercentage
import net.sf.l2j.gameserver.model.actor.Player

interface SecretManaRegen {
    fun regenMana(autobot: Player){
        if(autobot.getManaPercentage() < 20 && autobot is Autobot) {
            autobot.currentMp = autobot.maxMp.toDouble()
        }
    }
}