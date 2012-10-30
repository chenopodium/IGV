/* Copyright (C) 2010 Ion Torrent Systems, Inc. All Rights Reserved */
package org.iontorrent.sam2flowgram.flowalign;


import com.iontorrent.rawdataaccess.FlowValue;
import java.io.PrintStream;
import java.util.Arrays;
import org.iontorrent.sam2flowgram.util.AlignUtil;

/**
 * Represents an alignment in flow space.
 *
 * @author nils.homer@lifetech.com
 */
public class FlowgramAlignment {

    /**
     * The alignment was extended from a match.
     */
    public static final int FROM_MATCH = 0;
    /**
     * The alignment was extended from an insertion.
     */
    public static final int FROM_INS = 1;
    /**
     * The alignment was extended from an deletion.
     */
    public static final int FROM_DEL = 2;
    /**
     * The alignment was extended from a match with an empty flow.
     */
    public static final int FROM_MATCHEMPTY = 3;
    /**
     * The alignment was extended from an insertion with an empty flow.
     */
    public static final int FROM_INSEMPTY = 4;
    /**
     * The alignment was extended from a phased match (skipping a phase).
     */
    public static final int FROM_PHASEDMATCH = 5;
    /**
     * The alignment was extended from a phased insertion (skipping a phase).
     */
    public static final int FROM_PHASEDINS = 6;
    /**
     * The alignment was extended from an insertion.
     */
    public static final int FROM_S = 7;
    /**
     * The lower bound on the alignment score, or negative infinity.
     */
    public static final int MINOR_INF = -100000; // how to set this?
    /**
     * A flow deletion in the alignment string.
     */
    public static final char ALN_DEL = '-';
    /**
     * A flow insertion in the alignment string.
     */
    public static final char ALN_INS = '+';
    /**
     * A flow match in the alignment string.
     */
    public static final char ALN_MATCH = '|';
    /**
     * A flow mismatch in the alignment string.
     */
    public static final char ALN_MISMATCH = ':';
    /**
     * The alignment length.
     */
    public int length = 0; // the alignment length
    /**
     * The memory allocated for the alignment.
     */
    private int mem = 0; // the mememory allocated for the alignment
    /**
     * The best alignment score found so far.
     */
    private int score;
    // Depend on the alignment order 
    /**
     * The flow order for the alignment, including deleted reference bases.
     */
    public byte flowOrder[] = null;
    /**
     * The query or read sequence in the alignment, including gaps.
     */
    public int qseq[] = null; // read
    /**
     * The target or reference sequence in the alignment, including gaps.
     */
    public int tseq[] = null; // reference
    /**
     * The alignment string.
     */
    public char aln[] = null; // alignment string
    /**
     * The index of the first non-empty query flow.
     */
    public int nonEmptyFlowFirst; // the index of the first non-empty read flow
    /**
     * The index of the last non-empty query flow.
     */
    public int nonEmptyFlowLast; // the index of the last non-empty read flow
    /**
     * The zero-based index in the input tseq where the alignment starts.
     */
    public int tseqStart;
    /**
     * The zero-based index in the input tseq where the alignment ends.
     */
    public int tseqEnd;
    /**
     * The zero-based index of flowOrder/qseq/tseq/aln where a variant of
     * interest starts
     */
    public int variantStart;
    /**
     * The zero-based index of flowOrder/qseq/tseq/aln where a variant of
     * interest ends
     */
    public int variantEnd;
    /**
     * The tseq length, for this.reverseCompliment().
     */
    public int tseqLength;
    /**
     * auxiliary indexes for later reconstructing the MULTIPLE sequence
     * alignment using many alignments and one global reference or consensus. To
     * do that, we need to know where each query and target base call ended up
     * in the alignment
     */
    private FlowSeq flowQseq;
    private FlowSeq flowTseq;
    private FlowOrder tseqFlowOrder;
    private int[] alignpos_to_qpos;
    private int[] alignpos_to_tpos;
    private int[] qpos_to_alignpos;
    private int[] tpos_to_alignpos;
    private int[] tflowpos_to_tbasepos;
    private int[] tbasepos_to_tflowpos;
    /**
     * The penalty for phasing a flow.
     */
    public static final int FLOW_SPACE_PHASE_PENALTY = 1;

    // qseq - query - read
    // tseq - target - reference
    /**
     * Represents an alignment in flow space.
     *
     * Notes: we want to align the flow flowQseq to a subsequence of tseq
     *
     * @param flowQseq the query's flow sequence.
     * @param tseq the target base in integer format.
     * @param qseqFlowOrder the flow order of the query flow sequence.
     */
    public FlowgramAlignment(FlowSeq flowQseq, byte tseq[],
            FlowOrder qseqFlowOrder)
            throws Exception {
        this(flowQseq, tseq, qseqFlowOrder, false, false,
                FLOW_SPACE_PHASE_PENALTY);
    }

    /**
     * Represents an alignment in the flowgram.
     *
     * Notes: we want to align the flow flowQseq to a subsequence of tseq
     *
     * @param flowQseq the query's flow sequence.
     * @param tseq the target base in integer format.
     * @param qseqFlowOrder the flow order of the query flow sequence.
     * @param startLocal false if the we must begin the alignment at the start
     * of the target, true otherwise
     * @param endLocal false if the we must end the alignment at the end of the
     * target, true otherwise
     * @param phasePenalty the penalty for phasing in the alignment.
     */
    public FlowgramAlignment(FlowSeq flowQseq, byte tseq[], FlowOrder qseqFlowOrder,
            boolean startLocal, boolean endLocal, int phasePenalty)
            throws Exception {
        this.computeAlignment(flowQseq, tseq, qseqFlowOrder, startLocal, endLocal, phasePenalty);
    }

