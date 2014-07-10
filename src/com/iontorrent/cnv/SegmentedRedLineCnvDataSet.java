/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.utils.ErrorHandler;
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
        int ichr = CnvData.getChr(chr);
        p(" ============================== loadData "+chr+", ichr="+ichr);
        data.loadData(chr);
        
       
        if (data.getPoints() != null) {
            // load just data for chr unless it is null
            int count = 0;
            for (CnvDataPoint point : data.getPoints()) {
                // use hash map or similar
                
                if (chr == null || chr.toLowerCase().endsWith("all")  || ichr == point.chr) {
                    this.addSegment(chr, id, "chr" + point.chr, (int) point.pos, (int) point.end + 1, (float) point.ratio, point.clone, point.getAttributes());
                    count++;
                }
            }
           if (count >0) {
              // p("Remembering that we loaded "+chr+":  we found "+count);
               loadedChromosomes.add(chr);
           }
           
        }
        else p("Probably just have summary and red line data");
        int refs = 0;
        if ( data.getRedLinePoints() != null ) {
            p("loadData: Adding red line data "+data.getRedLinePoints().size()+" for chr "+chr);
            for (CnvDataPoint point : data.getRedLinePoints()) {
                // use hash map or similar
                if (chr == null || chr.toLowerCase().endsWith("all") || ichr == point.chr) {
           //         p("Adding REF data");
                    this.addSegment(chr, id, "chr" + point.chr, (int) point.pos, (int) point.end + 1, (float) point.ratio, "REF", point.getAttributes());
                    refs++;
                }    
               // else p("Not right chromosome: "+point.chr+"<>"+chr);
            }
            p("Got "+refs+" red lines for chr "+chr);
        }
        else p("Got NO redline data");
        if (data.getSummaryPoints() != null) {
        //    p("loadData: Adding SUMMARY data "+data.getRedLinePoints().size()+" for chr "+chr);
            for (CnvDataPoint point : data.getSummaryPoints()) {
                // use hash map or similar
                if (chr == null || chr.toLowerCase().endsWith("all") || ichr == point.chr ) {
             //       p("Adding SUMMARY data");
                    // xxx atts
                    this.addSegment(chr, id, "chr" + point.chr, (int) point.pos, (int) point.end + 1, (float) point.ratio, "SUMMARY", point.getAttributes());
                }
            }
        }
        //else p("Got NO summary data points");
        sortLists();
    }

    /**
     * initially we will just load entire chromosome data, but at some point we
     * might do a more fine grained version
     */
    private void loadSegments(String heading, String chr) {
        if (loadedChromosomes.contains(chr)) {
      //      p("Data for "+chr+" already loaded");
            return;
        }
        Map<String, List<LocusScore>> chrSegments = segments.get(heading);
        if (chrSegments == null) {
            headings.add(heading);
            chrSegments = new HashMap();
            segments.put(heading, chrSegments);
        }
     
     //   p("loadSegments: calling loadData");
        loadData(chr);
    }

    /**
     *
     */
    public void addSegment(String chrToRemember, String heading, String c, int start, int end, float value, String des, HashMap<String,String> atts) {
        count++;
        String chr = genome == null ? c : genome.getChromosomeAlias(c);
        String alt = getAlt(chr);
        
        
        Map<String, List<LocusScore>> chrSegments = segments.get(heading);
        if (chrSegments == null) {
            headings.add(heading);
            chrSegments = new HashMap();
            segments.put(heading, chrSegments);
        }
        //if (!chrToRemember.toLowerCase().endsWith("all")) {
            List<LocusScore> segmentList = chrSegments.get(chrToRemember);
            if (segmentList == null) {
                segmentList = new ArrayList<LocusScore>();
                chrSegments.put(chrToRemember, segmentList);
            }
            segmentList.add(getSegForChr(des, chr, start, end, value, atts));    
            chromosomes.add(chrToRemember);
        //}
        if (!chr.equals(chrToRemember)) {
             segmentList = chrSegments.get(chr);
            if (segmentList == null) {
                segmentList = new ArrayList<LocusScore>();
                chrSegments.put(chr, segmentList);
            }            
            segmentList.add(getSegForChr(des, chr, start, end, value, atts)); 
            chromosomes.add(chr);
        }
                 
         if (alt != null) {
            List<LocusScore> altsegmentList = chrSegments.get(alt);
            if (altsegmentList == null) {
                altsegmentList = new ArrayList<LocusScore>();
                chrSegments.put(alt, altsegmentList);
//                if (chrToRemember.equalsIgnoreCase("all")) {
//                    p("Tracing ALL:"+ErrorHandler.getString(new Exception("test")));
//                }
                p("Adding seg to chr " + chr+"/"+chrToRemember+" and " + alt);
            }                        
            altsegmentList.add(getSegForChr(des, alt, start, end, value, atts));
            chromosomes.add(alt);
        }
        dataMax = Math.max(dataMax, value);
        dataMin = Math.min(dataMin, value);
        if (value < 0) {
            logNormalized = true;
        }
     

      //  p("Adding "+chr+" to chromosomes, remembering segments with heading "+heading+"/"+chr);
        

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
       // p("=================  getSegments for "+chr+", useIndex is: "+useIndex);
        if (useIndex ||  !loadedChromosomes.contains(chr))  {
         //   p(chr+" not loaded yet, calling loadSegments "+heading+" on chr "+chr);
            loadSegments(heading, chr);
        }
        else {
         //   p("NOT loading segments, because useIndex="+useIndex+", loaded.contains(chr)="+loadedChromosomes.contains(chr));
        }
        Map<String, List<LocusScore>> chrSegments = segments.get(heading);
        if (chrSegments == null) p(" ------------ Got NO segments for heading "+heading);
        List<LocusScore> res = (chrSegments == null) ? null : chrSegments.get(chr);
        if (res != null) {
//            p("Got locus scores: "+res.size()+" for chr "+chr);
            
        }
     //   else p("----------- getSements: Got NO locus scores for "+heading+"/"+chr);
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
        p("========= getWholeGenomeScores ");

        int refs = 0;
        List<LocusScore> wholeGenomeScores = wholeGenomeScoresCache.get(heading);
        
        if ((wholeGenomeScores == null) || wholeGenomeScores.isEmpty()) {
            p("COMPUTING WHOLE GENOME SCORES: "+heading);
            // Compute the smallest concievable feature that could be viewed on the
            // largest screen.  Be conservative.   The smallest feature is one at
            // the screen resolution scale in <chr units> / <pixel>
            double minFeatureSize = 0; // ((double) genome.getLength()) / (maxScreenSize * locationUnit);

            wholeGenomeScores = new ArrayList(1000);

            //for (String chr : genome.getLongChromosomeNames()) {
            String chr = "ALL";
            List<LocusScore> chrSegments = getSegments(heading, chr);
            if (chrSegments == null || chrSegments.size()<1) {
              chrSegments = getSegments(heading, "chrAll");  
            }
           
            if (chrSegments != null) {
                p("getWholeGenomeScores: computing coords for  "+chrSegments.size()+" for "+chr);
                int count = 0;
                for (LocusScore score : chrSegments) {
                    Segment seg = (Segment) score;
                    String segchr = seg.getChr();
                    if (genome == null) {
                        this.genome = IGV.getInstance().getGenomeManager().getCurrentGenome();
                    }
                    if (genome == null) {
                        //log.fatal("Got no genome");
                    }
                    String actualChr = genome.getChromosomeAlias(segchr);
                    
                    String alt = this.getAlt(actualChr);
                    if (alt == null) alt = segchr;
                    
                   if (count % 1000 ==0) {
                       
                //       p("checking seg on chr "+segchr+"/"+actualChr+", alt="+alt+" at "+seg.getStart());
                   }
                   if (!genome.getLongChromosomeNames().contains(actualChr)) {
                       actualChr = alt;
                   }
                   try {
                        int gStart = genome.getGenomeCoordinate(actualChr, seg.getStart());
                        int gEnd = genome.getGenomeCoordinate(actualChr, seg.getEnd());

                        if ((gEnd - gStart) >= minFeatureSize) {
                            Segment s = null;
                            if (score instanceof ReferenceSegment) {
                                //p("Got ref segment: at gstart "+gStart);
                                s = new ReferenceSegment(segchr, gStart, gStart, gEnd,
                                        gEnd, seg.getScore(), seg.getDescription(), seg.getAttributes());
                                refs++;
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
                        else p("Too small: "+(gEnd-gStart));
                   }
                   catch (Exception e) {
                       p("Got a problem: "+ErrorHandler.getString(e));
                   }
                    count++;
                }

           }
              //  offset += genome.getChromosome(chr).getLength();
           // }
            wholeGenomeScoresCache.put(heading, wholeGenomeScores);
        }
        else p("Got "+wholeGenomeScores.size()+" whole genome scores, "+refs+" ref");
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

    public Segment getSegForChr(String des, String chr, int start, int end, float value, HashMap<String, String> atts) {
        Segment seg = null;
        if (des != null && des.equalsIgnoreCase("REF")) {
            seg=  new ReferenceSegment(chr, start, start, end, end, value, des, atts);
           // p("Created REF segment: "+seg);
        }
        else if (des != null && des.equalsIgnoreCase("SUMMARY")) {
            seg=  new SummarySegment(chr, start, start, end, end, value, des, atts);
            p("Created SUMMARY segment: "+seg);
        }
        else seg=   new Segment(chr, start, start, end, end, value, des, atts);
        return seg;
    }

    public String getAlt(String chr) {
        // p("Adding seg to chr "+chr);
        String alt = null;
        if (chr.endsWith("23")) {
            alt = "chrX";
        } else if (chr.endsWith("24")) {
            alt = "chrY";
        } else if (chr.endsWith("25")) {
            alt = "chrM";
        } else if (chr.endsWith("X")) {
            alt = "chr23";
        } else if (chr.endsWith("Y")) {
            alt = "chr24";
        } else if (chr.endsWith("M")) {
            alt = "chr25";
        }
        return alt;
    }
}
