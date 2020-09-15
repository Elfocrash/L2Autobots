package dev.l2j.autobots.behaviors.sequences

import dev.l2j.autobots.extensions.findClosestGatekeeper
import dev.l2j.autobots.extensions.getSocialBehavior
import dev.l2j.autobots.extensions.location
import dev.l2j.autobots.extensions.moveTo
import dev.l2j.autobots.utils.CancellationToken
import dev.l2j.autobots.utils.distance
import kotlinx.coroutines.delay
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.xml.TeleportLocationData
import net.sf.l2j.gameserver.enums.ZoneId
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.location.Location

internal class ReturnToDeathLocationSequence(override val bot: Player) : Sequence {

    override var cancellationToken: CancellationToken? = null

    override suspend fun definition() {
        
        if (bot.lastDeathLocation == null) {
            return
        }

        delay(Rnd.get(5000, 8000).toLong())

        val closestGatekeeper = bot.findClosestGatekeeper() ?: return
        bot.target = closestGatekeeper
        bot.moveTo(closestGatekeeper.x, closestGatekeeper.y, closestGatekeeper.z)
        while (distance(bot, closestGatekeeper) >= 110) {

            if (bot.isDead) {
                return
            }

            if (distance(bot, closestGatekeeper) <= 1000) {
                closestGatekeeper.onAction(bot)
            }

            if (!bot.isMoving) {

            }
            delay(Rnd.get(4000, 6000).toLong())
        }
        delay(Rnd.get(2000, 4000).toLong())

        val closestTeleportLocation = TeleportLocationData.getInstance().allTeleportLocations
                .filter { !it.isNoble }
                .minBy { distance(bot.lastDeathLocation!!, Location(it.x, it.y, it.z)) }

        bot.teleportTo(closestTeleportLocation, 20)

        delay(Rnd.get(5000, 8000).toLong())
        if (bot.isInsideZone(ZoneId.TOWN)) {
            return bot.getSocialBehavior().onRespawn()
        }

        val deathLoc = bot.lastDeathLocation!!
        bot.moveTo(deathLoc.x, deathLoc.y, deathLoc.z)
        bot.lastDeathLocation = null
        while (distance(bot.location(), deathLoc) >= 1500) {

            if (bot.isDead) {
                return
            }

            if (bot.isInCombat) {
                return
            }

            if (!bot.isMoving) {
                if(!bot.isMoving) {
                    bot.moveTo(deathLoc.x, deathLoc.y, deathLoc.z)
                }
            }

            delay(Rnd.get(2000, 4000).toLong())
        }
    }
}