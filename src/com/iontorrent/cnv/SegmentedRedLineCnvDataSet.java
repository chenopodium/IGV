/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.broad.igv.data.seg.ReferenceSegment;
import org.broad.igv.data.seg.Segment;
import org.broad.igv.data.seg.SegmentedDataSet;
import org.broad.igv.data.seg.SummarySegment;
import org.broad.igv.feature.FeatureUtils;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.track.TrackType;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal
 */
public class SegmentedRedLineCnvDataSet implements SegmentedDataSet {

    TrackType trackType = TrackType.COPY_NUMBER;
    float dataMax = -Float.MAX_VALUE;
    float dataMin = Float.MAX_VALUE;
    /**
     * Assume data is non-log value until suggested otherwise by the precense of
     * negative numbers. TODO This is a fragile assumption, the user should
     * input this information directly.
     */
    private boolean logNormalized = false;
    /**
     * Map of [heading -> [chr -> [list of chrSegments]]]
     */
    private Map<String, Map<String, List<LocusScore>>> segments = new HashMap();
    /**
     * Set of chromosomes represented in this dataset
     */
    private Set<String> chromosomes = new HashSet();
    private List<String> headings = new ArrayList();
    private Map<String, List<LocusScore>> wholeGenomeScoresCache = new HashMap();
    private TrackProperties trackProperties;
    Genome genome;
    int count;
    private ArrayList<String> loadedChromosomes;
    private CnvData data;
    private String id;
    private boolean useIndex;

    public SegmentedRedLineCnvDataSet(boolean useIndex, String id, CnvData data) {
        this.genome = IGV.getInstance().getGenomeManager().getCurrentGenome();
        loadedChromosomes = new ArrayList<String>();
        this.data = data;
        this.id = id;
        p("Constructor of SegmentedRedLineCnvDataSet. useIndex is: "+useIndex+" (but ignoring it)");
      //  if (useIndex != true) {
            loadData("ALL");
     //   }
     //   else p("NOT loading data yet");
    }

    public void sortLists() {
        for (Map.Entry<String, Map<String, List<LocusScore>>> sampleEntry : segments.entrySet()) {
            for (Map.Entry<String, List<LocusScore>> chrEntry : sampleEntry.getValue().entrySet()) {
                List<LocusScore> tmp = chrEntry.getValue();
                FeatureUtils.sortFeatureList(tmp);
            }
        }
    }

    private void loadData(String chr) {
        p(" ============================== loadData "+chr);
        data.loadData(chr);
        
        if (data.getPoints() != null) {
            // load just data for chr unless it is null
            int count = 0;
            for (CnvDataPoint point : data.getPoints()) {
                // use hash map or similar
                if (chr == null || chr.equals("ALL") || chr.equalsIgnoreCase("chr" + point.chr)) {
                    this.addSegment(chr, id, "chr" + point.chr, (int) point.pos, (int) point.end + 1, (float) point.ratio, point.clone, point.getAttributes());
                    count++;
                }
            }
           if (count >0) {
               p("Remembering that we loaded "+chr+":  we found "+count);
               loadedChromosomes.add(chr);
           }
           
        }
        else p("Probably just have summary and red line data");
        if ( data.getRedLinePoints() != null ) {
            p("loadData: Adding read line data "+data.getRedLinePoints().size()+" for chr "+chr);
            for (CnvDataPoint point : data.getRedLinePoints()) {
                // use hash map or similar
                if (chr == null || chr.equals("ALL") || chr.equalsIgnoreCase("chr" + point.chr)) {
                //    p("Adding REF data");
                    this.addSegment(chr, id, "chr" + point.chr, (int) point.pos, (int) point.end + 1, (float) point.ratio, "REF", point.getAttributes());
                }                
            }
        }
        else p("Got NO redline data");
        if (data.getSummaryPoints() != null) {
            p("loadData: Adding SUMMARY data "+data.getRedLinePoints().size()+" for chr "+chr);
            for (CnvDataPoint point : data.getSummaryPoints()) {
                // use hash map or similar
                if (chr == null || chr.equals("ALL")|| chr.equalsIgnoreCase("chr" + point.chr) ) {
             //       p("Adding SUMMARY data");
                    // xxx atts
                    this.addSegment(chr, id, "chr" + point.chr, (int) point.pos, (int) point.end + 1, (float) point.ratio, "SUMMARY", point.getAttributes());
                }
            }
        }
        else p("Got NO summary data points");
        sortLists();
    }

