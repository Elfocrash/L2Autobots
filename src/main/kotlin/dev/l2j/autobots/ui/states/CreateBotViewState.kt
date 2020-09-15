package dev.l2j.autobots.ui.states

import dev.l2j.autobots.models.CreateBotDetails

internal data class CreateBotViewState (
        var botDetails: CreateBotDetails = default(),
        override var isActive: Boolean = false) : ViewState{

    var editAction: CreateBotEditAction = CreateBotEditAction.None

    override fun reset(){
        botDetails = default()
    }
    
    companion object{
        fun default(): CreateBotDetails{
            return CreateBotDetails()
        }    
    }
}

internal enum class CreateBotEditAction {
    None,
    EditingName,
    EditingLevel,
    EditingWeaponEnchant,
    EditingArmorEnchant,
    EditingJewelsEnchant
}