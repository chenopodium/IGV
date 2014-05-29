package org.broad.tribble.readers;

import java.io.IOException;

/**
 * Interface for line-oriented readers.
 */
public interface LineReader {
    public String readLine() throws IOException;
    public void close();
}