    /**
     * initially we will just load entire chromosome data, but at some point we
     * might do a more fine grained version
     */
    private void loadSegments(String heading, String chr) {
        if (loadedChromosomes.contains(chr)) {
            p("Data for "+chr+" already loaded");
            return;
        }
        Map<String, List<LocusScore>> chrSegments = segments.get(heading);
        if (chrSegments == null) {
            headings.add(heading);
            chrSegments = new HashMap();
            segments.put(heading, chrSegments);
        }
     
        p("loadSegments: calling loadData");
        loadData(chr);
    }

    /**
     *
     */
    public void addSegment(String chrToRemember, String heading, String c, int start, int end, float value, String des, HashMap<String,String> atts) {
        count++;
        String chr = genome == null ? c : genome.getChromosomeAlias(c);
        // p("Adding seg to chr "+chr);
        Map<String, List<LocusScore>> chrSegments = segments.get(heading);
        if (chrSegments == null) {
            headings.add(heading);
            chrSegments = new HashMap();
            segments.put(heading, chrSegments);
        }

        List<LocusScore> segmentList = chrSegments.get(chrToRemember);
        if (segmentList == null) {
            segmentList = new ArrayList<LocusScore>();
            chrSegments.put(chrToRemember, segmentList);
        }
        Segment seg = null;
        if (des != null && des.equalsIgnoreCase("REF")) {
            seg=  new ReferenceSegment(chr, start, start, end, end, value, des, atts);
         //   p("Created REF segment: "+seg);
        }
        else if (des != null && des.equalsIgnoreCase("SUMMARY")) {
            seg=  new SummarySegment(chr, start, start, end, end, value, des, atts);
            p("Created SUMMARY segment: "+seg);
        }
        else seg=   new Segment(chr, start, start, end, end, value, des, atts);
        segmentList.add(seg);
        dataMax = Math.max(dataMax, value);
        dataMin = Math.min(dataMin, value);
        if (value < 0) {
            logNormalized = true;
        }
     //   if (count % 100 ==0) p("Got segment: "+seg+", max="+dataMax+", min="+dataMin);

      //  p("Adding "+chr+" to chromosomes, remembering segments with heading "+heading+"/"+chr);
        chromosomes.add(chr);

    }

    private void p(String s) {
       Logger.getLogger(getClass().getName()).info(s);
           System.out.println(getClass().getName()+": "+s);
    }

    /**
     * Method description
     *
     * @return
     */
    public Set<String> getChromosomes() {
        return chromosomes;
    }

    /**
     * Method description
     *
     * @param heading
     * @param chr
     * @return
     */
    @Override
    public List<LocusScore> getSegments(String heading, String chr) {
        p("========  getSegments for "+chr+", useIndex is: "+useIndex);
        if (useIndex ||  !loadedChromosomes.contains(chr))  {
            p(chr+" not loaded yet, calling loadSegments "+heading+" on chr "+chr);
            loadSegments(heading, chr);
        }
        else {
            p("NOT loading segments, because useIndex="+useIndex+", loaded.contains(chr)="+loadedChromosomes.contains(chr));
        }
        Map<String, List<LocusScore>> chrSegments = segments.get(heading);
        if (chrSegments == null) p(" ------------ Got NO segments for heading "+heading);
        List<LocusScore> res = (chrSegments == null) ? null : chrSegments.get(chr);
        if (res != null) {
//            p("Got locus scores: "+res.size()+" for chr "+chr);
            
        }
        else p("----------- getSements: Got NO locus scores for "+heading+"/"+chr);
        return res;
    }