    public FlowgramAlignment(FlowSeq flowQseq, String consensus, FlowOrder qorder, boolean startLocal, boolean endLocal, int phasePenalty)
     throws Exception {
        byte[] targetseq = new byte[consensus.length()];
        int r = 0;
       
        for (int i = 0; i < consensus.length(); i++) {
            char base = consensus.charAt(i);
            if (base != ' ' && base != '_') {
                tseq[r] = (byte) AlignUtil.baseCharToInt(base);
                r++;                
            }
        }
       
        computeAlignment(flowQseq, targetseq, qorder, startLocal, endLocal, phasePenalty);
    }
    
        
    private void init(FlowSeq flowQseq, byte targetseq[]) throws Exception {          
        this.flowQseq = flowQseq;        
        // create tseq flow order
        tseqFlowOrder = new FlowOrder(targetseq, true);
        // for this.reverseCompliment().
        tseqLength = targetseq.length;
        // HERE
//        for(i=0;i<tseqFlowOrder.getLength();i++) {
//            System.out.println("i=" + i + " tseqFlowOrder.flowOrder[i]=" + tseqFlowOrder.flowOrder[i]);
//        }

        // convert bases to flow space
        flowTseq = new FlowSeq(targetseq, tseqFlowOrder.flowOrder);
      
        tflowpos_to_tbasepos = new int[flowTseq.getLength()];
        tbasepos_to_tflowpos = new int[targetseq.length];
        int basepos = 0;
        for (int t = 0; t < flowTseq.getLength(); t++) {            
            tflowpos_to_tbasepos[t] = basepos;
            tbasepos_to_tflowpos[basepos] = t;
            if (!flowTseq.isEmpty(t)) basepos++;
        }
        // init
        this.mem = flowQseq.getLength() + flowTseq.getLength();
        this.flowOrder = new byte[mem];
        this.qseq = new int[mem];
        this.alignpos_to_qpos = new int[mem];
        this.alignpos_to_tpos = new int[mem];
        this.qpos_to_alignpos = new int[mem];
        this.tpos_to_alignpos = new int[mem];
        this.tseq = new int[mem];
        this.aln = new char[mem];
        this.length = 0;

    }
    public FlowSeq getQueryFlowSeq() {
        return this.flowQseq;
    }
    public FlowSeq getTargetFlowSeq() {
        return this.flowTseq;
    }
    public FlowValue getQueryFlowValue(int queryflowpos) {
        return this.flowQseq.getFlow(queryflowpos);
    }
    public FlowValue getTargetFlowValue(int targetflowpos) {
        return this.flowTseq.getFlow(targetflowpos);
    }
    public int getQueryPosForTargetBasePos(int targetbasepos) {
        int alignpos = this.getAlignPosForTpos(targetbasepos);
        int qpos = this.getQposForAlignPos(alignpos);
        return qpos;
    }
    // qseq - query - read
    // tseq - target - reference
    /**
     * Represents an alignment in flow space.
     *
     * Notes: we want to align the flow flowQseq to a subsequence of tseq
     *
     * @param flowQseq the query's flow sequence.
     * @param tseq the target base in integer format.
     * @param qseqFlowOrder the flow order of the query flow sequence.
     * @param startLocal false if the we must begin the alignment at the start
     * of the target, true otherwise
     * @param endLocal false if the we must end the alignment at the end of the
     * target, true otherwise
     * @param phasePenalty the penalty for phasing in the alignment.
     */
    public void computeAlignment(FlowSeq flowQseq, byte targetseq[], FlowOrder qseqFlowOrder,
            boolean startLocal, boolean endLocal, int phasePenalty)
            throws Exception {
        int i, j, k;
        int vScoreP, vScoreE, vFromP, vFromE;
        int cType, iFrom;
        int bestI, bestJ, bestCType;
        
        init(flowQseq, targetseq);
        
        //init gap sums & dp matrix 
        int gapSumsI[] = new int[flowQseq.getLength()];
        FlowSpaceAlignmentCell dp[][] = new FlowSpaceAlignmentCell[1 + flowQseq.getLength()][1 + flowTseq.getLength()];

        for (i = 0; i <= flowQseq.getLength(); i++) {
            if (i < flowQseq.getLength()) {
                k = i % qseqFlowOrder.getLength();
                //j = (i < qseqFlowOrder.jumpRev[k]) ? 0 : (i - qseqFlowOrder.jumpRev[k]);
                //j = (i < qseqFlowOrder.jumpRev[k]) ? 0 : (i - qseqFlowOrder.jumpRev[k] + 1);
                j = (i < qseqFlowOrder.jumpRev[k]) ? 0 : (i - qseqFlowOrder.jumpRev[k] + 1);
                gapSumsI[i] = phasePenalty;
                while (j <= i) {
                    gapSumsI[i] += flowQseq.getSignal(j);
                    j++;
                }
                /*
                 * System.err.println("i=" + i + " flowQseq.flow[i]=" +
                 * flowQseq.flow[i] + " qseqFlowOrder.flowOrder[i]=" +
                 * qseqFlowOrder.flowOrder[i] + " qseqFlowOrder.jumpRev[i]=" +
                 * qseqFlowOrder.jumpRev[i] + " gapSumsI[i]=" + gapSumsI[i]);
                 */
            }
            // dp matrix init
            for (j = 0; j <= flowTseq.getLength(); j++) {
                dp[i][j] = new FlowSpaceAlignmentCell();
                dp[i][j].matchScore = dp[i][j].insScore = dp[i][j].delScore = MINOR_INF;
                dp[i][j].matchFrom = dp[i][j].insFrom = dp[i][j].delFrom = FROM_S;
            }

            if (i > 0) {
                k = (i - 1) % qseqFlowOrder.getLength();
                iFrom = ((i < qseqFlowOrder.jumpRev[k]) ? 0 : (i - qseqFlowOrder.jumpRev[k]));

                // vertical
                // only allow phasing from an insertion
                if (0 == iFrom) {
                    dp[i][0].insScore = 0 - gapSumsI[i - 1];
                    dp[i][0].insFrom = FROM_PHASEDINS;
                } else {
                    dp[i][0].insScore = dp[iFrom][0].insScore - gapSumsI[i - 1];
                    dp[i][0].insFrom = FROM_PHASEDINS;
                }
            }
        }

        // init start cells
        dp[0][0].matchScore = 0;
       
        // align
        for (i = 1; i <= flowQseq.getLength(); i++) { // query
            k = (i - 1) % qseqFlowOrder.getLength();
            int qflowvalue = flowQseq.getSignal(i - 1);
            iFrom = ((i < qseqFlowOrder.jumpRev[k]) ? 0 : (i - qseqFlowOrder.jumpRev[k]));
            for (j = 1; j <= flowTseq.getLength(); j++) { // target
                // horizontal
                int tflowvalue = flowTseq.getSignal(j - 1);
                if (dp[i][j - 1].delScore < dp[i][j - 1].matchScore) {
                    if (dp[i][j - 1].insScore <= dp[i][j - 1].matchScore) {
                        dp[i][j].delScore = dp[i][j - 1].matchScore - tflowvalue;
                        dp[i][j].delFrom = FROM_MATCH;
                    } else {
                        dp[i][j].delScore = dp[i][j - 1].insScore - tflowvalue;
                        dp[i][j].delFrom = FROM_INS;
                    }
                } else {
                    if (dp[i][j - 1].insScore <= dp[i][j - 1].delScore) {
                        dp[i][j].delScore = dp[i][j - 1].delScore - tflowvalue;
                        dp[i][j].delFrom = FROM_DEL;
                    } else {
                        dp[i][j].delScore = dp[i][j - 1].insScore - tflowvalue;
                        dp[i][j].delFrom = FROM_INS;
                    }
                }
               

                // vertical
                // four moves:
                // 1. phased from match
                // 2. phased from ins
                // 3. empty from match
                // 4. empth from ins
                // Note: use the NEXT reference base for flow order matching
                if (j == flowTseq.getLength() // no next reference base
                        || (1 == i) // always start with leading phasing
                        || (qseqFlowOrder.flowOrder[(i - 1) % qseqFlowOrder.getLength()] == tseqFlowOrder.flowOrder[j % tseqFlowOrder.getLength()])) {
                    vScoreE = MINOR_INF;
                    vFromE = FROM_MATCHEMPTY;
                } else {
                    if (dp[i - 1][j].insScore <= dp[i - 1][j].matchScore) {
                        vScoreE = dp[i - 1][j].matchScore - qflowvalue;
                        vFromE = FROM_MATCHEMPTY;
                    } else {
                        vScoreE = dp[i - 1][j].insScore - qflowvalue;
                        vFromE = FROM_INSEMPTY;
                    }
                    // Start anywhere in tseq
                    if (i == 1 && vScoreE + qflowvalue < 0) {
                        vScoreE = 0 - qflowvalue;
                        vFromE = FROM_S;
                    }
                }
                // phased from ...
                if (dp[iFrom][j].insScore <= dp[iFrom][j].matchScore) {
                    vScoreP = dp[iFrom][j].matchScore - gapSumsI[i - 1];
                    vFromP = FROM_PHASEDMATCH;
                } else {
                    vScoreP = dp[iFrom][j].insScore - gapSumsI[i - 1];
                    vFromP = FROM_PHASEDINS;
                }
                // compare empty vs. phased
                if (vScoreP <= vScoreE) { // Note: always choose empty over phased
                    dp[i][j].insScore = vScoreE;
                    dp[i][j].insFrom = vFromE;
                } else {
                    dp[i][j].insScore = vScoreP;
                    dp[i][j].insFrom = vFromP;
                }

                // diagonal
                if (qseqFlowOrder.flowOrder[(i - 1) % qseqFlowOrder.getLength()] != tseqFlowOrder.flowOrder[(j - 1) % tseqFlowOrder.getLength()]) {
                    // out of phase, do not want
                    dp[i][j].matchScore = MINOR_INF;
                    dp[i][j].matchFrom = FROM_S;
                } else {
                    int signaldiff = ((qflowvalue < tflowvalue) ? (tflowvalue - qflowvalue) : (qflowvalue - tflowvalue));
                    // NB: do not penalize it full on the first or last flow
                    if (i == 1 || i == flowQseq.getLength()) {
                        signaldiff = AlignUtil.getFlowSignalFromBaseCall(getBC(qflowvalue));
                        signaldiff = (qflowvalue < signaldiff) ? (signaldiff - qflowvalue) : (qflowvalue - signaldiff);
                    }
                    // NB: choose del first, then match, then ins
                    if (dp[i - 1][j - 1].insScore <= dp[i - 1][j - 1].matchScore) {
                        if (dp[i - 1][j - 1].delScore < dp[i - 1][j - 1].matchScore) {
                            dp[i][j].matchScore = dp[i - 1][j - 1].matchScore - signaldiff;
                            dp[i][j].matchFrom = FROM_MATCH;
                        } else {
                            dp[i][j].matchScore = dp[i - 1][j - 1].delScore - signaldiff;
                            dp[i][j].matchFrom = FROM_DEL;
                        }
                    } else {
                        if (dp[i - 1][j - 1].delScore < dp[i - 1][j - 1].insScore) {
                            dp[i][j].matchScore = dp[i - 1][j - 1].insScore - signaldiff;
                            dp[i][j].matchFrom = FROM_INS;
                        } else {
                            dp[i][j].matchScore = dp[i - 1][j - 1].delScore - signaldiff;
                            dp[i][j].matchFrom = FROM_DEL;
                        }
                    }

                    // Start anywhere in tseq
                    if (startLocal && 1 == i && dp[i][j].matchScore + signaldiff < 0) {
                        dp[i][j].matchScore = 0 - signaldiff;
                        dp[i][j].matchFrom = FROM_S;
                    }
                }

                // HERE

//                System.out.print("i=" + i + " j=" + j 
//                        + " qseq=" + qseqFlowOrder.flowOrder[(i-1) % qseqFlowOrder.getLength()]
//                        + " tseq=" + tseqFlowOrder.flowOrder[(j-1) % tseqFlowOrder.getLength()]
//                        + " ");
//                dp[i][j].print(System.err);

            }
        }

        // Get best scoring cell
        this.score = MINOR_INF - 1;
        bestCType = FROM_S;
        bestI = -1;
        bestJ = -1;

        // TODO: want to map the query into a sub-sequence of the target
        // We can end anywhere in the target, but we haven't done the beginning.
        // We also need to return where the start end in the target to update start/end position(s).
        if (endLocal) {
            for (j = 1; j <= flowTseq.getLength(); j++) { // target
                 int tflowvalue = flowTseq.getSignal(j - 1);
                /*
                 * System.err.println("j=" + j + " " +
                 * tseqFlowOrder.flowOrder[(j-1) % tseqFlowOrder.getLength()] + " " +
                 * flowTseq.flow[j-1] + " " + dp[flowQseq.getLength()][j].delScore +
                 * " " + dp[flowQseq.getLength()][j].insScore + " " +
                 * dp[flowQseq.getLength()][j].matchScore + " " + this.score );
                 */
                //if(this.score <= dp[flowQseq.getLength()][j].delScore) {
                if (0 < tflowvalue && this.score <= dp[flowQseq.getLength()][j].delScore) {
                    bestI = flowQseq.getLength();
                    bestJ = j;
                    this.score = dp[flowQseq.getLength()][j].delScore;
                    bestCType = FROM_DEL;
                }
                if (this.score <= dp[flowQseq.getLength()][j].insScore) {
                    bestI = flowQseq.getLength();
                    bestJ = j;
                    this.score = dp[flowQseq.getLength()][j].insScore;
                    bestCType = FROM_INS;
                }
                //if(0 < flowTseq.flow[j-1] && this.score <= dp[flowQseq.getLength()][j].matchScore) {
                if (this.score <= dp[flowQseq.getLength()][j].matchScore) {
                    bestI = flowQseq.getLength();
                    bestJ = j;
                    this.score = dp[flowQseq.getLength()][j].matchScore;
                    bestCType = FROM_MATCH;
                }
            }
        } else {
            if (this.score <= dp[flowQseq.getLength()][flowTseq.getLength()].delScore) {
                bestI = flowQseq.getLength();
                bestJ = flowTseq.getLength();
                this.score = dp[flowQseq.getLength()][flowTseq.getLength()].delScore;
                bestCType = FROM_DEL;
            }
            if (this.score <= dp[flowQseq.getLength()][flowTseq.getLength()].insScore) {
                bestI = flowQseq.getLength();
                bestJ = flowTseq.getLength();
                this.score = dp[flowQseq.getLength()][flowTseq.getLength()].insScore;
                bestCType = FROM_INS;
            }
            if (this.score <= dp[flowQseq.getLength()][flowTseq.getLength()].matchScore) {
                bestI = flowQseq.getLength();
                bestJ = flowTseq.getLength();
                this.score = dp[flowQseq.getLength()][flowTseq.getLength()].matchScore;
                bestCType = FROM_MATCH;
            }
        }


//        System.err.println("flowQseq.getLength()=" + flowQseq.getLength()
//                + " flowTseq.getLength()=" + flowTseq.getLength());
//        System.err.println("bestI=" + bestI
//                + " bestJ=" + bestJ
//                + " score=" + score
//                + " bestCType=" + bestCType);
//        

        // Calculate tseqEnd
        this.tseqEnd = 0;
        for (j = 0; j < bestJ; j++) {
            this.tseqEnd += getBC(flowTseq.getSignal(j));
            //      System.err.println(SamToFlowgramAlignUtil.DNA[tseqFlowOrder.flowOrder[j]] + " " + flowTseq.flow[j] + " " + this.tseqEnd);
        }
        this.tseqEnd--;

        i = bestI;
        j = bestJ;
        cType = bestCType;


        // trace path back
        while (0 < i) { // qseq flows left
            int nextCType = -1;

            // HERE
            //System.err.println("i=" + i + " j=" + j + " cType=" + cType);

            if (FROM_MATCH == cType) {
                nextCType = dp[i][j].matchFrom;
                this.addToAlignment(i - 1, j - 1, qseqFlowOrder.flowOrder[(i - 1) % qseqFlowOrder.getLength()]);
                i--;
                j--;
            } else if (FROM_INS == cType) {
                nextCType = dp[i][j].insFrom;
                if (dp[i][j].insFrom == FROM_MATCHEMPTY || dp[i][j].insFrom == FROM_INSEMPTY) {
                    // we can't use zero or else it will use the tseq value at position zero.
                    // so we use -2 as a special value (great! :-)
                    this.addToAlignment(i - 1, -2, qseqFlowOrder.flowOrder[(i - 1) % qseqFlowOrder.getLength()]);
                    i--;
                } else if (dp[i][j].insFrom == FROM_PHASEDMATCH || dp[i][j].insFrom == FROM_PHASEDINS) {
                    k = (i - 1) % qseqFlowOrder.getLength();
                    iFrom = ((i < qseqFlowOrder.jumpRev[k]) ? 0 : (i - qseqFlowOrder.jumpRev[k]));
                    while (iFrom < i) {
                        k = (i - 1) % qseqFlowOrder.getLength();
                        this.addToAlignment(i - 1, -1, qseqFlowOrder.flowOrder[k]);
                        i--;
                    }
                } else if (dp[i][j].insFrom == FROM_S) {
                    while (0 < i) {
                        // always a start insertion
                        this.addToAlignment(i - 1, -1, qseqFlowOrder.flowOrder[(i - 1) % qseqFlowOrder.getLength()]);
                        //this.add(i-1, 0, qseqFlowOrder.flowOrder[(i-1) % qseqFlowOrder.getLength()]);
                        i--;
                    }
                } else {
                    System.err.println("dp[i][j].insFrom=" + dp[i][j].insFrom);
                    throw new Exception("bug encountered");
                }
            } else if (FROM_DEL == cType) {
                nextCType = dp[i][j].delFrom;
                //System.err.println("FOUND!");
                this.addToAlignment(-1, j - 1, tseqFlowOrder.flowOrder[(j - 1) % tseqFlowOrder.getLength()]);
                j--;
            } else {
                System.err.println("cType=" + cType);
                System.err.println("i=" + i + " j=" + j);
                qseqFlowOrder.print(System.err);
                flowQseq.print(System.err);
                tseqFlowOrder.print(System.err);
                flowTseq.print(System.err);
                throw new Exception("bug encountered");
            }

            // HERE
            //System.err.println("nextCType=" + nextCType);
            switch (nextCType) {
                case FROM_MATCH:
                case FROM_INS:
                case FROM_DEL:
                case FROM_S:
                    cType = nextCType;
                    break;
                case FROM_MATCHEMPTY:
                case FROM_PHASEDMATCH:
                    cType = FROM_MATCH;
                    break;
                case FROM_INSEMPTY:
                case FROM_PHASEDINS:
                    cType = FROM_INS;
                    break;
                default:
                    throw new Exception("bug encountered");
            }
        }
        // Calculate tseqStart
        this.tseqStart = 0;
        for (i = 0; i < j; i++) {
            this.tseqStart += getBC(flowTseq.getSignal(i));
        }

        // reverse the arrays tseq, qseq, aln, flowOrder
        this.reverse();

        // TODO: are these needed?
        this.nonEmptyFlowFirst = 0;
        for (i = 0; i < this.getLength(); i++) {
            if (0 < getBC(this.qseq[i])) {
                this.nonEmptyFlowFirst = i;
                break;
            }
        }
        this.nonEmptyFlowLast = 0;
        for (i = this.getLength() - 1; 0 <= i; i--) {
            if (0 < getBC(this.qseq[i])) {
                this.nonEmptyFlowLast = i;
                break;
            }
        }

        checkAndFixEmptyFlowsInAlignment();
        fixHelperArrays();
        // HERE
        // this.print(System.out);
    }
    public int getLength() {
        return length;
    }

