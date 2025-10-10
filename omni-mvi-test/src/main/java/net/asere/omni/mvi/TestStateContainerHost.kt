package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer
import kotlin.coroutines.coroutineContext

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
 * @param policy argument to define the behavior of the execution.
 * - RunUntil.StatesEmitted -> Amount of states that must be collected before stopping execution
 * - RunUntil.EffectsEmitted -> Amount of effects that must be collected before stopping execution
 * - RunUntil.TotalEmitted -> Amount of effects and states that must be collected before stopping execution
 * - DoNotAwait -> Does not await for completion
 * - Unlimited -> Awaits for intent completion. Default value.
 */
suspend fun <State : Any, Effect : Any, Action : Any> ActionContainerHost<State, Effect, Action>.testOn(
    action: Action,
    withState: State? = null,
    policy: ExecutionPolicy = Unlimited,
) = testIntent(
    withState = withState,
    policy = policy
) { on(action) }

/**
 * Call this method to start testing an intent
 *
 * @param testBlock intent reference or testing content
 * @param policy argument to define the behavior of the execution.
 * - RunUntil.StatesEmitted -> Amount of states that must be collected before stopping execution
 * - RunUntil.EffectsEmitted -> Amount of effects that must be collected before stopping execution
 * - RunUntil.TotalEmitted -> Amount of effects and states that must be collected before stopping execution
 * - DoNotAwait -> Does not await for completion
 * - Unlimited -> Awaits for intent completion. Default value.
 * @param withState the state initialized before the test. By default will be the host initial state
 */
suspend fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> Host.testIntent(
    withState: State? = null,
    policy: ExecutionPolicy = Unlimited,
    testBlock: Host.() -> Unit
): TestResult<State, Effect> {
    val testContext = coroutineContext
    return withContext(ExecutableContainer.blockedContext()) {
        val initialState = withState ?: container.asStateContainer().initialState

        cancelOngoingExecutions()
        container.asStateContainer().update { initialState }
        container.onError {
            CoroutineScope(testContext).launch {
                throw it
            }
        }
        val testContainer = container.buildTestContainer().also { delegate(it) }
        testContainer.reset()
        testBlock()

        with(testContainer) {
            runWithPolicy(policy)
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
 * @param policy argument to define the behavior of the execution.
 * - RunUntil.StatesEmitted -> Amount of states that must be collected before stopping execution
 * - RunUntil.EffectsEmitted -> Amount of effects that must be collected before stopping execution
 * - RunUntil.TotalEmitted -> Amount of effects and states that must be collected before stopping execution
 * - DoNotAwait -> Does not await for completion
 * - Unlimited -> Awaits for intent completion. Default value.
 */
suspend fun <State : Any, Effect : Any> testConstructor(
    initialState: State? = null,
    policy: ExecutionPolicy = Unlimited,
    builder: () -> StateContainerHost<State, Effect>
): TestResult<State, Effect> {
    val testContext = coroutineContext
    return withContext(ExecutableContainer.blockedContext()) {
        val host = builder()

        val chosenInitialState = initialState ?: host.currentState
        initialState?.let { host.container.asStateContainer().update { it } }
        val testContainer = host.container.buildTestContainer().also { host.delegate(it) }
        host.onError {
            CoroutineScope(testContext).launch {
                throw it
            }
        }

        with(testContainer) {
            runWithPolicy(policy)
            TestResult(
                initialState = chosenInitialState,
                emittedStates = emittedStates,
                emittedEffects = emittedEffects,
                emittedElements = emittedElements,
            )
        }
    }
}

internal fun <State : Any, Effect : Any> StateContainerHost<State, Effect>.onError(block: (Throwable) -> Unit) {
    container.coroutineExceptionHandler.onError(block)
}

internal fun <State : Any, Effect : Any> StateContainer<State, Effect>.onError(block: (Throwable) -> Unit) {
    coroutineExceptionHandler.onError(block)
}

private suspend fun <State : Any, Effect : Any> TestStateContainer<State, Effect>.runWithPolicy(
    policy: ExecutionPolicy,
) {
    when (policy) {
        Unlimited -> joinChildren()
        DoNotAwait -> launchJobs()
        is RunUntil -> {
            await(mode = policy.mode) {
                policy is RunUntil.StatesEmitted && policy.count == emittedStates.size ||
                        policy is RunUntil.EffectsEmitted && policy.count == emittedEffects.size ||
                        policy is RunUntil.TotalEmitted &&
                        policy.count == emittedStates.size + emittedEffects.size
            }
        }
    }
}
