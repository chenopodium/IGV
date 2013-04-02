/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.logging.Logger;
import org.broad.igv.data.seg.Segment;

import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class SegmentMetaInfo extends FeatureMetaInfo {

    public SegmentMetaInfo(String name){
        super(name);
    }
    @Override
    public void populateMetaInfo(Feature f) {
        if (!(f instanceof Segment)) {
            p("Feature is not a segement: "+f.getClass().getName());
            return;
        }
        Segment s = (Segment) f;
        super.addAtt("Score", s.getScore());
    }

    private void p(String s) {
        Logger.getLogger("SegmentMetaInfo").info(s);
    }
}
