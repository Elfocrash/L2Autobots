package dev.l2j.autobots.extensions

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.AutobotsManager
import dev.l2j.autobots.autofarm.AutofarmManager
import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.SocialBehavior
import dev.l2j.autobots.behaviors.sequences.Sequence
import dev.l2j.autobots.utils.distance
import dev.l2j.autobots.utils.getBehaviorByClassId
import dev.l2j.autobots.utils.supportedCombatPrefs
import net.sf.l2j.Config
import net.sf.l2j.commons.concurrent.ThreadPool
import net.sf.l2j.commons.math.MathUtil
import net.sf.l2j.gameserver.enums.AiEventType
import net.sf.l2j.gameserver.enums.IntentionType
import net.sf.l2j.gameserver.enums.ZoneId
import net.sf.l2j.gameserver.enums.actors.ClassId
import net.sf.l2j.gameserver.enums.actors.StoreType
import net.sf.l2j.gameserver.enums.items.ActionType
import net.sf.l2j.gameserver.enums.items.EtcItemType
import net.sf.l2j.gameserver.enums.items.WeaponType
import net.sf.l2j.gameserver.enums.skills.L2SkillType
import net.sf.l2j.gameserver.geoengine.GeoEngine
import net.sf.l2j.gameserver.handler.ItemHandler
import net.sf.l2j.gameserver.model.L2Skill
import net.sf.l2j.gameserver.model.World
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.ai.NextAction
import net.sf.l2j.gameserver.model.actor.instance.Gatekeeper
import net.sf.l2j.gameserver.model.actor.instance.Pet
import net.sf.l2j.gameserver.model.craft.ManufactureItem
import net.sf.l2j.gameserver.model.craft.ManufactureList
import net.sf.l2j.gameserver.model.item.Recipe
import net.sf.l2j.gameserver.model.item.kind.Item
import net.sf.l2j.gameserver.model.itemcontainer.Inventory
import net.sf.l2j.gameserver.model.location.Location
import net.sf.l2j.gameserver.model.tradelist.TradeList
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgBuy
import net.sf.l2j.gameserver.network.serverpackets.PrivateStoreMsgSell
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopMsg
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager

internal fun Autobot.createPrivateSellStore(items: List<Triple<Int, Int, Int>>, message: String = "", isPackageSale: Boolean = false, creator: Player? = null){
    if (items.isEmpty()) {
        storeType = StoreType.NONE
        broadcastUserInfo()
        creator?.sendMessage("Cannot create store without items")
        return
    }

    if (!accessLevel.allowTransaction()) {
        creator?.sendMessage("Access level does not allow transaction")
        return
    }

    if (AttackStanceTaskManager.getInstance().isInAttackStance(this) || isCastingNow || isCastingSimultaneouslyNow || isInDuel) {
        creator?.sendMessage("Cannot create store in combat")
        return
    }

    if (isInsideZone(ZoneId.NO_STORE)) {
        creator?.sendMessage("Cannot create store in NOSTORE zone")
        return
    }
    
    if (items.size > privateSellStoreLimit) {
        creator?.sendMessage("Cannot create store with more than $privateSellStoreLimit items")
        return
    }

    sellList.clear()
    sellList.isPackaged = isPackageSale
    sellList.title = message

    var totalCost: Int = adena
    for (i in items) {
        
        val hasEnoughItems = inventory.getItemByItemId(i.first) != null && inventory.getItemByItemId(i.first).count >= i.second
        
        val itemObjectId = if(!hasEnoughItems) {
            inventory.addItem("botsell", i.first, i.second, this, null).objectId
        } else {
            inventory.getItemByItemId(i.first).objectId
        }
        
        if (!addToTradeList(itemObjectId, i.second, i.third, sellList)) {
            creator?.sendMessage("Item failed to be added to store")
            return
        }
        totalCost += i.third
        if (totalCost > Int.MAX_VALUE) {
            creator?.sendMessage("Cost integer overflowed")
            return
        }
    }

    sitDown()
    storeType = if (isPackageSale) StoreType.PACKAGE_SELL else StoreType.SELL
    broadcastUserInfo()
    broadcastPacket(PrivateStoreMsgSell(this))
}

