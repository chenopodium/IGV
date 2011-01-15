/*
 * Copyright (c) 2007-2011 by The Broad Institute, Inc. and the Massachusetts Institute of
 * Technology.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.sam;

import org.apache.commons.math.stat.StatUtils;

import java.util.Iterator;

/**
 * @author jrobinso
 * @date Jan 14, 2011
 */
public class PairedEndStats {

    double averageInsertSize;
    double insertSizeStdev;
    private static final int MAX_PAIRS = 10000;

    public PairedEndStats(double averageInsertSize, double insertSizeStdev) {
        this.averageInsertSize = averageInsertSize;
        this.insertSizeStdev = insertSizeStdev;
    }

    public static PairedEndStats compute(Iterator<Alignment> alignments) {


        double[] insertSizes = new double[MAX_PAIRS];
        int nPairs = 0;
        int ff;
        int fr;
        int rf;
        int rr;

        while (alignments.hasNext()) {
            Alignment al = alignments.next();

            if (al.isProperPair()) {
                insertSizes[nPairs] = al.getInferredInsertSize();
                if (al instanceof SamAlignment) {
                    SamAlignment sa = (SamAlignment) al;
                    if (!sa.isSmallInsert()) {
                        if (sa.isFirstInPair()) {
                            sa.isNegativeStrand();
                            sa.getMate().isNegativeStrand();
                        }
                    }
                }
                nPairs++;
            }


            if (nPairs >= MAX_PAIRS) {
                break;
            }

        }


        double mean = StatUtils.mean(insertSizes, 0, nPairs);
        double stdDev = StatUtils.variance(insertSizes, 0, nPairs);

        PairedEndStats stats = new PairedEndStats(mean, stdDev);

        return stats;


    }
}
