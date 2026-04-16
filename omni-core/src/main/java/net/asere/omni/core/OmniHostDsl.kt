package net.asere.omni.core

/**
 * DSL marker for Omni MVI host configurations.
 *
 * This annotation prevents accidental access to methods from outer DSL scopes
 * when nesting DSL calls, ensuring that the DSL remains typesafe and predictable.
 */
@DslMarker
annotation class OmniHostDsl
