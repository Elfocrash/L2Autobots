package dev.l2j.autobots.ui

import dev.l2j.autobots.Autobot
import dev.l2j.autobots.AutobotData
import dev.l2j.autobots.AutobotsManager
import dev.l2j.autobots.AutobotsNameService
import dev.l2j.autobots.behaviors.preferences.*
import dev.l2j.autobots.behaviors.preferences.skills.DuelistSkillPreferences
import dev.l2j.autobots.dao.AutobotsDao
import dev.l2j.autobots.extensions.getControllingBot
import dev.l2j.autobots.extensions.isControllingBot
import dev.l2j.autobots.models.AutobotInfo
import dev.l2j.autobots.models.ChatType
import dev.l2j.autobots.models.CreateBotDetails
import dev.l2j.autobots.ui.UiComponents.checkboxComponent
import dev.l2j.autobots.ui.UiComponents.comboboxComponent
import dev.l2j.autobots.ui.UiComponents.textbotComponent
import dev.l2j.autobots.ui.html.HtmlAlignment
import dev.l2j.autobots.ui.states.*
import dev.l2j.autobots.ui.tabs.*
import dev.l2j.autobots.utils.IconsTable
import dev.l2j.autobots.utils.getSupportedClassesForLevel
import net.sf.l2j.gameserver.data.ItemTable
import net.sf.l2j.gameserver.data.SkillTable
import net.sf.l2j.gameserver.data.sql.ClanTable
import net.sf.l2j.gameserver.data.xml.MapRegionData
import net.sf.l2j.gameserver.enums.actors.StoreType
import net.sf.l2j.gameserver.model.actor.Player
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard
import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

internal object AutobotsUi {
    private const val chatPageSize = 15

    internal fun readFileText(fileName: String) = AutobotsNameService.javaClass.classLoader.getResource(fileName)!!.readText()
    
    fun loadLastActive(player: Player){
        when(val lastActiveState = ViewStates.getActiveState(player)){
            is IndexViewState -> {
                loadIndex(player)
                return
            }
            is BotDetailsViewState -> {
                loadBotDetails(player, AutobotsManager.getBotFromOnlineOrDb(lastActiveState.activeBot.name)!!)
                return
            }
            is CreateBotViewState -> {
                loadCreateBot(player)
            }
            is SettingsViewState -> {
                loadSettings(player)
            }
        }
    }
    
    fun loadIndex(player: Player){
        val state = ViewStates.indexViewState(player)
        val nameSearch = state.nameToSearch
        val pageNumber = state.pagination.first
        val pageSize = state.pagination.second
        val totalPages = totalPageCount(nameSearch, pageSize)
        val html = readFileText("views/index.htv")
                .replace("{{onord}}", getOrdering(state.botOrdering, IndexBotOrdering.OnAsc, IndexBotOrdering.OnDesc, "ondesc", "onasc", "ondesc"))
                .replace("{{lvlord}}", getOrdering(state.botOrdering, IndexBotOrdering.LevelAsc, IndexBotOrdering.LevelDesc,"lvldesc", "lvlasc", "lvldesc"))
                .replace("{{index_checkbox.ptv}}", indexCheckboxPartialView(state))
                .replace("{{index_filter.ptv}}", indexFilterPartialView(state))
                .replace("{{index_tabs.ptv}}", indexTabsPartialView(state))
                .replace("{{index_tabtable.ptv}}", indexTabContentPartialView(state))
                .replace("{{listbotsrow.ptv}}", partialListBots(state, nameSearch, pageNumber, pageSize))
                .replace("{{selected_options.ptv}}", loadSelectedPartialView(state))
                .replace("{{activebotscount}}", AutobotsManager.activeBots.size.toString())
                .replace("{{pagination}}", getPagination(pageNumber, pageSize, totalPages, nameSearch))
        sendWindow(player, html)
    }
    
    fun loadSettings(player: Player){
        val state = ViewStates.settingsViewState(player)
        val html = readFileText("views/settings.htv")
                .replace("{{iteretiontxt}}", textbotComponent(UiComponents.EditThinkIteration, "Think iteration (ms)", "thinkms", AutobotData.settings.iterationDelay.toString(), state.editAction == SettingsEditAction.ThinkIteration, alignment = HtmlAlignment.Center))
                .replace("{{titletxt}}", textbotComponent(UiComponents.EditDefaultTitle, "Default title", "deftit", AutobotData.settings.defaultTitle, state.editAction == SettingsEditAction.DefaultTitle, alignment = HtmlAlignment.Center))
                .replace("{{rangetxt}}", textbotComponent(UiComponents.EditTargetingRange, "Default targeting range", "deftgr", AutobotData.settings.targetingRange.toString(), state.editAction == SettingsEditAction.TargetingRange, alignment = HtmlAlignment.Center))
        sendWindow(player, html)
    }

