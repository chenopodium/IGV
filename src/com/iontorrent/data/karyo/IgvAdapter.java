/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import com.iontorrent.data.karyo.filter.KaryoFilter;
import com.iontorrent.views.karyo.TrackSelectionPanel;
import java.util.ArrayList;

import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal Roth
 */
public class IgvAdapter {
      private TrackSelectionPanel selpanel;
  
    public IgvAdapter(){
    }
    
    public void showTrackSelection() {
         if (selpanel == null) {
            selpanel = new TrackSelectionPanel();
        }
         if (selpanel.getNrListedTracks() <1) {
             JOptionPane.showConfirmDialog(IGV.getMainFrame(), "There are no suitable tracks to show.\nTry a variant file (such as SNPs, CNVs etc)."
                     + "\nI will just show the chromosome bands then.",
                     "No suitable tracks for Karyo view", JOptionPane.OK_OPTION); 
         }
         else JOptionPane.showConfirmDialog(IGV.getMainFrame(), selpanel,"Pick the tracks to view", JOptionPane.OK_OPTION); 
    }
    public ArrayList<FeatureTrack> getSelectedTracks(boolean show) {
        if (selpanel == null) {
            selpanel = new TrackSelectionPanel();
        }
      //  if (show || selpanel.getNrTracks()>1) 
        showTrackSelection(); 
        return selpanel.getSelectedTracks();
    }
//    public VariantTrack getVariantTrack() {
//        if (IGV.getInstance() == null) {
//            p("NO IGV instance is running");
//            return null;
//        }
//        for (Track t: IGV.getInstance().getAllTracks()) {
//            if (t instanceof VariantTrack) {
//                return (VariantTrack)t;
//            }
//        }
//        p("Got no variant track");
//        return null;
//    }
//    
    public FeatureTree createTree(KaryoTrack ktrack , Chromosome chr) {        
        if (ktrack == null || ktrack.getTrack()==null) {
             p("Got no variant track or no IGV running");
             return null;
        }
        
        FeatureTrack track = ktrack.getTrack();
      //  p("Got track with name: "+track.getName());
        FeatureTree tree = new FeatureTree(ktrack, ktrack.getTrack(), chr);        
        String n = track.getName();
        int pos = n.indexOf("_");
        if (pos >0) n = n.substring(0, pos);
        pos = n.indexOf(".");
        if (pos >0) n = n.substring(0, pos);
        tree.setName(n);
        tree.loadFeatures();
        ArrayList<KaryoFilter> filters = ktrack.getPossibleFilters();
        if (filters != null) {
            for (KaryoFilter filter: filters) {
                if (filter.isForFeature(tree.getSampleFeature())) {
                    tree.addFilter(filter);
                }
            }
        }
            
        return tree;
    }
    private void p(String s) {
        Logger.getLogger("IgvAdapter").info(s);
    }

    public void showLocation(String name, int loc, int end) {
    //    p("Going to lucis "+name+":"+loc);
        IGV.getInstance().goToLocus("chr"+name+":"+loc+"-"+end);
        IGV.getMainFrame().toFront();
    }
}
