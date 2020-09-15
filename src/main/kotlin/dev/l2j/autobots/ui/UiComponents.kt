package dev.l2j.autobots.ui

import dev.l2j.autobots.ui.html.HtmlAlignment

internal object UiComponents{
    internal const val TargetRadius = "trgr"
    internal const val TargetPref = "trpr"
    internal const val AttackPlayerTypeUi = "atkplt"
    internal const val KiteRadius = "ktr"
    internal const val IsKiting = "kt"
    internal const val SummonsPet = "sump"
    internal const val PetAssists = "ptass"
    internal const val PetUsesShots = "ptush"
    internal const val PetHasBuffs = "pthab"
    internal const val PrivateSellMessage = "psmsg"
    internal const val PrivateBuyMessage = "pbmsg"
    internal const val PrivateCraftMessage = "pcmsg"        
    internal const val UseSkillsOnMobs = "uskom"    
    internal const val UseCpPots = "ucp"
    internal const val UseQhPots = "uqhp"
    internal const val UseGhPots = "ughp"
    internal const val EditUptime = "biedu"    
    internal const val CreateBotName = "crbtnm"
    internal const val CreateBotLevel = "crbtlm"
    internal const val CreateBotWeaponEnch = "crbtwe"
    internal const val CreateBotArmorEnch = "crbtae"
    internal const val CreateBotJewelEnch = "crbtje"    
    internal const val ActivityNoneActive = "ana"
    internal const val ActivityUptimeActive = "aua"
    internal const val ActivityScheduleActive = "asa"
    internal const val EditThinkIteration = "thinkms"
    internal const val EditDefaultTitle = "deftit"
    internal const val EditTargetingRange = "deftgr"

    fun textbotComponent(componentId: String, label: String, variableName: String, value: String, isUnderEdit: Boolean, labelWidth: Int = 75, contentWidth: Int = 100, isNumber: Boolean = false, isMulti: Boolean = false, alignment: HtmlAlignment = HtmlAlignment.Right): String{
        return AutobotsUi.readFileText("components/textbox_withsave.htc")
                .replace("{{label}}", label)
                .replace("{{align}}", alignment.toString())
                .replace("{{labelwidth}}", labelWidth.toString())
                .replace("{{txtcontent}}", if(isUnderEdit) "<td width=$contentWidth><${if(isMulti) "multiedit" else "edit"} var=\"{{varname}}\" width=$contentWidth height=14${if(isNumber) " type=number" else ""}></td>" else "<td>$value</td>")
                .replace("{{varname}}", variableName)
                .replace("{{action}}", if(isUnderEdit) "bypass admin_a b sv $componentId \$$variableName" else "bypass admin_a b ed $componentId")
                .replace("{{actionname}}", if(isUnderEdit) "Save" else "Edit")
    }

    fun comboboxComponent(componentId: String, label: String, variableName: String, selectedValue: String, values: List<String>): String{
        return AutobotsUi.readFileText("components/combobox_withsave.htc")
                .replace("{{label}}", label)
                .replace("{{varname}}", variableName)
                .replace("{{items}}", listOf(selectedValue).union(values.filter { it != selectedValue }).joinToString(";"))
                .replace("{{action}}", "bypass admin_a b sv $componentId \$$variableName")
                .replace("{{actionname}}",  "Save")
    }

    fun checkboxComponent(componentId: String, label: String, isChecked: Boolean, labelWidth: Int = 75): String{
        return AutobotsUi.readFileText("components/checkbox.htc")
                .replace("{{label}}", label)
                .replace("{{componentId}}", componentId)
                .replace("{{action}}", if(isChecked) "false" else "true")
                .replace("{{checked}}",  if(isChecked) "_checked" else "")
                .replace("{{labelwidth}}", labelWidth.toString())
    }
}