internal fun Autobot.createPrivateBuyStore(items: List<Triple<Int, Int, Int>>, message: String = "", creator: Player? = null) {
    if (items.isEmpty()) {
        storeType = StoreType.NONE
        broadcastUserInfo()
        creator?.sendMessage("Cannot create store without items")
        return
    }

    if (!accessLevel.allowTransaction()) {
        creator?.sendMessage("Access level does not allow transaction")
        return
    }

    if (AttackStanceTaskManager.getInstance().isInAttackStance(this) || isCastingNow || isCastingSimultaneouslyNow || isInDuel) {
        creator?.sendMessage("Cannot create store in combat")
        return
    }

    if (isInsideZone(ZoneId.NO_STORE)) {
        creator?.sendMessage("Cannot create store in NOSTORE zone")
        return
    }
    
    buyList.clear()
    
    if (items.size > privateBuyStoreLimit) {
        creator?.sendMessage("Cannot create store with more than $privateBuyStoreLimit items")
        return
    }

    var totalCost = 0
    for (i in items) {
        if (!addToTradeListByItemId(i.first, i.second, i.third, buyList)) {
            creator?.sendMessage("Cannot add item with id ${i.first}")
            return
        }
        totalCost += i.third
        if (totalCost > Int.MAX_VALUE) {
            creator?.sendMessage("Exceeded the total code max amount")
            return
        }
    }
    
    if (totalCost > adena) {
        val missingAmount = totalCost - adena
        inventory.addAdena("add bot adena", missingAmount, this, null)
    }
    buyList.title = message
    
    sitDown()
    storeType = StoreType.BUY
    broadcastUserInfo()
    broadcastPacket(PrivateStoreMsgBuy(this))
}

internal fun Autobot.createPrivateCraftStore(recipes: List<Pair<Recipe, Int>>, message: String = "", creator: Player? = null) {
    if (isInDuel || isInCombat) {
        creator?.sendMessage("Cannot create a store while in combat")
        return
    }

    if (isInsideZone(ZoneId.NO_STORE)) {
        creator?.sendMessage("Cannot create store in NOSTORE zone")
        return
    }

    if (recipes.isEmpty()) {        
        forceStandUp() 
        return
    }
    
    val createList = ManufactureList()

    for (recipe in recipes) {
        val newRecipe = recipe.first

        if(!hasRecipeList(newRecipe.recipeId)) {
            if(newRecipe.isDwarven) {
                registerDwarvenRecipeList(newRecipe)
            }else {
                registerCommonRecipeList(newRecipe)
            }
        }

        val cost: Int = recipe.second
        createList.add(ManufactureItem(newRecipe.id, cost))
    }

    createList.storeName = message
    setCreateList(createList)
    storeType = StoreType.MANUFACTURE
    sitDown()
    broadcastUserInfo()
    broadcastPacket(RecipeShopMsg(this))
}

internal fun Player.getCombatBehavior() : CombatBehavior{
    if(this is Autobot) {
        return combatBehavior
    }
    
    return AutofarmManager.combatBehaviors[objectId] ?: error("CombatBehavior was not found for player $name and class id $classId")
}

internal fun Player.getSocialBehavior() : SocialBehavior{
    if(this is Autobot) {
        return socialBehavior
    }

    return AutofarmManager.socialBehaviors[objectId]!!
}

internal fun Player.getActiveSequence() : Sequence?{
    if(this is Autobot) {
        return activeSequence
    }

    return AutofarmManager.sequences[objectId]
}

internal fun Player.setCombatBehavior(combatBehavior: CombatBehavior){
    AutofarmManager.combatBehaviors[objectId] = combatBehavior
}

internal fun Player.setSocialBehavior(socialBehavior: SocialBehavior){
    AutofarmManager.socialBehaviors[objectId] = socialBehavior
}

internal fun Player.setActiveSequence(sequence: Sequence?){
    if(this is Autobot) {
        activeSequence = sequence
        return
    }
    AutofarmManager.sequences[objectId] = sequence
}

internal fun Player.getCombatBehaviorForClass() : CombatBehavior {
    return getBehaviorByClassId(classId, this, (supportedCombatPrefs[classId] ?: error("Unsupported class with id $classId")).invoke())
}

