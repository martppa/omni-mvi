package net.asere.omni.core

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.coroutines.EmptyCoroutineContext

@OptIn(ExperimentalCoroutinesApi::class)
class ExecutableContainerTest {

    @Test
    fun `On execution thrown error should be redirected to coroutine exception handler`() = runTest {
        val expectedThrownError = Exception("random error message")
        var actualThrownError: Throwable? = null

        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
                actualThrownError = throwable
            }
        ) {}

        container.execute(
            context = EmptyCoroutineContext,
            start = CoroutineStart.DEFAULT,
            onError = {}
        ) {
            throw expectedThrownError
        }

        container.awaitJobs()
        assertEquals(expectedThrownError, actualThrownError)
    }

    @Test
    fun `On execute must throw an IllegalStateException if the container is not an ExecutableContainer`() = runTest {
        val container = object : Container {
            override val coroutineScope: CoroutineScope = CoroutineScope(EmptyCoroutineContext)
            override val coroutineExceptionHandler: CoroutineExceptionHandler = EmptyCoroutineExceptionHandler
        }
        val host = object : ContainerHost {
            override val container = container
        }
        try {
            host.execute(
                scope = ExecutionScope(),
                block = {}
            )
        } catch (ex: Exception) {
            assertTrue(ex is IllegalStateException)
        }
    }

    @Test
    fun `On execute must call provided block`() = runTest {
        var blockExecuted = false
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler
        ) {}
        val host = object : ContainerHost {
            override val container = container
        }
        host.execute(
            scope = ExecutionScope()
        ) {
            blockExecuted = true
        }
        container.awaitJobs()
        assert(blockExecuted)
    }

    @Test
    fun `On execute must call provided block with expected scope`() = runTest {
        var blockExecutedInScope = false
        val container = object : ExecutableContainer(
            coroutineScope = CoroutineScope(EmptyCoroutineContext),
            coroutineExceptionHandler = EmptyCoroutineExceptionHandler
        ) {}
        val host = object : ContainerHost {
            override val container = container
        }
        val scope = ExecutionScope()
        host.execute(
            scope = scope
        ) {
            blockExecutedInScope = this == scope
        }
        container.awaitJobs()
        assert(blockExecutedInScope)
    }
}