/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.FeatureMetaInfo;

/**
 *
 * @author Chantal Roth
 */
public abstract class FilterPanel extends javax.swing.JPanel {

    protected KaryoFilter filter;
    protected KaryoTrack track;
    protected FeatureMetaInfo meta;

    /**
     * Creates new form VariantAttributePanel
     */
    public FilterPanel(KaryoFilter filter, KaryoTrack track) {
        this.filter = filter;
        this.track = track;
        this.meta = track.getMetaInfo();
      
    }
   
    public static FilterPanel createPanel(KaryoFilter filter,KaryoTrack track ) {
        if (filter instanceof VariantAttributeFilter) {
             VariantAttributeFilter vfil = (VariantAttributeFilter)filter;
             VariantAttributePanel pan = new VariantAttributePanel(vfil, track);
             return pan;
        }
        else if (filter instanceof LocusScoreFilter) {
             
             ScoreFilterPanel pan = new ScoreFilterPanel("Score", filter, track);
             return pan;
        }
         else if (filter instanceof VariantFrequencyFilter) {             
             ScoreFilterPanel pan = new ScoreFilterPanel("Treshold", filter, track);
             return pan;
        }
        else return null;
    }
    public abstract void updateFilter() ;
      
  
}
