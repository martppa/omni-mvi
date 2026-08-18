package net.asere.omni.mvi

/**
 * Represents an intent block that is stored to be executed later.
 *
 * @param id The identifier for the pending intent.
 * @param block The suspendable logic to execute when launched.
 */
class PendingIntent<State : Any, Effect : Any>(
    val id: Any,
    val block: suspend IntentScope<State, Effect>.() -> Unit
)
