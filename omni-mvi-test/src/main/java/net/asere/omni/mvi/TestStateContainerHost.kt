package net.asere.omni.mvi

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.withContext
import net.asere.omni.core.ExecutableContainer

/**
 * Puts the container host in evaluation status and provides a scope for asserting
 * emitted states and effects.
 *
 * @param State The type of the UI state.
 * @param Effect The type of the side effect.
 * @param relaxed If `true`, the evaluation won't fail if some emissions were not asserted.
 *                If `false` (default), it will throw an [IllegalStateException] if there
 *                are any remaining unverified emissions.
 * @param block A lambda executed within an [EvaluationScope] to perform assertions.
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
 * A wrapper class that associates an MVI host with a [TestScope] for testing.
 *
 * @param Host The type of the [StateContainerHost] being tested.
 * @property host The actual host instance.
 * @property scope The [TestScope] used to control coroutine timing.
 */
class TestStateContainerHost<State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> internal constructor(
    internal val host: Host,
    internal val scope: TestScope,
)

/**
 * Factory function to create a [TestStateContainerHost] within a [TestScope].
 *
 * @param block A lambda that returns an instance of the host to be tested.
 * @return A configured [TestStateContainerHost].
 */
fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> TestScope.createTestHost(
    block: () -> Host
): TestStateContainerHost<State, Effect, Host> {
    val stateHost = block()
    val testHost = TestStateContainerHost(
        host = stateHost,
        scope = this
    )
    return testHost
}

/**
 * Launches an intent for testing and records all resulting emissions.
 *
 * @param withState An optional initial state to set before running the intent.
 * @param testBlock A lambda where the intent is invoked on the host.
 * @return A [TestResult] containing all states and effects emitted during the intent execution.
 */
fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>>
        TestStateContainerHost<State, Effect, Host>.testIntent(
    withState: State? = null,
    testBlock: Host.() -> Unit
): TestResult<State, Effect> {
    scope.advanceUntilIdle()

    val initialState = withState ?: host.container.asStateContainer().initialState
    host.container.asStateContainer().update { initialState }
    val testContainer = host.container.buildTestContainer().also { host.delegate(it) }
    testContainer.reset()

    testBlock(host)
    scope.advanceUntilIdle()

    return with(testContainer) {
        TestResult(
            initialState = withState ?: initialState,
            emittedStates = emittedStates,
            emittedEffects = emittedEffects,
            emittedElements = emittedElements,
        )
    }
}

/**
 * Tests the constructor (initialization) logic of a host.
 *
 * This function uses a blocked context to ensure that any intents launched during
 * host initialization are captured sequentially.
 *
 * @param initialState An optional initial state to set.
 * @param builder A lambda that creates the [TestStateContainerHost].
 * @return A [TestResult] containing emissions from the initialization phase.
 */
suspend fun <State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> TestScope.testConstructor(
    initialState: State? = null,
    builder: TestScope.() -> TestStateContainerHost<State, Effect, Host>
): TestResult<State, Effect> {
    return withContext(ExecutableContainer.blockedContext()) {
        val testHost = builder()

        val chosenInitialState = initialState ?: testHost.host.currentState
        initialState?.let { testHost.host.container.asStateContainer().update { it } }
        val testContainer = testHost.host.container.buildTestContainer().also { testHost.host.delegate(it) }

        testHost.host.joinChildren()
        advanceUntilIdle()

        with(testContainer) {
            TestResult(
                initialState = chosenInitialState,
                emittedStates = emittedStates,
                emittedEffects = emittedEffects,
                emittedElements = emittedElements,
            )
        }
    }
}
