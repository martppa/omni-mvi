package net.asere.omni.mvi

import kotlinx.coroutines.withContext

fun <State, Effect> TestResult<State, Effect>
        .evaluate(block: TestResult<State, Effect>.() -> Unit) = this.block()

class TestResult<State, Effect>(
    val emittedStates: List<State>,
    val emittedEffects: List<Effect>
)

suspend fun <State, Effect, Action> ActionContainerHost<State, Effect, Action>.testOn(
    action: Action
) = testIntent { on(action) }

suspend fun <State, Effect, Host : StateContainerHost<State, Effect>> Host.testIntent(
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    val testContainer = TestStateContainer(container)
    delegate(testContainer)
    awaitJobs()
    testContainer.reset()
    testBlock()
    awaitJobs()
    with(testContainer) { TestResult(emittedStates, emittedEffects) }
}

suspend fun <State, Effect> testConstructor(
    builder: () -> StateContainerHost<State, Effect>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()
    val testContainer = TestStateContainer(host.container)
    host.delegate(testContainer)
    host.awaitJobs()
    with(testContainer) { TestResult(emittedStates, emittedEffects) }
}