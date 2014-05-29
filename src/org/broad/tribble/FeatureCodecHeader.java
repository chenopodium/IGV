package org.broad.tribble;

/**
 * A class to represent a header of a feature containing file.  Specific to a codec.  All
 * codecs must return a non-null value now for their header, but the header can be the
 * empty header object or the end case be set to NO_HEADER_END.
 *
 * Note that if the headerEnd value is > 0 the readers will skip the header for the codec upfront,
 * so that decode() doesn't have to deal with potentially seeing header records in the inputstream.
 *
 * @author Mark DePristo
 * @since 5/2/12
 */
public class FeatureCodecHeader {
    /** The value of the headerEnd field when there's no header */
    public final static long NO_HEADER_END = 0;

    /** An public instance representing no header */
    public final static FeatureCodecHeader EMPTY_HEADER = new FeatureCodecHeader(null, NO_HEADER_END);

    private final Object headerValue;
    private final long headerEnd;

    /**
     * Create a FeatureCodecHeader indicating the contents of the header (can be null)
     * and the byte position in the file where the header ends (not inclusive).  headerEnd
     * should be NO_HEADER_END when no header is present.
     *
     * @param headerValue the header data read by the codec
     * @param headerEnd the position (not inclusive) of the end of the header.  1 would
     *                  mean just the first byte of the file is the header.  Zero indicates
     *                  there's no header at all
     */
    public FeatureCodecHeader(final Object headerValue, final long headerEnd) {
        if ( headerEnd < 0 ) throw new TribbleException("Header end < 0");
        this.headerValue = headerValue;
        this.headerEnd = headerEnd;
    }

    /**
     * @return the header value provided by the codec for this file
     */
    public Object getHeaderValue() {
        return headerValue;
    }

    /**
     * @return to position, not inclusive, where the header ends.  Must be >= 0
     */
    public long getHeaderEnd() {
        return headerEnd;
    }

    /**
     * @return true if we should skip some bytes to skip this header
     */
    public boolean skipHeaderBytes() {
        return getHeaderEnd() != NO_HEADER_END;
    }
}
