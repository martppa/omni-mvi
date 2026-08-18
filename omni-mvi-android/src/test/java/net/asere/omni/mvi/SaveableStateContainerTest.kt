package net.asere.omni.mvi

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import net.asere.omni.mvi.shared.test.stateContainerHost
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SaveableStateContainerTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `On SaveableStateContainer initialized with empty SavedStateHandle should use initialState`() {
        val savedStateHandle = SavedStateHandle()
        val scope = CoroutineScope(SupervisorJob() + testDispatcher)
        val container = SaveableStateContainer<String, String>(
            initialState = "DefaultInitial",
            savedStateHandle = savedStateHandle,
            coroutineScope = scope,
            coroutineExceptionHandler = net.asere.omni.core.EmptyCoroutineExceptionHandler,
        )

        assertEquals("DefaultInitial", container.state.value)
    }

    @Test
    fun `On SaveableStateContainer initialized with pre-existing state should restore it`() {
        val savedStateHandle = SavedStateHandle(mapOf("omni_state" to "RestoredState"))
        val scope = CoroutineScope(SupervisorJob() + testDispatcher)
        val container = SaveableStateContainer<String, String>(
            initialState = "DefaultInitial",
            savedStateHandle = savedStateHandle,
            coroutineScope = scope,
            coroutineExceptionHandler = net.asere.omni.core.EmptyCoroutineExceptionHandler,
        )

        assertEquals("RestoredState", container.state.value)
    }

    @Test
    fun `On update should persist state in SavedStateHandle`() {
        val savedStateHandle = SavedStateHandle()
        val scope = CoroutineScope(SupervisorJob() + testDispatcher)
        val container = SaveableStateContainer<String, String>(
            initialState = "DefaultInitial",
            savedStateHandle = savedStateHandle,
            coroutineScope = scope,
            coroutineExceptionHandler = net.asere.omni.core.EmptyCoroutineExceptionHandler,
        )

        container.update { "NewUpdatedState" }

        assertEquals("NewUpdatedState", container.state.value)
        assertEquals("NewUpdatedState", savedStateHandle.get<String>("omni_state"))
    }

    @Test
    fun `On post should emit effect`() = runTest(testDispatcher) {
        val savedStateHandle = SavedStateHandle()
        val scope = CoroutineScope(SupervisorJob() + testDispatcher)
        val container = SaveableStateContainer<String, String>(
            initialState = "DefaultInitial",
            savedStateHandle = savedStateHandle,
            coroutineScope = scope,
            coroutineExceptionHandler = net.asere.omni.core.EmptyCoroutineExceptionHandler,
        )

        val effects = mutableListOf<String>()
        val collectJob = launch {
            container.effect.collect { effects.add(it) }
        }

        yield()
        container.post("Effect1")
        yield()

        assertEquals(listOf("Effect1"), effects)
        collectJob.cancel()
    }

    @Test
    fun `On saveableStateContainer extension should correctly configure container`() {
        val savedStateHandle = SavedStateHandle()
        val scope = CoroutineScope(SupervisorJob() + testDispatcher)
        val host = stateContainerHost<String, String>("InitialState")

        val saveableContainer = host.saveableStateContainer(
            initialState = "InitialState",
            savedStateHandle = savedStateHandle,
            coroutineScope = scope,
            coroutineExceptionHandler = net.asere.omni.core.EmptyCoroutineExceptionHandler,
        )

        assertEquals("InitialState", saveableContainer.state.value)
    }
}
