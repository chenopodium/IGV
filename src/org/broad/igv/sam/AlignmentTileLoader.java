/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */
package org.broad.igv.sam;

import com.iontorrent.utils.ErrorHandler;
import net.sf.samtools.util.CloseableIterator;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.SpliceJunctionFeature;
import org.broad.igv.sam.reader.AlignmentReader;
import org.broad.igv.sam.reader.ReadGroupFilter;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.ObjectCache;
import org.broad.igv.util.RuntimeUtils;
import org.broad.igv.util.collections.LRUCache;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * A wrapper for an AlignmentQueryReader that caches query results
 *
 * @author jrobinso
 */
public class AlignmentTileLoader {

    private static Logger log = Logger.getLogger(AlignmentTileLoader.class);
    private static Set<WeakReference<AlignmentTileLoader>> activeLoaders = Collections.synchronizedSet(new HashSet());
    /**
     * Flag to mark a corrupt index. Without this attempted reads will continue
     * in an infinite loop
     */
    private boolean corruptIndex = false;
    private AlignmentReader reader;
    private boolean cancel = false;
    private boolean pairedEnd = false;
    private int errors = 0;
    private static final long MB = 1000000;
    
    // Map of read group -> paired end stats
    //private PairedEndStats peStats;
    private static void cancelReaders() {
        for (WeakReference<AlignmentTileLoader> readerRef : activeLoaders) {
            AlignmentTileLoader reader = readerRef.get();
            if (reader != null) {
                reader.cancel = true;
            }
        }
        log.debug("Readers canceled");
        activeLoaders.clear();
    }

    public AlignmentTileLoader(AlignmentReader reader) {
        this.reader = reader;
        activeLoaders.add(new WeakReference<AlignmentTileLoader>(this));
    }
    public int getErrors() {
        return errors;
    }
    public void close() throws IOException {
        reader.close();
    }

    public List<String> getSequenceNames() {
        return reader.getSequenceNames();
    }

    public CloseableIterator<Alignment> iterator() {
        return reader.iterator();
    }

    public boolean hasIndex() {
        return reader.hasIndex();
    }

