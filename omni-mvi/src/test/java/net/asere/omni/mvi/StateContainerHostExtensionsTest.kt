package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import net.asere.omni.mvi.shared.test.stateContainerHost

@OptIn(ExperimentalCoroutinesApi::class)
class StateContainerHostExtensionsTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun testScope() = CoroutineScope(SupervisorJob() + testDispatcher)

    @Test
    fun `On intentScope execution should return block result`() = runTest(testDispatcher) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val result = host.intentScope {
            "Hello from scope"
        }
        assertEquals("Hello from scope", result)
    }

    @Test
    fun `On suspendIntent execution should return block result`() = runTest(testDispatcher) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val result = host.suspendIntent {
            "Hello from suspendIntent"
        }
        assertEquals("Hello from suspendIntent", result)
    }

    @Test
    fun `On intentJob execution should return active job`() = runTest(testDispatcher) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val job = host.intentJob {
            reduce { "StateUpdated" }
        }
        assertTrue(job.isActive)
        job.join()
        assertEquals("StateUpdated", host.currentState)
    }

    @Test
    fun `On observeState should trigger callback on state changes`() = runTest(testDispatcher) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val states = mutableListOf<String>()
        host.observeState {
            states.add(it)
        }
        
        testScheduler.advanceUntilIdle()
        
        host.intent {
            reduce { "State1" }
        }
        testScheduler.advanceUntilIdle()
        
        host.intent {
            reduce { "State2" }
        }
        testScheduler.advanceUntilIdle()
        
        assertEquals(listOf("Initial", "State1", "State2"), states)
        (host.container.asStateContainer() as net.asere.omni.core.ExecutableContainer).cancel()
    }

    @Test
    fun `On observeEffect should trigger callback on effects posted`() = runTest(testDispatcher) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val effects = mutableListOf<String>()
        host.observeEffect {
            effects.add(it)
        }
        
        testScheduler.advanceUntilIdle()
        
        host.intent {
            post("Effect1")
        }
        testScheduler.advanceUntilIdle()
        
        host.intent {
            post("Effect2")
        }
        testScheduler.advanceUntilIdle()
        
        assertEquals(listOf("Effect1", "Effect2"), effects)
        (host.container.asStateContainer() as net.asere.omni.core.ExecutableContainer).cancel()
    }
}
