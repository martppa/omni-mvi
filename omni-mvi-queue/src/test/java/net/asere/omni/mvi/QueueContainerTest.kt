package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import net.asere.omni.mvi.shared.test.stateContainerHost

@OptIn(ExperimentalCoroutinesApi::class)
class QueueContainerTest {

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

    private class Host(scope: CoroutineScope) : QueueContainerHost<String, String> {
        override val container = stateContainerHost<String, String>("Initial", scope)
            .container
            .buildQueueContainer()
    }

    @Test
    fun `On queueIntent should execute intents sequentially in order`() = runTest(testDispatcher) {
        val host = Host(testScope())
        val sequence = mutableListOf<String>()

        host.queueIntent {
            sequence.add("start1")
            delay(100)
            sequence.add("finish1")
        }

        host.queueIntent {
            sequence.add("start2")
            delay(50)
            sequence.add("finish2")
        }

        host.queueIntent {
            sequence.add("start3")
            sequence.add("finish3")
        }

        testScheduler.advanceUntilIdle()

        val expected = listOf(
            "start1", "finish1",
            "start2", "finish2",
            "start3", "finish3",
        )
        assertEquals(expected, sequence)
    }

    @Test
    fun `On clearQueue should cancel pending and allow running to finish`() = runTest(testDispatcher) {
        val host = Host(testScope())
        val sequence = mutableListOf<String>()

        host.queueIntent {
            sequence.add("start1")
            delay(100)
            sequence.add("finish1")
        }

        host.queueIntent {
            sequence.add("start2")
            sequence.add("finish2")
        }

        testScheduler.advanceTimeBy(10)
        assertEquals(listOf("start1"), sequence)

        host.clearQueue()
        testScheduler.advanceUntilIdle()

        // start2 (pending) should not have run, and start1 (already active) finishes
        assertEquals(listOf("start1", "finish1"), sequence)
    }

    @Test
    fun `On queueIntent after clearQueue should auto-restart and execute`() = runTest(testDispatcher) {
        val host = Host(testScope())
        val sequence = mutableListOf<String>()

        host.queueIntent {
            sequence.add("start1")
        }
        testScheduler.advanceUntilIdle()

        host.clearQueue()
        testScheduler.advanceUntilIdle()

        host.queueIntent {
            sequence.add("start2")
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("start1", "start2"), sequence)
    }

    @Test
    fun `On Channel isClosed should return true when closed`() {
        val channel = Channel<Int>()
        channel.close()
        assertTrue(channel.isClosed())
    }

    @Test
    fun `On asQueueContainer must resolve from decoration stack`() {
        val host = Host(testScope())
        val queueContainer = host.container.asQueueContainer()
        assertNotNull(queueContainer)
    }
}
