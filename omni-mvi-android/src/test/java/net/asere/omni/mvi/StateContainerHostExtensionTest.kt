package net.asere.omni.mvi

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StateContainerHostExtensionTest {

    private val testDispatcher = StandardTestDispatcher()

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle = registry

        fun handleLifecycleEvent(event: Lifecycle.Event) {
            registry.handleLifecycleEvent(event)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(android.os.Looper::class)
        every { android.os.Looper.getMainLooper() } returns mockk(relaxed = true)

        mockkStatic(androidx.arch.core.executor.ArchTaskExecutor::class)
        val archTaskExecutor = mockk<androidx.arch.core.executor.ArchTaskExecutor>()
        every { androidx.arch.core.executor.ArchTaskExecutor.getInstance() } returns archTaskExecutor
        every { archTaskExecutor.isMainThread } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    private fun testScope() = CoroutineScope(SupervisorJob() + testDispatcher)

    @Test
    fun `On observeState with repeatOnLifecycle should collect state when lifecycle is at least STARTED`() = runTest(
        testDispatcher
    ) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val lifecycleOwner = TestLifecycleOwner()

        val observedStates = mutableListOf<String>()

        host.observeState(
            lifecycleOwner = lifecycleOwner,
            lifecycleState = Lifecycle.State.STARTED,
        ) {
            observedStates.add(it)
        }

        testScheduler.advanceUntilIdle()
        assertEquals(emptyList<String>(), observedStates)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Initial"), observedStates)

        host.intent {
            reduce { "State1" }
        }
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Initial", "State1"), observedStates)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        testScheduler.advanceUntilIdle()

        host.intent {
            reduce { "State2" }
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Initial", "State1"), observedStates)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Initial", "State1", "State2"), observedStates)
    }

    @Test
    fun `On observeEffect with repeatOnLifecycle should collect effects when lifecycle is at least STARTED`() = runTest(
        testDispatcher
    ) {
        val host = stateContainerHost<String, String>("Initial", testScope())
        val lifecycleOwner = TestLifecycleOwner()

        val observedEffects = mutableListOf<String>()

        host.observeEffect(
            lifecycleOwner = lifecycleOwner,
            lifecycleState = Lifecycle.State.STARTED,
        ) {
            observedEffects.add(it)
        }

        testScheduler.advanceUntilIdle()
        assertEquals(emptyList<String>(), observedEffects)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        testScheduler.advanceUntilIdle()

        host.intent {
            post("Effect1")
        }
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Effect1"), observedEffects)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        testScheduler.advanceUntilIdle()

        host.intent {
            post("Effect2")
        }
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("Effect1"), observedEffects)

        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        testScheduler.advanceUntilIdle()

        host.intent {
            post("Effect3")
        }
        testScheduler.advanceUntilIdle()
        assertEquals(listOf("Effect1", "Effect2", "Effect3"), observedEffects)
    }
}
