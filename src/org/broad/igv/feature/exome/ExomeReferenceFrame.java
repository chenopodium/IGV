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

package org.broad.igv.feature.exome;

import org.apache.log4j.Logger;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.Locus;
import org.broad.igv.feature.NamedFeature;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.tribble.Feature;

import java.util.*;

/**
 * @author Jim Robinson
 * @date 5/30/12
 */
public class ExomeReferenceFrame extends ReferenceFrame {

    private static Logger log = Logger.getLogger(ExomeReferenceFrame.class);

    Map<String, ExomeData> exomeBlockData = new HashMap();

    int firstBlockIdx;

    int exomeOrigin;

    private int blockGap;

    public ExomeReferenceFrame(ReferenceFrame otherFrame, FeatureTrack referenceTrack) {
        super(otherFrame);
        init(referenceTrack);
    }


    public ExomeReferenceFrame(ReferenceFrame otherFrame, Map<String, List<Feature>> featureMap) {
        super(otherFrame);
        init(featureMap);
    }


    private void init(FeatureTrack geneTrack) {
        Genome genome = GenomeManager.getInstance().getCurrentGenome();
        for (Chromosome chromosome : genome.getChromosomes()) {
            String chr = chromosome.getName();
            List<Feature> features = geneTrack.getFeatures(chr, 0, chromosome.getLength());
            if (features != null && features.size() > 0) {
                processFeatures(chr, features);
            }

        }
    }

    private void init(Map<String, List<Feature>> featureMap) {
        for (String chr : featureMap.keySet()) {
            List<Feature> features = featureMap.get(chr);
            if (features.size() > 0) {
                processFeatures(chr, features);
            }

        }
    }

    private void processFeatures(String chr, List<Feature> features) {
        List<ExomeBlock> blocks = ExomeUtils.collapseTranscripts(features);
        List<Gene> genes = ExomeUtils.collapseToGenes(features);
        ExomeData exomeData = new ExomeData(blocks, genes);
        exomeBlockData.put(chr, exomeData);
    }

    /**
     * Move by the specified pixel amount.
     *
     * @param delta
     */
    @Override
    public void shiftOriginPixels(int delta) {

        if (exomeOrigin == 0 && delta < 0) return;

        double shiftBP = delta * getScale();
        exomeOrigin += shiftBP;
        if (exomeOrigin < 0) exomeOrigin = 0;

        // Find exome blocks that contains the new position.  We're assuming is very close to the current blocks.
        List<ExomeBlock> blocks = getBlocks(getChrName());
        ExomeBlock b = blocks.get(firstBlockIdx);
        int comp = b.compareExomePosition(exomeOrigin);
        if (comp > 0) {
            while (firstBlockIdx < blocks.size() - 1) {
                firstBlockIdx++;
                b = blocks.get(firstBlockIdx);
                if (b.compareExomePosition(exomeOrigin) <= 0) break;
            }
        } else if (comp < 0) {
            while (firstBlockIdx > 0) {
                firstBlockIdx--;
                b = blocks.get(firstBlockIdx);
                if (b.compareExomePosition(exomeOrigin) >= 0) break;
            }
        }

        // Find genomePosition
        double genomePosition = b.getGenomeStart() + (exomeOrigin - b.getExomeStart());

        setOrigin(genomePosition, true);
    }


    @Override
    public void setOrigin(double genomePosition, boolean repaint) {

        super.setOrigin(genomePosition, false);

        //super.setOrigin(genomePosition, false);
        List<ExomeBlock> blocks = getBlocks(chrName);
        firstBlockIdx = getIndexForGenomePosition(blocks, origin);
        ExomeBlock firstBlock = blocks.get(firstBlockIdx);

        exomeOrigin = firstBlock.getExomeStart() + (int) (origin - firstBlock.getGenomeStart());

        if (repaint) {
            IGV.getInstance().repaintDataAndHeaderPanels();
            IGV.getInstance().repaintStatusAndZoomSlider();
        }

    }

    @Override
    public void jumpTo(String chr, int start, int end) {

        setInterval(new Locus(chr, start, end));
        IGV.getInstance().repaintDataAndHeaderPanels();
        IGV.getInstance().repaintStatusAndZoomSlider();

    }

