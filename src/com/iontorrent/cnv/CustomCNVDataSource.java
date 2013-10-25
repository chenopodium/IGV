/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import java.util.List;
import java.util.logging.Logger;
import org.broad.igv.data.seg.SegmentedDataSet;
import org.broad.igv.data.seg.SegmentedDataSource;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.track.WindowFunction;

/**
 *
 * @author Chantal
 */
public class CustomCNVDataSource extends SegmentedDataSource {
    
    WindowFunction func;
            
    public CustomCNVDataSource(String trackIdentifier, SegmentedDataSet dataset) {
        super(trackIdentifier, dataset);
    }
   
    
    @Override
    public double getMedian(int zoom, String chr) {
        return 0.0;
    }
}
