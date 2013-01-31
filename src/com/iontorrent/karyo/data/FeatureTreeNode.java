/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.utils.ErrorHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.sam.CoverageTrack;
import org.broad.igv.track.AbstractTrack;
import org.broad.igv.track.DataSourceTrack;
import org.broad.igv.track.FeatureTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class FeatureTreeNode {

    private static final int MB = 1000000;
    private static final int SMALLEST_GENOMIC_REGION = 100 * MB;
    protected int totalNrChildren;
    private int start;
    private int end;
    protected String chr;
    private FeatureTreeNode nodes[];
    private int nrbuckets;
    protected List<KaryoFeature> features;
    protected AbstractTrack source;
    private boolean compact;
    private int MAX_FEATURES_BEFORE_COMPACT = 100;
    // filter
    private int nrFilterPassed;

    public FeatureTreeNode(AbstractTrack source, String chr, int start, int end, int nrbuckets) {
        this.nrbuckets = nrbuckets;
        this.source = source;
        this.start = start;
        this.chr = chr;
        if (!chr.startsWith("chr")) {
            this.chr = "chr" + chr;
        }
        this.end = end;
        if (nrbuckets > 0) {
            nodes = new FeatureTreeNode[nrbuckets];
        }
    }

    public List<KaryoFeature> getFilteredFeatures(KaryoFilter filter) {
        return getFilteredFeatures(filter, false);
    }

    public List<KaryoFeature> getFilteredFeatures(KaryoFilter filter, boolean debug) {
        if (!filter.isValid()) {
            if (debug) {
                p("getFilteredFeatures(filter): Filter " + filter + " is not valied, returning null");
            }
            return null;
        }
        if (debug) {
            p("getFilteredFeatures(filter): Filter " + filter + " is valid");
        }
        List<KaryoFeature> res = new ArrayList<KaryoFeature>();

        if (this.isLeaf()) {
            this.loadFeatures();
            for (KaryoFeature f : features) {
                if (filter.filter(f)) {
                    res.add(f);
                }
            }
            if (this.compact) {
                features = null;
            }
        } else {
            for (FeatureTreeNode node : getNodes()) {
                if (node != null) {
                    List<KaryoFeature> subres = node.getFilteredFeatures(filter);
                    if (subres != null) {
                        res.addAll(subres);
                    }
                }
            }
        }
        if (debug) {
            p("getFilteredFeatures(filter): Filter " + filter + " is valid, got " + res.size() + " filtered features for node " + this);
        }
        return res;
    }

    @Override
    public String toString() {
        return this.chr + " node " + this.start + "-" + end + ", features:" + this.totalNrChildren;
    }

    public int getNrFilterPassed() {
        return nrFilterPassed;
    }

    public int filter(KaryoFilter filter) {
        nrFilterPassed = 0;
        if (!filter.isValid()) {
            return 0;
        }
        if (this.isLeaf()) {
            this.loadFeatures();
            //  p("Filtering "+features.size()+" features");
            for (KaryoFeature f : features) {
                if (filter.filter(f)) {
                    nrFilterPassed++;
                }
            }
            if (this.compact) {
                features = null;
            }
        } else {
            for (FeatureTreeNode node : getNodes()) {
                if (node != null) {
                    nrFilterPassed += node.filter(filter);
                }
            }
        }

        return nrFilterPassed;
    }

    public boolean isLeaf() {
        return getNrbuckets() <= 0;
    }

    public int getTotalNrChildren() {
        return this.totalNrChildren;
    }

//    protected List loadFeatures_r() {
//        List res = null;
//        
//        if (source instanceof FakeCnvTrack) {
//            p("Loading features of FakeCnvTrack for node from " + getStart() + "-" + getEnd());
//        }
//        else if (source instanceof CoverageTrack) {
//            CoverageTrack ctrack = (CoverageTrack)source;
//            int zoom = 1;
//            res = ctrack.getDataSource().getSummaryScoresForRange(chr, start, end, zoom);
//            
//        }
//        else if (source instanceof DataSourceTrack) {
//            DataSourceTrack ctrack = (DataSourceTrack)source;
//            int zoom = 1;
//            res = ctrack.getSummaryScores(chr, start, end, zoom);
//            
//        }
//        if (source instanceof FeatureTrack) {
//            p(source.getName()+": Loading features from feature track");
//            FeatureTrack fsource = (FeatureTrack)source;
//            res = fsource.getFeatures(chr, getStart(), getEnd());
//        }
//        return res;
//    }
    private void loadFeatures() {
        if (features != null) {
            return;
        }
        features = new ArrayList<KaryoFeature>();
        List res = loadFeatures_r(chr, start, end);
        if (res != null) {
            for (Object o : res) {
                Feature f = (Feature) o;
                features.add(new KaryoFeature(f));
            }
        }
        
    }

    protected List loadFeatures_r(String chr, int start, int end) {
        int zoom = 3;
        if (!chr.startsWith("chr")) {
            chr = "chr" + chr;
        }
        p(source.getName() + ": loadFeatures");

        List res = null;
        if (source instanceof CoverageTrack) {
            CoverageTrack ctrack = (CoverageTrack) source;
            res = ctrack.getDataSource().getSummaryScoresForRange(chr, start, end, zoom);
            p("Loading coverage info from CoverageTrack track NOT DONE");
            features = new ArrayList<KaryoFeature>();


        } else if (source instanceof DataSourceTrack) {
            p("Loading data for DataSourceTrack ");
            DataSourceTrack ctrack = (DataSourceTrack) source;
            res = ctrack.getSummaryScores(chr, start, end, zoom);

        } else if (source instanceof FeatureTrack) {
            p("Loading features from feature track");
            FeatureTrack fsource = (FeatureTrack) source;
            res = fsource.getFeatures(chr, getStart(), getEnd());
        } else if (source instanceof FakeTrack) {
            p("Loading FeatureTrack");
            FakeTrack fsource = (FakeTrack)source;
            res = fsource.getFeatures(chr, 0, chr.length());
        
        } else {
            err("========= Got abstract track " + source.getClass().getName() + ", not sure how to load anything");
        }
        return res;
    }

    public void addFeature(KaryoFeature f) {
        if (isLeaf()) {
            if (compact) {
                // do nothing, just count!
            } else {
                if (features == null) {
                    features = new ArrayList<KaryoFeature>();
                }
                if (features.size() > MAX_FEATURES_BEFORE_COMPACT) {
                    compact = true;
                    features = null;
                    // they will be loaded when needed!
                } else {
                    features.add(f);
                }

            }
        } else {
            int b1 = getBucket(f.getStart());
            int b2 = getBucket(f.getEnd());

            for (int b = b1; b <= b2 && b < nodes.length; b++) {
                if (b >= 0) {
                    if (getNodes()[b] == null) {
                        int bucketsize = (getEnd() - getStart()) / getNrbuckets();
                        int bucketstart = getStart() + (b) * bucketsize;
                        int bucketend = getStart() + (b + 1) * bucketsize;
                        int childbuckets = getNrbuckets();
                        if (bucketsize < SMALLEST_GENOMIC_REGION) {
                            // if small genome region, just store all features there and do not recurse any further                    
                            childbuckets = 0; // leaf!
                        }
                        nodes[b] = new FeatureTreeNode(source, chr, bucketstart, bucketend, childbuckets);
                    }
                    getNodes()[b].addFeature(f);
                }
            }
        }
        totalNrChildren++;
    }

    public int getMaxNodeChildren() {
        int max = 0;
        for (int b = 0; b < this.nrbuckets; b++) {
            if (nodes[b] != null) {
                int nr = nodes[b].getTotalNrChildren();
                if (nr > max) {
                    max = nr;
                }
            }
        }
        return max;
    }

    public FeatureTreeNode getNodeForBucket(int b) {
        if (b < 0 || b >= nodes.length) {
            //    p("Location out of bounds: "+location+"-> bucket "+b);
            return null;
        }
        return getNodes()[b];
    }

    public FeatureTreeNode getNodeForLocation(int location) {
        int b = getBucket(location);
        if (b < 0 || b >= nodes.length) {
            //    p("Location out of bounds: "+location+"-> bucket "+b);
            return null;
        }
        return getNodes()[getBucket(location)];
    }

    private int getBucket(int location) {
        return (int) ((double) (location - getStart()) / (double) (getEnd() - getStart()) * (double) getNrbuckets());
    }

    public List<KaryoFeature> getFeaturesAt(int location, int errortolerance) {
        List<KaryoFeature> res = new ArrayList<KaryoFeature>();
        if (isLeaf()) {
            // find feat
            if (features != null && features.size() > 0) {
                p("Finding which of " + features.size() + " features is at " + location + ", start-end=" + start + "-" + end);
                if (location < start || location > end) {
                    p("Should not be in this tree node! Location off");
                }
                for (KaryoFeature f : features) {
                    // just appoximately, such as for tool tip text
                    if (f.getStart() - errortolerance <= location && f.getEnd() + errortolerance >= location) {
                        // p("   checking " + f.getStart() + "-" + f.getEnd());
                        res.add(f);
                    }
                }
                return res;
            } else {
                return null;
            }
        } else {
            FeatureTreeNode node = getNodeForLocation(location);
            if (node != null) {
                return node.getFeaturesAt(location, errortolerance);
            }
            //else p("Got no node at "+location);
        }
        return null;
    }

    public List<KaryoFeature> getAllFeatures() {
        return getAllFeatures(false);
    }

    public List<KaryoFeature> getAllFeatures(boolean debug) {
        if (isLeaf()) {
            if (features == null && this.totalNrChildren > 0) {
                loadFeatures();
            }
            if (this.getTotalNrChildren() != features.size()) {
                err("getAllFeatures: Total nr children " + getTotalNrChildren() + ", but loaded features:" + features.size());
            }
            //   if (debug) p("getAllFeatures: leaf. "+this);
            return features;
        }

        List<KaryoFeature> childfeatures = new ArrayList<KaryoFeature>();
        // if (debug) p("getAllFeatures: Not a leaf. Getting all sub children. "+this);
        for (FeatureTreeNode node : getNodes()) {
            if (node != null) {
                childfeatures.addAll(node.getAllFeatures());
            }
        }

        return childfeatures;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public int getEnd() {
        return end;
    }

    /**
     * @return the nodes
     */
    public FeatureTreeNode[] getNodes() {
        return nodes;
    }

    /**
     * @return the nrbuckets
     */
    public int getNrbuckets() {
        return nrbuckets;
    }

    private void p(String s) {
        //Logger.getLogger("FeatureTreeNode").info(s);
        System.out.println("FeatureTreeNode: " + s);
    }

    private void err(String s) {
        Logger.getLogger("FeatureTreeNode").warning(s);
        Exception e = new Exception("FATAL ERROR IN FeatureTreeNode: " + s);
        Logger.getLogger("FeatureTreeNode").warning(ErrorHandler.getString(e));
        System.exit(0);
    }
}
