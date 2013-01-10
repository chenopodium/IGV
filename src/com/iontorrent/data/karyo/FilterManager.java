/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import com.iontorrent.data.karyo.filter.KaryoFilter;
import com.iontorrent.data.karyo.filter.VariantFrequencyFilter;
import com.iontorrent.data.karyo.filter.VariantAttributeFilter;
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
        allfilters.add(new VariantFrequencyFilter());
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
        ArrayList<KaryoFilter> res = new ArrayList<KaryoFilter>();
        for (KaryoFilter fil: allfilters) {
           // p("Checking filters for feature "+f);
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