    /**
     * Makes a new object from an existing one.
     *
     * @param modelAlign the alignment where the new one will be modeled after.
     * This allows for making a subsequence of the full alignment.
     * @param fullyClone the new alignment will be a clone of the old one. [Not
     * implemented yet.]
     */
    public FlowgramAlignment(FlowgramAlignment modelAlign, boolean fullyClone) throws Exception {
        if (fullyClone) {
            this.length = modelAlign.getLength();
            this.score = modelAlign.score;
            this.flowOrder = modelAlign.flowOrder.clone();
            this.qseq = modelAlign.qseq.clone();
            this.tseq = modelAlign.tseq.clone();
            this.aln = modelAlign.aln.clone();
            throw new Exception(String.format("fullyClone not fully implemented in FlowgramAlignment constructor."));
        }
        //TODO, see if there's other items, i.e. nonEmptyFlowLast, that are needed
        /*
         * nonEmptyFlowFirst nonEmptyFlowLast tseqStart tseqEnd tseqLength
         */
    }

    /**
     * Reverse the alignment.
     *
     * Note: this does not reverse non-empty first/last index.
     */
    private void reverse() {
        int i;
        for (i = 0; i < this.getLength() / 2; i++) {
            int b;
            char c;
            byte by;

            int irev = this.getLength() - i - 1;
            b = this.qseq[i];
            this.qseq[i] = this.qseq[irev];
            this.qseq[irev] = b;

            c = this.aln[i];
            this.aln[i] = this.aln[irev];
            this.aln[irev] = c;

            b = this.tseq[i];
            this.tseq[i] = this.tseq[irev];
            this.tseq[irev] = b;

            by = this.flowOrder[i];
            this.flowOrder[i] = this.flowOrder[irev];
            this.flowOrder[irev] = by;

            b = this.alignpos_to_qpos[i];
            alignpos_to_qpos[i] = alignpos_to_qpos[irev];
            alignpos_to_qpos[irev] = b;
            
            b = this.alignpos_to_tpos[i];
            alignpos_to_tpos[i] = alignpos_to_tpos[irev];
            alignpos_to_tpos[irev] = b;
        }
        int tmp[] = tpos_to_alignpos;
        for (i = 0; i < tpos_to_alignpos.length; i++) {
            int al =  tmp[i];
            tpos_to_alignpos[i] = this.getLength() - al - 1;
        }
        tmp = qpos_to_alignpos;
        for (i = 0; i < qpos_to_alignpos.length; i++) {
            int al =  tmp[i];
            qpos_to_alignpos[i] = this.getLength() - al - 1;
        }
    }
    private void fixHelperArrays() {
        // fix zeros
        for (int i = 0; i < alignpos_to_tpos.length; i++) {
            int tpos = this.getTargetFlowposForAlignPos(i);
            alignpos_to_tpos[i] = tpos;
        }
        // fix invalid values
        for (int i = 0; i < tpos_to_alignpos.length; i++) {
            int al =  tpos_to_alignpos[i];
            if (al >= this.length) tpos_to_alignpos[i] = 0;
        }
    }
    public String showHelperArrays() {
        String s = "Target flows: "+this.flowTseq.toString()+"\n";
        s += "qpos to alignpos:    "+Arrays.toString(qpos_to_alignpos)+"\n";
        s += "tpos to alignpos: "+Arrays.toString(tpos_to_alignpos)+"\n";
        
        s += "alignpos to qpos:    "+Arrays.toString(this.alignpos_to_qpos)+"\n";
        s += "alignpos to tpos:    "+Arrays.toString(this.alignpos_to_tpos)+"\n";
        
        s += "tbasepos to flowpos: "+Arrays.toString(this.tbasepos_to_tflowpos)+"\n";
        s += "tflowpos to basepos: "+Arrays.toString(this.tflowpos_to_tbasepos)+"\n";
        return s;
    }
    public int getQposForAlignPos(int i) {
        return alignpos_to_qpos[i];
    }
    public int getTargetBaseposForTargetFlowPos(int i) {
        if (i <=0) return tflowpos_to_tbasepos[0];
        int pos = this.tflowpos_to_tbasepos[i];
        if (pos ==0) return getTargetBaseposForTargetFlowPos(i-1);
        else return pos;
    }
    public int getTargetFlowposForAlignPos(int i) {
        if (i <=0)return alignpos_to_tpos[0];
        int pos = this.alignpos_to_tpos[i];
        if (pos ==0) return getTargetFlowposForAlignPos(i-1);
        else return pos;
    }
    public int getAlignPosForQpos(int i) {
        return this.qpos_to_alignpos[i];
    }
     public int getAlignPosForTBasepos(int i) {
        int flow = tbasepos_to_tflowpos[i];
        return this.tpos_to_alignpos[flow];
    }
     public int getAlignPosForTpos(int i) {
        return this.tpos_to_alignpos[i];
    }
    /**
     * Reverse compliments this alignment, to faciliate its representation on
     * the forward genomic strand for mapped reverse strand queries.
     */
    public void reverseCompliment() {
        int i;
        for (i = 0; i < this.getLength() / 2; i++) {
            int b;
            char c;
            byte by;

            b = this.qseq[i];
            this.qseq[i] = this.qseq[this.getLength() - i - 1];
            this.qseq[this.getLength() - i - 1] = b;

            c = this.aln[i];
            this.aln[i] = this.aln[this.getLength() - i - 1];
            this.aln[this.getLength() - i - 1] = c;

            b = this.tseq[i];
            this.tseq[i] = this.tseq[this.getLength() - i - 1];
            this.tseq[this.getLength() - i - 1] = b;

            by = AlignUtil.NTINT2COMP[(int) this.flowOrder[i]];
            this.flowOrder[i] = AlignUtil.NTINT2COMP[(int) this.flowOrder[this.getLength() - i - 1]];
            this.flowOrder[this.getLength() - i - 1] = by;
        }
        if (1 == (this.getLength() % 2)) {
            this.flowOrder[i] = AlignUtil.NTINT2COMP[(int) this.flowOrder[i]];
        }

        // reverse tseq bounds
        i = this.tseqStart;
        this.tseqStart = this.tseqLength - this.tseqEnd - 1;
        this.tseqEnd = this.tseqLength - i - 1;

        // convert leading empty flows to insertions
        for (i = 0; i < this.getLength(); i++) {
            if (0 == this.tseq[i] && (ALN_MATCH == this.aln[i] || ALN_MISMATCH == this.aln[i])) {
                this.tseq[i] = 0;
                this.aln[i] = ALN_INS;
            } else {
                break;
            }
        }

        // Reverse non-empty first/last
        //nonEmptyFlowFirst = this.getLength() - nonEmptyFlowFirst - 1;
        //nonEmptyFlowLast = this.getLength() - nonEmptyFlowLast - 1;
    }

