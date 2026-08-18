package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.asere.omni.mvi.shared.test.stateContainerHost
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LockContainerTest {

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

    private class Host(scope: CoroutineScope) : LockContainerHost<String, String> {
        override val container = stateContainerHost<String, String>("Initial", scope)
            .container
            .buildLockContainer()
    }

    @Test
    fun `On lockIntent with identical ID should ignore second execution if first is running`() = runTest(
        testDispatcher
    ) {
        val host = Host(testScope())
        var execution1Count = 0
        var execution2Count = 0

        host.lockIntent("MyIntent") {
            execution1Count++
            delay(100)
        }

        testScheduler.advanceTimeBy(10)

        host.lockIntent("MyIntent") {
            execution2Count++
        }

        testScheduler.advanceUntilIdle()

        assertEquals(1, execution1Count)
        assertEquals(0, execution2Count)
    }

    @Test
    fun `On lockIntent with identical ID should allow second execution if first has completed`() = runTest(
        testDispatcher
    ) {
        val host = Host(testScope())
        var execution1Count = 0
        var execution2Count = 0

        host.lockIntent("MyIntent") {
            execution1Count++
            delay(100)
        }

        testScheduler.advanceUntilIdle()

        host.lockIntent("MyIntent") {
            execution2Count++
        }

        testScheduler.advanceUntilIdle()

        assertEquals(1, execution1Count)
        assertEquals(1, execution2Count)
    }

    @Test
    fun `On lockIntent with different IDs should allow concurrent execution`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var execution1Count = 0
        var execution2Count = 0

        host.lockIntent("Intent1") {
            execution1Count++
            delay(100)
        }

        host.lockIntent("Intent2") {
            execution2Count++
            delay(100)
        }

        testScheduler.advanceUntilIdle()

        assertEquals(1, execution1Count)
        assertEquals(1, execution2Count)
    }

    @Test
    fun `On manually lockIntent and unlockIntent should restrict and then allow execution`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var executionCount = 0

        // Run once first to populate the intent in the intents map
        host.lockIntent("MyIntent") {
            executionCount++
        }
        testScheduler.advanceUntilIdle()
        assertEquals(1, executionCount)

        // Manually lock it
        host.lockIntent("MyIntent")
        testScheduler.advanceUntilIdle()

        // Trigger should be ignored
        host.lockIntent("MyIntent") {
            executionCount++
        }
        testScheduler.advanceUntilIdle()
        assertEquals(1, executionCount)

        // Unlock
        host.unlockIntent("MyIntent")
        testScheduler.advanceUntilIdle()

        // Trigger should run
        host.lockIntent("MyIntent") {
            executionCount++
        }
        testScheduler.advanceUntilIdle()
        assertEquals(2, executionCount)
    }

    @Test
    fun `On cancelIntent should abort running execution and remove lock`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var executionCompleted = false

        host.lockIntent("MyIntent") {
            delay(100)
            executionCompleted = true
        }

        testScheduler.advanceTimeBy(10)
        assertFalse(executionCompleted)

        host.cancelIntent("MyIntent")
        testScheduler.advanceUntilIdle()

        assertFalse(executionCompleted)

        var secondRunCompleted = false
        host.lockIntent("MyIntent") {
            secondRunCompleted = true
        }
        testScheduler.advanceUntilIdle()
        assertTrue(secondRunCompleted)
    }
}
