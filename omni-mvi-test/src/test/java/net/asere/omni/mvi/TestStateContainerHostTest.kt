package net.asere.omni.mvi

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestStateContainerHostTest {

    @get:Rule
    val coroutineRule = TestCoroutineRule()

    private class TestHost(initialState: String) : StateContainerHost<String, String> {
        override val container = stateContainer(
            initialState = initialState,
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
        )

        fun performAction() {
            intent {
                reduce { "State1" }
                post("Effect1")
                reduce { "State2" }
            }
        }
    }

    private class ConstructorHost : StateContainerHost<String, String> {
        override val container = stateContainer(
            initialState = "Initial",
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main),
        )

        init {
            intent {
                reduce { "ConstructorState" }
                post("ConstructorEffect")
            }
        }
    }

    @Test
    fun `On createTestHost must initialize host wrapper`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        assertNotNull(testHost)
        assertNotNull(testHost.hostBuilder)
        assertEquals(this, testHost.scope)
    }

    @Test
    fun `On testConstructor must capture constructor emissions`() = runTest {
        val testHost = createTestHost { ConstructorHost() }
        val result = testHost.testConstructor()
        
        result.evaluate {
            expectState { "ConstructorState" }
            expectEffect("ConstructorEffect")
        }
    }

    @Test
    fun `On testIntent must capture intent emissions`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        result.evaluate {
            expectState { "State1" }
            expectEffect("Effect1")
            expectState { "State2" }
        }
    }

    @Test
    fun `On evaluate without relaxed must throw if not all states are tested`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(IllegalStateException::class.java) {
            result.evaluate(relaxed = false) {
                expectState { "State1" }
                expectEffect("Effect1")
                // Missing assertion for State2
            }
        }
    }

    @Test
    fun `On evaluate without relaxed must throw if not all effects are tested`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(IllegalStateException::class.java) {
            result.evaluate(relaxed = false) {
                expectState { "State1" }
                // Missing assertion for Effect1
                expectState { "State2" }
            }
        }
    }

    @Test
    fun `On evaluate with relaxed must succeed even if not all emissions are tested`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        result.evaluate(relaxed = true) {
            expectState { "State1" }
        }
    }

    @Test
    fun `On nextState verification must succeed with correct states`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        result.evaluate {
            nextState { previous, current ->
                assertEquals("Initial", previous)
                assertEquals("State1", current)
            }
            nextEffect {
                assertEquals("Effect1", it)
            }
            nextState { previous, current ->
                assertEquals("State1", previous)
                assertEquals("State2", current)
            }
        }
    }

    @Test
    fun `On nextState must throw if expecting state but next is an effect`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(IllegalStateException::class.java) {
            result.evaluate(relaxed = true) {
                expectState { "State1" }
                nextState { _, _ -> } // Here it should be an effect ("Effect1")
            }
        }
    }

    @Test
    fun `On expectState must throw if state transition does not match expectation`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(AssertionError::class.java) {
            result.evaluate(relaxed = true) {
                expectState { "WrongExpectedState" }
            }
        }
    }

    @Test
    fun `On expectEffect must throw if effect does not match expectation`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(AssertionError::class.java) {
            result.evaluate(relaxed = true) {
                expectState { "State1" }
                expectEffect("WrongEffect")
            }
        }
    }

    @Test
    fun `On nextEffect must throw if expecting effect but next is a state`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(IllegalStateException::class.java) {
            result.evaluate(relaxed = true) {
                // First element is State1, but we try to assert effect
                nextEffect { }
            }
        }
    }

    @Test
    fun `On nextState or expectState must throw if there are no more states`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(IllegalStateException::class.java) {
            result.evaluate(relaxed = true) {
                expectState { "State1" }
                expectEffect("Effect1")
                expectState { "State2" }
                expectState { "NoMoreState" }
            }
        }
    }

    @Test
    fun `On nextEffect or expectEffect must throw if there are no more effects`() = runTest {
        val testHost = createTestHost { TestHost("Initial") }
        val result = testHost.testIntent { performAction() }
        
        assertThrows(IllegalStateException::class.java) {
            result.evaluate(relaxed = true) {
                expectState { "State1" }
                expectEffect("Effect1")
                expectEffect("NoMoreEffect")
            }
        }
    }

    @Test
    fun `On asExecutableContainer must resolve executable container`() {
        val host = TestHost("Initial")
        val executable = host.container.asExecutableContainer()
        assertNotNull(executable)
    }

    @Test
    fun `On asDelegatorContainer must resolve delegator container`() {
        val host = TestHost("Initial")
        val delegator = host.container.asDelegatorContainer()
        assertNotNull(delegator)
    }

    @Test
    fun `On delegate and clearDelegate should configure delegator`() {
        val host = TestHost("Initial")
        val dummyContainer = host.container.buildTestContainer()
        
        // Delegate using host extension
        host.delegate(dummyContainer)
        
        // Delegate using container extension
        host.container.delegate(dummyContainer)
        
        // Clear delegate
        host.container.clearDelegate()
    }

    @Test
    fun `On joinChildren must suspend until all children jobs are completed`() = runTest {
        val host = TestHost("Initial")
        var childCompleted = false
        host.intent {
            kotlinx.coroutines.delay(100)
            childCompleted = true
        }
        
        org.junit.Assert.assertTrue(!childCompleted)
        
        // Wait/join children using host extension
        host.joinChildren()
        
        org.junit.Assert.assertTrue(childCompleted)
    }

    @Test
    fun `On container joinChildren must suspend until all children jobs are completed`() = runTest {
        val host = TestHost("Initial")
        var childCompleted = false
        host.intent {
            kotlinx.coroutines.delay(100)
            childCompleted = true
        }
        
        org.junit.Assert.assertTrue(!childCompleted)
        
        // Wait/join children using container extension
        host.container.joinChildren()
        
        org.junit.Assert.assertTrue(childCompleted)
    }
}
