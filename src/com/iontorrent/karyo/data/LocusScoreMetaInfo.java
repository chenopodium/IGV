/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.broad.igv.feature.BasicFeature;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.util.collections.MultiMap;

import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class LocusScoreMetaInfo extends FeatureMetaInfo {

    @Override
    public void populateMetaInfo(Feature f) {
        if (!(f instanceof BasicFeature)) {
            return;
        }
        LocusScore var = (LocusScore) f;
        super.addAtt("Score", var.getScore());
    }

    private void p(String s) {
        Logger.getLogger("LocusScoreMetaInfo").info(s);
    }
}
