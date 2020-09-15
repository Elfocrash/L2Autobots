package dev.l2j.autobots.utils

import dev.l2j.autobots.AutobotData
import dev.l2j.autobots.behaviors.CombatBehavior
import dev.l2j.autobots.behaviors.classes.*
import dev.l2j.autobots.behaviors.classes.pre.*
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.extensions.getCombatBehavior
import kotlinx.coroutines.delay
import net.sf.l2j.commons.math.MathUtil
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.ItemTable
import net.sf.l2j.gameserver.data.SkillTable
import net.sf.l2j.gameserver.data.manager.RaidBossManager
import net.sf.l2j.gameserver.data.sql.SpawnTable
import net.sf.l2j.gameserver.data.xml.NpcData
import net.sf.l2j.gameserver.enums.actors.ClassId
import net.sf.l2j.gameserver.enums.actors.ClassRace
import net.sf.l2j.gameserver.enums.actors.Sex
import net.sf.l2j.gameserver.enums.items.ShotType
import net.sf.l2j.gameserver.enums.skills.L2EffectType
import net.sf.l2j.gameserver.model.WorldObject
import net.sf.l2j.gameserver.model.WorldRegion
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.Monster
import net.sf.l2j.gameserver.model.actor.player.Appearance
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate
import net.sf.l2j.gameserver.model.item.instance.ItemInstance
import net.sf.l2j.gameserver.model.item.kind.Item
import net.sf.l2j.gameserver.model.itemcontainer.Inventory
import net.sf.l2j.gameserver.model.location.Location
import net.sf.l2j.gameserver.model.spawn.L2Spawn
import net.sf.l2j.gameserver.network.SystemMessageId
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive
import java.awt.Color
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin


internal fun getDefaultFighterBuffs(): Array<IntArray> {
    return arrayOf(intArrayOf(1204, 2), intArrayOf(1040, 3), intArrayOf(1035, 4), intArrayOf(1045, 6), intArrayOf(1068, 3), intArrayOf(1062, 2), intArrayOf(1086, 2), intArrayOf(1077, 3), intArrayOf(1388, 3), intArrayOf(1036, 2), intArrayOf(274, 1), intArrayOf(273, 1), intArrayOf(268, 1), intArrayOf(271, 1), intArrayOf(267, 1), intArrayOf(349, 1), intArrayOf(264, 1), intArrayOf(269, 1), intArrayOf(364, 1), intArrayOf(1363, 1), intArrayOf(4699, 5), intArrayOf(310, 1), intArrayOf(1268, 4))
}

internal fun getDefaultMageBuffs(): Array<IntArray> {
    return arrayOf(intArrayOf(1204, 2), intArrayOf(1040, 3), intArrayOf(1035, 4), intArrayOf(4351, 6), intArrayOf(1036, 2), intArrayOf(1045, 6), intArrayOf(1303, 2), intArrayOf(1085, 3), intArrayOf(1062, 2), intArrayOf(1059, 3), intArrayOf(1389, 3), intArrayOf(273, 1), intArrayOf(276, 1), intArrayOf(365, 1), intArrayOf(264, 1), intArrayOf(268, 1), intArrayOf(267, 1), intArrayOf(349, 1), intArrayOf(1413, 1), intArrayOf(4703, 4))
}

internal fun Creature.hasEffect(skillId: Int) : Boolean {
    return getFirstEffect(skillId) != null
}

