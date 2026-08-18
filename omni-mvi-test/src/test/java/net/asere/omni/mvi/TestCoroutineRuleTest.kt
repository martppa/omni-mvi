package net.asere.omni.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TestCoroutineRuleTest {

    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule(testDispatcher)

    @Test
    fun `On rule applied main dispatcher is set to the test dispatcher`() = runTest(testDispatcher) {
        var executed = false
        launch(Dispatchers.Main) {
            executed = true
        }
        
        assertTrue(!executed)
        testScheduler.advanceUntilIdle()
        assertTrue(executed)
    }
}
