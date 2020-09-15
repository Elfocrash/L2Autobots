package dev.l2j.autobots.behaviors

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.behaviors.attributes.*
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.extensions.*
import dev.l2j.autobots.skills.BotSkill
import dev.l2j.autobots.utils.*
import kotlinx.coroutines.delay
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.xml.MapRegionData
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType
import net.sf.l2j.gameserver.enums.IntentionType
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.geoengine.GeoEngine
import net.sf.l2j.gameserver.model.L2Skill
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.Servitor
import net.sf.l2j.gameserver.model.location.Location


internal abstract class CombatBehavior(val player: Player, var combatPreferences: CombatPreferences) : ConsumableUser {

    var activityPreferences: ActivityPreferences = ActivityPreferences()
    
    open val conditionalSkills: List<Int> = emptyList()
    
    abstract var skillPreferences: SkillPreferences
    
    internal var committedTarget: Creature? = null
    
    internal var isMovingTowardsTarget: Boolean = false
    
    private val blacklistedTargets: MutableList<Int> = mutableListOf()
    
    internal suspend fun onUpdate() {
        if(player.isDead) {
            handleDeath()
            return
        }
        
        ensureSummonPet()        
        handleShots()
        targetAppropriateTarget()
        checkSelfSupportingSkills()
        beforeAttack()
        attack()
        afterAttack()
    }
    
    internal fun isMoving() : Boolean{
        if(player is Autobot) {
            player.isInMotion()
        }
        
        return player.isMoving
    }

    private suspend fun handleDeath() {
        val reserAround = player.getClosestEntityInRadius<Autobot>(2000)
        { it is Autobot && it.combatBehavior is Reser && ((player.isInParty && it.isInParty && player.party == it.party) || player.clan != null && it.clan != null && player.clanId == it.clanId) }

        if (reserAround != null) {
            delay(3000)
            return
        }
        delay(Rnd.get(3000L, 8000L))
        val location: Location = MapRegionData.getInstance().getLocationToTeleport(player, TeleportType.TOWN)

        if (player.isDead) {
            player.lastDeathLocation = player.location()
            resetTarget()
            player.doRevive()
            player.teleportTo(location.x, location.y, location.z, 20)
            player.getSocialBehavior().onRespawn()
        }
    }

    private fun ensureSummonPet() {
        if (this is PetOwner && combatPreferences is PetOwnerPreferences) {
            
            if((combatPreferences as PetOwnerPreferences).summonPet) {
                (this as PetOwner).summonPet(player)
            }else {
                if(player.hasServitor()) {
                    (player.summon as Servitor).unSummon(player)
                }
            }
        }
    }

    protected open suspend fun targetAppropriateTarget() {
        tryTargetCreatureByTypeInRadius(combatPreferences.targetingRadius, combatPreferences.targetingPreference)
    }
    
    private fun resetTarget(){
        committedTarget = null
        player.target = null
        isMovingTowardsTarget = false
    }

    protected open suspend fun beforeAttack(){
        if(this is RequiresMiscItem) {
            handleMiscItems(player)
        }
        
        handleConsumables(player)
        
        if(this is Kiter) {
            kite(this.player)
        }
        
        if(this is SecretManaRegen) {
            regenMana(this.player)
        }
    }
    
    protected open suspend fun afterAttack(){}

    protected open suspend fun attack() {
        if(player.target == null)
            return
        
        val skill = getNextAvailableSkill()

        if(skill == null) {
            if(!skillPreferences.isSkillsOnly) {
                player.attack(shouldAttackPlayer())
            }
        }
        else {
            player.useMagicSkill(skill, shouldAttackPlayer())
        }

        if(this is PetOwner) {
            (this as PetOwner).petAssist(player)
        }
    }

    private fun shouldAttackPlayer() : Boolean {
        if(player.target !is Player)
            return false
        
        return when(combatPreferences.attackPlayerType){
            AttackPlayerType.None -> return false
            AttackPlayerType.Flagged -> (player.target as Player).pvpFlag > 0 || (player.target as Player).karma > 0
            AttackPlayerType.Innocent -> return true
        }
    }

    fun getNextAvailableSkill(): L2Skill? {
        
        if(getOffensiveSkills.isEmpty()) return null

        var skillToUse: L2Skill? = null
        for(botSkill in getOffensiveSkills) {
            val skill = player.getSkill(botSkill.skillId) ?: continue

            if(!player.checkDoCastConditions(skill))
                continue
            
            if(botSkill.condition(player, skill, player.target as Creature?) && skill.checkCondition(player, player.target, false) && skill.mpConsume < player.currentMp) {
                skillToUse = skill
                break
            }
        }
        
        if(skillToUse == null) return null
        
        return skillToUse
    }
    
