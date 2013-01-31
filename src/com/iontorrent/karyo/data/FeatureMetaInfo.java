/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.broad.igv.data.seg.Segment;
import org.broad.igv.feature.BasicFeature;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 * information about fields and values
 *
 * @author Chantal Roth
 */
public abstract class FeatureMetaInfo {

    public static int MAX_NR_FEATURES = 10000;
    protected ArrayList<String> atts;
    protected HashMap<String, ArrayList<String>> attvaluesmap;
    protected HashMap<String, Range> attrangemap;

    public FeatureMetaInfo() {
        atts = new ArrayList<String>();
        attvaluesmap = new HashMap<String, ArrayList<String>>();
        attrangemap = new HashMap<String, Range>();
    }

    public static class Range {

        public double min;
        public double max;
        int nr;

        public Range() {
            min = Double.MAX_VALUE;
            max = Double.MIN_VALUE;
        }

        public void add(double d) {
            if (d > max) {
                max = d;
            }
            if (d < min) {
                min = d;
            }
            nr++;
        }

        public double getMiddle() {
            return (max - min) / 2;
        }
        public String toString() {
            return min+"-"+max;
        }
    }

    public static FeatureMetaInfo createMetaInfo(Feature samplefeature) {
        p("Creating metainfo for feature " + samplefeature.getClass().getName());
        if (samplefeature instanceof Variant) {
            return new VariantMetaInfo();
        } else if (samplefeature instanceof Segment) {
            Segment f = (Segment) samplefeature;            
            p("Got type:" + f.getDescription() + ", creating SegmentMetaInfo");
            return new SegmentMetaInfo();
        } else if (samplefeature instanceof LocusScore) {
            LocusScore f = (LocusScore) samplefeature;
            p("Got type:" + f+ ", creating LocusScoreMetaInfo");
            return new LocusScoreMetaInfo();
        } else if (samplefeature instanceof BasicFeature) {

            BasicFeature f = (BasicFeature) samplefeature;
            p("Got type:" + f.getType());
            return new BedMetaInfo();
        } else {
            err("Don't know what meta info to use for " + samplefeature.getClass().getName());
            return null;
        }
    }

    public abstract void populateMetaInfo(Feature f);

    public void addAtt(String att) {
        if (!atts.contains(att)) {
            atts.add(att);
        }
    }

    public void addAtt(String att, double value) {
        Range r = attrangemap.get(att);
        if (r == null) {
            r = new Range();
            attrangemap.put(att, r);
        }
        p("adding "+value+ " for "+att);
        r.add(value);
    }

    public void addAtt(String att, String value) {
        // check fo rnumber
        try {
            Double d = Double.parseDouble(value);
            addAtt(att, d.doubleValue());
            return;
        } catch (Exception e) {
        }
        ArrayList<String> vals = attvaluesmap.get(att);
        if (vals == null) {
            vals = new ArrayList<String>();
            attvaluesmap.put(att, vals);
        }
        if (!vals.contains(value)) {
            vals.add(value);
        }
    }

    public void populateMetaInfo(FeatureTree tree) {
        int nrprocessed = 0;
        // we collect data for say at least 1000 features
        for (int b = 0; b < tree.getNrbuckets(); b++) {
            FeatureTreeNode node = tree.getNodeForBucket(b);
            if (node != null && node.getTotalNrChildren() > 0) {
                for (Feature f : node.getAllFeatures()) {
                    if (f != null) {
                        populateMetaInfo(f);
                        nrprocessed++;
                        if (nrprocessed > MAX_NR_FEATURES) {
                            return;
                        }

                    }
                };

            }
        }
    }

    public ArrayList<String> getAttributes() {
        return atts;
    }

    public boolean isRange(String name) {
        return attrangemap.keySet().contains(name);
    }

    public ArrayList<String> getValuesForAttribute(String att) {
        return attvaluesmap.get(att);
    }

    public Range getRangeForAttribute(String att) {
        return attrangemap.get(att);
    }

    private static void p(String msg) {
        Logger.getLogger("FeatureMetaInfo").info(msg);
    }

    private static void err(String msg) {
        Logger.getLogger("FeatureMetaInfo").warning(msg);
    }
}
