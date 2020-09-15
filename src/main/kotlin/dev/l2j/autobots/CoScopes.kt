package dev.l2j.autobots

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

//Needs to be reworked
internal object CoScopes {
    internal val generalScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    
    internal val sequenceScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    internal val massSpawnerScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    internal val massDespawnerScope: CoroutineScope = CoroutineScope(Dispatchers.Default)
    
    internal val onUpdateScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
}