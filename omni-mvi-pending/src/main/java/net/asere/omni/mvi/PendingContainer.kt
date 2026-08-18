package net.asere.omni.mvi

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.asere.omni.core.Container

/**
 * A specialized [StateContainerDecorator] that allows intents to be held as pending and executed later.
 *
 * Intents can be marked as pending with a given [intentId], launched individually or all together,
 * and cleared individually or all together.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @property container The inner [StateContainer] to be decorated with pending intent capabilities.
 */
open class PendingContainer<State : Any, Effect : Any> internal constructor(
    override val container: StateContainer<State, Effect>,
) : StateContainerDecorator<State, Effect>(
    container
),
    Container,
    PendingContainerHost<State, Effect> {

    private val mutex = Mutex()
    private val pendingIntents = mutableMapOf<Any, PendingIntent<State, Effect>>()

    /**
     * Stores an intent identified by [intentId] to be executed later.
     *
     * @param intentId The identifier for the pending intent.
     * @param block The suspendable logic to execute when launched.
     */
    internal fun pendingIntent(
        intentId: Any,
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = intent {
        mutex.withLock {
            pendingIntents[intentId] = PendingIntent(intentId, block)
        }
    }

    /**
     * Launches the pending intent identified by [intentId] if present, and removes it from pending.
     *
     * @param intentId The identifier of the pending intent to launch.
     */
    internal fun launchPendingIntent(intentId: Any) = intent {
        val pendingIntent = mutex.withLock {
            pendingIntents.remove(intentId)
        }
        pendingIntent?.let {
            intent(id = it.id) {
                it.block(this)
            }
        }
    }

    /**
     * Launches all currently stored pending intents and clears them.
     */
    internal fun launchPendingIntents() = intent {
        val toLaunch = mutex.withLock {
            val list = pendingIntents.values.toList()
            pendingIntents.clear()
            list
        }
        toLaunch.forEach { pending ->
            intent(id = pending.id) {
                pending.block(this)
            }
        }
    }

    /**
     * Clears the pending intent identified by [intentId] without executing it.
     *
     * @param intentId The identifier of the pending intent to clear.
     */
    internal fun clearPendingIntent(intentId: Any) = intent {
        mutex.withLock {
            pendingIntents.remove(intentId)
        }
    }

    /**
     * Clears all stored pending intents without executing them.
     */
    internal fun clearPendingIntents() = intent {
        mutex.withLock {
            pendingIntents.clear()
        }
    }

    /**
     * Checks if a pending intent with the given [intentId] exists.
     *
     * @param intentId The identifier of the pending intent.
     * @return `true` if a pending intent with the given [intentId] exists, `false` otherwise.
     */
    internal suspend fun hasPendingIntent(intentId: Any): Boolean = mutex.withLock {
        pendingIntents.containsKey(intentId)
    }

    /**
     * Checks if there are any pending intents stored.
     *
     * @return `true` if there are pending intents, `false` otherwise.
     */
    internal suspend fun hasPendingIntents(): Boolean = mutex.withLock {
        pendingIntents.isNotEmpty()
    }
}

/**
 * Internal factory function to create a [PendingContainer].
 */
internal fun <State : Any, Effect : Any> pendingContainer(
    container: StateContainer<State, Effect>
) = PendingContainer(container)

/**
 * Extension to wrap an existing [StateContainer] into a [PendingContainer].
 *
 * @return A new [PendingContainer] instance decorating the original one.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.buildPendingContainer() = pendingContainer(this)

/**
 * Searches the decoration chain for a [PendingContainer].
 *
 * @return The [PendingContainer] found in the stack.
 * @throws RuntimeException if no [PendingContainer] is found.
 */
internal fun <State : Any, Effect : Any>
    StateContainer<State, Effect>.asPendingContainer() =
    asStateContainer().seek<PendingContainer<State, Effect>> { it is PendingContainer<*, *> }
