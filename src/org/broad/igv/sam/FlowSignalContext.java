package org.broad.igv.sam;

import com.iontorrent.rawdataaccess.FlowValue;

/**
 * Represents a flow signals context in an alignment block.  Added to support IonTorrent alignments.
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
    protected void listValues(FlowSignalSubContext context, StringBuffer buf, String type) {
        int n = 0;
        for (int i = 0; i < context.getNrSignalTypes(); i++) {
            FlowValue[] flowvalues = context.getValuesOfType(i);
            if (null != flowvalues && 0 < flowvalues.length) {
                if (i == 1) {
                    if (n > 0) {
                        buf.append(",");
                    }
                    buf.append("[");
                }
                for (int j = 0; j < flowvalues.length; j++) {
                    if (1 != i && 0 < n) {
                        buf.append(",");
                    }
                    if (type.equalsIgnoreCase("RAWERROR")) {
                        buf.append((int) flowvalues[j].getRawError());
                    } else if (type.equalsIgnoreCase("VALUE")) {
                        buf.append((int) flowvalues[j].getRawFlowvalue());
                    }
                    if (type.equalsIgnoreCase("ERROR")) {
                        buf.append((int) flowvalues[j].getComputedError());
                    }
                    char base = flowvalues[j].getBase();
                    if (flowvalues[j].isEmpty()) {
                        base = Character.toLowerCase(base);
                    }
                    buf.append(base);
                    n++;
                }
                if (1 == i) {
                    buf.append("]");
                }
            }
        }
        buf.append("<br>");

    }
}
