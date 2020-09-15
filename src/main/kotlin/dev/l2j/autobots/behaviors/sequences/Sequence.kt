package dev.l2j.autobots.behaviors.sequences

import dev.l2j.autobots.CoScopes
import dev.l2j.autobots.extensions.getActiveSequence
import dev.l2j.autobots.extensions.setActiveSequence
import dev.l2j.autobots.utils.CancellationToken
import kotlinx.coroutines.async
import net.sf.l2j.gameserver.model.actor.Player

internal interface Sequence {
    val bot: Player
    
    var cancellationToken: CancellationToken?
    
    suspend fun definition()

    suspend fun execute(){
        if(bot.getActiveSequence() != null) {
            bot.getActiveSequence()?.cancellationToken?.cancelLambda?.invoke()
            bot.setActiveSequence(null)
        }
        
        val job = CoScopes.sequenceScope.async { definition() }
        cancellationToken = CancellationToken({ job.cancel() })
        job.invokeOnCompletion { 
            bot.setActiveSequence(null)
        }
        bot.setActiveSequence(this)
        job.await()
    }
}