package dev.l2j.autobots.ui.states

internal data class SettingsViewState(
        var activeTab: SettingsTab = SettingsTab.Home,
        var editAction: SettingsEditAction = SettingsEditAction.None,
        override var isActive: Boolean = true) : ViewState{
    override fun reset(){
    }
}

internal enum class SettingsEditAction{
    None,
    ThinkIteration,
    DefaultTitle,
    TargetingRange
}

internal enum class SettingsTab{
    Home,
    Combat
}