    /**
     * Cells in the dynamic programming matrix.
     */
    private class FlowSpaceAlignmentCell {

        /**
         * Stores the score for extending with a match.
         */
        public int matchScore;
        /**
         * Stores the score for extending with a insertion.
         */
        public int insScore;
        /**
         * Stores the score for extending with a deletion.
         */
        public int delScore;
        /**
         * Stores the previous cell in the path to a match.
         */
        public int matchFrom;
        /**
         * Stores the previous cell in the path to a insertion.
         */
        public int insFrom;
        /**
         * Stores the previous cell in the path to a deletion.
         */
        public int delFrom;

        /**
         * Creates a new cell.
         */
        public FlowSpaceAlignmentCell() {
            // do nothing
        }

        /**
         * Debugging print function.
         *
         * @param out the output stream.
         */
        public void print(PrintStream out) {
            out.println("[" + this.matchScore + "," + this.matchFrom
                    + ":" + this.insScore + "," + this.insFrom
                    + ":" + this.delScore + "," + this.delFrom
                    + "]");
        }
    }

    /**
     * Adds the given flow to the alignment.
     *
     * @param qseqN the number of query bases.
     * @param tseqN the number of target bases.
     * @param base the flow base.
     */
    private void addToAlignment(int positionInQseq, int positionInTseq, byte base)
            throws Exception {
        // not enough memory
        if (this.mem <= this.getLength()) {
            throw new Exception("this.mem <= this.getLength() [" + this.mem + "<=" + this.getLength() + "]");
        }
        // add in the alignment
        this.flowOrder[this.getLength()] = base;
        int qseqN = 0;
        if (positionInQseq >= 0) {
            qseqN = this.flowQseq.getSignal(positionInQseq);
            this.alignpos_to_qpos[length] = positionInQseq;
            this.qpos_to_alignpos[positionInQseq] = length;
        } else {
            qseqN = positionInQseq;
        }

        int tseqN = 0;
        if (positionInTseq >= 0) {
            tseqN = this.flowTseq.getSignal(positionInTseq);
            this.alignpos_to_tpos[length] = positionInTseq;
            this.tpos_to_alignpos[positionInTseq] = length;
        } else {
            tseqN = positionInTseq;
        }
        if (tseqN < -1) {
            tseqN = 0;
        }

        this.qseq[this.getLength()] = qseqN;
        this.tseq[this.getLength()] = tseqN;
        if (-1 == qseqN) {
            this.aln[this.getLength()] = ALN_DEL;
            this.qseq[this.getLength()] = 0;
        } else if (-1 == tseqN) {
            this.aln[this.getLength()] = ALN_INS;
            this.tseq[this.getLength()] = 0;
        } else if (getBC(qseqN) == getBC(tseqN)) {
            this.aln[this.getLength()] = ALN_MATCH;
        } else {
            this.aln[this.getLength()] = ALN_MISMATCH;
        }
        this.length++;
    }

