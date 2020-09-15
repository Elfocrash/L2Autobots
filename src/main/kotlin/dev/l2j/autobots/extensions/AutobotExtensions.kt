package dev.l2j.autobots.extensions

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.utils.getSupportedClasses
import net.sf.l2j.gameserver.enums.actors.ClassId


internal fun Autobot.shouldChangeClass(oldLevel: Int, newLevel: Int) : Boolean {
    return oldLevel < 20 && newLevel >= 20 ||
            oldLevel < 40 && newLevel >= 40 ||
            oldLevel < 76 && newLevel >= 76
}

internal fun Autobot.getClassLevelForPlayerLevel(level: Int) : Int{
    return when{
        level in 1..19 -> 0
        level in 20..39 -> 1
        level in 40..75 -> 2
        level >= 76 -> 3
        else -> 3
    }
}

@OptIn(ExperimentalStdlibApi::class)
internal fun Autobot.getNewClassId(currentClassId: ClassId) : ClassId? {
    val classLevels = getClassLevelForPlayerLevel(level) - classId.level()
    if(classLevels == 0)
        return currentClassId

    val newClassId = (if(currentClassId.level() < 2) ClassId.values().filter { it.parent == currentClassId && getSupportedClasses.contains(it) }.randomOrNull() else ClassId.values().firstOrNull { it.parent == currentClassId })
            ?: return null

    if(newClassId.level() == getClassLevelForPlayerLevel(level)) {
        return newClassId
    }

    return getNewClassId(newClassId)
}