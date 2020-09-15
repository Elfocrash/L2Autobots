package dev.l2j.autobots

import net.sf.l2j.commons.lang.StringUtil
import net.sf.l2j.commons.random.Rnd
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable
import net.sf.l2j.gameserver.data.xml.NpcData
import java.io.BufferedReader
import java.io.LineNumberReader
import java.io.StringReader
import java.util.logging.Level
import java.util.logging.Logger

object AutobotsNameService {

    private val logger = Logger.getLogger(AutobotsNameService::class.java.name)
    private lateinit var fakePlayerNames: List<String>

    init {
        loadWordlist()
    }

    fun getRandomAvailableName(): String {
        var name = getRandomNameFromWordlist()

        while (!nameIsValid(name)) {
            name = getRandomNameFromWordlist()
        }

        return name
    }

    private fun getRandomNameFromWordlist(): String {
        return fakePlayerNames[Rnd.get(0, fakePlayerNames.size - 1)]
    }

    private fun loadWordlist() {
        try {
            LineNumberReader(BufferedReader(StringReader(javaClass.getResource("/fakenamewordlist.txt").readText()))).use { lnr ->
                val playersList = ArrayList<String>()
                lnr.readLines().forEach { line ->
                    run {
                        if (line.trim { it <= ' ' }.isNotEmpty() && !line.startsWith("#")) {
                            playersList.add(line)
                        }
                    }
                }
                fakePlayerNames = playersList
                logger.log(Level.INFO, "Loaded ${fakePlayerNames.size} fake player names.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun nameAlreadyExists(name: String): Boolean {
        return PlayerInfoTable.getInstance().getPlayerObjectId(name) > 0
    }
    
    fun nameIsValid(name: String) : Boolean{
        if (!StringUtil.isValidString(name, "^[A-Za-z0-9]{3,16}$")) {
            return false
        }
        
        if (NpcData.getInstance().getTemplateByName(name) != null) {
            return false
        }
        
        if(nameAlreadyExists(name)) {
            return false
        }
        
        return true
        
    }
}