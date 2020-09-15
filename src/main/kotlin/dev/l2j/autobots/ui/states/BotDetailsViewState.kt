package dev.l2j.autobots.ui.states

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.ui.tabs.BotDetailsChatTab
import dev.l2j.autobots.ui.tabs.BotDetailsCombatEditAction
import dev.l2j.autobots.ui.tabs.BotDetailsSocialPage
import dev.l2j.autobots.ui.tabs.BotDetailsTab
import net.sf.l2j.gameserver.model.item.Recipe

internal data class BotDetailsViewState (
        var activeBot: Autobot,
        var activeTab: BotDetailsTab = BotDetailsTab.Info,
        var chatTab: BotDetailsChatTab = BotDetailsChatTab.All,
        var activityEditAction: ActivityEditAction = ActivityEditAction.None,
        var combatEditAction: BotDetailsCombatEditAction = BotDetailsCombatEditAction.None,
        var skillUnderEdit: Int = 0,
        var socialPage: BotDetailsSocialPage = BotDetailsSocialPage.Home,
        var sellList: MutableList<Triple<Int, Int, Int>> = mutableListOf(),
        var sellMessage: String = "",
        var buyList: MutableList<Triple<Int, Int, Int>> = mutableListOf(),
        var buyMessage: String = "",
        var craftList: MutableList<Pair<Recipe, Int>> = mutableListOf(),
        var craftMessage: String = "",
        var socialEditAction: BotDetailsSocialEditAction = BotDetailsSocialEditAction.None,
        override var isActive: Boolean = false) : ViewState{

    override fun reset(){
        //activeTab = BotDetailsTab.Info
        combatEditAction = BotDetailsCombatEditAction.None
        activityEditAction = ActivityEditAction.None
        socialPage = BotDetailsSocialPage.Home
        skillUnderEdit = 0
        sellList.clear()
        sellMessage = ""
        buyList.clear()
        buyMessage = ""
        craftList.clear()
        craftMessage = ""
    }
}

internal enum class ActivityEditAction{
    None,
    Uptime,
    EditUptime,
    Schedule
}

internal enum class BotDetailsSocialEditAction{
    None,
    SellMessage,
    BuyMessage,
    CraftMessage
}