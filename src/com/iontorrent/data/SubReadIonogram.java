/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import java.util.ArrayList;

/**
 * The inogram of a part of an alignment, including the empties
 * @author Chantal Roth
 */
public class SubReadIonogram {
    
    private String readname;
    
    private String locusinfo;
    
    private int chromosome_center_location;
    
    private int nrbases_left_right;
    
    private ArrayList<FlowValue> flowvalues;
       
    private String floworder;
    
    
    public SubReadIonogram(String readname, int chromosome_center_location) {
        this.readname = readname;
        this.chromosome_center_location = chromosome_center_location;        
        
        flowvalues = new ArrayList<FlowValue>();
    }
    
    public void addFlowValue(FlowValue flowvalue) {
        flowvalues.add(flowvalue);
        // add to maps
    }

    public void addFlow(int flowvalue, int flowposition, char base, int chromosome_location, boolean empty){        
        FlowValue val = new FlowValue(flowvalue, flowposition, base, chromosome_location, empty);
        addFlowValue(val);
    }
    
    @Override
    public String toString() {
        String s = readname+"@ "+locusinfo+":\n";
        s += FlowValue.getHeader()+"\n";
        for (FlowValue fv: flowvalues) {
            s += fv.toString()+"\n";
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
    
    
}
