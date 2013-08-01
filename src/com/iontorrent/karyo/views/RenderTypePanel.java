/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.guiutils.FlowPanel;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.renderer.RenderType;
import com.iontorrent.utils.BagPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Chantal
 */
public class RenderTypePanel extends javax.swing.JPanel {

    RenderType render;
    KaryoTrack track;
    
    JLabel lbls[];
    ColorBtn btns[];
   int nr;
    
    /**
     * Creates new form RenderTypePanel
     */
    public RenderTypePanel(RenderType render, KaryoTrack track) {
        initComponents();
        nr = render.getNrColors();
        
        lbls = new JLabel[nr];
        btns = new ColorBtn[nr];
        this.render = render;
        this.track = track;
       // this.lblName.setText("<html><b>"+render.getName()+"</b> for track "+track.getTrackName()+"</html>");
        this.lblDesc.setText("<html>" +render.getDescription()+"</html>");
        JPanel pan = new JPanel();
        BagPanel bag = new BagPanel();
        pan.setLayout(new BorderLayout());
        pan.add("Center", bag);
        panColors.setLayout(new BorderLayout());
        double parcutoff = render.getParCutoffScore();
        txtCutoff.setText(""+parcutoff);
        for (int i = 0; i < nr; i++) {
            Color c = render.getColor(i);
            if (c == null) {
                p("Got no color for render "+render.getName()+" and color "+i+" will use default color ");
                c = render.getDefaultColor(i);
            }
            String name = render.getColorName(i);
            if (name == null) name = "Color "+i;
            lbls[i] = new JLabel(name);
            
            bag.place(0, i, lbls[i]);
            ColorBtn btn = new ColorBtn(render.getColorShortName(i), c, true);
            btn.setToolTipText("<html>"+name+ "<br> "+btn.getColorString()+"<html>");
           
            
            Dimension d = new Dimension(100,24);
            btn.setMaximumSize(d);
            btn.setMinimumSize(d);
            btn.setPreferredSize(d);
            btn.setSize(d);
            btn.addActionListener(new BtnListener(btn, i));
            bag.place(1, i,btn);
            bag.place(2, i,new JPanel());
                    
        }
        panColors.add("North", pan);
        List<String> atts  = null;
        if (track.getMetaInfo() != null) atts = track.getMetaInfo().getAttributes();
        if (atts == null || atts.size()<1) {
            this.lblBox.setText("I found no attributes for "+track.getMetaInfo());
            boxAtts.setVisible(false);
        }
        else {
            this.lblBox.setText("Which attribute should be used as 'score'? ");
            boxAtts.setVisible(true);
            String scorename = track.getRenderType().getRelevantAttName();
            for (String att: atts) {
                this.boxAtts.addItem(att);
                if (scorename != null && att.equalsIgnoreCase(scorename)) boxAtts.setSelectedItem(att);
            }
           
            if (scorename != null) {
                p("====== track render type has relevant name: "+scorename);
                // case INsensitive
                
                boxAtts.setSelectedItem(scorename);
            }
            else p("Track "+track.getTrackDisplayName()+"/"+track.getRenderType().getName()+" has no relevant att name");
        }
    }
    private class BtnListener implements ActionListener{
        int nr;
        JButton btn;
          public BtnListener(JButton btn, int nr) {
              this.nr = nr;
              this.btn = btn;
          }

