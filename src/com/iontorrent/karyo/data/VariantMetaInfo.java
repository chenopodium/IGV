/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/** 
 *
 * @author Chantal Roth
 */
public class VariantMetaInfo extends FeatureMetaInfo{

    @Override
    public void populateMetaInfo(Feature f) {
        if (!(f instanceof Variant)) return;
        Variant var = (Variant)f;
        boolean show = Math.random()>0.99;
        Map<String, Object> map = var.getAttributes();
        if (map != null && !map.isEmpty()) {
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                super.addAtt(name);
                if (show) p("Variant has att "+name+":"+var.getType()+","+var.toString());
                Object val = map.get(name);
                if (val != null) {
                    super.addAtt(name, val.toString());
                }
            }
        }
        else{
            if (show) p("Variant has no attributes: "+var.getType()+","+var.toString());
        }
        
    }
    private void p(String s) {
        Logger.getLogger("VariantMetaInfo").info(s);
    }
}
