/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.data.seg;

//~--- non-JDK imports --------------------------------------------------------

import org.broad.igv.Globals;
import org.broad.igv.data.DataSource;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.track.TrackType;
import org.broad.igv.track.WindowFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author jrobinso
 */
public class SegmentedDataSource implements DataSource {

    /**
     * Identifies the track.  Often a sample name.
     */
    String trackIdentifier;
    SegmentedDataSet dataset;
    WindowFunction statType;

    /**
     * Constructs ...
     *
     * @param trackIdentifier
     * @param dataset
     */
    public SegmentedDataSource(String trackIdentifier, SegmentedDataSet dataset) {
        this.trackIdentifier = trackIdentifier;
        this.dataset = dataset;
    }

    public TrackType getTrackType() {
        return dataset.getType();
    }


    public void setWindowFunction(WindowFunction statType) {
        this.statType = statType;
       
    }

    protected void p(String s) {
         Logger.getLogger("SegmentedDataSource").info(s);
    }

    public boolean isLogNormalized() {
        return dataset.isLogNormalized();
    }


    public double getDataMax() {
        return dataset.getDataMax(Globals.CHR_ALL);
    }


    public double getDataMin() {
        return dataset.getDataMin(Globals.CHR_ALL);
    }


    public double getMedian(int zoom, String chr) {
        return 1.0;
    }

    private List<LocusScore> getSegments(String chr) {
        //p("getSegments for "+chr);
        return dataset.getSegments(trackIdentifier, chr);

    }


    @Override
    public List<LocusScore> getSummaryScoresForRange(String chr, int startLocation,
                                                     int endLocation, int zoom) {
        if (chr.equals(Globals.CHR_ALL)) {
      //      p("Getting wholegenome scores");
            return getWholeGenomeScores();
        }
        return getSegments(chr);
    }


    public List<LocusScore> getWholeGenomeScores() {
       // Logger.getLogger(getClass()).info("Getting whole genome scores");
        return dataset.getWholeGenomeScores(trackIdentifier);
    }


    public Collection<WindowFunction> getAvailableWindowFunctions() {
        Collection<WindowFunction> res = new ArrayList<WindowFunction>();
        res.add(WindowFunction.noRefLine);
        return res;
    }

    
    public WindowFunction getWindowFunction() {
        return statType;
    }
}