    /**
     * Moves flow insertions and deletions.
     */
    public boolean rightAdjustIndels() {
        int i, j, k;
        boolean adjusted = false;
        int lower;

        lower = 0;
        // skip over start indels
        while (lower < this.getLength()) {
            if (ALN_MATCH == this.aln[lower] || ALN_MISMATCH == this.aln[lower]) {
                break;
            }
            lower++;
        }

        i = this.getLength() - 2; // since if we end with a deletion, we have no bases to shift
        while (lower < i) {
            // find the end of the indel
            if (ALN_INS == this.aln[i] || ALN_DEL == this.aln[i]) {
                // get the left-most indel base
                j = i; // start with theright-most indel base
                while (lower < j - 1 && this.aln[i] == this.aln[j - 1]) {
                    j--;
                }
                // while we have a base to shift
                k = i + 1; // right non-indel base
                // shift over while valid shifts exist
                //System.err.println("Found at i=" + i + " j=" + j);
                while (lower < j
                        && (ALN_MATCH == this.aln[k] || ALN_MISMATCH == this.aln[k])
                        //&& this.tseq[k] == 0
                        && this.flowOrder[k] == this.flowOrder[j]) {
                    int tmpInt;
                    byte tmpByte;
                    char tmpChar;

                    // swap
                    //System.err.println("SWAPING k=" + k + " j=" + j + " " + this.qseq[k] + " with " + this.qseq[j]);
                    //tmpInt = this.qseq[k]; this.qseq[k] = this.qseq[j]; this.qseq[j] = tmpInt;
                    tmpInt = this.tseq[k];
                    this.tseq[k] = this.tseq[j];
                    this.tseq[j] = tmpInt;
                    //tmpByte = this.flowOrder[k]; this.flowOrder[k] = this.flowOrder[j]; this.flowOrder[j] = tmpByte;
                    tmpChar = this.aln[k];
                    this.aln[k] = this.aln[j];
                    this.aln[j] = tmpChar;

                    j++;
                    k++;
                    adjusted = true;
                }
                // update past the indel
                i = j - 1;
            } else {
                i--;
            }
        }

        return adjusted;
    }

