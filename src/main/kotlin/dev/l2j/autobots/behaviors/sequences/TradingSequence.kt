package dev.l2j.autobots.behaviors.sequences

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.utils.CancellationToken
import kotlinx.coroutines.delay
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.ItemTable
import net.sf.l2j.gameserver.model.tradelist.TradeList
import java.util.concurrent.atomic.AtomicLong

// Experimental
internal class TradingSequence(override val bot: Autobot) : Sequence {

    override var cancellationToken: CancellationToken? = null

    private var usedQuestionsMark = false
    
    private var maxWaitMilliseconds = 60000
    
    private var waitedMilliseconds = AtomicLong(0)
    
    //TODO humanize this
    override suspend fun definition() {
        
        if(bot.socialBehavior.socialPreferences.tradingAction == null) {
            bot.cancelActiveTrade()
            return
        }

        ensureSufficientTradeItems()
        
        if (bot.hasActiveTrade()) {
            
            if(!usedQuestionsMark && bot.activeTradeList != null && bot.activeTradeList.items.isEmpty()) {
                delay(2000)
                bot.say("?")
                usedQuestionsMark = true
                delay(3000)
            }
            
            while (bot.activeTradeList != null && !requestedItemsAreSufficient(bot.activeTradeList.partner.activeTradeList)) {
                delay(1000)
                waitedMilliseconds.set(waitedMilliseconds.get() + 1000)
                
                if(waitedMilliseconds.get() >= maxWaitMilliseconds) {
                    bot.cancelActiveTrade()
                    delay(1000)
                    bot.say("bb")
                    return
                }
                
                addedWhatIOffer()
                
                if(bot.activeTradeList != null && bot.activeTradeList.partner.activeTradeList != null && !requestedItemsAreSufficient(bot.activeTradeList.partner.activeTradeList) && bot.activeTradeList.partner.activeTradeList.isConfirmed) {
                    delay(2000)
                    bot.say("sorry more")
                    delay(1000)
                    bot.cancelActiveTrade()
                }                
                
                
            }

            if(bot.activeTradeList != null) {
                delay(Rnd.get(2000, 4000).toLong())
                bot.activeTradeList.confirm()
            }            
        }
    }

    private suspend fun addedWhatIOffer(): Boolean {
        val itemsIOffer = bot.socialBehavior.socialPreferences.tradingAction!!.offersItems
        val itemsIWant = bot.socialBehavior.socialPreferences.tradingAction!!.looksForItems.random()
        itemsIOffer.forEach {itemIOffer ->
            val items = bot.activeTradeList?.items?.filter { it.item.itemId == itemIOffer.itemId } ?: return false
            if(items.isEmpty()) {
                val itemsToOffer = bot.inventory.getAllItemsByItemId(itemIOffer.itemId)

                if(itemsToOffer.isEmpty()) {
                    bot.addItem("bot trading", itemIOffer.itemId, itemIOffer.itemCount, null, false)
                }else {
                    itemsToOffer.forEach { itemCheckCount -> 
                        if(itemCheckCount.count < itemIOffer.itemCount) {
                            bot.addItem("bot trading", itemIOffer.itemId, itemIOffer.itemCount - itemCheckCount.count, null, false)
                        }
                    }
                }
                
                itemsToOffer.forEach { singleItem ->
                    
                    if(bot.activeTradeList == null) {
                        return false
                    }
                    
                    bot.addTradeItem(singleItem)
                    delay(1000)
                }
                delay(2000)
                val itemInstance = ItemTable.getInstance().getTemplate(itemsIWant.itemId) ?: return false
                bot.say("for ${itemInstance.name.toLowerCase()}")
            }
        }
        return true
    }

    private fun ensureSufficientTradeItems() {
        val looksForItems = bot.socialBehavior.socialPreferences.tradingAction?.offersItems ?: return
        looksForItems.forEach { 
            val item = bot.inventory.getItemByItemId(it.itemId)
            if(item == null) {
                bot.addItem("bot trading", it.itemId, it.itemCount, null, false)
            }else if(item.count < it.itemCount) {
                bot.addItem("bot trading", it.itemId, it.itemCount - item.count, null, false)
            }
        }
    }

    private fun requestedItemsAreSufficient(tradeList: TradeList): Boolean {
        val looksForItems = bot.socialBehavior.socialPreferences.tradingAction?.looksForItems ?: return false
        
        looksForItems.forEach { tradingItem -> 
            val itemInTradeList = tradeList.items.filter { it.item.itemId == tradingItem.itemId }
            
            if(itemInTradeList.isEmpty()) {
                return false
            }
            
            val count = itemInTradeList.sumBy { it.count }
            if(count < tradingItem.itemCount) {
                return false
            }
        }
        
        return true
    }
}