    protected fun checkSelfSupportingSkills(){
        if(getSelfSupportSkills.isEmpty()) return
        
        for (supportSkill in getSelfSupportSkills) {
            val skill = player.getSkill(supportSkill.skillId) ?: continue
            
            if(skill.isToggle && supportSkill.isTogglableSkill && 
                    player.getCombatBehavior().skillPreferences.togglableSkills[supportSkill.skillId] != null &&
                    !player.getCombatBehavior().skillPreferences.togglableSkills[supportSkill.skillId]!! && player.hasEffect(skill.id)) {
                player.removeEffect(player.getFirstEffect(skill.id))
                continue
            }
            
            if(supportSkill.condition(player, skill, if (supportSkill.forceTargetSelf) player else player.target as Creature? )) {
                
                if(supportSkill.forceTargetSelf) {
                    val previousTarget = player.target
                    player.target = player
                    player.useMagicSkill(skill)
                    player.target = previousTarget
                    return
                }
                player.useMagicSkill(skill)
            }
        }
    }
    
    internal open fun applyBuffs() {
        player.applyBuffs(*combatPreferences.buffs)

        if(player.hasServitor() && this is PetOwner) {
            val servitor = player.summon as Servitor
            if(combatPreferences is PetOwnerPreferences && (combatPreferences as PetOwnerPreferences).petHasBuffs) {
                servitor.applyBuffs(*(this as PetOwner).petBuffs)
            }

            if(combatPreferences is PetOwnerPreferences && !(combatPreferences as PetOwnerPreferences).petHasBuffs) {
                (this as PetOwner).petBuffs.forEach { 
                    if(servitor.getFirstEffect(it[0]) != null) {
                        servitor.removeEffect(servitor.getFirstEffect(it[0]))
                    }
                }
            }
        }
    }