    private int getBC(int FS) {
        return AlignUtil.getBaseCallFromFlowSignal(FS);
    }

    private int getFS(int BC) {
        return AlignUtil.getFlowSignalFromBaseCall(BC);
    }

    /**
     * Tries to handle how an insertion of base in the middle of an HP is
     * represented in flow space.
     */
    public boolean splitReferenceFlows() {
        int i, j, k, numRemoveDel = 0;
        boolean adjusted = false, found, removeDel = false;
        int lower = 0;
        int diffEnd, diffStart, val;

        i = this.getLength() - 1;
        while (lower < i) {
            if (ALN_MISMATCH != this.aln[i] && ALN_DEL != this.aln[i]) {
                i--;
                continue;
            }
            //System.err.println("FOUND " + this.aln[i] + " at i=" + i);
            diffEnd = getBC(this.tseq[i]) - getBC(this.qseq[i]);
            // test if we can shift over the tseq values
            j = i;
            diffStart = 0;
            found = false;
            while (lower < j - 1) {
                // break when:
                // 1. different flow order with tseq > 0
                // 2. same flow order with diffStart != 0 
                if (0 < this.tseq[j - 1] && this.flowOrder[j] == this.flowOrder[i]) {
                    break;
                }
                diffStart = getBC(this.tseq[j - 1]) - getBC(this.qseq[j - 1]);
                if (0 != diffStart && this.flowOrder[j - 1] == this.flowOrder[i]) {
                    j--;
                    found = true; // This is what we want
                    break;
                }
                j--;
            }
            //System.err.println("Found start =" + found + " at j=" + j);
            if (!found) {
                i--;
                continue;
            }
            // same flow order with diffStart != 0
            // NB: val is the # of bases to subtract from the start and
            // add to the end.
            val = 0;
            if (diffStart < 0 && 0 < diffEnd) {
                // NB: subtract a negative value 
                if (-diffStart < diffEnd) {
                    val = diffStart;
                } else {
                    val = -diffEnd;
                }
            } else if (0 < diffStart && diffEnd < 0) {
                // NB: subtract a positive value 
                if (diffStart < -diffEnd) {
                    val = diffStart;
                } else {
                    val = -diffEnd;
                }
            }
            if (0 < val) {
                this.tseq[j] -= getFS(val);
                this.tseq[i] += getFS(val);
                // adjust alignment
                if (getBC(this.tseq[i]) == getBC(this.qseq[i])) {
                    if (ALN_MISMATCH == this.aln[i]) {
                        this.aln[i] = ALN_MATCH;
                    } else if (ALN_DEL == this.aln[i]) {
                        if (0 == this.tseq[i]) {
                            // TODO: remove the deletion
                            removeDel = true;
                            numRemoveDel++;
                        }
                    }
                } else { // mismatch
                    if (ALN_MISMATCH == this.aln[i]) {
                        // do nothing
                    } else if (ALN_DEL == this.aln[i]) {
                        // do nothing
                    }
                }
                if (getBC(this.tseq[j]) == getBC(this.qseq[j])) {
                    if (ALN_MISMATCH == this.aln[j]) {
                        this.aln[j] = ALN_MATCH;
                    } else if (ALN_DEL == this.aln[j]) {
                        if (0 == this.tseq[j]) { // no more deletion
                            // TODO: remove this deletion
                            removeDel = true;
                            numRemoveDel++;
                        }
                    } else if (ALN_DEL == this.aln[j]) { // NB: getBC(this.qseq[j]) == 0
                        if (0 < this.tseq[j]) {
                            this.aln[j] = ALN_MATCH;
                        }
                    }
                } else { // mismatch
                    if (ALN_MISMATCH == this.aln[j]) {
                        // ignore
                    } else if (ALN_DEL == this.aln[j]) { // NB: getBC(this.qseq[j]) == 0
                        // ignore, since if 0 < this.tseq[j] then it is still a deletion,
                        // and if this.tseq[j] == 0, then it should never get here...
                    } else if (ALN_INS == this.aln[j]) {
                        if (0 < this.tseq[j]) {
                            this.aln[j] = ALN_MISMATCH;
                        }
                    }
                }
                adjusted = true;
            }
            i--;
        }

        // remove deletions that are no longer valid
        if (removeDel) {
            // save temp
            byte tmpFlowOrder[] = this.flowOrder;
            int tmpQseq[] = this.qseq;
            char tmpAln[] = this.aln;
            int tmpTseq[] = this.tseq;
            // realloc
            int newalignlen = this.aln.length - numRemoveDel;
            this.qseq = new int[this.qseq.length - numRemoveDel];
            this.aln = new char[newalignlen];
            this.tseq = new int[this.tseq.length - numRemoveDel];
            this.alignpos_to_qpos = new int[newalignlen];
            this.alignpos_to_tpos = new int[newalignlen];
            this.qpos_to_alignpos = new int[newalignlen];
            this.tpos_to_alignpos = new int[newalignlen];
            this.flowOrder = new byte[this.flowOrder.length - numRemoveDel];
            // save
            for (i = j = 0; i < tmpAln.length; i++) {
                if (ALN_DEL == tmpAln[i] && 0 == tmpTseq[i]) {
                    continue;
                }
                // copy
                this.qseq[j] = tmpQseq[j];
                this.aln[j] = tmpAln[j];
                this.tseq[j] = tmpTseq[j];
                this.flowOrder[j] = tmpFlowOrder[j];
                j++;
            }
        }

        return adjusted;
    }

