/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.KaryoFeature;
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
        if (!isEnabled()) return true;
        Feature f = kf.getFeature();
        if (!(f instanceof Variant)) return true;
        Variant var = (Variant)f;
        
        double frac = var.getAlleleFraction();
        
        boolean passed = false;
        if (not) passed= (frac <= treshold);
        else passed= frac> treshold;
       if (passed) this.filterCount++;
       // p("     passed? "+passed);
        filterCount++;
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
        return (this.fieldname != null);
    }
}
