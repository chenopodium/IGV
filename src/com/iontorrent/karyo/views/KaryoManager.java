/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.karyo.data.KaryoParser;
import com.iontorrent.karyo.data.Chromosome;
import com.iontorrent.karyo.data.FakeIndelTrack;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.FakeCnvTrack;
import com.iontorrent.karyo.data.IgvAdapter;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.ErrorHandler;
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
    private IgvAdapter igvadapter ;
    private ArrayList<GuiFeatureTree> guitrees;
    private ArrayList<KaryoTrack> karyotracks;
    private boolean tracksLoaded;
    private static KaryoManager manager;
    private KaryoControlPanel control;
    public static KaryoManager getManager(Frame frame, KaryoControlPanel control) {
        if (manager == null) manager = new KaryoManager(frame);
        return manager;
    }
    private KaryoManager(Frame frame) {
        this.frame = frame;
        igvadapter = new IgvAdapter(control);
        setKaryotracks(new ArrayList<KaryoTrack>());
        loadBandDataIfMissing();
    }

    public boolean addFakeTracks() {
        return true;
    }
    
    private void loadBandDataIfMissing() {        
        if (chromosomes == null) {
            KaryoParser parser = new KaryoParser("karyotype.human.txt");
            chromosomes = parser.getChromosomes();            
        }
    }
    private void loadTracks() {   
        p("KaryoManager.loadTracks");
       // boolean show = !tracksLoaded || karyotracks.size() <1;
        boolean show = true;
        ArrayList<KaryoTrack> tracks = igvadapter.getSelectedIgvTracks(show);
        p("loadTracks: Got "+tracks.size()+" tracks to load ");
        this.karyotracks = new ArrayList<KaryoTrack>();
        try {
            if (tracks.size()<1 && this.addFakeTracks()) {
                p("Adding a few fake tracks");
                tracks.add(new KaryoTrack(new FakeCnvTrack()));
                tracks.add(new KaryoTrack(new FakeIndelTrack()));
            }
            int nr = 1;
            for (KaryoTrack track : tracks) {

                getKaryotracks().add(track);
                nr++;
            }
        }
        catch (Exception e) {
            p("Got an error when adding/loading tracks: "+ErrorHandler.getString(e));
        }
        p("== loadTracks: Got "+this.getKaryoTracks().size()+" karyotracks now");
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
            p("LoadTracksTaks: Got "+karyotracks.size()+" after loadTracks" );
            return null;
        }
 
    }
    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }

    public void pickTracks() {
        karyotracks = new ArrayList<KaryoTrack>();
        igvadapter.getSelectedIgvTracks(true);
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
        p("Filtering all "+getGuitrees().size()+" guitrees");
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
