package net.asere.omni.core

import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ExecutionScopeTest {

    @Test
    fun `On onError must update errorBlock`() {
        val scope = ExecutionScope()
        val mockErrorBlock: (Throwable) -> Unit = mockk(relaxed = true)
        
        scope.onError(mockErrorBlock)
        
        val exception = RuntimeException("Test error")
        scope.errorBlock(exception)
        
        verify { mockErrorBlock(exception) }
    }

    @Test
    fun `On map must delegate errors to target scope`() = runBlocking {
        val parentScope = ExecutionScope()
        val childScope = ExecutionScope()
        val mockChildErrorBlock: (Throwable) -> Unit = mockk(relaxed = true)
        
        childScope.onError(mockChildErrorBlock)
        
        val mappedBlock = parentScope.map(childScope) {
            // This block is intended to be executed in the context of parentScope
        }
        
        val exception = RuntimeException("Parent error")
        parentScope.errorBlock(exception)
        
        verify { mockChildErrorBlock(exception) }
    }

    @Test
    fun `On map should return the provided block`() = runBlocking {
        val parentScope = ExecutionScope()
        val childScope = ExecutionScope()
        var blockExecuted = false
        val block: suspend ExecutionScope.() -> Unit = {
            blockExecuted = true
        }
        
        val resultBlock = parentScope.map(childScope, block)
        resultBlock(childScope)
        
        assert(blockExecuted)
    }
}
