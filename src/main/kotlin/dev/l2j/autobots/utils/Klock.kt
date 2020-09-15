package dev.l2j.autobots.utils

import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicBoolean

internal class Klock internal constructor(
        name: String,
        private val delayDuration: Long,
        private val repeat: Long,
        private val coroutineScope: CoroutineScope = GlobalScope,
        action: suspend () -> Unit
) {
    private val keepRunning = AtomicBoolean(true)
    private var job: Job? = null
    private val tryAction = suspend {
        try {
            action()
        } catch (e: Throwable) {
            println("$name timer action failed: $action")
        }
    }

    fun start() {
        job = coroutineScope.launch {
            delay(delayDuration)
            while (keepRunning.get()) {
                tryAction()
                delay(repeat)
            }
        }
    }
    
    fun shutdown() {
        keepRunning.set(false)
    }
    
    fun cancel() {
        shutdown()
        job?.cancel("cancel() called")
    }

    companion object {
        fun start(
                name: String,
                delay: Long,
                repeat: Long,
                coroutineScope: CoroutineScope = GlobalScope,
                action: suspend () -> Unit
        ): Klock = Klock(name, delay, repeat, coroutineScope, action).also { it.start() }
    }
}