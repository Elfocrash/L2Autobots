package dev.l2j.autobots.dao

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.l2j.autobots.Autobot
import dev.l2j.autobots.behaviors.preferences.ActivityPreferences
import dev.l2j.autobots.behaviors.preferences.DefaultCombatPreferences
import dev.l2j.autobots.behaviors.preferences.SocialPreferences
import dev.l2j.autobots.models.AutobotInfo
import dev.l2j.autobots.models.ScheduledSpawnInfo
import dev.l2j.autobots.ui.tabs.IndexBotOrdering
import dev.l2j.autobots.utils.supportedCombatPrefs
import net.sf.l2j.L2DatabaseFactory
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager
import net.sf.l2j.gameserver.data.manager.HeroManager
import net.sf.l2j.gameserver.data.sql.ClanTable
import net.sf.l2j.gameserver.data.xml.PlayerData
import net.sf.l2j.gameserver.enums.actors.ClassId
import net.sf.l2j.gameserver.enums.actors.Sex
import net.sf.l2j.gameserver.model.World
import net.sf.l2j.gameserver.model.actor.player.Appearance
import net.sf.l2j.gameserver.model.pledge.Clan
import net.sf.l2j.gameserver.model.pledge.ClanMember
import net.sf.l2j.gameserver.model.pledge.SubPledge

internal object AutobotsDao {
    private const val Create = "INSERT INTO autobots (obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade, heading, x, y, z, creationDate, modificationDate) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
    private const val Update = "UPDATE autobots SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?," +
            "clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,modificationDate=?,combat_prefs=?,activity_prefs=?,skill_prefs=?,social_prefs=? WHERE obj_Id=?"
    private const val LoadByName = "SELECT * FROM autobots WHERE char_name=? LIMIT 1"
    private const val LoadById = "SELECT * FROM autobots WHERE obj_id=? LIMIT 1"
    private const val Search = "SELECT char_name, level, online, classid, obj_Id, clanid from autobots ORDER BY {{orderterm}} LIMIT ?,?"
    private const val SearchByName = "SELECT char_name, level, online, classid, obj_Id, clanid from autobots where char_name LIKE ? ORDER BY {{orderterm}} LIMIT ?,?"
    private const val CountBots = "SELECT Count(1) from autobots where char_name LIKE ?"
    private const val UpdateOnlineStatus = "Update autobots set online=?"
    private const val GetBotInfoByName = "SELECT char_name, level, online, classid, obj_Id, clanid from autobots where char_name=? LIMIT 1"
    private const val BotNameExists = "SELECT count(1) where char_name=? LIMIT 1"
    private const val GetAllBotInfo = "SELECT char_name, level, online, classid, obj_Id, clanid from autobots"
    private const val DeleteById = "DELETE FROM autobots where obj_Id = ?"
    private const val ScheduledBotSpawn = "SELECT char_name, json_extract(activity_prefs, '\$.loginTime') AS loginTime, json_extract(activity_prefs, '\$.logoutTime') AS logoutTime FROM autobots WHERE JSON_CONTAINS(activity_prefs, '\"Schedule\"', '\$.activityType')"

