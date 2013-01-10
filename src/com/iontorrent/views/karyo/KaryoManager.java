/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.karyo;

import com.iontorrent.views.karyo.drawables.GuiFeatureTree;
import com.iontorrent.data.karyo.*;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import java.awt.Frame;
import java.util.ArrayList;
import org.broad.igv.track.FeatureTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class KaryoManager {

    private ArrayList<Chromosome> chromosomes;
    
    private Frame frame;
    private IgvAdapter igvadapter = new IgvAdapter();
    private ArrayList<GuiFeatureTree> guitrees;
    private ArrayList<KaryoTrack> karyotracks;
    private boolean tracksLoaded;
    
    public KaryoManager(Frame frame) {
        this.frame = frame;
        setKaryotracks(new ArrayList<KaryoTrack>());
        loadBandDataIfMissing();
    }

    public boolean addFakeTracks() {
        return false;
    }
    
    private void loadBandDataIfMissing() {        
        if (chromosomes == null) {
            KaryoParser parser = new KaryoParser("karyotype.human.txt");
            chromosomes = parser.getChromosomes();            
        }
    }
    private void loadTracks() {        
        boolean show = !tracksLoaded;
        ArrayList<FeatureTrack> tracks = igvadapter.getSelectedTracks(show);
        
        if (this.addFakeTracks()) {
            tracks.add(new FakeCnvTrack());
            tracks.add(new FakeIndelTrack());
        }
        int nr = 1;
        for (FeatureTrack track : tracks) {
            KaryoTrack kt = new KaryoTrack(track, ""+track.getName().charAt(0));
            getKaryotracks().add(kt);
            nr++;
        }
        tracksLoaded = true;
    }

    public void loadTracks(TaskListener list) {
        LoadTracksTaks t = new LoadTracksTaks(list);
        t.run();
    }
    private class LoadTracksTaks extends Task {

        public LoadTracksTaks(TaskListener listener) {
            super(listener);            
        }
        @Override
        public boolean isSuccess() {
            return karyotracks.size()>0;
        }

        @Override
        protected Void doInBackground() throws Exception {
            loadTracks();
            
            return null;
        }
 
    }
    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void pickTracks() {
        igvadapter.getSelectedTracks(true);
    }

    public ArrayList<Feature> getSampleFeatures() {
        ArrayList<Feature> res = new ArrayList<Feature>();
        for (GuiFeatureTree gt : getGuitrees()) {
            Feature f = gt.getTree().getSampleFeature();
            if (f != null) {
                res.add(f);
            }
        }
        return res;
    }

    public ArrayList<GuiFeatureTree> getGuiTrees() {
        return getGuitrees();
    }

    public void filterTrees() {
        p("Filtering all trees");
        for (GuiFeatureTree guitree : getGuitrees()) {
            guitree.filter();
        }
    }

    private void p(String msg) {
        // Logger.getLogger("KaryoManager").info(msg);
        System.out.println("KaryoManager: " + msg);
    }

    public ArrayList<KaryoTrack> getKaryoTracks() {
        return getKaryotracks();
    }

    public IgvAdapter getIgvAdapter() {
        return igvadapter;
    }

    /**
     * @return the karyotracks
     */
    public ArrayList<KaryoTrack> getKaryotracks() {
        return karyotracks;
    }

    /**
     * @param karyotracks the karyotracks to set
     */
    public void setKaryotracks(ArrayList<KaryoTrack> karyotracks) {
        this.karyotracks = karyotracks;
    }

    /**
     * @return the guitrees
     */
    public ArrayList<GuiFeatureTree> getGuitrees() {
        return guitrees;
    }

    /**
     * @param guitrees the guitrees to set
     */
    public void setGuitrees(ArrayList<GuiFeatureTree> guitrees) {
        this.guitrees = guitrees;
    }

    /**
     * @return the frame
     */
    public Frame getFrame() {
        return frame;
    }

    /**
     * @param frame the frame to set
     */
    public void setFrame(Frame frame) {
        this.frame = frame;
    }

    public KaryoOverviewPanel createOverView() {
        KaryoOverviewPanel pan = new KaryoOverviewPanel(this);
        pan.createView();
        return pan;
    }
}