internal suspend fun giveItemsByClassAndLevel(player: Player, weaponEnchant: Int = 0, armorEnchant: Int = 0, jewelEnchant: Int = 0, addRealistically: Boolean = false){
    val equipment = AutobotData.equipment.firstOrNull { player.level >= it.minLevel && player.level <= it.maxLevel && player.classId == it.classId }
    
    if(equipment == null) {
        println("No equipment found for class ${player.classId} and level ${player.level}")
        return
    }

    if(equipment.head != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_HEAD) != equipment.head) giveAndEquipItem(player, equipment.head, armorEnchant)
    
    if(addRealistically) {
        delay(1000)
        player.broadcastCharInfo()
    }
    
    if(equipment.chest != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_CHEST) != equipment.chest) giveAndEquipItem(player, equipment.chest, armorEnchant)

    if(addRealistically) {
        delay(1000)
        player.broadcastCharInfo()
    }
    
    if(equipment.legs != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEGS) != equipment.legs && ItemTable.getInstance().getTemplate(equipment.chest).bodyPart != Item.SLOT_FULL_ARMOR) giveAndEquipItem(player, equipment.legs, armorEnchant)

    if(addRealistically) {
        delay(1000)
        player.broadcastCharInfo()
    }
    
    if(equipment.hands != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES) != equipment.hands) giveAndEquipItem(player, equipment.hands, armorEnchant)

    if(addRealistically) {
        delay(1000)
        player.broadcastCharInfo()
    }
    
    if(equipment.feet != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_FEET) != equipment.feet) giveAndEquipItem(player, equipment.feet, armorEnchant)

    if(addRealistically) {
        delay(1000)
        player.broadcastCharInfo()
    }
    
    if(equipment.rightHand != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_RHAND) != equipment.rightHand) giveAndEquipItem(player, equipment.rightHand, weaponEnchant)

    if(addRealistically) {
        delay(1000)
        player.broadcastCharInfo()
    }
    
    if(equipment.leftHand != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LHAND) != equipment.leftHand && ItemTable.getInstance().getTemplate(equipment.rightHand).bodyPart != Item.SLOT_LR_HAND) giveAndEquipItem(player, equipment.leftHand, weaponEnchant)
    
    if(equipment.neck != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_NECK) != equipment.neck) giveAndEquipItem(player, equipment.neck, jewelEnchant)    
    if(equipment.leftEar != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEAR) != equipment.leftEar) giveAndEquipItem(player, equipment.leftEar, jewelEnchant)    
    if(equipment.rightEar != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_REAR) != equipment.rightEar) giveAndEquipItem(player, equipment.rightEar, jewelEnchant)    
    if(equipment.leftRing != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER) != equipment.leftRing) giveAndEquipItem(player, equipment.leftRing, jewelEnchant)    
    if(equipment.rightRing != 0 && player.inventory.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER) != equipment.rightRing) giveAndEquipItem(player, equipment.rightRing, jewelEnchant)
    
    player.broadcastCharInfo()
}

internal fun giveAndEquipItem(player: Player, itemId: Int, enchant: Int, broadcast: Boolean = false){
    player.inventory.addItem("AutobotItem", itemId, 1, player, null)
    val item: ItemInstance = player.inventory.getItemByItemId(itemId)
    item.enchantLevel = enchant
    player.inventory.equipItemAndRecord(item)
    player.inventory.reloadEquippedItems()
    if(broadcast) {
        player.broadcastCharInfo()
    }
}

internal fun getBehaviorByClassId(classId: ClassId, autobot: Player, combatPreferences: CombatPreferences): CombatBehavior {
    return (allBehaviors[classId] ?: error("Not implemented class id")).invoke(autobot, combatPreferences)
}

