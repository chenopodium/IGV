/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.karyo.filter.LocusScoreFilter;
import com.iontorrent.karyo.filter.VariantFrequencyFilter;
import com.iontorrent.karyo.filter.VariantAttributeFilter;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class FilterManager {
    
    private ArrayList<KaryoFilter> allfilters;
    
    private static FilterManager manager;
    
    public static FilterManager getManager() {
        if (manager == null) manager = new FilterManager();
        return manager;
    }
    private FilterManager() {
        allfilters = new  ArrayList<KaryoFilter>();
        allfilters.add(new VariantAttributeFilter());
     //   allfilters.add(new VariantFrequencyFilter());
        allfilters.add(new LocusScoreFilter());
    }
    public ArrayList<KaryoFilter> getAllFilters() {
        return allfilters;
    }
    public ArrayList<KaryoFilter> getFiltersFor(Feature f) {
        //p("getFiltersFor "+f);
        if (f == null) {
            p("No sample features");
            return null;
        }
      //  p("Getting filters for feature "+f.getClass().getName()+" out of "+allfilters.size()+" possible filters");
        ArrayList<KaryoFilter> res = new ArrayList<KaryoFilter>();
        for (KaryoFilter fil: allfilters) {
           // p("Checking filters for feature "+f);
            // clone !!
            if (fil.isForFeature(f)) res.add(fil.copy());
        }
        return res;
    }
     private void p(String msg) {
        Logger.getLogger("FilterManager").info(msg);
    }
     private void err(String msg) {
        Logger.getLogger("FilterManager").warning(msg);
    }
    
}
