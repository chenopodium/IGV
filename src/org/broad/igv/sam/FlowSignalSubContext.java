package org.broad.igv.sam;

import com.iontorrent.rawdataaccess.FlowValue;

/**
 * Represents a flow signals context in an alignment block focused on a given base.  Added to support IonTorrent alignments.
 *
 * @author Nils Homer
 * @date 4/11/12
 * Modified by Chantal Roth, 6/21/2012
 */
public class FlowSignalSubContext {
    private int flowOrderIndex;
    
    public static final int PREV = 0;
    public static final int CURR = 1;
    public static final int NEXT = 2;
    
    FlowValue[][] flowvalues;
    
    public FlowSignalSubContext(FlowValue[][] flowvalues, int flowOrderIndex) {
       this.flowvalues = flowvalues;
        this.flowOrderIndex = flowOrderIndex;
    }   
    public int getFlowOrderIndex() {
        return flowOrderIndex;
    }
    public FlowValue[] getPreviousValues() {
        return flowvalues[PREV];
    }
    public FlowValue[] getCurrentValues() {
        return flowvalues[CURR];
    }
     public FlowValue getCurrentValue() {
        return flowvalues[CURR][0];
    }
     public FlowValue[] getNextValues() {
        return flowvalues[NEXT];
    }

    public int getNrSignalTypes() {
        return flowvalues.length;
    }

    public FlowValue[] getValuesOfType(int type) {
        return flowvalues[type];
    }
    public char getBaseForNextEmpty(int emptypos) {
        return flowvalues[NEXT][emptypos].getBase();
    }


    public FlowValue[][] getFlowValues() {
        return flowvalues;
    }
} 
