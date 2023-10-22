package net.asere.omni.core

import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.coroutines.resume

private class RandomScope : ExecutionScope()

class ExecutionScopeTest {

    private val errorBlock: (Throwable) -> Unit = mockk(relaxed = true)

    @Test
    fun `On scope mapping should bypass errors among scopes`(): Unit = runBlocking {
        val thrownException = Exception()
        val scope1 = RandomScope()
        val block1: RandomScope.() -> Unit = {
            onError {
                assertEquals(it, thrownException)
            }
            throw thrownException
        }
        val scope2 = ExecutionScope()
        runBlocking {
            val mappedBlock = scope2.map(scope1) {
                block1()
            }
            try {
                scope1.mappedBlock()
            } catch(ex: Exception) {
                scope2.errorBlock(ex)
            }
        }
    }

    @Test
    fun `On failing scope invocation must call onError`() = runBlocking {
        val scope = ExecutionScope(errorBlock)
        val errorLambda: (Throwable) -> Unit = mockk()
        val execution: ExecutionScope.() -> Unit = { onError(errorLambda) }
        execution(scope)
        assertEquals(scope.errorBlock, errorLambda)
    }
}