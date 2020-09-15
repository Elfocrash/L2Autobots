package dev.l2j.autobots.ui

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.AutobotsManager
import dev.l2j.autobots.CoScopes
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.dao.AutobotsDao
import dev.l2j.autobots.ui.states.BotDetailsViewState
import dev.l2j.autobots.ui.states.ViewStates
import dev.l2j.autobots.ui.tabs.BotDetailsTab
import kotlinx.coroutines.launch
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.SkillTable
import net.sf.l2j.gameserver.data.sql.ClanTable
import net.sf.l2j.gameserver.model.actor.Player

internal object AdminActions {
    internal fun createAndSpawnRandomBots(splitCommand: List<String>, activeChar: Player) {
        val count = splitCommand.getOrElse(2) { "1" }.toInt()
        createAndSpawnRandomAutobots(count, activeChar)
    }

    internal fun toggleSitting(bot: Autobot) {
        if (bot.isSitting) {
            bot.forceStandUp()
        } else {
            bot.sitDown()
        }
    }

    internal fun selectBotDetailsTab(activeChar: Player, tab: BotDetailsTab) {
        val state = ViewStates.getActiveState(activeChar)
        if (state is BotDetailsViewState) {
            state.activeTab = tab
        }
    }

    internal fun removeSkillPreference(splitCommand: List<String>, state: BotDetailsViewState) {
        val skillId = splitCommand[4].toInt()
        state.activeBot.combatBehavior.skillPreferences.skillUsageConditions.removeAll { it.skillId == skillId }
        AutobotsDao.saveSkillPreferences(state.activeBot)
    }

    internal fun removeSelectedBotsFromClan(activeChar: Player) {
        ViewStates.indexViewState(activeChar).selectedBots.forEach {
            val bot = AutobotsManager.getBotFromOnlineOrDb(it.key) ?: return@forEach

            if (bot.clan == null) return@forEach

            if (bot.isClanLeader) {
                if (bot.clan.allyId != 0) {
                    activeChar.sendMessage("You cannot delete a clan in an ally. Delete the ally first.")
                    return@forEach
                }

                ClanTable.getInstance().destroyClan(bot.clan)
                return@forEach
            }

            AutobotsDao.removeClanMember(bot, bot.clan)
            if (!bot.isOnline) {
                AutobotsDao.saveAutobot(bot)
            }
        }
    }

    internal fun saveSkillUsageCondition(splitCommand: List<String>, state: BotDetailsViewState, statusCondition: StatusCondition, comparisonCondition: ComparisonCondition, conditionValueType: ConditionValueType, targetCondition: TargetCondition, value: Int) {
        val skillName = splitCommand.subList(9, splitCommand.size).joinToString(" ")
        val skillId = state.activeBot.combatBehavior.conditionalSkills.filter { SkillTable.getInstance().getInfo(it, 1).name == skillName }.map { it }.first()
        val skillUsageCondition = SkillUsageCondition(skillId, statusCondition, comparisonCondition, conditionValueType, targetCondition, value)

        if (splitCommand[3] == "s") {
            state.activeBot.combatBehavior.skillPreferences.skillUsageConditions.removeAll { it.skillId == skillId }
        }

        state.activeBot.combatBehavior.skillPreferences.skillUsageConditions.add(skillUsageCondition)
        AutobotsDao.saveSkillPreferences(state.activeBot)
        state.skillUnderEdit = 0
    }

    internal fun setCurrentPagination(splitCommand: List<String>, pageNumberIndex: Int, pageSizeIndex: Int): Pair<Int, Int> {
        val pageNumber = splitCommand[pageNumberIndex].toInt()
        val pageSize = splitCommand[pageSizeIndex].toInt()
        return Pair(pageNumber, pageSize)
    }

    internal fun despawnBotsInRadius(activeChar: Player, radius: String) {
        val botsInRadius = activeChar.getKnownTypeInRadius(Autobot::class.java, radius.toInt())
        CoScopes.massDespawnerScope.launch {
            for (bot in botsInRadius) {
            
                despawnAutobot(bot)
            }
        }
    }

    internal fun createAndSpawnRandomAutobots(count: Int, activeChar: Player) {
        CoScopes.generalScope.launch {
            for (i in 1..count) {
                CoScopes.massSpawnerScope.launch {
                    AutobotsManager.createAndSpawnAutobot(activeChar.x + Rnd.get(-150, 150), activeChar.y + Rnd.get(-150, 150), activeChar.z)
                }
            }
        }
    }

    internal fun despawnAutobot(autobot: Autobot?) {
        autobot?.despawn()
    }
}