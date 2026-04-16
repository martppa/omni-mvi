package net.asere.omni.mvi

/**
 * A wrapper for a single element emitted by a [TestStateContainer].
 *
 * This class allows for tracking the interleaved order of states and effects.
 *
 * @property type The [Type] of the emission (State or Effect).
 * @property element The actual state object or effect object that was emitted.
 */
data class EmittedElement(
    val type: Type,
    val element: Any,
) {
    /**
     * Enum representing the type of emission.
     */
    enum class Type {
        /** Represents a UI state emission. */
        State,
        /** Represents a side effect emission. */
        Effect
    }
}
