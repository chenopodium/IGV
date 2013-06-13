package org.broad.igv.sam;

import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.expmodel.FlowSeq;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.utils.ErrorHandler;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;
import org.iontorrent.seq.DNASequence;

/**
 * Builds a flow context in an alignment block. Added to support IonTorrent
 * alignments.
 *
 * @author Nils Homer
 * @date 4/11/12 Modified by Chantal Roth, 6/21/2012
 */
public class FlowSignalContextBuilder {

    private boolean show;
    public static final int PREV = 0;
    public static final int CURR = 1;
    public static final int NEXT = 2;
    private FlowSeq flowseq;
    private SamAlignment sam;
    // private int flowstart;
    private String revseq;

    public FlowSignalContextBuilder(SamAlignment sam, boolean show) {
        this.sam = sam;
        this.show = show;
        if (sam.isNegativeStrand()) {
            DNASequence dna = new DNASequence(sam.getReadSequence());
            DNASequence rev = dna.reverse().complement();
            revseq = rev.toSequenceString();

        }

    }

    public static void computeFlowSeqWithPrediction(FlowSeq flowseq, SamAlignment sam) {
        ExperimentContext exp = new ExperimentContext();
        //   p("---- computeConfidence for alignment " + sam.getReadName() + " -----");
        flowseq = sam.getFlowseq();
        if (flowseq == null) {
            computeFlowSeqNoPred(flowseq, sam);
        }
        if (!flowseq.hasPredictedValues()) {
            int flowstart = sam.getFlowSignalsStart();
            exp.setFlowOrder(sam.getFlowOrderNoKey());
            exp.setNrFlows(sam.getRawFlowSignals().length);
            exp.setModelParameters(sam.getCF(), sam.getIE(), sam.getDR());
            //  String key = sam.getKeySequence();
            String seq = sam.getReadSequence();
            if (sam.isNegativeStrand()) {
                DNASequence dna = new DNASequence(seq);
                DNASequence rev = dna.reverse().complement();
                seq = rev.toSequenceString();
            }
            exp.computePredictedSignal(flowseq, seq);

            float[] signals = sam.getRawFlowSignals();
            // NO KEY
            for (int f = sam.getFlowSignalsStart(); f < flowseq.getLength(); f++) {
                FlowValue fv = flowseq.getFlow(f);
                int flow = fv.getFlowPosition();
                if (flow < signals.length) {
                    fv.setRawFlowvalue((int) signals[flow]);
                }
            }
            exp.computeConfidence(flowseq.getFlowValues());
        }

    }
    private static void p(String msg) {
        System.out.println("FlowSignalContextBuilder: " + msg);
        //log.info(msg);
    }

    public static FlowSeq computeFlowSeqNoPred(FlowSeq flowseq, SamAlignment sam) {
        return computeFlowSeqNoPred(flowseq, sam, false);
    }
    public static FlowSeq computeFlowSeqNoPred(FlowSeq flowseq, SamAlignment sam, boolean debug) {

        if (flowseq == null) {

            String seq = sam.getReadSequence();
            if (sam.isNegativeStrand()) {
                DNASequence dna = new DNASequence(sam.getReadSequence());
                DNASequence rev = dna.reverse().complement();
                seq = rev.toSequenceString();
                if (debug) p("rev seq:"+seq);
            }
            // something fisy here... get rid of key
            String order = sam.getFlowOrderNoKey();

            flowseq = new FlowSeq(seq, order);
            sam.setFlowseq(flowseq); // NO KEY

            float[] signals = sam.getRawFlowSignals();
            // the flow order is the whole thing
            // the sequence is without key
            // the raw data is from the read sequence
            // flow start says which flow the first raw signal matches to

            for (int f = 0; f < flowseq.getLength(); f++) {
                FlowValue fv = flowseq.getFlow(f);
                int flow = fv.getFlowPosition();
                if (flow != f) {
                   if (debug) p("Flow < f: f="+f+", flow="+flow);
                   
                }
                if (f >= 0 && f < signals.length) {
                    fv.setRawFlowvalue((int) signals[f]);
                    if (debug) p("Flow "+f+": setting raw value "+signals[f]+" to "+fv);
                }
            }
        }
        return flowseq;
    }
    
    // TODO:
    // - support IUPAC bases
    // - support lower/upper cases (is this necessary)?

