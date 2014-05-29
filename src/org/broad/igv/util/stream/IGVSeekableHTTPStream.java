package org.broad.igv.util.stream;

import org.broad.tribble.util.SeekableStream;
import org.broad.tribble.util.URLHelper;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;
import java.util.logging.Logger;

/**
 * TODO Get rid of this class
 * Temporary workaround to let us use IGVUrlHelper, think
 * it's what's causing the issue with reading TDF from web start over http
 */
public class IGVSeekableHTTPStream extends SeekableStream {

    private long position = 0;
    private long contentLength = -1;

    private final Proxy proxy;
    private URLHelper helper;

    public IGVSeekableHTTPStream(final URL url) {
        this(url, null);
        p("Create IGVSeekableHTTPStream for URL "+url);

    }
    private void p(String s){
        Logger.getLogger(getClass().getName()).info(s);
    }
    public IGVSeekableHTTPStream(final URL url, Proxy proxy) {

        this.proxy = proxy;
        this.helper = new IGVUrlHelper(url);
        try {
            this.contentLength = this.helper.getContentLength();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    public long position() {
        return position;
    }

    public long length() {
        return contentLength;
    }

    @Override
    public long skip(long n) throws IOException {
        long bytesToSkip = Math.min(n, contentLength - position);
        position += bytesToSkip;
        return bytesToSkip;
    }

    public boolean eof() throws IOException {
        return contentLength > 0 && position >= contentLength;
    }

    public void seek(final long position) {
        this.position = position;
    }

    public int read(byte[] buffer, int offset, int len) throws IOException {

        if (offset < 0 || len < 0 || (offset + len) > buffer.length) {
            throw new IndexOutOfBoundsException("Offset="+offset+",len="+len+",buflen="+buffer.length);
        }
        if (len == 0) {
            return 0;
        }

        InputStream is = null;
        int n = 0;
        try {

            long endRange = position + len - 1;
            // IF we know the total content length, limit the end range to that.
            if (contentLength > 0) {
                endRange = Math.min(endRange, contentLength);
            }
            is = this.helper.openInputStreamForRange(position, endRange);

            while (n < len) {
                int count = is.read(buffer, offset + n, len - n);
                if (count < 0) {
                    if (n == 0) {
                        return -1;
                    } else {
                        break;
                    }
                }
                n += count;
            }

            position += n;

            return n;

        }

        catch (IOException e) {
            // THis is a bit of a hack, but its not clear how else to handle this.  If a byte range is specified
            // that goes past the end of the file the response code will be 416.  The MAC os translates this to
            // an IOException with the 416 code in the message.  Windows translates the error to an EOFException.
            //
            //  The BAM file iterator  uses the return value to detect end of file (specifically looks for n == 0).
            e.printStackTrace();
            if (e.getMessage().contains("416") || (e instanceof EOFException)) {
                if (n == 0) {
                    return -1;
                } else {
                    position += n;
                    // As we are at EOF, the contentLength and position are by definition =
                    contentLength = position;
                    return n;
                }
            } else {
                throw e;
            }

        }

        finally {
            if (is != null) {
                is.close();
            }
        }
    }


    public void close() throws IOException {
        // Nothing to do
    }


    public int read() throws IOException {
        byte []tmp=new byte[1];
        read(tmp,0,1);
        return (int) tmp[0] & 0xFF;
    }

   
    public String getSource() {
        return this.helper.getUrl().toExternalForm();
    }
}