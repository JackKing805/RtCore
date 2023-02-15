package com.jerry.rt.core.http.request.impl

import java.io.BufferedInputStream
import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater

open class NoWaitBufferedInputStream @JvmOverloads constructor(
    `in`: InputStream,
    size: Int = DEFAULT_BUFFER_SIZE
) : FilterInputStream(`in`) {

    @Volatile
    protected var buf: ByteArray

    protected var count = 0

    protected var pos = 0

    protected var markpos = -1

    protected var marklimit = 0

    @get:Throws(IOException::class)
    private val inIfOpen: InputStream
        private get() = `in` ?: throw IOException("Stream closed")

    @get:Throws(IOException::class)
    private val bufIfOpen: ByteArray
        private get() = buf ?: throw IOException("Stream closed")

    init {
        require(size > 0) { "Buffer size <= 0" }
        buf = ByteArray(size)
    }

    @Throws(IOException::class)
    private fun fill() {
        var buffer = bufIfOpen
        if (markpos < 0) pos =
            0  else if (pos >= buffer.size)  if (markpos > 0) {
            val sz = pos - markpos
            System.arraycopy(buffer, markpos, buffer, 0, sz)
            pos = sz
            markpos = 0
        } else if (buffer.size >= marklimit) {
            markpos = -1
            pos = 0
        } else if (buffer.size >= MAX_BUFFER_SIZE) {
            throw OutOfMemoryError("Required array size too large")
        } else {
            var nsz =
                if (pos <= MAX_BUFFER_SIZE - pos) pos * 2 else MAX_BUFFER_SIZE
            if (nsz > marklimit) nsz = marklimit
            val nbuf = ByteArray(nsz)
            System.arraycopy(buffer, 0, nbuf, 0, pos)
            if (!bufUpdater.compareAndSet(this, buffer, nbuf)) {
                throw IOException("Stream closed")
            }
            buffer = nbuf
        }
        count = pos
        val n = inIfOpen.read(buffer, pos, buffer.size - pos)
        if (n > 0) count = n + pos
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(): Int {
        if (pos >= count) {
            fill()
            if (pos >= count) return -1
        }
        return bufIfOpen[pos++].toInt() and 0xff
    }

    @Throws(IOException::class)
    private fun read1(b: ByteArray, off: Int, len: Int): Int {
        var avail = count - pos
        if (avail <= 0) {
            /* If the requested length is at least as large as the buffer, and
               if there is no mark/reset activity, do not bother to copy the
               bytes into the local buffer.  In this way buffered streams will
               cascade harmlessly. */
            if (len >= bufIfOpen.size && markpos < 0) {
                return inIfOpen.read(b, off, len)
            }
            fill()
            avail = count - pos
            if (avail <= 0) return -1
        }
        val cnt = if (avail < len) avail else len
        System.arraycopy(bufIfOpen, pos, b, off, cnt)
        pos += cnt
        return cnt
    }

    @Synchronized
    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        bufIfOpen // Check for closed stream
        if (off or len or off + len or b.size - (off + len) < 0) {
            throw IndexOutOfBoundsException()
        } else if (len == 0) {
            return 0
        }
        var n = 0
        while (true) {
            val nread = read1(b, off + n, len - n)
            if (nread <= 0) return if (n == 0) nread else n
            n += nread
            if (n >= len) return n
            // if not closed but no bytes available, return
            val input = `in`
            if (input != null && input.available() <= 0) return n
        }
    }

    @Synchronized
    @Throws(IOException::class)
    override fun skip(n: Long): Long {
        bufIfOpen // Check for closed stream
        if (n <= 0) {
            return 0
        }
        var avail = (count - pos).toLong()
        if (avail <= 0) {
            // If no mark position set then don't keep in buffer
            if (markpos < 0) return inIfOpen.skip(n)

            // Fill in buffer to save bytes for reset
            fill()
            avail = (count - pos).toLong()
            if (avail <= 0) return 0
        }
        val skipped = if (avail < n) avail else n
        pos += skipped.toInt()
        return skipped
    }

    @Synchronized
    @Throws(IOException::class)
    override fun available(): Int {
        val n = count - pos
        val avail = inIfOpen.available()
        return if (n > Int.MAX_VALUE - avail) Int.MAX_VALUE else n + avail
    }


    @Synchronized
    override fun mark(readlimit: Int) {
        marklimit = readlimit
        markpos = pos
    }


    @Synchronized
    @Throws(IOException::class)
    override fun reset() {
        bufIfOpen // Cause exception if closed
        if (markpos < 0) throw IOException("Resetting to invalid mark")
        pos = markpos
    }

    override fun markSupported(): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun close() {
        var buffer: ByteArray?
        while (buf.also { buffer = it } != null) {
            if (bufUpdater.compareAndSet(this, buffer, null)) {
                val input = `in`
                `in` = null
                input?.close()
                return
            }
            // Else retry in case a new buf was CASed in fill()
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 8192

        private const val MAX_BUFFER_SIZE = Int.MAX_VALUE - 8

        private val bufUpdater = AtomicReferenceFieldUpdater.newUpdater(
            NoWaitBufferedInputStream::class.java,
            ByteArray::class.java, "buf"
        )
    }
}
