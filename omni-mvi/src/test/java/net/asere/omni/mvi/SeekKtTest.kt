package net.asere.omni.mvi

import net.asere.omni.mvi.shared.test.stateContainerHost
import org.junit.Assert.assertTrue
import org.junit.Test

private open class FakeBaseContainer(container: Container<Any, Any>)
    : ContainerDecorator<Any, Any>(container)

private class FakeContainer1(container: Container<Any, Any>) : FakeBaseContainer(container)
private class FakeContainer2(container: Container<Any, Any>) : FakeBaseContainer(container)
private class FakeContainer3(container: Container<Any, Any>) : FakeBaseContainer(container)
private class FakeContainer4(container: Container<Any, Any>) : FakeBaseContainer(container)

class SeekKtTest {

    @Test
    fun `Assert seek method performs a search of desired container`() {
        FakeContainer1(stateContainerHost<Any, Any>(Unit).container)
            .decorate { FakeContainer2(it) }
            .decorate { FakeContainer3(it) }
            .decorate { FakeContainer4(it) }
            .seek<FakeContainer3> { it is FakeContainer3 }
        assertTrue(true)
    }
}