/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FlowSeq;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.prefs.IonTorrentPreferencesManager;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Locus;
import org.broad.igv.sam.*;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.iontorrent.seq.DNASequence;

/**
 *
 * @author Chantal Roth
 */
public class ConfidenceDistribution {

    private ArrayList<FlowValue> flowvalues;
    private String information;
    private String name;
    private char base;
    private int nrflows;
    private String chromosome;
    private boolean hasOldData;
    /**
     * the chromosome location
     */
    private int location;
    private ArrayList<ReadInfo> readinfos;
    private boolean forward;
    private boolean reverse;
    private static ExperimentContext exp;
    private double minx = 50;
    private double maxx = 150;

    public ConfidenceDistribution(int location, int nrflows, ArrayList<FlowValue> flowvalues, String name, char base, boolean forward, boolean reverse, String information) {
        this.flowvalues = flowvalues;
        this.information = information;
        this.name = name;
        this.nrflows = nrflows;
        this.location = location;
        this.forward = forward;
        this.reverse = reverse;
        this.base = base;
    }

    public static ExperimentContext getExperimentContext() {
        return exp;
    }

    public static void computeConfidence(SamAlignment sam) {
        if (exp == null) {
            exp = new ExperimentContext();
        }
        p("---- computeConfidence for alignment " + sam.getReadName() + " -----");
        FlowSeq flowseq = null;
        if (!sam.hasComputedConfidence()) {
            exp.setFlowOrder(sam.getFlowOrderNoKey());
            exp.setNrFlows(sam.getRawFlowSignals().length);
            exp.setModelParameters(sam.getCF(), sam.getIE(), sam.getDR());
           // String key = sam.getKeySequence();
            String seq = sam.getReadSequence();
            if (sam.isNegativeStrand()) {
                DNASequence dna = new DNASequence(seq);
                DNASequence rev = dna.reverse().complement();
                seq = rev.toSequenceString();
            }
            flowseq = exp.computePredictedSignal( seq);
            sam.setFlowseq(flowseq);

            float[] signals = sam.getRawFlowSignals();
            for (int f = sam.getFlowSignalsStart(); f < flowseq.getLength(); f++) {
                FlowValue fv = flowseq.getFlow(f);
                int flow = fv.getFlowPosition();
                fv.setRawFlowvalue((int) signals[flow - sam.getFlowSignalsStart()]);
            }

            exp.computeConfidence(flowseq.getFlowValues());
        } else {
            flowseq = sam.getFlowseq();
            if (!flowseq.hasPredictedValues()) {
                FlowSignalContextBuilder.computeFlowSeqWithPrediction(flowseq, sam);
            }
        }
        AlignmentBlock[] blocks = sam.getAlignmentBlocks();
        for (int b = 0; b < blocks.length; b++) {
            AlignmentBlockFS block = (AlignmentBlockFS) blocks[b];
            FlowValue[][][] values = block.getFlowSignalContext().getFlowvalues();

            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    for (int j = 0; j < values[i].length; j++) {
                        if (values[i][j] != null) {
                            for (int k = 0; k < values[i][j].length; k++) {
                                FlowValue fvBlock = values[i][j][k];
                                if (fvBlock != null) {
                                    int flow = fvBlock.getFlowPosition();
                                    if (flow < flowseq.getLength()) {
                                        FlowValue fvPred = flowseq.getFlow(flow);
                                        fvBlock.setConfidence(fvPred.getConfidence());
                                        fvBlock.setPredictedValue(fvPred.getPredictedValue());
                                    }
                                }
                            }
                        }
                    }
                }//  
            }
        }
    }

    public static ArrayList<ConfidenceDistribution> extractDistributions(AlignmentDataManager dataManager, ReferenceFrame frame, int location, boolean forward, boolean reverse) {
        ArrayList< ArrayList<FlowValue>> allelelists = new ArrayList< ArrayList<FlowValue>>();
        exp = new ExperimentContext();
        PreferenceManager prefs = PreferenceManager.getInstance();
        String expinfo = prefs.get(IonTorrentPreferencesManager.BAM_FILE);
        exp.setExperimentInfo(expinfo);

        boolean hasOld = false;
        int nrflows = 0;
        ArrayList<ConfidenceDistribution> alleledist = new ArrayList<ConfidenceDistribution>();
        String bases = "";
        // also store information on read and position

        p("---- extractDistributions at " + location + " -----");
        ArrayList<ArrayList<ReadInfo>> allelereadinfos = new ArrayList<ArrayList<ReadInfo>>();
        for (AlignmentInterval interval : dataManager.getLoadedIntervals()) {
            Iterator<Alignment> alignmentIterator = interval.getAlignmentIterator();
            while (alignmentIterator.hasNext()) {
                Alignment alignment = alignmentIterator.next();
                if ((alignment.isNegativeStrand() && !reverse) || (!alignment.isNegativeStrand() && !forward)) {
                    continue;
                }
                if (!alignment.contains(location)) {
                    continue;
                }
                // we don't want the beginning or the end of the alignment! HP might might give misleading results
                if (alignment.getAlignmentStart() == location || alignment.getAlignmentEnd() == location) {
                    //log.info(location + " for read " + alignment.getReadName() + " is at an end, not taking it");
                    continue;
                }
                // also throw away positions near the end if we have the same base until the end if the user preference is set that way
                boolean hideFirstHPs = PreferenceManager.getInstance().getAsBoolean(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_HIDE_FIRST_HP);

                if (hideFirstHPs) {
                    char baseatpos = (char) alignment.getBase(location);
                    boolean hp = true;
                    for (int pos = alignment.getAlignmentStart(); pos < location; pos++) {
                        if ((char) alignment.getBase(pos) != baseatpos) {
                            hp = false;
                            break;
                        }
                    }
                    if (hp) {
                        //  log.info("Got all same bases " + baseatpos + " for read " + alignment.getReadName() + " at START.");
                        continue;
                    }
                    hp = true;
                    for (int pos = location + 1; pos < alignment.getAlignmentEnd(); pos++) {
                        if ((char) alignment.getBase(pos) != baseatpos) {
                            hp = false;
                            break;
                        }
                    }
                    if (hp) {
                        //    log.info("Got all same bases " + baseatpos + " for read " + alignment.getReadName() + " at END");
                        continue;
                    }
                }
                SamAlignment sam = (SamAlignment) alignment;
                FlowSeq flowseq = null;
                if (!sam.hasComputedConfidence()) {

                    exp.setFlowOrder(sam.getFlowOrderNoKey());
                    exp.setNrFlows(sam.getRawFlowSignals().length);
                    exp.setModelParameters(sam.getCF(), sam.getIE(), sam.getDR());
                   // String key = sam.getKeySequence();
                    String seq = sam.getReadSequence();
                    if (sam.isNegativeStrand()) {
                        DNASequence dna = new DNASequence(seq);
                        DNASequence rev = dna.reverse().complement();
                        seq = rev.toSequenceString();
                    }

                    flowseq = exp.computePredictedSignal(seq);
                    sam.setFlowseq(flowseq);

                    float[] signals = sam.getRawFlowSignals();
                    hasOld = sam.getOldSignals() != null;
                    for (int f = sam.getFlowSignalsStart(); f < flowseq.getLength(); f++) {
                        FlowValue fv = flowseq.getFlow(f);
                        int flow = fv.getFlowPosition();
                        if (flow != f) {
                            p("flow nr out of order:" + f + "/" + flow);
                        }
                        int pos = flow - sam.getFlowSignalsStart();
                        if (pos < signals.length) {
                            fv.setRawFlowvalue((int) signals[pos]);
                        }
                    }

                    // p("Computing conf for read "+sam.getReadName()); 
                    exp.computeConfidence(flowseq.getFlowValues());
                } else {
                    p("We alredy have the confidence computed");
                    flowseq = sam.getFlowseq();
                }


                AlignmentBlock[] blocks = alignment.getAlignmentBlocks();
                for (int i = 0; i < blocks.length; i++) {
                    AlignmentBlock block = blocks[i];
                    if (block.hasFlowSignals()) {
                        int posinblock = (int) location - block.getStart();
                        if (!block.contains((int) location) || !block.hasFlowSignals()) {
                            continue;
                        }

                        int flownr = block.getFlowSignalSubContext(posinblock).getCurrentValue().getFlowPosition();
                        nrflows++;
                        FlowSignalSubContext sub = block.getFlowSignalSubContext(posinblock);
                        if (sub != null && sub.hasFlowValues()) {
                            FlowValue fvBlock = sub.getCurrentValue();
                            short rawflowSignal = (short) fvBlock.getRawFlowvalue();

                            char base = (char) block.getBase(posinblock);

                            int whichbase = bases.indexOf(base);
                            ArrayList<FlowValue> flowlist = null;
                            ArrayList<ReadInfo> readinfos = null;
                            if (whichbase < 0) {
                                bases += base;
                                flowlist = new ArrayList<FlowValue>();
                                allelelists.add(flowlist);
                                readinfos = new ArrayList<ReadInfo>();
                                allelereadinfos.add(readinfos);
                            } else {
                                flowlist = allelelists.get(whichbase);
                                readinfos = allelereadinfos.get(whichbase);
                            }
                            FlowValue fvPred = flowseq.getFlow(flownr);
                            int hp = fvPred.getHpLen();
                            //  public FlowValue(int flowvalue, int flowposition, char base, int location_in_sequence, boolean empty, char alignmentbase) {
                            FlowValue fv = new FlowValue(hp, rawflowSignal, flownr, base, location, false, base);

                            fvPred.setOldvalue(fvBlock.getOldvalue());
                            fvBlock.setConfidence(fvPred.getConfidence());
                            fvBlock.setPredictedValue(fvPred.getPredictedValue());
                            fvBlock.setNext(fvPred.getNext());
                            fvBlock.setPrev(fvPred.getPrev());
                            fvBlock.setOldvalue(fvPred.getOldvalue());

                            fv.setConfidence(fvPred.getConfidence());
                            fv.setPredictedValue(fvPred.getPredictedValue());
                            fv.setOldvalue(fvBlock.getOldvalue());
                            fv.setPrev(fvPred.getPrev());
                            fv.setNext(fvPred.getNext());
                            ReadInfo readinfo = new ReadInfo(alignment.getReadName(), fv);
                            readinfos.add(readinfo);
                            flowlist.add(fv);
                        } else {
                            p("Got no subcontext for block " + block + " at posinblock=" + posinblock + ":" + sub);
                        }
                    } else {
                        p("Found no flow signals for block: " + block);
                    }
                }
            }
        }

        String locus = Locus.getFormattedLocusString(frame.getChrName(), (int) location, (int) location);

        int which = 0;
        for (ArrayList<FlowValue> flowlist : allelelists) {
            String name = "";
            if (forward && reverse) {
                name += "both strand";
            } else if (forward) {
                name += "forward strand";
            } else {
                name += "reverse strand";
            }
            char base = bases.charAt(which);
            name += ", " + base + ", " + nrflows + " flows";
            String info = locus + ", " + bases;

            ConfidenceDistribution dist = new ConfidenceDistribution(location, nrflows, flowlist, name, base, forward, reverse, info);
            dist.setHasOldData(hasOld);
            dist.setChromosome(frame.getChrName());
            dist.setReadInfos(allelereadinfos.get(which));
            alleledist.add(dist);
            which++;
        }
        return alleledist;
    }

    private static void p(String s) {
        Logger.getLogger("ScoreDistribution").info(s);
    }

    public int getNrFlows() {
        return nrflows;
    }

    public String getName() {
        return name;
    }

    public String toCsv(int binsize) {
        String nl = "\n";
        StringBuilder csv = new StringBuilder();

        int[] bins = getBinnedData(binsize, 0, false);
        csv = csv.append(getInformation());
        csv = csv.append(nl).append("confidence, count\n");
        for (int b = 0; b < bins.length; b++) {
            csv = csv.append(b * binsize - minx).append(",").append(bins[b]).append(nl);
        }
        csv = csv.append(nl);
        return csv.toString();
    }

    public String toJson() {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        buf.append("    \"comment\" : \"HP lenght, confidence\"  ,\n");
        for (FlowValue fv : flowvalues) {
            buf.append("    \"").append(fv.getHpLen()).append("\" : \"").append((int) fv.getConfidence()).append("\",\n");
        }
        buf.append("}\n");
        return buf.toString();
    }

    public ArrayList<ReadInfo> getReadInfos() {
        return readinfos;
    }

    public String getReadInfoString() {
        String nl = "\n";
        StringBuilder csv = new StringBuilder();
        csv = csv.append(getInformation());
        csv = csv.append(nl).append(ReadInfo.getHeader()).append(nl);
        for (ReadInfo ri : readinfos) {
            csv = csv.append(ri.toCsv()).append(nl);
        }
        csv = csv.append(nl);
        return csv.toString();
    }

    public String getReadNames() {
        StringBuilder names = new StringBuilder();
        for (ReadInfo ri : readinfos) {
            names = names.append(ri.getReadName()).append("_");
        }

        return names.toString();
    }

    public int getMaxCount(int binsize, int hplen, boolean old) {
        int bins[] = getBinnedData(binsize, hplen, old);
        int maxy = 0;
        for (int i = 0; i < bins.length; i++) {
            int count = bins[i];
            if (maxy < count) {
                maxy = count;
            }
        }
        return maxy;
    }

    public ArrayList<FlowValue> getFlowValues() {
        return this.flowvalues;
    }

    public int[] getBinnedData(int binsize, int hplen, boolean old) {

        if (old) {
            minx = 0;
            maxx = this.computeMaxValue();
        }
        p("getBinnedData: minx=" + minx + ", maxx=" + maxx);
        int nrbins = (int) ((maxx - minx) / binsize) + 1;
        int bins[] = new int[nrbins];
        for (FlowValue fv : flowvalues) {
            if (fv.getHpLen() == hplen || hplen <= 0) {
                double conf = fv.getConfidence();
                if (old) {
                    conf = fv.getOldvalue();
                }
                int bin = (int) ((conf - minx) / binsize);
                if (bin >= 0 && bin < bins.length) {
                    bins[bin] += 1;
                }
            }
        }
        int maxy = 0;
        for (int i = 0; i < bins.length; i++) {
            int count = bins[i];
            if (maxy < count) {
                maxy = count;
            }
        }

        return bins;
    }

    /**
     * @return the name
     */
    public String getInformation() {
        return information;
    }

    /**
     * @return the location
     */
    public int getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(int location) {
        this.location = location;
    }

    public double computeMaxConf() {
        maxx = 0;
        for (FlowValue fv : flowvalues) {
            double conf = fv.getConfidence();
            if (conf > maxx) {
                maxx = conf;
            }
        }
        return maxx;
    }

    public double computeMaxValue() {
        double maxx = 0;
        for (FlowValue fv : flowvalues) {
            double val = fv.getOldvalue();
            if (val > maxx) {
                maxx = val;
            }
        }
        return maxx + 50;
    }

    public double getMaxX() {
        return maxx;
    }

    public double computeMinValue() {
        double minx = Integer.MAX_VALUE;
        for (FlowValue fv : flowvalues) {
            double val = fv.getOldvalue();
            if (val < minx && val > 0) {
                minx = val;
            }
        }
        return minx;
    }

    public double computeMinX() {
        minx = Integer.MAX_VALUE;
        for (FlowValue fv : flowvalues) {
            double conf = fv.getConfidence();
            if (conf < minx && conf > 0) {
                minx = conf;
            }
        }
        return minx;
    }

    public double getMinX() {
        return minx;
    }

    public void setReadInfos(ArrayList<ReadInfo> readinfos) {
        this.readinfos = readinfos;
    }

    public char getBase() {
        return base;
    }

    public boolean isForward() {
        return forward;
    }

    public boolean isReverse() {
        return reverse;
    }

    public String getChromosome() {
        return chromosome;
    }

    /**
     * @param chromosome the chromosome to set
     */
    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * @return the hasOldData
     */
    public boolean isHasOldData() {
        return hasOldData;
    }

    /**
     * @param hasOldData the hasOldData to set
     */
    public void setHasOldData(boolean hasOldData) {
        this.hasOldData = hasOldData;
    }
}