    @Override
    public List<String> getSampleNames() {
        return headings;
    }

    /**
     * Method description
     *
     * @return
     */
    @Override
    public TrackType getType() {
        return trackType;
    }

    /**
     * Method description
     *
     * @return
     */
    @Override
    public boolean isLogNormalized() {
        return logNormalized;
    }

    /**
     * Method description
     *
     * @param chr
     * @return
     */
    @Override
    public double getDataMax(String chr) {
        return dataMax;
    }

    /**
     * Method description
     *
     * @param chr
     * @return
     */
    @Override
    public double getDataMin(String chr) {
        return dataMin;
    }

    /**
     * Method description
     *
     * @param heading
     * @return
     */
    @Override
    public List<LocusScore> getWholeGenomeScores(String heading) {
        p("========= getWholeGenomeScores "+heading);

        List<LocusScore> wholeGenomeScores = wholeGenomeScoresCache.get(heading);
        if ((wholeGenomeScores == null) || wholeGenomeScores.isEmpty()) {
            p("Got no whole genome scores yet");
            // Compute the smallest concievable feature that could be viewed on the
            // largest screen.  Be conservative.   The smallest feature is one at
            // the screen resolution scale in <chr units> / <pixel>
            double minFeatureSize = 0; // ((double) genome.getLength()) / (maxScreenSize * locationUnit);

            wholeGenomeScores = new ArrayList(1000);

            //for (String chr : genome.getLongChromosomeNames()) {
            String chr = "ALL";
            List<LocusScore> chrSegments = getSegments(heading, chr);
            if (chrSegments != null) {
         //        p("getWholeGenomeScores: got "+chrSegments.size()+" for "+heading+"/"+chr);
                for (LocusScore score : chrSegments) {
                    Segment seg = (Segment) score;
                    String segchr = seg.getChr();
                    int gStart = genome.getGenomeCoordinate(segchr, seg.getStart());
                    int gEnd = genome.getGenomeCoordinate(segchr, seg.getEnd());
                    if ((gEnd - gStart) >= minFeatureSize) {
                        Segment s = null;
                        if (score instanceof ReferenceSegment) {
                          //  p("Got ref segment: "+score);
                            s = new ReferenceSegment(segchr, gStart, gStart, gEnd,
                                    gEnd, seg.getScore(), seg.getDescription(), seg.getAttributes());
                        }else if (score instanceof SummarySegment) {
                          //  p("Got ref segment: "+score);
                            s = new SummarySegment(segchr, gStart, gStart, gEnd,
                                    gEnd, seg.getScore(), seg.getDescription(), seg.getAttributes());                        
                        } else {
                            s = new Segment(segchr, gStart, gStart, gEnd,
                                    gEnd, seg.getScore(), seg.getDescription(), seg.getAttributes());
                        }
                        wholeGenomeScores.add(s);
                    }
                    //           else p("Too small: "+(gEnd-gStart));
                }

           }
              //  offset += genome.getChromosome(chr).getLength();
           // }
            wholeGenomeScoresCache.put(heading, wholeGenomeScores);
        }
        else p("Got "+wholeGenomeScores.size()+" whole genome scores");
        return wholeGenomeScores;

    }

    /**
     * Method description
     *
     * @return
     */
    public TrackType getTrackType() {
        return trackType;
    }

    /**
     * Method description
     *
     * @param trackType
     */
    public void setTrackType(TrackType trackType) {
        this.trackType = trackType;
    }

    public void setTrackProperties(TrackProperties props) {
        this.trackProperties = props;
    }

    public TrackProperties getTrackProperties() {
        if (trackProperties == null) {
            trackProperties = new TrackProperties();
        }
        return trackProperties;
    }
}
