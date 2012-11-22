/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.sam;

import com.iontorrent.rawdataaccess.FlowValue;
import org.broad.igv.feature.Strand;
import org.broad.igv.track.WindowFunction;

import java.awt.*;

/**
 * @author jrobinso
 */
public abstract class AbstractAlignment implements Alignment {

    String chr;
    int inferredInsertSize;
    int mappingQuality = 255;  // 255 by default
    ReadMate mate;
    String readName;
    AlignmentBlock[] alignmentBlocks;
    AlignmentBlock[] insertions;
    char[] gapTypes;
    private boolean negativeStrand;

    public AbstractAlignment() {
    }

    public String getChromosome() {
        return getChr();
    }

    public String getChr() {
        return chr;
    }

    public String getDescription() {
        return getReadName();
    }

    public ReadMate getMate() {
        return mate;
    }

    public String getMateSequence() {
        return null;
    }

    public String getReadName() {
        return readName;
    }

    public int getMappingQuality() {
        return mappingQuality;
    }

    public int getInferredInsertSize() {
        return inferredInsertSize;
    }

    public AlignmentBlock[] getAlignmentBlocks() {
        return alignmentBlocks;
    }

    public AlignmentBlock[] getInsertions() {
        return insertions;
    }

    public boolean isNegativeStrand() {
        return negativeStrand;
    }

    public void setNegativeStrand(boolean negativeStrand) {
        this.negativeStrand = negativeStrand;
    }

    public boolean contains(double location) {
        return location >= getStart() && location < getEnd();
    }

    public byte getBase(double position) {
        int basePosition = (int) position;
        for (AlignmentBlock block : this.alignmentBlocks) {
            if (block.contains(basePosition)) {
                int offset = basePosition - block.getStart();
                byte base = block.getBases()[offset];
                return base;
            }
        }
        return 0;
    }

