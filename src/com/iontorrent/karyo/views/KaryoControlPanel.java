/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.karyo.filter.FilterListPanel;
import com.iontorrent.cnv.CnvController;
import com.iontorrent.cnv.CnvData;
import com.iontorrent.cnv.CnvPanel;
import com.iontorrent.guiutils.FlowPanel;
import com.iontorrent.karyo.filter.FilterController;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.apache.log4j.Logger;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal Roth
 */
public class KaryoControlPanel extends javax.swing.JPanel {

    private KaryoManager manager;
    private Frame frame;
    private KaryoOverviewPanel view;
    private JFrame kframe;
    private JLabel messagelabel;
    private static KaryoControlPanel control;

    public KaryoManager getManager() {
        return manager;
    }

    public static KaryoControlPanel getPanel(Frame frame) {
        if (control == null) {
            control = new KaryoControlPanel(frame);
        }
        return control;
    }

    /**
     * Creates new form KaryoControlPanel
     */
    private KaryoControlPanel(Frame frame) {
        initComponents();
        this.frame = frame;
//        if (!IGV.DEBUG) {
//            this.btnFilter.setVisible(false);
//        }
        manager = KaryoManager.getManager(frame, this);
    }

    public void showPanel(int width, int height) {
        if (kframe != null) {
            kframe.dispose();
        }
        kframe = new JFrame("Karyotype View");
        kframe.setSize(width, height);
        //if (view == null) {
        recreateView();
        //   }

        kframe.getContentPane().add(this);
        if (frame.getIconImage() != null) {
            kframe.setIconImage(frame.getIconImage());
        }
        // kframe.setSize(1600, 800);
        kframe.setVisible(true);
        kframe.toFront();
        // view.test1();
        view.repaint();

        // also show CNV Panel

//       

    }

    public void recreateView() {
        recreateView(true);
    }

    public void showKaryoStatusMessage(String msg) {  
        if (msg == null) msg = "Status: OK";
        if (messagelabel != null) {
            this.remove(messagelabel);
            messagelabel.setText(msg);
        }
        else messagelabel = new JLabel(msg);           
        add("South", messagelabel);
        
      //  p("Added msg label: "+msg);
    }
    public void recreateView(boolean loadData) {
        recreateView(loadData, "");
    }
    public void recreateView(boolean loadData, String msg) {
        p("=============== recreating view, loadData=" + loadData);
        if (view != null) {            
            this.remove(view);
        }        
        if (loadData) {
            msg = "<html><h3><font color='000066'>Loading tracks... you can still click on a chromosome to see details, but the data may not show for a while for large tracks...</font></h3></html>";
        }
        view = manager.createOverView(msg);
        showKaryoStatusMessage(msg);       
        add("Center", view);
        if (loadData) {
            p("load Data is true");
            view.invalidate();
            view.revalidate();
            view.loadTracks();
        } else {
            view.addTracksToOverview(false);            
        }
        panWest.setLayout(new BorderLayout());
        panWest.removeAll();
        JSplitPane westsplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        SimpleTrackListPanel trackp = new SimpleTrackListPanel(manager, false, this);
        westsplit.add(trackp);
        FilterListPanel filterp = new FilterListPanel(manager, false, this);
        westsplit.add(filterp);
        westsplit.setDividerLocation(0.5);
        panWest.add("North", westsplit);    
        this.invalidate();
        this.revalidate();
        this.repaint();
        view.invalidate();
        view.revalidate();
        westsplit.invalidate();                
        westsplit.revalidate();
        panWest.invalidate();
        panWest.revalidate();
        view.setSliderValue(10);
       // p("================================================= control repainted");
    }

