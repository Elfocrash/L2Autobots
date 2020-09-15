package dev.l2j.autobots.ui.states

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.behaviors.preferences.ActivityType
import net.sf.l2j.gameserver.model.actor.Player

internal object ViewStates {
    private val states = HashMap<Int, HashMap<String, ViewState?>>()
    
    fun indexViewState(player: Player): IndexViewState{
        ensureStateExists(player, "IndexViewState") {IndexViewState()}
        deactivateStates(player, "IndexViewState")
        
        val state = states[player.objectId]!!["IndexViewState"]!! as IndexViewState
        state.isActive = true
        states[player.objectId]!!["IndexViewState"] = state
        return state
    }

    fun botDetailsViewState(player: Player, bot: Autobot): BotDetailsViewState{
        ensureStateExists(player, "BotDetailsViewState") {
            BotDetailsViewState(bot, isActive = true,
                activityEditAction = when(bot.combatBehavior.activityPreferences.activityType){
                    ActivityType.None -> ActivityEditAction.None
                    ActivityType.Uptime -> ActivityEditAction.Uptime
                    ActivityType.Schedule -> ActivityEditAction.Schedule
                })}
        deactivateStates(player, "BotDetailsViewState")

        val state = states[player.objectId]!!["BotDetailsViewState"] as BotDetailsViewState? ?: BotDetailsViewState(bot, isActive = true, activityEditAction = when(bot.combatBehavior.activityPreferences.activityType){
            ActivityType.None -> ActivityEditAction.None
            ActivityType.Uptime -> ActivityEditAction.Uptime
            ActivityType.Schedule -> ActivityEditAction.Schedule
            else -> ActivityEditAction.None
        })
        state.isActive = true
        if(state.activeBot.name != bot.name) {
            state.reset()
            state.activeBot = bot
            state.activityEditAction = when(bot.combatBehavior.activityPreferences.activityType){
            ActivityType.None -> ActivityEditAction.None
            ActivityType.Uptime -> ActivityEditAction.Uptime
            ActivityType.Schedule -> ActivityEditAction.Schedule
            else -> ActivityEditAction.None
        }
        }else {
            state.activeBot = bot
        }

        states[player.objectId]!!["BotDetailsViewState"] = state
        return state
    }

    fun createBotViewState(player: Player): CreateBotViewState{
        ensureStateExists(player, "CreateBotViewState") {CreateBotViewState(isActive = true)}
        deactivateStates(player, "CreateBotViewState")
        
        val state = states[player.objectId]!!["CreateBotViewState"] as CreateBotViewState? ?: CreateBotViewState(isActive = true)
        if(!state.isActive) {
            state.reset()
        }
        
        state.isActive = true        
        states[player.objectId]!!["CreateBotViewState"] = state
        return state
    }
    
    fun settingsViewState(player: Player) : SettingsViewState{
        ensureStateExists(player, "SettingsViewState") {SettingsViewState(isActive = true)}
        deactivateStates(player, "SettingsViewState")
        val state = states[player.objectId]!!["SettingsViewState"] as SettingsViewState? ?: SettingsViewState(isActive = true)
        state.isActive = true
        states[player.objectId]!!["SettingsViewState"] = state
        return state
    }
    
    fun getActiveState(player: Player) : ViewState {
        if(!states.containsKey(player.objectId)) {
            states[player.objectId] = hashMapOf<String, ViewState?>(
                    Pair("IndexViewState", IndexViewState()),
                    Pair("BotDetailsViewState", null),
                    Pair("CreateBotViewState", CreateBotViewState()),
                    Pair("SettingsViewState", SettingsViewState())
            )
        }
        
        return states[player.objectId]!!.values.firstOrNull { it != null && it.isActive } ?: indexViewState(player)
    }
    
    private fun ensureStateExists(player: Player, name: String, item: () -> ViewState){
        if(!states.containsKey(player.objectId)) {
            states[player.objectId] = hashMapOf<String, ViewState?>()
        }

        if(!states[player.objectId]!!.containsKey(name)) {
            states[player.objectId]!![name] = item()
        }
    }
    
    private fun deactivateStates(player: Player, exceptForName: String){
        if(!states.containsKey(player.objectId)) {
            states[player.objectId] = hashMapOf<String, ViewState?>()
        }

        states[player.objectId]!!.filter{entry -> entry.value != null && entry.key != exceptForName }.forEach { it.value?.isActive = false }
    }
}