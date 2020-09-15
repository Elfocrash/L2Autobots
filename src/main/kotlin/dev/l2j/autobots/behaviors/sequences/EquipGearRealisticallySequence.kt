package dev.l2j.autobots.behaviors.sequences

import dev.l2j.autobots.utils.CancellationToken
import dev.l2j.autobots.utils.giveItemsByClassAndLevel
import net.sf.l2j.gameserver.model.actor.Player

internal class EquipGearRealisticallySequence(override val bot: Player) : Sequence {

    override var cancellationToken: CancellationToken? = null

    override suspend fun definition() {
        giveItemsByClassAndLevel(bot, addRealistically = true)
    }
}