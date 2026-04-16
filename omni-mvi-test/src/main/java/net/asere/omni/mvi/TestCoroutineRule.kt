package net.asere.omni.mvi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * A JUnit4 [TestWatcher] rule that overrides the Main dispatcher with a [TestDispatcher].
 *
 * This is essential for unit testing code that uses `Dispatchers.Main`.
 *
 * @property dispatcher The dispatcher to use during tests. Defaults to [StandardTestDispatcher].
 */
class TestCoroutineRule(
    private val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    /**
     * Set up the Main dispatcher before each test.
     */
    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    /**
     * Reset the Main dispatcher to its original state after each test.
     */
    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}