    private fun getOrdering(botOrdering: IndexBotOrdering, ascOrdering: IndexBotOrdering, descOrdering: IndexBotOrdering, default: String, asc: String, desc: String): String {
        return when(botOrdering){
            ascOrdering -> desc
            descOrdering -> asc
            else -> default
        }
    }

    fun loadBotDetails(player: Player, autobot: Autobot) {
        val state = ViewStates.botDetailsViewState(player, autobot)
        
        var html = readFileText("views/bot_details.htv")
                .replace("{{top_buttons}}", botDetailsTopButtonsPartialView(state))
                .replace("{{activebotscount}}", AutobotsManager.activeBots.size.toString())
                .replace("{{botdetails_tabs.ptv}}", botDetailsTabsPartialView(state))
                .replace("{{content}}", contentPartialView(player, state))
                .replace("{{name}}", autobot.name)
                
        
        sendWindow(player, html)
    }
    
    fun loadCreateBot(player: Player){
        val state = ViewStates.createBotViewState(player)
        
        var html = readFileText("views/create_bot.htv")        
                .replace("{{human_selected}}", if(state.botDetails.race == "Human") "2" else "1")
                .replace("{{elf_selected}}", if(state.botDetails.race == "Elf") "2" else "1")
                .replace("{{delf_selected}}", if(state.botDetails.race == "Dark elf") "2" else "1")
                .replace("{{orc_selected}}", if(state.botDetails.race == "Orc") "2" else "1")
                .replace("{{dwarf_selected}}", if(state.botDetails.race == "Dwarf") "2" else "1")
                .replace("{{botname}}", textbotComponent(UiComponents.CreateBotName, "Bot name", "crbtn", state.botDetails.name, state.editAction == CreateBotEditAction.EditingName))
                .replace("{{botlevel}}", textbotComponent(UiComponents.CreateBotLevel, "Bot level", "crbtl", state.botDetails.level.toString(), state.editAction == CreateBotEditAction.EditingLevel))
                .replace("{{weaponench}}", textbotComponent(UiComponents.CreateBotWeaponEnch, "Weapon enchant", "crbtw", state.botDetails.weaponEnchant.toString(), state.editAction == CreateBotEditAction.EditingWeaponEnchant, contentWidth = 40))
                .replace("{{armorench}}", textbotComponent(UiComponents.CreateBotArmorEnch, "Armor enchant", "crbta", state.botDetails.armorEnchant.toString(), state.editAction == CreateBotEditAction.EditingArmorEnchant, contentWidth = 40))
                .replace("{{jewelench}}", textbotComponent(UiComponents.CreateBotJewelEnch, "Jewel enchant", "crbtj", state.botDetails.jewelEnchant.toString(), state.editAction == CreateBotEditAction.EditingJewelsEnchant, contentWidth = 40))
                .replace("{{classTypes}}", let{ 
                    val classes = CreateBotDetails.classesForDropdown(state.botDetails.race)
                    if(classes.contains(state.botDetails.classType)) {
                        classes.remove(state.botDetails.classType)
                        classes.add(0, state.botDetails.classType)
                    }
                    classes
                }.joinToString(";"))
                .replace("{{genders}}", let {
                    val genders = CreateBotDetails.gendersForDropdown()
                    if(genders.contains(state.botDetails.genger)) {
                        genders.remove(state.botDetails.genger)
                        genders.add(0, state.botDetails.genger)
                    }
                    genders
                }.joinToString(";"))
                .replace("{{faces}}", let{ 
                    val faces = CreateBotDetails.facesForDropdown()
                    if(faces.contains(state.botDetails.face)) {
                        faces.remove(state.botDetails.face)
                        faces.add(0, state.botDetails.face)
                    }
                    faces
                }.joinToString(";"))
                .replace("{{haircolors}}", let{ 
                    val hairColors = CreateBotDetails.hairColorForDropdown()
                    if(hairColors.contains(state.botDetails.hairColor)) {
                        hairColors.remove(state.botDetails.hairColor)
                        hairColors.add(0, state.botDetails.hairColor)
                    }
                    hairColors
                }.joinToString(";"))
                .replace("{{hairstyles}}", let{ 
                    val hairstyles = CreateBotDetails.hairstyleForDropdown()                    
                    if(hairstyles.contains(state.botDetails.hairStyle)) {
                        hairstyles.remove(state.botDetails.hairStyle)
                        hairstyles.add(0, state.botDetails.hairStyle)
                    }
                    hairstyles
                }.joinToString(";"))
                .replace("{{availableClasses}}", let{ 
                    val classes = getSupportedClassesForLevel(state.botDetails.level)
                            .filter { it.race == CreateBotDetails.textToRace(state.botDetails.race) }.map { it.toString() }.toMutableList()
                    
                    if(classes.contains(state.botDetails.className)) {
                        classes.remove(state.botDetails.className)
                        classes.add(0, state.botDetails.className)                        
                    }
                    classes
                }.joinToString(";"))
        
        
        sendWindow(player, html)
    }

