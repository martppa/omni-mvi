package net.asere.omni.mvi

@Suppress("UNCHECKED_CAST")
fun <T> Container<*, *, *>.seek(predicate: (Any) -> Boolean): T {
    if (this is ContainerDecorator) {
        if (predicate(this)) return this as T
        return this.container.seek(predicate)
    } else throw RuntimeException("Container decorator fails. Have you wrapped all containers?")
}