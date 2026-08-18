package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
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
class PendingContainerTest {

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

    private class Host(scope: CoroutineScope) : PendingContainerHost<String, String> {
        override val container = stateContainerHost<String, String>("Initial", scope)
            .container
            .buildPendingContainer()
    }

    @Test
    fun `On pendingIntent block should not be executed immediately`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var executed = false

        host.pendingIntent("Pending1") {
            executed = true
        }

        testScheduler.advanceUntilIdle()
        assertFalse(executed)
        assertTrue(host.hasPendingIntent("Pending1"))
        assertTrue(host.hasPendingIntents())
    }

    @Test
    fun `On launchPendingIntent with specific ID should execute and remove it`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var executionCount = 0

        host.pendingIntent("Pending1") {
            executionCount++
            reduce { "Updated" }
        }

        testScheduler.advanceUntilIdle()
        assertEquals(0, executionCount)
        assertEquals("Initial", host.currentState)

        host.launchPendingIntent("Pending1")
        testScheduler.advanceUntilIdle()

        assertEquals(1, executionCount)
        assertEquals("Updated", host.currentState)
        assertFalse(host.hasPendingIntent("Pending1"))
        assertFalse(host.hasPendingIntents())
    }

    @Test
    fun `On launchPendingIntents should execute all pending intents and clear map`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var count1 = 0
        var count2 = 0

        host.pendingIntent("Intent1") {
            count1++
        }
        host.pendingIntent("Intent2") {
            count2++
        }

        testScheduler.advanceUntilIdle()
        assertEquals(0, count1)
        assertEquals(0, count2)

        host.launchPendingIntents()
        testScheduler.advanceUntilIdle()

        assertEquals(1, count1)
        assertEquals(1, count2)
        assertFalse(host.hasPendingIntents())
    }

    @Test
    fun `On clearPendingIntent with specific ID should remove it without execution`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var count1 = 0
        var count2 = 0

        host.pendingIntent("Intent1") {
            count1++
        }
        host.pendingIntent("Intent2") {
            count2++
        }

        testScheduler.advanceUntilIdle()
        assertTrue(host.hasPendingIntent("Intent1"))
        assertTrue(host.hasPendingIntent("Intent2"))

        host.clearPendingIntent("Intent1")
        testScheduler.advanceUntilIdle()

        assertFalse(host.hasPendingIntent("Intent1"))
        assertTrue(host.hasPendingIntent("Intent2"))

        host.launchPendingIntents()
        testScheduler.advanceUntilIdle()

        assertEquals(0, count1)
        assertEquals(1, count2)
    }

    @Test
    fun `On clearPendingIntents should remove all without execution`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var count = 0

        host.pendingIntent("Intent1") {
            count++
        }
        host.pendingIntent("Intent2") {
            count++
        }

        testScheduler.advanceUntilIdle()
        assertTrue(host.hasPendingIntents())

        host.clearPendingIntents()
        testScheduler.advanceUntilIdle()

        assertFalse(host.hasPendingIntents())

        host.launchPendingIntents()
        testScheduler.advanceUntilIdle()

        assertEquals(0, count)
    }

    @Test
    fun `On launchPendingIntent with non-existent ID should do nothing`() = runTest(testDispatcher) {
        val host = Host(testScope())
        host.launchPendingIntent("NonExistent")
        testScheduler.advanceUntilIdle()
        assertFalse(host.hasPendingIntents())
    }

    @Test
    fun `On pendingIntent overwriting existing ID should store latest intent`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var version = 0

        host.pendingIntent("Intent1") {
            version = 1
        }
        host.pendingIntent("Intent1") {
            version = 2
        }

        testScheduler.advanceUntilIdle()
        assertEquals(0, version)

        host.launchPendingIntent("Intent1")
        testScheduler.advanceUntilIdle()

        assertEquals(2, version)
    }
}