    /**
     * Jump to a specific locus (in genome coordinates).
     *
     * @param locus
     */
    @Override
    public void setInterval(Locus locus) {

        this.initialLocus = locus;
        this.chrName = locus.getChr();
        this.origin = locus.getStart();    // Genome locus
        int genomeEnd = locus.getEnd();

        List<ExomeBlock> blocks = getBlocks(chrName);
        firstBlockIdx = getIndexForGenomePosition(blocks, origin);
        ExomeBlock firstBlock = blocks.get(firstBlockIdx);

        exomeOrigin = origin > firstBlock.getGenomeEnd() ? firstBlock.getExomeEnd() :
                firstBlock.getExomeStart() + (int) (origin - firstBlock.getGenomeStart());

        int exomeEnd = Math.max(exomeOrigin + 40, genomeToExomePosition(genomeEnd));


        int bp = exomeEnd - exomeOrigin;
        int pw = widthInPixels <= 0 ? 1000 : widthInPixels;
        locationScale = ((double) bp) / pw;
        locationScaleValid = true;

        imputeZoom(exomeOrigin, exomeEnd);
    }

    @Override
    public void zoomTo(int newZoom, double newCenter) {

        newZoom = Math.max(0, Math.min(newZoom, maxZoom));
        double zoomFactor = Math.pow(2, newZoom - zoom);


        int currentBPLength = (int) (locationScale * widthInPixels);
        int delta = (int) (currentBPLength / (2 * zoomFactor));

        int exomeCenter = genomeToExomePosition((int) newCenter);
        exomeOrigin = exomeCenter - delta;

        origin = exomeToGenomePosition(exomeOrigin);
        locationScale /= zoomFactor;
        zoom = newZoom;

        IGV.getInstance().repaintDataAndHeaderPanels();
        IGV.getInstance().repaintStatusAndZoomSlider();

    }

    /**
     * Return the chromosome (genomic) position corresponding to the screen pixel position.
     *
     * @param screenPosition
     * @return
     */
    @Override
    public double getChromosomePosition(int screenPosition) {

        double exomePosition = exomeOrigin + getScale() * screenPosition;
        return exomeToGenomePosition((int) exomePosition);
    }

    @Override
    protected double getGenomeCenterPosition() {
        double centerExomePosition = exomeOrigin + getScale() * widthInPixels / 2;
        return exomeToGenomePosition((int) centerExomePosition);
    }

    @Override
    public double getEnd() {
        int exomeEnd = exomeOrigin + (int) (locationScale * widthInPixels);
        int genomeEnd = exomeToGenomePosition(exomeEnd);
        return genomeEnd;
    }

    public int genomeToExomePosition(int genomePosition) {

        ExomeData ed = exomeBlockData.get(chrName);
        if (ed == null) return -1;

        List<ExomeBlock> blocks = ed.getBlocks();
        int idx = getIndexForGenomePosition(blocks, genomePosition);
        ExomeBlock b = blocks.get(idx);

        if (genomePosition < b.getGenomeStart()) {
            return b.getExomeStart();
        } else {
            return genomePosition < b.getGenomeEnd() ?
                    b.getExomeStart() + (genomePosition - b.getGenomeStart()) :
                    b.getExomeEnd();
        }
    }

    public int exomeToGenomePosition(int exomePosition) {


        ExomeData ed = exomeBlockData.get(chrName);
        if (ed == null) return -1;

        List<ExomeBlock> blocks = ed.getBlocks();
        ExomeBlock b = getBlockAtExomePosition(blocks, exomePosition);
        if (b != null) {
            return b.getGenomeStart() + (exomePosition - b.getExomeStart());
        } else {
            // ?? Should be impossible if "exomePosition" is in bounds
            b = blocks.get(blocks.size() - 1);
            return b.getGenomeEnd();
        }
    }

    public List<ExomeBlock> getBlocks(String chr) {
        ExomeData exomeData = exomeBlockData.get(chr);
        return exomeData == null ? null : exomeData.blocks;
    }

