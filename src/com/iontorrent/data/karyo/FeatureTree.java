/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import com.iontorrent.data.karyo.filter.KaryoFilter;
import java.util.ArrayList;
import java.util.List;
import org.broad.igv.track.FeatureTrack;
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

    public FeatureTree(KaryoTrack track, FeatureTrack source, Chromosome chr) {
        this(track, source, chr, DEFAULT_BUCKETS);
    }

    public FeatureTree(KaryoTrack track, FeatureTrack source, Chromosome chr, int nrbuckets) {
        super(source, chr.getName(), 0, (int) chr.getLength(), nrbuckets);
        this.track = track;
    }

    public void loadFeatures() {
        if (!chr.startsWith("chr")) {
            chr = "chr" + chr;
        }
        //   p("Loading features of " + chr + " from 0 - " + getEnd());
        List<Feature> feat = source.getFeatures(chr, 0, getEnd());

        if (feat.isEmpty()) {
            p("loadFeatures: No features found");
        } else {
           

            int total = 0;
            for (Feature f : feat) {
                if (sampleFeature == null) {
                    sampleFeature = f;
                    track.setSampleafeture(f);
                    track.setMetainfo(FeatureMetaInfo.createMetaInfo(sampleFeature));
                }
                total++;
                this.addFeature(new KaryoFeature(f));
                if (total < FeatureMetaInfo.MAX_NR_FEATURES) {
                    if (total < 10) {
                        //   p("Populating metainfo with feature "+f);

                        track.getMetaInfo().populateMetaInfo(f);
                    }
                }
            }
             if (source instanceof FakeCnvTrack) {
                p("+++++ Adding " + feat.size() + " CNV features of chr " + chr + " to feature tree " + name+", total is noww="+total);
            }
        }

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
}
