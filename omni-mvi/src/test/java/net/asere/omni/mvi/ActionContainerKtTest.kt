package net.asere.omni.mvi

import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class ActionContainerKtTest {

    private val actionValue = Random.nextInt()

    private fun onActionFunc(action: Int) {
        assertEquals(action, actionValue)
    }

    private val host = object : ActionContainerHost<Any, Any, Int> {
        override val container: ActionContainer<Any, Any, Int> = stateContainer(
            initialState = Unit
        ).onAction(::onActionFunc)
    }

    @Test
    fun `On 'on' call should trigger onAction callback inside host`() = runTest {
        host.on(actionValue)
    }
}