internal val allBehaviors = mapOf<ClassId, (Player, CombatPreferences) -> CombatBehavior>(

        //Pair(ClassId.CLERIC, { bot, prefs -> ClericBehavior(bot, prefs)}),
        //Pair(ClassId.SHILLIEN_ORACLE, { bot, prefs -> ShillienOracleBehavior(bot, prefs)}),
        //Pair(ClassId.ELVEN_ORACLE, { bot, prefs -> ElvenOracleBehavior(bot, prefs)}),
        
        Pair(ClassId.HUMAN_FIGHTER, { bot, prefs -> FighterBehavior(bot, prefs) }),
        Pair(ClassId.WARRIOR, { bot, prefs -> WarriorBehavior(bot, prefs) }),
        Pair(ClassId.KNIGHT, { bot, prefs -> KnightBehavior(bot, prefs) }),
        Pair(ClassId.ROGUE, { bot, prefs -> RogueBehavior(bot, prefs) }),
        Pair(ClassId.HUMAN_MYSTIC, { bot, prefs -> MysticBehavior(bot, prefs) }),
        Pair(ClassId.HUMAN_WIZARD, { bot, prefs -> HumanWizardBehavior(bot, prefs)}),
        Pair(ClassId.ELVEN_FIGHTER, { bot, prefs -> FighterBehavior(bot, prefs)}),
        Pair(ClassId.ELVEN_MYSTIC, { bot, prefs -> MysticBehavior(bot, prefs)}),
        Pair(ClassId.ELVEN_KNIGHT, { bot, prefs -> KnightBehavior(bot, prefs)}),
        Pair(ClassId.ELVEN_SCOUT, { bot, prefs -> RogueBehavior(bot, prefs)}),
        Pair(ClassId.ELVEN_WIZARD, { bot, prefs -> ElvenWizardBehavior(bot, prefs)}),        
        Pair(ClassId.DARK_FIGHTER, { bot, prefs -> FighterBehavior(bot, prefs)}),
        Pair(ClassId.PALUS_KNIGHT, { bot, prefs -> KnightBehavior(bot, prefs)}),
        Pair(ClassId.ASSASSIN, { bot, prefs -> RogueBehavior(bot, prefs)}),        
        Pair(ClassId.DARK_MYSTIC, { bot, prefs -> MysticBehavior(bot, prefs)}),
        Pair(ClassId.DARK_WIZARD, { bot, prefs -> DarkElvenWizardBehavior(bot, prefs)}),
        Pair(ClassId.ORC_FIGHTER, { bot, prefs -> OrcFighterBehavior(bot, prefs)}),
        Pair(ClassId.ORC_RAIDER, { bot, prefs -> OrcRaiderBehavior(bot, prefs)}),
        Pair(ClassId.MONK, { bot, prefs -> MonkBehavior(bot, prefs)}),
        Pair(ClassId.ORC_MYSTIC, { bot, prefs -> OrcMysticBehavior(bot, prefs)}),
        Pair(ClassId.ORC_SHAMAN, { bot, prefs -> OrcMysticBehavior(bot, prefs)}),
        Pair(ClassId.DWARVEN_FIGHTER, { bot, prefs -> MaestroBehavior(bot, prefs)}),
        Pair(ClassId.ARTISAN, { bot, prefs -> FortuneSeekerBehavior(bot, prefs)}),
        Pair(ClassId.SCAVENGER, { bot, prefs -> FortuneSeekerBehavior(bot, prefs)}),
        Pair(ClassId.SPELLHOWLER, { bot, prefs -> StormScreamerBehavior(bot, prefs)}),
        Pair(ClassId.STORM_SCREAMER, { bot, prefs -> StormScreamerBehavior(bot, prefs)}),
        Pair(ClassId.SPELLSINGER, { bot, prefs -> MysticMuseBehavior(bot, prefs)}),
        Pair(ClassId.MYSTIC_MUSE, { bot, prefs -> MysticMuseBehavior(bot, prefs)}),
        Pair(ClassId.SORCERER, { bot, prefs -> ArchmageBehavior(bot, prefs)}),
        Pair(ClassId.ARCHMAGE, { bot, prefs -> ArchmageBehavior(bot, prefs)}),
        Pair(ClassId.NECROMANCER, { bot, prefs -> SoultakerBehavior(bot, prefs)}),
        Pair(ClassId.SOULTAKER, { bot, prefs -> SoultakerBehavior(bot, prefs)}),
        Pair(ClassId.HAWKEYE, { bot, prefs -> SagittariusBehavior(bot, prefs)}),
        Pair(ClassId.SAGGITARIUS, { bot, prefs -> SagittariusBehavior(bot, prefs)}),
        Pair(ClassId.SILVER_RANGER, { bot, prefs -> MoonlightSentinelBehavior(bot, prefs)}),
        Pair(ClassId.MOONLIGHT_SENTINEL, { bot, prefs -> MoonlightSentinelBehavior(bot, prefs)}),
        Pair(ClassId.PHANTOM_RANGER, { bot, prefs -> GhostSentinelBehavior(bot, prefs)}),
        Pair(ClassId.GHOST_SENTINEL, { bot, prefs -> GhostSentinelBehavior(bot, prefs)}),
        Pair(ClassId.TREASURE_HUNTER, { bot, prefs -> AdventurerBehavior(bot, prefs)}),
        Pair(ClassId.ADVENTURER, { bot, prefs -> AdventurerBehavior(bot, prefs)}),
        Pair(ClassId.PLAINS_WALKER, { bot, prefs -> WindRiderBehavior(bot, prefs)}),
        Pair(ClassId.WIND_RIDER, { bot, prefs -> WindRiderBehavior(bot, prefs)}),
        Pair(ClassId.ABYSS_WALKER, { bot, prefs -> GhostHunterBehavior(bot, prefs)}),
        Pair(ClassId.GHOST_HUNTER, { bot, prefs -> GhostHunterBehavior(bot, prefs)}),
        Pair(ClassId.BISHOP, { bot, prefs -> CardinalBehavior(bot, prefs)}),
        Pair(ClassId.CARDINAL, { bot, prefs -> CardinalBehavior(bot, prefs)}),
        Pair(ClassId.OVERLORD, { bot, prefs -> DominatorBehavior(bot, prefs)}),
        Pair(ClassId.DOMINATOR, { bot, prefs -> DominatorBehavior(bot, prefs)}),
        Pair(ClassId.DESTROYER, { bot, prefs -> TitanBehavior(bot, prefs)}),
        Pair(ClassId.TITAN, { bot, prefs -> TitanBehavior(bot, prefs)}),
        Pair(ClassId.GLADIATOR, { bot, prefs -> DuelistBehavior(bot, prefs as DuelistCombatPreferences)}),
        Pair(ClassId.DUELIST, { bot, prefs -> DuelistBehavior(bot, prefs as DuelistCombatPreferences)}),
        Pair(ClassId.TYRANT, { bot, prefs -> GrandKhavatariBehavior(bot, prefs)}),
        Pair(ClassId.GRAND_KHAVATARI, { bot, prefs -> GrandKhavatariBehavior(bot, prefs)}),
        Pair(ClassId.WARLORD, { bot, prefs -> DreadnoughtBehavior(bot, prefs)}),
        Pair(ClassId.DREADNOUGHT, { bot, prefs -> DreadnoughtBehavior(bot, prefs)}),
        Pair(ClassId.PALADIN, { bot, prefs -> PhoenixKnightBehavior(bot, prefs)}),
        Pair(ClassId.PHOENIX_KNIGHT, { bot, prefs -> PhoenixKnightBehavior(bot, prefs)}),
        Pair(ClassId.DARK_AVENGER, { bot, prefs -> HellKnightBehavior(bot, prefs)}),
        Pair(ClassId.HELL_KNIGHT, { bot, prefs -> HellKnightBehavior(bot, prefs)}),
        Pair(ClassId.TEMPLE_KNIGHT, { bot, prefs -> EvasTemplarBehavior(bot, prefs)}),
        Pair(ClassId.EVAS_TEMPLAR, { bot, prefs -> EvasTemplarBehavior(bot, prefs)}),
        Pair(ClassId.SHILLIEN_KNIGHT, { bot, prefs -> ShillienTemplarBehavior(bot, prefs)}),
        Pair(ClassId.SHILLIEN_TEMPLAR, { bot, prefs -> ShillienTemplarBehavior(bot, prefs)}),
        Pair(ClassId.BLADEDANCER, { bot, prefs -> SpectralDancerBehavior(bot, prefs)}),
        Pair(ClassId.SPECTRAL_DANCER, { bot, prefs -> SpectralDancerBehavior(bot, prefs)}),
        Pair(ClassId.SWORD_SINGER, { bot, prefs -> SwordMuseBehavior(bot, prefs)}),
        Pair(ClassId.SWORD_MUSE, { bot, prefs -> SwordMuseBehavior(bot, prefs)}),
        Pair(ClassId.BOUNTY_HUNTER, { bot, prefs -> FortuneSeekerBehavior(bot, prefs)}),
        Pair(ClassId.FORTUNE_SEEKER, { bot, prefs -> FortuneSeekerBehavior(bot, prefs)}),
        Pair(ClassId.WARSMITH, { bot, prefs -> MaestroBehavior(bot, prefs)}),
        Pair(ClassId.MAESTRO, { bot, prefs -> MaestroBehavior(bot, prefs)})
)