    val mapper = jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    
    fun createAutobot(autobot: Autobot) {
        try {
            L2DatabaseFactory.getInstance().connection.use { con ->
                con.prepareStatement(Create).use { ps ->
                    ps.setInt(1, autobot.objectId)
                    ps.setString(2, autobot.name)
                    ps.setInt(3, autobot.level)
                    ps.setInt(4, autobot.maxHp)
                    ps.setDouble(5, autobot.currentHp)
                    ps.setInt(6, autobot.maxCp)
                    ps.setDouble(7, autobot.currentCp)
                    ps.setInt(8, autobot.maxMp)
                    ps.setDouble(9, autobot.currentMp)
                    ps.setInt(10, autobot.appearance.face.toInt())
                    ps.setInt(11, autobot.appearance.hairStyle.toInt())
                    ps.setInt(12, autobot.appearance.hairColor.toInt())
                    ps.setInt(13, autobot.appearance.sex.ordinal)
                    ps.setLong(14, autobot.exp)
                    ps.setInt(15, autobot.sp)
                    ps.setInt(16, autobot.karma)
                    ps.setInt(17, autobot.pvpKills)
                    ps.setInt(18, autobot.pkKills)
                    ps.setInt(19, autobot.clanId)
                    ps.setInt(20, autobot.race.ordinal)
                    ps.setInt(21, autobot.classId.id)
                    ps.setLong(22, autobot.deleteTimer)
                    ps.setInt(23, if (autobot.hasDwarvenCraft()) 1 else 0)
                    ps.setString(24, autobot.title)
                    ps.setInt(25, autobot.accessLevel.level)
                    ps.setInt(26, autobot.isOnlineInt)
                    ps.setInt(27, if (autobot.isIn7sDungeon) 1 else 0)
                    ps.setInt(28, autobot.clanPrivileges)
                    ps.setInt(29, if (autobot.wantsPeace()) 1 else 0)
                    ps.setInt(30, autobot.baseClass)
                    ps.setInt(31, if (autobot.isNoble) 1 else 0)
                    ps.setLong(32, 0)
                    ps.setInt(33, autobot.heading)
                    ps.setInt(34, autobot.x)
                    ps.setInt(35, autobot.y)
                    ps.setInt(36, autobot.z)
                    ps.setLong(37, System.currentTimeMillis())
                    ps.setLong(38, System.currentTimeMillis())
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            println(e)
            return
        }
    }

    fun loadByName(name: String): Autobot? {
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(LoadByName).use { ps ->
                ps.setString(1, name)
                ps.executeQuery().use { rs ->
                    
                    if(!rs.next()) {
                        return null
                    }
                    
                    val objectId = rs.getInt("obj_Id")
                    val activeClassId = rs.getInt("classid")
                    val template = PlayerData.getInstance().getTemplate(activeClassId)
                    val app = Appearance(rs.getByte("face"), rs.getByte("hairColor"), rs.getByte("hairStyle"), Sex.values()[rs.getInt("sex")])
                    val player = Autobot(objectId, template, app)
                    player.name = rs.getString("char_name")
                    player.stat.exp = rs.getLong("exp")
                    player.stat.level = rs.getByte("level")
                    player.stat.sp = rs.getInt("sp")
                    player.expBeforeDeath = rs.getLong("expBeforeDeath")
                    player.setWantsPeace(rs.getInt("wantspeace") == 1)
                    player.karma = rs.getInt("karma")
                    player.pvpKills = rs.getInt("pvpkills")
                    player.pkKills = rs.getInt("pkkills")
                    player.setOnlineTime(rs.getLong("onlinetime"))
                    player.setNoble(rs.getInt("nobless") == 1, false)
                    player.clanJoinExpiryTime = rs.getLong("clan_join_expiry_time")
                    if (player.clanJoinExpiryTime < System.currentTimeMillis()) player.clanJoinExpiryTime = 0
                    player.clanCreateExpiryTime = rs.getLong("clan_create_expiry_time")
                    if (player.clanCreateExpiryTime < System.currentTimeMillis()) player.clanCreateExpiryTime = 0
                    player.powerGrade = rs.getInt("power_grade")
                    player.pledgeType = rs.getInt("subpledge")
                    val clanId = rs.getInt("clanid")
                    if (clanId > 0) 
                        player.clan = ClanTable.getInstance().getClan(clanId)
                    if (player.clan != null) {
                        if (player.clan.leaderId != player.objectId) {
                            if (player.powerGrade == 0) player.powerGrade = 5
                            player.clanPrivileges = player.clan.getPriviledgesByRank(player.powerGrade)
                        } else {
                            player.clanPrivileges = Clan.CP_ALL
                            player.powerGrade = 1
                        }
                    } else player.clanPrivileges = Clan.CP_NOTHING
                    player.deleteTimer = rs.getLong("deletetime")
                    player.title = rs.getString("title")
                    player.setAccessLevel(rs.getInt("accesslevel"))
                    player.uptime = System.currentTimeMillis()
                    player.recomHave = rs.getInt("rec_have")
                    player.recomLeft = rs.getInt("rec_left")
                    player.classIndex = 0
                    try {
                        player.baseClass = rs.getInt("base_class")
                    } catch (e: java.lang.Exception) {
                        player.baseClass = activeClassId
                    }
                    
                    if (player.classIndex == 0 && activeClassId != player.baseClass) player.setClassId(player.baseClass) else player.mySetActiveClass(activeClassId)
                    player.apprentice = rs.getInt("apprentice")
                    player.sponsor = rs.getInt("sponsor")
                    player.lvlJoinedAcademy = rs.getInt("lvl_joined_academy")
                    player.setIsIn7sDungeon(rs.getInt("isin7sdungeon") == 1)
                    player.punishment.load(rs.getInt("punish_level"), rs.getLong("punish_timer"))
                    CursedWeaponManager.getInstance().checkPlayer(player)
                    player.allianceWithVarkaKetra = rs.getInt("varka_ketra_ally")
                    player.deathPenaltyBuffLevel = rs.getInt("death_penalty_level")
                    player.position.set(rs.getInt("x"), rs.getInt("y"), rs.getInt("z"), rs.getInt("heading"))
                    if (HeroManager.getInstance().isActiveHero(objectId)) player.isHero = true
                    player.pledgeClass = ClanMember.calculatePledgeClass(player)
                    player.rewardSkills()
                    val currentHp = rs.getDouble("curHp")
                    player.currentCp = rs.getDouble("curCp")
                    player.currentHp = currentHp
                    player.currentMp = rs.getDouble("curMp")
                    if (currentHp < 0.5) {
                        player.setIsDead(true)
                        player.status.stopHpMpRegeneration()
                    }
                    val pet = World.getInstance().getPet(player.objectId)
                    if (pet != null) {
                        player.summon = pet
                        pet.owner = player
                    }

                    val jsonCombatPrefs = rs.getString("combat_prefs")
                    val combatPrefs = if(jsonCombatPrefs.isNullOrEmpty()) {
                        supportedCombatPrefs[player.classId]!!.invoke()
                    }else {
                        mapper.readValue(jsonCombatPrefs, supportedCombatPrefs.getOrDefault(player.classId) { DefaultCombatPreferences() }.invoke().javaClass)
                    }
                    
                    player.combatBehavior.combatPreferences = combatPrefs

                    val jsonActivityPrefs = rs.getString("activity_prefs")
                    val activityPrefs = if(jsonActivityPrefs.isNullOrEmpty()) {
                        ActivityPreferences()
                    }else {
                        mapper.readValue(jsonActivityPrefs)
                    }
                    
                    player.combatBehavior.activityPreferences = activityPrefs
                    
                    val jsonSkillPrefs = rs.getString("skill_prefs")
                    if(!jsonSkillPrefs.isNullOrEmpty()) {
                        val skillPrefs = mapper.readValue(jsonSkillPrefs, player.combatBehavior.skillPreferences.javaClass)
                        player.combatBehavior.skillPreferences = skillPrefs
                    }

                    val jsonSocialPrefs = rs.getString("social_prefs")
                    val socialPrefs = if(jsonSocialPrefs.isNullOrEmpty()) {
                        SocialPreferences()
                    }else {
                        mapper.readValue(jsonSocialPrefs, SocialPreferences::class.java)
                    }

                    player.socialBehavior.socialPreferences = socialPrefs
                    
                    return player
                }
            }
        }
    }
    
    fun saveCombatPreferences(autobot: Autobot){
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement("UPDATE autobots SET combat_prefs=? WHERE obj_Id=?").use { ps ->
                ps.setString(1, mapper.writeValueAsString(autobot.combatBehavior.combatPreferences))
                ps.setInt(2, autobot.objectId)
                ps.executeUpdate()
            }
        }
    }

