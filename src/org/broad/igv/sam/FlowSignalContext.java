package org.broad.igv.sam;

import com.iontorrent.rawdataaccess.FlowValue;

/**
 * Represents a flow context in an alignment block.  Added to support IonTorrent alignments.
 *
 * @author Nils Homer
 * @date 4/11/12
 * modified by Chantal Roth
 */
public class FlowSignalContext {
     FlowValue[][][] flowvalues;
    private int[] flowOrderIndices;

    public FlowSignalContext(FlowValue[][][] flowvalues, int[] flowOrderIndices) {
        this.flowvalues = flowvalues;
        this.flowOrderIndices = flowOrderIndices;
    }
    public FlowValue[][][]  getFlowvalues() {
        return flowvalues;
    }
    public int getNrSignals() {
        return flowvalues.length;
    }
    public int getNrBases() {
        return flowvalues.length;
    }
    public FlowValue[][] getValuesForOffset(int offset) {
        return flowvalues[offset];
    }
    
    public int getFlowOrderIndexForOffset(int offset) {
        return flowOrderIndices[offset];
    }
   
}