//Warning. Reading this map, might give you a seizure. If you get a headache please speak to a pathologist
internal val supportedCombatPrefs = mapOf<ClassId, () -> CombatPreferences>(

        Pair(ClassId.HUMAN_FIGHTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.PHOENIX_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.WARRIOR, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.PHOENIX_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.GLADIATOR, { DuelistCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DUELIST) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.WARLORD, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DREADNOUGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.KNIGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.PHOENIX_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.PALADIN, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.PHOENIX_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DARK_AVENGER, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.HELL_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ROGUE, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ADVENTURER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.TREASURE_HUNTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ADVENTURER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.HAWKEYE, { ArcherCombatPreferences(500, true, 700, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SAGGITARIUS) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.HUMAN_MYSTIC, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SOULTAKER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.HUMAN_WIZARD, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SOULTAKER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.SORCERER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ARCHMAGE) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.NECROMANCER, { PetOwnerCombatPreferences(false, true, false, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SOULTAKER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.WARLOCK, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ARCANA_LORD) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.CLERIC, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.CARDINAL) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.BISHOP, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.CARDINAL) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.PROPHET, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.HIEROPHANT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ELVEN_FIGHTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.ELVEN_KNIGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.TEMPLE_KNIGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SWORD_SINGER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SWORD_MUSE) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ELVEN_SCOUT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MOONLIGHT_SENTINEL) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.PLAINS_WALKER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.WIND_RIDER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SILVER_RANGER, { ArcherCombatPreferences(500, true, 700, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MOONLIGHT_SENTINEL) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ELVEN_MYSTIC, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MYSTIC_MUSE) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.ELVEN_WIZARD, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MYSTIC_MUSE) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.SPELLSINGER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MYSTIC_MUSE) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ELEMENTAL_SUMMONER, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ELEMENTAL_MASTER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ELVEN_ORACLE, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_SAINT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.ELVEN_ELDER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_SAINT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DARK_FIGHTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.PALUS_KNIGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.SHILLIEN_KNIGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.BLADEDANCER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SPECTRAL_DANCER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ASSASSIN, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GHOST_HUNTER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.ABYSS_WALKER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GHOST_HUNTER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.PHANTOM_RANGER, { ArcherCombatPreferences(500, true, 700, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GHOST_SENTINEL) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DARK_MYSTIC, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.STORM_SCREAMER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.DARK_WIZARD, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.STORM_SCREAMER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.SPELLHOWLER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.STORM_SCREAMER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.PHANTOM_SUMMONER, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SPECTRAL_MASTER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SHILLIEN_ORACLE, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_SAINT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.SHILLIEN_ELDER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_SAINT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ORC_FIGHTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.TITAN) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.ORC_RAIDER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GRAND_KHAVATARI) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.DESTROYER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.TITAN) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.TYRANT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GRAND_KHAVATARI) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.MONK, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GRAND_KHAVATARI) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.ORC_MYSTIC, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DOMINATOR) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.ORC_SHAMAN, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DOMINATOR) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.OVERLORD, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DOMINATOR) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.WARCRYER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DOOMCRYER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DWARVEN_FIGHTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.FORTUNE_SEEKER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, false, false, false, false) }),
        Pair(ClassId.SCAVENGER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.FORTUNE_SEEKER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.BOUNTY_HUNTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.FORTUNE_SEEKER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ARTISAN, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MAESTRO) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, false, AutobotData.settings.useGreaterHealingPots, false) }),
        Pair(ClassId.WARSMITH, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MAESTRO) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DUELIST, { DuelistCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DUELIST) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DREADNOUGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DREADNOUGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.PHOENIX_KNIGHT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.PHOENIX_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.HELL_KNIGHT, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true,AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.HELL_KNIGHT) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SAGGITARIUS, { ArcherCombatPreferences(500, true, 700, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SAGGITARIUS) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.STORM_SCREAMER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.STORM_SCREAMER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.MYSTIC_MUSE, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MYSTIC_MUSE) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ARCHMAGE, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ARCHMAGE) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SOULTAKER, { PetOwnerCombatPreferences(false, true, false, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SOULTAKER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.MOONLIGHT_SENTINEL, { ArcherCombatPreferences(500, true, 700, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MOONLIGHT_SENTINEL) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.GHOST_SENTINEL, { ArcherCombatPreferences(500, true, 700, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GHOST_SENTINEL) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ADVENTURER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ADVENTURER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.WIND_RIDER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.WIND_RIDER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.GHOST_HUNTER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GHOST_HUNTER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.CARDINAL, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.CARDINAL) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DOMINATOR, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DOMINATOR) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.TITAN, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.TITAN) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),        
        Pair(ClassId.GRAND_KHAVATARI, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.GRAND_KHAVATARI) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),        
        Pair(ClassId.EVAS_TEMPLAR, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SHILLIEN_TEMPLAR, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_TEMPLAR) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SPECTRAL_DANCER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SPECTRAL_DANCER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SWORD_MUSE, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SWORD_MUSE) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.FORTUNE_SEEKER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.FORTUNE_SEEKER) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.MAESTRO, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.MAESTRO) { getDefaultFighterBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ARCANA_LORD, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ARCANA_LORD) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.HIEROPHANT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.HIEROPHANT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.EVAS_SAINT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.EVAS_SAINT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.ELEMENTAL_MASTER, { PetOwnerCombatPreferences(true, true, true, beastSoulShotId, true, AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.ELEMENTAL_MASTER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.SHILLIEN_SAINT, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.SHILLIEN_SAINT) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) }),
        Pair(ClassId.DOOMCRYER, { DefaultCombatPreferences(AutobotData.settings.targetingRange, AutobotData.settings.attackPlayerType, AutobotData.buffs.getOrElse(ClassId.DOOMCRYER) { getDefaultMageBuffs() }, AutobotData.settings.targetingPreference, AutobotData.settings.useManaPots, AutobotData.settings.useQuickHealingPots, AutobotData.settings.useGreaterHealingPots, AutobotData.settings.useGreaterCpPots) })
)

