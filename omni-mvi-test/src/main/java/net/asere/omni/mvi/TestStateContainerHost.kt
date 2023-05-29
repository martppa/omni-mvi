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
    takeStates: Int? = null,
    takeEffects: Int? = null,
) = testIntent(
    takeStates = takeStates,
    takeEffects = takeEffects
) { on(action) }

suspend fun <State, Effect, Host : StateContainerHost<State, Effect>> Host.testIntent(
    takeStates: Int? = null,
    takeEffects: Int? = null,
    testBlock: Host.() -> Unit
) = withContext(ExecutableContainer.blockedContext()) {
    if (takeStates != null && takeStates <= 0)
        throw IllegalArgumentException("takeStates argument should be grater than 0 if set")
    if (takeEffects != null && takeEffects <= 0)
        throw IllegalArgumentException("takeEffects argument should be grater than 0 if set")
    val testContainer = TestStateContainer(container)
    delegate(testContainer)
    awaitJobs()
    testContainer.reset()
    testBlock()
    with(testContainer) {
        awaitJobs {
            takeStates != null && takeStates == emittedStates.size ||
                    takeEffects != null && takeEffects == emittedEffects.size
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