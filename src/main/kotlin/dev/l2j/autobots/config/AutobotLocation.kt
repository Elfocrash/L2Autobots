package dev.l2j.autobots.config

import net.sf.l2j.gameserver.model.location.Location

data class AutobotLocation(val x: Int, val y: Int, val z: Int, val location: Location = Location(x, y, z))