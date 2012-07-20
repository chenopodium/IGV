/* Copyright (C) 2010 Ion Torrent Systems, Inc. All Rights Reserved */
package org.iontorrent.sam2flowgram.flowalign;

import com.iontorrent.data.FlowValue;
import java.io.PrintStream;
import java.util.ArrayList;
import org.iontorrent.sam2flowgram.util.AlignUtil;

/**
 * Represents an base sequence in flow space.
 *
 * @author nils.homer@lifetech.com
 * refactored by chantal.roth@lifetech.com, using FlowValue data structure
 */
public class FlowSeq {
    /**
     * The gap alignment character.
     */
    public static char GAP1 = '-';
    public static char GAP2 = '_';

    /** 
     * The first non-empty flow.
     */
    public int nonEmptyFlowFirst;

    /**
     * The last non-empty flow.
     */
    public int nonEmptyFlowLast;

    private ArrayList<FlowValue> flows;
    
    
    /**
     * Creates a new flow sequence.
     * @param seq the sequence in integer format.
     * @param flowOrder the flow order in integer format.
     */
    public FlowSeq(byte seq[], byte flowOrder[])
        throws Exception
    {
        this(seq, flowOrder, 0);
    }
    
    /**
     * Creates a new flow sequence.
     * @param seq the sequence in integer format.
     * @param flowOrder the flow order in integer format.
     * @param startFlowIndex the zero-based index in which to start in the flow order.
     */
    public FlowSeq(byte seq[], byte flowOrder[], int startFlowIndex)
        throws Exception
    {
        this(seq, null, flowOrder, startFlowIndex);
    }
    
    /**
     * Creates a new flow sequence.
     * @param seq the sequence in integer format.
     * @param signals the flow signals for the read (100x), null if not present
     * @param flowOrder the flow order in integer format.
     */
    public FlowSeq(byte seq[], int signals[], byte flowOrder[])
        throws Exception
    {
        this(seq, signals, flowOrder, 0);
    }
    
    public FlowValue getFlow(int flow) {
        return flows.get(flow);
    }
  
    public boolean isEmpty(int flow) {
        return getFlow(flow).isEmpty();
    }
    public char getBase(int flow) {
        return getFlow(flow).getBase();
    }
    public int getBasecallPosition(int flow) {
        return getFlow(flow).getBasecall_location();
    }
    public int getSignal(int flow) {
        return flows.get(flow).getFlowvalue();
    }
      /**
     * Creates a new flow sequence.
     * @param signals the flow signals for the read (100x), null if not present
     */
    public FlowSeq(ArrayList<FlowValue> flows ) {
       this.flows = flows;        
       
    }
    
