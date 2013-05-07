/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.views.KaryoControlPanel;
import com.iontorrent.karyo.views.IgvTrackSelectionPanel;
import java.util.ArrayList;

import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.broad.igv.track.AbstractTrack;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal Roth
 */
public class IgvAdapter {
    private IgvTrackSelectionPanel igvselpanel;
    private KaryoControlPanel control;
    public IgvAdapter(KaryoControlPanel control){
        this.control = control;
    }
    
    private void showIgvTrackSelection() {
        // if (igvselpanel == null) {
            igvselpanel = new IgvTrackSelectionPanel(null);
        //}
         if (igvselpanel.getNrListedTracks() <1) {
             JOptionPane.showConfirmDialog(IGV.getMainFrame(), "There are no suitable tracks to show.\nTry a variant file (such as SNPs, CNVs etc)."
                     + "\nI will just show the chromosome bands then.",
                     "No suitable tracks for Karyo view", JOptionPane.OK_OPTION); 
         }
         else JOptionPane.showConfirmDialog(IGV.getMainFrame(), igvselpanel,"Pick the tracks to view", JOptionPane.OK_OPTION); 
    }
    public ArrayList<KaryoTrack> getSelectedIgvTracks(boolean show) {
      //  if (igvselpanel == null) {
       //     igvselpanel = new IgvTrackSelectionPanel(null);
      //  }
       // if (show || igvselpanel.getNrTracks()>1) {
            showIgvTrackSelection(); 
      //  }
        return igvselpanel.getSelectedTracks();
    }
    public FeatureTree createTree(KaryoTrack ktrack , Chromosome chr) {        
        if (ktrack == null || ktrack.getTrack()==null) {
             p("Got no variant track or no IGV running");
             return null;
        }
    //    p("IGVAdapter: CreateTree and load data called");
        AbstractTrack track = ktrack.getTrack();
      //  p("Got track with name: "+track.getName());
        FeatureTree tree = new FeatureTree(ktrack, ktrack.getTrack(), chr);        
        String n = track.getName();
        int pos = n.indexOf("_");
        if (pos >0) n = n.substring(0, pos);
        pos = n.indexOf(".");
        if (pos >0) n = n.substring(0, pos);
        tree.setName(n);
        tree.loadFeatures();
//        ArrayList<KaryoFilter> filters = ktrack.getPossibleFilters();
//        if (filters != null) {
//            for (KaryoFilter filter: filters) {
//                if (filter.isForFeature(tree.getSampleFeature())) {
//                    tree.addFilter(filter);
//                }
//            }
//        }
            
        return tree;
    }
    private void p(String s) {
        Logger.getLogger("IgvAdapter").info(s);
    }

    public void showLocation(String name, int loc, int end) {
    //    p("Going to lucis "+name+":"+loc);
        if (!name.startsWith("chr")) {
            name = "chr"+name;
        }
        IGV.getInstance().goToLocus(name+":"+loc+"-"+end);
        IGV.getMainFrame().toFront();
    }

    
}
