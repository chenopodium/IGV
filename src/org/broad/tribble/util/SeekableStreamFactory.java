package org.broad.tribble.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.broad.igv.util.stream.IGVSeekableFTPStream;
import org.broad.igv.util.stream.IGVSeekableHTTPStream;

/**
 * @author jrobinso
 * @date Nov 30, 2009
 */
 public class SeekableStreamFactory {

    private static Logger log = Logger.getLogger(SeekableStreamFactory.class);

    public static SeekableStream getStreamFor(String path) throws IOException {
        // todo -- add support for SeekableBlockInputStream

        if (path.startsWith("http:") || path.startsWith("https:")) {
            final URL url = new URL(path);
            return getHttpStream(url);

        } else if (path.startsWith("ftp:")) {
            log.info("===============  CREATING IGVSeekableFTPStream");
            return new IGVSeekableFTPStream(new URL(path));
        } else {
            return new SeekableFileStream(new File(path));
        }
    }

    public static SeekableStream getHttpStream(URL url) {
        try {
            log.info("==================  CREATING IGVSeekableHTTPStream");
            return new IGVSeekableHTTPStream(url);
        } catch (Exception e) {
            log.error("Error creating URL helper: ", e);
            throw new RuntimeException("Error creating URL helper: " + e.toString());
        }
    }

}
