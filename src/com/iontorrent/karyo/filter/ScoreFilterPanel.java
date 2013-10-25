/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.FeatureMetaInfo.Range;
import com.iontorrent.karyo.data.KaryoTrack;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Chantal
 */
public class ScoreFilterPanel extends FilterPanel {
    LocusScoreFilter sfilter;
    /**
     * Creates new form ScoreFilterPanel
     */
    public ScoreFilterPanel(String name, KaryoFilter filter, KaryoTrack track) {
        super(filter, track);
        sfilter = (LocusScoreFilter)filter;
        initComponents();
        add("West", boxen);
        
         boxen.setSelected(filter.isEnabled());
        setEnabledFontAndColor(boxen);
        this.jLabel1.setName(name);
        Range r = track.getMetaInfo().getRangeForAttribute("Score");
        p("Range for "+track.getTrackDisplayName()+" is:"+r);
        
        Range range = sfilter.getRange();
        double min = 0;
        double max = 0;
        if (range != null) {
            if (range.min != Double.NaN) min=range.min;
            else if (r.min != Double.NaN) min=r.min;
            if (range.max != Double.NaN)  max=range.max;
            else if (r.max != Double.NaN) max=r.max;
        }
        if (min > max) {
            double tmp = min;
            min = max;
            max = tmp;
        }
        min = Math.max(0, min);
        max = Math.min(1000, max);
        txtMin.setText(""+min);
        txtMax.setText(""+max);
        
    
    }
    private void p(String s) {
        System.out.println("ScoreFilterPanel:"+s);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        boxen = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        txtMin = new javax.swing.JTextField();
        txtMax = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setText(org.openide.util.NbBundle.getMessage(ScoreFilterPanel.class, "ScoreFilterPanel.jLabel1.text")); // NOI18N

        boxen.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        boxen.setText(org.openide.util.NbBundle.getMessage(ScoreFilterPanel.class, "ScoreFilterPanel.boxen.text")); // NOI18N
        boxen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boxenActionPerformed(evt);
            }
        });

        jLabel2.setText(org.openide.util.NbBundle.getMessage(ScoreFilterPanel.class, "ScoreFilterPanel.jLabel2.text")); // NOI18N

        txtMin.setText(org.openide.util.NbBundle.getMessage(ScoreFilterPanel.class, "ScoreFilterPanel.txtMin.text")); // NOI18N

        txtMax.setText(org.openide.util.NbBundle.getMessage(ScoreFilterPanel.class, "ScoreFilterPanel.txtMax.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(boxen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(boxen)
                    .addComponent(jLabel2)
                    .addComponent(txtMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void boxenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boxenActionPerformed
        setEnabledFontAndColor(boxen);
    }//GEN-LAST:event_boxenActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox boxen;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtMax;
    private javax.swing.JTextField txtMin;
    // End of variables declaration//GEN-END:variables

    @Override
    public void updateFilter() {
         this.filter.setEnabled(this.boxen.isSelected());
          
        Range r = new FeatureMetaInfo.Range();
//        
        
        double min = Double.NaN;
        double max = Double.NaN;
        String smin = txtMin.getText();
        String smax = txtMax.getText();
        if (smin != null) {
            smin = smin.trim();
            try  {
                min = Double.parseDouble((smin));
            }
            catch (Exception e) {
                p("Could not parse "+smin+" to double");
            }
        }
         if (smax != null) {
            smax = smax.trim();
            try  {
                max = Double.parseDouble((smax));
            }
            catch (Exception e) {
                p("Could not parse "+smax+" to double");
            }
        }
        r.min = min;
        r.max = max;
        p("Updating LocusScore filter with range "+r);
        ((LocusScoreFilter) filter).setRange(r);
    }
}
