package dev.l2j.autobots.behaviors.attributes

import dev.l2j.autobots.behaviors.preferences.PetOwnerPreferences
import dev.l2j.autobots.extensions.getCombatBehavior
import dev.l2j.autobots.extensions.useMagicSkill
import dev.l2j.autobots.skills.BotSkill
import net.sf.l2j.gameserver.enums.IntentionType
import net.sf.l2j.gameserver.geoengine.GeoEngine
import net.sf.l2j.gameserver.model.actor.Creature
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.model.actor.instance.Servitor
import net.sf.l2j.gameserver.model.item.instance.ItemInstance
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed

internal interface PetOwner {
    
    val summonInfo: Pair<BotSkill, MiscItem?>
    
    val petBuffs: Array<IntArray>
    
    fun summonPet(bot: Player){
        if(bot.hasServitor()) {
            return
        }
        
        val skill = bot.getSkill(summonInfo.first.skillId) ?: return

        if(summonInfo.second != null) {
            if (bot.inventory.getItemByItemId(summonInfo.second!!.miscItemId(bot)) != null) {
                if (bot.inventory.getItemByItemId(summonInfo.second!!.miscItemId(bot)).count <= summonInfo.second!!.minimumAmountBeforeGive) {
                    bot.inventory.addItem("", summonInfo.second!!.miscItemId(bot), summonInfo.second!!.countToGive, bot, null)
                }
            } else {
                bot.inventory.addItem("", summonInfo.second!!.miscItemId(bot), summonInfo.second!!.countToGive, bot, null)
                val consumable: ItemInstance = bot.inventory.getItemByItemId(summonInfo.second!!.miscItemId(bot))
                if (consumable.isEquipable) bot.inventory.equipItem(consumable)
            }
        }
        
        if(summonInfo.first.condition(bot, skill, bot.target as Creature?)) {
            bot.useMagicSkill(skill)
        }        
    }
    
    fun petAssist(bot: Player) {
        if (bot.getCombatBehavior().combatPreferences is PetOwnerPreferences) {
            if (bot.hasServitor()) {
                val prefs = bot.getCombatBehavior().combatPreferences as PetOwnerPreferences
                if (prefs.petAssists) {
                    if(!bot.hasServitor())
                        return

                    if(bot.target == null)
                        return

                    val servitor = bot.summon as Servitor
                    servitor.target = bot.target

                    if(servitor.target == null) return
                    if (servitor.target.isAutoAttackable(servitor)) {
                        if (servitor.target is Player && ((servitor.target as Player).isCursedWeaponEquipped && servitor.level < 21 || bot.isCursedWeaponEquipped && bot.level < 21)) {
                            bot.sendPacket(ActionFailed.STATIC_PACKET)
                            return
                        }
                        if (servitor.target != null && GeoEngine.getInstance().canSeeTarget(servitor, servitor.target)) {

                            if(prefs.petUsesShots) {
                                if (bot.inventory.getItemByItemId(prefs.petShotId) != null) {
                                    if (bot.inventory.getItemByItemId(prefs.petShotId).count <= 5) {
                                        bot.inventory.addItem("", prefs.petShotId, 100, bot, null)
                                    }
                                } else {
                                    bot.inventory.addItem("", prefs.petShotId, 100, bot, null)
                                }                                
                                
                                if(!bot.autoSoulShot.contains(prefs.petShotId)) {
                                    bot.addAutoSoulShot(prefs.petShotId)
                                    bot.rechargeShots(true, true)
                                }
                            }else {
                                if(bot.autoSoulShot.contains(prefs.petShotId)) {
                                    bot.removeAutoSoulShot(prefs.petShotId)
                                }
                            }
                            servitor.ai.setIntention(IntentionType.ATTACK, servitor.target)
                        }
                    } else {
                        if (servitor.target != null && !GeoEngine.getInstance().canSeeTarget(servitor, servitor.target)) servitor.ai.setIntention(IntentionType.FOLLOW, this)
                    }
                } else {
                    val servitor = bot.summon as Servitor
                    if (!servitor.followStatus) {
                        servitor.followOwner()
                    }
                }
            }
        }
    }
}