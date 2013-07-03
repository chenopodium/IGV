/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import org.broad.igv.data.seg.SegmentedDataSet;
import org.broad.igv.data.seg.SegmentedDataSource;

/**
 *
 * @author Chantal
 */
public class CustomCNVDataSource extends SegmentedDataSource {
    public CustomCNVDataSource(String trackIdentifier, SegmentedDataSet dataset) {
        super(trackIdentifier, dataset);
    }
    
    @Override
    public double getMedian(int zoom, String chr) {
        return 0.0;
    }
}
