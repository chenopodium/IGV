/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.FeatureMetaInfo;
import java.awt.Color;
import java.util.ArrayList;
import javax.swing.JCheckBox;

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
   
    protected void setEnabledFontAndColor(JCheckBox box) {
       
        if (box.isSelected()) {
            box.setText("Enabled");
            box.setForeground(Color.green.darker());
        }
        else {
            box.setText("Disabled");
            box.setForeground(Color.red.darker());
        }
    }
    public static FilterPanel createPanel(KaryoFilter filter,KaryoTrack track ) {
        if (filter instanceof VariantAttributeFilter) {
            ArrayList<String> allowedAtts = new  ArrayList<String> ();
            allowedAtts.add("CONFIDENCE");
//            allowedAtts.add("OMIM");
            // XXX add confidence or toerh
             VariantAttributeFilter vfil = (VariantAttributeFilter)filter;
             VariantAttributePanel pan = new VariantAttributePanel(vfil, track, allowedAtts);
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
