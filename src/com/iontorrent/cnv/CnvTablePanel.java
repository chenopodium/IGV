/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.utils.GuiUtils;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import org.apache.log4j.Logger;


/**
 *
 * @author Chantal
 */
public class CnvTablePanel extends javax.swing.JPanel {

    CnvTableModel model;
    private JTable table;
    private static final String DESC = "description";
    private static final String CHR = "chromosome";
    private static final String RATIO = "value with ratio";
    private static final String SAMPLE = "sample value";
    private static final String CONTROL = "control value";
    private static final String POS = "start position";
    private static final String END = "end position";
    private static final int CHR_POS = 0;
    private static final int POS_POS = 1;
    private static final int RATIO_POS = 2;
    private static final int DESC_POS = 3;
    private static final int SAMPLE_POS = 4;
    private static final int CONTROL_POS = 5;
    private static final int END_POS = 6;
    
    private TableCol active;
    // static to remember the values!
    private static TableCol[] cols;

    static int COLW = 100;
    /**
     * Creates new form CnvTablePanel
     */
    public CnvTablePanel(CnvTableModel model) {
        initComponents();
       
        table = new JTable();
        panTable.setLayout(new BorderLayout());
        JScrollPane scroll = new JScrollPane(table);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        panTable.add("Center", scroll );
        table.setModel(model);

        table.getTableHeader().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                p("Table header clicked");
                tableClicked(evt);
            }
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                p("Table clicked");
                tableClicked(evt);
            }
        });
        int minw = table.getColumnCount()*COLW;
        int minh = table.getRowCount()*18;
        Dimension d = new Dimension(minw, minh);
       /// table.setPreferredSize(d);
       // table.setMinimumSize(d);
        scroll.setMaximumSize(new Dimension(1000, 400));
        panTable.setMaximumSize(new Dimension(1000, 400));
        TableCol[] oldcols = cols;

        cols = new TableCol[7];
        cols[0] = new TableCol(CHR, btnChr, txtChr, lblChr, new Color(255, 255, 220));
        cols[1] = new TableCol(POS, btnPos, txtPos, lblPos, new Color(220, 255, 220));
        cols[2] = new TableCol(RATIO, btnRatio, txtRatio, lblRatio, new Color(255, 220, 220));
        cols[3] = new TableCol(DESC, btnDesc, txtDesc, lblDesc, new Color(230, 230, 230));
        cols[4] = new TableCol(SAMPLE, btnSample, txtSample, lblSample, new Color(220, 255, 255));
        cols[5] = new TableCol(CONTROL, btnControl, txtControl, lblControl, new Color(200, 200, 255));
        cols[6] = new TableCol(END, btnEnd, txtEnd, lblEnd, new Color(200, 245, 200));
        if (oldcols != null) {
            for (int i = 0; i < cols.length; i++ ){
                TableCol old = oldcols[i];                
                if (old.curcol >= 0) {
                    System.out.println("Got a curcol for " + old.name + ":" + old.curcol);
                    TableCol tc = cols[i];
                    tc.txt.setText("" + tc.curcol);
                    tc.lbl.setText(table.getModel().getColumnName(tc.curcol));
                }
            }
        }

        updateRenderers();
    }

    private void p(String s) {
        Logger.getLogger("CnvTablePanel").info(s);
    }
    public boolean tableClicked(MouseEvent evt) {
        if (active == null) {
            GuiUtils.showNonModalMsg("Click the button for which you wish to update the column");
            return true;
        }
        int row = this.table.rowAtPoint(evt.getPoint());
        int col = table.columnAtPoint(evt.getPoint());
        columnPicked(col);
        return false;
    }

    private class TableCol {

        JTextField txt;
        JLabel lbl;
        Color color;
        JButton btn;
        String name;
        int curcol = -1;

        public TableCol(String name, JButton btn, JTextField txt, JLabel lbl, Color color) {
            this.txt = txt;
            this.name = name;
            this.lbl = lbl;
            this.color = color;
            this.btn = btn;
        }
    }

    private void pickColumn() {

        active.txt.setBackground(active.color);
        active.btn.setBackground(active.color);
        active.lbl.setText("Pick the column");
        // MessageUtils.showMessage("Pick the column in the table with "+activeName);
        GuiUtils.showNonModelMsg("Pick a column", "Pick the column in the table with " + active.name, true, 10);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panNorth = new javax.swing.JPanel();
        panTable = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        btnDesc = new javax.swing.JButton();
        btnChr = new javax.swing.JButton();
        btnPos = new javax.swing.JButton();
        btnRatio = new javax.swing.JButton();
        txtDesc = new javax.swing.JTextField();
        txtChr = new javax.swing.JTextField();
        txtPos = new javax.swing.JTextField();
        txtRatio = new javax.swing.JTextField();
        lblDesc = new javax.swing.JLabel();
        lblChr = new javax.swing.JLabel();
        lblPos = new javax.swing.JLabel();
        lblRatio = new javax.swing.JLabel();
        btnEnd = new javax.swing.JButton();
        txtEnd = new javax.swing.JTextField();
        lblEnd = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        btnControl = new javax.swing.JButton();
        lblSample = new javax.swing.JLabel();
        lblControl = new javax.swing.JLabel();
        txtSample = new javax.swing.JTextField();
        btnSample = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        txtControl = new javax.swing.JTextField();

        javax.swing.GroupLayout panNorthLayout = new javax.swing.GroupLayout(panNorth);
        panNorth.setLayout(panNorthLayout);
        panNorthLayout.setHorizontalGroup(
            panNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 630, Short.MAX_VALUE)
        );
        panNorthLayout.setVerticalGroup(
            panNorthLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 157, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout panTableLayout = new javax.swing.GroupLayout(panTable);
        panTable.setLayout(panTableLayout);
        panTableLayout.setHorizontalGroup(
            panTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panTableLayout.setVerticalGroup(
            panTableLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 182, Short.MAX_VALUE)
        );

        btnDesc.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnDesc.text")); // NOI18N
        btnDesc.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnDesc.toolTipText")); // NOI18N
        btnDesc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDescActionPerformed(evt);
            }
        });

        btnChr.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnChr.text")); // NOI18N
        btnChr.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnChr.toolTipText")); // NOI18N
        btnChr.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChrActionPerformed(evt);
            }
        });

        btnPos.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnPos.text")); // NOI18N
        btnPos.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnPos.toolTipText")); // NOI18N
        btnPos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPosActionPerformed(evt);
            }
        });

        btnRatio.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnRatio.text")); // NOI18N
        btnRatio.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnRatio.toolTipText")); // NOI18N
        btnRatio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRatioActionPerformed(evt);
            }
        });

        txtDesc.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtDesc.text")); // NOI18N

        txtChr.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtChr.text")); // NOI18N

        txtPos.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtPos.text")); // NOI18N

        txtRatio.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtRatio.text")); // NOI18N
        txtRatio.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtRatio.toolTipText")); // NOI18N

        lblDesc.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblDesc.text")); // NOI18N

        lblChr.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblChr.text")); // NOI18N

        lblPos.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblPos.text")); // NOI18N

        lblRatio.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblRatio.text")); // NOI18N

        btnEnd.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnEnd.text")); // NOI18N
        btnEnd.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnEnd.toolTipText")); // NOI18N
        btnEnd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEndActionPerformed(evt);
            }
        });

        txtEnd.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtEnd.text")); // NOI18N

        lblEnd.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblEnd.text")); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(btnPos, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnChr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(btnRatio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEnd, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtChr, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPos, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtEnd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtDesc, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtRatio, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPos, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                    .addComponent(lblChr, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblEnd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblDesc, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lblRatio, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnChr)
                    .addComponent(txtChr, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblChr, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblPos, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnPos)
                        .addComponent(txtPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblEnd, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnEnd)
                        .addComponent(txtEnd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnDesc)
                        .addComponent(txtDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnRatio)
                        .addComponent(txtRatio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblRatio, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel2Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {lblChr, lblDesc, lblPos, lblRatio});

        jLabel1.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.jLabel1.text")); // NOI18N

        btnControl.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnControl.text")); // NOI18N
        btnControl.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnControl.toolTipText")); // NOI18N
        btnControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnControlActionPerformed(evt);
            }
        });

        lblSample.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblSample.text")); // NOI18N

        lblControl.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.lblControl.text")); // NOI18N

        txtSample.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtSample.text")); // NOI18N
        txtSample.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtSample.toolTipText")); // NOI18N
        txtSample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSampleActionPerformed(evt);
            }
        });

        btnSample.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnSample.text")); // NOI18N
        btnSample.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.btnSample.toolTipText")); // NOI18N
        btnSample.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSampleActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.jLabel2.text")); // NOI18N
        jLabel2.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.jLabel2.toolTipText")); // NOI18N

        txtControl.setText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtControl.text")); // NOI18N
        txtControl.setToolTipText(org.openide.util.NbBundle.getMessage(CnvTablePanel.class, "CnvTablePanel.txtControl.toolTipText")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnControl)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnSample)))
                .addGap(10, 10, 10)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(txtControl, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtSample, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(11, 11, 11)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(lblSample, javax.swing.GroupLayout.DEFAULT_SIZE, 241, Short.MAX_VALUE)
                    .addComponent(lblControl, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnControl)
                        .addComponent(txtControl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnSample)
                                .addComponent(txtSample, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jLabel2))
                            .addComponent(lblSample, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblControl, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2))))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panNorth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(panTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(panNorth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panTable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSampleActionPerformed
        active = cols[SAMPLE_POS];
        pickColumn();
    }//GEN-LAST:event_btnSampleActionPerformed

    private void btnControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnControlActionPerformed
        active = cols[CONTROL_POS];
        pickColumn();
    }//GEN-LAST:event_btnControlActionPerformed

    private void txtSampleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSampleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSampleActionPerformed

    private void btnEndActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEndActionPerformed
         active = cols[END_POS];
        pickColumn();
    }//GEN-LAST:event_btnEndActionPerformed

    private void btnRatioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRatioActionPerformed
        active = cols[RATIO_POS];
        pickColumn();
    }//GEN-LAST:event_btnRatioActionPerformed

    private void btnPosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPosActionPerformed
        active = cols[POS_POS];
        pickColumn();
    }//GEN-LAST:event_btnPosActionPerformed

    private void btnChrActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnChrActionPerformed
        active = cols[CHR_POS];
        pickColumn();
    }//GEN-LAST:event_btnChrActionPerformed

    private void btnDescActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDescActionPerformed
        active = cols[DESC_POS];
        pickColumn();
    }//GEN-LAST:event_btnDescActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnChr;
    private javax.swing.JButton btnControl;
    private javax.swing.JButton btnDesc;
    private javax.swing.JButton btnEnd;
    private javax.swing.JButton btnPos;
    private javax.swing.JButton btnRatio;
    private javax.swing.JButton btnSample;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lblChr;
    private javax.swing.JLabel lblControl;
    private javax.swing.JLabel lblDesc;
    private javax.swing.JLabel lblEnd;
    private javax.swing.JLabel lblPos;
    private javax.swing.JLabel lblRatio;
    private javax.swing.JLabel lblSample;
    private javax.swing.JPanel panNorth;
    private javax.swing.JPanel panTable;
    private javax.swing.JTextField txtChr;
    private javax.swing.JTextField txtControl;
    private javax.swing.JTextField txtDesc;
    private javax.swing.JTextField txtEnd;
    private javax.swing.JTextField txtPos;
    private javax.swing.JTextField txtRatio;
    private javax.swing.JTextField txtSample;
    // End of variables declaration//GEN-END:variables

    /**
     * @return the colChr
     */
    public int getColChr() {
        return cols[CHR_POS].curcol;
    }

    /**
     * @param colChr the colChr to set
     */
    public void setColChr(int colChr) {
        
         setCol(CHR_POS, colChr);
    }

    /**
     * @return the colPos
     */
    public int getColPos() {
        return cols[POS_POS].curcol;
    }
     public int getColEnd() {
        return cols[END_POS].curcol;
    }
     public void setColEnd(int i) {
        setCol(END_POS, i);
    }
     private void setCol(int WHICH, int i) {
        cols[WHICH].curcol = i;
        cols[WHICH].txt.setText(""+i);
     }

    /**
     * @param colPos the colPos to set
     */
    public void setColPos(int col) {
        setCol(POS_POS, col);
       
    }

    /**
     * @return the colDesc
     */
    public int getColDesc() {
        return cols[DESC_POS].curcol;
    }

    /**
     * @param colDesc the colDesc to set
     */
    public void setColDesc(int col) {
          
          setCol(DESC_POS, col);
    }

    /**
     * @return the colRatio
     */
    public int getColRatio() {
        return cols[RATIO_POS].curcol;
    }

    /**
     * @param colRatio the colRatio to set
     */
    public void setColRatio(int col) {
      
        setCol(RATIO_POS, col);
    }

    /**
     * @return the colSample
     */
    public int getColSample() {
        return cols[SAMPLE_POS].curcol;
    }

    /**
     * @param colSample the colSample to set
     */
    public void setColSample(int col) {
         
         setCol(SAMPLE_POS, col);
    }

    /**
     * @return the colControl
     */
    public int getColControl() {
        return cols[CONTROL_POS].curcol;
    }

    /**
     * @param colControl the colControl to set
     */
    public void setColControl(int col) {
       
       setCol(CONTROL_POS, col);
    }

    public void columnPicked(int col) {

        if (col > -1 && col < table.getModel().getColumnCount()) {
            active.txt.setText("" + col);
            active.txt.setToolTipText("Column " + table.getModel().getColumnName(col));
            active.btn.setToolTipText("Column " + table.getModel().getColumnName(col));
            active.lbl.setText(table.getModel().getColumnName(col));
            table.getColumnModel().getColumn(col).setCellRenderer(new CustomRenderer(active.color));
        }

        active = null;
        updateRenderers();
    }

    public void updateRenderers() {
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new DefaultTableCellRenderer());
          //  table.getColumnModel().getColumn(i).setPreferredWidth(COLW);
            //table.getColumnModel().getColumn(i).setMaxWidth(COLW*3);
            table.getColumnModel().getColumn(i).setMinWidth(COLW);
        }
        for (int i = 0; i < cols.length; i++) {
            TableCol tc = cols[i];
            JTextField txt = tc.txt;
            JLabel lbl = tc.lbl;
            JButton btn = tc.btn;
            Color c = tc.color;
            btn.setBackground(c);
            txt.setBackground(c);

            int col = getCol(txt);
            tc.curcol = col;
            if (col > -1) {
                table.getColumnModel().getColumn(col).setCellRenderer(new CustomRenderer(c));
                lbl.setText(table.getModel().getColumnName(col));
                
            } else {
                txt.setText("");
                lbl.setText("");
            }
        }
        table.invalidate();
        table.repaint();
        repaint();
    }

    private int getCol(JTextField txt) {
        if (txt.getText() == null) {
            return -1;
        }
        try {
            int col = Integer.parseInt(txt.getText());
            return col;
        } catch (Exception e) {
            return -1;
        }
    }
}
class CustomRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 6703872492730589499L;
    private Color color;

    public CustomRenderer(Color color) {
        this.color = color;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        cellComponent.setBackground(color);

        return cellComponent;
    }
}