    private void showCnvPanel(CnvData data) {
        JFrame f = new JFrame("CNV Whole Genome Plot");
        f.setSize(1200, 800);
        if (frame.getIconImage() != null) {
            f.setIconImage(frame.getIconImage());
        }
        CnvPanel cnv = new CnvPanel(manager, data);
        f.getContentPane().add(cnv);
        f.setVisible(true);
        f.toFront();
        // view.test1();
        f.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnIgv = new javax.swing.JButton();
        btnFilter = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnReload = new javax.swing.JButton();
        btnCnv = new javax.swing.JButton();
        panSouth = new javax.swing.JPanel();
        panWest = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        jToolBar1.setRollover(true);

        btnIgv.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/icons/view-list-icons-2.png"))); // NOI18N
        btnIgv.setText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnIgv.text")); // NOI18N
        btnIgv.setToolTipText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnIgv.toolTipText")); // NOI18N
        btnIgv.setFocusable(false);
        btnIgv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnIgv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnIgv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIgvActionPerformed(evt);
            }
        });
        jToolBar1.add(btnIgv);

        btnFilter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/icons/filter.png"))); // NOI18N
        btnFilter.setText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnFilter.text")); // NOI18N
        btnFilter.setToolTipText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnFilter.toolTipText")); // NOI18N
        btnFilter.setFocusable(false);
        btnFilter.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFilter.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFilterActionPerformed(evt);
            }
        });
        jToolBar1.add(btnFilter);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/icons/export.png"))); // NOI18N
        btnExport.setText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        jToolBar1.add(btnExport);

        btnReload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/icons/database-refresh.png"))); // NOI18N
        btnReload.setText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnReload.text")); // NOI18N
        btnReload.setToolTipText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnReload.toolTipText")); // NOI18N
        btnReload.setFocusable(false);
        btnReload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnReload.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnReload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReloadActionPerformed(evt);
            }
        });
        jToolBar1.add(btnReload);

        btnCnv.setText(org.openide.util.NbBundle.getMessage(KaryoControlPanel.class, "KaryoControlPanel.btnCnv.text")); // NOI18N
        btnCnv.setFocusable(false);
        btnCnv.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCnv.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCnv.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCnvActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCnv);

        add(jToolBar1, java.awt.BorderLayout.PAGE_START);
        add(panSouth, java.awt.BorderLayout.PAGE_END);
        add(panWest, java.awt.BorderLayout.LINE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnReloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReloadActionPerformed
        recreateView();
    }//GEN-LAST:event_btnReloadActionPerformed

    private void btnFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFilterActionPerformed
        FilterController fil = new FilterController(this.manager);
        if (fil.getNrTracksWithFilters() > 0) {
            int ans = JOptionPane.showConfirmDialog(null, fil, "Pick parameters for filters", JOptionPane.OK_CANCEL_OPTION);
            if (ans == JOptionPane.OK_OPTION) {
                fil.okClicked();
                // now apply filters
                p("Should now apply new filters to tree - redraw?");
                this.recreateView(false);

            }
        }
    }//GEN-LAST:event_btnFilterActionPerformed

    private void btnIgvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnIgvActionPerformed
        if (view.getCurrent_chromosome() == null) {
            int ans = JOptionPane.showConfirmDialog(null, "Click on a chromosome to select one", "Nothing selected", JOptionPane.OK_OPTION);
            return;
        }
        view.showLocation(view.getCurrent_chromosome(), view.getCurrent_location());
    }//GEN-LAST:event_btnIgvActionPerformed

    private void btnCnvActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCnvActionPerformed
        loadCnv();
    }//GEN-LAST:event_btnCnvActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        IGV igv = IGV.getInstance();
        igv.saveImage(view);
    }//GEN-LAST:event_btnExportActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCnv;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnFilter;
    private javax.swing.JButton btnIgv;
    private javax.swing.JButton btnReload;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JPanel panSouth;
    private javax.swing.JPanel panWest;
    // End of variables declaration//GEN-END:variables

    private void p(String msg) {
         Logger.getLogger("KaryoControlPanel").info(msg);
      //  System.out.println("KaryoControlPanel: " + msg);
    }

    public void loadCnv() {
        CnvController cont = new CnvController(null);
        if (!cont.gatherParameters()) {
            return;
        }
        CnvData data = cont.readData();
        if (data != null) {
            showCnvPanel(data);
        }
    }
}
