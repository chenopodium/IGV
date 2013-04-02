package org.broad.igv.sam;

import com.iontorrent.expmodel.FlowSeq;
import com.iontorrent.rawdataaccess.FlowValue;
import java.util.Arrays;
import org.apache.log4j.Logger;

/**
 * Builds a flow context in an alignment block. Added to support IonTorrent
 * alignments.
 *
 * @author Nils Homer
 * @date 4/11/12 Modified by Chantal Roth, 6/21/2012
 */
public class FlowSignalContextBuilder {

    private float[] flowSignals = null;
    /**
     * obsolete, will be removed soon
     */
    private float[] oldSignals = null;
    private int[] hplengths = null;
    private String flowOrder = null;
    private int flowStartAfterKeyOrBarcode = -1;
    private int flowOrderIndex = -1;
    private int prevFlowSignalsStart = -1;
    private int prevFlowSignalsEnd = -1;
    private int flowOrderStart = -1;
    private boolean readNegativeStrandFlag;
    private boolean[] incorporations = null; // required for the reverse strand
    public static final int PREV = 0;
    public static final int CURR = 1;
    public static final int NEXT = 2;
    //  private FlowSeq flowseq;
    static int NRMESS = 0;

    public FlowSignalContextBuilder(FlowSeq flowseq, float[] oldSignals, float[] flowSignals, String flowOrder, int flowOrderStart,
            byte[] readBases, int fromIdx, boolean readNegativeStrandFlag) {
        if (null == flowSignals || null == flowOrder || flowOrderStart < 0) {
            return;
        }
        // this.flowseq = flowseq;

        this.flowSignals = flowSignals;
        /**
         * obsolete, will be removed soon
         */
        this.oldSignals = oldSignals;
        this.flowOrder = flowOrder;
        this.hplengths = new int[flowSignals.length];
        this.flowOrderIndex = this.flowOrderStart = flowOrderStart;
        this.flowStartAfterKeyOrBarcode = 0; // NB: the key sequence/barcode sequence should have been removed for the signals already
        this.readNegativeStrandFlag = readNegativeStrandFlag;


        if (NRMESS < 10 && readNegativeStrandFlag) {
            p("nr bases=" + readBases.length + ",  flowsignals.length=" + flowSignals.length + ", softclippedbasecount (fromIndx)=" + fromIdx + ", flowOrderStart=" + flowOrderStart);
            NRMESS++;
        }
        // init
        if (this.readNegativeStrandFlag) {
            int i;
          //  p("Finding incorp in NEG. flowSignal.length="+flowSignals.length);
            this.incorporations = new boolean[this.flowSignals.length];
            // go to the end of the signals to find the first sequenced base
            for (i = readBases.length - 1; 0 <= i; i--) {
                char baseinread = SamAlignment.NT2COMP[readBases[i]];
                while (flowStartAfterKeyOrBarcode < flowSignals.length && this.flowOrder.charAt(this.flowOrderIndex) != baseinread) {
                    // p("     No inc at "+flowStartAfterKeyOrBarcode+" because baseinread "+baseinread+"<>"+this.flowOrder.charAt(this.flowOrderIndex));                                                
                    this.flowOrderIndex++;                   
                    if (this.flowOrder.length() <= this.flowOrderIndex) {
                        this.flowOrderIndex = 0;
                    }
                    this.flowStartAfterKeyOrBarcode++;
                }
                if (flowStartAfterKeyOrBarcode < flowSignals.length) {
                    this.incorporations[this.flowStartAfterKeyOrBarcode] = true;
                    //p("     Inc at "+flowStartAfterKeyOrBarcode);
                    
                }
              //  else p("        flowStartAfterKeyOrBarcode "+flowStartAfterKeyOrBarcode+">= "+flowSignals.length+", not setting to true for inc");

            }
            this.prevFlowSignalsStart = this.flowStartAfterKeyOrBarcode + 1;
            this.prevFlowSignalsEnd = this.flowSignals.length - 1;
        } else {
            this.prevFlowSignalsStart = this.prevFlowSignalsEnd = 0;
            while (this.flowOrder.charAt(this.flowOrderIndex) != readBases[0]) {
                this.flowOrderIndex++;
                this.flowStartAfterKeyOrBarcode++;
                if (this.flowOrder.length() <= this.flowOrderIndex) {
                    this.flowOrderIndex = 0;
                }
            }

            this.prevFlowSignalsEnd = this.flowStartAfterKeyOrBarcode - 1;
        }
        if (0 < fromIdx) { // skip over leading bases (ex. soft clipped bases)
            p("Need to skip over " + fromIdx + " soft clipped bases! flowStartAfterKeyOrBarcode=" + flowStartAfterKeyOrBarcode);
            p("Flow signals: " + Arrays.toString(flowSignals));
            int nrSkipped = 0;
            while (0 <= this.flowStartAfterKeyOrBarcode && this.flowStartAfterKeyOrBarcode < this.flowSignals.length && nrSkipped < fromIdx) {
                float s = this.flowSignals[this.flowStartAfterKeyOrBarcode];
                int nextFlowSignalsStart = -1, nextFlowSignalsEnd = -1;
                int posAfterSkipped = nrSkipped + 1;
                char baseafter = SamAlignment.NT2COMP[readBases[posAfterSkipped]];
                if (posAfterSkipped < readBases.length) {
                    if (this.readNegativeStrandFlag) {
                        
                     //   p("         skipping clipped bases in reverse flow. posAfterSkipped="+posAfterSkipped+", base(posAfterSkipped)="+baseafter);
                        nextFlowSignalsEnd = this.flowStartAfterKeyOrBarcode - 1;
                        // NB: loop condition is not symmetric to the forward, as we must respect the directionality of sequencing.
                        // For example, if our flow order is TACAG, and our read bases are TAG, then the flow signal vector is 
                        // approximately 100,100,0,0,100.  Since we move in the reverse direction with respect to the flow signal 
                        // vector we must pre-compute where the flows incorporations are expected to occur, instead of just looking 
                        // for the next flow that matches our next read base (we would place the A incorporation flow in the fourth flow,
                        // which is wrong).
                        while (flowStartAfterKeyOrBarcode > 0 && !this.incorporations[this.flowStartAfterKeyOrBarcode]
                                || this.flowOrder.charAt(this.flowOrderIndex) != baseafter) { // NB: malicious input can cause infinite loops here
                            this.flowOrderIndex--;
                            this.flowStartAfterKeyOrBarcode--;
                     //       p("                flowStartAfterKeyOrBarcode (rev)="+flowStartAfterKeyOrBarcode+", baseafter="+baseafter+", flowbase @ floworder "+flowOrderIndex+"="+ this.flowOrder.charAt(this.flowOrderIndex)+", inc?"+ this.incorporations[this.flowStartAfterKeyOrBarcode]);
                            if (this.flowOrderIndex < 0) {
                                this.flowOrderIndex = this.flowOrder.length() - 1;
                            }
                        }
                        nextFlowSignalsStart = this.flowStartAfterKeyOrBarcode + 1;
                    } else {
                        nextFlowSignalsStart = this.flowStartAfterKeyOrBarcode + 1;
                        while (this.flowOrder.charAt(this.flowOrderIndex) != baseafter) { // NB: malicious input can cause infinite loops here
                            this.flowOrderIndex++;
                            this.flowStartAfterKeyOrBarcode++;
                        //    p("                flowStartAfterKeyOrBarcode="+flowStartAfterKeyOrBarcode+", baseafter="+baseafter+", flowchar="+flowOrder.charAt(this.flowOrderIndex));
                            if (this.flowOrder.length() <= this.flowOrderIndex) {
                                this.flowOrderIndex = 0;
                            }
                        }
                        nextFlowSignalsEnd = this.flowStartAfterKeyOrBarcode - 1;
                    }
                }
                // update for the next iteration
                this.prevFlowSignalsStart = nextFlowSignalsStart;
                this.prevFlowSignalsEnd = nextFlowSignalsEnd;
                nrSkipped++; // next base
            }
        }
    }

