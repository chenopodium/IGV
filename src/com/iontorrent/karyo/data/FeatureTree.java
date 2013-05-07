/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.filter.KaryoFilter;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.broad.igv.track.AbstractTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class FeatureTree extends FeatureTreeNode {

    static final int DEFAULT_BUCKETS = 200;
    private String name;
    private ArrayList<KaryoFilter> filters;
    private Feature sampleFeature;
    private KaryoTrack track;

    public FeatureTree(KaryoTrack track, AbstractTrack source, Chromosome chr) {
        this(track, source, chr, DEFAULT_BUCKETS);
    }

    public FeatureTree(KaryoTrack track, AbstractTrack source, Chromosome chr, int nrbuckets) {
        super(source, chr.getName(), 0, (int) chr.getLength(), nrbuckets);
        this.track = track;
    }

    public AbstractTrack getSource() {
        return track.getTrack();
    }
    public void loadFeatures() {
      //  p("========= loading ALL features for " + track.getTrackDisplayName() + ", " + track.getTrack().toString() + ", " + track.getTrack().getAttributeValue("DATA_FILE"));

        this.totalNrChildren = 0;
        if (!chr.startsWith("chr")) {
            chr = "chr" + chr;
        }
        //      p("Loading features of " + chr + " from 0 - " + getEnd());
        List res = super.loadFeatures_r(chr, 0, chr.length());


        if (res == null || res.isEmpty()) {
//            p(" !!!!!!!! loadFeatures: Nothing found for " + track.getTrackDisplayName());
        } else {
            int total = 0;
            int totalwithatts = 0;
            //   p("Got "+res.size()+" items");
            for (int i = 0; i < res.size(); i++) {
                Object obj = res.get(i);
                Feature f = (Feature) obj;
                if (f != null) {
                    if (sampleFeature == null) {
                        sampleFeature = f;
                        track.setSampleafeture(f);
                        track.setMetainfo(FeatureMetaInfo.createMetaInfo(track, sampleFeature));
                    }
                    total++;
                    this.addFeature(new KaryoFeature(f));
                    if (total < FeatureMetaInfo.MAX_NR_FEATURES) {
                        if (total < 100000) {
                            //   p("Populating metainfo with feature "+f);
                            if (f instanceof KaryoFeature) {
                                track.getMetaInfo().populateMetaInfo(((KaryoFeature) f).getFeature());
                            } else {
                                track.getMetaInfo().populateMetaInfo(f);
                            }

                        }
                    }
                }
            }
        }
        // p("LOADING DONE. Found "+this.getTotalNrChildren()+ " on the entire chromosome");
    }

    public Feature getSampleFeature() {
        return sampleFeature;
    }

    public List<KaryoFeature> getAllFeatures(int location) {
        FeatureTreeNode node = getNodeForLocation(location);
        return node.getAllFeatures();
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<KaryoFilter> getFilters() {
        return filters;
    }

    public KaryoFilter getFilter() {
        if (filters != null && filters.size() > 0) {
            return filters.get(0);
        } else {
            return null;
        }
    }

    public boolean hasFilter() {
        return getFilter() != null;
    }

    public int filter() {
        int filtered = 0;
        if (filters != null) {
            for (KaryoFilter f : filters) {
                if (f.isInitialized() && f.isValid()) {

                    filtered = this.filter(f);
                    // p("Got "+filtered+" features");
                    p("Filtering tree by " + f.getDescription() + ", got " + filtered + " filtered features");
                    // use AND.... not done yet
                }
            }
        }
        return filtered;
    }

    public void addFilter(KaryoFilter filter) {
        if (filters == null) {
            filters = new ArrayList<KaryoFilter>();
        }
        filters.add(filter);
    }

    private void p(String msg) {
        // Logger.getLogger("FeatureTree").info(msg);
        System.out.println("FeatureTree: " + msg);
    }

    private void err(String msg) {
        Logger.getLogger("FeatureTree").error(msg);
        System.err.println("FeatureTree: ERROR: " + msg);
    }
}
