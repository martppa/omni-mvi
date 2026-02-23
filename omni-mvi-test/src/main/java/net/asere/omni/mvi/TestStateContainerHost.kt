package net.asere.omni.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer

/**
 * Puts the container host in evaluation status and offers a scope of with testing data such as
 * emitted states and emitted effects.
 *
 * @param relaxed Set true if you want to avoid iterating over all emitted states and effects
 * @param block Evaluation code block
 */
fun <State : Any, Effect : Any> TestResult<State, Effect>.evaluate(
    relaxed: Boolean = false,
    block: EvaluationScope<State, Effect>.() -> Unit = {}
) {
    val scope = EvaluationScope(this)
    block(scope)
    with(scope) {
        if (!relaxed) {
            if (effectIterator.hasNext())
                throw IllegalStateException("${effectIterator.nextIndex()} effects were tested but ${emittedEffects.size} were emitted.")
            if (stateIterator.hasNext())
                throw IllegalStateException("${stateIterator.nextIndex()} states were tested but ${emittedStates.size} were emitted.")
        }
    }
}

/**
 * Call this method to start testing an intent
 *
 * @param testBlock intent reference or testing content
 * @param withState the state initialized before the test. By default will be the host initial state
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> Host.testIntent(
    withState: State? = null,
    testBlock: Host.() -> Unit
): TestResult<State, Effect> {
    return withContext(ExecutableContainer.blockedContext()) {
        val initialState = withState ?: container.asStateContainer().initialState

        await()

        container.asStateContainer().update { initialState }
        val testContainer = container.buildTestContainer().also { delegate(it) }
        testContainer.reset()

        testBlock()

        with(testContainer) {
            joinChildren()
            TestResult(
                initialState = withState ?: initialState,
                emittedStates = emittedStates,
                emittedEffects = emittedEffects,
                emittedElements = emittedElements,
            )
        }
    }
}

/**
 * Test a host constructor
 *
 * @param builder Host construction builder function
 * @param initialState the state initialized before the test. By default will be the host initial state
 */
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun <State : Any, Effect : Any> testConstructor(
    initialState: State? = null,
    builder: () -> StateContainerHost<State, Effect>
): TestResult<State, Effect> {
    return withContext(ExecutableContainer.blockedContext()) {
        val host = builder()

        val chosenInitialState = initialState ?: host.currentState
        initialState?.let { host.container.asStateContainer().update { it } }
        val testContainer = host.container.buildTestContainer().also { host.delegate(it) }

        with(testContainer) {
            joinChildren()
            TestResult(
                initialState = chosenInitialState,
                emittedStates = emittedStates,
                emittedEffects = emittedEffects,
                emittedElements = emittedElements,
            )
        }
    }
}

/**
 * Returns itself as an executable container
 */
fun <State : Any, Effect : Any> StateContainer<State, Effect>.asExecutableContainer(): ExecutableContainer =
    asStateContainer().seek { it is ExecutableContainer }

/**
 * Seeks all children jobs and await for their completion. Use this method to await all
 * children executions asynchronously. This method does not join the container job itself.
 */
suspend fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.await() =
    container.asExecutableContainer().await()

/**
 * Seeks all children jobs and joins them. Use this method to await children jobs sequentially.
 * This method does not join the container job itself.
 */
suspend fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.joinChildren() =
    container.asExecutableContainer().joinChildren()

/**
 * Seeks all children jobs and cancels them. Use this method to cancel all children executions.
 * This method does not cancel the container job itself.
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.cancel() =
    container.asExecutableContainer().cancel()

/**
 * Performs an action everytime a state or an effect is emitted
 *
 * @param container Container to delegate
 * @param block Code to execute
 *
 * @return Delegated container
 */
private fun <State : Any, Effect : Any> doOnAnyEmission(
    container: InnerStateContainer<State, Effect>,
    block: () -> Unit
): InnerStateContainer<State, Effect> {
    return object : DelegatorContainer<State, Effect>(container) {
        override fun update(function: State.() -> State) {
            super.update(function)
            block()
        }

        override fun post(effect: Effect) {
            super.post(effect)
            block()
        }
    }
}

/**
 * Start children jobs (not recursively)
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.launchJobs() =
    container.asExecutableContainer().launchJobs()

/**
 * Release the execution of intents in the container. This means unblock executions when
 * running under a blocked context. Any holding execution will be started.
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.releaseExecution() =
    container.asExecutableContainer().releaseExecution()

/**
 * When running under a blocked context this method will force the block of executions.
 * Lock the execution of intents in the container. This means, running executions will
 * put on hold.
 */
fun <State : Any, Effect : Any>
        StateContainerHost<State, Effect>.lockExecution() =
    container.asExecutableContainer().lockExecution()

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

/**
 * Await container children jobs completion asynchronously
 */
suspend fun <State : Any, Effect : Any> StateContainer<State, Effect>.await() =
    asExecutableContainer().await()
