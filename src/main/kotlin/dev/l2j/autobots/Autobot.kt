package dev.l2j.autobots

import dev.l2j.autobots.AutobotsManager.loadAutobotDashboard
import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.SocialBehavior
import dev.l2j.autobots.behaviors.preferences.ActivityType
import dev.l2j.autobots.behaviors.preferences.SocialPreferences
import dev.l2j.autobots.behaviors.preferences.TownAction
import dev.l2j.autobots.behaviors.sequences.EquipGearRealisticallySequence
import dev.l2j.autobots.behaviors.sequences.Sequence
import dev.l2j.autobots.dao.AutobotsDao
import dev.l2j.autobots.extensions.getNewClassId
import dev.l2j.autobots.extensions.shouldChangeClass
import dev.l2j.autobots.models.AutobotLoc
import dev.l2j.autobots.models.BotChat
import dev.l2j.autobots.models.BotDebugAction
import dev.l2j.autobots.models.RespawnAction
import dev.l2j.autobots.utils.clearCircle
import dev.l2j.autobots.utils.getBehaviorByClassId
import dev.l2j.autobots.utils.supportedCombatPrefs
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.sf.l2j.Config
import net.sf.l2j.L2DatabaseFactory
import net.sf.l2j.commons.math.MathUtil
import net.sf.l2j.gameserver.data.SkillTable
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill
import net.sf.l2j.gameserver.data.manager.CastleManager
import net.sf.l2j.gameserver.data.manager.ClanHallManager
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager
import net.sf.l2j.gameserver.data.xml.AdminData
import net.sf.l2j.gameserver.enums.MessageType
import net.sf.l2j.gameserver.enums.SiegeSide
import net.sf.l2j.gameserver.enums.ZoneId
import net.sf.l2j.gameserver.enums.actors.ClassId
import net.sf.l2j.gameserver.enums.skills.L2EffectType
import net.sf.l2j.gameserver.handler.ChatHandler
import net.sf.l2j.gameserver.model.L2Skill
import net.sf.l2j.gameserver.model.World
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Npc
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.StaticObject
import net.sf.l2j.gameserver.model.actor.npc.RewardInfo
import net.sf.l2j.gameserver.model.actor.player.Appearance
import net.sf.l2j.gameserver.model.actor.player.BlockList
import net.sf.l2j.gameserver.model.actor.player.Experience
import net.sf.l2j.gameserver.model.actor.template.PlayerTemplate
import net.sf.l2j.gameserver.model.holder.skillnode.GeneralSkillNode
import net.sf.l2j.gameserver.model.item.instance.ItemInstance
import net.sf.l2j.gameserver.model.location.Location
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager
import net.sf.l2j.gameserver.model.tradelist.TradeList
import net.sf.l2j.gameserver.network.SystemMessageId
import net.sf.l2j.gameserver.network.serverpackets.*
import net.sf.l2j.gameserver.taskmanager.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors

class Autobot(objectId: Int, template: PlayerTemplate?, app: Appearance?) : Player(objectId, template, "Autobots", app) {
    
    internal var activeSequence: Sequence? = null
    
    internal val devActions: HashMap<Int, HashMap<BotDebugAction, (Player, Autobot) -> Unit>> = hashMapOf()
    
    internal var isBusyThinking: AtomicBoolean = AtomicBoolean(false)
    
    internal val combatBehavior : CombatBehavior = getBehaviorByClassId(classId, this, (supportedCombatPrefs[classId] ?: error("Unsupported class with id $classId")).invoke())
    
    internal var socialBehavior: SocialBehavior = SocialBehavior(this, SocialPreferences(TownAction.TeleToRandomLocation))
    
    internal var respawnAction: RespawnAction = RespawnAction.ReturnToDeathLocation
    
    internal val chatMessages = mutableListOf<BotChat>()    
    
    internal var spawnTime: Long = 0
    
    internal var controller: Player? = null
    
    private val previousIterationLocation: AutobotLoc = AutobotLoc(x, y, z)
    
    internal fun isInMotion() : Boolean{
        return previousIterationLocation.x != x || previousIterationLocation.y != y || previousIterationLocation.z != z  
    } 
    
