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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.tdf;

import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.data.BasicScore;
import org.broad.igv.data.CompositeScore;
import org.broad.igv.data.CoverageDataSource;
import org.broad.igv.data.NamedScore;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.TrackType;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.util.collections.LRUCache;

import java.util.*;

/**
 * @author jrobinso
 */
public class TDFDataSource implements CoverageDataSource {

    private static Logger log = Logger.getLogger(TDFDataSource.class);

    int maxPrecomputedZoom = 6;
    TDFReader reader;
    private int trackNumber = 0;
    String trackName;
    LRUCache<String, List<LocusScore>> summaryScoreCache = new LRUCache(this, 20);
    Genome genome;
    Interval currentInterval;
    WindowFunction windowFunction = WindowFunction.mean;
    List<WindowFunction> availableFunctions;

    private boolean aggregateLikeBins = true;

    boolean normalizeCounts = false;
    int totalCount = 0;
    float normalizationFactor = 1.0f;
    private Map<String, String> chrNameMap = new HashMap();


    public TDFDataSource(TDFReader reader, int trackNumber, String trackName, Genome genome) {

        this.genome = genome;

        // TODO -- a single reader will be shared across data sources
        this.trackNumber = trackNumber;
        this.trackName = trackName;
        this.reader = reader;
        this.availableFunctions = reader.getWindowFunctions();

        TDFGroup rootGroup = reader.getGroup("/");
        try {
            maxPrecomputedZoom = Integer.parseInt(rootGroup.getAttribute("maxZoom"));
        } catch (Exception e) {
            log.error("Error reading attribute 'maxZoom'", e);
        }
        try {
            String dataGenome = rootGroup.getAttribute("genome");
            // TODO -- throw exception if data genome != current genome 
        } catch (Exception e) {
            log.error("Unknown genome " + rootGroup.getAttribute("genome"));
            throw new RuntimeException("Unknown genome " + rootGroup.getAttribute("genome"));
        }

        try {
            String totalCountString = rootGroup.getAttribute("totalCount");
            if (totalCountString != null) {
                totalCount = Integer.parseInt(totalCountString);
            }
        } catch (Exception e) {
            log.error("Error reading attribute 'totalCount'", e);
        }

        // If we have a genome, build a reverse-lookup table for queries
        if (genome != null) {
            Set<String> chrNames = reader.getChromosomeNames();
            for (String chr : chrNames) {
                String igvChr = genome.getChromosomeAlias(chr);
                if (igvChr != null && !igvChr.equals(chr)) {
                    chrNameMap.put(igvChr, chr);
                }
            }
        }

        boolean normalizeCounts = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.NORMALIZE_COVERAGE);
        setNormalize(normalizeCounts);


    }

    public void setNormalize(boolean normalizeCounts) {
        setNormalizeCounts(normalizeCounts, 1.0e6f);
    }

    public void setNormalizeCounts(boolean normalizeCounts, float scalingFactor) {
        this.normalizeCounts = normalizeCounts;
        if (normalizeCounts && totalCount > 0) {
            normalizationFactor = scalingFactor / totalCount;
        } else {
            normalizationFactor = 1;
        }

    }

    public String getPath() {
        return reader == null ? null : reader.getPath();
    }

    public String getTrackName() {
        return trackName;
    }

    public double getDataMax() {
        return reader.getUpperLimit() * normalizationFactor;
    }

    public double getDataMin() {
        return reader.getLowerLimit() * normalizationFactor;
    }

    public void setAggregateLikeBins(boolean aggregateLikeBins) {
        this.aggregateLikeBins = aggregateLikeBins;
    }

    class Interval {

        String chr;
        private int start;
        private int end;
        private int zoom;
        private List<LocusScore> scores;

        public Interval(String chr, int start, int end, int zoom, List<LocusScore> scores) {
            this.chr = chr;
            this.start = start;
            this.end = end;
            this.zoom = zoom;
            this.scores = scores;
        }

        public boolean contains(String chr, int s, int e, int zoom) {
            return chr.equals(this.chr) && zoom == this.zoom && s >= getStart() && e <= getEnd();
        }

        /**
         * @return the start
         */
        public int getStart() {
            return start;
        }

        /**
         * @return the end
         */
        public int getEnd() {
            return end;
        }

        /**
         * @return the scores
         */
        public List<LocusScore> getScores() {
            return scores;
        }
    }

    private List<LocusScore> getCachedSummaryScores(String querySeq, int zoom, int tileNumber, double tileWidth) {

        String key = querySeq + "_" + zoom + "_" + tileNumber + "_" + windowFunction;

        List<LocusScore> scores = summaryScoreCache.get(key);
        if (scores == null) {

            int startLocation = (int) (tileNumber * tileWidth);
            int endLocation = (int) ((tileNumber + 1) * tileWidth);

            scores = getSummaryScores(querySeq, startLocation, endLocation, zoom);

            summaryScoreCache.put(key, scores);
        }

        return scores;

    }

    protected List<LocusScore> getSummaryScores(String querySeq, int startLocation, int endLocation, int zoom) {
        List<LocusScore> scores;
        if (zoom <= this.maxPrecomputedZoom) {

            // Window function == none => no windowing, so its not clear what to do.  For now use mean
            WindowFunction wf = (windowFunction == WindowFunction.none ? WindowFunction.mean : windowFunction);

            scores = new ArrayList(1000);
            TDFDataset ds = reader.getDataset(querySeq, zoom, wf);
            if (ds != null) {
                List<TDFTile> tiles = ds.getTiles(startLocation, endLocation);
                if (tiles.size() > 0) {
                    for (TDFTile tile : tiles) {

                        if (tile.getSize() > 0) {
                            for (int i = 0; i < tile.getSize(); i++) {
                                float v = tile.getValue(trackNumber, i);
                                if (!Float.isNaN(v)) {
                                    v *= normalizationFactor;
                                    scores.add(new BasicScore(tile.getStartPosition(i), tile.getEndPosition(i), v));
                                }
                            }
                        }
                    }
                }
            }
        } else {

            int chrLength = getChrLength(querySeq);
            if (chrLength == 0) {
                return Collections.emptyList();
            }
            endLocation = Math.min(endLocation, chrLength);
            // By definition there are 2^z tiles per chromosome, and 700 bins per tile, where z is the zoom level.
            // By definition there are 2^z tiles per chromosome, and 700 bins per tile, where z is the zoom level.
            //int maxZoom = (int) (Math.log(chrLength / 700) / Globals.log2) + 1;
            //int z = Math.min(zReq, maxZoom);
            int nTiles = (int) Math.pow(2, zoom);
            double binSize = Math.max(1, (((double) chrLength) / nTiles) / 700);

            scores = computeSummaryScores(querySeq, startLocation, endLocation, binSize);
        }
        return scores;
    }


    public int getChrLength(String chr) {
        if (chr.equals(Globals.CHR_ALL)) {
            return (int) (genome.getLength() / 1000);
        } else {
            Chromosome c = genome.getChromosome(chr);
            return c == null ? 0 : c.getLength();
        }
    }

    private List<LocusScore> computeSummaryScores(String chr, int startLocation, int endLocation, double scale) {

        List<LocusScore> scores = new ArrayList(1000);

        String dsName = "/" + chr + "/raw";

        TDFDataset rawDataset = reader.getDataset(dsName);
        if (rawDataset != null) {
            List<TDFTile> rawTiles = rawDataset.getTiles(startLocation, endLocation);
            if (rawTiles.size() > 0) {

                if (windowFunction == WindowFunction.none) {
                    for (TDFTile rawTile : rawTiles) {
                        // Tile of raw data
                        if (rawTile != null && rawTile.getSize() > 0) {

                            for (int i = 0; i < rawTile.getSize(); i++) {
                                int s = rawTile.getStartPosition(i);
                                int e = Math.max(s, rawTile.getEndPosition(i) - 1);

                                if (e < startLocation) {
                                    continue;
                                } else if (s > endLocation) {
                                    break;
                                }
                                float v = rawTile.getValue(trackNumber, i);
                                if (!Float.isNaN(v)) {
                                    v *= normalizationFactor;
                                }
                                scores.add(new BasicScore(s, e, v));
                            }
                        }
                    }


                } else {


                    Accumulator accumulator = new Accumulator(windowFunction, 5);
                    int accumulatedStart = -1;
                    int accumulatedEnd = -1;
                    int lastEndBin = 0;
                    for (TDFTile rawTile : rawTiles) {
                        // Tile of raw data
                        int size = rawTile.getSize();
                        if (rawTile != null && size > 0) {

                            int[] starts = rawTile.getStart();
                            int[] ends = rawTile.getEnd();
                            String[] features = rawTile.getNames();
                            float[] values = rawTile.getData(trackNumber);

                            // Loop through and bin scores for this interval.
                            for (int i = 0; i < size; i++) {

                                if (starts[i] >= endLocation) {
                                    break;  // We're beyond the end of the requested interval
                                }


                                int s = Math.max(startLocation, starts[i]);
                                int e = ends == null ? s + 1 : Math.min(endLocation, ends[i]);
                                float v = values[i] * normalizationFactor;

                                if (e < startLocation || Float.isNaN(v)) {
                                    continue;
                                }

                                String probeName = features == null ? null : features[i];

                                // Compute bin numbers, relative to start of this tile
                                int endBin = (int) ((e - startLocation) / scale);
                                int startBin = (int) ((s - startLocation) / scale);

                                // If this feature spans multiple bins, or extends beyond last end bin, record
                                if (endBin > lastEndBin || endBin > startBin) {
                                    if (accumulator.hasData()) {
                                        scores.add(getCompositeScore(accumulator, accumulatedStart, accumulatedEnd));
                                        accumulator = new Accumulator(windowFunction, 5);
                                    }
                                }

                                if (endBin > startBin) {
                                    scores.add(new NamedScore(s, e, v, probeName));
                                } else {
                                    if (!accumulator.hasData()) accumulatedStart = s;
                                    accumulatedEnd = e;
                                    accumulator.add(e - s, v, probeName);
                                }

                                lastEndBin = endBin;
                            }

                            // End of loop cleanup
                            if (accumulator.hasData()) {
                                scores.add(getCompositeScore(accumulator, accumulatedStart, accumulatedEnd));
                            }
                        }
                    }
                }
            }
        }
        return scores;
    }


    private LocusScore getCompositeScore(Accumulator accumulator, int accumulatedStart, int accumulatedEnd) {
        LocusScore ls;
        if (accumulator.getNpts() == 1) {
            ls = new NamedScore(accumulatedStart, accumulatedEnd, accumulator.getData()[0], accumulator.getNames()[0]);
        } else {
            float value = accumulator.getValue();
            ls = new CompositeScore(accumulatedStart, accumulatedEnd, value, accumulator.getData(),
                    accumulator.getNames(), windowFunction);
        }
        return ls;

    }

    public List<LocusScore> getSummaryScoresForRange(String chr, int startLocation, int endLocation, int zoom) {

        Chromosome chromosome = genome.getChromosome(chr);

        String tmp = chrNameMap.get(chr);
        String querySeq = tmp == null ? chr : tmp;

        // If we are in gene list view bypass caching.
        if (Globals.isHeadless() || FrameManager.isGeneListMode() || FrameManager.isExomeMode()) {

            return getSummaryScores(querySeq, startLocation, endLocation, zoom);

        } else {

            ArrayList scores = new ArrayList();

            // TODO -- this whole section could be computed once and stored,  it is only a function of the genome, chr, and zoom level.
            double tileWidth = 0;
            if (chr.equals(Globals.CHR_ALL)) {
                tileWidth = (genome.getLength() / 1000.0);
            } else {
                if (chromosome != null) {
                    tileWidth = chromosome.getLength() / ((int) Math.pow(2.0, zoom));
                }
            }
            if (tileWidth == 0) {
                return null;
            }


            int startTile = (int) (startLocation / tileWidth);
            int endTile = (int) (endLocation / tileWidth);
            for (int t = startTile; t <= endTile; t++) {
                List<LocusScore> cachedScores = getCachedSummaryScores(querySeq, zoom, t, tileWidth);
                if (cachedScores != null) {
                    for (LocusScore s : cachedScores) {
                        if (s.getEnd() >= startLocation) {
                            scores.add(s);
                        } else if (s.getStart() > endLocation) {
                            break;
                        }
                    }
                }

            }

            return scores;
        }
    }


    public TrackType getTrackType() {
        return reader.getTrackType();
    }

    public void setWindowFunction(WindowFunction wf) {
        this.windowFunction = wf;
    }

    public boolean isLogNormalized() {
        //return false;
        return getDataMin() < 0;
    }

    public void refreshData(long timestamp) {
        // ignored
    }

    public WindowFunction getWindowFunction() {
        return windowFunction;
    }

    public Collection<WindowFunction> getAvailableWindowFunctions() {
        return availableFunctions;
    }

}

