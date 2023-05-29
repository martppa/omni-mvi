package net.asere.omni.mvi

import kotlinx.coroutines.withContext
import java.lang.IllegalArgumentException

fun <State, Effect> TestResult<State, Effect>.evaluate(block: TestResult<State, Effect>.() -> Unit) =
    this.block()

class TestResult<State, Effect>(
    val emittedStates: List<State>,
    val emittedEffects: List<Effect>
)

suspend fun <State, Effect, Action> ActionContainerHost<State, Effect, Action>.testOn(
    action: Action,
    take: Take? = null,
) = testIntent(
    take = take
) { on(action) }

suspend fun <State, Effect, Host : StateContainerHost<State, Effect>> Host.testIntent(
    take: Take? = null,
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    if (take != null && take.count <= 0)
        throw IllegalArgumentException("take argument count should be grater than 0 if set")
    val testContainer = TestStateContainer(container)
    delegate(testContainer)
    awaitJobs()
    testContainer.reset()
    testBlock()
    with(testContainer) {
        awaitJobs {
            take is TakeStates && take.count == emittedStates.size ||
                    take is TakeEffects && take.count == emittedEffects.size
        }
        TestResult(emittedStates, emittedEffects)
    }
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