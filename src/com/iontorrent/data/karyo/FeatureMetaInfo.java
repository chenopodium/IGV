/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 * information about fields and values
 * @author Chantal Roth
 */
public abstract class FeatureMetaInfo {
    
    public static int MAX_NR_FEATURES = 10000;
    
    
    protected ArrayList<String> atts;
    protected HashMap<String, ArrayList<String>> attvaluesmap; 
    
    public FeatureMetaInfo() {
        atts = new ArrayList<String>();
        attvaluesmap = new HashMap<String, ArrayList<String>> ();
    }   
    
    public static FeatureMetaInfo createMetaInfo(Feature samplefeature) {
        if (samplefeature instanceof Variant) {
            return new VariantMetaInfo();
        }
        else {
          err("Don't know what meta info to use for "+samplefeature.getClass().getName());
          return null;
        }
    }
    
    public abstract void populateMetaInfo(Feature f) ;
    
    public void addAtt(String att) {
        if (!atts.contains(att)) atts.add(att);
    }
    public void addAtt(String att, String value) {
        ArrayList<String> vals =attvaluesmap.get(att);
        if (vals == null) {
            vals = new ArrayList<String>();
            attvaluesmap.put(att, vals);
        }
        if (!vals.contains(value)) vals.add(value);
    }
    public void populateMetaInfo(FeatureTree tree) {
        int nrprocessed = 0;
        // we collect data for say at least 1000 features
        for (int b = 0; b < tree.getNrbuckets(); b++) {
            FeatureTreeNode node = tree.getNodeForBucket(b);
            if (node != null && node.getTotalNrChildren()>0) {
                for (Feature f: node.getAllFeatures()) {
                    if (f != null) {
                        populateMetaInfo(f);
                        nrprocessed++;
                        if (nrprocessed > MAX_NR_FEATURES) return;
                                
                    }
                };
                    
            }
        }
    }
    
    public ArrayList<String> getAttributes() {
        return atts;
    }
    public ArrayList<String> getValuesForAttribute(String att) {
        return attvaluesmap.get(att);
    }
    
    private static void p(String msg) {
        Logger.getLogger("FeatureMetaInfo").info(msg);
    }
     private static void err(String msg) {
        Logger.getLogger("FeatureMetaInfo").warning(msg);
    }
    
}
