package org.broad.tribble;

import java.io.File;

/**
 * Common, tribble wide constants and static functions
 */
public class Tribble {
    private Tribble() { } // can't be instantiated

    public final static String STANDARD_INDEX_EXTENSION = ".idx";

    public static String indexFile(String filename) {
        return filename + STANDARD_INDEX_EXTENSION;
    }

    public static File indexFile(File file) {
        return new File(file.getAbsoluteFile() + STANDARD_INDEX_EXTENSION);
    }
}
