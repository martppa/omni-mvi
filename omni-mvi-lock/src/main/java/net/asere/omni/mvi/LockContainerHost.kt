package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

/**
 * Host of Lock Containers
 */
interface LockContainerHost<State : Any, Effect : Any>
    : StateContainerHost<State, Effect> {
    override val container: StateContainer<State, Effect>
}

/**
 * Cancels the intent identified with provided id
 *
 * @param intentId intent identifier
 */
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)

/**
 * Unlocks the intent identified with provided id
 *
 * @param intentId intent identifier
 */
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)

/**
 * Launches a lock intent
 *
 * @param intentId intent identifier
 * @param block intent content
 */
@OmniHostDsl
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asLockContainer().lockIntent(intentId, block)

/**
 * Locks the intent identified with provided id
 *
 * @param intentId intent identifier
 */
fun <State : Any, Effect : Any>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)