package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * A specialized [StateContainerHost] that supports locked intents.
 *
 * Implement this interface in your ViewModel or host class to enable the `lockIntent`,
 * `unlockIntent`, and `cancelIntent` DSL functions.
 */
interface LockContainerHost<State : Any, Effect : Any>
    : StateContainerHost<State, Effect> {
    /**
     * The [StateContainer] managed by this host.
     */
    override val container: StateContainer<State, Effect>
}

/**
 * Cancels the intent identified by [intentId].
 *
 * This stops the execution of the coroutine associated with the intent ID and removes
 * its lock status.
 *
 * @param intentId The identifier of the intent to cancel. Defaults to [Unit].
 */
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)

/**
 * Unlocks the intent identified by [intentId].
 *
 * This allows the intent to be executed again if it was manually locked or if
 * its previous execution was stuck.
 *
 * @param intentId The identifier of the intent to unlock. Defaults to [Unit].
 */
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)

/**
 * Launches an intent that is locked by [intentId].
 *
 * If another intent with the same ID is already running or manually locked,
 * this block will be ignored.
 *
 * @param intentId The identifier used for locking. Defaults to [Unit].
 * @param block The suspendable logic to execute.
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asLockContainer().lockIntent(intentId, block)

/**
 * Manually locks an intent ID, preventing any [lockIntent] calls with the same ID
 * from executing until [unlockIntent] is called.
 *
 * @param intentId The identifier to lock. Defaults to [Unit].
 */
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)
