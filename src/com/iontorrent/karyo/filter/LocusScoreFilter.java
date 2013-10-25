/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.FeatureMetaInfo.Range;
import com.iontorrent.karyo.data.KaryoFeature;
import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.broad.igv.feature.LocusScore;

import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class LocusScoreFilter extends KaryoFilter{
    
    private Range range;
    
    private String scorename;
    public LocusScoreFilter() {        
        super();
        this.scorename = "score";
        name= "Score filter";
        range = new Range();
       
    }
    public Range getRange() {
        return range;
    }
  
   
    // example VT = SNP, INDEL, SV
   @Override
    public String toString() {
        String s = "";
        if (range == null) return "Undefined";
        if (this.isHighlightFiltered()) s = "Highlight areas that have features where ";
        else if (this.isRemoveFiltered())s = "Hide features where ";
        else s = "Show features with ";
        s += scorename;
        if (not) s += " is  ";
        else s +=" is not ";
        s += " between "+range.min+"-"+range.max;
       // s += " (filter mode: "+filterMode.name()+")";
        return s;
    }
    
    public String toHtml() {
        String s = scorename;
        if (not) s += " not ";
        if (range.min == Double.NaN) {
            s += " &lt; "+range.max;
        }
        else if (range.max == Double.NaN) {
            s += " &gt; "+range.max;
        }
        else {
            s += " between "+ range.min+" and "+range.max;
        }
        
        return s;
    }
    @Override
    public boolean filter(KaryoFeature kf) {
        if (!isEnabled() || range == null) return true;
        
        Feature f = kf.getFeature();
        if (!(f instanceof LocusScore)) return true;
        LocusScore var = (LocusScore)f;
        double score = var.getScore();
        boolean passed = true;
        if (range.min == Double.NaN) {
             passed =  score <= range.max;
        }
        else if (range.max == Double.NaN) {
             passed =  score >= range.min ;
        }
        else  passed =  score >= range.min && score <= range.max;
        if (not) passed= !passed;
         if (passed) this.filterCount++;
       // p("     passed? "+passed);        
        this.totalCount++;
        return passed;
    }
    @Override
    public Color getFilteredColor(KaryoFeature kf) {
        Feature f = kf.getFeature();
        if (!(f instanceof LocusScore)) return null;
        LocusScore var = (LocusScore)f;
        return getColorByValue(var.getScore() - range.min);
    }
    
    protected Color getColorByValue(double score) {
        double mid = range.getMiddle();
        if (score <=mid) return Color.red;
        else return Color.green;
    }
    public void setRange(Range r) {
        this.range = r;
    }
    private void p(String s) {
        Logger.getLogger("LocusScoreFilter").info(s);
    }
    @Override
    public boolean isForFeature(Feature feature) {
        if (feature instanceof KaryoFeature) return isForFeature(((KaryoFeature)feature).getFeature());
        return feature instanceof LocusScore;
    }

    @Override
    public KaryoFilter copy() {
        LocusScoreFilter fil = new LocusScoreFilter();
        fil.setRange(range);
        fil.scorename = scorename;
        fil.setFilteredColor(super.getFilteredColor());
        fil.setNonfilteredColor(super.getNonfilteredColor());
        return fil;
    }

    @Override
    public boolean isValid() {
        return range != null;
    }

    /**
     * @return the scorename
     */
    public String getScorename() {
        return scorename;
    }

    /**
     * @param scorename the scorename to set
     */
    public void setScorename(String scorename) {
        this.scorename = scorename;
    }
}