internal fun getRandomAppearance(race: ClassRace): Appearance {

    val randomSex = if (Rnd.get(1, 2) == 1) Sex.MALE else Sex.FEMALE
    val hairStyle = Rnd.get(0, if (randomSex == Sex.MALE) 4 else 6)
    val hairColor = Rnd.get(0, 3)
    val faceId = Rnd.get(0, 2)

    return Appearance(faceId.toByte(), hairColor.toByte(), hairStyle.toByte(), randomSex)
}

internal fun getSupportedClassesForLevel(level: Int) : List<ClassId>{
    return getSupportedClasses.filter { (level in 1..19 && it.level() == 0 ) || (level in 20..39 && it.level() == 1 ) || (level in 40..75 && it.level() == 2 ) || (level in 76..81 && it.level() == 3 ) }
}

internal val getSupportedClasses = allBehaviors.keys.toList() 

internal fun createCirclePacket(name: String?, x: Int, y: Int, z: Int, radius: Int, color: Color?, initX: Int, initY: Int): ExServerPrimitive? {
    val packet = ExServerPrimitive(name, initX, initY, -65535)
    for (i in 0..359) {
        val newX = (x + radius * cos(Math.toRadians(i.toDouble()))).toInt()
        val newY = (y + radius * sin(Math.toRadians(i.toDouble()))).toInt()
        val newXT = (x + radius * cos(Math.toRadians(i + 1.toDouble()))).toInt()
        val newYT = (y + radius * sin(Math.toRadians(i + 1.toDouble()))).toInt()
        val loc = Location(newX, newY, z)
        val locPlus = Location(newXT, newYT, z)
        packet.addLine(color, loc, locPlus)
    }
    return packet
}


