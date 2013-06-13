/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import com.iontorrent.data.ConfidenceDistribution;
import com.iontorrent.data.IonogramAlignment;
import com.iontorrent.guiutils.GuiUtils;
import com.iontorrent.prefs.IonTorrentPreferencesManager;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.utils.LocationListener;
import com.iontorrent.utils.SimpleDialog;
import com.iontorrent.views.alignment.AlignmentControlPanel;
import com.iontorrent.views.dist.DistPanel;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.Locus;
import org.broad.igv.sam.AbstractAlignment;
import org.broad.igv.sam.AlignmentDataManager;
import org.broad.igv.sam.AlignmentTrack;
import org.broad.igv.sam.AlignmentTrackHandler;
import org.broad.igv.track.TrackClickEvent;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.IGVMenuBar;
import org.broad.igv.ui.action.MenuAction;
import org.broad.igv.ui.panel.FrameManager;
import org.broad.igv.ui.panel.IGVPopupMenu;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.ui.util.MenuAndToolbarUtils;
import org.broad.igv.ui.util.MessageUtils;

/**
 *
 * @author Chantal
 */
public class IonTorrentAlignmentTrackHandler implements AlignmentTrackHandler {

    private static Logger log = Logger.getLogger(IonTorrentAlignmentTrackHandler.class);
    private AlignmentTrack track;
    private IGVPopupMenu popup;
    private AlignmentDataManager dataManager;

    public IonTorrentAlignmentTrackHandler() {
    }

    public void addCustomMenusAndActions(AlignmentTrack track, IGVPopupMenu popup, TrackClickEvent e) {
        this.track = track;
        this.popup = popup;
        this.dataManager = track.getDataManager();
        if (dataManager.isIonTorrent()) {
            log.info("Adding IonTorrent menus");
            addCopyDistributionToClipboardItem(e);
            addIonTorrentAuxiliaryViews(e);
        } else {
            log.info("Not adding IonTorrent menus");
        }
    }

    private void add(JMenuItem it) {
        popup.add(it);
    }

    private void add(JMenu it) {
        popup.add(it);
    }

