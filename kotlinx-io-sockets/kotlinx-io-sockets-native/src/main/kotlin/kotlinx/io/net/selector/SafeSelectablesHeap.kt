package kotlinx.io.net.selector

import platform.posix.memset

internal actual class SafeSelectablesHeap actual constructor(size: Int) {
    actual fun add(element: Selectable): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual fun poll(): Selectable? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    actual val isEmpty: Boolean
        get() = TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}

internal actual fun <T : Any> Array<T?>.zero(count: Int) {
    repeat(count) {
        this[it] = null
    }
}
