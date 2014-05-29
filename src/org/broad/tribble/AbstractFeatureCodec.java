package org.broad.tribble;

import org.broad.tribble.readers.PositionalBufferedStream;

import java.io.IOException;

/**
 * Simple basic class providing much of the basic functionality of codecs
 */
public abstract class AbstractFeatureCodec<T extends Feature> implements FeatureCodec {
    Class<T> myClass;

    protected AbstractFeatureCodec(final Class<T> myClass) {
        this.myClass = myClass;
    }

    @Override
    public Feature decodeLoc(final PositionalBufferedStream stream) throws IOException {
        return decode(stream);
    }

    @Override
    public Class<T> getFeatureType() {
        return myClass;
    }

    @Override
    public boolean canDecode(final String path) {
        return false;
    }

}