    internal fun addChat(chat: BotChat){
        chatMessages.add(chat)
        
        if(chatMessages.size > 30) {
            chatMessages.removeAt(0)
        }
    }    
    
    internal suspend fun onUpdate() {
        updateLocation()
        devDebug()
        if(controller != null) return
        
        if(activeSequence != null) return
        
        if(isBusyThinking.get()) return
        
        isBusyThinking.set(true)
        handleScheduledLogout()

        combatBehavior.applyBuffs()
        
        if(isInsideZone(ZoneId.TOWN)) {
            socialBehavior.onUpdate()
        } else {
            combatBehavior.onUpdate()
        }
        updateLocation()
        isBusyThinking.set(false)
    }

    private fun updateLocation() {
        previousIterationLocation.x = x
        previousIterationLocation.y = y
        previousIterationLocation.z = z
    }

    private fun handleScheduledLogout() {
        if (combatBehavior.activityPreferences.activityType == ActivityType.Uptime) {
            val minutesOnline = ((System.currentTimeMillis() - spawnTime) / 1000) / 60
            if (minutesOnline >= combatBehavior.activityPreferences.uptimeMinutes) {
                despawn()
            }
        }
    }

    private fun devDebug() {
        if(devActions.isEmpty()) return

        devActions.forEach { (playerId, actions) ->
            val gm = World.getInstance().getPlayer(playerId)

            if(gm == null) {
                devActions.remove(playerId)
                return@forEach
            }

            actions.forEach{ (_, action) ->
                action(gm, this)
            }
        }
    }

    fun addDevAction(player: Player, actionType: BotDebugAction, action: (Player, Autobot) -> Unit) {
        if(!devActions.containsKey(player.objectId)) {
            devActions[player.objectId] = hashMapOf()
        }

        devActions[player.objectId]!![actionType] = action
    }

    fun removeDevAction(player: Player, actionType: BotDebugAction){
        if(!devActions.containsKey(player.objectId)) {
            devActions[player.objectId] = hashMapOf()
        }

        devActions[player.objectId]!!.remove(actionType)
    }

    fun hasDevAction(player: Player, actionType: BotDebugAction) : Boolean{
        if(!devActions.containsKey(player.objectId)) {
            return false
        }

        return devActions[player.objectId]!!.containsKey(actionType)
    }

    fun clearDevActions(){
        devActions.forEach { (playerId, actions) ->
            val gm = World.getInstance().getPlayer(playerId) ?: return@forEach

            actions.forEach { (actionType, _) ->
                if(actionType == BotDebugAction.VisualizeVision) {
                    clearCircle(gm, "${name}${BotDebugAction.VisualizeVision}")
                }
            }
        }
    }
    
    override fun onPlayerEnter() {
        super.onPlayerEnter()
        spawnTime = System.currentTimeMillis()
    }
    
    internal fun mySetActiveClass(classId: Int){
        _activeClass = classId
    }

    override fun setClassId(id: Int) {
        super.setClassId(id)
        onClassChange(ClassId.values()[id])
    }
    
    internal fun onClassChange(classId: ClassId){
        
    }

    internal fun heal(){
        currentCp = maxCp.toDouble()
        currentHp = maxHp.toDouble()
        currentMp = maxMp.toDouble()
    }

    internal fun setLevel(level: Int) {
        if (level >= 1 && level <= Experience.MAX_LEVEL) {
            val pXp = exp
            val tXp = Experience.LEVEL[level]

            if (pXp > tXp)
                removeExpAndSp(pXp - tXp, 0)
            else if (pXp < tXp)
                addExpAndSp(tXp - pXp, 0)
        }
    }

    override fun onActionShift(player: Player?) {
        if(player!!.isGM) {
            loadAutobotDashboard(player, this)
            if (player.target !== this)
                player.target = this
            player.sendPacket(ActionFailed.STATIC_PACKET)
        }
        
    }

    override fun teleportTo(x: Int, y: Int, z: Int, randomOffset: Int) {
        super.teleportTo(x, y, z, randomOffset)
        onTeleported()
    }
    
    override fun getWeightPenalty(): Int {
        return 0
    }

