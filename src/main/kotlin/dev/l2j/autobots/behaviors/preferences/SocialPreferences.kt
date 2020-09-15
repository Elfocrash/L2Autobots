package dev.l2j.autobots.behaviors.preferences

import com.fasterxml.jackson.annotation.JsonProperty

internal open class SocialPreferences(
        @JsonProperty("townAction")var townAction: TownAction = TownAction.None,
        @JsonProperty("tradingAction")var tradingAction: TradingAction? = null 
)

internal enum class TownAction {
    None,
    TeleToRandomLocation,
    TeleToSpecificLocation,
    Trade
}

internal data class TradingAction(
        @JsonProperty("looksFor")var looksForItems: MutableList<TradingItem>,
        @JsonProperty("offers")var offersItems: MutableList<TradingItem>
)

internal data class TradingItem(
        @JsonProperty("itemId")var itemId: Int,
        @JsonProperty("itemCount")var itemCount: Int
)