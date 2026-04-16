package net.asere.omni.mvi

import net.asere.omni.core.ExecutableContainer

/**
 * Returns itself as an executable container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asExecutableContainer(): ExecutableContainer =
    asStateContainer().seek { it is ExecutableContainer }

/**
 * Seeks all children jobs and joins them. Use this method to await children jobs sequentially.
 * This method does not join the container job itself.
 */
suspend fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.joinChildren() =
    container.asExecutableContainer().joinChildren()

/**
 * Recursively seeks a delegator container and return it
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asDelegatorContainer(): DelegatorContainer<State, Effect> =
    asStateContainer().seek { it is DelegatorContainer<*, *> }

/**
 * Delegate behavior to the provided container
 *
 * @param container Delegating container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.delegate(
    container: InnerStateContainer<State, Effect>
) = asDelegatorContainer().delegate(container)

/**
 * Clears delegating container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

/**
 * Delegate behavior to the provided container
 *
 * @param container Delegating container
 */
fun <State : Any, Effect : Any> StateContainerHost<State, Effect>.delegate(
    container: InnerStateContainer<State, Effect>
) = this.container.delegate(container)

/**
 * Joins container children sequentially
 */
suspend fun <State : Any, Effect : Any> StateContainer<State, Effect>.joinChildren() =
    asExecutableContainer().joinChildren()