internal fun isClassSupported(classId: ClassId) : Boolean{
    return supportedCombatPrefs.containsKey(classId)
}

internal fun Player.attack(forceAttack: Boolean = false){
    if(target == null) return
    if (target is Player && ((target as Player).isCursedWeaponEquipped && level < 21 || isCursedWeaponEquipped && level < 21)) {
        sendPacket(ActionFailed.STATIC_PACKET)
        return
    }

    if (target != null) {

        val combatBehavior = getCombatBehavior()
        
        if(GeoEngine.getInstance().canSeeTarget(this, target)) {
            if(forceAttack) {
                ai.setIntention(IntentionType.IDLE)
            }
            ai.setIntention(IntentionType.ATTACK, target)
            if(combatBehavior.isMovingTowardsTarget) {
                combatBehavior.isMovingTowardsTarget = false
            }
            onActionRequest()
        }else {
            ai.setIntention(IntentionType.FOLLOW, target)
            combatBehavior.isMovingTowardsTarget = true
        }
    }
}

internal inline fun <reified T : Creature> Player.getClosestEntityInRadius(radius: Int, condition: (Creature) -> Boolean = { !it.isDead }): T? {
    val region = region ?: return null

    return region.surroundingRegions.flatMap { it.objects }
            .filterIsInstance<T>()
            .filter { condition(it) && !it.isGM && it.objectId != objectId && MathUtil.checkIfInRange(radius, this, it, true) }.minBy { distance(x, y, z, it.x, it.y, it.z) }
}

internal fun Player.useMagicSkill(skill: L2Skill, forceAttack: Boolean = false){

    if (skill.skillType == L2SkillType.RECALL && !Config.KARMA_PLAYER_CAN_TELEPORT && karma > 0) {
        return
    }

    if (skill.isToggle && isMounted) {
        return
    }

    if (isOutOfControl) {
        return
    }
    val combatBehavior = getCombatBehavior()
    
    if (target != null && !GeoEngine.getInstance().canSeeTarget(this, target)) {
        
        if(combatBehavior.committedTarget == target && combatBehavior.isMovingTowardsTarget) {
        }else {
            ai.setIntention(IntentionType.FOLLOW, target)
            combatBehavior.isMovingTowardsTarget = true
        }        
    }else {
        when {
            isAttackingNow -> {
                ai.setNextAction(NextAction(AiEventType.READY_TO_ACT, IntentionType.CAST, Runnable {
                    if(combatBehavior.isMovingTowardsTarget) {
                        combatBehavior.isMovingTowardsTarget = false
                    }
                    useMagic(skill, forceAttack, false)
                }))
            }
            isMoving -> {
                ai.setNextAction(NextAction(AiEventType.READY_TO_ACT, IntentionType.CAST, Runnable {
                    if(combatBehavior.isMovingTowardsTarget) {
                        combatBehavior.isMovingTowardsTarget = false
                    }
                    useMagic(skill, forceAttack, false)
                }))
            }
            else -> {
                if(combatBehavior.isMovingTowardsTarget) {
                    combatBehavior.isMovingTowardsTarget = false
                }
                useMagic(skill, forceAttack, false)
            }
        }
    }
}

