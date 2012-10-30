/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.wellmodel.WellCoordinate;
import java.util.ArrayList;
import org.iontorrent.sam2flowgram.flowalign.FlowSeq;

/**
 * The inogram of a part of an alignment, including the empties
 * @author Chantal Roth
 */
public class Ionogram {
    
    private String readname;
    private boolean reverse;
    
    private String locusinfo;
    private String chromosome;
    
    private int chromosome_center_location;
    
    private int nrbases_left_right;
    
    private ArrayList<FlowValue> flowvalues;
       
    private String floworder;
    
    private FlowValue[] slotrow;
    private boolean selected[];
    public Ionogram(String readname, int chromosome_center_location, boolean reverse) {
        this.readname = readname;
        this.chromosome_center_location = chromosome_center_location;        
        this.reverse = reverse;
        flowvalues = new ArrayList<FlowValue>();
    }
    
    public void clear() {
        flowvalues = new ArrayList<FlowValue>();
    }
    public boolean isReverse(){
        return reverse;
    }
    public boolean isSameAsPrev(FlowValue flowvalue) {
        if (flowvalues == null || flowvalues.isEmpty()) return false;
        FlowValue last = flowvalues.get(flowvalues.size()-1);
        return (last.getBase() == flowvalue.getBase());
    }
    public void addFlowValue(FlowValue flowvalue) {
        getFlowvalues().add(flowvalue);
        // add to maps
    }
    public void setFlowValues(ArrayList<FlowValue> flowvalues) {
        this.flowvalues = flowvalues;
    }
    public void setFlowSequence(FlowSeq seq) {
        this.flowvalues = seq.getFlowValues();
    }
    @Override
    public String toString() {
        String s = readname+"@ "+getLocusinfo()+":\n";
        s += FlowValue.getHeader()+"\n";
        for (FlowValue fv: getFlowvalues()) {
            s += fv.toString()+"\n";
        }
        return s;
    }
   
    public String toShortString() {
        String s = readname+"@ "+getLocusinfo()+": ";        
        for (FlowValue fv: getFlowvalues()) {
            s += fv.getBase()+" ("+fv.getFlowvalue()+") "+" ";
        }
        return s;
    }
     public String toHtml() {
        String nl = "<br>";
        String s = readname+"@ "+getLocusinfo()+":"+nl;
        s += FlowValue.getHeader()+nl;
        for (FlowValue fv: getFlowvalues()) {
            s += fv.toString()+nl;
        }
        return s;
    }
    
    /**
     * @return the chromosome_center_location
     */
    public int getChromosome_center_location() {
        return chromosome_center_location;
    }

    /**
     * @param chromosome_center_location the chromosome_center_location to set
     */
    public void setChromosome_center_location(int chromosome_center_location) {
        this.chromosome_center_location = chromosome_center_location;
    }

    /**
     * @return the nrbases_left_right
     */
    public int getNrbases_left_right() {
        return nrbases_left_right;
    }

    /**
     * @param nrbases_left_right the nrbases_left_right to set
     */
    public void setNrbases_left_right(int nrbases_left_right) {
        this.nrbases_left_right = nrbases_left_right;
    }
    /**
     * @return the floworder
     */
    public String getFloworder() {
        return floworder;
    }

    /**
     * @param floworder the floworder to set
     */
    public void setFloworder(String floworder) {
        this.floworder = floworder;
    }

    /**
     * @return the readname
     */
    public String getReadname() {
        return readname;
    }

    public WellCoordinate getCoord() {
         // Y9VO3:844:1030_Y9VO3:38:643
            int col = readname.indexOf(":");
            readname = readname.substring(col + 1);
            readname = readname.replace(":", "_");
            int ul = readname.indexOf("_");
            int x = Integer.parseInt(readname.substring(0, ul));
            int y = Integer.parseInt(readname.substring(ul+1));
            return new WellCoordinate(x, y);
    }
    /**
     * @param readname the readname to set
     */
    public void setReadname(String readname) {
        this.readname = readname;
    }

    /**
     * @return the locusinfo
     */
    public String getLocusinfo() {
        return locusinfo;
    }

    /**
     * @param locusinfo the locusinfo to set
     */
    public void setLocusinfo(String locusinfo) {
        this.locusinfo = locusinfo;
    }

    /**
     * @return the flowvalues
     */
    public ArrayList<FlowValue> getFlowvalues() {
        return flowvalues;
    }

    /**
     * @param flowvalues the flowvalues to set
     */
    public void setFlowvalues(ArrayList<FlowValue> flowvalues) {
        this.flowvalues = flowvalues;
    }

    /**
     * @return the slotrow
     */
    public FlowValue[] getSlotrow() {
        return slotrow;
    }

    /**
     * @param slotrow the slotrow to set
     */
    public void setSlotrow(FlowValue[] slotrow) {
        this.slotrow = slotrow;
        selected = new boolean[slotrow.length];
    }

    public boolean isSelected(int slot) {
        return selected[slot];
    }
    public void toggleSelect(int slot) {
        selected[slot] = !selected[slot];
    }
    
    public int getMaxValue() {
        int max = 0;
        for (FlowValue f: flowvalues) {
            if (f.getFlowvalue() > max) max = f.getFlowvalue();
        }
        return max;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * @return the chromosome
     */
    public String getChromosome() {
        return chromosome;
    }
    
    
}