    AlignmentTile loadTile(String chr, int start, int end,
            boolean showSpliceJunctions,
            AlignmentDataManager.DownsampleOptions downsampleOptions,
            Map<String, PEStats> peStats,
            AlignmentTrack.BisulfiteContext bisulfiteContext) {

        AlignmentTile t = new AlignmentTile(start, end, showSpliceJunctions, downsampleOptions, bisulfiteContext);


        //assert (tiles.size() > 0);
        if (corruptIndex) {
            return t;
        }

        boolean filterFailedReads = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SAM_FILTER_FAILED_READS);
        ReadGroupFilter filter = ReadGroupFilter.getFilter();
        boolean showDuplicates = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SAM_SHOW_DUPLICATES);
        int qualityThreshold = PreferenceManager.getInstance().getAsInt(PreferenceManager.SAM_QUALITY_THRESHOLD);

        CloseableIterator<Alignment> iter = null;

        //log.debug("Loading : " + start + " - " + end);
        int alignmentCount = 0;
        WeakReference<AlignmentTileLoader> ref = new WeakReference(this);
        try {
            ObjectCache<String, Alignment> mappedMates = new ObjectCache<String, Alignment>(1000);
            ObjectCache<String, Alignment> unmappedMates = new ObjectCache<String, Alignment>(1000);


            activeLoaders.add(ref);
            try {
                iter = reader.query(chr, start, end, false);
            }
            catch (Exception e) {
                log.error(ErrorHandler.getString(e));
                if (errors < 3) MessageUtils.showMessage("<html>Error encountered querying alignments: " + e.toString());
                return null;
            }

            log.info("loadTile: Loading alignment data for "+chr+":"+start+"-"+end);
            while (iter != null && iter.hasNext()) {

                if (cancel) {
                    return t;
                }

                Alignment record = iter.next();

                // Set mate sequence of unmapped mates
                // Put a limit on the total size of this collection.
                String readName = record.getReadName();
                if (record.isPaired()) {
                    pairedEnd = true;
                    if (record.isMapped()) {
                        if (!record.getMate().isMapped()) {
                            // record is mapped, mate is not
                            Alignment mate = unmappedMates.get(readName);
                            if (mate == null) {
                                mappedMates.put(readName, record);
                            } else {
                                record.setMateSequence(mate.getReadSequence());
                                unmappedMates.remove(readName);
                                mappedMates.remove(readName);
                            }

                        }
                    } else if (record.getMate().isMapped()) {
                        // record not mapped, mate is
                        Alignment mappedMate = mappedMates.get(readName);
                        if (mappedMate == null) {
                            unmappedMates.put(readName, record);
                        } else {
                            mappedMate.setMateSequence(record.getReadSequence());
                            unmappedMates.remove(readName);
                            mappedMates.remove(readName);
                        }
                    }
                }


                if (!record.isMapped() || (!showDuplicates && record.isDuplicate())
                        || (filterFailedReads && record.isVendorFailedRead())
                        || record.getMappingQuality() < qualityThreshold
                        || (filter != null && filter.filterAlignment(record))) {
                    continue;
                }

                t.addRecord(record);

                alignmentCount++;
                int interval = Globals.isTesting() ? 100000 : 1000;
                if (alignmentCount % interval == 0) {
                    if (cancel) {
                        log.info("loadTile: Cancelling, returning null");
                        return null;
                    }
                    MessageUtils.setStatusBarMessage("Reads loaded: " + alignmentCount);
                    if (checkMemory() == false) {
                        cancelReaders();
                        return t;        // <=  TODO need to cancel all readers
                    }
                }

                // Update pe stats
                if (peStats != null && record.isPaired() && record.isProperPair()) {
                    String lb = record.getLibrary();
                    if (lb == null) {
                        lb = "null";
                    }
                    PEStats stats = peStats.get(lb);
                    if (stats == null) {
                        stats = new PEStats(lb);
                        peStats.put(lb, stats);
                    }
                    stats.update(record);

                }
            }
            // End iteration over alignments

            // Compute peStats
            if (peStats != null) {
                // TODO -- something smarter re the percentiles.  For small samples these will revert to min and max
                double minPercentile = PreferenceManager.getInstance().getAsFloat(PreferenceManager.SAM_MIN_INSERT_SIZE_PERCENTILE);
                double maxPercentile = PreferenceManager.getInstance().getAsFloat(PreferenceManager.SAM_MAX_INSERT_SIZE_PERCENTILE);
                for (PEStats stats : peStats.values()) {
                    stats.compute(minPercentile, maxPercentile);
                }
            }

            // Clean up any remaining unmapped mate sequences
            for (String mappedMateName : mappedMates.getKeys()) {
                Alignment mappedMate = mappedMates.get(mappedMateName);
                Alignment mate = unmappedMates.get(mappedMate.getReadName());
                if (mate != null) {
                    mappedMate.setMateSequence(mate.getReadSequence());
                }
            }
            t.setLoaded(true);

            return t;

        } catch (java.nio.BufferUnderflowException e) {
            // This almost always indicates a corrupt BAM index, or less frequently a corrupt bam file
            corruptIndex = true;
           if (errors < 3) MessageUtils.showMessage("<html>Error encountered querying alignments: " + e.toString()
                    + "<br>This is often caused by a corrupt index file.");
            return null;

        } catch (Exception e) {
            log.error("Error loading alignment data", e);
            log.error(ErrorHandler.getString(e));
            if (errors < 3)MessageUtils.showMessage("<html>Error encountered querying alignments: " + e.toString());
            return null;
        } finally {
            // reset cancel flag.  It doesn't matter how we got here,  the read is complete and this flag is reset
            // for the next time
            cancel = false;
            activeLoaders.remove(ref);
            if (iter != null) {
                iter.close();
            }
            if (!Globals.isHeadless()) {
                IGV.getInstance().resetStatusMessage();
            }
        }
    }

    private static synchronized boolean checkMemory() {
        
        if (RuntimeUtils.getAvailableMemoryFraction() < 0.2 || RuntimeUtils.getAvailableMemory()< 200*MB) {
            LRUCache.clearCaches();
            log.info("Checkig memory, trying system.gc: "+RuntimeUtils.getAvailableMemory()/MB+" MB avaiable");
            System.gc();
            if (RuntimeUtils.getAvailableMemoryFraction() < 0.2 ||  RuntimeUtils.getAvailableMemory()< 200*MB) {
                int maxmb = (int) (Runtime.getRuntime().maxMemory() / 1000000);
                PreferenceManager pref = PreferenceManager.getInstance();
                boolean down = pref.getAsBoolean(PreferenceManager.SAM_DOWNSAMPLE_READS);
                int reads = pref.getAsInt(PreferenceManager.SAM_SAMPLING_COUNT);
                int range = pref.getAsInt(PreferenceManager.SAM_MAX_VISIBLE_RANGE);

                int moremb = (int) (maxmb * 2 / 500) * 500;
                String mem = moremb + "MB";

                String msg = "Memory is getting low, I will have to stop loading reads.<br>"
                        + "The current max read depth is <b>" + reads + "</b>, and the visibility range is "
                        + "<b>" + range + " kb</b>.<br>";
                msg += "<b><u>Would you like me to reduce those parameters automatically?</u></b><br>";
                msg += "<br>(Note: you can also try to give IGV more memory!<br>"
                        + "Via URL: add <b>&maxheap=" + mem + "&</b> to the URL (as an example).<br>"
                        + "Via command line: java -jar <b>-Xmx" + mem + "</b> igv.jar)";

                boolean ok = MessageUtils.getOrOrCancel(msg);
                if (ok) {
                    reads = reads / 2;
                    range = range / 2;
                    range = Math.max(3, range);
                    reads = Math.max(10, reads);
                    pref.put(PreferenceManager.SAM_DOWNSAMPLE_READS, "true");
                    pref.put(PreferenceManager.SAM_SAMPLING_COUNT, "" + reads);
                    pref.put(PreferenceManager.SAM_MAX_VISIBLE_RANGE, "" + range);
                } 
                return false;
            }

        }
        return true;
    }

    /**
     * Does this file contain paired end data? Assume not until proven
     * otherwise.
     */
    public boolean isPairedEnd() {
        return pairedEnd;
    }

    public Set<String> getPlatforms() {
        return reader.getPlatforms();
    }

    /**
     * Caches alignments, coverage, splice junctions, and downsampled intervals
     */
    public static class AlignmentTile {

        private boolean loaded = false;
        private int end;
        private int start;
        private AlignmentCounts counts;
        private List<Alignment> alignments;
        private List<DownsampledInterval> downsampledIntervals;
        private List<SpliceJunctionFeature> spliceJunctionFeatures;
        private SpliceJunctionHelper spliceJunctionHelper;
        private boolean downsample;
        private int samplingWindowSize;
        private int samplingDepth;
        private SamplingBucket currentSamplingBucket;
        private static final Random RAND = new Random(System.currentTimeMillis());

        AlignmentTile(int start, int end,
                boolean showSpliceJunctions,
                AlignmentDataManager.DownsampleOptions downsampleOptions,
                AlignmentTrack.BisulfiteContext bisulfiteContext) {
            this.start = start;
            this.end = end;
            alignments = new ArrayList(16000);
            downsampledIntervals = new ArrayList();

            // Use a sparse array for large regions  (> 10 mb)
            if ((end - start) > 10000000) {
                this.counts = new SparseAlignmentCounts(start, end, bisulfiteContext);
            } else {
                this.counts = new DenseAlignmentCounts(start, end, bisulfiteContext);
            }

            // Set the max depth, and the max depth of the sampling bucket.
            if (downsampleOptions == null) {
                // Use default settings (from preferences)
                downsampleOptions = new AlignmentDataManager.DownsampleOptions();
            }
            this.downsample = downsampleOptions.isDownsample();
            this.samplingWindowSize = downsampleOptions.getSampleWindowSize();
            this.samplingDepth = Math.max(1, downsampleOptions.getMaxReadCount());

            if (showSpliceJunctions) {
                spliceJunctionFeatures = new ArrayList<SpliceJunctionFeature>(100);
                spliceJunctionHelper = new SpliceJunctionHelper();
            }

        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }
        int ignoredCount = 0;    // <= just for debugging

        /**
         * Add an alignment record to this tile. This record is not necessarily
         * retained after down-sampling.
         *
         * @param alignment
         */
        public void addRecord(Alignment alignment) {

            counts.incCounts(alignment);

            if (downsample) {
                final int alignmentStart = alignment.getAlignmentStart();
                if (currentSamplingBucket == null || alignmentStart >= currentSamplingBucket.end) {
                    if (currentSamplingBucket != null) {
                        emptyBucket();
                    }
                    int end = alignmentStart + samplingWindowSize;
                    currentSamplingBucket = new SamplingBucket(alignmentStart, end);
                }

                if (spliceJunctionHelper != null) {
                    spliceJunctionHelper.addAlignment(alignment);
                }

                currentSamplingBucket.add(alignment);
            } else {
                alignments.add(alignment);
            }
        }

        private void emptyBucket() {
            if (currentSamplingBucket == null) {
                return;
            }
            //List<Alignment> sampledRecords = sampleCurrentBucket();
            for (Alignment alignment : currentSamplingBucket.getAlignments()) {
                alignments.add(alignment);
            }

            if (currentSamplingBucket.isSampled()) {
                DownsampledInterval interval = new DownsampledInterval(currentSamplingBucket.start,
                        currentSamplingBucket.end, currentSamplingBucket.downsampledCount);
                downsampledIntervals.add(interval);
            }

            currentSamplingBucket = null;

        }

        public List<Alignment> getAlignments() {
            return alignments;
        }

        public List<DownsampledInterval> getDownsampledIntervals() {
            return downsampledIntervals;
        }

        public boolean isLoaded() {
            return loaded;
        }

        public void setLoaded(boolean loaded) {
            this.loaded = loaded;

            if (loaded) {
                // Empty any remaining alignments in the current bucket
                emptyBucket();
                currentSamplingBucket = null;
                finalizeSpliceJunctions();
                counts.finish();
            }
        }

        public AlignmentCounts getCounts() {
            return counts;
        }

        private void finalizeSpliceJunctions() {
            if (spliceJunctionHelper != null) {
                spliceJunctionHelper.finish();
                List<SpliceJunctionFeature> features = spliceJunctionHelper.getFeatures();
                for (SpliceJunctionFeature f : features) {
                    spliceJunctionFeatures.add(f);
                }
            }
            spliceJunctionHelper = null;
        }

        public List<SpliceJunctionFeature> getSpliceJunctionFeatures() {
            return spliceJunctionFeatures;
        }

        private class SamplingBucket {

            int start;
            int end;
            int downsampledCount = 0;
            List<Alignment> alignments;

            private SamplingBucket(int start, int end) {
                this.start = start;
                this.end = end;
                alignments = new ArrayList(samplingDepth);
            }

            public void add(Alignment alignment) {
                // If the current bucket is < max depth we keep it.  Otherwise,  keep with probability == samplingProb
                if (alignments.size() < samplingDepth) {
                    alignments.add(alignment);
                } else {
                    double samplingProb = ((double) samplingDepth) / (samplingDepth + downsampledCount + 1);
                    if (RAND.nextDouble() < samplingProb) {
                        int idx = (int) (RAND.nextDouble() * (alignments.size() - 1));
                        // Replace random record with this one
                        alignments.set(idx, alignment);
                    }
                    downsampledCount++;

                }

            }

            public List<Alignment> getAlignments() {
                return alignments;
            }

            public boolean isSampled() {
                return downsampledCount > 0;
            }

            public int getDownsampledCount() {
                return downsampledCount;
            }
        }
    }
}