    private fun botDetailsTopButtonsPartialView(state: BotDetailsViewState): String {
        return when(state.activeBot.isIngame()){
            true -> {
                readFileText("views/partialviews/botdetails/botdetails_online.ptv")
                        .replace("{{spawn_command}}", "des ${state.activeBot.name}")
                        .replace("{{spawn_text}}", "Despawn")
            }
            false -> {
                readFileText("views/partialviews/botdetails/botdetails_offline.ptv")
                        .replace("{{spawn_command}}", "lm  ${state.activeBot.name}")
                        .replace("{{spawn_text}}", "Spawn on me")
            }
        }
    }

    private fun contentPartialView(player: Player, state: BotDetailsViewState): String {        
        return when(state.activeTab){
            BotDetailsTab.Info -> partialBotDetailsInfoTab(state)
            BotDetailsTab.Combat -> partialBotDetailsCombatTab(state)
            BotDetailsTab.Skills -> partialBotDetailsSkillsTab(state)            
            BotDetailsTab.Social -> partialBotDetailsSocialTab(player, state)
            BotDetailsTab.Chat -> partialBotDetailsChatTab(state)
        }
    }

    private fun partialBotDetailsSocialTab(player: Player, state: BotDetailsViewState): String{

        fun renderStoreList(state: BotDetailsViewState, cmd: String = "s"): String {
            if(cmd == "s" && state.sellList.isEmpty()) {
                return "<tr><td></td></tr>"
            }

            if(cmd == "b" && state.buyList.isEmpty()) {
                return "<tr><td></td></tr>"
            }
            
            var index = 0
            val sb = StringBuilder()
            for (storeItem in (if(cmd == "s") state.sellList else state.buyList)) {
                val item = ItemTable.getInstance().getTemplate(storeItem.first) ?: continue
                sb.append(readFileText("views/partialviews/botdetails/social/botdetails_social_create_store_item.ptv")
                        .replace("{{itemicon}}", IconsTable.getItemIcon(storeItem.first))
                        .replace("{{itemname}}", item.name)
                        .replace("{{itemcount}}", storeItem.second.toString())
                        .replace("{{itemcost}}", storeItem.third.toString())
                        .replace("{{index}}", index.toString())
                        .replace("{{buysell}}", cmd)
                )
                index++
            }
            
            return sb.toString()
        }
        
        fun renderCraftStoreList(state: BotDetailsViewState) : String {
            if(state.craftList.isEmpty()) {
                return "<tr><td></td></tr>"
            }

            var index = 0
            val sb = StringBuilder()
            for (craftItem in state.craftList) {
                
                val item = ItemTable.getInstance().getTemplate(craftItem.first.product.id) ?: continue
                sb.append(readFileText("views/partialviews/botdetails/social/botdetails_social_create_craft_item.ptv")
                        .replace("{{itemicon}}", IconsTable.getItemIcon(item.itemId))
                        .replace("{{itemname}}", item.name)
                        .replace("{{itemcost}}", craftItem.second.toString())
                        .replace("{{index}}", index.toString())
                )
                index++
            }

            return sb.toString()
        }
        
        fun renderCreateButtons(command: String) : String{
            val sb = StringBuilder("<tr>")
            
            if((command == "s c" && state.sellList.isNotEmpty()) || (command == "b c" && state.buyList.isNotEmpty()) || (command == "c c" && state.craftList.isNotEmpty()) && state.activeBot.storeType == StoreType.NONE) {
                sb.append("<td width=\"90\" align=\"center\"><button action=\"bypass admin_a b t bs $command\" value=\"Create store\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td>")
            }
            
            if(state.activeBot.storeType != StoreType.NONE) {
                sb.append("<td width=\"90\" align=\"center\"><button action=\"bypass admin_a b t bs stop\" value=\"Stop store\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td>")
            }
            
            sb.append("</tr>")
            return sb.toString()
        }
        
        return when(state.socialPage){
            BotDetailsSocialPage.Home -> readFileText("views/partialviews/botdetails/social/botdetails_social.ptv")
                    .replace("{{ctrlbotcmd}}", if(player.isControllingBot() && player.getControllingBot()?.objectId == state.activeBot.objectId) "uncont" else "cont")
                    .replace("{{ctrlbotimg}}", if(player.isControllingBot() && player.getControllingBot()?.objectId == state.activeBot.objectId) "_over" else "")
            BotDetailsSocialPage.CreateSellStore -> readFileText("views/partialviews/botdetails/social/botdetails_social_create_store.ptv")
                    .replace("{{createstore}}", renderCreateButtons("s c"))
                    .replace("{{additembtn}}", if(state.activeBot.storeType != StoreType.NONE) "<tr></tr>" else "<tr><td width=\"90\" align=\"center\"><button action=\"bypass admin_a b t bs s a \$itemid \$itemcount \$priceperitem\" value=\"Add item\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td></tr>")
                    .replace("{{message_textbox}}", textbotComponent(UiComponents.PrivateSellMessage, "Message", "psmsg", state.sellMessage, state.socialEditAction == BotDetailsSocialEditAction.SellMessage, contentWidth = 200, isMulti = true))
                    .replace("{{botdetails_social_create_store_item.ptv}}", renderStoreList(state, "s"))
            BotDetailsSocialPage.CreateBuyStore -> readFileText("views/partialviews/botdetails/social/botdetails_social_create_store.ptv")
                    .replace("{{createstore}}", renderCreateButtons("b c"))
                    .replace("{{additembtn}}", if(state.activeBot.storeType != StoreType.NONE) "<tr></tr>" else "<tr><td width=\"90\" align=\"center\"><button action=\"bypass admin_a b t bs b a \$itemid \$itemcount \$priceperitem\" value=\"Add item\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td></tr>")
                    .replace("{{message_textbox}}", textbotComponent(UiComponents.PrivateBuyMessage, "Message", "pbmsg", state.buyMessage, state.socialEditAction == BotDetailsSocialEditAction.BuyMessage, contentWidth = 200, isMulti = true))
                    .replace("{{botdetails_social_create_store_item.ptv}}", renderStoreList(state, "b"))
            BotDetailsSocialPage.CreateCraftStore -> readFileText("views/partialviews/botdetails/social/botdetails_social_create_craft.ptv")
                    .replace("{{createstore}}", renderCreateButtons("c c"))
                    .replace("{{additembtn}}", if(state.activeBot.storeType != StoreType.NONE) "<tr></tr>" else "<tr><td width=\"90\" align=\"center\"><button action=\"bypass admin_a b t bs c a \$idtype \$id \$cost\" value=\"Add item\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td></tr>")
                    .replace("{{message_textbox}}", textbotComponent(UiComponents.PrivateCraftMessage, "Message", "pcmsg", state.craftMessage, state.socialEditAction == BotDetailsSocialEditAction.CraftMessage, contentWidth = 200, isMulti = true))
                    .replace("{{botdetails_social_create_craft_item.ptv}}", renderCraftStoreList(state))
        }
    }
    
