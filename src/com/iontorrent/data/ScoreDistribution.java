/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import com.iontorrent.rawdataaccess.FlowValue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Locus;
import org.broad.igv.sam.Alignment;
import org.broad.igv.sam.AlignmentBlock;
import org.broad.igv.sam.AlignmentDataManager;
import org.broad.igv.sam.AlignmentInterval;
import org.broad.igv.ui.panel.ReferenceFrame;

/**
 *
 * @author Chantal Roth
 */
public class ScoreDistribution {

    private TreeMap<Short, Integer> map;
    private String information;
    private String name;
    private char base;
    private int nrflows;
    private String chromosome;
    /**
     * the chromosome location
     */
    private int location;
    private ArrayList<ReadInfo> readinfos;
    private boolean forward;
    private boolean reverse;

    public ScoreDistribution(int location, int nrflows, TreeMap<Short, Integer> map, String name, char base, boolean forward, boolean reverse, String information) {
        this.map = map;
        this.information = information;
        this.name = name;
        this.nrflows = nrflows;
        this.location = location;
        this.forward = forward;
        this.reverse = reverse;
        this.base = base;
    }

    public static ArrayList<ScoreDistribution> extractFlowDistributions(AlignmentDataManager dataManager, ReferenceFrame frame, int location, boolean forward, boolean reverse) {
        ArrayList<TreeMap<Short, Integer>> alleletrees = new ArrayList<TreeMap<Short, Integer>>();

        int nrflows = 0;
        ArrayList<ScoreDistribution> alleledist = new ArrayList<ScoreDistribution>();
        String bases = "";
        // also store information on read and position


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
                boolean hideFirstHPs = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.IONTORRENT_FLOWDIST_HIDE_FIRST_HP);

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
                AlignmentBlock[] blocks = alignment.getAlignmentBlocks();
                for (int i = 0; i < blocks.length; i++) {
                    AlignmentBlock block = blocks[i];
                    int posinblock = (int) location - block.getStart();
                    if (!block.contains((int) location) || !block.hasFlowSignals()) {
                        continue;
                    }

                    int flownr = block.getFlowSignalSubContext(posinblock).getFlowOrderIndex();
                    nrflows++;
                    short flowSignal = block.getFlowSignalSubContext(posinblock).getCurrentSignal();

                    char base = (char) block.getBase(posinblock);

                    int whichbase = bases.indexOf(base);
                    TreeMap<Short, Integer> map = null;
                    ArrayList<ReadInfo> readinfos = null;
                    if (whichbase < 0) {
                        bases += base;
                        map = new TreeMap<Short, Integer>();
                        alleletrees.add(map);
                        readinfos = new ArrayList<ReadInfo>();
                        allelereadinfos.add(readinfos);
                    } else {
                        map = alleletrees.get(whichbase);
                        readinfos = allelereadinfos.get(whichbase);
                    }
                    //  public FlowValue(int flowvalue, int flowposition, char base, int location_in_sequence, boolean empty, char alignmentbase) {
                    FlowValue fv = new FlowValue(flowSignal, flownr, base, location, false, base );
                    ReadInfo readinfo = new ReadInfo(alignment.getReadName(), fv);
                    readinfos.add(readinfo);
                    if (map.containsKey(flowSignal)) {
                        // increment
                        map.put(flowSignal, map.get(flowSignal) + 1);
                    } else {
                        // insert
                        map.put(flowSignal, 1);
                    }
                }
            }
        }

        String locus = Locus.getFormattedLocusString(frame.getChrName(), (int) location, (int) location);

        int which = 0;
        for (TreeMap<Short, Integer> map : alleletrees) {
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

            ScoreDistribution dist = new ScoreDistribution(location, nrflows, map, name, base, forward, reverse, info);
            dist.setChromosome(frame.getChrName());
            dist.setReadInfos(allelereadinfos.get(which));
            alleledist.add(dist);
            which++;
        }
        return alleledist;
    }
    public int getNrFlows() {
        return nrflows;
    }

    public String getName() {
        return name;
    }

    public String toCsv(int binsize) {
        int[] bins = getBinnedData(binsize);
        String nl = "\n";
        StringBuilder csv = new StringBuilder();
        csv = csv.append(getInformation());
        csv = csv.append(nl).append("flow value, count\n");
        for (int b = 0; b < bins.length; b++) {
            csv = csv.append(b * binsize).append(",").append(bins[b]).append(nl);
        }
        csv = csv.append(nl);
        return csv.toString();
    }

    public String toJson() {
        StringBuilder buf = new StringBuilder();
        buf.append("{\n");
        for (Short key : map.keySet()) {
            buf.append("    \"").append(key).append("\" : \"").append(map.get(key)).append("\"\n");
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

    public int getMaxCount(int binsize) {
        int bins[] = getBinnedData(binsize);
        int maxy = 0;
        for (int i = 0; i < bins.length; i++) {
            int count = bins[i];
            if (maxy < count) {
                maxy = count;
            }
        }
        return maxy;
    }
    public int[] getBinnedData(int binsize) {
        int maxx = 0;
        for (Short x : map.keySet()) {
            if (x > maxx) {
                maxx = x;
            }
        }
        int nrbins = maxx / binsize + 1;
        int bins[] = new int[nrbins];
        for (Short x : map.keySet()) {
            int y = map.get(x);
            bins[x / binsize] += y;
        }
        return bins;
    }

    /**
     * @return the map
     */
    public TreeMap<Short, Integer> getMap() {
        return map;
    }

    /**
     * @param map the map to set
     */
    public void setMap(TreeMap<Short, Integer> map) {
        this.map = map;
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

    public int getMaxX() {
        int maxx = 0;
        for (Short x : map.keySet()) {
            if (x > maxx) {
                maxx = x;
            }
        }
        return maxx;
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
}