    public byte getPhred(double position) {
        int basePosition = (int) position;
        for (AlignmentBlock block : this.alignmentBlocks) {
            if (block.contains(basePosition)) {
                int offset = basePosition - block.getStart();
                byte qual = block.getQuality(offset);
                return qual;
            }
        }
        return 0;
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

    private byte[] getQualityArray() {
        int totLen = 0;
        for (AlignmentBlock block : this.alignmentBlocks) {
            totLen += block.getQualities().length;
        }
        byte[] allQualities = new byte[totLen];
        int start = 0;
        for (AlignmentBlock block : this.alignmentBlocks) {
            System.arraycopy(block.getQualities(), 0, allQualities, start, block.getQualities().length);
            start += block.getQualities().length;
        }
        return allQualities;
    }

    private void bufAppendFlowInfo(AlignmentBlock block, StringBuffer buf, int offset) {
        if (block.hasFlowSignals()) {
            // flow signals           
            FlowSignalSubContext f = block.getFlowSignalSubContext(offset);
            if (f != null && f.getFlowValues() != null && f.getFlowValues().length > 0) {
                buf.append("ZM = ");
                listValues(f, buf, "VALUE");
                FlowValue fv = f.getCurrentValue();
                if (fv != null) {
                    buf.append(fv.toHtml());
                }
                // maybe also add flow order?                
                SamAlignment sam = (SamAlignment) this;
                if (sam.hasComputedErrors()) {
                    buf.append("Raw errors = ");
                    listValues(f, buf, "RAWERROR");
                    buf.append("Error % = ");
                    listValues(f, buf, "ERROR");
                }
            }
        }
    }

    @Override
    public String getValueString(double position, WindowFunction windowFunction) {
        StringBuffer buf = null;

        // First check insertions.  Position is zero based, block coords 1 based
        if (this.insertions != null) {
            for (AlignmentBlock block : this.insertions) {
                double insertionLeft = block.getStart() - .25;
                double insertionRight = block.getStart() + .25;
                if (position > insertionLeft && position < insertionRight) {
                    if (block.hasFlowSignals()) {
                        int offset;
                        buf = new StringBuffer();
                        buf.append("Insertion: " + new String(block.getBases()) + "<br>");
                        buf.append("Base phred quality = ");
                        for (offset = 0; offset < block.getBases().length; offset++) {
                            byte quality = block.getQuality(offset);
                            if (0 < offset) {
                                buf.append(",");
                            }
                            buf.append(quality);
                        }
                        buf.append("<br>");
                        for (offset = 0; offset < block.getBases().length; offset++) {
                            byte base = block.getBase(offset);
                            buf.append((char) base + ": ");
                            bufAppendFlowInfo(block, buf, offset);
                        }
                        buf.append("----------------------"); // NB: no <br> required
                        return buf.toString();
                    } else {
                        return "Insertion: " + new String(block.getBases());
                    }
                }
            }
        }

        buf = new StringBuffer();

        String sample = getSample();
        if (sample != null) {
            buf.append("Sample = " + sample + "<br>");
        }
        String readGroup = getReadGroup();
        if (sample != null) {
            buf.append("Read group = " + readGroup + "<br>");
        }
        buf.append("----------------------" + "<br>");

        int basePosition = (int) position;
        buf.append("Read name = " + getReadName() + "<br>");
        buf.append("Alignment start = " + (getAlignmentStart() + 1) + " (" + (isNegativeStrand() ? "-" : "+") + ")<br>");
        buf.append("Cigar = " + getCigarString() + "<br>");
        buf.append("Mapped = " + (isMapped() ? "yes" : "no") + "<br>");
        buf.append("Mapping quality = " + getMappingQuality() + "<br>");
        buf.append("----------------------" + "<br>");

        for (AlignmentBlock block : this.alignmentBlocks) {
            if (block.contains(basePosition)) {
                int offset = basePosition - block.getStart();
                byte base = block.getBase(offset);
                byte quality = block.getQuality(offset);
                buf.append("Base = " + (char) base + "<br>");
                buf.append("Base phred quality = " + quality + "<br>");
                if (block.hasCounts()) {
                    buf.append("Count = " + block.getCount(offset) + "<br>");
                }
                // raw flow signals
                if (block.hasFlowSignals()) {
                    bufAppendFlowInfo(block, buf, offset);
                }
            }
        }

        if (this.isPaired()) {
            buf.append("----------------------" + "<br>");
            buf.append("Pair start = " + getMate().positionString() + "<br>");
            buf.append("Pair is mapped = " + (getMate().isMapped() ? "yes" : "no") + "<br>");
            //buf.append("Pair is proper = " + (getProperPairFlag() ? "yes" : "no") + "<br>");
            if (getChr().equals(getMate().getChr())) {
                buf.append("Insert size = " + getInferredInsertSize() + "<br>");
            }
            if (getPairOrientation().length() > 0) {
                buf.append("Pair orientation = " + getPairOrientation() + "<br>");
            }
        }
        buf.append("----------------------");
        return buf.toString();
    }

    public abstract String getCigarString();

    public abstract boolean isMapped();

    public abstract boolean isPaired();

    public abstract boolean isProperPair();

    public boolean isSmallInsert() {
        int absISize = Math.abs(getInferredInsertSize());
        return absISize > 0 && absISize <= getReadSequence().length();
    }

    public float getScore() {
        return getMappingQuality();
    }

    /**
     * @param mappingQuality the mappingQuality to set
     */
    public void setMappingQuality(int mappingQuality) {
        this.mappingQuality = mappingQuality;
    }

    /**
     * @param inferredInsertSize the inferredInsertSize to set
     */
    public void setInferredInsertSize(int inferredInsertSize) {
        this.inferredInsertSize = inferredInsertSize;
    }

    /**
     * @param mate the mate to set
     */
    public void setMate(ReadMate mate) {
        this.mate = mate;
    }

    public String getReadGroup() {
        return null;
    }

    public String getLibrary() {
        return null;
    }

    public String getClipboardString(double location) {
        return getValueString(location, null);
    }

    public char[] getGapTypes() {
        return null;
    }

    public Object getAttribute(String key) {
        return null;
    }

    public void setMateSequence(String sequence) {
        // ignore by default
    }

    public String getPairOrientation() {
        return "";
    }

    public boolean isVendorFailedRead() {
        return false;
    }

    public Color getDefaultColor() {
        return AlignmentRenderer.grey1;
    }

    public Strand getReadStrand() {
        return isNegativeStrand() ? Strand.NEGATIVE : Strand.POSITIVE;
    }
}
