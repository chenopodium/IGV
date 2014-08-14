package org.broad.tribble.example;

import org.broad.tribble.*;
import org.broad.tribble.readers.AsciiLineReader;
import org.broad.tribble.readers.PositionalBufferedStream;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An example binary codec that encodes / decodes contig / start / stop values via DataInputStreams
 *
 * @author Mark DePristo
 */
public class ExampleBinaryCodec implements FeatureCodec<Feature> {
    public final static String HEADER_LINE = "# BinaryTestFeature";

    @Override
    public Feature decodeLoc(final PositionalBufferedStream stream) throws IOException {
        return decode(stream);
    }

    @Override
    public Feature decode(final PositionalBufferedStream stream) throws IOException {
        DataInputStream dis = new DataInputStream(stream);
        String contig = dis.readUTF();
        int start = dis.readInt();
        int stop = dis.readInt();
        return new BasicFeature(contig, start, stop);
    }

    @Override
    public FeatureCodecHeader readHeader(final PositionalBufferedStream stream) throws IOException {
        final AsciiLineReader reader = new AsciiLineReader(stream);
        String line;
        List<String> headerLines = new ArrayList<String>();
        long headerLengthInBytes = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                headerLines.add(line);
                headerLengthInBytes = stream.getPosition();
            } else {
                break; // no more header lines
            }
        }
        return new FeatureCodecHeader(headerLines, headerLengthInBytes);
    }

    @Override
    public Class<Feature> getFeatureType() {

        return Feature.class;
    }
    @Override
    public boolean canDecode(final String path) {
        return false;
    }

    /**
     * Convenience method that creates an ExampleBinaryCodec file from another feature file.
     *
     * For testing purposes really
     *
     * @param source file containing the features
     * @param dest the place to write the binary features
     * @param codec of the source file features
     * @throws IOException
     */
    public static void convertToBinaryTest(final File source, final File dest, final FeatureCodec<Feature> codec) throws IOException {
        final FeatureReader<Feature> reader = AbstractFeatureReader.getFeatureReader(source.getAbsolutePath(), codec, false); // IndexFactory.loadIndex(idxFile));
        final OutputStream output = new FileOutputStream(dest);
        ExampleBinaryCodec.convertToBinaryTest(reader, output);
    }

    /**
     * Convenience method that creates an ExampleBinaryCodec file from another feature file.
     *
     * For testing purposes really
     *
     * @throws IOException
     */
    public static void convertToBinaryTest(final FeatureReader<Feature> reader, final OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.writeBytes(HEADER_LINE + "\n");
        Iterator<Feature> it = reader.iterator();
        while ( it.hasNext() ) {
            final Feature f = it.next();
            dos.writeUTF(f.getChr());
            dos.writeInt(f.getStart());
            dos.writeInt(f.getEnd());
        }
        dos.close();
        reader.close();
    }
}