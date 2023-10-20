package net.asere.omni.mvi

import net.asere.omni.core.OmniHostDsl

interface LockContainerHost<State, Effect>
    : StateContainerHost<State, Effect> {
    override val container: ExposedStateContainer<State, Effect>
}

fun <State, Effect>
        LockContainerHost<State, Effect>.cancelIntent(
    intentId: Any = Unit,
) = container.asLockContainer().cancelIntent(intentId)

fun <State, Effect>
        LockContainerHost<State, Effect>.unlockIntent(
    intentId: Any = Unit
) = container.asLockContainer().unlockIntent(intentId)

@OmniHostDsl
fun <State, Effect>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit,
    block: suspend IntentScope<State, Effect>.() -> Unit
) = container.asLockContainer().lockIntent(intentId, block)

fun <State, Effect>
        LockContainerHost<State, Effect>.lockIntent(
    intentId: Any = Unit
) = container.asLockContainer().lockIntent(intentId)