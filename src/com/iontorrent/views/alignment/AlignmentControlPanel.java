/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.alignment;

import com.iontorrent.data.ConfidenceDistribution;
import com.iontorrent.data.Ionogram;
import com.iontorrent.data.IonogramAlignment;
import com.iontorrent.expmodel.ExperimentContext;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.rawdataaccess.ReadFlow;
import com.iontorrent.torrentscout.explorer.process.*;
import com.iontorrent.utils.*;
import com.iontorrent.views.dist.DistPanel;
import com.iontorrent.wellmodel.WellCoordinate;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal Roth
 */
public class AlignmentControlPanel extends javax.swing.JPanel {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlignmentControlPanel.class);
    private LocationListener listener;
    private int iono_height;
    private int location;
    private ArrayList<Ionogram> ionograms;
    private String filename;
    private boolean ignore_events;
    private IonogramAlignment alignment;
    private JPanel center;
    private JPanel labels;
    private BagPanel main;
    private JScrollPane slabels;
    private JScrollPane scenter;
    private JScrollPane sheader;
    private JLabel corner;
    private boolean flowBased;
    private SignalFetchPanel sig;

    /**
     * Creates new form IonogramAlignmentPanel
     */
    public AlignmentControlPanel(int location, IonogramAlignment alignment) {
        this.location = location;

        initComponents();
        this.setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyTyped(evt);
            }
        });
        main = new BagPanel();
        ToolTipManager.sharedInstance().registerComponent(main);
        flowBased = false;

        setAlignment(alignment, location);
        add("Center", main);

    }

    public void recomputeAlignment() {
        alignment.recomputeAlignment();
        flowBased = true;

        setAlignment(alignment, location);
    }

    public void setAlignment(IonogramAlignment alignment, int chromosomepos) {
        location = chromosomepos;
        if (alignment == null) {
            p("Got no alignment");
            return;
        }
        this.ionograms = alignment.getIonograms();
        this.alignment = alignment;
        PreferenceManager prefs = PreferenceManager.getInstance();
        this.iono_height = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN);
        this.ignore_events = true;
        spinBin.setValue(iono_height);
        ignore_events = false;

        if (sheader != null) {
            main.remove(sheader);
        }
        if (slabels != null) {
            main.remove(slabels);
        }
        if (scenter != null) {
            main.remove(scenter);
        }
        if (corner != null) {
            main.remove(corner);
        }

        center = new JPanel();

        labels = new JPanel();
        labels.setBackground(Color.white);
        ToolTipManager.sharedInstance().registerComponent(center);
        ToolTipManager.sharedInstance().registerComponent(labels);
        int nrionograms = ionograms.size();

        center.setLayout(new GridLayout(nrionograms, 1));
        labels.setLayout(new GridLayout(nrionograms, 1));
        p("Slots: " + alignment.getNrslots());
        // first one just shows bases
        AlignmentPanel header = new AlignmentPanel(ionograms.get(0), alignment, true);


        int slotheight = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN) + AlignmentPanel.TOP;
        int slotwidth = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN) + AlignmentPanel.BORDER;
        int lblwidth = 40;
        for (Ionogram iono : ionograms) {
            // p("Adding ionogram to alignmentpanel: " + iono.toString());
            AlignmentPanel ionopanel = new AlignmentPanel(iono, alignment, false);
            //ionopanel.setToolTipText(iono.getFloworder());
            center.add(ionopanel);
            JLabel lbl = new JLabel(iono.getReadname());
            lbl.setBackground(Color.white);
            lbl.setToolTipText("<html>" + iono.toHtml() + "</html>");
            //  p("Got floworder: " + iono.getFloworder());
            lbl.setSize(lblwidth, slotheight);
            lbl.setMinimumSize(new Dimension(30, slotheight));
            lbl.setMaximumSize(new Dimension(lblwidth, slotheight));
            lbl.setPreferredSize(new Dimension(lblwidth, slotheight));
            labels.add(lbl);
        }

        int totwidth = slotwidth * alignment.getNrslots() + AlignmentPanel.BORDER;
        int totheight = (nrionograms + 1) * slotheight;
        center.setSize(totwidth, totheight);
        labels.setSize(lblwidth, totheight);
        labels.setMaximumSize(new Dimension(lblwidth, totheight));
        labels.setPreferredSize(new Dimension(lblwidth, totheight));

        center.setMinimumSize(new Dimension(totwidth, totheight));
        //   p("Setting size of center: " + totheight + ", single height=" + slotheight);

        slabels = new JScrollPane(labels, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scenter = new JScrollPane(center);
        sheader = new JScrollPane(header, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        slabels.getVerticalScrollBar().setModel(scenter.getVerticalScrollBar().getModel());
        sheader.getHorizontalScrollBar().setModel(scenter.getHorizontalScrollBar().getModel());


        corner = new JLabel("<html><b>Base</b> alignment from BAM file at <br>" + alignment.getLocus() + "</html>");
        if (flowBased) {
            corner.setText("<html><b>Flow space</b> alignment at <br>" + alignment.getLocus() + "</html>");
        }
        corner.setBackground(Color.white);
        corner.setSize(lblwidth, slotheight);
        corner.setMaximumSize(new Dimension(lblwidth, slotheight));
        corner.setPreferredSize(new Dimension(lblwidth, slotheight));
        int weigth = 15;
        main.place(0, 0, 1, 1, corner);
        main.place(1, 0, weigth, 1, weigth, 1, sheader);
        main.place(0, 1, 1, weigth, 1, weigth, slabels);
        main.place(1, 1, weigth, weigth, weigth, weigth, scenter);

        this.repaint();
        main.invalidate();
        main.revalidate();

        Container parent = this;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        if (parent != null && parent instanceof JFrame) {
            JFrame f = (JFrame) parent;
            p("Found parent frame :-)");
            f.setTitle(alignment.getTitle());
        }
        // I know this is a hack, but it just won't repaint... not sure why
        paintImmediately(0, 0, 1000, 1000);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topbar = new javax.swing.JToolBar();
        btnRecompute = new javax.swing.JButton();
        btnDist = new javax.swing.JButton();
        btnRaw = new javax.swing.JButton();
        btnLeft = new javax.swing.JButton();
        btnRight = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        spinBin = new javax.swing.JSpinner();
        btnConfigure = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnLink = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        leftbar = new javax.swing.JToolBar();
        zoomIn = new javax.swing.JButton();
        zoomOut = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        topbar.setRollover(true);

        btnRecompute.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/msa_flows.png"))); // NOI18N
        btnRecompute.setToolTipText("Recompute alignment by considering empty flows as well");
        btnRecompute.setFocusable(false);
        btnRecompute.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRecompute.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRecompute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecomputeActionPerformed(evt);
            }
        });
        topbar.add(btnRecompute);

        btnDist.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/dist.png"))); // NOI18N
        btnDist.setToolTipText("show confidence distribution for currently selected slot");
        btnDist.setFocusable(false);
        btnDist.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDist.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDist.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDistActionPerformed(evt);
            }
        });
        topbar.add(btnDist);

        btnRaw.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/raw.png"))); // NOI18N
        btnRaw.setToolTipText("Load the raw traces of the selected cells a via IonRetriever (a server side Data API)");
        btnRaw.setFocusable(false);
        btnRaw.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRaw.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRaw.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRawActionPerformed(evt);
            }
        });
        topbar.add(btnRaw);

        btnLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/arrow-left.png"))); // NOI18N
        btnLeft.setToolTipText("move to the next base on the left");
        btnLeft.setFocusable(false);
        btnLeft.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLeft.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLeft.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLeftActionPerformed(evt);
            }
        });
        topbar.add(btnLeft);

        btnRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/arrow-right.png"))); // NOI18N
        btnRight.setToolTipText("move to the next base on the right");
        btnRight.setFocusable(false);
        btnRight.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRight.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRightActionPerformed(evt);
            }
        });
        topbar.add(btnRight);

        jLabel1.setText("Alignment height:");
        topbar.add(jLabel1);

        spinBin.setModel(new javax.swing.SpinnerNumberModel(50, 10, 200, 5));
        spinBin.setMaximumSize(new java.awt.Dimension(50, 19));
        spinBin.setMinimumSize(new java.awt.Dimension(47, 18));
        spinBin.setPreferredSize(new java.awt.Dimension(47, 18));
        spinBin.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                spinBinStateChanged(evt);
            }
        });
        topbar.add(spinBin);

        btnConfigure.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/configure.png"))); // NOI18N
        btnConfigure.setToolTipText("Change ionogram alignment view settings");
        btnConfigure.setFocusable(false);
        btnConfigure.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConfigure.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigureActionPerformed(evt);
            }
        });
        topbar.add(btnConfigure);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/save.png"))); // NOI18N
        btnSave.setToolTipText("Save the image of the alignment or data in .csv file (to be used in Excel for instance)");
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        topbar.add(btnSave);

        btnLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/link.png"))); // NOI18N
        btnLink.setToolTipText("Open Torrent Scout light in a browser and load the currently shown reads");
        btnLink.setFocusable(false);
        btnLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLinkActionPerformed(evt);
            }
        });
        topbar.add(btnLink);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/view-refresh-3.png"))); // NOI18N
        btnRefresh.setToolTipText("Reload original alignment from BAM file (base space)");
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        topbar.add(btnRefresh);

        add(topbar, java.awt.BorderLayout.PAGE_START);

        leftbar.setOrientation(1);
        leftbar.setRollover(true);

        zoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/zoom-in.png"))); // NOI18N
        zoomIn.setToolTipText("Zoom in (vertically)");
        zoomIn.setFocusable(false);
        zoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInActionPerformed(evt);
            }
        });
        leftbar.add(zoomIn);

        zoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/zoom-out.png"))); // NOI18N
        zoomOut.setToolTipText("Zoom out (vertically)");
        zoomOut.setFocusable(false);
        zoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        zoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutActionPerformed(evt);
            }
        });
        leftbar.add(zoomOut);

        add(leftbar, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed


        String[] options = new String[2];
        String msg = "1) Save image of chart\n";
        msg += "2) Save data in .csv file\n";
        options[0] = "1) Image";
        options[1] = "2) Data";

        int ans = JOptionPane.showOptionDialog(this, msg, "Export",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (ans < 0) {
            return;
        }
        if (ans == 0) {
            doSaveImageAction();
        } else if (ans == 1) {
            doSaveDataAction();
        }

    }//GEN-LAST:event_btnSaveActionPerformed
    private void doSaveDataAction() {
        // get file name from user
        filename = FileTools.getFile("File to store ionogram alignment", ".csv", filename, true);
        if (filename == null || filename.length() < 1) {
            return;
        }
        String csv = getCsvString();

        File fileToSave = new File(filename);
        FileTools.writeStringToFile(fileToSave, csv, false);
    }

    public String getCsvString() {
        String s = alignment.toString();
        return s;
    }

    public boolean export() {
        if (center == null) {
            JOptionPane.showMessageDialog(this, "There is no image to export");
            return false;
        }
        String file = FileTools.getFile("Save image to a file", "*.*", null, true);
        return export(file);
    }

    public boolean export(String file) {
        if (file == null || file.length() < 3) {
            JOptionPane.showMessageDialog(this, "I need to know if it is a .png or a .jpg file");
            return false;
        }
        if (center == null) {
            return false;
        }
        File f = new File(file);
        String ext = file.substring(file.length() - 3);
        RenderedImage image = myCreateImage();
        try {
            return ImageIO.write(image, ext, f);
        } catch (IOException ex) {
            err("Could not write image to file " + f);
        }
        return false;
    }
    // Returns a generated image.

    public RenderedImage myCreateImage() {
        return myCreateImage(main.getWidth(), main.getHeight());
    }

    public RenderedImage myCreateImage(int minw, int minh) {

        BufferedImage bufferedImage = new BufferedImage(minw, minh, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        main.paintAll(g2d);

        return bufferedImage;
    }

    private void doSaveImageAction() {
        export();
    }

    private void btnConfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigureActionPerformed
        // let user pick bin sizem chart type etc
        IGV.getInstance().doViewPreferences("IonTorrent");

        // in case the hide setting was changed, we  have to recompute the distributions again
        refresh();
    }//GEN-LAST:event_btnConfigureActionPerformed

    public void refresh() {
        flowBased = false;
        getListener().locationChanged(location);

    }
    private void btnRawActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRawActionPerformed


        final long loc = alignment.getChromosome_center_location();

        final String chr = alignment.getChromosome();
        // LinkUtils.linkToTSL(readnames, chr, loc);

        final ArrayList<ReadFlow> readflows = new ArrayList<ReadFlow>();

        int nrselected = 0;
        for (Ionogram iono : this.alignment.getIonograms()) {
            int center = alignment.getCenterSlot();
            if (iono.isSelected(center)) {
                nrselected++;
            }
        }
        int max = 10;
        if (nrselected == 0) {
            JOptionPane.showMessageDialog(IGV.getMainFrame(), "I will get the first " + max + " raw traces in the alignment.\nOtherwise, just select the (center) flows you would like me to get!");
        }
        for (Ionogram iono : this.alignment.getIonograms()) {
            if (readflows.size() > max) {
                break;
            }
            WellCoordinate coord = iono.getCoord();

            iono.getChromosome_center_location();
            int center = alignment.getCenterSlot();
            if (nrselected < 1 || iono.isSelected(center)) {
                FlowValue fv = iono.getSlotrow()[center];

                if (fv != null) {
                    p("got coord: " + coord + " and fv:" + fv);
                    ReadFlow rf = new ReadFlow(fv.getFlowPosition(), coord.getX(), coord.getY());
                    rf.setFlowValue(fv);
                    readflows.add(rf);
                }
            }
        }

        PreferenceManager prefs = PreferenceManager.getInstance();
        String server = prefs.get(PreferenceManager.IONTORRENT_SERVER);
        String expinfo = prefs.get(PreferenceManager.BAM_FILE);

        ExperimentContext exp = new ExperimentContext();
        exp.setExperimentInfo(expinfo);

        boolean show = sig == null;
        if (sig == null) {
            sig = new SignalFetchPanel(exp, readflows, "location " + chr + ":" + location);

        } else {
            sig.setReadflows(readflows);
            sig.setExp(exp);
            sig.setTitle("location " + chr + ":" + location);

        }
        sig.setServer(server);
        if (show || sig.isShow()) {
            sig.showPanel();
        } else {
            sig.loadDataFromServer();
        }

    }//GEN-LAST:event_btnRawActionPerformed

    private void spinBinStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_spinBinStateChanged
        if (ignore_events) {
            return;
        }
        if (this.spinBin.getValue() != null) {
            changeIonoHeight(((Integer) spinBin.getValue()).intValue());
        }
    }//GEN-LAST:event_spinBinStateChanged

    private void changeIonoHeight(int new_iono_height) {
        this.iono_height = new_iono_height;
        PreferenceManager pref = PreferenceManager.getInstance();
        pref.put(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN, "" + iono_height);
        refresh();
        if (((Integer) spinBin.getValue()).intValue() != new_iono_height) {
            ignore_events = true;
            spinBin.setValue(new_iono_height);
            ignore_events = false;
        }
    }
    private void btnLeftActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLeftActionPerformed
        moveLeft();
    }//GEN-LAST:event_btnLeftActionPerformed

    private void btnRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRightActionPerformed
        moveRight();
    }//GEN-LAST:event_btnRightActionPerformed

    private void zoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInActionPerformed
        changeIonoHeight(this.iono_height + 5);
    }//GEN-LAST:event_zoomInActionPerformed

    private void zoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutActionPerformed
        changeIonoHeight(this.iono_height - 5);
    }//GEN-LAST:event_zoomOutActionPerformed

    private void btnRecomputeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecomputeActionPerformed
        recomputeAlignment();
    }//GEN-LAST:event_btnRecomputeActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnDistActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDistActionPerformed
        showDistForSlot(this.alignment.getCenterSlot());
    }//GEN-LAST:event_btnDistActionPerformed

    private void btnLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLinkActionPerformed
        String readnames = this.getReadNames();
        final long loc = alignment.getChromosome_center_location();
        final String chr = alignment.getChromosome();
        LinkUtils.linkToTSL(readnames, chr, loc);
    }//GEN-LAST:event_btnLinkActionPerformed

    private void showDistForSlot(int slot) {

        if (ionograms == null || ionograms.isEmpty()) {
            return;
        }
        final String locus = ionograms.get(0).getLocusinfo();
        ConfidenceDistribution[] distributions = alignment.getDistribution(locus, slot);

        final ArrayList<DistPanel> pans = DistPanel.createPanels(distributions, 3);
        for (final DistPanel pan : pans) {
            LocationListener listener = new LocationListener() {

                @Override
                public void locationChanged(int newslot) {
                    log.info("Got new location from panel: " + newslot + ", (old location was: " + location + ")");
                    ConfidenceDistribution[] newdist = alignment.getDistribution(locus, newslot);
                    pan.setDistributions(newdist);
                    //frame.jumpTo(frame.getChrName(), location, location);
                }
            };
            pan.setListener(listener);
            // listen to left/right mouse clicks from panel and navigate accordingly
            ImageIcon image = new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/chip_16.png"));
            SimpleDialog dia = new SimpleDialog("Model-Data Confidence", pan, 800, 500, 200, 200, image.getImage());
        }


    }

    private void moveLeft() {
        if (getListener() != null) {
            getListener().locationChanged(location - 1);
        }
    }

    private void moveRight() {
        if (getListener() != null) {
            getListener().locationChanged(location + 1);
        }
    }

    /**
     * @return the listener
     */
    public LocationListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(LocationListener listener) {
        this.listener = listener;
    }

    private void formKeyTyped(java.awt.event.KeyEvent evt) {
        handleKeyEvent(evt);
    }

    public void handleKeyEvent(KeyEvent e) {
        int c = e.getKeyCode();
        p("Got key: " + c + ", left/right etc: " + KeyEvent.VK_LEFT + "/" + KeyEvent.VK_RIGHT + "/" + KeyEvent.VK_UP + "/" + KeyEvent.VK_DOWN + "/" + KeyEvent.VK_DELETE);
        if (c == KeyEvent.VK_LEFT || c == 37) {
            this.moveLeft();
        } else if (c == KeyEvent.VK_RIGHT || c == 39) {
            this.moveRight();
        } else if (c == KeyEvent.VK_UP || c == KeyEvent.VK_PAGE_UP || c == 38) {
            changeIonoHeight(this.iono_height + 5);
        } else if (c == KeyEvent.VK_DOWN || c == KeyEvent.VK_PAGE_DOWN || c == 40) {
            changeIonoHeight(iono_height - 5);
        }
    }

    private String getReadNames() {
        StringBuilder rinfo = new StringBuilder();
        for (int i = 0; i < ionograms.size(); i++) {
            Ionogram iono = ionograms.get(i);
            rinfo = rinfo.append("_").append(iono.getReadname());
        }
        return rinfo.toString();
    }

    private void p(String msg) {
        log.info(msg);
    }

    private void err(String msg) {
        log.error(msg);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConfigure;
    private javax.swing.JButton btnDist;
    private javax.swing.JButton btnLeft;
    private javax.swing.JButton btnLink;
    private javax.swing.JButton btnRaw;
    private javax.swing.JButton btnRecompute;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRight;
    private javax.swing.JButton btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JToolBar leftbar;
    private javax.swing.JSpinner spinBin;
    private javax.swing.JToolBar topbar;
    private javax.swing.JButton zoomIn;
    private javax.swing.JButton zoomOut;
    // End of variables declaration//GEN-END:variables
}