        @Override
        public void actionPerformed(ActionEvent e) {
           Color c = JColorChooser.showDialog(null, render.getColorName(nr), render.getColor(nr));
           if (c != null) {
               render.setColor(c, nr);
               btn.setBackground(c);
               if (nr == 0) track.setColor(c);
           }
        }
    }
    private void p(String s ){
        System.out.println("RenderTypePanel: "+s);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panColors = new javax.swing.JPanel();
        panDesc = new javax.swing.JPanel();
        boxAtts = new javax.swing.JComboBox();
        lblDesc = new javax.swing.JLabel();
        lblBox = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        txtCutoff = new javax.swing.JTextField();

        panColors.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.panColors.border.title"))); // NOI18N
        panColors.setLayout(new java.awt.GridLayout(3, 2));

        panDesc.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.panDesc.border.title"))); // NOI18N

        boxAtts.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                boxAttsItemStateChanged(evt);
            }
        });
        boxAtts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxAttsActionPerformed(evt);
            }
        });
        boxAtts.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                boxAttsFocusLost(evt);
            }
        });
        boxAtts.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                boxAttsPropertyChange(evt);
            }
        });

        lblDesc.setText(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.lblDesc.text")); // NOI18N

        lblBox.setText(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.lblBox.text")); // NOI18N
        lblBox.setToolTipText(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.lblBox.toolTipText")); // NOI18N

        jLabel1.setText(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.jLabel1.text")); // NOI18N

        txtCutoff.setText(org.openide.util.NbBundle.getMessage(RenderTypePanel.class, "RenderTypePanel.txtCutoff.text")); // NOI18N
        txtCutoff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCutoffActionPerformed(evt);
            }
        });
        txtCutoff.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtCutoffFocusLost(evt);
            }
        });

        javax.swing.GroupLayout panDescLayout = new javax.swing.GroupLayout(panDesc);
        panDesc.setLayout(panDescLayout);
        panDescLayout.setHorizontalGroup(
            panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDescLayout.createSequentialGroup()
                .addGroup(panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 464, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panDescLayout.createSequentialGroup()
                        .addGroup(panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panDescLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(panDescLayout.createSequentialGroup()
                                .addComponent(lblBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(21, 21, 21)))
                        .addGroup(panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(boxAtts, 0, 132, Short.MAX_VALUE)
                            .addComponent(txtCutoff))
                        .addGap(120, 120, 120)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panDescLayout.setVerticalGroup(
            panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panDescLayout.createSequentialGroup()
                .addComponent(lblDesc, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblBox)
                    .addComponent(boxAtts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panDescLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtCutoff, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panDesc, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
            .addComponent(panColors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(panDesc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panColors, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void setRelevantAttribute(String s) {
        p("==== Setting relevant att name: "+s);
        this.render.setRelevantAttName(s);
    }
    private void boxAttsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxAttsActionPerformed
        p("Got action performed, selectd item is: "+boxAtts.getSelectedItem());
        setRelevantAttribute(""+boxAtts.getSelectedItem());
    }//GEN-LAST:event_boxAttsActionPerformed

    private void boxAttsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_boxAttsItemStateChanged
        int state = evt.getStateChange();
        p((state == evt.SELECTED) ? "Selected" : "Deselected");
        p("Item: " + evt.getItem());
        ItemSelectable is = evt.getItemSelectable();
      //  setRelevantAttribute(""+evt.getItem());
    }//GEN-LAST:event_boxAttsItemStateChanged

    private void boxAttsPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_boxAttsPropertyChange
      //  setRelevantAttribute(""+boxAtts.getSelectedItem());
    }//GEN-LAST:event_boxAttsPropertyChange

    private void boxAttsFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_boxAttsFocusLost
         
        //setRelevantAttribute(""+boxAtts.getSelectedItem());
    }//GEN-LAST:event_boxAttsFocusLost

    private void getCutoffFromText() {
        String s = txtCutoff.getText();
        if (s != null ) {
            try {
                double c = Double.parseDouble(s);
                render.setCutoffScore(c);
            }
            catch (Exception e) {
                p("Could not parse to double: "+s);
            }
        }
    }
        
    private void txtCutoffActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtCutoffActionPerformed
        getCutoffFromText();
    }//GEN-LAST:event_txtCutoffActionPerformed

    private void txtCutoffFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtCutoffFocusLost
       getCutoffFromText();
    }//GEN-LAST:event_txtCutoffFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox boxAtts;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lblBox;
    private javax.swing.JLabel lblDesc;
    private javax.swing.JPanel panColors;
    private javax.swing.JPanel panDesc;
    private javax.swing.JTextField txtCutoff;
    // End of variables declaration//GEN-END:variables
}