    public int getLength(){
        return flows.size();
    }
    /**
     * Creates a new flow sequence.
     * @param seq the sequence in integer format.
     * @param signals the flow signals for the read (100x), null if not present
     * @param flowOrder the flow order in integer format.
     * @param startFlowIndex the zero-based index in which to start in the flow order.
     */
    public FlowSeq(byte seq[], int signals[], byte flowOrder[], int startFlowIndex)
        throws Exception
    {
        int posinseq, flowOrderPosition, nrbases, nextPosInSeq;

        int curflownr = 0;
        int[] flowsignals;
        flows = new ArrayList<FlowValue>();
        int mem = 0;
        if(null != signals) {
            mem = signals.length;
        }
        else {
            // ? is this a good approximation of the flow length ?
            mem = seq.length * flowOrder.length; 
            // get rid of Ns
            for(posinseq=0;posinseq<seq.length;posinseq++) {
                if(3 < seq[posinseq]) {
                    seq[posinseq] = 0; // Ns to As
                }
            }
        }
        flowsignals = new int[mem];

        posinseq = 0;
        flowOrderPosition = startFlowIndex;
        while(posinseq < seq.length) {
            int before = posinseq;

            // move beyond the initial gaps
            while(posinseq < seq.length && (GAP1 == seq[posinseq] || GAP2 == seq[posinseq])) {
                posinseq++;
            }
            if(seq.length <= posinseq) break;

            // skip over empty flow
            while(flowOrder[flowOrderPosition] != seq[posinseq] && seq[posinseq] <= 3) {
                flowsignals[curflownr] = 0;
                // public FlowValue(int flowvalue, int flowposition, char base, int chromosome_location, boolean empty, char alignmentbase) {
                FlowValue fv = new FlowValue(0, flowOrderPosition, AlignUtil.ntIntToChar(seq[posinseq]), posinseq, true, ' ');
                flows.add(fv);
                
                curflownr++;
                flowOrderPosition = (flowOrderPosition+1) % flowOrder.length;
            }

            // get the number of bases in current flow
            nextPosInSeq = posinseq+1;
            nrbases = 1;
            char base = AlignUtil.ntIntToChar(seq[posinseq]);
            while(nextPosInSeq < seq.length && (flowOrder[flowOrderPosition] == seq[nextPosInSeq] || 3 < seq[nextPosInSeq])) {
                if(flowOrder[flowOrderPosition] == seq[nextPosInSeq] || 3 < seq[posinseq]) {
                    nrbases++;
                }
                nextPosInSeq++;
            }
            if(flowsignals.length <= curflownr) {
                throw new Exception("Not enough flow signals!");
            }
             // public FlowValue(int flowvalue, int flowposition, char base, int sequence_location, boolean empty, char alignmentbase) {
            FlowValue fv = new FlowValue(nrbases * 100, flowOrderPosition, base, posinseq, false, base);
            flows.add(fv);
            
            flowsignals[curflownr] = nrbases * 100;
            curflownr++;
            flowOrderPosition = (flowOrderPosition+1) % flowOrder.length;
            posinseq = nextPosInSeq;
            if(posinseq <= before) {
                throw new Exception("posinseq <= before ["+posinseq+"<="+before+"]");
            }
        }

        this.nonEmptyFlowFirst = 0;
        while(this.nonEmptyFlowFirst < flowsignals.length && 0 == flowsignals[this.nonEmptyFlowFirst]) {
            this.nonEmptyFlowFirst++;
        }
        
        this.nonEmptyFlowLast = curflownr-1;
        while(0 < this.nonEmptyFlowLast && 0 == flowsignals[this.nonEmptyFlowLast]) {
            this.nonEmptyFlowLast--;
        }

        // copy over flow signals
        if(null != signals) {
            for(posinseq=startFlowIndex, flowOrderPosition=0;posinseq<signals.length;posinseq++,flowOrderPosition++) {
                // the read sequence has the priority
                if(AlignUtil.getBaseCallFromFlowSignal(flowsignals[flowOrderPosition]) == AlignUtil.getBaseCallFromFlowSignal(signals[flowOrderPosition])) {
                    flowsignals[flowOrderPosition] = signals[flowOrderPosition];
                    flows.get(flowOrderPosition).setFlowvalue( signals[flowOrderPosition]);
                }
                else {
                    flowsignals[flowOrderPosition] += (signals[flowOrderPosition] - (AlignUtil.getBaseCallFromFlowSignal(signals[flowOrderPosition]) * 100));
                    if(flowsignals[flowOrderPosition] < 0) {
                        flowsignals[flowOrderPosition] = 0;
                        flows.get(flowOrderPosition).setFlowvalue(0);
                        flows.get(flowOrderPosition).setEmpty(true);
                    }
                }
            }
        }
        
        
    }

    public String toString() {
       String s = "";
       for (FlowValue fv: flows) {
            s += fv.getBase()+"("+fv.getFlowvalue()+") "+" ";
        }
       return s;
    }
    public ArrayList<FlowValue> getFlowValues() {
        return flows;
    }
    /**
     * Debuggin print function.
     * @param out the output stream.
     */
    public void print(PrintStream out)
    {
        int i;

        for(i=0;i<this.getLength();i++) {
            if(0 < i) {
                out.print(",");
            }
            out.print(this.flows.get(i));
        }
        out.println("");
    }
} 
