package org.broad.tribble.readers;

import java.io.IOException;

/**
 * User: depristo
 * Date: Oct 7, 2010
 * Time: 10:53:20 AM
 *
 * Minimal interface for an object at support getting the current position in the stream / writer / file.
 *
 * The constrain here is simple.  If you are a output stream / writer, and you've written 50 bytes to the stream,
 * then getFilePointer() should return 50 bytes.  If you are an input stream or file reader, and you've read
 * 25 bytes from the object, then getFilePointer() should return 25.
 */
public interface Positional {
    /**
     * @return the current offset, in bytes, in the stream / writer / file.
     */
    public long getPosition();

    /**
     * Is the stream done?  Equivalent to ! hasNext() for an iterator?
     * @return true if the stream has reached EOF, false otherwise
     */
    public boolean isDone() throws IOException;

    /**
     * Skip the next nBytes in the stream.
     * @param nBytes to skip, must be >= 0
     * @return the number of bytes actually skippped.
     * @throws IOException
     */
    public long skip(long nBytes) throws IOException;

    /**
     * Return the next byte in the first, without actually reading it from the stream.
     *
     * Has the same output as read()
     *
     * @return the next byte, or -1 if EOF encountered
     * @throws IOException
     */
    public int peek() throws IOException;
}
