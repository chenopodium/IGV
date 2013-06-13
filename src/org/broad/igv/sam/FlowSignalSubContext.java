package org.broad.igv.sam;

import com.iontorrent.expmodel.FlowSeq;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.utils.AlignUtil;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 * Represents a flow context in an alignment block focused on a given
 * base. Added to support IonTorrent alignments.
 *
 * @author Nils Homer @date 4/11/12 Modified by Chantal Roth, 6/21/2012
 */
public class FlowSignalSubContext {

//    private int flowOrderIndex;
    public static final int PREV = 0;
    public static final int CURR = 1;
    public static final int NEXT = 2;
    FlowValue[][] flowvalues;

    private static final boolean DEBUG = true;
    
    public FlowSignalSubContext(FlowValue[][] flowvalues) {
        this.flowvalues = flowvalues;
     //   this.flowOrderIndex = flowOrderIndex;
    }

    public boolean hasFlowValues() {
        return flowvalues != null;
    }
    protected void appendFlowInfo(StringBuffer buf, SamAlignment sam) {
        FlowSeq fs =  sam.getFlowseq();
        if (!fs.hasPredictedValues()) {
            FlowSignalContextBuilder.computeFlowSeqWithPrediction(fs, sam);
        }
        if (getFlowValues() != null && getFlowValues().length > 0) {
            buf.append("ZM = ");
            listValues(buf, "VALUE");
            FlowValue fv = getCurrentValue();
            if (fv != null) {
                buf.append(fv.toHtml());
              //  int keylen = sam.getKeySequence().length();
                buf.append("Position in read = "+(fv.getBasecall_location()));
                buf.append("<br>");
            }
            // maybe also add flow order?                
            //SamAlignment sam = (SamAlignment) this;
            if (sam.hasComputedConfidence()) {
                buf.append("Model deviation = ");
                listValues(buf, "DEVIATION");                
                buf.append("Confidence = ");
                listValues(buf, "CONFIDENCE");
               
            }
            int flow = fv.getFlowPosition();
            int pos = fv.getBasecall_location();
            int delta = 7;
          //  if (DEBUG) {
                buf.append("Flow sequence around this flow=");
                for (int f = Math.max(0, flow-delta); f< flow+delta; f++) {
                    FlowValue v = fs.getFlow(f);
                    if (v != null) {
                        String base = ""+v.getBase();
                        int val = (int)v.getRawFlowvalue();
                        if (f == flow) {
                            buf.append("<b>["+val+base+"]</b>");
                        }
                        else {
                            buf.append(val+base);
                        }
                         buf.append(" ");
                    }
                }
                buf.append("<br>");
                buf.append("FlowOrder around this flow=");
                for (int f = Math.max(0, flow-delta); f< flow+delta; f++) {                    
                    if (f < sam.getFlowOrderNoKey().length()) {
                        char c = sam.getFlowOrderNoKey().charAt(f);
                        if (f == flow)buf.append("<b>"+c+"</b>");
                        else  buf.append(c);
                    }
                }
                buf.append("<br>");
                buf.append("Base calls around this position=");
                String seq = sam.getReadSequence();
                if (sam.isNegativeStrand()) {
                     String rev = new StringBuilder(sam.getReadSequence()).reverse().toString();
                     seq = AlignUtil.complement(rev);
                }
                for (int f = Math.max(0, pos-delta); f< pos+delta; f++) {                    
                    if (f < seq.length()) {
                        char c = seq.charAt(f);
                        if (f == pos)buf.append("<b>"+c+"</b>");
                        else  buf.append(c);
                    }
                }
                buf.append("<br>");
                buf.append("FlowStart="+sam.getFlowSignalsStart());
                buf.append("<br>");
                if (sam.getSoftClippedBaseCount()>0) {
                    buf.append("getSoftClippedBaseCount="+sam.getSoftClippedBaseCount());
                    buf.append("<br>");                
                }
//                if (sam.getReadName().endsWith("02796")) {
//                    int maxpos = 30;
//                    int maxflow = 51;
//                    Logger.getLogger(getClass()).info(buf.toString().replace("<br>", "\n"));
//                    
//                    String s = "read: "+sam.getReadName();
//                    s += "\nflow order (no key): "+sam.getFlowOrderNoKey();
//                    s += "\nZM(no key): "+Arrays.toString(sam.getRawFlowSignals());
//                    s += "\ncigar: "+sam.getCigarString();
//                    s += "\nread: "+sam.getReadSequence();
//                    String rev = new StringBuilder(sam.getReadSequence()).reverse().toString();
//                    s += "\ncomplement reverse: "+AlignUtil.complement(rev).substring(0, maxpos);
//                    s += "\nclip: "+sam.getSoftClippedBaseCount();
//                    s += "\nflow seq: "+sam.getFlowseq().toString();
//                    for (int f = 0; f < maxflow; f++) {
//                        fv = sam.getFlowseq().getFlow(f);
//                        s += "\n"+fv.getFlowPosition()+" -> "+fv.getBasecall_location()+":"+fv.toString();
//                    }
//                                        
//                     Logger.getLogger(getClass()).info(s);
//                }
                
          //  }
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
                    if (type.equalsIgnoreCase("DEVIATION")) {
                        buf.append((int) flowvalues[j].getModelDeviation());
                    } else if (type.equalsIgnoreCase("VALUE")) {
                        buf.append((int) flowvalues[j].getRawFlowvalue());
                    }
                    if (type.equalsIgnoreCase("CONFIDENCE")) {
                        buf.append((int) flowvalues[j].getConfidence());
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

    public FlowValue[] getPreviousValues() {
        return flowvalues[PREV];
    }

    public FlowValue[] getCurrentValues() {
        return flowvalues[CURR];
    }

    public FlowValue getCurrentValue() {
        if (flowvalues == null) return null;
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

