package dev.l2j.autobots.autofarm

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dev.l2j.autobots.CoScopes
import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.SocialBehavior
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.behaviors.sequences.Sequence
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.extensions.getCombatBehaviorForClass
import dev.l2j.autobots.extensions.getSocialBehavior
import dev.l2j.autobots.extensions.isClassSupported
import dev.l2j.autobots.utils.Klock
import dev.l2j.autobots.utils.supportedCombatPrefs
import kotlinx.coroutines.launch
import net.sf.l2j.L2DatabaseFactory
import net.sf.l2j.gameserver.enums.ZoneId
import net.sf.l2j.gameserver.enums.actors.ClassId
import net.sf.l2j.gameserver.model.actor.Player
import java.util.concurrent.ConcurrentHashMap

object AutofarmManager {

    private val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    private val activePlayers = ConcurrentHashMap<Int, Player>()
    internal val combatBehaviors = HashMap<Int, CombatBehavior>()
    internal val socialBehaviors = HashMap<Int, SocialBehavior>()
    internal val sequences = HashMap<Int, Sequence?>()
    private val busyStates = ConcurrentHashMap<Int, Boolean>()
    private val combatPreferences = HashMap<Int, HashMap<Int, CombatPreferences>>()
    private val skillPreferences = HashMap<Int, HashMap<Int, SkillPreferences?>>()

    internal var onUpdateTask =
            Klock.start("autofarmOnUpdate", 1000, 500, CoScopes.onUpdateScope, action = {
                activePlayers.values.forEach {
                    CoScopes.onUpdateScope.launch {
                        try {
                            if(busyStates.contains(it.objectId)) return@launch

                            busyStates[it.objectId] = true

                            if(it.isInsideZone(ZoneId.TOWN)) {
                                it.getSocialBehavior().onUpdate()
                            } else {
                                it.getCombatBehavior().onUpdate()
                            }
                            busyStates.remove(it.objectId)
                        } catch (e: Exception){
                            busyStates.remove(it.objectId)
                            println(e)
                        }
                    }
                }
            })
    
    fun isAutoFarming(player: Player) : Boolean{
        return activePlayers.containsKey(player.objectId)
    }
    
    @Synchronized
    fun startFarm(player: Player){
        if(!isClassSupported(ClassId.values().first { it.id == player.activeClass})) {
            player.sendMessage("Autofarm doesn't support your class")
            return
        }
        
        if(activePlayers.containsKey(player.objectId)) {
            player.sendMessage("Autofarm is already enabled")
            return
        }

        val behavior = getBehaviorForActiveClass(player)

        activePlayers[player.objectId] = player
        combatBehaviors[player.objectId] = behavior
        socialBehaviors[player.objectId] = SocialBehavior(player, SocialPreferences(TownAction.None))
        
        player.sendMessage("Autofarm activated")
    }
    
    @Synchronized
    fun stopFarm(player: Player){
        if(!activePlayers.containsKey(player.objectId)) {
            player.sendMessage("Autofarm is not enabled")
            return
        }

        cleanUpStates(player)
        player.sendMessage("Autofarm deactivated")
    }
    
    @Synchronized
    internal fun onEnterWorld(player: Player) {
        val playerPreferencesDto = restorePreferences(player)

        val isInit = playerPreferencesDto.isEmpty()
        
        combatPreferences[player.objectId] = hashMapOf()
        skillPreferences[player.objectId] = hashMapOf()
        
        playerPreferencesDto.forEach { 
            combatPreferences[it.playerId]!![it.classId] = it.combatPreferences
            skillPreferences[it.playerId]!![it.classId] = it.skillPreferences
        }
        
        if(isInit) {
            getBehaviorForActiveClass(player)
            savePreferences(player)
        }
    }
    