    private fun partialBotDetailsChatTab(state: BotDetailsViewState): String {
        return readFileText("views/partialviews/botdetails/chat/botdetails_chat.ptv")
                .replace("{{chat}}", chatContentPartialView(state))
                .replace("{{chat_input}}", chatMultiEditPartialView(state))
                .replace("{{botdetails_chat_tabs.ptv}}", chatTabsPartialView(state))
    }

    private fun partialBotDetailsSkillsTab(state: BotDetailsViewState) : String{
        val remainingSkills = state.activeBot.combatBehavior.conditionalSkills.filter { !state.activeBot.combatBehavior.skillPreferences.skillUsageConditions.map { t -> t.skillId }.contains(it) }
        return readFileText("views/partialviews/botdetails/skills/botdetails_skills.ptv")
                .replace("{{existingconditions}}", renderExistingConditions(state))
                .replace("{{addcondition}}", if(!remainingSkills.any()) "<br><center>No skills left to configure</center>" else {
                    readFileText("views/partialviews/botdetails/skills/botdetails_skills_addcondition.ptv")
                            .replace("{{availableskills}}", remainingSkills.joinToString(";") { SkillTable.getInstance().getInfo(it, 1).name })
                            .replace("{{availabletargets}}", TargetCondition.values().joinToString(";"))
                            .replace("{{availablestatuses}}", StatusCondition.values().joinToString(";"))
                            .replace("{{availableconditions}}", ComparisonCondition.values().joinToString(";") { it.operator })
                            .replace("{{availablecondvaluetypes}}", ConditionValueType.values().joinToString(";"))
                })
    }
    