    public List<Gene> getGenes(String chr) {
        ExomeData exomeData = exomeBlockData.get(chr);
        return exomeData == null ? null : exomeData.genes;

    }

    private ExomeBlock getBlockAtExomePosition(List<ExomeBlock> blocks, int exomePosition) {
        ExomeBlock key = new ExomeBlock(-1, -1, exomePosition, 1);
        int r = Collections.binarySearch(blocks, key, EXOME_POSITION_COMPARATOR);
        if (r >= 0) {
            return blocks.get(r);
        } else {
            return null;
        }
    }

    /**
     * Return the index to the last feature in the list with a start < the given position
     *
     * @param position
     * @param blocks
     * @return
     */
    public static int getIndexForGenomePosition(List<ExomeBlock> blocks, double position) {


        int startIdx = 0;
        int endIdx = blocks.size() - 1;

        while (startIdx != endIdx) {
            int idx = (startIdx + endIdx) / 2;
            double distance = blocks.get(idx).getGenomeStart() - position;
            if (distance <= 0) {
                startIdx = idx;
            } else {
                endIdx = idx;
            }
            if (endIdx - startIdx < 10) {
                break;
            }
        }

        if (blocks.get(endIdx).getGenomeStart() >= position) {
            for (int idx = endIdx; idx >= 0; idx--) {
                if (blocks.get(idx).getGenomeStart() <= position) {
                    return idx;
                }
            }
            return 0;
        } else {
            for (int idx = endIdx + 1; idx < blocks.size(); idx++) {
                if (blocks.get(idx).getGenomeStart() >= position) {
                    return idx - 1;
                }

            }
            return blocks.size() - 1;
        }
    }


    public List<ExomeBlock> getBlocks() {
        return getBlocks(getChrName());
    }

    public int getFirstBlockIdx() {
        return firstBlockIdx;
    }

    public boolean isExomeMode() {
        return true;
    }

    public int getExomeOrigin() {
        return exomeOrigin;
    }

    public int getBlockGap() {
        return 0; //blockGap;
    }


    public static class Gene implements NamedFeature {

        String name;
        String chr;
        int start;
        int end;

        Gene(NamedFeature f) {
            name = f.getName();
            chr = f.getChr();
            start = f.getStart();
            end = f.getEnd();
        }

        @Override
        public String getChr() {
            return chr;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getStart() {
            return start;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getEnd() {
            return end;  //To change body of implemented methods use File | Settings | File Templates.
        }


        @Override
        public String getName() {
            return name;
        }

        public void expand(Feature f) {
            //if(!chr.equals(f.getChr())) throw error
            start = Math.min(start, f.getStart());
            end = Math.max(end, f.getEnd());
        }

    }

    public static class ExomeData {
        private List<ExomeBlock> blocks;
        private List<Gene> genes;

        ExomeData(List<ExomeBlock> block, List<Gene> genes) {
            this.blocks = block;
            this.genes = genes;
        }

        public List<ExomeBlock> getBlocks() {
            return blocks;
        }

        public List<Gene> getGenes() {
            return genes;
        }
    }


    private static final Comparator<ExomeBlock> GENOME_POSITION_COMPARATOR = new Comparator<ExomeBlock>() {
        public int compare(ExomeBlock o1, ExomeBlock o2) {
            int genomeStart2 = o2.getGenomeStart();
            int genomeStart1 = o1.getGenomeStart();
            if (genomeStart2 >= genomeStart1 && o2.getGenomeEnd() <= o1.getGenomeEnd()) {
                return 0;
            } else {
                return genomeStart1 - genomeStart2;
            }
        }
    };

    private static final Comparator<ExomeBlock> EXOME_POSITION_COMPARATOR = new Comparator<ExomeBlock>() {
        public int compare(ExomeBlock o1, ExomeBlock o2) {
            int exomeStart2 = o2.getExomeStart();
            int exomeStart1 = o1.getExomeStart();
            if (exomeStart2 >= exomeStart1 && o2.getExomeEnd() <= o1.getExomeEnd()) {
                return 0;
            } else {
                return exomeStart1 - exomeStart2;
            }
        }
    };

}
