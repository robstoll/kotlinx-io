package kotlinx.io.benchmarks

import kotlinx.io.core.*
import org.jetbrains.gradle.benchmarks.*
import java.io.*
import java.nio.*
import java.util.*

@State(Scope.Benchmark)
class JvmPacketReadBenchmark {
    private final val size = 32 * 1024 * 1024
    private final val array = ByteArray(size)
    private final val packet: ByteReadPacket
    private final val directBuffer = ByteBuffer.allocateDirect(size)!!

    init {
        Random().nextBytes(array)
        packet = BytePacketBuilder().apply {
            writeFully(array)
        }.build()
        directBuffer.put(array)
        directBuffer.clear()
    }

    @Benchmark
    fun bufferedInputStream(): Long {
        val stream = BufferedInputStream(ByteArrayInputStream(array))
        var c = 0L

        while (true) {
            val rc = stream.read()
            if (rc == -1) break
            c += rc.toLong() and 0xff
        }

        return c
    }

    @Benchmark
    fun byteArrayInputStream(): Long {
        val stream = ByteArrayInputStream(array)
        var c = 0L

        while (true) {
            val rc = stream.read()
            if (rc == -1) break
            c += rc.toLong() and 0xff
        }

        return c
    }

    @Benchmark
    fun byteArrayInputStreamReal(): Long {
        val stream = { ByteArrayInputStream(array) }()
        var c = 0L

        while (true) {
            val rc = stream.read()
            if (rc == -1) break
            c += rc.toLong() and 0xff
        }

        return c
    }

    @Benchmark
    fun copyAndRelease() {
        val input = packet.copy()
        input.release()
    }

    @Benchmark
    fun myInput(): Long {
        val input = packet.copy()
        var c = 0L

        repeat(size) {
            c += input.readByte().toLong() and 0xff
        }

        return c
    }

    @Benchmark
    fun myInputTakeWhile(): Long {
        val input = packet.copy()
        var c = 0L

        input.takeWhile { buffer ->
            repeat(buffer.readRemaining) {
                c += buffer.readByte().toLong() and 0xff
            }
            true
        }

        return c
    }

    @Benchmark
    fun directArrayAccess(): Long {
        val input = array
        var c = 0L

        for (i in 0 until size - 1) {
            c += input[i].toLong() and 0xff
        }

        return c
    }

    @Benchmark
    fun directBuffer(): Long {
        val input = directBuffer.duplicate()
        var c = 0L
        while (input.hasRemaining()) {
            c += input.get().toLong() and 0xff
        }

        return c
    }

    @Benchmark
    fun dataInputStreamReadLong(): Long {
        val input = DataInputStream(ByteArrayInputStream(array))
        var c = 0L

        repeat(size / 8) {
            c += input.readLong()
        }

        return c
    }

    @Benchmark
    fun myInputReadLong(): Long {
        val input = packet.copy()
        var c = 0L

        repeat(input.remaining.toInt() / 8) {
            c += input.readLong()
        }

        return c
    }
}