    public void addCopyDistributionToClipboardItem(final TrackClickEvent te) {
        final MouseEvent me = te.getMouseEvent();
        JMenuItem item = new JMenuItem("Copy the confidence distribution for this base to the clipboard");
        final ReferenceFrame frame = te.getFrame();
        if (frame == null) {
            item.setEnabled(false);
        } else {
            final int location = (int) (frame.getChromosomePosition(me.getX()));

            // Change track height by attribute
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvt) {
                    copyDistribution(te, location);
                }
            });
        }
        add(item);
    }

    private void addIonTorrentAuxiliaryViews(final TrackClickEvent e) {


        final ReferenceFrame frame = e.getFrame();
        if (frame == null) {
            return;

        } else {

            final int location = (int) (frame.getChromosomePosition(e.getMouseEvent().getX()));
            JMenu groupMenu = createDistributionContextMenuItems(location, e);
            add(groupMenu);

            
            final AbstractAlignment alignment = (AbstractAlignment) track.getAlignmentAt(location, e.getMouseEvent().getY(), frame);
            if (alignment != null) {
                final JMenuItem itemIonoAlignment = new JCheckBoxMenuItem("<html>View ionogram for <b>this</b> read <b>"+alignment.getReadName()+"</b></html>");
                itemIonoAlignment.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent aEvt) {
                        showIonogramAlignment(!alignment.isNegativeStrand(), location, e.getFrame(), null, alignment, false);
                    }
                });
                add(itemIonoAlignment);
            }



            final JMenuItem itemIonoMultiAlignment = new JCheckBoxMenuItem("<html>View <b>forward</b> ionogram <b>multiple</b> alignment </html>");
            itemIonoMultiAlignment.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvt) {
                    showIonogramAlignment(true, location, e.getFrame(), null, alignment, true);
                }
            });

            add(itemIonoMultiAlignment);
            
            final JMenuItem itemIonoMultiAlignmentrev = new JCheckBoxMenuItem("<html>View <b>reverse</b> ionogram <b>multiple</b> alignment </html>");
            itemIonoMultiAlignmentrev.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent aEvt) {
                    showIonogramAlignment(false, location, e.getFrame(), null, alignment, true);
                }
            });

            add(itemIonoMultiAlignmentrev);
            
            this.addGetRawTraceItem(e);
            
            IGVMenuBar bar = IGV.getInstance().getMenuBar();
            if (!bar.hasMenu("IonTorrent")) {
                JMenu ionmenu = new JMenu("IonTorrent");
                bar.add(ionmenu);
                MenuAction menuAction =
                        new MenuAction("<html>View <b>forward ionogram</b> multiple alignment</html>", null, KeyEvent.VK_K) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                showIonogramAlignment(true, location, frame, null, alignment, true);
                            }
                        };
                ionmenu.add(MenuAndToolbarUtils.createMenuItem(menuAction));
                
                MenuAction menuActionrev =
                        new MenuAction("<html>View <b>reverse ionogram</b> multiple alignment</html>", null, KeyEvent.VK_K) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                showIonogramAlignment(false, location, frame, null, alignment, true);
                            }
                        };
                ionmenu.add(MenuAndToolbarUtils.createMenuItem(menuActionrev));
                groupMenu = createDistributionContextMenuItems(location, e);
                ionmenu.add(groupMenu);
            }
        }
    }

    public void addGetRawTraceItem(final TrackClickEvent te) {
        final MouseEvent me = te.getMouseEvent();
        JMenuItem item = new JMenuItem("Get raw data for this base and read");
// xx add menu for just this ionoagram
        final ReferenceFrame frame = te.getFrame();
        if (frame == null) {
            item.setEnabled(false);
        } else {
            final double location = frame.getChromosomePosition(me.getX());
            final AbstractAlignment alignment = (AbstractAlignment) track.getAlignmentAt(location, me.getY(), frame);
            if (alignment == null) {
                item.setEnabled(false);
            } else {
                final FlowValue fv = alignment.getFlowValue(location);
                if (fv == null) {
                    item.setEnabled(false);
                } else {
                    item.setText("<html>Get raw data for <b>" + alignment.getReadName() + "</b>, base " + fv.getBase() + " and flow <b>" + fv.getFlowPosition() + "</b></html>");

                    // Change track height by attribute
                    item.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent aEvt) {

                            showIonogramAlignment(!alignment.isNegativeStrand(), (int) location, frame, fv, alignment, false);

                        }
                    });

                }
            }
        }

        add(item);
    }

    /**
     * Copy the contents of the popup text to the system clipboard.
     */
    public void copyDistribution(final TrackClickEvent e, int location) {
        ArrayList<ConfidenceDistribution> dists = getDistribution(e.getFrame(), location);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        String json = "";
        for (ConfidenceDistribution dist : dists) {
            json += dist.toJson() + "\n";
        }
        clipboard.setContents(new StringSelection(json), null);
    }

    private JMenu createDistributionContextMenuItems(final int location, final TrackClickEvent e) {
        JMenu groupMenu = new JMenu("<html>View flow confidence distribution for");
        ButtonGroup group = new ButtonGroup();
        JCheckBoxMenuItem itemb = new JCheckBoxMenuItem("<html><b>forward and reverse (2 data series)</b></html>");
        groupMenu.add(itemb);
        group.add(itemb);
        itemb.setSelected(true);
        itemb.setFont(itemb.getFont().deriveFont(Font.BOLD));
        JCheckBoxMenuItem item = new JCheckBoxMenuItem("both strands combined");
        groupMenu.add(item);
        group.add(item);
        JCheckBoxMenuItem itemf = new JCheckBoxMenuItem("forward strand only");
        groupMenu.add(itemf);
        group.add(itemf);
        JCheckBoxMenuItem itemr = new JCheckBoxMenuItem("reverse strand only");
        groupMenu.add(itemr);
        group.add(itemr);
        // Change track height by attribute
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvt) {
                showDistribution(location, e.getFrame(), true, true);
            }
        });
        itemf.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvt) {
                showDistribution(location, e.getFrame(), true, false);
            }
        });
        itemr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvt) {
                showDistribution(location, e.getFrame(), false, true);
            }
        });
        itemb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aEvt) {
                showDistribution(location, e.getFrame(), false, false);
            }
        });
        return groupMenu;
    }

    /*
     * Open a seperate window that shows the alignment of ionograms - including
     * empty flows (might be difficult to show in a track due to those
     * empties....)
     */
    private void showIonogramAlignment(boolean forwardOnly, final int center_location, final ReferenceFrame frame, FlowValue fv, AbstractAlignment alignment, final boolean multiAlignment) {
        // first get parameters from preferences such as how many bases to the left/right we want to consider
        PreferenceManager prefs = PreferenceManager.getInstance();
        int nrbases_left_right = prefs.getAsInt(IonTorrentPreferencesManager.IONTORRENT_NRBASES_IONOGRAM_ALIGN);
        
        nrbases_left_right = Math.max(2, nrbases_left_right);
        nrbases_left_right = Math.min(20, nrbases_left_right);
        log.info("Got nr nrbases_left_right:"+nrbases_left_right);
        final int bases = nrbases_left_right;
        // now we get the flow values for each read at each location 
        final IonogramAlignment forward_align;
        if (forwardOnly) forward_align = getIonogramAlignment(frame, center_location, nrbases_left_right, true, alignment,multiAlignment);
        else forward_align = null;
        
        final IonogramAlignment reverse_align;
        if (!forwardOnly) reverse_align = getIonogramAlignment(frame, center_location, nrbases_left_right, false, alignment,multiAlignment);
        else reverse_align = null;
        
        if (fv != null) {
            log.info("Got flow value to load raw data for: " + fv);
        }
        final AlignmentControlPanel forpanel = AlignmentControlPanel.getForPanel(center_location, forward_align, fv);
        final AlignmentControlPanel revpanel = AlignmentControlPanel.getRevPanel(center_location, reverse_align, fv);

        String locus = Locus.getFormattedLocusString(frame.getChrName(), (int) center_location, (int) center_location);
        LocationListener listener = new LocationListener() {
            @Override
            public void locationChanged(int newLocation) {
                log.info("Got new location from panel: " + newLocation);
                String locus = Locus.getFormattedLocusString(frame.getChrName(), (int) newLocation, (int) newLocation);
                if (forward_align != null) {
                    IonogramAlignment forward = getIonogramAlignment(frame, newLocation, bases, true, forward_align.getSelectedAlignment(),multiAlignment);
                    forpanel.setAlignment(forward, newLocation);
                    forward.setTitle("Ionogram Alignment (forward) at " + locus);
                }
                if (reverse_align != null) {
                    IonogramAlignment reverse = getIonogramAlignment(frame, newLocation, bases, false, reverse_align.getSelectedAlignment(),multiAlignment);
                    reverse.setTitle("Ionogram Alignment (reverse) at " + locus);
                    revpanel.setAlignment(reverse, newLocation);
                }
                frame.centerOnLocation(newLocation + 1);
                IGV.getInstance().repaintDataAndHeaderPanels();
                IGV.getInstance().repaintStatusAndZoomSlider();
            }
        };
        if (forward_align != null) {
            forpanel.setListener(listener);
        }

        // now create two panels, one for forward, and one for the reverse strand
        if (reverse_align != null) {
            revpanel.setListener(listener);
        }
        ImageIcon image = new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/msa.gif"));
        AlignmentControlPanel.showModalDialog(forpanel, revpanel, locus, image);
//        if (forpanel.hasAlignment()) {
//            AlignmentControlPanel.showForwardPanel(forpanel, locus, image);
//        } else {
//            GuiUtils.showNonModalMsg("Found no data for the forward alignment");
//        }
//        if (revpanel.hasAlignment()) {
//            AlignmentControlPanel.showReversePanel(revpanel, locus, image);
//        } else {
//            GuiUtils.showNonModalMsg("Found no data for the reverse alignment");
//        }
    }

    /**
     * if neither forward nor reverse, create 2 charts in one
     */
    private void showDistribution(final int location, final ReferenceFrame frame, final boolean forward, final boolean reverse) {
        ConfidenceDistribution[] distributions = getDistributions(forward, reverse, frame, location);

        if (distributions == null || distributions.length < 1) {
            MessageUtils.showMessage("I found no flow signal distributions at this location " + location);
            return;
        }

        ImageIcon image = new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/chip_16.png"));

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

        int x = (int) Math.max(100, screen.getWidth() / 2 - 400);
        int y = (int) 200;

        final ArrayList<DistPanel> panels = DistPanel.createPanels(distributions, 2);
        for (final DistPanel panel : panels) {
            LocationListener listener = new LocationListener() {
                @Override
                public void locationChanged(int newLocation) {
                    log.info("Got new location from panel: " + newLocation + ", (old location was: " + location + ")");
                    ConfidenceDistribution[] newdist = getDistributions(forward, reverse, frame, newLocation);
                    panel.setDistributions(newdist);
                    // xxx compute new maxy
                    panel.recreateChart();
                    //frame.jumpTo(frame.getChrName(), location, location);

                    frame.centerOnLocation(newLocation + 1);
                    IGV.getInstance().repaintDataAndHeaderPanels();
                    IGV.getInstance().repaintStatusAndZoomSlider();
                }
            };
            panel.setListener(listener);
            SimpleDialog dia = new SimpleDialog("Model Data Confidence Distribution " + panel.getBase(), panel, 800, 500, x, y, image.getImage(), false);
            y += 500;
            if (y > 1000) {
                y = 200;
                x += 500;
            }
        }

    }

    public void createDistScreenShot(AlignmentTrack track, boolean forward, boolean reverse, String filename, boolean closeAfter) {
        this.track = track;
        this.dataManager = track.getDataManager();
        ReferenceFrame frame = FrameManager.getDefaultFrame();
        int location = (int) (frame.getOrigin() + frame.getEnd()) / 2;
        log.info("Frame center=" + frame.getCenter());
        log.info("Got location " + location);
        ConfidenceDistribution[] distributions = getDistributions(forward, reverse, frame, location);


        final ArrayList<DistPanel> panels = DistPanel.createPanels(distributions, 2);
        int count = 0;
        for (DistPanel pan : panels) {

            JFrame f = new JFrame();
            f.getContentPane().add(pan);
            f.setSize(800, 600);
            f.setVisible(true);
            try {
                String file = filename + count + ".png";
                count++;
                log.info("createDistScreenShot: Trying to write image to " + file);
                IGV.getInstance().createSnapshotNonInteractive(pan.getCenter(), new File(file));
            } catch (Exception ex) {
                log.error(ex);
            }
            if (closeAfter) {
                f.dispose();
            }
        }

    }

    private ConfidenceDistribution[] getDistributions(boolean forward, boolean reverse, ReferenceFrame frame, int location) {
        ConfidenceDistribution distributions[] = null;
        if (forward || reverse) {
            ArrayList<ConfidenceDistribution> dists = getDistribution(frame, location, forward, reverse);
            distributions = new ConfidenceDistribution[dists.size()];
            for (int i = 0; i < dists.size(); i++) {
                distributions[i] = dists.get(i);
            }
        } else {
            ArrayList<ConfidenceDistribution> distsf = getDistribution(frame, location, true, false);
            ArrayList<ConfidenceDistribution> distsr = getDistribution(frame, location, false, true);
            distributions = new ConfidenceDistribution[distsf.size() + distsr.size()];
            for (int i = 0; i < distsf.size(); i++) {
                distributions[i] = distsf.get(i);
            }
            for (int i = 0; i < distsr.size(); i++) {
                distributions[i + distsf.size()] = distsr.get(i);
            }

        }
        return distributions;
    }

    /**
     * by default, returns both for forward and backward strand
     */
    private ArrayList<ConfidenceDistribution> getDistribution(ReferenceFrame frame, int location) {
        return getDistribution(frame, location, true, true);
    }

    private ArrayList<ConfidenceDistribution> getDistribution(ReferenceFrame frame, int location, boolean forward, boolean reverse) {

        return ConfidenceDistribution.extractDistributions(dataManager, frame, location, forward, reverse);
    }

    private IonogramAlignment getIonogramAlignment(ReferenceFrame frame, int center_location, int nrbases_left_right, boolean forward, AbstractAlignment alignment, boolean multi) {

        IonogramAlignment ionoalign = IonogramAlignment.extractIonogramAlignment(dataManager, frame, center_location, nrbases_left_right, forward, alignment, multi);
        return ionoalign;
    }
}
