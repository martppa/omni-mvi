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
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import net.asere.omni.mvi.shared.test.stateContainerHost

@OptIn(ExperimentalCoroutinesApi::class)
class OverrideContainerTest {

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

    private class Host(scope: CoroutineScope) : OverrideContainerHost<String, String> {
        override val container = stateContainerHost<String, String>("Initial", scope)
            .container
            .buildOverrideContainer()
    }

    @Test
    fun `On overrideIntent with same ID should cancel previous and run latest`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var firstRunCompleted = false
        var secondRunCompleted = false

        host.overrideIntent("Search") {
            delay(100)
            firstRunCompleted = true
        }

        testScheduler.advanceTimeBy(10)
        assertFalse(firstRunCompleted)

        host.overrideIntent("Search") {
            secondRunCompleted = true
        }

        testScheduler.advanceUntilIdle()

        assertFalse(firstRunCompleted)
        assertTrue(secondRunCompleted)
    }

    @Test
    fun `On overrideIntent with different IDs should run both concurrently`() = runTest(testDispatcher) {
        val host = Host(testScope())
        var firstRunCompleted = false
        var secondRunCompleted = false

        host.overrideIntent("Task1") {
            delay(100)
            firstRunCompleted = true
        }

        host.overrideIntent("Task2") {
            delay(100)
            secondRunCompleted = true
        }

        testScheduler.advanceUntilIdle()

        assertTrue(firstRunCompleted)
        assertTrue(secondRunCompleted)
    }

    @Test
    fun `On asOverrideContainer must resolve from decoration stack`() {
        val host = Host(testScope())
        val overrideContainer = host.container.asOverrideContainer()
        assertNotNull(overrideContainer)
    }
}
