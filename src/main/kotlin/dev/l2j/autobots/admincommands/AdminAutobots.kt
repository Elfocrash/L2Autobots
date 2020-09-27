package dev.l2j.autobots.admincommands

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.AutobotsManager
import dev.l2j.autobots.AutobotsManager.createAutobot
import dev.l2j.autobots.CoScopes
import dev.l2j.autobots.autofarm.AutofarmManager
import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.behaviors.sequences.TeleportToLocationSequence
import dev.l2j.autobots.dao.AutobotsDao
import dev.l2j.autobots.extensions.*
import dev.l2j.autobots.models.BotChat
import dev.l2j.autobots.models.BotDebugAction
import dev.l2j.autobots.models.ChatType
import dev.l2j.autobots.ui.AdminActions
import dev.l2j.autobots.ui.AdminUiActions
import dev.l2j.autobots.ui.AutobotsUi
import dev.l2j.autobots.ui.states.ActivityEditAction
import dev.l2j.autobots.ui.states.BotDetailsViewState
import dev.l2j.autobots.ui.states.CreateBotViewState
import dev.l2j.autobots.ui.states.ViewStates
import dev.l2j.autobots.ui.tabs.*
import dev.l2j.autobots.utils.*
import dev.l2j.autobots.utils.packets.GMViewBuffs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.sf.l2j.Config
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.ItemTable
import net.sf.l2j.gameserver.data.SkillTable
import net.sf.l2j.gameserver.data.cache.CrestCache
import net.sf.l2j.gameserver.data.sql.ClanTable
import net.sf.l2j.gameserver.data.xml.RecipeData
import net.sf.l2j.gameserver.enums.IntentionType
import net.sf.l2j.gameserver.handler.ChatHandler
import net.sf.l2j.gameserver.handler.IAdminCommandHandler
import net.sf.l2j.gameserver.model.World
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.location.Location
import net.sf.l2j.gameserver.network.SystemMessageId
import net.sf.l2j.gameserver.network.serverpackets.*
import net.sf.l2j.gameserver.taskmanager.PvpFlagTaskManager
import java.awt.Color

class AdminAutobots : IAdminCommandHandler {

    private val ADMIN_COMMANDS = arrayOf("admin_a")
    
