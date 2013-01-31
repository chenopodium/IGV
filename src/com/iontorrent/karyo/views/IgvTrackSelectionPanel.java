/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.utils.ErrorHandler;
import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.broad.igv.sam.CoverageTrack;
import org.broad.igv.track.AbstractTrack;
import org.broad.igv.track.DataSourceTrack;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackType;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal Roth
 */
public class IgvTrackSelectionPanel extends javax.swing.JPanel {

    private int nrlisted;
    private ArrayList<KaryoTrack> karyotracks;

    /**
     * Creates new form TrackSelectionPanel
     */
    public IgvTrackSelectionPanel(KaryoControlPanel control) {
        initComponents();

        setLayout(new BorderLayout());
        add("North", new JLabel("Please pick the tracks you wish to see in the Karyo View"));
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        Collection<Track> tracks = IGV.getInstance().getAllTracks();

        // karyotracks = control.getManager().getKaryoTracks();
        if (karyotracks == null) {
            karyotracks = new ArrayList<KaryoTrack>();
        }

        MouseListener list = null;

        for (Track igvtrack : tracks) {

            p("Got track: " + igvtrack.getName() + ", " + igvtrack.getClass().getName());
            TrackType type = igvtrack.getTrackType();
            String n = igvtrack.getName();
            p("Got track " + n + ":" + type);
            try {
                if (!(igvtrack instanceof AbstractTrack)) {
                    p("Don't know what to do with this track type");
                } else {
                    AbstractTrack atrack = (AbstractTrack) igvtrack;
                    p("Got an abstract track");
                    KaryoTrack ktrack = new KaryoTrack(atrack);
                    SingleTrackPanel cb = null;
                    if (atrack instanceof DataSourceTrack) {
                        cb = new SingleTrackPanel(ktrack, true, control, list);
                        p("Adding DataSourceTrack");
                    } else if (atrack instanceof CoverageTrack) {
                        cb = new SingleTrackPanel(ktrack, true, control, list);
                        p("Adding CoverageTrack");
                    } else if (atrack instanceof FeatureTrack) {
                        if (n.startsWith("RefSeq")) {
                            // ignore
                            p("Ignoring RefSeq");
                        } else {
                            if (type == TrackType.GENE || type == TrackType.CHIP || type == TrackType.EXPR) {
                                ktrack.setVisible(false);
                                cb = new SingleTrackPanel(ktrack, true, control, list);
                                cb.setToolTipText("This type could potentially take long to load");
                                p("This track could take too long, not adding");

                            } else if (n.endsWith(".bam") || n.endsWith(".BAM")) {
                                ktrack.setVisible(false);
                                cb = new SingleTrackPanel(ktrack, true, control, list);
                                p("This is a bam file");
                                if (!atrack.getResourceLocator().isLocal()) {

                                    cb.setEnabled(false);
                                    cb.setText("Bam files can be huge, and this one is remote. It could take too long to load the entire file remotely!"
                                            + "<br>You can store it locally first and then load it if you really want to.");
                                } else {
                                    cb.setText("Bam files can be huge, and it could take too long to load the entire file!");
                                }
                            } else {

                                cb = new SingleTrackPanel(ktrack, true, control, list);
                            }

                        }
                    }
                    if (cb != null) {
                        center.add(cb);

                        karyotracks.add(ktrack);
                        nrlisted++;
                    }
                }

            } catch (Exception e) {
                p(ErrorHandler.getString(e));
            }
        }
        this.add("Center", center);
    }

    public int getNrListedTracks() {
        return nrlisted;
    }

    public int getNrTracks() {
        return karyotracks.size();
    }

    private void p(String msg) {
        System.out.println("IgvTrackSelectionPanel: " + msg);
    }

    public ArrayList<KaryoTrack> getSelectedTracks() {

        return karyotracks;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
