package net.asere.omni.core

/**
 * Accessible scope from within an execution block
 */
open class ExecutionScope(
    internal var errorBlock: (Throwable) -> Unit = {}
)

/**
 * Allows you to set a logic to react to possible thrown errors
 * from within an execution block
 */
@OmniHostDsl
fun ExecutionScope.onError(
    block:  (Throwable) -> Unit
) { errorBlock = block }

/**
 * Map execution scopes behaviors
 */
fun <Scope : ExecutionScope> ExecutionScope.map(
    scope: Scope,
    block: suspend Scope.() -> Unit
): suspend Scope.() -> Unit  {
    onError { scope.errorBlock(it) }
    return block
}