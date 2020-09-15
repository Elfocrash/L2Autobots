package dev.l2j.autobots.utils

import net.sf.l2j.gameserver.data.cache.CrestCache
import net.sf.l2j.gameserver.idfactory.IdFactory
import java.net.URL

internal fun uploadCrest(urlString: String, crestType: CrestCache.CrestType) : Int {
    try {
        URL(urlString).openConnection().getInputStream().use {
            val buffer = DDSConverter.convertToDDS(it)!!
            buffer.position(0)
            var arr = ByteArray(buffer.remaining())
            buffer.get(arr)
            arr[12] = 16

            val header = arr.copyOfRange(0, 128)
            val middle = ByteArray(32)
            val last = arr.copyOfRange(128, arr.size)

            arr = header.plus(middle).plus(last)

            if(arr.size != crestType.size) {
                return 0
            }

            val crestId = IdFactory.getInstance().nextId
            CrestCache.getInstance().saveCrest(CrestCache.CrestType.PLEDGE, crestId, arr)
            return crestId
        }
    }catch (e: Exception){  
        return 0
    }    
}