    fun saveSocialPreferences(autobot: Autobot){
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement("UPDATE autobots SET social_prefs=? WHERE obj_Id=?").use { ps ->
                ps.setString(1, mapper.writeValueAsString(autobot.socialBehavior.socialPreferences))
                ps.setInt(2, autobot.objectId)
                ps.executeUpdate()
            }
        }
    }

    fun saveSkillPreferences(autobot: Autobot){
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement("UPDATE autobots SET skill_prefs=? WHERE obj_Id=?").use { ps ->
                ps.setString(1, mapper.writeValueAsString(autobot.combatBehavior.skillPreferences))
                ps.setInt(2, autobot.objectId)
                ps.executeUpdate()
            }
        }
    }
    
    fun saveActivityPreferences(autobot: Autobot) {
        
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement("UPDATE autobots SET activity_prefs=? WHERE obj_Id=?").use { ps ->
                ps.setString(1, mapper.writeValueAsString(autobot.combatBehavior.activityPreferences))
                ps.setInt(2, autobot.objectId)
                ps.executeUpdate()
            }
        }
    }

    fun saveAutobot(autobot: Autobot) {
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(Update).use { ps ->

                ps.setInt(1, autobot.level)
                ps.setInt(2, autobot.maxHp)
                ps.setDouble(3, autobot.currentHp)
                ps.setInt(4, autobot.maxCp)
                ps.setDouble(5, autobot.currentCp)
                ps.setInt(6, autobot.maxMp)
                ps.setDouble(7, autobot.currentMp)
                ps.setInt(8, autobot.appearance.face.toInt())
                ps.setInt(9, autobot.appearance.hairStyle.toInt())
                ps.setInt(10, autobot.appearance.hairColor.toInt())
                ps.setInt(11, autobot.appearance.sex.ordinal)
                ps.setInt(12, autobot.heading)
                ps.setInt(13, autobot.x)
                ps.setInt(14, autobot.y)
                ps.setInt(15, autobot.z)
                ps.setLong(16, autobot.exp)
                ps.setLong(17, autobot.expBeforeDeath)
                ps.setInt(18, autobot.sp)
                ps.setInt(19, autobot.karma)
                ps.setInt(20, autobot.pvpKills)
                ps.setInt(21, autobot.pkKills)
                ps.setInt(22, autobot.clanId)
                ps.setInt(23, autobot.race.ordinal)
                ps.setInt(24, autobot.classId.id)
                ps.setLong(25, autobot.deleteTimer)
                ps.setString(26, autobot.title)
                ps.setInt(27, autobot.accessLevel.level)
                ps.setInt(28, autobot.isOnlineInt)
                ps.setInt(29, if (autobot.isIn7sDungeon) 1 else 0)
                ps.setInt(30, autobot.clanPrivileges)
                ps.setInt(31, if (autobot.wantsPeace()) 1 else 0)
                ps.setInt(32, autobot.baseClass)
                ps.setLong(33, autobot.powerGrade.toLong())
                ps.setInt(34, autobot.pledgeType)
                ps.setInt(35, autobot.lvlJoinedAcademy)
                ps.setLong(36, autobot.apprentice.toLong())
                ps.setLong(37, autobot.sponsor.toLong())
                ps.setInt(38, autobot.allianceWithVarkaKetra)
                ps.setLong(39, autobot.clanJoinExpiryTime)
                ps.setLong(40, autobot.clanCreateExpiryTime)
                ps.setString(41, autobot.name)
                ps.setLong(42, autobot.deathPenaltyBuffLevel.toLong())
                ps.setLong(43, System.currentTimeMillis())
                ps.setString(44, mapper.writeValueAsString(autobot.combatBehavior.combatPreferences))
                ps.setString(45, mapper.writeValueAsString(autobot.combatBehavior.activityPreferences))
                ps.setString(46, mapper.writeValueAsString(autobot.combatBehavior.skillPreferences))
                ps.setString(47, mapper.writeValueAsString(autobot.socialBehavior.socialPreferences))
                ps.setInt(48, autobot.objectId)
                ps.executeUpdate()
            }
        }
    }
    
    fun deleteBot(objectId: Int) {
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(DeleteById).use { ps ->
                ps.setInt(1, objectId)
                ps.execute()
            }
        }
    }
    
    fun loadScheduledSpawns() : List<ScheduledSpawnInfo> {        
        val list = mutableListOf<ScheduledSpawnInfo>()
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(ScheduledBotSpawn).use { ps ->

                ps.executeQuery().use { rset ->
                    while (rset.next()) {
                        list.add(ScheduledSpawnInfo(rset.getString("char_name"),rset.getString("loginTime").trim('"'),rset.getString("logoutTime").trim('"')))
                    }
                }
            }
        }
        return list
    }
    
    fun searchForAutobots(nameSearch: String = "", pageNumber: Int = 1, pageSize: Int = 10, ordering: IndexBotOrdering = IndexBotOrdering.None) : List<AutobotInfo> {
        
        fun replaceOrdering() : String{
            return when(ordering){
                IndexBotOrdering.None -> "creationDate DESC"
                IndexBotOrdering.LevelAsc -> "level ASC"
                IndexBotOrdering.LevelDesc -> "level DESC"
                IndexBotOrdering.OnAsc -> "online ASC"
                IndexBotOrdering.OnDesc -> "online DESC"
            }
        }        
        
        val list = mutableListOf<AutobotInfo>()
        val query = (if (nameSearch == "") Search else SearchByName)
                .replace("{{orderterm}}", replaceOrdering())
        
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(query).use { ps ->
                
                if(nameSearch != "") {
                    ps.setString(1, "%$nameSearch%")
                    ps.setInt(2, (pageNumber - 1) * pageSize)
                    ps.setInt(3, pageSize)
                }else {
                    ps.setInt(1, (pageNumber - 1) * pageSize)
                    ps.setInt(2, pageSize)
                }
                
                ps.executeQuery().use { rset ->
                    while (rset.next()) {
                        val info = AutobotInfo(
                                rset.getString("char_name"), 
                                rset.getInt("level"), 
                                rset.getInt("online") == 1, 
                                ClassId.values()[rset.getInt("classid")],
                                rset.getInt("obj_Id"),
                                rset.getInt("clanid")
                        )
                        list.add(info)
                    }
                }
            }
        }
        return list
    }
    
    fun getInfoByName(name: String) : AutobotInfo? {
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(GetBotInfoByName).use { ps ->
                ps.setString(1, name)
                
                ps.executeQuery().use { rset ->
                    if(!rset.next()) return null
                    
                    val info = AutobotInfo(
                            rset.getString("char_name"),
                            rset.getInt("level"),
                            rset.getInt("online") == 1,
                            ClassId.values()[rset.getInt("classid")],
                            rset.getInt("obj_Id"),
                            rset.getInt("clanid")
                    )
                    return info
                }
            }
        }
    }
    
    fun botWithNameExists(name: String) : Boolean{
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(BotNameExists).use { ps ->
                ps.setString(1, name)

                ps.executeQuery().use { rset ->
                    rset.next()
                    return rset.getInt(1) == 1
                }
            }
        }
    }

    fun getAllInfo(): List<AutobotInfo> {
        val list = mutableListOf<AutobotInfo>()
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(GetAllBotInfo).use { ps ->
                ps.executeQuery().use { rset ->
                    while (rset.next()) {
                        val info = AutobotInfo(
                                rset.getString("char_name"),
                                rset.getInt("level"),
                                rset.getInt("online") == 1,
                                ClassId.values()[rset.getInt("classid")],
                                rset.getInt("obj_Id"),
                                rset.getInt("clanid")
                        )
                        list.add(info)
                    }
                }
            }
        }
        return list
    }    

    fun getTotalBotCount(filter: String): Int {
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(CountBots).use { ps ->
                ps.setString(1, "%$filter%")
                ps.executeQuery().use { rs ->
                    rs.next()
                    return rs.getInt(1)
                }
            }
        }
    }
    
    fun updateAllBotOnlineStatus(online: Boolean){
        L2DatabaseFactory.getInstance().connection.use { con ->
            con.prepareStatement(UpdateOnlineStatus).use { ps ->
                ps.setInt(1, if(online) 1 else 0)
                ps.executeUpdate()
            }
        }
    }

    fun removeClanMember(autobot: Autobot, clan: Clan) {
        val exMember: ClanMember? = clan.clanMembers.remove(autobot.objectId) ?: return
        val subPledgeId: Int = clan.getLeaderSubPledge(autobot.objectId)
        if (subPledgeId != 0) {
            val pledge: SubPledge? = clan.getSubPledge(subPledgeId)
            if (pledge != null) {
                pledge.leaderId = 0
                clan.updateSubPledgeInDB(pledge)
            }
        }
        
        if (exMember!!.isOnline) {
            val player = exMember.playerInstance
            if (!player.isNoble) player.title = ""

            if (player.activeWarehouse != null) player.activeWarehouse = null
            player.apprentice = 0
            player.sponsor = 0
            player.siegeState = 0.toByte()
            if (player.isClanLeader) {
                player.removeSiegeSkills()
                player.clanCreateExpiryTime = 0
            }
            
            for (skill in clan.clanSkills.values) player.removeSkill(skill.id, false)
            player.sendSkillList()
            player.clan = null
            
            if (exMember.pledgeType != Clan.SUBUNIT_ACADEMY) player.clanJoinExpiryTime = 0
            player.pledgeClass = ClanMember.calculatePledgeClass(player)
            player.broadcastUserInfo()
        } else {
            L2DatabaseFactory.getInstance().connection.use { con ->
                con.prepareStatement("UPDATE autobots SET clanid=0, title='', clan_join_expiry_time=0, clan_create_expiry_time=0, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?").use { ps ->
                    ps.setInt(1, exMember.objectId)
                    ps.executeUpdate()
                    autobot.clan = null
                }
            }
        }
    }
}