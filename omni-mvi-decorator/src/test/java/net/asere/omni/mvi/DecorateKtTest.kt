package net.asere.omni.mvi

import org.junit.Assert.assertEquals
import org.junit.Test

class DecorateKtTest : StateContainerHost<Any, Any, Any> {

    override val container = stateContainer(Unit)

    @Test
    fun `On decorate, decorating object must be passed to block`() {
        container.decorate { assertEquals(it, container); it }
    }
}