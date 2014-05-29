package org.broad.tribble.util;

import java.io.*;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * A wrapper class to provide buffered read access to a SeekableStream.  Just wrapping such a stream with
 * a BufferedInputStream will not work as it does not support seeking.  In this implementation a
 * seek call is delegated to the wrapped stream, and the buffer reset.
 */
public class SeekableBufferedStream extends SeekableStream {

    private int bufferSize;
    private SeekableStream wrappedStream;
    private long position;

    long bufferPosition = Long.MIN_VALUE;  // <= signal for "un-initialized"
    byte[] buffer;

    public SeekableBufferedStream(SeekableStream wrappedStream) {
        this(wrappedStream, 512000);
    }


    public SeekableBufferedStream(SeekableStream wrappedStream, int bufferSize) {
        this.wrappedStream = wrappedStream;
        this.position = 0;
        this.bufferSize = bufferSize;
        buffer = new byte[bufferSize];
    }

    public long length() {
        return wrappedStream.length();
    }

    public void seek(long position) throws IOException {
        this.position = position;
    }

    @Override
    public long position() throws IOException {
        return position;
    }


    public int read() throws IOException {

        if (position < bufferPosition || position >= (bufferPosition + buffer.length)) {
            //  buffer = new byte[bufferSize];
            wrappedStream.seek(position);
            fill(buffer, 0, buffer.length);
            bufferPosition = position;
        }
        int idx = (int) (position - bufferPosition);
        position++;
        int value = buffer[idx] & 0xff;
        return value;
    }


    /**
     * Reads up to len bytes of data from the input stream into an array of bytes. An attempt is made to read as many
     * as len bytes, but a smaller number may be read, possibly zero. The number of bytes actually read is returned
     * as an integer.
     * <p/>
     * This method blocks until input data is available, end of file is detected, or an exception is thrown.
     * <p/>
     * If len is zero, then no bytes are read and 0 is returned; otherwise, there is an attempt to read at least
     * one byte. If no byte is available because the stream is at end of file, the value -1 is returned; otherwise,
     * at least one byte is read and stored into b.
     * <p/>
     * The first byte read is stored into element b[off], the next one into b[off+1], and so on. The number of
     * bytes read is, at most, equal to len. Let k be the number of bytes actually read; these bytes will be stored
     * in elements b[off] through b[off+k-1], leaving elements b[off+k] through b[off+len-1] unaffected.
     * <p/>
     * In every case, elements b[0] through b[off] and elements b[off+len] through b[b.len-1] are unaffected.
     * <p/>
     * If the first byte cannot be read for any reason other than end of file, then an IOException is thrown.
     * In particular, an IOException is thrown if the input stream has been closed.
     *
     * @param b   - the b into which the data is read.
     * @param off - the start off in array b at which the data is writ
     * @param len - the maximum number of bytes to read.
     * @return
     * @throws IOException
     * @throws NullPointerException      - If b is null
     * @throws IndexOutOfBoundsException - If off is negative, or len is negative, or off+len is
     *                                   greater than the len of the array b
     */
    public int read(byte[] b, int off, int len) throws IOException {

        int count = 0;

        long right = position + len;
        long bufferRight = bufferPosition + buffer.length;
        boolean noOverlap =  right <= bufferPosition || position >= bufferRight;

        if (bufferPosition < 0 || noOverlap) {
            wrappedStream.seek(position);
            fill(buffer, 0, buffer.length);
            bufferPosition = position;
        }

        if (position < bufferPosition) {
            int delta = (int) Math.min(len, bufferPosition - position);
            wrappedStream.seek(position);
            int n = fill(b, off, delta);
            position += n;
            count += n;
            off += n;
            len -= count;
        }

        // Copy from buffer
        if (len > 0) {
            // Offset into buffered byte array
            long bufferOffset = position - bufferPosition;  // <= keep "long" until we know we are close (under/overflow)
            if (bufferOffset < buffer.length) {
                int bo = (int) bufferOffset;
                int nBytes = buffer.length - bo;
                int delta = Math.min(len, nBytes);
                System.arraycopy(buffer, bo, b, off, delta);

                position += delta;
                count += delta;
                off += delta;
                len -= delta;
            }
        }

        // Still more?
        if (len > 0) {
            wrappedStream.seek(position);
            int n = fill(b, off, len);
            count += n;
            position += n;
        }
        return count;

    }

    public void close() throws IOException {
        wrappedStream.close();
    }

    public boolean eof() throws IOException {
        return position >= wrappedStream.length();
    }


    /**
     * Read enough bytes to fill the input buffer
     */
    public int fill(byte[] b, int offset, int len) throws IOException {


        int n = 0;
        while (n < len) {
            int count = wrappedStream.read(b, n + offset, len - n);
            if (count < 0) {
                //EOF -- stop reading & fill rest of buffer with zeroes
                Arrays.fill(b, n, b.length - 1, (byte) 0);
                return n;
            }
            n += count;
        }
        return n;
    }

}
