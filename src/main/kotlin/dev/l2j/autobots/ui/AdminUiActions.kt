package dev.l2j.autobots.ui

import dev.l2j.autobots.AutobotData
import dev.l2j.autobots.AutobotScheduler
import dev.l2j.autobots.AutobotsNameService
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.behaviors.preferences.skills.DuelistSkillPreferences
import dev.l2j.autobots.dao.AutobotsDao
import dev.l2j.autobots.ui.states.*
import dev.l2j.autobots.ui.tabs.BotDetailsCombatEditAction
import net.sf.l2j.gameserver.model.actor.Player
import java.time.Clock
import java.time.LocalDateTime

//TODO A shit ton of redundant code. I need to refactor this and extract some stuff
internal object AdminUiActions {
    
    internal fun selectRace(splitCommand: List<String>, activeChar: Player) {
        val raceName = splitCommand[3].replace("_", " ")
        val state = ViewStates.createBotViewState(activeChar)
        state.botDetails.race = raceName
    }
    
    internal fun handleSaveInfo(activeChar: Player, splitCommand: List<String>): Boolean {
        val activeState = ViewStates.getActiveState(activeChar)

        when (splitCommand[3]) {
            "bass" -> {
                if (activeState is BotDetailsViewState) {
                    val loginTime = "${splitCommand[4]}:${splitCommand[5]}"
                    val logoutTime = "${splitCommand[6]}:${splitCommand[7]}"

                    val dateTimeNow = LocalDateTime.now(Clock.systemUTC())
                    val loginDateTime = LocalDateTime.parse("${dateTimeNow.year}-${if (dateTimeNow.monthValue < 10) "0${dateTimeNow.monthValue}" else dateTimeNow.monthValue}-${if (dateTimeNow.dayOfMonth < 10) "0${dateTimeNow.dayOfMonth}" else dateTimeNow.dayOfMonth} ${loginTime}", AutobotScheduler.formatter)
                    val logoutDateTime = LocalDateTime.parse("${dateTimeNow.year}-${if (dateTimeNow.monthValue < 10) "0${dateTimeNow.monthValue}" else dateTimeNow.monthValue}-${if (dateTimeNow.dayOfMonth < 10) "0${dateTimeNow.dayOfMonth}" else dateTimeNow.dayOfMonth} ${logoutTime}", AutobotScheduler.formatter)

                    if(loginDateTime > logoutDateTime) {
                        activeChar.sendMessage("Login time needs to be before the logout time")
                        return true
                    }else {
                        activeState.activeBot.combatBehavior.activityPreferences.loginTime = loginTime
                        activeState.activeBot.combatBehavior.activityPreferences.logoutTime = logoutTime
                        AutobotsDao.saveActivityPreferences(activeState.activeBot)
                        if(logoutDateTime < dateTimeNow) {
                            activeState.activeBot.despawn()
                            AutobotScheduler.removeBot(activeState.activeBot)
                        }else {
                            AutobotScheduler.addBot(activeState.activeBot)
                        }

                    }
                }
            }
            UiComponents.TargetRadius -> {
                val radius = splitCommand.getOrElse(4) { "0" }.toInt()
                if (radius < 100 || radius > 20000) {
                    activeChar.sendMessage("Radius needs to be between 100 and 20000")
                    return true
                }

                if (activeState is BotDetailsViewState) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    activeState.activeBot.combatBehavior.combatPreferences.targetingRadius = radius
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.CreateBotName -> {
                if (activeState is CreateBotViewState) {
                    val name = splitCommand.getOrElse(4) { activeState.botDetails.name }
                    if (!AutobotsNameService.nameIsValid(name)) {
                        activeChar.sendMessage("Name is invalid")
                        return true
                    }

                    activeState.editAction = CreateBotEditAction.None
                    activeState.botDetails.name = name
                }
            }
            UiComponents.CreateBotLevel -> {
                if (activeState is CreateBotViewState) {
                    val level = splitCommand.getOrElse(4) { activeState.botDetails.level.toString() }.toInt()
                    if (level < 1 || level > 80) {
                        activeChar.sendMessage("Level needs to be between 1 and 80")
                        return true
                    }
                    activeState.editAction = CreateBotEditAction.None
                    activeState.botDetails.level = level
                }
            }
            UiComponents.CreateBotWeaponEnch -> {
                if (activeState is CreateBotViewState) {
                    val enchant = splitCommand.getOrElse(4) { activeState.botDetails.weaponEnchant.toString() }.toInt()
                    if (enchant < 0 || enchant > 65535) {
                        activeChar.sendMessage("Enchant needs to be between 0 and 65535")
                        return true
                    }
                    activeState.editAction = CreateBotEditAction.None
                    activeState.botDetails.weaponEnchant = enchant
                }
            }
            UiComponents.CreateBotArmorEnch -> {
                if (activeState is CreateBotViewState) {
                    val enchant = splitCommand.getOrElse(4) { activeState.botDetails.armorEnchant.toString() }.toInt()
                    if (enchant < 0 || enchant > 65535) {
                        activeChar.sendMessage("Enchant needs to be between 0 and 65535")
                        return true
                    }
                    activeState.editAction = CreateBotEditAction.None
                    activeState.botDetails.armorEnchant = enchant
                }
            }
            UiComponents.CreateBotJewelEnch -> {
                if (activeState is CreateBotViewState) {
                    val enchant = splitCommand.getOrElse(4) { activeState.botDetails.jewelEnchant.toString() }.toInt()
                    if (enchant < 0 || enchant > 65535) {
                        activeChar.sendMessage("Enchant needs to be between 0 and 65535")
                        return true
                    }
                    activeState.editAction = CreateBotEditAction.None
                    activeState.botDetails.jewelEnchant = enchant
                }
            }
            UiComponents.TargetPref -> {

                val pref = TargetingPreference.valueOf(splitCommand[4])
                if (activeState is BotDetailsViewState) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    activeState.activeBot.combatBehavior.combatPreferences.targetingPreference = pref
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.AttackPlayerTypeUi -> {

                val pref = AttackPlayerType.valueOf(splitCommand[4])
                if (activeState is BotDetailsViewState) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    activeState.activeBot.combatBehavior.combatPreferences.attackPlayerType = pref
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.KiteRadius -> {
                val pref = splitCommand[4].toInt()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.combatPreferences is ArcherCombatPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.combatPreferences as ArcherCombatPreferences).kiteRadius = pref
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.IsKiting -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.combatPreferences is ArcherCombatPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.combatPreferences as ArcherCombatPreferences).isKiting = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.SummonsPet -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.combatPreferences is PetOwnerPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).summonPet = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.PetAssists -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.combatPreferences is PetOwnerPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).petAssists = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.PetUsesShots -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.combatPreferences is PetOwnerPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).petUsesShots = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.PetHasBuffs -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.combatPreferences is PetOwnerPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).petHasBuffs = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.UseSkillsOnMobs -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState && activeState.activeBot.combatBehavior.skillPreferences is DuelistSkillPreferences) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    (activeState.activeBot.combatBehavior.skillPreferences as DuelistSkillPreferences).useSkillsOnMobs = checked
                    AutobotsDao.saveSkillPreferences(activeState.activeBot)
                }
            }
            UiComponents.UseCpPots -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    activeState.activeBot.combatBehavior.combatPreferences.useGreaterCpPots = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.UseGhPots -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    activeState.activeBot.combatBehavior.combatPreferences.useGreaterHealingPots = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.UseQhPots -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState) {
                    activeState.combatEditAction = BotDetailsCombatEditAction.None
                    activeState.activeBot.combatBehavior.combatPreferences.useQuickHealingPots = checked
                    AutobotsDao.saveCombatPreferences(activeState.activeBot)
                }
            }
            UiComponents.ActivityNoneActive -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState) {
                    if (activeState.activeBot.combatBehavior.activityPreferences.activityType != ActivityType.None) {
                        activeState.activityEditAction = ActivityEditAction.None
                        activeState.activeBot.combatBehavior.activityPreferences.activityType = ActivityType.None
                        AutobotScheduler.removeBot(activeState.activeBot)
                        AutobotsDao.saveActivityPreferences(activeState.activeBot)
                    }
                }
            }
            UiComponents.ActivityUptimeActive -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState) {
                    if(checked) {
                        activeState.activeBot.combatBehavior.activityPreferences.activityType = ActivityType.Uptime
                        AutobotsDao.saveActivityPreferences(activeState.activeBot)
                    }else {
                        activeState.activeBot.combatBehavior.activityPreferences.activityType = ActivityType.None
                        AutobotsDao.saveActivityPreferences(activeState.activeBot)
                    }
                    AutobotScheduler.removeBot(activeState.activeBot)
                }
            }
            UiComponents.ActivityScheduleActive -> {
                val checked = splitCommand[4].toBoolean()
                if (activeState is BotDetailsViewState) {
                    if(checked) {
                        activeState.activeBot.combatBehavior.activityPreferences.activityType = ActivityType.Schedule
                        AutobotScheduler.addBot(activeState.activeBot)
                        AutobotsDao.saveActivityPreferences(activeState.activeBot)
                    }else {
                        activeState.activeBot.combatBehavior.activityPreferences.activityType = ActivityType.None
                        AutobotScheduler.removeBot(activeState.activeBot)
                        AutobotsDao.saveActivityPreferences(activeState.activeBot)
                    }
                }
            }
            UiComponents.EditUptime -> {
                val uptime = splitCommand[4].toInt()
                if (activeState is BotDetailsViewState) {
                    activeState.activityEditAction = ActivityEditAction.Uptime
                    activeState.activeBot.combatBehavior.activityPreferences.uptimeMinutes = uptime
                    AutobotsDao.saveActivityPreferences(activeState.activeBot)
                }
            }
            UiComponents.PrivateSellMessage -> {
                val sellMessage = splitCommand.subList(4, splitCommand.size).joinToString(" ")
                if (activeState is BotDetailsViewState) {
                    activeState.sellMessage = sellMessage
                    activeState.socialEditAction = BotDetailsSocialEditAction.None
                }
            }
            UiComponents.PrivateBuyMessage -> {
                val buyMessage = splitCommand.subList(4, splitCommand.size).joinToString(" ")
                if (activeState is BotDetailsViewState) {
                    activeState.buyMessage = buyMessage
                    activeState.socialEditAction = BotDetailsSocialEditAction.None
                }
            }
            UiComponents.PrivateCraftMessage -> {
                val craftMessage = splitCommand.subList(4, splitCommand.size).joinToString(" ")
                if (activeState is BotDetailsViewState) {
                    activeState.craftMessage = craftMessage
                    activeState.socialEditAction = BotDetailsSocialEditAction.None
                }
            }
            UiComponents.EditThinkIteration -> {
                if (activeState is SettingsViewState) {
                    val duration = splitCommand[4].toLong()
                    
                    if(duration < 200 || duration > 5000) {
                        
                        activeChar.sendMessage("Value needs to be between 200 and 5000 ms")
                        return false
                    }
                    
                    AutobotData.settings.iterationDelay = duration
                    activeState.editAction = SettingsEditAction.None
                    AutobotData.settings.save()
                }
            }
            UiComponents.EditDefaultTitle -> {
                if (activeState is SettingsViewState) {
                    val title = splitCommand.subList(4, splitCommand.size).joinToString(" ")
                    AutobotData.settings.defaultTitle = title
                    activeState.editAction = SettingsEditAction.None
                    AutobotData.settings.save()
                }
            }
            UiComponents.EditTargetingRange -> {
                if (activeState is SettingsViewState) {
                    val range = splitCommand[4].toInt()

                    if(range < 100 || range > 10000) {

                        activeChar.sendMessage("Value needs to be between 100 and 10000 yards")
                        return false
                    }
                    
                    AutobotData.settings.targetingRange = range
                    activeState.editAction = SettingsEditAction.None
                    AutobotData.settings.save()
                }
            }
        }
        return false
    }
    
    internal fun handleEditInfo(splitCommand: List<String>, activeChar: Player) {
        when (splitCommand[3]) {
            UiComponents.TargetRadius -> {
                (ViewStates.getActiveState(activeChar) as BotDetailsViewState?)?.combatEditAction = BotDetailsCombatEditAction.TargetRadius
            }
            UiComponents.KiteRadius -> {
                (ViewStates.getActiveState(activeChar) as BotDetailsViewState?)?.combatEditAction = BotDetailsCombatEditAction.KiteRadius
            }
            UiComponents.CreateBotName -> {
                (ViewStates.getActiveState(activeChar) as CreateBotViewState?)?.editAction = CreateBotEditAction.EditingName
            }
            UiComponents.CreateBotLevel -> {
                (ViewStates.getActiveState(activeChar) as CreateBotViewState?)?.editAction = CreateBotEditAction.EditingLevel
            }
            UiComponents.CreateBotWeaponEnch -> {
                (ViewStates.getActiveState(activeChar) as CreateBotViewState?)?.editAction = CreateBotEditAction.EditingWeaponEnchant
            }
            UiComponents.CreateBotArmorEnch -> {
                (ViewStates.getActiveState(activeChar) as CreateBotViewState?)?.editAction = CreateBotEditAction.EditingArmorEnchant
            }
            UiComponents.CreateBotJewelEnch -> {
                (ViewStates.getActiveState(activeChar) as CreateBotViewState?)?.editAction = CreateBotEditAction.EditingJewelsEnchant
            }
            UiComponents.EditUptime -> {
                (ViewStates.getActiveState(activeChar) as BotDetailsViewState?)?.activityEditAction = ActivityEditAction.EditUptime
            }
            UiComponents.PrivateSellMessage -> {
                (ViewStates.getActiveState(activeChar) as BotDetailsViewState?)?.socialEditAction = BotDetailsSocialEditAction.SellMessage
            }
            UiComponents.PrivateBuyMessage -> {
                (ViewStates.getActiveState(activeChar) as BotDetailsViewState?)?.socialEditAction = BotDetailsSocialEditAction.BuyMessage
            }
            UiComponents.PrivateCraftMessage -> {
                (ViewStates.getActiveState(activeChar) as BotDetailsViewState?)?.socialEditAction = BotDetailsSocialEditAction.CraftMessage
            }
            UiComponents.EditThinkIteration -> {
                (ViewStates.getActiveState(activeChar) as SettingsViewState?)?.editAction = SettingsEditAction.ThinkIteration
            }
            UiComponents.EditDefaultTitle -> {
                (ViewStates.getActiveState(activeChar) as SettingsViewState?)?.editAction = SettingsEditAction.DefaultTitle
            }
            UiComponents.EditTargetingRange -> {
                (ViewStates.getActiveState(activeChar) as SettingsViewState?)?.editAction = SettingsEditAction.TargetingRange
            }
        }
    }
}