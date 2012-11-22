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

package org.broad.igv.hic.track;

import org.broad.igv.Globals;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author jrobinso
 *         Date: 9/14/12
 *         Time: 8:49 AM
 */
public class HiCFragmentAxis implements HiCGridAxis {

    Bin[] bins;
    double averageBinSize;
    int igvZoom;

    int[] sites;


    /**
     * @param bins ordered by start position.  Its assumed bins are contiguous, no gaps and no overlap.
     */
    public HiCFragmentAxis(Bin[] bins) {

        this.bins = bins;

        Bin lastBin = bins[bins.length - 1];
        double chrLength = lastBin.start + lastBin.width;

        averageBinSize = bins.length == 0 ? 0 : (chrLength / bins.length);

        // Compute an approximate igv zoom level

        igvZoom = (int) (Math.log((chrLength / 700) / averageBinSize) / Globals.log2);


    }


    @Override
    public int getGenomicStart(int binNumber) {

        return bins[binNumber].start;

    }

    @Override
    public int getGenomicEnd(int binNumber) {
        Bin b = bins[binNumber];
        return b.start + b.width;
    }

    @Override
    public int getGenomicMid(int binNumber) {
        return bins[binNumber].start + bins[binNumber].width / 2;
    }


    @Override
    public int getIGVZoom() {
        return igvZoom;
    }


    /**
     * Return fragment (bin) that this position lies on.  Fragment 0 means position < sites[0].
     * Fragment 1 means position >= sites[0] and < sites[1].
     *
     * @param position The genome position to search for within that array
     * @return The fragment location such that position >= sites[retVal-1] and position <  sites[retVal]
     */    @Override
    public int getBinNumberForGenomicPosition(int position) {
        int lo = 0;
        int hi = sites.length - 1;
        while (lo <= hi) {
            // Base case - found range
            int mid = lo + (hi - lo) / 2;

            if (position > sites[mid]) lo = mid + 1;
            else if (position < sites[mid]) hi = mid - 1;
            else return mid + 1;
        }
        return lo;
    }

    @Override
    public int getBinCount() {
        return bins.length;
    }



    public static class Bin {

        public Bin(int start, int width) {
            this.start = start;
            this.width = width;
        }

        int start;
        int width;

        public boolean contains(int genomePosition) {
            return genomePosition >= start && genomePosition < (start + width);
        }
    }

}
