package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * A specialized [StateContainerHost] that supports pending intents.
 *
 * Implement this interface in your ViewModel or host class to enable the `pendingIntent`,
 * `launchPendingIntent`, `launchPendingIntents`, `clearPendingIntent`, `clearPendingIntents`,
 * `hasPendingIntent`, and `hasPendingIntents` DSL functions.
 */
interface PendingContainerHost<State : Any, Effect : Any> :
    StateContainerHost<State, Effect> {
    /**
     * The [StateContainer] managed by this host.
     */
    override val container: StateContainer<State, Effect>
}

/**
 * Sets an intent identified by [intentId] as pending to be executed later.
 *
 * @param intentId The identifier used for the pending intent. Defaults to [Unit].
 * @param block The suspendable logic to execute when launched.
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.pendingIntent(
        intentId: Any = Unit,
        block: suspend IntentScope<State, Effect>.() -> Unit
    ) = container.asPendingContainer().pendingIntent(intentId, block)

/**
 * Launches the pending intent identified by [intentId] and removes it from pending.
 *
 * @param intentId The identifier of the pending intent to launch. Defaults to [Unit].
 */
fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.launchPendingIntent(
        intentId: Any = Unit
    ) = container.asPendingContainer().launchPendingIntent(intentId)

/**
 * Launches all stored pending intents and removes them from pending.
 */
fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.launchPendingIntents() =
    container.asPendingContainer().launchPendingIntents()

/**
 * Clears the pending intent identified by [intentId] without executing it.
 *
 * @param intentId The identifier of the pending intent to clear. Defaults to [Unit].
 */
fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.clearPendingIntent(
        intentId: Any = Unit
    ) = container.asPendingContainer().clearPendingIntent(intentId)

/**
 * Clears all stored pending intents without executing them.
 */
fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.clearPendingIntents() =
    container.asPendingContainer().clearPendingIntents()

/**
 * Checks if a pending intent with the given [intentId] exists.
 *
 * @param intentId The identifier of the pending intent. Defaults to [Unit].
 * @return `true` if a pending intent with the given [intentId] exists, `false` otherwise.
 */
suspend fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.hasPendingIntent(
        intentId: Any = Unit
    ): Boolean = container.asPendingContainer().hasPendingIntent(intentId)

/**
 * Checks if there are any pending intents stored.
 *
 * @return `true` if there are pending intents, `false` otherwise.
 */
suspend fun <State : Any, Effect : Any>
    PendingContainerHost<State, Effect>.hasPendingIntents(): Boolean =
    container.asPendingContainer().hasPendingIntents()
