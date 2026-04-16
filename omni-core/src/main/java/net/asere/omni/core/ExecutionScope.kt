package net.asere.omni.core

/**
 * Accessible scope from within an execution block.
 *
 * This class provides a way to handle errors locally within an [ExecutableContainer.execute] block.
 * Subclasses can extend this to provide more specific data or functionality (e.g., state or side effects).
 *
 * @property errorBlock A callback that is triggered when a [Throwable] is caught during execution.
 */
open class ExecutionScope(
    internal var errorBlock: (Throwable) -> Unit = {}
)

/**
 * Allows you to set a logic to react to possible thrown errors
 * from within an execution block.
 *
 * @param block The lambda to execute when an error occurs.
 */
@OmniHostDsl
fun ExecutionScope.onError(
    block: (Throwable) -> Unit
) {
    errorBlock = block
}

/**
 * Maps the error behavior from one [ExecutionScope] to another and prepares a block for execution.
 *
 * This is useful when nesting execution scopes or delegating execution to a different scope type.
 *
 * @param scope The target scope to map errors to.
 * @param block The suspend block to be executed within the target [scope].
 * @return A suspend function that, when called, sets up error delegation and runs the block.
 */
fun <Scope : ExecutionScope> ExecutionScope.map(
    scope: Scope,
    block: suspend Scope.() -> Unit
): suspend Scope.() -> Unit {
    onError { scope.errorBlock(it) }
    return block
}
