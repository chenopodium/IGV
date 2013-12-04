/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;


import com.iontorrent.karyo.views.KaryoControlPanel;
import com.iontorrent.karyo.views.IgvTrackSelectionPanel;
import com.iontorrent.utils.GuiUtils;
import java.util.ArrayList;

import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.broad.igv.feature.genome.GenderManager;

import org.broad.igv.track.AbstractTrack;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.RuntimeUtils;

/**
 *
 * @author Chantal Roth
 */
public class IgvAdapter {
    private IgvTrackSelectionPanel igvselpanel;
    private KaryoControlPanel control;
    private static boolean memWarningShown = false;
            
    public IgvAdapter(KaryoControlPanel control){
        this.control = control;
        
    }
    
    private void showIgvTrackSelection() {
        // if (igvselpanel == null) {
            igvselpanel = new IgvTrackSelectionPanel(null);
        //}
         if (igvselpanel.getNrListedTracks() <1) {
             GuiUtils.showNonModalMsg("There are no suitable tracks to show.\nTry a variant file (such as SNPs, CNVs etc)."
                     + "\nI will just show the chromosome bands then.");
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
        p("IGVAdapter: CreateTree and load data called. Checking memory");
        if (RuntimeUtils.getAvailableMemory()/1000000 < 50) {
            // stop loading
            String msg = "IGV is running low on memory, I will stop loading data now.<br>If you are using Java 32 bit, please <b>upgrade to Java 64 bit!</b>"
                    + "<br>(Check your system for installed programs, and remove any old Java 32 bit versions)";
            if (!memWarningShown) {
                MessageUtils.showMessage(msg);
                memWarningShown = true;
            }
            else GuiUtils.showNonModalMsg(msg);
            return null;
        }
        AbstractTrack track = ktrack.getTrack();
      //  p("Got track with name: "+track.getName());
        FeatureTree tree = new FeatureTree(ktrack, ktrack.getTrack(), chr);        
        String n = track.getName();
        int pos = n.indexOf("_");
        if (pos >0) n = n.substring(0, pos);
        pos = n.lastIndexOf(".");
        if (pos >0) n = n.substring(0, pos);
        tree.setName(n);
        // can take a long times
        tree.loadFeatures();
        
        if (GenderManager.isSexChromosome(chr.getName())) {
             if (tree.getTotalNrChildren()>0) {
               // p("FOUND DATA ON X or Y CHROMOSOME");
                if (GenderManager.isY(chr.getName())) ktrack.setMale(true);
                else ktrack.setFemale(true);
             }           
        }
        return tree;
    }
    private void p(String s) {
        Logger.getLogger("IgvAdapter").info(s);
    }

    public void showLocation(String name, int loc, int end) {
        p("++++++++++++++++++++++++++++++++  Going to locus "+name+":"+loc+" +++++++++++++++++++++++++ ");
        if (!name.startsWith("chr")) {
            name = "chr"+name;
        }
        IGV.getInstance().goToLocus(name+":"+loc+"-"+end);
        IGV.getMainFrame().toFront();
        IGV.getInstance().goToLocus(name+":"+loc+"-"+end);
        IGV.getMainFrame().toFront();
    }

    
}