    /**
     * Debugging print function.
     *
     * @param stream the output stream.
     */
    public void print(PrintStream stream) {
        this.print(stream, 150, true);
    }

    public void print(PrintStream stream, int colWidth, boolean align) {
        stream.println(this.getAlignmentString(colWidth, align));
    }

    private int charWidth(int val) {
        if (val <= 0) {
            return 1;
        } else {
            return (1 + (int) Math.log10(val));
        }
    }

    public void checkAndFixEmptyFlowsInAlignment() {
        for (int alignpos = 0; alignpos < this.getLength(); alignpos++) {
            // tseq
            if (ALN_INS == this.aln[alignpos]) {
                // stseq.append(ALN_INS);
                int signal = this.qseq[alignpos];
                if (signal <= 50) {
                    // does not count as insertion, it is just an empty flow!
                    aln[alignpos] = ALN_MATCH;
                    tseq[alignpos] = 0;
                } else {
                    tseq[alignpos] = (int) Math.round(signal / 100) * 100;
                }
            }
        }
    }

    public String getAlignmentString(int colWidth, boolean alignCharacters) {
        int alignpos, j, numChars = 0;
        StringBuilder stseq = new StringBuilder();
        StringBuilder sqseq = new StringBuilder();
        StringBuilder saln = new StringBuilder();
        StringBuilder sflowOrder = new StringBuilder();
        StringBuilder string = new StringBuilder();

        //System.out.println("getAlignmentString:\n qseq ="+Arrays.toString(qseq) + "\naln = " + Arrays.toString(aln) + "\ntseq = " + Arrays.toString(tseq) + "\nflow order=" + Arrays.toString(this.flowOrder) + "\n"); 
        // get the widths
        for (alignpos = 0; alignpos < this.getLength(); alignpos++) {
            int w, maxWidth = 1;
            w = this.charWidth(this.qseq[alignpos]);
            if (maxWidth < w) {
                maxWidth = w;
            }
            w = this.charWidth(this.tseq[alignpos]);
            if (maxWidth < w) {
                maxWidth = w;
            }
            if (colWidth < numChars + maxWidth + 1) {
                string.append("qseq:  " + sqseq.toString() + "\nalign: " + saln.toString() + "\ntseq:  " + stseq.toString() + "\norder: " + sflowOrder.toString() + "\n");
                // reset
                stseq = new StringBuilder();
                sqseq = new StringBuilder();
                saln = new StringBuilder();
                sflowOrder = new StringBuilder();
                numChars = 0;
            } else if (0 < alignpos) {
                sqseq.append(", ");
                saln.append(", ");
                stseq.append(", ");
                sflowOrder.append(", ");
                numChars++;
            }
            // qseq
            if (ALN_DEL == this.aln[alignpos]) {
                if (alignCharacters) {
                    for (j = 1; j < maxWidth; j++) {
                        sqseq.append(" ");
                    }
                }
                sqseq.append(ALN_DEL);
            } else {
                if (alignCharacters) {
                    for (j = this.charWidth(this.qseq[alignpos]); j < maxWidth; j++) {
                        sqseq.append(" ");
                    }
                }
                sqseq.append(this.qseq[alignpos]);
            }
            // aln
            if (alignCharacters) {
                for (j = 1; j < maxWidth; j++) {
                    saln.append(" ");
                }
            }

            // tseq
            if (ALN_INS == this.aln[alignpos]) {
                if (alignCharacters) {
                    for (j = 1; j < maxWidth; j++) {
                        stseq.append(" ");
                    }
                }
                // stseq.append(ALN_INS);
                int signal = this.qseq[alignpos];
                stseq.append((int) Math.round(signal / 100) * 100);
                if (signal <= 50) {
                    // does not count as insertion, it is just an empty flow!
                    aln[alignpos] = ALN_MATCH;
                    saln.append(ALN_MATCH);
                } else {
                    saln.append(this.aln[alignpos]);
                }
            } else {
                saln.append(this.aln[alignpos]);
                if (alignCharacters) {
                    for (j = this.charWidth(this.tseq[alignpos]); j < maxWidth; j++) {
                        stseq.append(" ");
                    }
                }
                stseq.append(this.tseq[alignpos]);
            }
            // flow order
            if (alignCharacters) {
                for (j = 1; j < maxWidth; j++) {
                    sflowOrder.append(" ");
                }
            }
            sflowOrder.append(AlignUtil.DNA[this.flowOrder[alignpos % this.flowOrder.length]]);
            numChars += maxWidth;
        }
        string.append("qseq:  " + sqseq.toString() + "\nalign: " + saln.toString() + "\ntseq:  " + stseq.toString() + "\norder: " + sflowOrder.toString() + "\n");
        return string.toString();
    }

    public String getAlignmentString(boolean prettyPrint) {
        return this.getAlignmentString(Integer.MAX_VALUE, prettyPrint);
    }

    public String getAlignmentString() {
        return this.getAlignmentString(Integer.MAX_VALUE, false);
    }

    public int getScore() {
        return this.score;
    }
}
