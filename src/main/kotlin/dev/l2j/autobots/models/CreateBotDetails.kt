package dev.l2j.autobots.models

import dev.l2j.autobots.AutobotsNameService
import dev.l2j.autobots.utils.getSupportedClassesForLevel
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.enums.actors.ClassId
import net.sf.l2j.gameserver.enums.actors.ClassRace
import net.sf.l2j.gameserver.enums.actors.Sex
import net.sf.l2j.gameserver.model.actor.player.Appearance

internal data class CreateBotDetails(
        var name: String = AutobotsNameService.getRandomAvailableName(), 
        var level: Int = 80, 
        var race: String = "Human", 
        var classType: String = "Fighter", 
        var genger: String = "Male", 
        var hairStyle: String = "Type A", 
        var hairColor: String = "Type A", 
        var face: String = "Type A", 
        var className: String = getSupportedClassesForLevel(level).filter { it.race == textToRace(race) }.random().toString(),
        var weaponEnchant: Int = 0,
        var armorEnchant: Int = 0,
        var jewelEnchant: Int = 0){
    
    fun getClassId(): ClassId{
        return ClassId.values().first { it.toString() == className }
    }
    
    fun getAppearance() : Appearance {
        return Appearance(textToFace(face), textToHairColor(hairColor), if(genger == "Male" && (hairStyle == "Type F" || hairStyle == "Type G")) 0 else textToHairStyle(hairStyle), textToSex(genger))
    }
    
    fun getRace(): ClassRace{
        return textToRace(race)
    }
    
    fun randomize(){
        name = AutobotsNameService.getRandomAvailableName()
        level = Rnd.get(1, 80)
        race = racesForDropdown().random()
        classType = if(race == "Dwarf") "Fighter" else listOf("Fighter", "Mystic").random()
        genger = listOf("Male", "Female").random()
        hairStyle = if(genger == "Male") listOf("Type A", "Type B", "Type C", "Type D", "Type E").random() else listOf("Type A", "Type B", "Type C", "Type D", "Type E", "Type F", "Type G").random()
        hairColor = listOf("Type A", "Type B", "Type C", "Type D", "Type E").random()
        face = listOf("Type A", "Type B", "Type C", "Type D").random()
        className = getSupportedClassesForLevel(level).filter { it.race == textToRace(race) }.random().toString()
    }
    
    companion object{
        fun textToHairColor(text: String): Byte{
            return when(text){
                "Type A" -> 0
                "Type B" -> 1
                "Type C" -> 2
                "Type D" -> 3
                "Type E" -> 4
                else -> 0
            }
        }
        
        fun textToSex(text: String): Sex {
            return when(text){
                "Male" -> Sex.MALE
                "Female" -> Sex.FEMALE
                else -> Sex.MALE
            }
        }

        fun textToHairStyle(text: String): Byte{
            return when(text){
                "Type A" -> 0
                "Type B" -> 1
                "Type C" -> 2
                "Type D" -> 3
                "Type E" -> 4
                "Type F" -> 5
                "Type G" -> 6
                else -> 0
            }
        }
        
        fun textToFace(text: String): Byte{
            return when(text){
                "Type A" -> 0
                "Type B" -> 1
                "Type C" -> 2
                "Type D" -> 3
                else -> 0
            }
        }
        
        fun textToRace(text: String): ClassRace{
            return when(text){
                "Human" -> ClassRace.HUMAN
                "Elf" -> ClassRace.ELF
                "Dark elf" -> ClassRace.DARK_ELF
                "Orc" -> ClassRace.ORC
                "Dwarf" -> ClassRace.DWARF
                else -> ClassRace.HUMAN
            }
        }
        
        fun racesForDropdown() : MutableList<String> {
            return mutableListOf("Human", "Elf", "Dark elf", "Orc", "Dwarf")
        }

        fun gendersForDropdown() : MutableList<String> {
            return mutableListOf("Male", "Female")
        }

        fun facesForDropdown() : MutableList<String> {
            return mutableListOf("Type A", "Type B", "Type C")
        }

        fun hairColorForDropdown() : MutableList<String> {
            return mutableListOf("Type A", "Type B", "Type C", "Type D")
        }
        
        fun hairstyleForDropdown() : MutableList<String>{
            return mutableListOf("Type A", "Type B", "Type C", "Type D", "Type E", "Type F", "Type G")
        }
        
        fun classesForDropdown(race: String) : MutableList<String>{
            return when(race){
                "Human", "Elf", "Dark elf", "Orc" -> mutableListOf("Fighter", "Mystic")
                "Dwarf" -> mutableListOf("Fighter")
                else -> mutableListOf("Fighter")
            }
        }
    }
}