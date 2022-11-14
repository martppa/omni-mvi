package net.asere.omni.mvi

import net.asere.omni.mvi.shared.test.stateContainerHost
import org.junit.Assert.assertEquals
import org.junit.Test

class DecorateKtTest {

    @Test
    fun `On decorate, decorating object must be passed to block`() {
        val container = stateContainerHost<Any, Any, Any>(Unit).container
        container.decorate { assertEquals(it, container); it }
    }
}