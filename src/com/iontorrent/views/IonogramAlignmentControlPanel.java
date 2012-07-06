/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views;

import com.iontorrent.data.Ionogram;
import com.iontorrent.data.IonogramAlignment;
import com.iontorrent.utils.BagPanel;
import com.iontorrent.utils.FileTools;
import com.iontorrent.utils.LocationListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
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
public class IonogramAlignmentControlPanel extends javax.swing.JPanel {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IonogramAlignmentControlPanel.class);
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

    /**
     * Creates new form IonogramAlignmentPanel
     */
    public IonogramAlignmentControlPanel(int location, IonogramAlignment alignment) {
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
        setAlignment(alignment, location);
        add("Center", main);

    }

    public void setAlignment(IonogramAlignment alignment, int chromosomepos) {
        location = chromosomepos;
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
        IonogramPanel header = new IonogramPanel(ionograms.get(0), alignment, true);

       
        int slotheight = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN)+IonogramPanel.TOP;
        int slotwidth = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN) + IonogramPanel.BORDER;
        int lblwidth = 40;
        for (Ionogram iono : ionograms) {
           // p("Adding ionogram to alignmentpanel: " + iono.toString());
            IonogramPanel ionopanel = new IonogramPanel(iono, alignment, false);
            //ionopanel.setToolTipText(iono.getFloworder());
            center.add(ionopanel);
            JLabel lbl = new JLabel(iono.getReadname());
            lbl.setBackground(Color.white);
            lbl.setToolTipText("<html>"+iono.toHtml()+"</html>");
          //  p("Got floworder: " + iono.getFloworder());
            lbl.setSize(lblwidth, slotheight);
            lbl.setMinimumSize(new Dimension(30, slotheight));
            lbl.setMaximumSize(new Dimension(lblwidth, slotheight));
            lbl.setPreferredSize(new Dimension(lblwidth, slotheight));
            labels.add(lbl);
        }

        int totwidth = slotwidth * alignment.getNrslots() + IonogramPanel.BORDER;
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

        corner = new JLabel("<html>"+alignment.getLocus()+"</html>");
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
        while (parent.getParent() != null ) {
            parent = parent.getParent();
        }
        if (parent != null && parent instanceof JFrame) {
            JFrame f = (JFrame)parent;
            p("Found parent frame :-)");
            f.setTitle(alignment.getTitle());
        }
        // I know this is a hack, but it just won't repaint... not sure why
        paintImmediately(0,0,1000,1000);
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
        btnSave = new javax.swing.JButton();
        btnConfigure = new javax.swing.JButton();
        btnTSL = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        spinBin = new javax.swing.JSpinner();
        btnLeft = new javax.swing.JButton();
        btnRight = new javax.swing.JButton();
        leftbar = new javax.swing.JToolBar();
        zoomIn = new javax.swing.JButton();
        zoomOut = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        topbar.setRollover(true);

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

        btnTSL.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/chip_16.png"))); // NOI18N
        btnTSL.setToolTipText("Open Torrent Scout light in a browser and load the currently shown reads");
        btnTSL.setFocusable(false);
        btnTSL.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTSL.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnTSL.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTSLActionPerformed(evt);
            }
        });
        topbar.add(btnTSL);

        jLabel1.setText("Ionogram height:");
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
        getListener().locationChanged(location);
    }
    private void btnTSLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTSLActionPerformed
        String server = PreferenceManager.getInstance().get(PreferenceManager.IONTORRENT_SERVER);
        String res = PreferenceManager.getInstance().get(PreferenceManager.IONTORRENT_RESULTS);
        String bam = null;
        if (res.endsWith(".bam")) {
            bam = res;
            File f = new File(bam);
            res = f.getParent().toString();

        }
        if (server == null || server.length() < 1) {
            server = "ioneast.ite";
        }

        if (!server.startsWith("http")) {
            server = "http://" + server;
        }

        if (server.lastIndexOf(":") < 7) {
            server += ":8080";
        }
        String url = server + "/TSL?restartApplication";
        if (res != null && res.length() > 0) {
            url += "&res_dir=" + res;
        }
        if (bam != null && bam.length() > 0) {
            url += "&bam=" + bam;
        }
        String readnames = this.getReadNames();
        if (readnames != null && readnames.length() > 0) {
            url += "&read_names=" + readnames;
        }

        JTextField txt = new JTextField();
        txt.setText(url);;
        if (!java.awt.Desktop.isDesktopSupported()) {
            JOptionPane.showMessageDialog(this, txt, "Please open a browser and paste the url below:", JOptionPane.OK_OPTION);
            return;
        }
        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
        try {

            java.net.URI uri = new java.net.URI(url);
            desktop.browse(uri);
            JOptionPane.showMessageDialog(this, "Raw data", "When TSL opens, pick the folder with the raw data to view raw traces\nand specify the .sff file to see ionograms", JOptionPane.OK_OPTION);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, txt, "Please open a browser and paste the url below:", JOptionPane.OK_OPTION);
        }
    }//GEN-LAST:event_btnTSLActionPerformed

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
    private javax.swing.JButton btnLeft;
    private javax.swing.JButton btnRight;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnTSL;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JToolBar leftbar;
    private javax.swing.JSpinner spinBin;
    private javax.swing.JToolBar topbar;
    private javax.swing.JButton zoomIn;
    private javax.swing.JButton zoomOut;
    // End of variables declaration//GEN-END:variables
}
