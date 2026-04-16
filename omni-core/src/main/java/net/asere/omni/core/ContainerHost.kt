package net.asere.omni.core

/**
 * Interface for an object that hosts an Omni MVI [Container].
 *
 * A [ContainerHost] typically exposes a [container] that manages state and
 * side effects. Classes implementing this interface can use the [execute]
 * extension functions to run intents.
 */
interface ContainerHost {
    /**
     * The [Container] instance hosted by this object.
     */
    val container: Container
}
