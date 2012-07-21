/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.broad.igv.PreferenceManager;
import org.iontorrent.sam2flowgram.flowalign.FlowOrder;
import org.iontorrent.sam2flowgram.flowalign.FlowSeq;
import org.iontorrent.sam2flowgram.flowalign.FlowgramAlignment;
import org.iontorrent.sam2flowgram.util.AlignUtil;

/**
 * compute the slots/flows for a list of subread ionograms
 *
 * @author Chantal Roth
 */
public class IonogramAlignment {

    private ArrayList<Ionogram> ionograms;
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IonogramAlignment.class);
    private String title;
    private int nrslots;
    private int nrionograms;
    private int maxemptyperlocation[];
    private int slotperlocation[];
    private int relativecenter;
    private int nrrelativelocations;
    private int chromosome_center_location;
    private boolean flowBased;
    /**
     * each row is a ionogram, each column is a slot, which may or may not map
     * to a flow value
     */
    private FlowValue[][] slotmatrix;
    private String emptyBasesInfo[];
    private String consensus;

   
    public IonogramAlignment(String consensus, ArrayList<Ionogram> ionograms, int maxemptyperlocation[], int nrbases_left_right, int chromosome_center_location) {
        this.ionograms = ionograms;
        this.consensus = consensus;
        this.chromosome_center_location = chromosome_center_location;
        nrionograms = ionograms.size();
        this.maxemptyperlocation = maxemptyperlocation;
        this.nrrelativelocations = nrbases_left_right * 2 + 1;
        this.relativecenter = nrbases_left_right;
        nrslots = computeSlots();
        emptyBasesInfo = new String[nrslots];
        slotmatrix = new FlowValue[nrionograms][nrslots];
        computeSlotsForGivenAlignment();
        flowBased = false;
        //  recomputeAlignment();
    }
    

    public FlowDistribution[] getFlowSignalDistribution(String locus, int slot) {
        // one for each base!
        if (ionograms == null || ionograms.isEmpty()) return null;
        ArrayList<TreeMap<Short, Integer>> alleletrees = new ArrayList<TreeMap<Short, Integer>>();

        int nrflows = 0;
        ArrayList<FlowDistribution> alleledist = new ArrayList<FlowDistribution>();
        String bases = "";
        // also store information on read and position
        boolean forward = false;
        boolean reverse = false;
        ArrayList<ArrayList<ReadInfo>> allelereadinfos = new ArrayList<ArrayList<ReadInfo>>();
        int curloc = 0;
        for (int i = 0; i < this.nrionograms; i++) {
            Ionogram iono = ionograms.get(i);
            forward = !iono.isReverse();
            reverse = !forward;

            FlowValue fv = this.slotmatrix[i][slot];
            
            if (fv != null) {
                curloc = fv.getBasecall_location();
                // also throw away positions near the end if we have the same base until the end if the user preference is set that way
                boolean hideFirstHPs = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.IONTORRENT_FLOWDIST_HIDE_FIRST_HP);
                int posinread = fv.getFlowPosition();
                if (hideFirstHPs) {
                    boolean hp = posinread == 0; // also add if last position, which we don't know as we need the read length in iono || posinread == iono.g                    
                    if (hp) {
                        continue;
                    }
                }
                nrflows++;
                TreeMap<Short, Integer> map = null;
                ArrayList<ReadInfo> readinfos = null;
                char base = fv.getBase();
                int whichbase = bases.indexOf(base);
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
                short flowSignal = (short) fv.getFlowvalue();
                ReadInfo readinfo = new ReadInfo(iono.getReadname(), fv.getFlowPosition(), flowSignal, fv.getBase());
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
            name += ", " + base + ", " + nrflows + " flows, slot "+slot;
            String info = locus + ", " + bases;
            
            FlowDistribution dist = new FlowDistribution(slot, nrflows, map, name, base, forward, reverse, info);
            dist.setReadInfos(allelereadinfos.get(which));
            alleledist.add(dist);
            which++;
        }
        
         FlowDistribution distributions[] = null;
         distributions = new FlowDistribution[alleledist.size()];
            for (int i = 0; i < alleledist.size(); i++) {
                distributions[i] = alleledist.get(i);
            }
        return distributions;
    }

    public void recomputeAlignment() {
        p("RECOMPUTING ALIGNMENT WITH FLOW SPACE");
        flowBased = true;
        ArrayList<FlowgramAlignment> aligns = recomputeAlignmentUsingFlowSpace();
        computeMaxEmpties(aligns);
        nrslots = computeSlots();
        emptyBasesInfo = new String[nrslots];
        
        slotmatrix = new FlowValue[nrionograms][nrslots];
        computeSlotsWithFlowAlignment(aligns);
//         pp("maxemptyperlocation");
//         for (int pos = 0; pos < getNrrelativelocations(); pos++) {
//             pp("pos="+ pos+"="+maxemptyperlocation[pos]+", slot="+this.slotperlocation[pos]);
//         }
    }

    private ArrayList<FlowgramAlignment> recomputeAlignmentUsingFlowSpace() {
        //  public FlowgramAlignment(FlowSeq flowQseq, byte tseq[],
        //                     FlowOrder qseqFlowOrder)

        ArrayList<FlowgramAlignment> aligns = new ArrayList<FlowgramAlignment>();
        byte[] tseq = new byte[consensus.length()];
        int r = 0;

        for (int i = 0; i < consensus.length(); i++) {
            char base = consensus.charAt(i);
            if (base != ' ' && base != '_') {
                tseq[r] = (byte) AlignUtil.baseCharToInt(base);
                r++;
            }
        }
        
        FlowOrder tseqFlowOrder = new FlowOrder(tseq, true);
        // compute new center location
        int newcenter = 0;
        char prev = ' ';
        for (int p = 0; p < this.relativecenter; p++) {
            char base = consensus.charAt(p);
            if (prev !=  base) newcenter++;
            prev = base;
        }
        p("Old center inc base call from left (base based): "+this.relativecenter+",  new center base call from left (flow based): "+newcenter);
        // for this.reverseCompliment().
        int targetlen = tseqFlowOrder.getLength();
        this.relativecenter = newcenter;
      //  this.nrrelativelocations = targetlen;
        // get flow value of previous center
                
    //    p("REFERENCE: " + consensus + "=" + Arrays.toString(tseq));
        for (int i = 0; i < ionograms.size(); i++) {
            // remove duplicate flows 
            Ionogram iono = ionograms.get(i);
            ArrayList<FlowValue> flows = iono.getFlowvalues();           
            iono.clear();
            for (FlowValue fv: flows) {
                if (!iono.isSameAsPrev(fv)) iono.addFlowValue(fv);
            }
            
            int len = iono.getFlowvalues().size();
            byte[] qorder = new byte[len];
            for (int f = 0; f < len; f++) {
                FlowValue fv = iono.getFlowvalues().get(f);
                qorder[f] = (byte) AlignUtil.baseCharToInt(fv.getBase());
            }
            
            FlowSeq flowQseq = new FlowSeq(iono.getFlowvalues());
            FlowOrder qseqFlowOrder = new FlowOrder(qorder);
            //  p("ref: "+Arrays.toString(tseq)+", signals="+Arrays.toString(signals)+", order="+Arrays.toString(qorder)+" :"+qseqFlowOrder.toString());
            FlowgramAlignment falign = null;
            try {
                falign = new FlowgramAlignment(flowQseq, tseq, qseqFlowOrder, true, true, 1);
                //      System.out.println(iono.getReadname()+":"+"\n" + falign.getAlignmentString(true));                
                //    p("aln="+Arrays.toString(falign.aln));                

            } catch (Exception ex) {
                Logger.getLogger(IonogramAlignment.class.getName()).log(Level.SEVERE, null, ex);
            }
            aligns.add(falign);
        }
        return aligns;
    }

    private void pp(String s) {
        System.out.println(s);
    }

    private void computeSlotsWithFlowAlignment(ArrayList<FlowgramAlignment> aligns) {
        // compute nr of slots:
        // for each actual incorporation, get the maximum number of empties.
        // the sum of each incorporation plus empties is the nr of slots
        // before we can creat an array, we use an array lost for each incorporation event
    //    p("Computing alignment: got " + this.nrslots + " slots and " + this.nrionograms + " ionograms");
        for (int i = 0; i < getNrionograms(); i++) {
            //  Ionogram iono = ionograms.get(i);
            FlowgramAlignment align = aligns.get(i);
            Ionogram iono = ionograms.get(i);
            int nrempty = 0;
            if (align != null) {
                int lastincalignpos = 0;
          //      pp(iono.getReadname() + ":" + "\n" + align.getAlignmentString(true));
          //      pp(align.showHelperArrays());
                for (int qpos = 0; qpos < iono.getFlowvalues().size(); qpos++) {
                    int alignpos = align.getAlignPosForQpos(qpos);
                    int tflowpos = align.getTargetFlowposForAlignPos(alignpos);
                    int tbasepos = align.getTargetBaseposForTargetFlowPos(tflowpos);
                    // if (tbasepos == 0) tbasepos = prevtbasepos;

                    //int relative = getRelativeLocation(loc);
                    int startslot = slotperlocation[tbasepos];
                    FlowValue fv = align.getQueryFlowValue(qpos);
                   
                    FlowValue tv = align.getTargetFlowValue(tflowpos);

                    if (fv.isEmpty()) {
                        // nrempty++;
                        nrempty = alignpos - lastincalignpos;
                    } else {
                        //incorporation, starting empty from scratch
                        nrempty = 0;
                        lastincalignpos = alignpos;
                    }
                    int slot = startslot + nrempty;

                //    pp("q " + qpos + " " + fv.getBase() + " " + (fv.isEmpty() ? "e" : "i") + " -> a " + alignpos + " -> t " + tflowpos + tv.getBase() + "-> tpos " + tbasepos + consensus.charAt(tbasepos) + "-> slot " + slot);

                    if (slot < slotmatrix[i].length) {
                        slotmatrix[i][slot] = fv;
                    }
                }
            }
            iono.setSlotrow(slotmatrix[i]);

        }
    }

    private void computeMaxEmpties(ArrayList<FlowgramAlignment> aligns) {
        // first we have to compute the size of the msa, the space between incorporations
        maxemptyperlocation = new int[this.getNrrelativelocations()];

        for (int pos = 0; pos < getNrrelativelocations(); pos++) {
            int maxempty = 0;

            for (int i = 0; i < this.nrionograms; i++) {
                FlowgramAlignment al = aligns.get(i);
                int invalid = al.getLength();
                if (al != null) {
                    int nextal = al.getAlignPosForTBasepos(pos + 1);
                    int preval = al.getAlignPosForTBasepos(pos);
                    int empties = 0;
                    if (nextal >= invalid) {
                        empties = 0;
                    } else {
                        empties = Math.max(0, nextal - preval - 1);
                    }
//                    if (pos == 8) {
//                        pp("pos "+pos+"-> nextal="+nextal+", preval="+preval+", invalid="+invalid+", nextval-preval-1="+Math.max(0, nextal - preval - 1)+", empties="+empties+", maxempties="+maxempty);
//                    }
                    // nr empties
                    if (empties > maxempty) {
                        maxempty = empties;
                    }
                }
            }
         //   p("Max empty for targetbase pos  " + pos + " = " + consensus.charAt(pos) + "=" + maxempty);
            maxemptyperlocation[pos] = maxempty;
        }
       

    }

    private int computeSlots() {
        int slots = 0;
        slotperlocation = new int[maxemptyperlocation.length];
        for (int relativeloc = 0; relativeloc < getNrrelativelocations(); relativeloc++) {
            // plus one as we also have to count the actual incorporation :-)
            slotperlocation[relativeloc] = slots;
            slots += maxemptyperlocation[relativeloc] + 1;
        }
        p("Computing slots: " + slots);
        return slots;
    }

    public String getLocus() {
        if (ionograms != null && ionograms.size() > 0) {
            return ionograms.get(0).getLocusinfo();
        } else {
            return "Unknown";
        }
    }

    public int getCenterSlot() {
        return slotperlocation[this.getRelativecenter()];
    }

    public String getAlignmentBase(int slot) {
        char base= ' ';
        int count = 0;
        double value = 0;
        for (int i = 0; i < this.nrionograms; i++) {
            FlowValue v = slotmatrix[i][slot];
            if (v != null && v.getAlignmentBase() != ' ' && v.getBase() == v.getAlignmentBase()) {                
                base= v.getAlignmentBase();
                count++;
                value += v.getFlowvalue();
            }
        }
        int nrbases = (int) Math.max(1, Math.round(value/(double)count/100.0));
        String res = "";
        if (!flowBased) nrbases = 1;
        for (int i = 0; i < nrbases; i++) {
            res += base;
        }
        return res;
    }

    public String getEmptyBasesInfo(int slot) {
        if (emptyBasesInfo[slot] == null) {
            getEmptyBases(slot);
        }
        return emptyBasesInfo[slot];
    }

    public String getEmptyBases(int slot) {
        double counts[] = new double[4];
        String GATC = "GATC";
        int total = 0;
        for (int i = 0; i < this.nrionograms; i++) {
            FlowValue v = slotmatrix[i][slot];
            if (v != null && v.getBase() != ' ') {
                int which = GATC.indexOf(v.getBase());
                if (which >= 0) {
                    counts[which]++;
                    total++;
                } else {
                    p("Strange base in slot " + slot + " for iono " + i + ": " + v.getBase());
                }
            }
        }
        //   p("Found "+total+" flows in slot "+slot);
        String bases = "";
        emptyBasesInfo[slot] = "";
        if (total > 0) {
            String info = "";
            int maxpos = -1;
            double max = 0;
            int secondpos = -1;
            double second = 0;

            for (int i = 0; i < 4; i++) {
                // convert to percentage
                counts[i] = counts[i] * 100.0 / total;
                if (counts[i] > max) {
                    max = counts[i];
                    maxpos = i;
                }
                if (counts[i] > second && counts[i] != max) {
                    second = counts[i];
                    secondpos = i;
                }
            }
            if (maxpos >= 0) {
                char base = GATC.charAt(maxpos);
                bases = "" + base;
                info = base + "(" + counts[maxpos] + "%) ";
            }
            if (secondpos >= 0 && secondpos != maxpos) {
                char base = Character.toLowerCase(GATC.charAt(secondpos));
                bases += base;
                info += base + "(" + counts[secondpos] + "%) ";
            }
            //    p("bases are: "+bases+", top: "+maxpos);
            emptyBasesInfo[slot] = info;
        }

        return bases;
    }

    @Override
    public String toString() {
        if (this.nrionograms <= 0) {
            return "got no alignment";
        }
        Ionogram iono = ionograms.get(0);
        String nl = "\n";
        String res = "Ionogram alignent at " + iono.getLocusinfo() + nl + nl;
        // I know using String + is usually not recommended
        // but, this method does not have to be fast or efficient, so for readability
        // + is still much nicer than all those appends :-)

        res += "readname, ";
        for (int s = 0; s < this.nrslots; s++) {
            res = res + "slot " + s + ", ";
        }
        res += nl;
        for (int i = 0; i < this.nrionograms; i++) {
            res += ionograms.get(i).getReadname() + ", ";
            for (int s = 0; s < this.nrslots; s++) {
                FlowValue v = slotmatrix[i][s];
                if (v == null) {
                    res += ",";
                } else {
                    res = res + v.getFlowvalue() + ", ";
                }
            }
            res += nl;
        }
        res += nl;
        return res;
    }

    private void computeSlotsForGivenAlignment() {
        // compute nr of slots:
        // for each actual incorporation, get the maximum number of empties.
        // the sum of each incorporation plus empties is the nr of slots
        // before we can creat an array, we use an array lost for each incorporation event
       // p("Computing alignment: got " + this.nrslots + " slots and " + this.nrionograms + " ionograms");
        for (int i = 0; i < getNrionograms(); i++) {
            Ionogram iono = ionograms.get(i);
            int nrempty = 0;
            for (FlowValue fv : iono.getFlowvalues()) {
                int relative = fv.getBasecall_location();
                //int relative = getRelativeLocation(loc);
                int startslot = slotperlocation[relative];

                if (fv.isEmpty()) {
                    nrempty++;
                } else {
                    //incorporation, starting empty from scratch
                    nrempty = 0;
                }
                int slot = startslot + nrempty;
                slotmatrix[i][slot] = fv;
            }
            iono.setSlotrow(slotmatrix[i]);

        }
    }

    private void p(String msg) {
        log.info(msg);
    }

    private void err(String msg) {
        log.error(msg);
    }

    /**
     * @return the nrslots
     */
    public int getNrslots() {
        return nrslots;
    }

    /**
     * @return the nrionograms
     */
    public int getNrionograms() {
        return nrionograms;
    }

    /**
     * @return the relativecenter
     */
    public int getRelativecenter() {
        return relativecenter;
    }

    /**
     * @return the nrrelativelocations
     */
    public int getNrrelativelocations() {
        return nrrelativelocations;
    }

    /**
     * @return the chromosome_center_location
     */
    public int getChromosome_center_location() {
        return chromosome_center_location;
    }

    public ArrayList<Ionogram> getIonograms() {
        return this.ionograms;
    }

    public int getMaxValue() {
        int max = 0;
        for (Ionogram iono : ionograms) {
            int v = iono.getMaxValue();
            if (v > max) {
                max = v;
            }
        }
        return max;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