    protected open suspend fun tryTargetCreatureByTypeInRadius(radius: Int, targetingPreference: TargetingPreference) {        
        
        if(player.target == null && committedTarget != null && !isMovingTowardsTarget && !committedTarget!!.isDead) {
            if(GeoEngine.getInstance().canSeeTarget(player, committedTarget)) {
                player.target = committedTarget
            }else {
                resetTarget()
            }            
        }
        
        if(player.target != null && (player.target as Creature).target == player && !(player.target as Creature).isDead && (player.target as Creature).isInCombat && player.isInCombat)
            return
        
        if(player.target != null && combatPreferences.attackPlayerType == AttackPlayerType.Flagged && (player.target is Player && (player.target as Player).pvpFlag.toInt() == 0 && (player.target as Player).karma == 0)) {
            resetTarget()
        }

        if(player.target != null && player.target is Player && combatPreferences.attackPlayerType == AttackPlayerType.None) {
            resetTarget()
        }
        
        if(committedTarget != null && player.target != null && player.target == committedTarget && !(player.target as Creature).isDead) {
            if(isMovingTowardsTarget && GeoEngine.getInstance().canSeeTarget(player, player.target)) {
                player.ai.setIntention(IntentionType.ACTIVE)
                isMovingTowardsTarget = false
                blacklistedTargets.clear()
                return
            }
        }
        
        val closestTargeter = player.getClosestEntityInRadius<Creature>(radius){ !areInTheSameClanOrParty(it) &&
                (if(combatPreferences.attackPlayerType == AttackPlayerType.None) it !is Player else true) && !it.isDead && it.target != null && it.target.objectId == player.objectId }

        if((player.target != null && closestTargeter != null && !blacklistedTargets.contains(closestTargeter.objectId) && player.target != closestTargeter && !(player.target as Creature).isInCombat) && GeoEngine.getInstance().canSeeTarget(player, closestTargeter)) {
            player.target = closestTargeter
            committedTarget = closestTargeter
            blacklistedTargets.clear()
            delay(Rnd.get(250L, 1000L))
            return
        }
        
        if(committedTarget != null && !committedTarget!!.isDead) {
            return
        }else {
            committedTarget == null
        }
        
        if (player.target == null || (player.target != null && (player.target as Creature).isDead)) {
            targetAppropriateCreatureByTypeInRadius(radius, targetingPreference)
        }else {
            if (player.target == null || !player.isMoving) {
                if(player.target != null) {
                    blacklistedTargets.add(player.targetId)
                }
                
                resetTarget()
            }
        }

        if(player.target != null && (player.target as Creature).isDead) {
            resetTarget()
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun targetAppropriateCreatureByTypeInRadius(radius: Int, targetingPreference: TargetingPreference) { // TODO alive only?
        
        val targets: MutableList<Creature> = when(targetingPreference) {
            TargetingPreference.Random -> player.getKnownTargatablesInRadius(radius, combatPreferences.attackPlayerType) { !areInTheSameClanOrParty(it) && !blacklistedTargets.contains(it.objectId) }.toMutableList()
            TargetingPreference.Closest -> {
                val target = player.getKnownTargatablesInRadius(radius, combatPreferences.attackPlayerType) { !areInTheSameClanOrParty(it) && !blacklistedTargets.contains(it.objectId) }.minBy { distance(player, it) }
                if(target != null) mutableListOf(target) else mutableListOf()
            }
        }

        if (targets.isNotEmpty()) {

            val closestAttacker = if(player.target != null && (player.target as Creature).getHealthPercentage() < 100) null else when(targetingPreference){
                TargetingPreference.Closest -> targets.firstOrNull{it.target != null && it.target.objectId == player.objectId && it.isInCombat && !blacklistedTargets.contains(it.objectId)}
                TargetingPreference.Random -> targets.sortedBy { distance(player, it) }.firstOrNull{it.target != null && it.target.objectId == player.objectId && it.isInCombat && !blacklistedTargets.contains(it.objectId)}
            }

            if(closestAttacker != null) {
                player.target = closestAttacker
                committedTarget = closestAttacker
                blacklistedTargets.clear()
                return
            }
            
            val target = when(targetingPreference){
                TargetingPreference.Closest -> when(combatPreferences.attackPlayerType) {
                    AttackPlayerType.None -> targets.firstOrNull()//{ it.z <= player.z + 50 && it.z >= player.z - 50}
                    AttackPlayerType.Innocent, AttackPlayerType.Flagged -> if(targets.any { it is Player }) targets.first { it is Player } else targets.firstOrNull()
                }
                TargetingPreference.Random -> when(combatPreferences.attackPlayerType) {
                    AttackPlayerType.None -> targets.randomOrNull()//.filter { it.z <= player.z + 50 && it.z >= player.z - 50}
                    AttackPlayerType.Innocent, AttackPlayerType.Flagged -> if(targets.any { it is Player }) targets.filterIsInstance<Player>().random() else targets.random()
                }
            }

            if(target == null && player.target == null)
                return
            
            if(player.isMoving) {
                return
            }
            
            player.target = target
            committedTarget = player.target as Creature?
        }
    }

    private fun areInTheSameClanOrParty(it: Creature) : Boolean {
        return (it is Player && it.isInParty && player.isInParty && player.party == it.party) ||
                (it is Player && it.clan != null && player.clan != null && player.clanId == it.clanId)
    }

    protected open fun handleShots() {
        if(player is Autobot) {
            if (player.inventory.getItemByItemId(getShotId(player)) != null) {
                if (player.inventory.getItemByItemId(getShotId(player)).count <= 20) {
                    player.inventory.addItem("", getShotId(player), 500, player, null)
                }
            } else {
                player.inventory.addItem("", getShotId(player), 500, player, null)
            }
        }        
        
        if (!player.autoSoulShot.contains(getShotId(player))) {
            player.addAutoSoulShot(getShotId(player))
            player.rechargeShots(true, true)
        }
    }

    override val consumables: List<Consumable> = mutableListOf(
            Consumable(1540) { autobot -> autobot.getCombatBehavior().combatPreferences.useQuickHealingPots &&  autobot.getHealthPercentage() < 30 },
            Consumable(728) { autobot -> autobot.getCombatBehavior().combatPreferences.useManaPots && autobot !is SecretManaRegen &&  autobot.getManaPercentage() < 60 },
            Consumable(1539) { autobot -> autobot.getCombatBehavior().combatPreferences.useGreaterHealingPots &&  autobot.getHealthPercentage() < 80 && !autobot.hasEffect(2037) },
            Consumable(5592) { autobot -> autobot.getCombatBehavior().combatPreferences.useGreaterCpPots &&  autobot.getCpPercentage() < 80 }
    )

    internal fun validateConditionalSkill(skill: L2Skill) =
            skillPreferences.skillUsageConditions.filter { it.skillId == skill.id }.any { it.isValid(player) }

    internal fun <T : SkillPreferences> typedSkillPreferences(): T {
        return skillPreferences as T
    }
    
    internal abstract fun getShotType(): ShotType
    protected abstract val getOffensiveSkills: List<BotSkill>
    protected open val getSelfSupportSkills: List<BotSkill> = emptyList()
    
    open fun onLevelUp(oldLevel: Int, newLevel: Int){}
    
    init {
        player.setIsRunning(true)
    }
}