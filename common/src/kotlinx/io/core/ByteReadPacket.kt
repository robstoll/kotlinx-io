@file:Suppress("RedundantModalityModifier", "FunctionName")

package kotlinx.io.core

import kotlinx.io.bits.Memory
import kotlinx.io.core.internal.*
import kotlinx.io.pool.*

/**
 * Read-only immutable byte packet. Could be consumed only once however it does support [copy] that doesn't copy every byte
 * but creates a new view instead. Once packet created it should be either completely read (consumed) or released
 * via [release].
 */
class ByteReadPacket internal constructor(head: ChunkBuffer, remaining: Long, pool: ObjectPool<ChunkBuffer>) :
    @kotlin.Suppress("DEPRECATION_ERROR") ByteReadPacketPlatformBase(head, remaining, pool), Input {
    constructor(head: ChunkBuffer, pool: ObjectPool<ChunkBuffer>) : this(head, head.remainingAll(), pool)

    init {
        markNoMoreChunksAvailable()
    }

    /**
     * Returns a copy of the packet. The original packet and the copy could be used concurrently. Both need to be
     * either completely consumed or released via [release]
     */
    final fun copy(): ByteReadPacket = ByteReadPacket(head.copyAll(), remaining, pool)

    final override fun fill() = null

    final override fun fill(destination: Memory, offset: Int, length: Int): Int {
        return 0
    }

    final override fun closeSource() {
    }

    override fun toString(): String {
        return "ByteReadPacket($remaining bytes remaining)"
    }

    companion object {
        val Empty: ByteReadPacket = ByteReadPacket(ChunkBuffer.Empty, 0L, ChunkBuffer.EmptyPool)

        @DangerousInternalIoApi
        val ReservedSize: Int
            get() = Buffer.ReservedSize
    }
}

@Suppress("DEPRECATION")
@DangerousInternalIoApi
@Deprecated(
    "Will be removed in future releases.",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("AbstractInput", "kotlinx.io.core.AbstractInput")
)
abstract class ByteReadPacketPlatformBase protected constructor(
    head: ChunkBuffer,
    remaining: Long,
    pool: ObjectPool<ChunkBuffer>
) : ByteReadPacketBase(head, remaining, pool) {
    @Deprecated("Binary compatibility.", level = DeprecationLevel.HIDDEN)
    constructor(head: IoBuffer,
                remaining: Long,
                pool: ObjectPool<ChunkBuffer>) : this(head as ChunkBuffer, remaining, pool)
}

expect fun ByteReadPacket(
    array: ByteArray, offset: Int = 0, length: Int = array.size,
    block: (ByteArray) -> Unit
): ByteReadPacket

@Suppress("NOTHING_TO_INLINE")
inline fun ByteReadPacket(array: ByteArray, offset: Int = 0, length: Int = array.size): ByteReadPacket {
    return ByteReadPacket(array, offset, length) {}
}
