package net.asere.omni.mvi

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
 * Call this extension function to start testing an action
 *
 * @param action Action to test
 * @param expect argument to define how many data items must be collected.
 * - StatesEmitted -> Amount of states that must be collected before stopping execution
 * - EffectsEmitted -> Amount of effects that must be collected before stopping execution
 * - AnyEmitted -> Amount of effects and states that must be collected before stopping execution
 * - DoNotExpect -> Does not await for completion
 * - Unlimited -> Awaits for intent completion. Default value.
 */
suspend fun <State : Any, Effect : Any, Action : Any> ActionContainerHost<State, Effect, Action>.testOn(
    action: Action,
    withState: State? = null,
    expect: ExpectedEmissions = Unlimited,
) = testIntent(
    withState = withState,
    expect = expect
) { on(action) }

/**
 * Call this method to start testing an intent
 *
 * @param testBlock intent reference or testing content
 * @param expect argument to define how many data items must be collected.
 * - StatesEmitted -> Amount of states that must be collected before stopping execution
 * - EffectsEmitted -> Amount of effects that must be collected before stopping execution
 * - AnyEmitted -> Amount of effects and states that must be collected before stopping execution
 * - DoNotExpect -> Does not await for completion
 * - Unlimited -> Awaits for intent completion. Default value.
 * @param withState the state initialized before the test. By default will be the host initial state
 */
suspend fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> Host.testIntent(
    withState: State? = null,
    expect: ExpectedEmissions = Unlimited,
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    val initialState = withState ?: container.asStateContainer().initialState

    cancelOngoingExecutions()
    container.asStateContainer().update { initialState }

    val testContainer = container.buildTestContainer().also { delegate(it) }

    testContainer.reset()

    testBlock()

    with(testContainer) {
        when (expect) {
            Unlimited -> await()
            DoNotExpect -> launchJobs()
            else -> await {
                expect is StatesEmitted && expect.count == emittedStates.size ||
                        expect is EffectsEmitted && expect.count == emittedEffects.size ||
                        expect is AnyEmitted && expect.count == emittedStates.size + emittedEffects.size
            }
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
 * @param initialState the state initialized before the test. By default will be the host initial state
 * @param expect argument to define how many data items must be collected.
 * - StatesEmitted -> Amount of states that must be collected before stopping execution
 * - EffectsEmitted -> Amount of effects that must be collected before stopping execution
 * - AnyEmitted -> Amount of effects and states that must be collected before stopping execution
 * - DoNotExpect -> Does not await for completion
 * - Unlimited -> Awaits for intent completion. Default value.
 */
suspend fun <State : Any, Effect : Any> testConstructor(
    initialState: State? = null,
    expect: ExpectedEmissions = Unlimited,
    builder: () -> StateContainerHost<State, Effect>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()

    val chosenInitialState = initialState ?: host.currentState
    initialState?.let { host.container.asStateContainer().update { it } }
    val testContainer = host.container.buildTestContainer().also { host.delegate(it) }

    with(testContainer) {
        when (expect) {
            Unlimited -> await()
            DoNotExpect -> launchJobs()
            else -> await {
                expect is StatesEmitted && expect.count == emittedStates.size ||
                        expect is EffectsEmitted && expect.count == emittedEffects.size ||
                        expect is AnyEmitted && expect.count == emittedStates.size + emittedEffects.size
            }
        }
        TestResult(
            initialState = chosenInitialState,
            emittedStates = emittedStates,
            emittedEffects = emittedEffects,
            emittedElements = emittedElements,
        )
    }
}

internal val <State : Any, Effect : Any> StateContainer<State, Effect>.coroutineContext
    get() = coroutineScope.coroutineContext