package net.asere.omni.mvi

import net.asere.omni.core.ExecutableContainer

/**
 * Attempts to resolve this container into an [ExecutableContainer].
 *
 * This searches through the decorator chain to find the underlying executable container
 * that manages coroutines and jobs.
 *
 * @return The [ExecutableContainer] instance.
 * @throws RuntimeException if no executable container is found in the stack.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asExecutableContainer(): ExecutableContainer =
    asStateContainer().seek { it is ExecutableContainer }

/**
 * Suspends until all children jobs of the hosted container's scope have completed.
 *
 * This is particularly useful in unit tests to ensure that all side-effects or
 * asynchronous operations launched during an intent have finished before making assertions.
 */
suspend fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.joinChildren() =
    container.asExecutableContainer().joinChildren()

/**
 * Resolves the [DelegatorContainer] from the decorator stack.
 *
 * @return The [DelegatorContainer] instance.
 * @throws RuntimeException if no delegator is found.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asDelegatorContainer(): DelegatorContainer<State, Effect> =
    asStateContainer().seek { it is DelegatorContainer<*, *> }

/**
 * Configures the container to delegate its state updates and effects to another container.
 *
 * This is a key mechanism for the testing framework to record emissions.
 *
 * @param container The container that will receive delegated updates and effects.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.delegate(
    container: InnerStateContainer<State, Effect>
) = asDelegatorContainer().delegate(container)

/**
 * Removes any previously set delegate from the container.
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.clearDelegate() =
    asDelegatorContainer().clearDelegate()

/**
 * Configures the host to delegate its container's emissions to another container.
 *
 * @param container The target delegating container.
 */
fun <State : Any, Effect : Any> StateContainerHost<State, Effect>.delegate(
    container: InnerStateContainer<State, Effect>
) = this.container.delegate(container)

/**
 * Joins all children jobs of this container sequentially.
 */
suspend fun <State : Any, Effect : Any> StateContainer<State, Effect>.joinChildren() =
    asExecutableContainer().joinChildren()
