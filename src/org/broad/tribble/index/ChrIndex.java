package org.broad.tribble.index;

import org.broad.tribble.util.LittleEndianInputStream;
import org.broad.tribble.util.LittleEndianOutputStream;

import java.io.IOException;
import java.util.List;

/**
 * Represents an index on a specific chromosome
 */

public interface ChrIndex {

    public String getName();

    /**
     * @return all blocks in ChrIndex
     */
    List<Block> getBlocks();
    
    List<Block> getBlocks(int start, int end);

    void write(LittleEndianOutputStream dos) throws IOException;

    void read(LittleEndianInputStream dis) throws IOException;
}
