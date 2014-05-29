package org.broad.tribble;

import org.broad.tribble.readers.*;
import org.broad.tribble.util.BlockCompressedInputStream;
import org.broad.tribble.util.ParsingUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jim Robinson
 * @since 2/11/12
 */
public class TabixFeatureReader extends AbstractFeatureReader {

    TabixReader tabixReader;
    List<String> sequenceNames;

    /**
     *
     * @param featureFile - path to a feature file. Can be a local file, http url, or ftp url
     * @param codec
     * @throws IOException
     */
    public TabixFeatureReader(String featureFile, AsciiFeatureCodec codec) throws IOException {
        super(featureFile, codec);
        tabixReader = new TabixReader(featureFile);
        sequenceNames = new ArrayList<String>(tabixReader.mChr2tid.keySet());
        readHeader();
    }


    /**
     * read the header
     *
     * @return a Object, representing the file header, if available
     * @throws IOException throws an IOException if we can't open the file
     */
    private void readHeader() throws IOException {
        PositionalBufferedStream is = null;
        try {
            is = new PositionalBufferedStream(new BlockCompressedInputStream(ParsingUtils.openInputStream(path)));
            header = codec.readHeader(is);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.equals("null")) msg=" (Maybe the file does not exist?)";
            throw new TribbleException.MalformedFeatureFile("Unable to parse header: \n<br>" + msg, path, e);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    public List<String> getSequenceNames() {
        return sequenceNames;
    }

    /**
     * Return iterator over all features overlapping the given interval
     *
     * @param chr
     * @param start
     * @param end
     * @return
     * @throws IOException
     */
    public CloseableTribbleIterator query(String chr, int start, int end) throws IOException {
        List<String> mp = getSequenceNames();
        if (mp == null) throw new TribbleException.TabixReaderFailure("Unable to find sequence named " + chr +
                " in the tabix index. ", path);
        if (!mp.contains(chr)) {
            return new EmptyIterator();
        }
        TabixIteratorLineReader lineReader = new TabixIteratorLineReader(tabixReader.query(tabixReader.mChr2tid.get(chr), start - 1, end));
        return new FeatureIterator(lineReader, start - 1, end);
    }

    public CloseableTribbleIterator iterator() throws IOException {
        final InputStream is = new BlockCompressedInputStream(ParsingUtils.openInputStream(path));
        final PositionalBufferedStream stream = new PositionalBufferedStream(is);
        final LineReader reader = new AsciiLineReader(stream);
        return new FeatureIterator(reader, 0, Integer.MAX_VALUE);
    }

    public void close() throws IOException {

    }


    class FeatureIterator<T extends Feature> implements CloseableTribbleIterator {
        private T currentRecord;
        private LineReader lineReader;
        private int start;
        private int end;

        public FeatureIterator(LineReader lineReader, int start, int end) throws IOException {
            this.lineReader = lineReader;
            this.start = start;
            this.end = end;
            readNextRecord();
        }


        /**
         * Advance to the next record in the query interval.
         *
         * @throws IOException
         */
        protected void readNextRecord() throws IOException {
            currentRecord = null;
            String nextLine;
            while (currentRecord == null && (nextLine = lineReader.readLine()) != null) {
                Feature f = null;
                try {
                    f = ((AsciiFeatureCodec)codec).decode(nextLine);
                    if (f == null) {
                        continue;   // Skip
                    }
                    if (f.getStart() > end) {
                        return;    // Done
                    }
                    if (f.getEnd() <= start) {
                        continue;   // Skip
                    }

                    currentRecord = (T) f;

                } catch (TribbleException e) {
                    e.setSource(path);
                    throw e;
                } catch (NumberFormatException e) {
                    String error = "Error parsing line: " + nextLine;
                    throw new TribbleException.MalformedFeatureFile(error, path, e);
                }


            }
        }


        public boolean hasNext() {
            return currentRecord != null;
        }

        public T next() {
            T ret = currentRecord;
            try {
                readNextRecord();
            } catch (IOException e) {
                throw new RuntimeException("Unable to read the next record, the last record was at " +
                        ret.getChr() + ":" + ret.getStart() + "-" + ret.getEnd(), e);
            }
            return ret;

        }

        public void remove() {
            throw new UnsupportedOperationException("Remove is not supported in Iterators");
        }

        public void close() {
            lineReader.close();
        }

        public Iterator<T> iterator() {
            return this;
        }
    }


}
