@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package kotlinx.io.core

import kotlinx.cinterop.*

fun Output.writeFully(src: CPointer<ByteVar>, offset: Int, length: Int) {
    writeFully(src, offset.toLong(), length.toLong())
}

fun Output.writeFully(src: CPointer<ByteVar>, offset: Long, length: Long) {
    require(length >= 0L)
    require(offset >= 0L)

    var position = offset
    var rem = length

    writeWhile { chunk ->
        val size = minOf(chunk.writeRemaining.toLong(), rem).toInt()
        chunk.writeFully(src, position, size)
        position += size
        rem -= size
        rem > 0
    }
}