    private fun renderExistingConditions(state: BotDetailsViewState) : String{
        if(!state.activeBot.combatBehavior.skillPreferences.skillUsageConditions.any() && !state.activeBot.combatBehavior.skillPreferences.togglableSkills.any()) {
            return "<tr><td>No skill conditions</td></tr>"
        }
        
        val sb = StringBuilder()
        
        state.activeBot.combatBehavior.skillPreferences.togglableSkills.forEach {
            val skill = SkillTable.getInstance().getInfo(it.key, 1) ?: return@forEach
            
            sb.append(readFileText("views/partialviews/botdetails/skills/botdetails_skills_condition_toggle.ptv")
                    .replace("{{skillname}}", skill.name)
                    .replace("{{skillid}}", skill.id.toString())
                    .replace("{{skillicon}}", IconsTable.getSkillIcon(skill.id))
                    .replace("{{checked}}", if(it.value) "_checked" else ""))
        }
        
        
        state.activeBot.combatBehavior.skillPreferences.skillUsageConditions.forEach {
            val skill = SkillTable.getInstance().getInfo(it.skillId, 1) ?: return@forEach
            
            if(state.skillUnderEdit == it.skillId) {
                sb.append(readFileText("views/partialviews/botdetails/skills/botdetails_skills_condition_edit.ptv")
                        .replace("{{skillname}}", skill.name)
                        .replace("{{skillid}}", skill.id.toString())
                        .replace("{{skillicon}}", IconsTable.getSkillIcon(skill.id))
                        .replace("{{edittargets}}", TargetCondition.values().joinToString(";"))
                        .replace("{{editstatuses}}", StatusCondition.values().joinToString(";"))
                        .replace("{{editconditions}}", ComparisonCondition.values().joinToString(";") { t -> t.operator })
                        .replace("{{editcondvaluetypes}}", ConditionValueType.values().joinToString(";")))
            } else {
                sb.append(readFileText("views/partialviews/botdetails/skills/botdetails_skills_condition.ptv")
                        .replace("{{skillname}}", skill.name)
                        .replace("{{skillid}}", skill.id.toString())
                        .replace("{{skillicon}}", IconsTable.getSkillIcon(skill.id))
                        .replace("{{conditiontext}}", it.getConditionText()))
            }
        }
        return sb.toString()
    }

