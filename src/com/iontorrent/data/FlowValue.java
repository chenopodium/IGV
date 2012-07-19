/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

/**
 * One particular flow value that maps to one particular position in the genome and what base it is
 * @author Chantal Roth
 */
public class FlowValue {
    private int flowvalue;
    private char base;
    private char alignmentbase;
    private int basecall_location;
    /** whether this flow is actually mapping to an empty base, so no true location on the genome
     * In that case, the location is the location of the previously mapped flow
     */
    private boolean empty;
    
    /** flow position in read */
    private int flowposition;
    
    
    public FlowValue(int flowvalue, int flowposition, char base, int location_in_sequence, boolean empty, char alignmentbase) {
        this.flowvalue = flowvalue;
        this.alignmentbase = alignmentbase;
        this.flowposition = flowposition;
        this.base = base;
        this.basecall_location = location_in_sequence;
        this.empty = empty;
                
    }
    public char getAlignmentBase() {
        return alignmentbase;
    }
    public int getFlowPosition() {
        return flowposition;
    }

    public static String getHeader() {
        return "flow position, base, flow value, chromosome location, is this flow empty";
    }
    @Override
    public String toString() {
        return  flowposition+", "+base+", "+flowvalue+", "+basecall_location+", "+empty;
    }
    
    public String toHtml() {
        String nl = "<br>";
        String s=  "Flow position: "+bold(flowposition)+nl+
                   "Base called: "+bold(""+base)+nl+
                   "Flow value: "+bold(flowvalue)+nl+
                   "basecall location: "+basecall_location+nl;
        if (empty) s += "Flow type: <b>empty flow</b>";
        else s += "Flow type: <b>incorporation</b>";
        return s;
    }
    private String bold(String s) {
        return "<b>"+s+"</b>";
    }
    private String bold(int s) {
        return "<b>"+s+"</b>";
    }
    /**
     * @return the flowvalue
     */
    public int getFlowvalue() {
        return flowvalue;
    }

    /**
     * @param flowvalue the flowvalue to set
     */
    public void setFlowvalue(int flowvalue) {
        this.flowvalue = flowvalue;
    }

    /**
     * @return the base
     */
    public char getBase() {
        return base;
    }

    /**
     * @param base the base to set
     */
    public void setBase(char base) {
        this.base = base;
    }

    /**
     * @return the basecall_location
     */
    public int getBasecall_location() {
        return basecall_location;
    }

    /**
     * @param basecall_location the basecall_location to set
     */
    public void setBasecall_location(int chromosome_location) {
        this.basecall_location = chromosome_location;
    }

    /**
     * @return the empty
     */
    public boolean isEmpty() {
        return empty;
    }

    /**
     * @param empty the empty to set
     */
    public void setEmpty(boolean empty) {
        this.empty = empty;
    }
}
