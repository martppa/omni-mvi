package net.asere.omni.mvi

import kotlinx.coroutines.withContext

fun <State, Effect> TestResult<State, Effect>
        .evaluate(block: TestResult<State, Effect>.() -> Unit) = this.block()

class TestResult<State, Effect>(
    val emittedStates: List<State>,
    val emittedEffects: List<Effect>
)

suspend fun <State, Effect, Action> StateContainerHost<State, Effect, Action>.testOn(
    action: Action
) = withContext(ExecutableContainer.blockedContext()) {
    val testContainer = TestStateContainer(container)
    delegate(testContainer)
    launchAndAwaitJobs()
    testContainer.reset()
    on(action)
    awaitJobs()
    return@withContext with(testContainer) { TestResult(emittedStates, emittedEffects) }
}

suspend fun <State, Effect, Action> testConstructor(
    builder: () -> StateContainerHost<State, Effect, Action>
) = withContext(ExecutableContainer.blockedContext()) {
    val host = builder()
    val testContainer = TestStateContainer(host.container)
    host.delegate(testContainer)
    host.launchAndAwaitJobs()
    host.clearDelegate()
    return@withContext with(testContainer) { TestResult(emittedStates, emittedEffects) }
}