    //TODO This class suck major pp and it's total spaghetti carbonara. I need to refactor this
    override fun useAdminCommand(command: String?, activeChar: Player): Boolean {
        val splitCommand = command!!.split(" ")
        when (splitCommand[1]){
            "random" -> {
                AdminActions.createAndSpawnRandomBots(splitCommand, activeChar)
            }
            "load" -> {
                val name = splitCommand[2]
                val onMe = splitCommand.getOrElse(3) {"false"}.toBoolean()
                val autobot = AutobotsDao.loadByName(name) ?: return false
                if(onMe) {
                    autobot.setXYZ(activeChar.x, activeChar.y, activeChar.z)
                }
                runBlocking {
                    AutobotsManager.spawnAutobot(autobot)
                }
            }            
            "count" ->{
                activeChar.sendMessage("Current bot count: ${AutobotsManager.activeBots.count()}")
            }
            "delete" -> {
                
                val radius = splitCommand.getOrNull(2)
                
                if(radius == null) {
                    AdminActions.despawnAutobot(activeChar.target as Autobot)
                    return true
                }

                AdminActions.despawnBotsInRadius(activeChar, radius)
            }
            "sm" -> {
                val mobName = splitCommand[2]
                val count = splitCommand.getOrElse(3) {"1"}.toInt()
                
                CoScopes.generalScope.launch {
                    for (i in 1..count) {
                        CoScopes.massSpawnerScope.launch {
                            spawn(activeChar, mobName)
                        }
                    }
                }
            }
            "debug" -> {
                when(splitCommand[2]){
                    "coffee" -> {
                        activeChar.sendMessage("Is this what you wanted?")
                    }
                    "trans" -> {
                    }
                    "tp" -> {
                        if(activeChar.target !is Autobot)
                            return true
                        
                        val x = splitCommand[3].toInt()
                        val y = splitCommand[4].toInt()
                        val z = splitCommand[5].toInt()
                        
                        GlobalScope.launch { TeleportToLocationSequence(activeChar.target as Autobot, Location(x, y, z)).execute() }
                    }
                    "trade" -> {
                        if(activeChar.target !is Autobot)
                            return true
                        
                        
                    }
                    "bot" -> {
                        when (splitCommand[3]) {
                            "save" -> {
                                AutofarmManager.savePreferences(activeChar)
                            }
                            "on" -> {
                                AutofarmManager.startFarm(activeChar)
                            }
                            "off" -> {
                                AutofarmManager.stopFarm(activeChar)
                            }
                            "buffs" -> {
                                if(AutofarmManager.combatBehaviors.containsKey(activeChar.objectId)) {
                                    AutofarmManager.combatBehaviors[activeChar.objectId]?.applyBuffs()
                                }
                            }
                        }
                    }
                    "z" -> {
                        when(splitCommand[3]){
                            "on" -> {
                                BotZoneService.player = activeChar
                            }
                            "off" -> {
                                BotZoneService.player = null
                            }
                            "clear" -> {
                                BotZoneService.graph.points.clear()
                                val packet = ExServerPrimitive(activeChar.name + "_", activeChar.enterWorldLocation.x, activeChar.enterWorldLocation.y, -65535)
                                packet.addPoint(Color.WHITE, 0, 0, 0)
                                activeChar.sendPacket(packet)
                            }
                        }
                    }
                }
            }
            "f" -> {
                PvpFlagTaskManager.getInstance().add(activeChar.target as Player?, Config.PVP_NORMAL_TIME.toLong())
            }
            "ar" -> {
                if(activeChar.target == null || (activeChar.target != null && activeChar.target !is Autobot)) return false
                
                val toggle = splitCommand[2] == "on"
                
                if((activeChar.target as Autobot).combatBehavior !is CombatBehavior) return false
                
                val autobotTarget = (activeChar.target as Autobot)
                
                if(!toggle) {
                    if(autobotTarget.hasDevAction(activeChar, BotDebugAction.VisualizeVision)) {
                        autobotTarget.removeDevAction(activeChar, BotDebugAction.VisualizeVision)
                    }                    
                    clearCircle(activeChar, "${activeChar.target.name}${BotDebugAction.VisualizeVision}")                    
                    return false
                }

                autobotTarget.addDevAction(activeChar, BotDebugAction.VisualizeVision) { player, bot ->
                    val packet = createCirclePacket("${bot.name}${BotDebugAction.VisualizeVision}", bot.x, bot.y, bot.z + 50, bot.combatBehavior.combatPreferences.targetingRadius, Color.BLUE,
                            player.enterWorldLocation.x, player.enterWorldLocation.y)
                    player.sendPacket(packet)
                }
            }
            "b" -> { // board related actions
                val next = splitCommand.getOrNull(2) ?: return returnFail(activeChar)

                when(next){
                    "reset" -> {
                        val state = splitCommand.getOrNull(3)
                        if(state.isNullOrEmpty()) {
                            ViewStates.indexViewState(activeChar).reset()
                        }
                        
                        when(state){
                            "index" -> ViewStates.indexViewState(activeChar).reset()
                        }
                    }
                    "navh" -> {
                        ViewStates.indexViewState(activeChar)
                    }
                    "sr"->{ //create and spawn random
                        val count = splitCommand.getOrElse(3) {"1"}.toInt()
                        AdminActions.createAndSpawnRandomAutobots(count, activeChar)
                    }
                    "sett" -> {
                        if(splitCommand.size == 3) {
                            ViewStates.settingsViewState(activeChar)
                        }
                    }
                    "s" -> { //search
                        ViewStates.indexViewState(activeChar).nameToSearch = splitCommand.getOrElse(5) {""}
                        ViewStates.indexViewState(activeChar).pagination = AdminActions.setCurrentPagination(splitCommand, 3, 4)
                    }
                    "ord" -> { //order by
                        when(splitCommand[3]){
                            "cl" -> ViewStates.indexViewState(activeChar).botOrdering = IndexBotOrdering.None
                            "lvlasc" -> ViewStates.indexViewState(activeChar).botOrdering = IndexBotOrdering.LevelAsc
                            "lvldesc" -> ViewStates.indexViewState(activeChar).botOrdering = IndexBotOrdering.LevelDesc
                            "onasc" -> ViewStates.indexViewState(activeChar).botOrdering = IndexBotOrdering.OnAsc
                            "ondesc" -> ViewStates.indexViewState(activeChar).botOrdering = IndexBotOrdering.OnDesc
                        }                        
                    }
                    "l" -> { //load and spawn
                        val name = splitCommand[3]
                        val autobot = AutobotsDao.loadByName(name) ?: return false
                        runBlocking {
                            AutobotsManager.spawnAutobot(autobot)
                        }                        
                    }
                    "ch"-> { //selected bot                        
                        val autobot = AutobotsManager.getBotInfoFromOnlineOrDb(splitCommand[3]) ?: return false
                        ViewStates.indexViewState(activeChar).selectedBots[autobot.name] = autobot
                    }
                    "uch" -> { //unselected bot
                        ViewStates.indexViewState(activeChar).selectedBots.remove(splitCommand[3])
                    }
                    "sk" -> {
                        val state = ViewStates.getActiveState(activeChar)
                        if(state is BotDetailsViewState) {
                            when(splitCommand[3]){
                                "a", "s" -> {
                                    if(splitCommand[8].toIntOrNull() == null) {
                                        activeChar.sendMessage("Value needs to be a number")                                        
                                        return returnFail(activeChar)
                                    }
                                    
                                    val targetCondition = TargetCondition.valueOf(splitCommand[4])
                                    val statusCondition = StatusCondition.valueOf(splitCommand[5])
                                    val comparisonCondition = ComparisonCondition.values().first { it.operator == splitCommand[6] }
                                    val conditionValueType = ConditionValueType.valueOf(splitCommand[7])                                    
                                    val value = splitCommand[8].toInt()

                                    if(statusCondition == StatusCondition.Distance && conditionValueType != ConditionValueType.Amount && targetCondition != TargetCondition.Target) {
                                        activeChar.sendMessage("Distance requires value type of \"Amount\" and target type of \"Target\"")
                                        return returnFail(activeChar)
                                    }

                                    if(statusCondition == StatusCondition.Level && conditionValueType != ConditionValueType.Amount) {
                                        activeChar.sendMessage("Level requires value type of \"Amount\"")
                                        return returnFail(activeChar)
                                    }

                                    if(conditionValueType == ConditionValueType.Percentage && value > 100) {
                                        activeChar.sendMessage("Percentage needs to be between 0-100")
                                        return returnFail(activeChar)
                                    }

                                    AdminActions.saveSkillUsageCondition(splitCommand, state, statusCondition, comparisonCondition, conditionValueType, targetCondition, value)
                                }
                                "t" -> {
                                    val skillId = splitCommand[4].toInt()
                                    state.activeBot.combatBehavior.skillPreferences.togglableSkills[skillId] = !state.activeBot.combatBehavior.skillPreferences.togglableSkills[skillId]!!
                                    AutobotsDao.saveSkillPreferences(state.activeBot)
                                }
                                "e" -> {
                                    state.skillUnderEdit = splitCommand[4].toInt()
                                }
                                "c" -> {
                                    state.skillUnderEdit = 0
                                }
                                "r" -> {
                                    AdminActions.removeSkillPreference(splitCommand, state)
                                }
                            }
                        }
                    }
                    "ctrl" -> { // control
                        
                        if(activeChar.target !is Autobot) {
                            activeChar.sendMessage("You can only control bots")
                            return false
                        }
                        
                        when(splitCommand[3]){
                            "on" -> activeChar.controlBot(activeChar.target as Autobot)
                            "off" -> activeChar.unControlBot(activeChar.target as Autobot)
                        }
                    }
                    "crtbot" -> {
                        val state = ViewStates.getActiveState(activeChar)
                        if(state is CreateBotViewState) {
                            val doubleWordClass = splitCommand[5] == "Male" || splitCommand[5] == "Female"
                            
                            state.botDetails.className = if(doubleWordClass) splitCommand[3] + " " + splitCommand[4] else splitCommand[3]
                            state.botDetails.genger = if(doubleWordClass) splitCommand[5] else splitCommand[4]
                            state.botDetails.hairStyle = if(doubleWordClass) splitCommand[6] + " " + splitCommand[7] else splitCommand[5] + " " + splitCommand[6]
                            state.botDetails.hairColor = if(doubleWordClass) splitCommand[8] + " " + splitCommand[9] else splitCommand[7] + " " + splitCommand[8]
                            state.botDetails.face = if(doubleWordClass) splitCommand[10] + " " + splitCommand[11] else splitCommand[9] + " " + splitCommand[10]

                            val bot = runBlocking {
                                    createAutobot(activeChar, state.botDetails.name, state.botDetails.level, state.botDetails.getClassId(), state.botDetails.getAppearance(), activeChar.x, activeChar.y, activeChar.z,
                                            state.botDetails.weaponEnchant, state.botDetails.armorEnchant, state.botDetails.jewelEnchant)
                            }
                            
                            if(bot != null) {
                                ViewStates.indexViewState(activeChar).reset()
                            }
                        }
                    }
                    "visar" -> {
                        val name = splitCommand[3]
                        val bot = AutobotsManager.activeBots.getOrDefault(name, null) ?: return false

                        if(bot.hasDevAction(activeChar, BotDebugAction.VisualizeVision)) {
                            bot.removeDevAction(activeChar, BotDebugAction.VisualizeVision)
                            clearCircle(activeChar, "${bot.name}${BotDebugAction.VisualizeVision}")
                            return false
                        }

                        bot.addDevAction(activeChar, BotDebugAction.VisualizeVision) { player, autobot -> 
                            val packet = createCirclePacket("${autobot.name}${BotDebugAction.VisualizeVision}", autobot.x, autobot.y, autobot.z + 50, autobot.combatBehavior.combatPreferences.targetingRadius, Color.BLUE,
                                    player.enterWorldLocation.x, player.enterWorldLocation.y)
                            player.sendPacket(packet)
                        }
                    }
                    "rndbot" -> {
                        val state = ViewStates.getActiveState(activeChar)
                        if(state is CreateBotViewState) {
                            state.botDetails.randomize()
                        }
                    }
                    "lm" -> { //load and spawn by name on me
                        val name = splitCommand[3]
                        val autobot = AutobotsDao.loadByName(name) ?: return false
                        
                        if(autobot.combatBehavior.activityPreferences.activityType == ActivityType.Schedule) {
                            autobot.combatBehavior.activityPreferences.activityType = ActivityType.None
                        }
                        
                        autobot.setXYZ(activeChar.x + Rnd.get(-100, 100), activeChar.y + Rnd.get(-100, 100), activeChar.z)
                        runBlocking {
                            AutobotsManager.spawnAutobot(autobot)
                        }
                    }
                    "ls"-> { //load and spawn selected
                        CoScopes.generalScope.launch {
                                ViewStates.indexViewState(activeChar).selectedBots.forEach {
                                    innerLaunch@CoScopes.massSpawnerScope.launch {
                                        val autobot = AutobotsDao.loadByName(it.value.name) ?: return@innerLaunch
                                        if (autobot.isOnline) return@innerLaunch

                                        AutobotsManager.spawnAutobot(autobot)
                                    }
                                }
                            }
                        ViewStates.indexViewState(activeChar).selectedBots.clear()
                    }
                    "clf" -> { //clear filter
                        ViewStates.indexViewState(activeChar).nameToSearch = ""
                        ViewStates.indexViewState(activeChar).pagination = Pair(1,10)
                    }
                    "edb" -> {
                        val bot = AutobotsManager.getBotFromOnlineOrDb(splitCommand[4])!!
                        AutobotsUi.loadLastActive(activeChar)
                        when(splitCommand[3]){
                            "st" -> {
                                activeChar.sendPacket(GMViewCharacterInfo(bot))
                                activeChar.sendPacket(GMViewHennaInfo(bot))
                                return true
                            }
                            "in" -> {
                                activeChar.sendPacket(GMViewItemList(bot))
                                activeChar.sendPacket(GMViewHennaInfo(bot))
                                return true
                            }
                            "sk" -> {
                                activeChar.sendPacket(GMViewSkillInfo(bot))
                                return true
                            }
                            "bf" -> {
                                activeChar.sendPacket(GMViewBuffs(bot))
                                return true
                            }
                            "rc" -> {
                                if(!bot.isIngame()) {
                                    activeChar.sendMessage("You cannot recall a bot that is offline")
                                    return true
                                }
                                bot.ai.setIntention(IntentionType.IDLE)
                                bot.teleportTo(activeChar.x, activeChar.y, activeChar.z, 0)
                                return true
                            }
                            "gt" -> {
                                if(!bot.isIngame()) {
                                    activeChar.sendMessage("You cannot goto a bot that is offline")
                                    return true
                                }
                                activeChar.ai.setIntention(IntentionType.IDLE)
                                activeChar.teleportTo(bot.x, bot.y, bot.z, 0)
                                return true
                            }
                            "un" -> {
                                if(!bot.isIngame()) {
                                    activeChar.sendMessage("Cannot unstuck a bot that is offline")
                                    return true
                                }
                                
                                bot.doCast(SkillTable.getInstance().getInfo(2099, 1))
                                return true
                            }
                            "dl" -> {
                                if(AutobotsManager.deleteAutobot(bot) {activeChar.sendMessage("Cannot delete a bot that is clan leader")}) {
                                    ViewStates.indexViewState(activeChar).reset()
                                    AutobotsUi.loadLastActive(activeChar)
                                }                                
                                return true
                            }
                        }                        
                    }
                    "ed" -> { //edit info
                        AdminUiActions.handleEditInfo(splitCommand, activeChar)
                    }
                    "sv" -> { //save info
                        if (AdminUiActions.handleSaveInfo(activeChar, splitCommand)) 
                            return false
                        
                        return returnFail(activeChar)
                    }
                    "slcs" -> {
                        AdminUiActions.selectRace(splitCommand, activeChar)
                    }
                    "t" -> { // tabs
                        when (splitCommand[3]) {
                            "g" -> { // general index tab
                                ViewStates.indexViewState(activeChar).indexTab = IndexTab.General
                            }
                            "c" -> { // clan index tab
                                ViewStates.indexViewState(activeChar).indexTab = IndexTab.Clan
                            }
                            "bi" -> { // bot details info tab
                                AdminActions.selectBotDetailsTab(activeChar, BotDetailsTab.Info)
                            }
                            "bc" -> { // bot details combat tab
                                AdminActions.selectBotDetailsTab(activeChar, BotDetailsTab.Combat)
                            }
                            "bs","bsh" -> { // bot details social tab
                                val state = ViewStates.getActiveState(activeChar)
                                if (state is BotDetailsViewState) {
                                    if(splitCommand.size == 4) {
                                        state.activeTab = BotDetailsTab.Social
                                        if(splitCommand[3] == "bsh") {
                                            state.socialPage = BotDetailsSocialPage.Home
                                        }
                                    }else {
                                        when(splitCommand[4]){
                                            "sell" -> {
                                                state.socialPage = BotDetailsSocialPage.CreateSellStore
                                            }
                                            "buy" -> {
                                                state.socialPage = BotDetailsSocialPage.CreateBuyStore
                                            }
                                            "craft" -> {
                                                state.socialPage = BotDetailsSocialPage.CreateCraftStore
                                            }
                                            "stop" -> {
                                                if(state.activeBot.isIngame()) {
                                                    state.activeBot.forceStandUp()
                                                }
                                            }
                                            "a" -> {
                                                when(splitCommand[5]) {
                                                    "st" -> { //stand toggle
                                                        val bot = state.activeBot
                                                        if(!bot.isIngame()) {
                                                            activeChar.sendMessage("Bot is not online")
                                                            AutobotsUi.loadLastActive(activeChar)
                                                            return false
                                                        }

                                                        AdminActions.toggleSitting(bot)
                                                    }
                                                    "ru" -> { //run toggle
                                                        val bot = state.activeBot
                                                        if(!bot.isIngame()) {
                                                            activeChar.sendMessage("Bot is not online")
                                                            AutobotsUi.loadLastActive(activeChar)
                                                            return false
                                                        }
                                                        bot.setIsRunning(!bot.isRunning)
                                                    }
                                                    "cont" -> { //control
                                                        val bot = state.activeBot
                                                        if(!bot.isIngame()) {
                                                            activeChar.sendMessage("Bot is not online")
                                                            AutobotsUi.loadLastActive(activeChar)
                                                            return false
                                                        }
                                                        
                                                        if(distance(activeChar, bot) >= 2000) {
                                                            activeChar.sendMessage("You need to be close to the bot to control it")
                                                            AutobotsUi.loadLastActive(activeChar)
                                                            return false
                                                        }

                                                        if(activeChar.isControllingBot()) {
                                                            activeChar.unControlBot(activeChar.getControllingBot()!!)
                                                        }
                                                        
                                                        activeChar.target = bot
                                                        activeChar.controlBot(bot)
                                                    }
                                                    "uncont" -> { //uncontrol
                                                        if(activeChar.isControllingBot()) {
                                                            activeChar.unControlBot(activeChar.getControllingBot()!!)
                                                        }
                                                    }
                                                }
                                            }
                                            "s" -> {
                                                when(splitCommand[5]){
                                                    "a" -> {
                                                        val itemId = splitCommand[6].toIntOrNull() ?: return false
                                                        val itemcount = splitCommand[7].toIntOrNull() ?: return false
                                                        val priceperitem = splitCommand[8].toIntOrNull() ?: return false
                                                        
                                                        val item = ItemTable.getInstance().getTemplate(itemId)
                                                        if(item == null) {
                                                            activeChar.sendMessage("There is no item with that id")
                                                            return false
                                                        }
                                                        
                                                        if(!item.isStackable && itemcount > 1) {
                                                            activeChar.sendMessage("Cannot add more than 1 count for non stackable items")
                                                            return false
                                                        }
                                                        
                                                        if(state.sellList.size > state.activeBot.privateSellStoreLimit) {
                                                            activeChar.sendMessage("You cannot have more than ${state.activeBot.privateSellStoreLimit} items")
                                                            return false
                                                        }
                                                        
                                                        state.sellList.add(Triple(itemId, itemcount, priceperitem))
                                                    }
                                                    "r" -> {
                                                        val index = splitCommand[6].toInt()
                                                        state.sellList.removeAt(index)
                                                    }
                                                    "c" -> {
                                                        val bot = state.activeBot
                                                        
                                                        if(bot.isIngame()) {
                                                            bot.createPrivateSellStore(state.sellList, state.sellMessage, creator = activeChar)
                                                        }else {
                                                            bot.setXYZ(activeChar.x, activeChar.y, activeChar.z)
                                                            runBlocking {
                                                                AutobotsManager.spawnAutobot(bot)
                                                            }
                                                            bot.createPrivateSellStore(state.sellList, state.sellMessage, creator = activeChar)
                                                        }
                                                    }
                                                }
                                            }
                                            "b" -> {
                                                when(splitCommand[5]){
                                                    "a" -> {
                                                        val itemId = splitCommand[6].toIntOrNull() ?: return false
                                                        val itemcount = splitCommand[7].toIntOrNull() ?: return false
                                                        val priceperitem = splitCommand[8].toIntOrNull() ?: return false

                                                        val item = ItemTable.getInstance().getTemplate(itemId)
                                                        if(item == null) {
                                                            activeChar.sendMessage("There is no item with that id")
                                                            return false
                                                        }

                                                        if(!item.isStackable && itemcount > 1) {
                                                            activeChar.sendMessage("Cannot add more than 1 count for non stackable items")
                                                            return false
                                                        }

                                                        if(state.buyList.size > state.activeBot.privateBuyStoreLimit) {
                                                            activeChar.sendMessage("You cannot have more than ${state.activeBot.privateBuyStoreLimit} items")
                                                            return false
                                                        }

                                                        state.buyList.add(Triple(itemId, itemcount, priceperitem))
                                                    }
                                                    "r" -> {
                                                        val index = splitCommand[6].toInt()
                                                        state.buyList.removeAt(index)
                                                    }
                                                    "c" -> {
                                                        val bot = state.activeBot

                                                        if(bot.isIngame()) {
                                                            bot.createPrivateBuyStore(state.buyList, state.buyMessage, activeChar)
                                                        }else {
                                                            bot.setXYZ(activeChar.x, activeChar.y, activeChar.z)
                                                            runBlocking {
                                                                AutobotsManager.spawnAutobot(bot)
                                                            }
                                                            bot.createPrivateBuyStore(state.buyList, state.buyMessage, activeChar)
                                                        }
                                                    }
                                                }
                                            }
                                            "c" -> {
                                                when(splitCommand[5]){
                                                    "a" -> {
                                                        val idType = splitCommand[6]
                                                        val id = splitCommand[7].toIntOrNull() ?: return false
                                                        val cost = splitCommand[8].toIntOrNull() ?: return false

                                                        val recipe = if(idType == "RecipeId") RecipeData.getInstance().getRecipeList(id) else RecipeData.getInstance().getRecipeByItemId(id)
                                                        
                                                        if(recipe == null) {
                                                            activeChar.sendMessage("There is no such recipe")
                                                            return returnFail(activeChar)
                                                        }
                                                        
                                                        if(!recipe.isDwarven && !state.activeBot.hasCommonCraft()) {
                                                            activeChar.sendMessage("This recipe can only be used on Common craft")
                                                            return returnFail(activeChar)
                                                        }
                                                        
                                                        if(recipe.isDwarven && !state.activeBot.hasDwarvenCraft()) {
                                                            activeChar.sendMessage("This recipe can only be used on Dwarven craft")
                                                            return returnFail(activeChar)
                                                        }

                                                        val isDwarven = state.craftList.any { it.first.isDwarven }

                                                        if(isDwarven && !recipe.isDwarven) {
                                                            activeChar.sendMessage("Cannot mix common recipes with dwarven ones")
                                                            return returnFail(activeChar)
                                                        }

                                                        if(!isDwarven && recipe.isDwarven) {
                                                            activeChar.sendMessage("Cannot add common recipes with dwarven ones")
                                                            return returnFail(activeChar)
                                                        }
                                                        
                                                        if(state.craftList.filter { it.first.isDwarven }.size >= state.activeBot.dwarfRecipeLimit) {
                                                            activeChar.sendMessage("Dwarven craft limit reached")
                                                            return returnFail(activeChar)
                                                        }

                                                        if(state.craftList.filter { !it.first.isDwarven }.size >= state.activeBot.commonRecipeLimit) {
                                                            activeChar.sendMessage("Common craft limit reached")
                                                            return returnFail(activeChar)
                                                        }

                                                        state.craftList.add(Pair(recipe, cost))
                                                    }
                                                    "r" -> {
                                                        val index = splitCommand[6].toInt()
                                                        state.craftList.removeAt(index)
                                                    }
                                                    "c" -> {
                                                        val bot = state.activeBot

                                                        if(bot.isIngame()) {
                                                            bot.createPrivateCraftStore(state.craftList, state.craftMessage, activeChar)
                                                        }else {
                                                            bot.setXYZ(activeChar.x, activeChar.y, activeChar.z)
                                                            runBlocking {
                                                                AutobotsManager.spawnAutobot(bot)
                                                            }
                                                            bot.createPrivateCraftStore(state.craftList, state.craftMessage, activeChar)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            "bsk" -> { // bot details skills tab
                                AdminActions.selectBotDetailsTab(activeChar, BotDetailsTab.Skills)
                            }
                            "bch" -> { // bot details chat tab
                                AdminActions.selectBotDetailsTab(activeChar, BotDetailsTab.Chat)
                            }
                            "chall" -> { // bot details chat all tab
                                val state = ViewStates.getActiveState(activeChar)
                                if (state is BotDetailsViewState) {
                                    state.activeTab = BotDetailsTab.Chat
                                    state.chatTab = BotDetailsChatTab.All
                                }
                            }
                            "chapm" -> { // bot details chat pm tab
                                val state = ViewStates.getActiveState(activeChar)
                                if (state is BotDetailsViewState) {
                                    state.activeTab = BotDetailsTab.Chat
                                    state.chatTab = BotDetailsChatTab.Pms
                                }
                            }
                        }
                    }
                    "c" -> {
                        val activeState = ViewStates.getActiveState(activeChar)
                        when(splitCommand[3]){
                            "ban" -> {
                                if(activeState is BotDetailsViewState && activeState.activeTab == BotDetailsTab.Info){
                                    activeState.activityEditAction = ActivityEditAction.None
                                }
                            }
                            "bau" -> {
                                if(activeState is BotDetailsViewState && activeState.activeTab == BotDetailsTab.Info){
                                    activeState.activityEditAction = ActivityEditAction.Uptime
                                }
                            }
                            "bas" -> {
                                if(activeState is BotDetailsViewState && activeState.activeTab == BotDetailsTab.Info){
                                    activeState.activityEditAction = ActivityEditAction.Schedule
                                }
                            }
                        }
                    }
                    "lsme"-> { //load and spawn selected on me
                        val bots = ViewStates.indexViewState(activeChar).selectedBots.filter { !it.value.isOnline }

                        CoScopes.generalScope.launch {
                            bots.forEach {
                                innerLaunch@CoScopes.massSpawnerScope.launch {
                                    val autobot = AutobotsDao.loadByName(it.value.name) ?: return@innerLaunch

                                    autobot.setXYZ(activeChar.x + Rnd.get(-150, 150), activeChar.y + Rnd.get(-150, 150), activeChar.z)
                                    AutobotsManager.spawnAutobot(autobot)
                                }
                            }
                        }
                        ViewStates.indexViewState(activeChar).selectedBots.clear()
                    }
                    "dess" -> { //despawn selected

                        val bots = ViewStates.indexViewState(activeChar).selectedBots.filter { it.value.isOnline }

                        CoScopes.generalScope.launch {
                            bots.forEach {
                                innerLaunch@CoScopes.massDespawnerScope.launch {
                                    val autobot = AutobotsManager.activeBots.getOrDefault(it.value.name, null) ?: return@innerLaunch
                                    AdminActions.despawnAutobot(autobot)
                                }
                            }
                        }
                        ViewStates.indexViewState(activeChar).selectedBots.clear()
                    }
                    "chall" -> { // select all bots
                        AutobotsDao.getAllInfo().forEach { ViewStates.indexViewState(activeChar).selectedBots[it.name] = it }                        
                    }
                    "cls" -> { //clear selected
                        ViewStates.indexViewState(activeChar).selectedBots.clear()
                    }
                    "des"->{ //despawn by name
                        val name = splitCommand[3]
                        val bot = AutobotsManager.activeBots.getOrElse(name, {null}) ?: return false
                        AdminActions.despawnAutobot(bot)
                    }
                    "det"->{ //despawn target
                        if(activeChar.target == null || activeChar.target !is Autobot) return false
                        AdminActions.despawnAutobot(activeChar.target as Autobot)
                    }
                    "der"->{ //despawn in radius
                        val radius = splitCommand.getOrNull(3) ?: return false
                        AdminActions.despawnBotsInRadius(activeChar, radius)
                    }
                    "eb" -> { //edit bot
                        val name = splitCommand[3]
                        val bot = AutobotsManager.getBotFromOnlineOrDb(name) ?: return false
                        
                        AutobotsUi.loadBotDetails(activeChar, bot)
                        return true
                    }
                    "crb" -> { //create bot
                        ViewStates.createBotViewState(activeChar).reset()
                        AutobotsUi.loadCreateBot(activeChar)
                        return true
                    }
                    "csend" -> { // chat send
                        val botName = splitCommand[3]
                        val autobot = AutobotsManager.activeBots.getOrElse(botName) {null} ?: return false
                        var message = splitCommand.subList(4, splitCommand.size).joinToString(" ") ?: return false
                        var chatType = 0
                        var target: String? = null
                        message = message.replace("\r\n", " ").replace("\\\\n".toRegex(), "")
                        
                        if(message.isEmpty()) {
                            activeChar.sendMessage("You have to say something")
                            return false
                        }

                        when {
                            message.startsWith('!') -> { // shout chat
                                chatType = 1
                                message = message.trimStart('!')
                                autobot.addChat(BotChat(ChatType.Shout, autobot.name, message))
                            }
                            message.startsWith('+') -> { // shout chat
                                chatType = 8
                                message = message.trimStart('+')
                                autobot.addChat(BotChat(ChatType.Trade, autobot.name, message))
                            }
                            message.startsWith('"') -> { // whisper chat
                                chatType = 2
                                message = message.trimStart('"')
                                val splitOnSpace = message.split(' ')

                                if(splitOnSpace.isEmpty() || splitOnSpace.size == 1) {
                                    activeChar.sendMessage("You have to say something")
                                    return returnFail(activeChar)
                                }

                                target = splitOnSpace[0]

                                if(World.getInstance().getPlayer(target) == null) {
                                    activeChar.sendMessage("Target player is not online")
                                    return returnFail(activeChar)
                                }

                                message = splitOnSpace.drop(1).joinToString(" ")
                                autobot.addChat(BotChat(ChatType.PmSent, autobot.name, message))
                            }
                            message.startsWith("/") ->{ // command
                                activeChar.sendMessage("Commands are not supported (yet)")
                                return returnFail(activeChar)
                            }
                            else -> {
                                autobot.addChat(BotChat(ChatType.All, autobot.name, message))
                            }
                        }
                        
                        val handler = ChatHandler.getInstance().getHandler(chatType)
                        handler.handleChat(chatType, autobot, target, message)
                    }
                    "cln" -> { //clan functions
                        when(splitCommand[3]){
                            "rmc" -> { //remove clan
                                val clanName = splitCommand.getOrNull(4)
                                if(clanName.isNullOrEmpty()) {
                                    activeChar.sendMessage("There is no clan with that name.")
                                    return returnFail(activeChar)
                                }
                                val clan = ClanTable.getInstance().clans.firstOrNull { it.name == clanName }
                                
                                if(clan == null) {
                                    activeChar.sendMessage("There is no clan with that name.")
                                    return returnFail(activeChar)
                                }
                                
                                if (clan.allyId != 0) {
                                    activeChar.sendMessage("You cannot delete a clan in an ally. Delete the ally first.")
                                    return returnFail(activeChar)
                                }

                                ClanTable.getInstance().destroyClan(clan)
                            }
                            "rmm" -> { //remove selected from clan
                                AdminActions.removeSelectedBotsFromClan(activeChar)
                            }
                        }
                    }
                    "clc" -> { //clan create
                        if(ViewStates.indexViewState(activeChar).selectedBots.isEmpty()) {
                            activeChar.sendMessage("You need to select at least one bot")
                            return true
                        }
                        
                        val clanName = splitCommand[3]
                        val clanLevel = splitCommand.getOrElse(4) { "1"}.toInt()
                        val clanLeaderName = splitCommand[5]                        
                        val crestUrl = splitCommand.getOrElse(6) {""}
                        
                        if(clanName.isEmpty()) {
                            activeChar.sendMessage("You need a clan name to create a clan.")
                            return returnFail(activeChar)
                        }
                        
                        CoScopes.generalScope.launch {
                            val leaderBot = AutobotsManager.getBotFromOnlineOrDb(clanLeaderName) ?: return@launch
                            leaderBot.clanCreateExpiryTime = 0
                            val clan = ClanTable.getInstance().createClan(leaderBot, clanName)
                            if (clan != null){
                                if(!leaderBot.isOnline)
                                    AutobotsDao.saveAutobot(leaderBot)
                                
                                if(clanLevel != 0) {
                                    clan.changeLevel(clanLevel)
                                }

                                if(clanLevel >= 3 && crestUrl.isNotEmpty()) {
                                    val crestId = uploadCrest(crestUrl, CrestCache.CrestType.PLEDGE)
                                    clan.changeClanCrest(crestId)
                                }

                                activeChar.sendMessage("Clan $clanName have been created. Clan leader is ${leaderBot.name}.")
                            } else {
                                activeChar.sendMessage("There was a problem while creating the clan.")
                                return@launch
                            }

                            ViewStates.indexViewState(activeChar).selectedBots.values.filter { it.name != clanLeaderName }.forEach {
                                val bot = AutobotsManager.getBotFromOnlineOrDb(it.name) ?: return@forEach
                                clan.addClanMember(bot)
                                bot.clanPrivileges = clan.getPriviledgesByRank(bot.powerGrade)
                                clan.broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addCharName(activeChar), activeChar)
                                clan.broadcastToOtherOnlineMembers(PledgeShowMemberListAdd(activeChar), activeChar)
                                clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan))
                                bot.clanJoinExpiryTime = 0
                                if(bot.isOnline) {
                                    bot.broadcastUserInfo()
                                }else {
                                    AutobotsDao.saveAutobot(bot)
                                }                                
                            }
                        }                        
                    }
                    "todo" -> {
                        activeChar.sendMessage("Not Implemented yet")
                    }
                    else -> AutobotsUi.loadLastActive(activeChar)
                }
                AutobotsUi.loadLastActive(activeChar)
            }
        }
        return true
    }

    private fun returnFail(player: Player) : Boolean{
        AutobotsUi.loadLastActive(player)
        return false
    }

    override fun getAdminCommandList(): Array<String> {
        return ADMIN_COMMANDS
    }
}