internal fun getShotId(bot: Player): Int {
    val playerLevel: Int = bot.level
    if (playerLevel < 20) return if (bot.getCombatBehavior().getShotType() == ShotType.SOULSHOT) 1835 else 3947
    if (playerLevel in 20..39) return if (bot.getCombatBehavior().getShotType() == ShotType.SOULSHOT) 1463 else 3948
    if (playerLevel in 40..51) return if (bot.getCombatBehavior().getShotType() == ShotType.SOULSHOT) 1464 else 3949
    if (playerLevel in 52..60) return if (bot.getCombatBehavior().getShotType() == ShotType.SOULSHOT) 1465 else 3950
    if (playerLevel in 61..75) return if (bot.getCombatBehavior().getShotType() == ShotType.SOULSHOT) 1466 else 3951
    return if (playerLevel >= 76) if (bot.getCombatBehavior().getShotType() == ShotType.SOULSHOT) 1467 else 3952 else 0
}

internal fun getArrowId(bot: Player): Int {
    val playerLevel: Int = bot.level
    
    if (playerLevel < 20) return 17 // wooden arrow
    if (playerLevel in 20..39) return 1341 // bone arrow
    if (playerLevel in 40..51) return 1342 // steel arrow
    if (playerLevel in 52..60) return 1343 // Silver arrow
    if (playerLevel in 61..75) return 1344 // Mithril Arrow
    return if (playerLevel >= 76) 1345 else 0 // shining
}

internal fun clearCircle(player: Player, circleName: String){
    val packet = ExServerPrimitive(circleName)
    packet.addPoint(Color.WHITE, 0, 0, 0)
    player.sendPacket(packet)
}