    private fun partialBotDetailsInfoTab(state: BotDetailsViewState) : String {
        return readFileText("views/partialviews/botdetails/info/botdetails_info.ptv")
                .replace("{{botname}}", state.activeBot.name)
                .replace("{{level}}", state.activeBot.level.toString())
                .replace("{{currentcp}}", state.activeBot.currentCp.toInt().toString())
                .replace("{{maxcp}}", state.activeBot.maxCp.toString())
                .replace("{{currenthp}}", state.activeBot.currentHp.toInt().toString())
                .replace("{{maxhp}}", state.activeBot.maxHp.toString())
                .replace("{{currentmp}}", state.activeBot.currentMp.toInt().toString())
                .replace("{{maxmp}}", state.activeBot.maxMp.toString())
                .replace("{{location}}", MapRegionData.getInstance().getClosestTownName(state.activeBot.x, state.activeBot.y))
                .replace("{{coordinates}}", "X: ${state.activeBot.x} Y: ${state.activeBot.y} Z: ${state.activeBot.z}")
                .replace("{{onlinetime}}", if(state.activeBot.isIngame()) {
                    val millis = System.currentTimeMillis() - state.activeBot.spawnTime
                    String.format("%d hour(s) %d min(s), %d sec(s)",
                            TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis),
                            TimeUnit.MILLISECONDS.toSeconds(millis) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                    );
                } else "N/A")
                .replace("{{botactivity}}", partialBotDetailsActivity(state))
    }

    private fun partialBotDetailsActivity(state: BotDetailsViewState): String {
        val activityPreferences = state.activeBot.combatBehavior.activityPreferences
        return readFileText("views/partialviews/botdetails/info/botdetails_activity.ptv")
                .replace("{{noneselected}}", if(state.activityEditAction == ActivityEditAction.None) "2" else "1")
                .replace("{{uptimeselected}}", if(state.activityEditAction == ActivityEditAction.Uptime || state.activityEditAction == ActivityEditAction.EditUptime) "2" else "1")
                .replace("{{scheduleselected}}", if(state.activityEditAction == ActivityEditAction.Schedule) "2" else "1")
                .replace("{{activityoptions}}", when(state.activityEditAction){
                    ActivityEditAction.None -> readFileText("views/partialviews/botdetails/info/botdetails_activity_none.ptv")
                            .replace("{{isactivechk}}", checkboxComponent(UiComponents.ActivityNoneActive, "Is Active", activityPreferences.activityType == ActivityType.None, 50))
                    ActivityEditAction.Uptime, ActivityEditAction.EditUptime -> readFileText("views/partialviews/botdetails/info/botdetails_activity_uptime.ptv")
                            .replace("{{uptimetextbox}}", textbotComponent(UiComponents.EditUptime, "Uptime in minutes", "UptimeMins", state.activeBot.combatBehavior.activityPreferences.uptimeMinutes.toString(), state.activityEditAction == ActivityEditAction.EditUptime, contentWidth = 50, isNumber = true))
                            .replace("{{isactivechk}}", checkboxComponent(UiComponents.ActivityUptimeActive, "Is Active", activityPreferences.activityType == ActivityType.Uptime, 50))
                    ActivityEditAction.Schedule -> readFileText("views/partialviews/botdetails/info/botdetails_activity_schedule.ptv")
                            .replace("{{currenttime}}", LocalTime.now(Clock.systemUTC()).format(DateTimeFormatter.ofPattern("HH:mm")))
                            .replace("{{isactivechk}}", checkboxComponent(UiComponents.ActivityScheduleActive, "Is Active", activityPreferences.activityType == ActivityType.Schedule, 50))
                            .replace("{{loginhours}}",let{
                                val hours = mutableListOf("00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23")
                                hours.remove(state.activeBot.combatBehavior.activityPreferences.loginTime.split(':')[0])
                                hours.add(0, state.activeBot.combatBehavior.activityPreferences.loginTime.split(':')[0])
                                hours.joinToString(";")
                            })
                            .replace("{{loginminutes}}", let {
                                val minutes = mutableListOf("00","05","10","15","20","25","30","35","40","45","50","55")
                                minutes.remove(state.activeBot.combatBehavior.activityPreferences.loginTime.split(':')[1])
                                minutes.add(0, state.activeBot.combatBehavior.activityPreferences.loginTime.split(':')[1])
                                minutes.joinToString(";")
                            })
                            .replace("{{logouthours}}",let{
                                val hours = mutableListOf("00","01","02","03","04","05","06","07","08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23")
                                hours.remove(state.activeBot.combatBehavior.activityPreferences.logoutTime.split(':')[0])
                                hours.add(0, state.activeBot.combatBehavior.activityPreferences.logoutTime.split(':')[0])
                                hours.joinToString(";")
                            })
                            .replace("{{logoutminutes}}", let {
                                val minutes = mutableListOf("00","05","10","15","20","25","30","35","40","45","50","55")
                                minutes.remove(state.activeBot.combatBehavior.activityPreferences.logoutTime.split(':')[1])
                                minutes.add(0, state.activeBot.combatBehavior.activityPreferences.logoutTime.split(':')[1])
                                minutes.joinToString(";")
                            })
                })
    }

    private fun partialBotDetailsCombatTab(state: BotDetailsViewState): String {
        return readFileText("views/partialviews/botdetails/combat/botdetails_combat.ptv")
                .replace("{{radiustextbox}}", textbotComponent(UiComponents.TargetRadius, "Target radius", "TargetRadius", state.activeBot.combatBehavior.combatPreferences.targetingRadius.toString(), state.combatEditAction == BotDetailsCombatEditAction.TargetRadius))
                .replace("{{targprefcombobox}}", comboboxComponent(UiComponents.TargetPref, "Target preference", "TargetPref", state.activeBot.combatBehavior.combatPreferences.targetingPreference.name, TargetingPreference.values().map { it.name }))
                .replace("{{atkplayertypecombobox}}", comboboxComponent(UiComponents.AttackPlayerTypeUi, "Attack player type", "AtkPlrType", state.activeBot.combatBehavior.combatPreferences.attackPlayerType.name, AttackPlayerType.values().map { it.name }))
                .replace("{{classspecific}}", classSpecificCombatPartialView(state))
                .replace("{{potions_cp}}", checkboxComponent(UiComponents.UseCpPots, "Use CP pots", state.activeBot.combatBehavior.combatPreferences.useGreaterCpPots))
                .replace("{{potions_qhp}}", checkboxComponent(UiComponents.UseQhPots, "Use QH pots", state.activeBot.combatBehavior.combatPreferences.useQuickHealingPots))
                .replace("{{potions_ghp}}", checkboxComponent(UiComponents.UseGhPots, "Use GH pots", state.activeBot.combatBehavior.combatPreferences.useGreaterHealingPots))
    }

    private fun classSpecificCombatPartialView(state: BotDetailsViewState): String {
        val sb = StringBuilder()
        
        if(state.activeBot.combatBehavior.combatPreferences is ArcherCombatPreferences) {
            sb.append(checkboxComponent(UiComponents.IsKiting, "Is kiting",  (state.activeBot.combatBehavior.combatPreferences as ArcherCombatPreferences).isKiting))
            sb.append(textbotComponent(UiComponents.KiteRadius, "Kite radius",  "KiteRadius", (state.activeBot.combatBehavior.combatPreferences as ArcherCombatPreferences).kiteRadius.toString(), state.combatEditAction == BotDetailsCombatEditAction.KiteRadius))
        }

        if(state.activeBot.combatBehavior.combatPreferences is PetOwnerPreferences) {
            sb.append(checkboxComponent(UiComponents.SummonsPet, "Summons pet",  (state.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).summonPet))
            sb.append(checkboxComponent(UiComponents.PetAssists, "Pet assists",  (state.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).petAssists))
            sb.append(checkboxComponent(UiComponents.PetUsesShots, "Pet uses shots",  (state.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).petUsesShots))
            sb.append(checkboxComponent(UiComponents.PetHasBuffs, "Pet has buffs",  (state.activeBot.combatBehavior.combatPreferences as PetOwnerPreferences).petHasBuffs))
        }

        if(state.activeBot.combatBehavior.combatPreferences is DuelistCombatPreferences) {
            sb.append(checkboxComponent(UiComponents.UseSkillsOnMobs, "Use skills on mobs",  (state.activeBot.combatBehavior.skillPreferences as DuelistSkillPreferences).useSkillsOnMobs))
        }
        
        return sb.toString()
    }
    
    private fun chatMultiEditPartialView(state: BotDetailsViewState): String{
        if(!state.activeBot.isOnline) {
            return ""
        }
        
        return "<tr><td><MultiEdit var=\"Message\" width=540 height=20></td><td><button action=\"bypass admin_a b csend ${state.activeBot.name} \$Message\" value=\"Send\" width=74 height=21 back=\"L2UI_CH3.Btn1_normalOn\" fore=\"L2UI_CH3.Btn1_normal\"></td></tr>"
    }            

    private fun chatTabsPartialView(state: BotDetailsViewState): String {
        return readFileText("views/partialviews/botdetails/chat/botdetails_chat_tabs.ptv")
                .replace("{{alltabactive}}", if(state.chatTab == BotDetailsChatTab.All) "siege_tab1" else "siege_tab3")
                .replace("{{pmtabactive}}", if(state.chatTab == BotDetailsChatTab.Pms) "siege_tab1" else "siege_tab3")
    }

    private fun botDetailsTabsPartialView(state: BotDetailsViewState) : String{
        return readFileText("views/partialviews/botdetails/botdetails_tabs.ptv")
                .replace("{{info_selected}}", if(state.activeTab == BotDetailsTab.Info) "On" else "")
                .replace("{{combat_selected}}", if(state.activeTab == BotDetailsTab.Combat) "On" else "")
                .replace("{{skills_selected}}", if(state.activeTab == BotDetailsTab.Skills) "On" else "")
                .replace("{{social_selected}}", if(state.activeTab == BotDetailsTab.Social) "On" else "")
                .replace("{{chattab_selected}}", if(state.activeTab == BotDetailsTab.Chat) "On" else "")
    }

    private fun chatContentPartialView(state: BotDetailsViewState): String {
        if(!state.activeBot.isIngame()) {
            return "Bot is offline"
        }
        
        val chat = when(state.chatTab){
            BotDetailsChatTab.All -> state.activeBot.chatMessages
            BotDetailsChatTab.Pms -> state.activeBot.chatMessages.filter { it.chatType == ChatType.PmSent || it.chatType == ChatType.PmReceived }
        }
        
        val chatText = chat.takeLast(chatPageSize).joinToString("<br1>") {
            when(it.chatType){
                ChatType.All -> "${it.senderName}: ${it.message}"
                ChatType.PmReceived -> "<font color=\"FF00FF\">${it.senderName}: ${it.message}</font>"
                ChatType.PmSent -> "<font color=\"FF00FF\">->${it.senderName}: ${it.message}</font>"
                ChatType.Shout ->  "<font color=\"FF7000\">${it.senderName}: ${it.message}</font>"
                else -> "${it.senderName}: ${it.message}"
            } 
        
        }
        return if (chatText.isEmpty()) "No chat" else chatText
    }

    private fun indexCheckboxPartialView(state: IndexViewState) : String{
        val selected = state.selectedBots.any()
        return readFileText("views/partialviews/index_checkbox.ptv")
                .replace("{{cmd}}", if(selected) "cls" else "chall")
                .replace("{{checked}}", if(selected) "_checked" else "")
    }

    private fun indexFilterPartialView(state: IndexViewState) : String{        
        if(state.nameToSearch.isEmpty()) return ""
        return readFileText("views/partialviews/index_filter.ptv")
                .replace("{{filter}}", state.nameToSearch)
    }

    private fun indexTabContentPartialView(state: IndexViewState) : String {

        return when(state.indexTab) {
            IndexTab.General -> {
                readFileText("views/partialviews/index_tab_table_general.ptv")
            }
            IndexTab.Clan -> {
                val memberNames = if(state.selectedBots.isEmpty()) "" else state.selectedBots.map { it.key }.take(10).joinToString(";")
                
                readFileText("views/partialviews/index_tab_table_clan.ptv")
                        .replace("{{membernames}}", memberNames)
            }
            else -> ""
        }
    }

    private fun indexTabsPartialView(state: IndexViewState) : String{
        return readFileText("views/partialviews/index_tabs.ptv")
                .replace("{{generalselected}}", if(state.indexTab == IndexTab.General) "siege_tab1" else "siege_tab3")
                .replace("{{clanselected}}", if(state.indexTab == IndexTab.Clan) "siege_tab1" else "siege_tab3")
    }

    private fun loadSelectedPartialView(state: IndexViewState): String {
        if(!state.selectedBots.any()) return ""

        return readFileText("views/partialviews/selected_options.ptv")
                .replace("{{selectedcount}}", state.selectedBots.size.toString())
                .replace("{{selected_options_offline.ptv}}", if(state.selectedBots.any { !it.value.isOnline }) readFileText("views/partialviews/selected_options_offline.ptv") else "")
                .replace("{{selected_options_online.ptv}}", if(state.selectedBots.any { it.value.isOnline }) readFileText("views/partialviews/selected_options_online.ptv") else "")
    }

    private fun getPagination(pageNumber: Int = 1, pageSize: Int = 10, totalPages: Int, filter: String = ""): String {        
        val delta = 2
        val left = pageNumber - delta
        var l: Int? = null
        val right = pageNumber + delta + 1
        val range = mutableListOf<Int>()
        
        val sb = StringBuilder()
        
        for(i in 1..totalPages) {
            if(i == 1 || i == totalPages || (i in left until right)) {
                range.add(i)
            }
        }
        
        for (i in range) {
            if(l != null) {
                if(i - l == 2) {
                    sb.append("<td><a action=\"bypass admin_a b s ${l + 1} $pageSize $filter\">${l + 1}</a></td>")
                }else if(i - l != 1) {
                    sb.append("<td>...</td>")
                }
            }
            if(i == pageNumber) {
                sb.append("<td>$i</td>")
            }else {
                sb.append("<td><a action=\"bypass admin_a b s $i $pageSize $filter\">$i</a></td>")
            }
            l = i
        }
         
        return sb.toString()
    }

    private fun partialListBots(state: IndexViewState, nameSearch: String = "", pageNumber: Int = 1, pageSize: Int = 10): String{
        val rowHtml = readFileText("views/partialviews/listbotsrow.ptv")
        val bots = AutobotsDao.searchForAutobots(nameSearch, pageNumber, pageSize, state.botOrdering)
        val sb = StringBuilder()
        bots.forEach { 
            sb.append(
                    rowHtml.replace("{{listbotsrow_offline.ptv}}", if(!it.isOnline) readFileText("views/partialviews/listbotsrow_offline.ptv") else "")
                            .replace("{{listbotsrow_online.ptv}}", if(it.isOnline) readFileText("views/partialviews/listbotsrow_online.ptv") else "")
                            .replace("{{listbotsrow_checked.ptv}}", if(state.selectedBots.containsKey(it.name)) readFileText("views/partialviews/listbotsrow_checked.ptv") else "")
                            .replace("{{listbotsrow_unchecked.ptv}}", if(!state.selectedBots.containsKey(it.name)) readFileText("views/partialviews/listbotsrow_unchecked.ptv") else "")
                            .replace("{{onlineicon}}", if (it.isOnline) "l2ui_ch3.msnicon1" else "l2ui_ch3.msnicon4" )
                            .replace("{{level}}", it.level.toString())
                            .replace("{{name}}", it.name)
                            .replace("{{classname}}", it.classId.toString())
                            .replace("{{claninfo}}", getClanInfo(it))
                            .replace("{{allyinfo}}", getAllyInfo(it))
                            .replace("{{pageNumber}}", pageNumber.toString())
                            .replace("{{pageSize}}", pageSize.toString())
                            .replace("{{behavior}}", "To remove")
                            .replace("{{botId}}", it.botId.toString())
                            
            ) 
        }
        return sb.toString()
    }

    private fun getClanInfo(it: AutobotInfo): String {
        val clan = ClanTable.getInstance().getClan(it.clanId) ?: return "No clan"
        return clan.name
    }

    private fun getAllyInfo(it: AutobotInfo): String {
        val clan = ClanTable.getInstance().getClan(it.clanId) ?: return "No ally"
        return if(clan.allyName.isNullOrEmpty()) "No ally" else clan.allyName
    }

    private fun sendWindow(player: Player, html: String){
        val optimisedHtml = optimiseHtml(html)
        
        when {
            optimisedHtml.length < 4090 -> {
                player.sendPacket(ShowBoard(optimisedHtml, "101"))
                player.sendPacket(ShowBoard.STATIC_SHOWBOARD_102)
                player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103)
            }
            optimisedHtml.length < 8180 -> {
                player.sendPacket(ShowBoard(optimisedHtml.substring(0, 4090), "101"))
                player.sendPacket(ShowBoard(optimisedHtml.substring(4090, optimisedHtml.length), "102"))
                player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103)
            }
            optimisedHtml.length < 12270 -> {
                player.sendPacket(ShowBoard(optimisedHtml.substring(0, 4090), "101"))
                player.sendPacket(ShowBoard(optimisedHtml.substring(4090, 8180), "102"))
                player.sendPacket(ShowBoard(optimisedHtml.substring(8180, optimisedHtml.length), "103"))
            }
            optimisedHtml.length > 12270 -> {
                player.sendMessage("Generated Html is bigger than 12270 bytes. Elfo fucked up. Use //a b reset")
            }
        }
    }

    private fun optimiseHtml(html: String): String {
        return html.replace(Regex(">\\s+<"), "><")
    }

    private fun totalPageCount(nameSearch: String, pageSize: Int = 10) : Int{
        return AutobotsDao.getTotalBotCount(nameSearch) / pageSize
    }
}