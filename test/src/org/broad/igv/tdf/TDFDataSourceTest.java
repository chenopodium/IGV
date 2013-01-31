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

package org.broad.igv.tdf;

import org.broad.igv.AbstractHeadlessTest;
import org.broad.igv.Globals;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.ChromosomeCoordinate;
import org.broad.igv.util.ResourceLocator;
import org.broad.igv.util.TestUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

/**
 * User: jacob
 * Date: 2012-Dec-28
 */
public class TDFDataSourceTest extends AbstractHeadlessTest {

    @Test
    public void testWholeGenomeScores() throws Exception {


        Map<String, Double> expectedValues = new HashMap<String, Double>();
        String[] chrNames = {"chr1", "chr2", "chr3", "chr4", "chr5", "chr6", "chr7", "chr8", "chr9", "chr10", "chr11",
                "chr12", "chr13", "chr14", "chr15", "chr16", "chr17", "chr18", "chr19", "chr20", "chr21", "chr22",
                "chrX", "chrY", "chrM"};
        double value = 1;
        for (String chr : chrNames) {
            expectedValues.put(chr, value);
            value++;
        }

        String testFile = TestUtils.DATA_DIR + "tdf/wholeGenomeTest.tdf";
        TDFReader reader = new TDFReader(new ResourceLocator(testFile));
        TDFDataSource ds = new TDFDataSource(reader, 0, "", genome);

        List<LocusScore> wgScores = ds.getSummaryScores(Globals.CHR_ALL, 0, Integer.MAX_VALUE, 0);
        for (LocusScore score : wgScores) {

            int genomeStart = score.getStart();
            ChromosomeCoordinate stCoord = genome.getChromosomeCoordinate(genomeStart);
            ChromosomeCoordinate endCoord = genome.getChromosomeCoordinate(score.getEnd());

            // Skip "edge" bins that include data from 2 chromosomes.  We don't know the expected value for these.
            if (stCoord.getChr().equals(endCoord.getChr())) {
                String chrName = stCoord.getChr();
                double ev = expectedValues.get(chrName);
                assertEquals(ev, score.getScore(), 0.001);
            }

        }

    }
}