internal fun distance(x1: Int, y1: Int,
             z1: Int, x2: Int,
             y2: Int, z2: Int) : Double {
    return ((x2 - x1.toDouble()).pow(2.0) + (y2 - y1.toDouble()).pow(2.0) + (z2 - z1.toDouble()).pow(2.0) * 1.0).pow(0.5)
}

internal fun distance(loc1: Location, loc2: Location) : Double {
    return distance(loc1.x, loc1.y, loc1.z, loc2.x, loc2.y, loc2.z)
}

internal fun distance(creature1: Creature, creature2: Creature): Double{
    return distance(creature1.x, creature1.y, creature1.z, creature2.x, creature2.y, creature2.z)
}


internal fun Creature.isFullHealth(): Boolean {
    return getHealthPercentage() > 95
}

internal fun Creature.getHealthPercentage(): Double {
    return currentHp * 100.0f / maxHp
}

internal fun Creature.getManaPercentage(): Double {
    return currentMp * 100.0f / maxMp
}

internal fun Creature.getCpPercentage(): Double {
    return currentCp * 100.0f / maxCp
}

internal fun Creature.applyBuffs(vararg buffs: IntArray){
    val activeEffects = allEffects.filter { it.effectType === L2EffectType.BUFF }.map { it.skill.id to it }.toMap()
    
    for (buff in buffs) {
        if (!activeEffects.containsKey(buff[0]) && activeEffects.size < maxBuffCount) {
            SkillTable.getInstance().getInfo(buff[0], buff[1]).getEffects(this, this)
            continue
        }
        
        if (activeEffects.containsKey(buff[0]) && activeEffects[buff[0]]!!.period - activeEffects[buff[0]]!!.time <= 5) {
            SkillTable.getInstance().getInfo(buff[0], buff[1]).getEffects(this, this)
        }
    }
}

internal fun spawn(activeChar: Player, monsterId: String, respawnTime: Int = 60, permanent: Boolean = false) {
    var monsterId = monsterId
    var target = activeChar.target
    if (target == null) target = activeChar
    val template: NpcTemplate
    if (monsterId.matches(Regex("[0-9]*"))) // First parameter was an ID number
        template = NpcData.getInstance().getTemplate(monsterId.toInt()) else  // First parameter wasn't just numbers, so go by name not ID
    {
        monsterId = monsterId.replace('_', ' ')
        template = NpcData.getInstance().getTemplateByName(monsterId)
    }
    try {
        val spawn = L2Spawn(template)
        spawn.setLoc(target.x + Rnd.get(-150, 150), target.y+ Rnd.get(-150, 150), target.z, activeChar.heading)
        spawn.respawnDelay = respawnTime
        if (template.isType("RaidBoss")) {
            if (RaidBossManager.getInstance().getBossSpawn(spawn.npcId) != null) {
                activeChar.sendMessage("You cannot spawn another instance of " + template.name + ".")
                return
            }
            spawn.respawnMinDelay = 43200
            spawn.respawnMaxDelay = 129600
            RaidBossManager.getInstance().addNewSpawn(spawn, 0, 0.0, 0.0, permanent)
        } else {
            SpawnTable.getInstance().addSpawn(spawn, permanent)
            spawn.doSpawn(false)
            if (permanent) spawn.setRespawnState(true)
        }
        if (!permanent) spawn.setRespawnState(false)
        activeChar.sendMessage("Spawned " + template.name + ".")
    } catch (e: java.lang.Exception) {
        activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT)
    }
}

internal fun Creature.getKnownTargatablesInRadius(radius: Int, attackPlayerType: AttackPlayerType, condition: (Creature) -> Boolean = {true}): List<Creature> {
    val region: WorldRegion = region ?: return emptyList()

    fun shouldTarget(it: WorldObject, attackPlayerType: AttackPlayerType) : Boolean {
        return when(attackPlayerType){
            AttackPlayerType.None -> it is Monster
            AttackPlayerType.Flagged -> (it is Player && (it.pvpFlag > 0 || it.karma > 0)) || it is Monster
            AttackPlayerType.Innocent -> it is Player || it is Monster
        }
    }

    return region.surroundingRegions.flatMap { it.objects }.filterIsInstance<Creature>().filter { condition(it) && shouldTarget(it, attackPlayerType) && !it.isGM && !it.isDead && it !== this && MathUtil.checkIfInRange(radius, this, it, true)}
}