    // TODO:
    // - support IUPAC bases
    // - support lower/upper cases (is this necessary)?
    public FlowSignalContext getFlowSignalContext( byte[] readBases, int startBasePosition, int nrBasesInBlock) {
        int currentBasePosition, flow;
        FlowValue[][][] blockFlowValues = null;

        if (null == this.flowSignals) {
            Logger.getLogger("FlowSignalContextBuilder").info("getFlowSignalContext: Got no flow signals");
            return null;
        }

        blockFlowValues = new FlowValue[nrBasesInBlock][][];

        // NB: should be at the first base of a HP
        // Go through the bases
        currentBasePosition = startBasePosition;
        flow = 0;

        int[] flowOrderIndices = new int[nrBasesInBlock];

        while (flowStartAfterKeyOrBarcode >= 0 && flowStartAfterKeyOrBarcode < this.flowSignals.length && currentBasePosition < startBasePosition + nrBasesInBlock) {
            float curvalue = this.flowSignals[flowStartAfterKeyOrBarcode];
            float oldcurvalue = 0;
            int curflow = flowStartAfterKeyOrBarcode + this.flowOrderStart;
            if (this.oldSignals != null) {
                oldcurvalue = oldSignals[flowStartAfterKeyOrBarcode];
            }
            char curbase = this.flowOrder.charAt((flowStartAfterKeyOrBarcode + this.flowOrderStart) % this.flowOrder.length());
            flowOrderIndices[flow] = flowStartAfterKeyOrBarcode + flowOrderStart;
            int nextFlowSignalsStart = -1, nextFlowSignalsEnd = -1;
            int basepos = currentBasePosition + 1;
            
            if (basepos < readBases.length) {
                char baseatcurpos = SamAlignment.NT2COMP[readBases[basepos]];
                if (this.readNegativeStrandFlag) {
                    nextFlowSignalsEnd = flowStartAfterKeyOrBarcode - 1;
                    // NB: loop condition is not symmetric to the forward, as we must respect the directionality of sequencing.
                    // Since we move in the reverse direction we must pre-compute where the flows incorporations are expected to occur, instead of just looking 
                    // for the next flow that matches our next read base (we would place the A incorporation flow in the fourth flow,
                    // which is wrong).
                    while (flowStartAfterKeyOrBarcode >= 0 && !this.incorporations[this.flowStartAfterKeyOrBarcode]
                            || this.flowOrder.charAt(this.flowOrderIndex) != baseatcurpos) { // NB: malicious input can cause infinite loops here
                        this.flowOrderIndex--;
                        this.flowStartAfterKeyOrBarcode--;
                        if (this.flowOrderIndex < 0) {
                            this.flowOrderIndex = this.flowOrder.length() - 1;
                        }
                    }
                    nextFlowSignalsStart = this.flowStartAfterKeyOrBarcode + 1;
                } else {
                    nextFlowSignalsStart = this.flowStartAfterKeyOrBarcode + 1;
                    while (this.flowOrder.charAt(this.flowOrderIndex) != baseatcurpos) { // NB: malicious input can cause infinite loops here
                        this.flowOrderIndex++;
                        this.flowStartAfterKeyOrBarcode++;
                        if (this.flowOrder.length() <= this.flowOrderIndex) {
                            this.flowOrderIndex = 0;
                        }
                    }
                    nextFlowSignalsEnd = this.flowStartAfterKeyOrBarcode - 1;
                }
            }
            // set-up block
            blockFlowValues[flow] = new FlowValue[3][];
            // now we are at the beginning of a HP, and the flow number is flowOrderIndex
            // this.previous context
            if (0 <= this.prevFlowSignalsStart && this.prevFlowSignalsStart <= this.prevFlowSignalsEnd && this.prevFlowSignalsEnd < this.flowSignals.length) {
                blockFlowValues[flow][PREV] = new FlowValue[this.prevFlowSignalsEnd - this.prevFlowSignalsStart + 1];
                if (this.readNegativeStrandFlag) {
                    for (int flowpos = this.prevFlowSignalsEnd; this.prevFlowSignalsStart <= flowpos; flowpos--) {
                        char c = this.flowOrder.charAt((flowpos + this.flowOrderStart) % this.flowOrder.length());
                        FlowValue fv = new FlowValue(0, flowpos + this.flowOrderStart, this.flowSignals[flowpos], c);
                        if (oldSignals != null) {
                            fv.setOldvalue(oldSignals[flowpos]);
                        }
                        blockFlowValues[flow][PREV][this.prevFlowSignalsEnd - flowpos] = fv;

                    }
                } else {
                    for (int flowpos = this.prevFlowSignalsStart; flowpos <= this.prevFlowSignalsEnd; flowpos++) {
                        char c = this.flowOrder.charAt((flowpos + this.flowOrderStart) % this.flowOrder.length());
                        FlowValue fv = new FlowValue(0, flowpos + this.flowOrderStart, this.flowSignals[flowpos], c);
                        if (oldSignals != null) {
                            fv.setOldvalue(oldSignals[flowpos]);
                        }
                        blockFlowValues[flow][PREV][flowpos - this.prevFlowSignalsStart] = fv;
                    }
                }
            } else {
                blockFlowValues[flow][PREV] = null;
            }
            // current context
            blockFlowValues[flow][CURR] = new FlowValue[1];


            FlowValue fv = new FlowValue((int) (curvalue + 50) / 100, curflow, curvalue, curbase);
            if (oldSignals != null && oldcurvalue > 0) {
                fv.setOldvalue(oldcurvalue);
            }
            blockFlowValues[flow][CURR][0] = fv;
            // next context
            if (0 <= nextFlowSignalsStart && nextFlowSignalsStart <= nextFlowSignalsEnd && nextFlowSignalsEnd < this.flowSignals.length) {

                blockFlowValues[flow][NEXT] = new FlowValue[nextFlowSignalsEnd - nextFlowSignalsStart + 1];

                if (this.readNegativeStrandFlag) {
                    for (int flowpos = nextFlowSignalsEnd; nextFlowSignalsStart <= flowpos; flowpos--) {
                        char c = this.flowOrder.charAt((flowpos + this.flowOrderStart) % this.flowOrder.length());

                        fv = new FlowValue(0, flowpos + this.flowOrderStart, this.flowSignals[flowpos], c);

                        if (oldSignals != null) {
                            fv.setOldvalue(oldSignals[flowpos]);
                        }
                        blockFlowValues[flow][NEXT][nextFlowSignalsEnd - flowpos] = fv;
                    }
                } else {
                    for (int flowpos = nextFlowSignalsStart; flowpos <= nextFlowSignalsEnd; flowpos++) {
                        char c = this.flowOrder.charAt((flowpos + this.flowOrderStart) % this.flowOrder.length());
                        fv = new FlowValue(0, flowpos + this.flowOrderStart, this.flowSignals[flowpos], c);
                        if (oldSignals != null) {
                            fv.setOldvalue(oldSignals[flowpos]);
                        }
                        blockFlowValues[flow][NEXT][flowpos - nextFlowSignalsStart] = fv;
                    }
                }
            } else {
                blockFlowValues[flow][NEXT] = null;
            }
            // update for the next iteration
            this.prevFlowSignalsStart = nextFlowSignalsStart;
            this.prevFlowSignalsEnd = nextFlowSignalsEnd;
            currentBasePosition++; // next base
            flow++; // next base
        }

        return new FlowSignalContext(blockFlowValues, flowOrderIndices);
    }

    private void p(String s) {
      //  System.out.println("FlowSignalContextBuilder: " + s);
    }
}