    @Synchronized
    fun savePreferences(player: Player){
        L2DatabaseFactory.getInstance().connection.use { con ->
            
            con.prepareStatement("INSERT INTO character_autofarm(obj_Id, classId, combat_prefs, skill_prefs) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE combat_prefs=?, skill_prefs=?").use { ps ->

                combatPreferences[player.objectId]?.forEach {

                    val skillPreferences = skillPreferences[player.objectId]?.get(it.key)
                    
                    ps.setInt(1, player.objectId)
                    ps.setInt(2, it.key)
                    
                    val combatPrefs = mapper.writeValueAsString(it.value)
                    val skillPrefs = if(skillPreferences != null) mapper.writeValueAsString(skillPreferences) else null                    
                    
                    ps.setString(3, combatPrefs)                    
                    ps.setString(4, skillPrefs)
                    ps.setString(5, combatPrefs)
                    ps.setString(6, skillPrefs)
                    ps.addBatch()
                }

                ps.executeBatch()
            }
        }
    }

    internal fun onLogout(player: Player) {
        savePreferences(player)
        cleanUpStates(player, true)
    }
    
    private fun cleanUpStates(player: Player, includeBehaviors: Boolean = false){
        activePlayers.remove(player.objectId)
        combatBehaviors.remove(player.objectId)
        socialBehaviors.remove(player.objectId)
        sequences.remove(player.objectId)
        busyStates.remove(player.objectId)
        if(includeBehaviors) {
            combatPreferences.remove(player.objectId)
            skillPreferences.remove(player.objectId)
        }
    }
    
    private fun restorePreferences(player: Player) : List<PlayerPreferencesDto> {
        val prefs = mutableListOf<PlayerPreferencesDto>()
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement("select * from character_autofarm where obj_Id=?").use { ps ->
                ps.setInt(1, player.objectId)
                ps.executeQuery().use { rs ->

                    while(rs.next()) {
                        val playerId = rs.getInt("obj_Id")
                        val classId = rs.getInt("classId")
                        
                        val jsonCombatPrefs = rs.getString("combat_prefs")
                        val combatPrefs = if(jsonCombatPrefs.isNullOrEmpty()) {
                            supportedCombatPrefs[ClassId.values().first { it.id == player.activeClass}]!!.invoke()
                        }else {
                            mapper.readValue(jsonCombatPrefs, supportedCombatPrefs.getOrDefault(ClassId.values().first { it.id == player.activeClass}) { DefaultCombatPreferences() }.invoke().javaClass)
                        }

                        val jsonSkillPrefs = rs.getString("skill_prefs")
                        val skillPrefs = if(!jsonSkillPrefs.isNullOrEmpty()) {
                            mapper.readValue(jsonSkillPrefs, player.getCombatBehaviorForClass().skillPreferences.javaClass)
                        }else {
                            null
                        }
                        
                        prefs.add(PlayerPreferencesDto(playerId, classId, combatPrefs, skillPrefs))
                    }
                }
            }
        }
        return prefs.toList()
    }

    private fun getBehaviorForActiveClass(player: Player): CombatBehavior {
        val behavior = player.getCombatBehaviorForClass()
        
        val combatPrefs = combatPreferences
                .getOrPut(player.objectId) { hashMapOf() }
                .getOrPut(player.activeClass) { supportedCombatPrefs.getOrDefault(ClassId.values().first { it.id == player.activeClass }) { DefaultCombatPreferences() }.invoke() }
        
        behavior.combatPreferences = combatPrefs
        behavior.activityPreferences = ActivityPreferences()

        skillPreferences.getOrPut(player.objectId) { hashMapOf() }
        val skillPrefs = skillPreferences.getOrElse(player.objectId) { hashMapOf() }[player.activeClass]

        if (skillPrefs != null) {
            behavior.skillPreferences = skillPrefs
        } else {
            skillPreferences[player.objectId]!![player.activeClass] = behavior.skillPreferences
        }
        return behavior
    }

    private data class PlayerPreferencesDto(val playerId: Int, val classId: Int, val combatPreferences: CombatPreferences, val skillPreferences: SkillPreferences?)
}