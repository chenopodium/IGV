/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.karyo.data.Band;
import com.iontorrent.karyo.data.KaryoParser;
import com.iontorrent.karyo.data.Chromosome;
import com.iontorrent.karyo.data.FakeIndelTrack;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.FakeCnvTrack;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.IgvAdapter;
import com.iontorrent.karyo.data.KaryoTrackComparator;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.karyo.renderer.RenderManager;
import com.iontorrent.threads.Task;
import com.iontorrent.threads.TaskListener;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.GuiUtils;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.broad.igv.feature.Cytoband;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class KaryoManager {
    
    private ArrayList<Chromosome> chromosomes;
    private Frame frame;
    private IgvAdapter igvadapter;
    private ArrayList<GuiFeatureTree> guitrees;
    private ArrayList<KaryoTrack> karyotracks;
    private boolean tracksLoaded;
    private static KaryoManager manager;
    private KaryoControlPanel control;

    public static KaryoManager getManager(Frame frame, KaryoControlPanel control) {
        if (manager == null) {
            manager = new KaryoManager(frame);
            manager.control = control;
        }
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

    public KaryoControlPanel getControl() {
        return control;
    }

    private void loadBandDataIfMissing() {        
        if (chromosomes == null) {
            // get genome from current setting
            Genome genome = IGV.getInstance().getGenomeManager().getCurrentGenome();
            Collection<org.broad.igv.feature.Chromosome> igvchromosomes = genome.getChromosomes();
            chromosomes = new ArrayList<Chromosome>();
            for (org.broad.igv.feature.Chromosome igvchr : igvchromosomes) {
                String name = igvchr.getName();
                if (name.indexOf("_") < 0) {
                    String cname = igvchr.getName();
                    if (cname.startsWith("chrchr")) {
                        cname = cname.substring(3);
                    }
                    Chromosome chr = new Chromosome(cname, "" + igvchr.getIndex(), igvchr.getLength(), "chromosome");
                    if (chr.getLength() < 1000000) {
                        p("Chromosome " + chr + " is too small, not using it");
                    } else {                        
                        List<Cytoband> igvbands = igvchr.getCytobands();
                        for (Cytoband cyto : igvbands) {                            
                            Band band = new Band(cyto);
                            chr.add(band);
                        }
                        chromosomes.add(chr);
                    }
                } else {
                    // p("Not using chr "+name);
                }
            }
            //KaryoParser parser = new KaryoParser("karyotype.human.txt");
            //chromosomes = parser.getChromosomes();            
            p("Got chromosomes: " + chromosomes);
        }
    }

    private void loadTracks() {        
        p("============= KaryoManager.loadTracks================ ");
        // boolean show = !tracksLoaded || karyotracks.size() <1;
        boolean show = true;
        ArrayList<KaryoTrack> tracks = igvadapter.getSelectedIgvTracks(show);
        p("loadTracks: Got " + tracks.size() + " tracks to load ");
        this.karyotracks = new ArrayList<KaryoTrack>();
        
        boolean takeslong = false;
        int nr = tracks.size();
        int time = 4*nr;
        for (KaryoTrack kt: tracks) {
            String disp = kt.getTrackDisplayName().toLowerCase();
            if (disp.startsWith("mother") || disp.startsWith("father") || disp.startsWith("self")) {
                time = nr*30;//takes a long time!
            }
            takeslong = true;
        }
        try {
            if (tracks.size() < 1 && this.addFakeTracks()) {
                p("Adding a few fake tracks");
                tracks.add(new KaryoTrack(new FakeCnvTrack(), 1));
                tracks.add(new KaryoTrack(new FakeIndelTrack(), 2));
            } else if (tracks.size() > 6 || takeslong) {
               if (time >= 30) {
                   MessageUtils.showMessage("Loading "+nr+" karyo tracks... some tracks are large, and it can take quite a long time to load");
                    //GuiUtils.showNonModelMsg("Loading", "Loading "+nr+" karyo tracks... some tracks are lager, and it can take quite a long time!", true, time);
               }
               else GuiUtils.showNonModelMsg("Loading", "Loading "+nr+" karyo tracks...", true, time);
               IGV.getInstance().setStatusBarMessage("Loading  karyo track data...");
            }
            nr = 1;
            for (KaryoTrack track : tracks) {
                getKaryotracks().add(track);
                nr++;
            }
        } catch (Exception e) {
            p("loadTracks: Got an error when adding/loading tracks: " + ErrorHandler.getString(e));
        }
        GuiProperties gui = RenderManager.getGuiProperties();
        for (KaryoTrack track : this.getKaryoTracks()) {
            // set order
            int order = gui.getTrackOrder(track.getSample(), track.getTrackName(), track.getFileExt());
            if (order != 0) {
                p("loadTracks: Got order " + order + " for track " + track.getSample() + "_" + track.getTrackName());
                track.setOrder(order);
            } else {
                track.setOrder(10);
            }
        }
        Collections.sort(this.getKaryoTracks(), new KaryoTrackComparator());
        nr = 1;
        for (KaryoTrack track : this.getKaryoTracks()) {
            // set nr
            track.setShortname(""+nr);           
            nr++;
        }
         for (int c = 0; c < this.chromosomes.size(); c++) {
            int trees = 0;
            int trackx;
            int totaltrees = karyotracks.size();
            for (KaryoTrack kt : karyotracks) {
                if (!kt.isVisible()) {
                    continue;
                }
                // p("Adding track " + kt + " to chr " + c);
                Chromosome chr = chromosomes.get(c);
                FeatureTree tree = chr.getTree(kt);
                if (tree == null) {
                    p("---> loadTracks:Creating tree, loading all features");
                    tree = igvadapter.createTree(kt, chr);
                    chr.addTree(kt, tree);
                }
            }
         }
        p("== loadTracks done: Sorted by order. Got " + this.getKaryoTracks().size() + " karyotracks now");
        tracksLoaded = true;
        IGV.getInstance().setStatusBarMessage("");
    }
    
    public void loadTracks(TaskListener list) {
        LoadTracksTaks t = new LoadTracksTaks(list);
        
        t.run();
    }
    
    public void saveGuiProperties() {
        
        RenderManager.saveGuiProperties();
        
    }

    private class LoadTracksTaks extends Task {
        
        public LoadTracksTaks(TaskListener listener) {
            super(listener);            
        }

        @Override
        public boolean isSuccess() {
            return karyotracks.size() > 0;
        }
        
        @Override
        protected Void doInBackground() throws Exception {
            loadTracks();
            p("LoadTracksTaks: Got " + karyotracks.size() + " after loadTracks");
            return null;
        }
    }

    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }

//    public void pickTracks() {
//        karyotracks = new ArrayList<KaryoTrack>();
//        igvadapter.getSelectedIgvTracks(true);
//    }
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
        p("Filtering all " + getGuitrees().size() + " guitrees");
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
    
    public ArrayList<KaryoTrack> getSelectdKaryoTracks() {
        if (karyotracks == null) {
            return null;
        }
        ArrayList<KaryoTrack> selected = new ArrayList<KaryoTrack>();
        int nr = 0;
        for (KaryoTrack t : karyotracks) {
            if (t.isVisible()) {
                selected.add(t);
            }
        }
        return selected;
    }

    public int getNrSelectdKaryoTracks() {
        if (karyotracks == null) {
            return 0;
        }
        int nr = 0;
        for (KaryoTrack t : karyotracks) {
            if (t.isVisible()) {
                nr++;
            }
        }
        return nr;
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
    
    public KaryoOverviewPanel createOverView(String msg) {
        KaryoOverviewPanel pan = new KaryoOverviewPanel(this);
        pan.setMsg(msg);
        pan.createView();
        return pan;
    }
}
