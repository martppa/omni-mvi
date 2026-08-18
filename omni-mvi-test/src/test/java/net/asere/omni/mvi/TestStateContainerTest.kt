package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.asere.omni.mvi.shared.test.stateContainerHost
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TestStateContainerTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)
    private val baseContainer = stateContainerHost<String, String>("Initial", scope).container

    @Test
    fun `On buildTestContainer should wrap container`() {
        val testContainer = baseContainer.buildTestContainer()
        assertEquals(baseContainer, testContainer.container)
    }

    @Test
    fun `On update should record state emission`() {
        val testContainer = baseContainer.buildTestContainer()
        testContainer.update { "NewState" }

        assertEquals(listOf("NewState"), testContainer.emittedStates)
        assertEquals(1, testContainer.emittedElements.size)
        assertEquals(EmittedElement.Type.State, testContainer.emittedElements[0].type)
        assertEquals("NewState", testContainer.emittedElements[0].element)
    }

    @Test
    fun `On post should record effect emission`() {
        val testContainer = baseContainer.buildTestContainer()
        testContainer.post("Effect1")

        assertEquals(listOf("Effect1"), testContainer.emittedEffects)
        assertEquals(1, testContainer.emittedElements.size)
        assertEquals(EmittedElement.Type.Effect, testContainer.emittedElements[0].type)
        assertEquals("Effect1", testContainer.emittedElements[0].element)
    }

    @Test
    fun `On reset should clear emissions`() {
        val testContainer = baseContainer.buildTestContainer()
        testContainer.update { "State1" }
        testContainer.post("Effect1")

        testContainer.reset()

        assertTrue(testContainer.emittedStates.isEmpty())
        assertTrue(testContainer.emittedEffects.isEmpty())
        assertTrue(testContainer.emittedElements.isEmpty())
    }

    @Test
    fun `On synchronizedAdd and synchronizedClear should modify list`() {
        val list = mutableListOf<String>()
        list.synchronizedAdd("test")
        assertEquals(1, list.size)
        list.synchronizedClear()
        assertTrue(list.isEmpty())
    }

    @Test
    fun `On stateFlow accessed should return flow from container and host`() {
        val host = stateContainerHost<String, String>("Initial", scope)
        assertEquals("Initial", host.stateFlow.value)
        assertEquals("Initial", host.container.stateFlow.value)
    }
}
