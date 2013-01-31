/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.broad.igv.feature.BasicFeature;
import org.broad.igv.util.collections.MultiMap;

import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class BedMetaInfo extends FeatureMetaInfo{

    @Override
    public void populateMetaInfo(Feature f) {
        if (!(f instanceof BasicFeature)) return;
        BasicFeature var = (BasicFeature)f;
        boolean show = Math.random()>0.99;
        
        MultiMap<String, String> map = var.getAttributes();
        if (map != null && map.size()>0) {
            Iterator<String> it = map.values().iterator();
            while (it.hasNext()) {
                String name = it.next();
                super.addAtt(name);
                if (show) p("Bed has att "+name+":"+var.getType()+","+var.toString());
                Object val = map.get(name);
                if (val != null) {
                    super.addAtt(name, val.toString());
                }
            }
        }
        else{
            if (show) p("BasicFeature BED has no attributes: "+var.getType()+","+var.toString());
        }
        
    }
    private void p(String s) {
        Logger.getLogger("BedMetaInfo").info(s);
    }
}
