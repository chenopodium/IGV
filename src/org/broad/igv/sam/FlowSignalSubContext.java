package org.broad.igv.sam;

import com.iontorrent.rawdataaccess.FlowValue;

/**
 * Represents a flow signals context in an alignment block focused on a given
 * base. Added to support IonTorrent alignments.
 *
 * @author Nils Homer @date 4/11/12 Modified by Chantal Roth, 6/21/2012
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

    protected void appendFlowInfo(StringBuffer buf, SamAlignment sam) {
        if (getFlowValues() != null && getFlowValues().length > 0) {
            buf.append("ZM = ");
            listValues(buf, "VALUE");
            FlowValue fv = getCurrentValue();
            if (fv != null) {
                buf.append(fv.toHtml());
            }
            // maybe also add flow order?                
            //SamAlignment sam = (SamAlignment) this;
            if (sam.hasComputedErrors()) {
                buf.append("Raw errors = ");
                listValues(buf, "RAWERROR");
                buf.append("Error % = ");
                listValues(buf, "ERROR");
            }
        }
    }

    protected void listValues(StringBuffer buf, String type) {
        int n = 0;
        for (int i = 0; i < getNrSignalTypes(); i++) {
            FlowValue[] flowvalues = getValuesOfType(i);
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
