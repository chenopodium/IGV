package org.broad.tribble.readers;

import java.io.IOException;

/**
 * @author Jim Robinson
 * @date 2/11/12
 */
public class TabixIteratorLineReader implements LineReader {

    TabixReader.Iterator iterator;


    public TabixIteratorLineReader(TabixReader.Iterator iterator) {
        this.iterator = iterator;
    }

    public String readLine() throws IOException {
        return iterator != null ? iterator.next() : null;
    }

    public void close() {
        // Ignore -
    }
}
