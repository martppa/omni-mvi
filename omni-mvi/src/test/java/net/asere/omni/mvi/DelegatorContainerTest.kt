package net.asere.omni.mvi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import net.asere.omni.mvi.shared.test.stateContainerHost
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DelegatorContainerTest {

    @Test
    fun `On DelegatorContainer with delegate should update both`() {
        val baseHost = stateContainerHost<String, String>("Initial")
        val delegator = DelegatorContainer(baseHost.container)

        val delegateHost = stateContainerHost<String, String>("DelegateInitial")
        val delegateContainer = delegateHost.container.asStateContainer()

        delegator.delegate(delegateContainer)

        delegator.update { "NewState" }
        assertEquals("NewState", baseHost.container.asStateContainer().state.value)
        assertEquals("NewState", delegateContainer.state.value)
    }

    @Test
    fun `On DelegatorContainer clearDelegate should stop delegating`() {
        val baseHost = stateContainerHost<String, String>("Initial")
        val delegator = DelegatorContainer(baseHost.container)

        val delegateHost = stateContainerHost<String, String>("DelegateInitial")
        val delegateContainer = delegateHost.container.asStateContainer()

        delegator.delegate(delegateContainer)
        delegator.clearDelegate()

        delegator.update { "OnlyBase" }
        assertEquals("OnlyBase", baseHost.container.asStateContainer().state.value)
        assertEquals("DelegateInitial", delegateContainer.state.value)
    }

    @Test
    fun `On DelegatorContainer with delegate should post effect to both`() = runTest {
        val baseHost = stateContainerHost<String, String>("Initial")
        val delegator = DelegatorContainer(baseHost.container)

        val delegateHost = stateContainerHost<String, String>("DelegateInitial")
        val delegateContainer = delegateHost.container.asStateContainer()

        delegator.delegate(delegateContainer)

        val baseEffects = mutableListOf<String>()
        val delegateEffects = mutableListOf<String>()

        val job1 = launch {
            baseHost.container.asStateContainer().effect.collect { baseEffects.add(it) }
        }
        val job2 = launch {
            delegateContainer.effect.collect { delegateEffects.add(it) }
        }

        yield()

        delegator.post("TestEffect")

        yield()

        assertEquals(listOf("TestEffect"), baseEffects)
        assertEquals(listOf("TestEffect"), delegateEffects)

        job1.cancel()
        job2.cancel()
    }
}