internal fun Player.useItem(itemId: Int){

    val item = inventory.getItemByItemId(itemId) ?: return

    if (isInStoreMode) {
        return
    }

    if (activeTradeList != null) {
        return
    }

    if (item.item.type2 == Item.TYPE2_QUEST) {
        return
    }

    if (isAlikeDead || isStunned || isSleeping || isParalyzed || isAfraid) return

    if (!Config.KARMA_PLAYER_CAN_TELEPORT && karma > 0) {
        val sHolders = item.item.skills
        if (sHolders != null) {
            for (sHolder in sHolders) {
                val skill = sHolder.skill
                if (skill != null && (skill.skillType == L2SkillType.TELEPORT || skill.skillType == L2SkillType.RECALL)) return
            }
        }
    }

    if (isFishing && item.item.defaultAction != ActionType.fishingshot) {
        return
    }

    if (item.isPetItem) {
        if (!hasPet()) {
            return
        }
        val pet = summon as Pet
        if (!pet.canWear(item.item)) {
            return
        }
        if (pet.isDead) {
            return
        }
        if (!pet.inventory.validateCapacity(item)) {
            return
        }
        if (!pet.inventory.validateWeight(item, 1)) {
            return
        }
        transferItem("Transfer", item.objectId, 1, pet.inventory, pet)
        
        if (item.isEquipped) {
            pet.inventory.unEquipItemInSlot(item.locationSlot)
        } else {
            pet.inventory.equipPetItem(item)
        }
        pet.updateAndBroadcastStatus(1)
        return
    }

    if (!item.isEquipped) {
        if (!item.item.checkCondition(this, this, true)) return
    }

    if (item.isEquipable) {
        if (isCastingNow || isCastingSimultaneouslyNow) {
            return
        }
        when (item.item.bodyPart) {
            Item.SLOT_LR_HAND, Item.SLOT_L_HAND, Item.SLOT_R_HAND -> {
                if (isMounted) {
                    return
                }
                if (isCursedWeaponEquipped) return
            }
        }
        if (isCursedWeaponEquipped && item.itemId == 6408)
            return
        if (isAttackingNow) ThreadPool.schedule({
            val itemToTest = inventory.getItemByObjectId(item.objectId) ?: return@schedule
            useEquippableItem(itemToTest, false)
        }, attackEndTime - System.currentTimeMillis()) else useEquippableItem(item, true)
    } else {
        if (isCastingNow && !(item.isPotion || item.isElixir)) return
        if (attackType == WeaponType.FISHINGROD && item.item.itemType === EtcItemType.LURE) {
            inventory.setPaperdollItem(Inventory.PAPERDOLL_LHAND, item)
            broadcastUserInfo()
            return
        }
        val handler = ItemHandler.getInstance().getHandler(item.etcItem)
        handler?.useItem(this, item, false)
        for (quest in item.questEvents) {
            val state = getQuestState(quest.name)
            if (state == null || !state.isStarted) continue
            quest.notifyItemUse(item, this, target)
        }
    }
}

internal fun Player.moveTo(x: Int, y: Int, z: Int){
    ai.setIntention(IntentionType.MOVE_TO, Location(x, y, z))
}

internal fun Player.findClosestGatekeeper() : Gatekeeper?{
    return World.getInstance().objects.filterIsInstance<Gatekeeper>().minBy { distance(location(), it.location()) }
}

private fun addToTradeList(itemObjectId: Int, count: Int, price: Int, list: TradeList): Boolean {
    if (Int.MAX_VALUE / count < price) return false
    list.addItem(itemObjectId, count, price)
    return true
}

private fun addToTradeListByItemId(itemId: Int, count: Int, price: Int, list: TradeList): Boolean {
    if (Int.MAX_VALUE / count < price) return false
    list.addItemByItemId(itemId, count, price)
    return true
}

internal fun Player.controlBot(autobot: Autobot){
    if(autobot.controller != null) {
        if(autobot.controller!!.objectId == objectId) {
            sendMessage("You are already controlling this bot")
        }else {
            sendMessage("Bot ${autobot.name} is already being controller by ${autobot.controller!!.name}")
        }
        return
    }

    autobot.controller = this
    sendMessage("You are now controlling ${autobot.name}")

    skills.values.forEach { removeSkill(it.id, false) }
    autobot.skills.values.forEach { addSkill(it, false) }
    sendSkillList()
}

internal fun Player.unControlBot(autobot: Autobot){
    if(autobot.controller == null || (autobot.controller != null && autobot.controller!!.objectId != objectId)) {
        sendMessage("You are not in control of ${autobot.name}")
        return
    }

    autobot.controller = null
    skills.values.forEach { removeSkill(it.id, false) }
    rewardSkills()
    sendMessage("You are no longer controlling ${autobot.name}")
}

internal fun Player.isControllingBot() : Boolean {
    return AutobotsManager.activeBots.values.firstOrNull { it.controller?.objectId == objectId } != null
}

internal fun Player.getControllingBot() : Autobot? {
    return AutobotsManager.activeBots.values.firstOrNull { it.controller?.objectId == objectId }
}

internal fun Creature.location() : Location {
    return Location(x, y, z)
}