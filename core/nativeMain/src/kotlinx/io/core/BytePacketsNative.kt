package kotlinx.io.core

import kotlinx.cinterop.*
import kotlinx.io.core.internal.*

/**
 * Read at most [limit] bytes to the specified [dst] address
 * @return number of bytes copied
 */
fun ByteReadPacket.readAvailable(dst: CPointer<ByteVar>, limit: Int) =
    readAsMuchAsPossible(dst, limit.toLong(), 0L).toInt()

/**
 * Read at most [limit] bytes to the specified [dst] address
 * @return number of bytes copied
 */
fun ByteReadPacket.readAvailable(dst: CPointer<ByteVar>, limit: Long) = readAsMuchAsPossible(dst, limit, 0L)

/**
 * Read exactly [size] bytes to the specified [dst] address
 * @return number of bytes copied
 */
fun ByteReadPacket.readFully(dst: CPointer<ByteVar>, size: Int): Int {
    val rc = readAsMuchAsPossible(dst, size.toLong(), 0L)
    if (rc != size.toLong())
        throw EOFException("Not enough data in packet to fill buffer: ${size.toLong() - rc} more bytes required")
    return rc.toInt()
}

/**
 * Read exactly [size] bytes to the specified [dst] address
 * @return number of bytes copied
 */
fun ByteReadPacket.readFully(dst: CPointer<ByteVar>, size: Long): Long {
    val rc = readAsMuchAsPossible(dst, size, 0L)
    if (rc != size) throw EOFException("Not enough data in packet to fill buffer: ${size - rc} more bytes required")
    return rc
}

private tailrec fun ByteReadPacket.readAsMuchAsPossible(
    buffer: CPointer<ByteVar>,
    destinationCapacity: Long,
    copied: Long
): Long {
    if (destinationCapacity == 0L) return copied
    val current: ChunkBuffer = prepareRead(1) ?: return copied

    val available = current.readRemaining.toLong()

    return if (destinationCapacity >= available) {
        current.readFully(buffer, 0L, available.toInt())
        releaseHead(current)

        readAsMuchAsPossible((buffer + available)!!, destinationCapacity - available, copied + available)
    } else {
        current.readFully(buffer, 0, destinationCapacity.toInt())
        completeReadHead(current)
        copied + destinationCapacity
    }
}

/**
 * Write all [src] buffer remaining bytes and change it's position accordingly
 */
fun BytePacketBuilder.writeFully(src: CPointer<ByteVar>, size: Int) {
    var remaining = size
    var offset = 0

    while (remaining > 0) {
        write(1) { buffer: Buffer ->
            val srcSize = remaining
            val capacity = buffer.writeRemaining

            val partSize = minOf(srcSize, capacity)
            buffer.writeFully(src, offset, partSize)
            offset += partSize
            remaining -= partSize

            partSize
        }
    }
}
