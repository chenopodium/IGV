package org.broad.igv.session;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Jim Robinson
 * @date 1/12/12
 */
public interface SessionReader {
    boolean loadSession(Session session, String sessionPath, int tries) throws IOException;
    //boolean loadSession(InputStream inputStream, Session session, String sessionPath) throws IOException;
}
