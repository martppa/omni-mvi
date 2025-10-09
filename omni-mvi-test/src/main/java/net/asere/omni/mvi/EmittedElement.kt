package net.asere.omni.mvi

data class EmittedElement(
    val type: Type,
    val element: Any,
) {
    enum class Type {
        State,
        Effect
    }
}
