package net.asere.omni.mvi

import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer
import java.lang.IllegalArgumentException

/**
 * Puts the container host in evaluation status and offers a scope of with testing data such as
 * emitted states and emitted effects.
 *
 * @param relaxed Set true if you want to avoid iterating over all emitted states and effects
 * @param block Evaluation code block
 */
fun <State : Any, Effect : Any> TestResult<State, Effect>.evaluate(
    relaxed: Boolean = false,
    block: EvaluationScope<State, Effect>.() -> Unit
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
 * Call this extension function to start testing an action
 *
 * @param action Action to test
 * @param take argument to define how many data items must be collected
 */
suspend fun <State : Any, Effect : Any, Action : Any> ActionContainerHost<State, Effect, Action>
        .testOn(
    action: Action,
    take: Take? = null,
    withState: State? = null,
) = testIntent(
    withState = withState,
    take = take
) { on(action) }

/**
 * Call this method to start testing an intent
 *
 * @param testBlock intent reference or testing content
 * @param take argument to define how many data items must be collected
 * @param withState the state initialized before the test. By default will be the host initial state
 */
suspend fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> Host.testIntent(
    take: Take? = null,
    withState: State? = null,
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    val initialState = withState ?: container.asStateContainer().initialState

    if (take != null && take.count <= 0)
        throw IllegalArgumentException("take argument count should be grater than 0 if set")

    val testContainer = container.buildTestContainer().also { delegate(it) }

    await()
    container.asStateContainer().update { initialState }
    testContainer.reset()

    testBlock()

    with(testContainer) {
        await {
            take is TakeStates && take.count == emittedStates.size ||
                    take is TakeEffects && take.count == emittedEffects.size
        }
        TestResult(
            initialState = withState ?: initialState,
            emittedStates = emittedStates,
            emittedEffects = emittedEffects,
            emittedElements = emittedElements,
        )
    }
}

/**
 * Test a host constructor
 *
 * @param builder Host construction builder function
 */
suspend fun <State : Any, Effect : Any> testConstructor(
    initialState: State? = null,
    builder: () -> StateContainerHost<State, Effect>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()

    val chosenInitialState = initialState ?: host.currentState
    initialState?.let { host.container.asStateContainer().update { it } }
    val testContainer = host.container.buildTestContainer().also { host.delegate(it) }

    host.await()

    with(testContainer) {
        TestResult(
            initialState = chosenInitialState,
            emittedStates = emittedStates,
            emittedEffects = emittedEffects,
            emittedElements = emittedElements,
        )
    }
}