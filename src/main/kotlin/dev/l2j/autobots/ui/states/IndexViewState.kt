package dev.l2j.autobots.ui.states

import dev.l2j.autobots.models.AutobotInfo
import dev.l2j.autobots.ui.tabs.IndexBotOrdering
import dev.l2j.autobots.ui.tabs.IndexTab

internal data class IndexViewState(
        var nameToSearch: String = "",
        var pagination: Pair<Int, Int> = Pair(1, 10),
        val selectedBots: MutableMap<String, AutobotInfo> = mutableMapOf(),
        var indexTab: IndexTab = IndexTab.General,
        var botOrdering: IndexBotOrdering = IndexBotOrdering.None,
        override var isActive: Boolean = true) : ViewState{
    override fun reset(){
        nameToSearch = ""
        pagination = Pair(1, 10)
        selectedBots.clear()
        indexTab = IndexTab.General
        botOrdering = IndexBotOrdering.None
    }
}

