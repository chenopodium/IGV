/* Copyright (C) 2010 Ion Torrent Systems, Inc. All Rights Reserved */
package org.iontorrent.sam2flowgram.flowalign;

import java.io.PrintStream;
import org.iontorrent.sam2flowgram.util.AlignUtil;

/**
 * Represents an base sequence in flow space.
 *
 * @author nils.homer@lifetech.com
 */
public class FlowSeq {
    /**
     * The gap alignment character.
     */
    public static char GAP = '-';

    /**
     * The flow sequence in integers (100x SFF format).
     */
    public int flow[] = null;

    /**
     * The flow sequence length.
     */
    public int length = 0;

    /**
     * The amount of memory allocated for the flow sequence.
     */
    private int mem = 0;

    /** 
     * The first non-empty flow.
     */
    public int nonEmptyFlowFirst;

    /**
     * The last non-empty flow.
     */
    public int nonEmptyFlowLast;

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
    
     /**
     * Creates a new flow sequence.
     * @param signals the flow signals for the read (100x), null if not present
     */
    public FlowSeq(int signals[]) {
       this.flow = signals;        
       this.length = flow.length;
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

        if(null != signals) {
            this.mem = signals.length;
        }
        else {
            // ? is this a good approximation of the flow length ?
            this.mem = seq.length * flowOrder.length; 
            // get rid of Ns
            for(posinseq=0;posinseq<seq.length;posinseq++) {
                if(3 < seq[posinseq]) {
                    seq[posinseq] = 0; // Ns to As
                }
            }
        }
        this.flow = new int[this.mem];

        posinseq = 0;
        flowOrderPosition = startFlowIndex;
        while(posinseq < seq.length) {
            int before = posinseq;

            // move beyond the initial gaps
            while(posinseq < seq.length && GAP == seq[posinseq]) {
                posinseq++;
            }
            if(seq.length <= posinseq) break;

            // skip over empty flow
            while(flowOrder[flowOrderPosition] != seq[posinseq] && seq[posinseq] <= 3) {
                this.flow[this.length] = 0;
                this.length++;
                flowOrderPosition = (flowOrderPosition+1) % flowOrder.length;
            }

            // get the number of bases in current flow
            nextPosInSeq = posinseq+1;
            nrbases = 1;
            while(nextPosInSeq < seq.length && (flowOrder[flowOrderPosition] == seq[nextPosInSeq] || 3 < seq[nextPosInSeq])) {
                if(flowOrder[flowOrderPosition] == seq[nextPosInSeq] || 3 < seq[posinseq]) {
                    nrbases++;
                }
                nextPosInSeq++;
            }
            if(this.flow.length <= this.length) {
                throw new Exception("Not enough flow signals!");
            }
            this.flow[this.length] = nrbases * 100;
            this.length++;
            flowOrderPosition = (flowOrderPosition+1) % flowOrder.length;
            posinseq = nextPosInSeq;
            if(posinseq <= before) {
                throw new Exception("posinseq <= before ["+posinseq+"<="+before+"]");
            }
        }

        this.nonEmptyFlowFirst = 0;
        while(this.nonEmptyFlowFirst < this.flow.length && 0 == this.flow[this.nonEmptyFlowFirst]) {
            this.nonEmptyFlowFirst++;
        }
        
        this.nonEmptyFlowLast = this.length-1;
        while(0 < this.nonEmptyFlowLast && 0 == this.flow[this.nonEmptyFlowLast]) {
            this.nonEmptyFlowLast--;
        }

        // copy over flow signals
        if(null != signals) {
            for(posinseq=startFlowIndex, flowOrderPosition=0;posinseq<signals.length;posinseq++,flowOrderPosition++) {
                // the read sequence has the priority
                if(AlignUtil.getBaseCallFromFlowSignal(this.flow[flowOrderPosition]) == AlignUtil.getBaseCallFromFlowSignal(signals[flowOrderPosition])) {
                    this.flow[flowOrderPosition] = signals[flowOrderPosition];
                }
                else {
                    this.flow[flowOrderPosition] += (signals[flowOrderPosition] - (AlignUtil.getBaseCallFromFlowSignal(signals[flowOrderPosition]) * 100));
                    if(this.flow[flowOrderPosition] < 0) {
                        this.flow[flowOrderPosition] = 0;
                    }
                }
            }
        }
    }

    /**
     * Debuggin print function.
     * @param out the output stream.
     */
    public void print(PrintStream out)
    {
        int i;

        for(i=0;i<this.length;i++) {
            if(0 < i) {
                out.print(",");
            }
            out.print(this.flow[i]);
        }
        out.println("");
    }
} 
