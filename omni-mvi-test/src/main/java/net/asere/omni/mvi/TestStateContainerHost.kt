package net.asere.omni.mvi

import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
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

class TestStateContainerHost<State : Any, Effect : Any, Host : StateContainerHost<State, Effect>> internal constructor(
    internal val host: Host,
    internal val scope: TestScope,
)

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
