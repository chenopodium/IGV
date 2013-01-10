/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo.filter;

import com.iontorrent.data.karyo.KaryoFeature;
import java.util.logging.Logger;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class VariantFrequencyFilter extends KaryoFilter{
    
   
    
    public VariantFrequencyFilter() {        
        super();
        name= "Variant allele fraction";
        this.fieldname=  "allele fraction";
    }
    
  
    @Override
    public KaryoFilter copy() {
        VariantAttributeFilter fil = new VariantAttributeFilter();
        fil.setAttname(fieldname);        
        fil.setTreshold(super.getTreshold());
        fil.setFilteredColor(super.getFilteredColor());
        fil.setNonfilteredColor(super.getNonfilteredColor());
        return fil;
    }
    
    @Override
    public boolean filter(KaryoFeature kf) {
        Feature f = kf.getFeature();
        if (!(f instanceof Variant)) return false;
        Variant var = (Variant)f;
        
        double frac = var.getAlleleFraction();
        boolean passed = false;
        if (not) passed= (frac <= treshold);
        else passed= frac> treshold;
     //   p(var.getAlleleFraction()+"-> filter "+ok);
        return passed;
    }
    private void p(String s) {
        Logger.getLogger("VariantFrequencyFilter").info(s);
    }
    @Override
    public boolean isForFeature(Feature feature) {
         if (feature instanceof KaryoFeature) return isForFeature(((KaryoFeature)feature).getFeature());
        return feature instanceof Variant;
    } 
    
    @Override
    public boolean isValid() {
        return (this.fieldname != null && this.isInitialized());
    }
}