    public FlowSignalContext getFlowSignalContext(byte[] readBases, int startBasePosition, int nrBasesInBlock) {


        if (flowseq == null) {
            flowseq = computeFlowSeqNoPred(flowseq, sam);
        }
        FlowValue[][][] blockFlowValues = null;

        if (null == flowseq) {
            Logger.getLogger("FlowSignalContextBuilder").info("getFlowSignalContext: Got no flow signals");
            return null;
        }


        blockFlowValues = new FlowValue[nrBasesInBlock][][];
        //  int keylen = sam.getKeySequence().length();
        //  int keyendpos = flowseq.getFlowPosForBasePos(keylen);
//        if (keyendpos != sam.getFlowSignalsStart()) {
//            p("keyendpos " + keyendpos + "<>" + sam.getFlowSignalsStart());
//            p("Clipped? "+sam.getSoftClippedBaseCount());
//           // show = true;
//        }

//        if (show) {
//            p("====== PROCESSING " + sam.getReadName());
//            p("Seq no key: " + sam.getReadSequence());
//            p("Floworder no key: " + sam.getFlowOrderNoKey());
//            //   p("keysequence: " + sam.getKeySequence());
//        }


        for (int posinseq = startBasePosition; posinseq < startBasePosition + nrBasesInBlock; posinseq++) {
            int blockpos = posinseq - startBasePosition;
            int baseposinflowseq = posinseq;

            if (sam.isNegativeStrand()) {
                baseposinflowseq = revseq.length() - posinseq-1;
            }

            char base = (char) readBases[posinseq];

            int flow = -1;
            try {
                flow = flowseq.getFlowPosForBasePos(baseposinflowseq);
            } catch (Exception e) {
                
              //  err("baseposinflowseq too large: " + baseposinflowseq+":"+ErrorHandler.getString(e));
                if (sam.isNegativeStrand()) {
                    // need to reverse position 
                    // readsq= ....GGGATCCCCC.... <-
                    // from flow: ->  aaaGGGGGATCCC
                    // reveread = GGGGGATCCC
                    // flowseq.indexof(reverad) = 3;
                    StringBuffer revread = new StringBuffer();
                    for (int i = readBases.length-1; i >= 0; i--) {
                        revread = revread.append(SamAlignment.NT2COMP[readBases[i]]);
                    }

                    int pos = revseq.indexOf(revread.toString());
//                    err("======= Got negative strand:========= ");
//                    err("revseq= " + revseq);
//                    err("revread= " + revread);
//                    err("baseposinflowseq= " + pos);

                }
               // err("Flowseq=" + this.flowseq);
               
            }
            if (flow>=0) {
                FlowValue curflow = flowseq.getFlow(flow);
                if (curflow.isEmpty()) {
                    err("Current flow is empty!: " + curflow + "\nflowseq=" + flowseq.toString() + "\n flow=" + flow + "\nbasepos in read=" + posinseq + ", pos in seq+key=" + baseposinflowseq + ", seq=" + sam.getReadSequence());
                } else if (!sam.isNegativeStrand() && curflow.getBase() != base) {
                    err("Current base wrong! base=" + base + " @ posinseq=" + posinseq + (char) readBases[posinseq] + ", flowbase=" + curflow.getBase() + ",  " + curflow + "\nflowseq=" + flowseq.toString() + "\n flow=" + flow + "\nbasepos in read=" + posinseq + ", pos in seq+key=" + baseposinflowseq + ", seq=" + sam.getReadSequence());
                    err("Clipped? " + sam.getSoftClippedBaseCount());

                    //err("Keyseq= "+sam.getKeySequence());
                    // err("Keyseq len = "+keylen);
                   // err("flow start = " + sam.getFlowSignalsStart());
                  //  err("Flows from " + (sam.getFlowSignalsStart() - 2) + "-" + (flow + 2));
                    for (int i = sam.getFlowSignalsStart() - 2; i < flow + 2; i++) {
                        FlowValue fv = flowseq.getFlow(i);
                        err(fv.toString());
                    }
                  //  System.exit(0);
                }
                ArrayList<FlowValue> before = flowseq.getEmptyFlowsBefore(flow);
                ArrayList<FlowValue> after = flowseq.getEmptyFlowsAfter(flow);

//                if (show) {
//                    p("====== Base =" + curflow.getBase() + ", baseposinflowseq=" + baseposinflowseq + ", flow=" + flow + ":" + curflow.toString() + ", base in read=" + posinseq + "=" + sam.getReadSequence().charAt(posinseq));
//
//                }

                blockFlowValues[blockpos] = new FlowValue[3][];
                if (before != null) {
                    FlowValue fv = flowseq.getFlowForBasePos(baseposinflowseq - 1);
                    if (fv != null) {
                        before.add(0, fv);
                    }
                    blockFlowValues[blockpos][PREV] = new FlowValue[before.size()];
                    for (int i = 0; i < before.size(); i++) {
                        blockFlowValues[blockpos][PREV][i] = before.get(i);                        
                    }
                } 

                blockFlowValues[blockpos][CURR] = new FlowValue[1];
//                if (show) {
//                    p("Got fv: " + curflow);
//                }
                blockFlowValues[blockpos][CURR][0] = curflow;
                if (after != null) {
                    FlowValue fv = flowseq.getFlowForBasePos(baseposinflowseq + curflow.getHpLen());
                    if (fv != null) {
                        after.add(fv);
                    }
                    blockFlowValues[blockpos][NEXT] = new FlowValue[after.size()];
                    for (int i = 0; i < after.size(); i++) {
                        blockFlowValues[blockpos][NEXT][i] = after.get(i);                        
                    }
                }

            }
        }

        return new FlowSignalContext(blockFlowValues);
    }

    private void sp(String s) {
//        if (show) {
//            System.out.println("FlowSignalContextBuilder: " + s);
//        }
    }

    private void err(String s) {
        System.err.println("FlowSignalContextBuilder: " + s);
        show = true;
    }
}
