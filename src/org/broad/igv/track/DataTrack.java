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
 * FeatureTrackH5.java
 *
 * Created on November 12, 2007, 8:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.broad.igv.track;

import com.iontorrent.utils.ErrorHandler;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.FeatureUtils;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.renderer.DataRange;
import org.broad.igv.renderer.DataRenderer;
import org.broad.igv.renderer.GraphicUtils;
import org.broad.igv.renderer.XYPlotRenderer;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ResourceLocator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.broad.igv.data.seg.ReferenceSegment;

/**
 * Represents a track of numeric data
 *
 * @author jrobinso
 */
public abstract class DataTrack extends AbstractTrack {

    private static Logger log = Logger.getLogger(DataTrack.class);
    private DataRenderer renderer;
    // TODO -- memory leak.  This needs to get cleared when the gene list changes
    private HashMap<String, LoadedDataInterval> loadedIntervalCache = new HashMap(200);
    private boolean featuresLoading = false;

    private boolean dataRangeComputed;
    
    public DataTrack(ResourceLocator locator, String id, String name) {
        super(locator, id, name);
        autoScale = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.CHART_AUTOSCALE);

    }

    private void p(String s) {
        Logger.getLogger(getClass().getName()).info(s);
        System.out.println(getClass().getName() + ": " + s);
    }

    @Override
    public void render(RenderContext context, Rectangle rect) {

        if (featuresLoading) {
            p("render: features still loading");
            return;
        }

        String chr = context.getChr();
        boolean whole = Globals.CHR_ALL.equalsIgnoreCase(chr);
        int start = (int) context.getOrigin();
        int end = (int) context.getEndLocation() + 1;
        int zoom = context.getZoom();

       // p("======================== Rendering: "+chr+", "+start+"-"+end+", whole: "+whole);
        List<LocusScore> inViewScores = null;

        LoadedDataInterval interval = loadedIntervalCache.get(context.getReferenceFrame().getName());
        if (interval != null && interval.contains(chr, start, end, zoom)) {
            inViewScores = interval.getScores();
      //         p("render: getting scores from cache via interval "+ context.getReferenceFrame().getName()+": "+inViewScores.size());
        } else {
            // Get a buffer +/- 50% of screen size
            int delta = (end - start) / 2;
            int adjustedStart = Math.max(0, start - delta);
            int adjustedEnd = end + delta;

            inViewScores = load(context, chr, adjustedStart, adjustedEnd, zoom);
       //        p("render: Loading scores from "+chr+", "+adjustedStart+"-"+adjustedEnd+": "+inViewScores.size());  
            //   p("render: got "+getRefs(inViewScores).size()+" REF segments");
        }

        //For TDF backwards incompatibility, tell user if CHR_ALL not available
        if (inViewScores.size() == 0 && whole) {
            Graphics2D g = context.getGraphic2DForColor(Color.gray);
            GraphicUtils.drawCenteredText("render: Data not available for whole genome view (yet); zoom in to see data", rect, g);
        } else if (whole) {
       //     p("render: After whole genome load scores, got: " + inViewScores.size());
        }

        if (autoScale && !FrameManager.isGeneListMode() && !FrameManager.isExomeMode()) {
            //  p("render:autoscale: got "+getRefs(inViewScores).size()+" REF segments");

            InViewInterval inter = computeScale(start, end, inViewScores);

            if (inter.endIdx > inter.startIdx) {
                if (!whole) {
                    ArrayList<LocusScore> ref = getRefs(inViewScores);
                    inViewScores = inViewScores.subList(inter.startIdx, inter.endIdx);
                    // keep references scores
                    if (ref.size()>0) {
                      //  p("render: got "+ref.size()+" reference segments, adding those to inviewscores");
                        for (LocusScore r: ref) {
                            if (!inViewScores.contains(r)) inViewScores.add(r);
                        }
                    }
                }
                if (!this.isDataRangeComputed()) {
                    DataRange dr = getDataRange();

                 //   log.info("      DataTrack.render: got data range: " + dr + ", checking max if needed");
                    for (LocusScore sc : inViewScores) {
                        if (sc.getScore() > dr.getMaximum()) {
                            //  log.info("Got larger value: "+sc.getScore());
                            dr.setMaximum((float) sc.getScore());
                        }
                    }
                    float min = Math.min(0, inter.dataMin);
                    float base = Math.max(min, dr.getBaseline());
                    float max = inter.dataMax;
                    // Pathological case where min ~= max  (no data in view)
                    if (max - min <= (2 * Float.MIN_VALUE)) {
                        max = min + 1;

                    }

                    DataRange newDR = new DataRange(min, base, max, dr.isDrawBaseline());
                    newDR.setType(dr.getType());
                    setDataRange(newDR);
                   // log.info("      DataTrack.render: got data range at end: " + dr);
                    this.setDataRangeComputed(true);
                }
            }

        }

        // else  p("Rendering "+inViewScores.size()+" in view scores, such as this one: "+inViewScores.get(0));
        if (whole && inViewScores != null) {
        //    p("render: Rendering " + inViewScores.size() + " in view scores, for WHOLE genome: "+getRenderer().getClass().getName());
        }
      //  p("render: Rendering from x="+rect.getX()+"-"+rect.getMaxX()+" and y="+rect.getY()+"-"+(rect.getMaxY()));
        getRenderer().render(inViewScores, context, rect, this);
    }

    public ArrayList<DataTrack> getLinkedTracks() {        
        ArrayList<DataTrack> linkedtracks = new ArrayList<DataTrack>();
        ArrayList<DataTrack> done = new ArrayList<DataTrack>();
       // log.info("=========== get linked Tracks for " + this.getDisplayName() + "============");
        linkDataRange_r(linkedtracks, done);

        return linkedtracks;
    }

    public void linkDataRanges(ArrayList<DataTrack> linkedtracks) {
        
        if (linkedtracks == null || linkedtracks.size() < 1) {
            //no linked tracks
            return;
        }
        DataRange dr = this.getDataRange();
        float min = dr.getMinimum();
        float max = dr.getMaximum();
        float baseline = dr.getBaseline();
        int nr = linkedtracks.size();
        linkedtracks.add(this);
        for (AbstractTrack track : linkedtracks) {
            DataRange r = track.getDataRange();
            min = Math.min(min, r.getMinimum());
            max = Math.max(max, r.getMaximum());
            baseline = (baseline + r.getBaseline());
        }
        baseline = baseline / (nr + 1);
        dr = new DataRange(min, baseline, max);
        setDataRange(dr);
       // log.info("linkDataRange: Got new data range: "+min+"-"+max+", base="+baseline+", updating other tracks!");
        for (AbstractTrack t : linkedtracks) {
           // log.info("       linkDataRange: updating "+t.getDisplayName()+" with "+min+"-"+max);
            t.setDataRange(dr);
        }
    }

    public void computeVisibleDataRange(RenderContext context ) {

        if (featuresLoading) {
            return;
        }

        String chr = context.getChr();
        boolean whole = Globals.CHR_ALL.equals(chr);
        int start = (int) context.getOrigin();
        int end = (int) context.getEndLocation() + 1;
        int zoom = context.getZoom();

     //   p("================computeVisibleDataRange: "+this.getDisplayName()+": " + chr + ", " + start + "-" + end);
        List<LocusScore> inViewScores = null;

        LoadedDataInterval interval = loadedIntervalCache.get(context.getReferenceFrame().getName());
        if (interval != null && interval.contains(chr, start, end, zoom)) {
            inViewScores = interval.getScores();
            //   p("render: getting scores from cache via interval "+ context.getReferenceFrame().getName()+": "+inViewScores.size());
        } else {
            // Get a buffer +/- 50% of screen size
            int delta = (end - start) / 2;
            int adjustedStart = Math.max(0, start - delta);
            int adjustedEnd = end + delta;

            inViewScores = load(context, chr, adjustedStart, adjustedEnd, zoom);
        }

        if (autoScale && !FrameManager.isGeneListMode() && !FrameManager.isExomeMode()) {

            InViewInterval inter = computeScale(start, end, inViewScores);

            if (inter.endIdx > inter.startIdx) {
                if (!whole) {
                    ArrayList<LocusScore> ref = getRefs(inViewScores);
                    inViewScores = inViewScores.subList(inter.startIdx, inter.endIdx);
                    // keep references scores
                 //   p("render: got "+ref.size()+" reference segments, adding those to inviewscores");
                    for (LocusScore r: ref) {                    
                        if (!inViewScores.contains(r)) {                            
                            inViewScores.add(r);
                        }
                    }
                }
                DataRange dr = getDataRange();

             //   log.info("      DataTrack.computeVisibleDataRange: got data range: " + dr + ", adjusting min/ max");
                for (LocusScore sc : inViewScores) {
                    if (sc.getScore() > dr.getMaximum()) {
                        //  log.info("Got larger value: "+sc.getScore());
                        dr.setMaximum((float) sc.getScore());
                    }
                }
                float min = Math.min(0, inter.dataMin);
                float base = Math.max(min, dr.getBaseline());
                float max = inter.dataMax;
                // Pathological case where min ~= max  (no data in view)
                if (max - min <= (2 * Float.MIN_VALUE)) {
                    max = min + 1;
                }

                DataRange newDR = new DataRange(min, base, max, dr.isDrawBaseline());
                newDR.setType(dr.getType());
                setDataRange(newDR);        
         //       log.info("      DataTrack.computeVisibleDataRange: at end: got data range: " + dr);
            }
        }
     //    p("================ END OF computeVisibleDataRange: dr=" + this.getDataRange());
         this.setDataRangeComputed(true);
    }

    public void linkDataRange_r(ArrayList<DataTrack> linkedtracks, ArrayList<DataTrack> done) {
        done.add(this);

        String other = null;

        if (this.getLinkedTrack() != null && this.getLinkedTrack().trim().length() > 0) {
            //   log.info("    linkDataRange_r: ========= checking for linked data track for "+this.getDisplayName());
            other = this.getLinkedTrack().trim().toLowerCase();
            //    log.info("    linkDataRange_r:Got linked track " + other.substring(other.length()/2) + ". Will try to find it, and use a common data range");
        }
        for (Track track : IGV.getInstance().getAllTracks()) {
            String tracklink = track.getLinkedTrack();
            boolean found = false;
            if (other != null && ((track.getId() != null && track.getId().equalsIgnoreCase(other))
                    || (track.getResourceLocator() != null && track.getResourceLocator().getPath() != null && track.getResourceLocator().getPath().equalsIgnoreCase(other)))) {
                if (track != this) {
                    found = true;
                    //             log.info("     linkDataRange_r:Found track link :"+other+"-> "+track.getId());
                }

            } else if (tracklink != null) {
                tracklink = tracklink.trim().toLowerCase();
                if ((getName() != null && getName().equalsIgnoreCase(tracklink))
                        || (getId() != null && getId().equalsIgnoreCase(tracklink))
                        || (getResourceLocator() != null && getResourceLocator().getPath() != null && track.getResourceLocator().getPath().equalsIgnoreCase(tracklink))) {
                    if (track != this) {
                        found = true;
                        //            log.info("     linkDataRange_r:Found reverse track link :"+track.getId()+"-> "+tracklink);
                    }
                }
            }
            if (found) {
                if (!done.contains(track)) {
                    //        log.info("     linkDataRange_r: ===== recursion: track not done "+track.getId());
                    DataRange odr = track.getDataRange();
                    if (odr != null && track instanceof AbstractTrack) {
                        linkedtracks.add((DataTrack) track);
                        DataTrack at = (DataTrack) track;
                        at.linkDataRange_r(linkedtracks, done);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void preload(RenderContext context) {
        LoadedDataInterval interval = loadedIntervalCache.get(context.getReferenceFrame().getName());

        String chr = context.getChr();
        int start = (int) context.getOrigin();
        int end = (int) context.getEndLocation() + 1;
        int zoom = context.getZoom();
        if (interval == null || !interval.contains(chr, start, end, zoom)) {
            int delta = (end - start) / 2;
            int adjustedStart = Math.max(0, start - delta);
            int adjustedEnd = end + delta;
            List<LocusScore> scores = load(context, chr, adjustedStart, adjustedEnd, zoom);

            if (autoScale && FrameManager.isExomeMode()) {
                InViewInterval inter = computeScale(start, end, scores);
                if (inter.endIdx > inter.startIdx) {

                    DataRange dr = getDataRange();
                    float min = Math.min(0, inter.dataMin);
                    float base = Math.max(min, dr.getBaseline());
                    float max = inter.dataMax;
                    // Pathological case where min ~= max  (no data in view)
                    if (max - min <= (2 * Float.MIN_VALUE)) {
                        max = min + 1;
                    }

                    DataRange newDR = new DataRange(min, base, max, dr.isDrawBaseline());
                    newDR.setType(dr.getType());
                    setDataRange(newDR);

                }
            }

        }


    }

    public List<LocusScore> load(final RenderContext context, final String chr, final int start, final int end, final int zoom) {

        try {
            featuresLoading = true;
            int maxEnd = end;
            Genome genome = GenomeManager.getInstance().getCurrentGenome();

            String queryChr = chr;
            if (genome != null) {
                queryChr = genome.getChromosomeAlias(chr);
                Chromosome c = genome.getChromosome(chr);
                if (c != null) {
                    maxEnd = Math.max(c.getLength(), end);
                }
            }
            // Expand interval +/- 50%
            int delta = (end - start) / 2;
            int expandedStart = Math.max(0, start - delta);
            int expandedEnd = Math.min(maxEnd, end + delta);
            List<LocusScore> inViewScores = getSummaryScores(queryChr, expandedStart, expandedEnd, zoom);
         //   p("called getSummaryScore: "+queryChr+", "+expandedStart+"-"+expandedEnd+":"+inViewScores.size());

            //   p("got "+getRefs(inViewScores).size()+" REF segments");
            LoadedDataInterval interval = new LoadedDataInterval(chr, start, end, zoom, inViewScores);
            //   p("load: put into interval cache "+inViewScores.size()+" scores from "+start+"-"+end+" for framename="+context.getReferenceFrame().getName());
            // count ref

            //   p("got "+getRefs(interval.getScores()).size()+" REF segments");
            loadedIntervalCache.put(context.getReferenceFrame().getName(), interval);
            return inViewScores;

        } finally {
            featuresLoading = false;
        }

    }

    private ArrayList<LocusScore> getRefs(List<LocusScore> scores) {
        ArrayList<LocusScore> refs = new ArrayList<LocusScore>();
        for (LocusScore s : scores) {
            if (s instanceof ReferenceSegment) {
                refs.add(s);
            }
        }
        return refs;
    }

    public void clearCaches() {
        loadedIntervalCache.clear();
    }

    public void setRendererClass(Class rc) {
        try {
            renderer = (DataRenderer) rc.newInstance();
        } catch (Exception ex) {
            log.error("Error instantiating renderer ", ex);
        }
    }

    public DataRenderer getRenderer() {
        if (renderer == null) {
            setRendererClass(getDefaultRendererClass());
        }
        return renderer;
    }

    /**
     * Return a value string for the tooltip window at the given location, or
     * null to signal there is no value at that location
     *
     * @param chr
     * @param position
     * @param y
     * @param frame
     * @return
     */
    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {
        StringBuilder buf = new StringBuilder();
        LocusScore score = getLocusScoreAt(chr, position, frame);
        // If there is no value here, return null to signal no popup
        if (score == null) {
            return null;
        }
        buf.append(getName() + "<br>");
        if ((getDataRange() != null) && (getRenderer() instanceof XYPlotRenderer)) {
            buf.append("Data scale: " + getDataRange().getMinimum() + " - " + getDataRange().getMaximum() + "<br>");
        }
        if (this.getLinkedTrack() != null) {
            buf.append("<b>Data scale is linked to track: " + getLinkedTrack() + "</b><br>");
        }
        
        if (this.getCutoffScore() != 0 && this.getAltColor() != this.getColor() && this.getDataRange() != null) {
            if (this.getCutoffScore() > -this.getDataRange().getMinimum() && this.getCutoffScore() <= this.getDataRange().getMaximum()) {
                buf.append("Value where color changes: " + this.getCutoffScore() + "<br>");
            }
        }

        buf.append(score.getValueString(position, getWindowFunction()));
        return buf.toString();
    }

    protected LocusScore getLocusScoreAt(String chr, double position, ReferenceFrame frame) {
        int zoom = Math.max(0, frame.getZoom());
        List<LocusScore> scores = getSummaryScores(chr, (int) position - 10, (int) position + 10, zoom);


        if (scores == null) {
            return null;
        } else {
            // give a 2 pixel window, otherwise very narrow features will be missed.
            double bpPerPixel = frame.getScale();
            int buffer = (int) (2 * bpPerPixel);    /* * */
            return (LocusScore) FeatureUtils.getClosestFeatureAt(position, buffer, scores);
        }
    }

    abstract public List<LocusScore> getSummaryScores(String chr, int startLocation, int endLocation, int zoom);

    @Override
    public void setColor(Color color) {
        super.setColor(color);
    }

    @Override
    public void setAltColor(Color color) {
        super.setAltColor(color);

    }

    private InViewInterval computeScale(double origin, double end, List<LocusScore> scores) {

        InViewInterval interval = new InViewInterval();
        // boolean show = scores.size()==2;
        boolean show = true;
        if (scores.size() == 1) {
            interval.dataMax = Math.max(0, scores.get(0).getScore());
            interval.dataMin = Math.min(0, scores.get(0).getScore());
        } else {
            interval.startIdx = 0;
            interval.endIdx = scores.size();
            for (int i = 1; i < scores.size(); i++) {
                if (!(scores.get(i) instanceof ReferenceSegment)) {
                    if (scores.get(i).getEnd() >= origin) {
                        interval.startIdx = i - 1;

                        break;
                    }
                }
            }
            //  if (show) p("interval: "+interval.toString());

            for (int i = interval.startIdx; i < scores.size(); i++) {
                if (!(scores.get(i) instanceof ReferenceSegment)) {
                    LocusScore locusScore = scores.get(i);
                    float value = locusScore.getScore();
                    if (Float.isNaN(value)) {
                        value = 0;
                    }
                    interval.dataMax = Math.max(interval.dataMax, value);
                    interval.dataMin = Math.min(interval.dataMin, value);
                    if (locusScore.getStart() > end) {
                        interval.endIdx = i;
                        break;
                    }
                }
            }
        }

        return interval;
    }

    @Override
    public Map<String, String> getPersistentState() {
        Map<String, String> properties = super.getPersistentState();
        properties.put("autoScale", String.valueOf(autoScale));
        return properties;
    }

    @Override
    public void restorePersistentState(Map<String, String> attributes) {
        super.restorePersistentState(attributes);
        String as = attributes.get("autoScale");
        if (as != null) {
            try {
                autoScale = Boolean.parseBoolean(as);

            } catch (Exception e) {
                log.error("Error restoring session.  Invalid autoScale value: " + autoScale);

            }
        }
    }

    /**
     * Get the score over the provided region for the given type. Different
     * types are processed differently. Results are cached according to the
     * provided frameName, if provided. If not, a string is created based on the
     * inputs.
     *
     * @param chr
     * @param start
     * @param end
     * @param zoom
     * @param type
     * @param frameName
     * @param tracks Mutation scores require other tracks to calculate the
     * score. If provided, use these tracks. If null and not headless we use the
     * currently loaded tracks.
     * @return
     */
    public float getRegionScore(String chr, int start, int end, int zoom, RegionScoreType type, String frameName,
            List<Track> tracks) {

        if (end <= start) {
            return 0;
        }
        if (isRegionScoreType(type)) {

            List<LocusScore> scores = null;
            if (frameName == null) {
                //Essentially covering headless case here
                frameName = (chr + start) + end;
                frameName += zoom;
                frameName += type;
            }

            LoadedDataInterval loadedInterval = loadedIntervalCache.get(frameName);
            if (loadedInterval != null && loadedInterval.contains(chr, start, end, zoom)) {
                scores = loadedInterval.getScores();
            } else {
                scores = this.getSummaryScores(chr, start, end, zoom);
                p("getRegionScore: put into interval cache " + scores.size() + " scores from " + start + "-" + end + " for framename=" + frameName);
                loadedIntervalCache.put(frameName, new LoadedDataInterval(chr, start, end, zoom, scores));
            }


            if (type == RegionScoreType.FLUX) {
                float sumDiffs = 0;
                float lastScore = Float.NaN;
                for (LocusScore score : scores) {
                    if ((score.getEnd() >= start) && (score.getStart() <= end)) {
                        if (Float.isNaN(lastScore)) {
                            lastScore = Math.min(2, Math.max(-2, logScaleData(score.getScore())));
                        } else {
                            float s = Math.min(2, Math.max(-2, logScaleData(score.getScore())));
                            sumDiffs += Math.abs(s - lastScore);
                            lastScore = s;
                        }
                    }
                }
                return sumDiffs;

            } else if (type == RegionScoreType.MUTATION_COUNT) {
                // Sort by overlaid mutation count.
                if (!Globals.isHeadless() && tracks == null) {
                    tracks = IGV.getInstance().getOverlayTracks(this);
                }
                float count = 0;
                String tSamp = this.getSample();
                if (tracks != null && tSamp != null) {
                    for (Track t : tracks) {
                        if (t.getTrackType() == TrackType.MUTATION && tSamp.equals(t.getSample())) {
                            count += t.getRegionScore(chr, start, end, zoom, type, frameName);
                        }
                    }
                }
                return count;
            } else {
                float regionScore = 0;
                int intervalSum = 0;
                for (LocusScore score : scores) {
                    if ((score.getEnd() >= start) && (score.getStart() <= end)) {
                        int interval = Math.min(end, score.getEnd()) - Math.max(start, score.getStart());
                        float value = score.getScore();
                        regionScore += value * interval;
                        intervalSum += interval;
                    }
                }
                if (intervalSum <= 0) {
                    // No scores in interval
                    return -Float.MAX_VALUE;
                } else {
                    regionScore /= intervalSum;
                    return (type == RegionScoreType.DELETION) ? -regionScore : regionScore;
                }
            }

        } else {
            return -Float.MAX_VALUE;
        }
    }

    /**
     * Return the average zcore over the interval.
     *
     * @param chr
     * @param start
     * @param end
     * @param zoom
     * @return
     */
    public double getAverageScore(String chr, int start, int end, int zoom) {
        double regionScore = 0;
        int intervalSum = 0;
        Collection<LocusScore> scores = getSummaryScores(chr, start, end, zoom);
        for (LocusScore score : scores) {
            if ((score.getEnd() >= start) && (score.getStart() <= end)) {
                int interval = 1; //Math.min(end, score.getEnd()) - Math.max(start, score.getStart());
                float value = score.getScore();
                regionScore += value * interval;
                intervalSum += interval;
            }
        }
        if (intervalSum > 0) {
            regionScore /= intervalSum;
        }
        return regionScore;
    }

    /**
     * @return the dataRangeComputed
     */
    public boolean isDataRangeComputed() {
        return dataRangeComputed;
    }

    /**
     * @param dataRangeComputed the dataRangeComputed to set
     */
    public void setDataRangeComputed(boolean dataRangeComputed) {
        this.dataRangeComputed = dataRangeComputed;
    }

    class InViewInterval {

        int startIdx;
        int endIdx;
        float dataMax = 0;
        float dataMin = 0;

        public String toString() {
            return "startIdx=" + startIdx + ", endIdx=" + endIdx + ", dataMin=" + dataMin + ", dataMax=" + dataMax;
        }
    }
}