    override fun getMaxLoad(): Int {
        return Int.MAX_VALUE
    }

    override fun getInventoryLimit(): Int {
        return Int.MAX_VALUE
    }

    internal fun setBotClass(newClassId: ClassId){
        if (classId != newClassId) {
            setClassId(newClassId.id)
            if (!isSubClassActive) baseClass = newClassId.id
            //bot.refreshOverloaded()
            //bot.store()
            AutobotsDao.saveAutobot(this)
            broadcastUserInfo()
        }
    }

    internal fun handlePlayerClanOnSpawn() {
        
        if(clan == null) return
        sendPacket(PledgeSkillList(clan))
        clan.getClanMember(objectId).playerInstance = this
        val msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(this)
        val update = PledgeShowMemberListUpdate(this)

        for (member in clan.onlineMembers) {
            if (member === this) continue
            member.sendPacket(msg)
            member.sendPacket(update)
        }

        if (sponsor != 0) {
            val sponsor: Player? = World.getInstance().getPlayer(sponsor)
            sponsor?.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(this))
        } else if (apprentice != 0) {
            val apprentice: Player? = World.getInstance().getPlayer(apprentice)
            apprentice?.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(this))
        }
        
        val clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan)
        if (clanHall != null && !clanHall.paid) sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW)
        for (castle in CastleManager.getInstance().castles) {
            val siege = castle.siege
            if (!siege.isInProgress) continue
            val type = siege.getSide(clan)
            if (type == SiegeSide.ATTACKER) this.siegeState = 1.toByte() else if (type == SiegeSide.DEFENDER || type == SiegeSide.OWNER) this.siegeState = 2.toByte()
        }
        sendPacket(PledgeShowMemberListAll(clan, 0))
        for (sp in clan.allSubPledges) this.sendPacket(PledgeShowMemberListAll(clan, sp.id))
        sendPacket(UserInfo(this))
        sendPacket(PledgeStatusChanged(clan))
    }

    @Synchronized
    internal fun despawn() {
        if(!isIngame()) {
            return
        }
        
        try{
            clearDevActions()
            setOnlineStatus(false, true)
            abortAttack()
            abortCast()
            stopMove(null)
            target = null
    
            removeMeFromPartyMatch()
    
            if (isFlying) removeSkill(FrequentSkill.WYVERN_BREATH.skill.id, false)
            if (isMounted) dismount() else if (summon != null) summon.unSummon(this)
            stopHpMpRegeneration()
            stopChargeTask()
            punishment.stopTask(true)
            WaterTaskManager.getInstance().remove(this)
            AttackStanceTaskManager.getInstance().remove(this)
            PvpFlagTaskManager.getInstance().remove(this)
            GameTimeTaskManager.getInstance().remove(this)
            ShadowItemTaskManager.getInstance().remove(this)
            for (character in getKnownType(Creature::class.java)) if (character.fusionSkill != null && character.fusionSkill.target === this) character.abortCast()
            
            for (effect in allEffects) {
                if (effect.skill.isToggle) {
                    effect.exit()
                    continue
                }
                when (effect.effectType) {
                    L2EffectType.SIGNET_GROUND, L2EffectType.SIGNET_EFFECT -> effect.exit()
                }
            }
            
            decayMe()
            
            if (party != null) party.removePartyMember(this, MessageType.DISCONNECTED)
            if (OlympiadManager.getInstance().isRegistered(this) || olympiadGameId != -1) OlympiadManager.getInstance().removeDisconnectedCompetitor(this)
            
            if (clan != null) {
                val clanMember = clan.getClanMember(objectId)
                if (clanMember != null) clanMember.playerInstance = null
            }
            
            if (activeRequester != null) {
                activeRequester = null
                cancelActiveTrade()
            }
            
            if (isGM) AdminData.getInstance().deleteGm(this)
    
            if (isInObserverMode) setXYZInvisible(savedLocation)
    
            if (boat != null) boat.oustPlayer(this, true, Location.DUMMY_LOC)
    
            inventory.deleteMe()
    
            clearWarehouse()
    
            clearFreight()
            clearDepositedFreight()
    
            if (isCursedWeaponEquipped) CursedWeaponManager.getInstance().getCursedWeapon(cursedWeaponEquippedId).player = null
            
            if (clanId > 0) clan.broadcastToOtherOnlineMembers(PledgeShowMemberListUpdate(this), this)
    
            if (isSeated) {
                val `object` = World.getInstance().getObject(_throneId)
                if (`object` is StaticObject) (`object` as StaticObject).isBusy = false
            }
    
            AutobotsDao.saveAutobot(this)
            
            World.getInstance().removePlayer(this) 
            AutobotsManager.activeBots.remove(name)
            blockList.playerLogout()    
        }
        catch (e: Exception)
        {
            LOGGER.error("Couldn't disconnect correctly the player.", e)
        }
        
        if (hasAI()) ai.stopAITask()
    }

    override fun doDie(killer: Creature?): Boolean {
        return super.doDie(killer)
    }
    
    override fun rewardSkills() {
        for (skill in allAvailableSkills) addSkill(skill.skill, skill.cost != 0)
        if (level >= 10 && hasSkill(L2Skill.SKILL_LUCKY)) removeSkill(L2Skill.SKILL_LUCKY, false)
        removeInvalidSkills()
    }

    private fun removeInvalidSkills() {
        if (skills.isEmpty()) return
    
        val availableSkills = template.skills.stream().filter { s: GeneralSkillNode -> s.minLvl <= level + if (s.id == L2Skill.SKILL_EXPERTISE) 0 else 9 }.collect(Collectors.groupingBy({ s: GeneralSkillNode -> s.id }, Collectors.maxBy(Comparator.comparing { obj: GeneralSkillNode -> obj.value })))
        for (skill in skills.values) {
            if (template.skills.stream().filter { s: GeneralSkillNode -> s.id == skill.id }.count() == 0L) continue
            
            val tempSkill = availableSkills[skill.id]
            if (tempSkill == null) {
                removeSkill(skill.id, false)
                continue
            }
            
            val availableSkill = tempSkill.get()
            val maxLevel = SkillTable.getInstance().getMaxLevel(skill.id)
            
            if (skill.level > maxLevel) {
                if ((level < 76 || availableSkill.value < maxLevel) && skill.level > availableSkill.value) addSkill(availableSkill.skill, false)
            } else if (skill.level > availableSkill.value) addSkill(availableSkill.skill, false)
        }        
    }

    internal fun setClassIndex(index: Int){
        _classIndex = index
    }

    override fun updateOnlineStatus() {
        try {
            L2DatabaseFactory.getInstance().connection.use { con ->
                con.prepareStatement("UPDATE autobots SET online=? WHERE obj_Id=?").use { ps ->
                    ps.setInt(1, isOnlineInt)
                    ps.setInt(2, objectId)
                    ps.execute()
                }
            }
        } catch (e: java.lang.Exception) {
            LOGGER.error("Couldn't set bot online status.", e)
        }
    }
    
    override fun isOnlineInt(): Int {
        return if (isOnline) 1 else 0 
    }

    internal fun isIngame(): Boolean {
        return AutobotsManager.activeBots.containsKey(name)
    }

    override fun addExpAndSp(addToExp: Long, addToSp: Int) {
        val prelevel = level
        super.addExpAndSp(addToExp, addToSp)
        val postLevel = level
        
        if(postLevel != prelevel) {
            onLevelChange(prelevel, postLevel)
            combatBehavior.onLevelUp(prelevel, postLevel)
            rewardSkills()
        }
    }

    override fun addExpAndSp(addToExp: Long, addToSp: Int, rewards: MutableMap<Creature, RewardInfo>?) {
        val prelevel = level
        super.addExpAndSp(addToExp, addToSp, rewards)
        val postLevel = level

        if(postLevel != prelevel) {
            onLevelChange(prelevel, postLevel)
            combatBehavior.onLevelUp(prelevel, postLevel)
            rewardSkills()
        }
    }

    override fun removeExpAndSp(removeExp: Long, removeSp: Int) {
        val prelevel = level
        super.removeExpAndSp(removeExp, removeSp)
        val postLevel = level

        if(postLevel != prelevel) {
            onLevelChange(prelevel, postLevel)
            combatBehavior.onLevelUp(prelevel, postLevel)
        }
    }
    
    @OptIn(ExperimentalStdlibApi::class)
    internal fun onLevelChange(oldLevel: Int, newLevel: Int){
        val bot = this

        val triggerClassChange = shouldChangeClass(oldLevel, newLevel)
        
        if(!triggerClassChange) {
            CoScopes.generalScope.launch { EquipGearRealisticallySequence(bot).execute() }
        } else {
            CoScopes.generalScope.launch {
                
                val newClassId = getNewClassId(classId)
                
                if(newClassId != null && newClassId != classId) {
                    delay(2500)
                    setClassId(newClassId.id)
                    rewardSkills()
                }

                delay(1000)
                EquipGearRealisticallySequence(bot).execute()
            }
        }
    }
    
    internal fun startTradeRequest(target: Player){
        if (!accessLevel.allowTransaction()) {
            return
        }
        
        if (!getKnownType(Player::class.java).contains(target) || target == this) {
            return
        }

        if (target.isInOlympiadMode || isInOlympiadMode) {
            return
        }
        
        if (!Config.KARMA_PLAYER_CAN_TRADE && (karma > 0 || target.karma > 0)) {
            return
        }

        if (isInStoreMode || target.isInStoreMode) {
            return
        }

        if (isProcessingTransaction) {
            return
        }

        if (target.isProcessingRequest || target.isProcessingTransaction) {
            return
        }

        if (target.tradeRefusal) {
            return
        }

        if (BlockList.isBlocked(target, this)) {
            return
        }

        if (MathUtil.calculateDistance(this, target, true) > Npc.INTERACTION_DISTANCE) {
            return
        }

        onTransactionRequest(target)
        target.sendPacket(SendTradeRequest(objectId))
    }
    
    internal fun answerTradeRequest(accept: Boolean){
        val partner: Player? = activeRequester
        if (partner == null || World.getInstance().getPlayer(partner.objectId) == null) {
            activeRequester = null
            return
        }

        if (accept && !partner.isRequestExpired) startTrade(partner) else partner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DENIED_TRADE_REQUEST).addCharName(this))

        activeRequester = null
        partner.onTransactionResponse()
    }
    
    internal fun hasActiveTradeRequest() : Boolean{
        val partner: Player? = activeRequester
        if (partner == null || World.getInstance().getPlayer(partner.objectId) == null) {
            activeRequester = null
            return false
        }
        
        return !partner.isRequestExpired
    }
    
    internal fun hasActiveTrade() : Boolean{
        return activeTradeList != null
    }

    override fun cancelActiveTrade() {
        super.cancelActiveTrade()
    }
    
    internal fun say(message: String){
        val handler = ChatHandler.getInstance().getHandler(0)
        handler.handleChat(0, this, null, message)
    }

    internal fun shout(message: String){
        val handler = ChatHandler.getInstance().getHandler(1)
        handler.handleChat(0, this, null, message)
    }

    override fun onTradeFinish(successfull: Boolean) {
        super.onTradeFinish(successfull)
        if(successfull) {
            val bot = this
            GlobalScope.launch { 
                delay(2000) 
                bot.say("ty") 
            }
        }
    }
    
    internal fun addTradeItem(item: ItemInstance){
        val trade: TradeList = activeTradeList ?: return

        val partner = trade.partner
        if (partner == null || World.getInstance().getPlayer(partner.objectId) == null || partner.activeTradeList == null) {
            cancelActiveTrade()
            return
        }

        if (trade.isConfirmed || partner.activeTradeList.isConfirmed) {
            return
        }

        if (!getAccessLevel().allowTransaction()) {
            cancelActiveTrade()
            return
        }

        if (validateItemManipulation(item.objectId) == null) {
            return
        }

        val addedItem = trade.addItem(item.objectId, item.count)
        if (addedItem != null) {
            //player.sendPacket(TradeOwnAdd(item))
           // player.sendPacket(TradeItemUpdate(trade, player))
            trade.partner.sendPacket(TradeOtherAdd(addedItem))
        }
    }

    internal fun cancelSequence() {
        activeSequence?.cancellationToken?.cancelLambda?.invoke()
    }
}