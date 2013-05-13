/*
 * Created by JFormDesigner on Wed May 08 20:31:37 CEST 2013
 */

package com.iontorrent.prefs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.broad.igv.ui.PreferencesTabPanel;

/**
 * @author Chantal Roth
 */
public class KaryoPreferencesTab extends PreferencesTabPanel {
    
    
    public KaryoPreferencesTab() {
        initComponents();
    }

     @Override
    public void initValues() {
            this.checkKaryoBamAllowed.setSelected(prefMgr.getAsBoolean(IonTorrentPreferencesManager.KARYO_ALLOW_BAMFILES));
        this.checkKaryoGeneAllowed.setSelected(prefMgr.getAsBoolean(IonTorrentPreferencesManager.KARYO_ALLOW_GENEFILES));
        this.checkKaryoGeneExprAllowed.setSelected(prefMgr.getAsBoolean(IonTorrentPreferencesManager.KARYO_ALLOW_EXPFILES));
     }

     
    private void checkKaryoBamAllowedActionPerformed(ActionEvent e) {
       boolean sel = this.checkKaryoBamAllowed.isSelected();
        updatedPreferenceMap.put(IonTorrentPreferencesManager.KARYO_ALLOW_BAMFILES, ""+sel);
    }

    private void checkKaryoGeneExprAllowedStateChanged(ChangeEvent e) {
        boolean sel = this.checkKaryoGeneExprAllowed.isSelected();
        updatedPreferenceMap.put(IonTorrentPreferencesManager.KARYO_ALLOW_EXPFILES, ""+sel);
    }

    private void checkKaryoGeneAllowedStateChanged(ChangeEvent e) {
         boolean sel = this.checkKaryoGeneAllowed.isSelected();
        updatedPreferenceMap.put(IonTorrentPreferencesManager.KARYO_ALLOW_GENEFILES, ""+sel);
    }

     private void checkBAMHasFlowValuesActionPerformed(ActionEvent e) {
         // TODO add your code here
     }

    
    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel10 = new JPanel();
        panel12 = new JPanel();
        panel13 = new JPanel();
        checkKaryoBamAllowed = new JCheckBox();
        checkKaryoGeneAllowed = new JCheckBox();
        checkKaryoGeneExprAllowed = new JCheckBox();

        //======== this ========
        setLayout(null);

        //======== panel10 ========
        {
            panel10.setLayout(null);

            //======== panel12 ========
            {
                panel12.setLayout(null);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel12.getComponentCount(); i++) {
                        Rectangle bounds = panel12.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel12.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel12.setMinimumSize(preferredSize);
                    panel12.setPreferredSize(preferredSize);
                }
            }
            panel10.add(panel12);
            panel12.setBounds(5, 5, panel12.getPreferredSize().width, 0);

            //======== panel13 ========
            {
                panel13.setBorder(new TitledBorder("Rendering of large files in whole genome view:"));
                panel13.setLayout(null);

                //---- checkKaryoBamAllowed ----
                checkKaryoBamAllowed.setText("<html>Allow rendering of <b>BAM</b> files<br></html>");
                checkKaryoBamAllowed.setToolTipText("<html><b>Note:</b> loading entire BAM files could take a <b>long time</b>, in particular if the .BAM file is not stored locally.<br>It is recommended <b>not</b> to enable this option, or use it only with small .BAM files</html>");
                checkKaryoBamAllowed.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkBAMHasFlowValuesActionPerformed(e);
                        checkKaryoBamAllowedActionPerformed(e);
                    }
                });
                panel13.add(checkKaryoBamAllowed);
                checkKaryoBamAllowed.setBounds(10, 20, 375, 35);

                //---- checkKaryoGeneAllowed ----
                checkKaryoGeneAllowed.setText("<html>Allow rendering of <b>gene</b> files</html>");
                checkKaryoGeneAllowed.setToolTipText("<html><b>Note:</b> loading entire GENE files could take a <b>long time</b>, in particular if the file is not stored locally.<br>It is recommended <b>not</b> to enable this option, or use it only with small files</html>");
                checkKaryoGeneAllowed.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkBAMHasFlowValuesActionPerformed(e);
                        checkKaryoBamAllowedActionPerformed(e);
                    }
                });
                checkKaryoGeneAllowed.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        checkKaryoGeneAllowedStateChanged(e);
                    }
                });
                panel13.add(checkKaryoGeneAllowed);
                checkKaryoGeneAllowed.setBounds(10, 55, 435, 30);

                //---- checkKaryoGeneExprAllowed ----
                checkKaryoGeneExprAllowed.setText("<html>Allow rendering of <b>expression</b> files</html>");
                checkKaryoGeneExprAllowed.setToolTipText("<html><b>Note:</b> loading entire GENE files could take a <b>long time</b>, in particular if the file is not stored locally.<br>It is recommended <b>not</b> to enable this option, or use it only with small files</html>");
                checkKaryoGeneExprAllowed.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        checkBAMHasFlowValuesActionPerformed(e);
                        checkKaryoBamAllowedActionPerformed(e);
                    }
                });
                checkKaryoGeneExprAllowed.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        checkKaryoGeneExprAllowedStateChanged(e);
                    }
                });
                panel13.add(checkKaryoGeneExprAllowed);
                checkKaryoGeneExprAllowed.setBounds(10, 85, 470, 30);

                { // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < panel13.getComponentCount(); i++) {
                        Rectangle bounds = panel13.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = panel13.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    panel13.setMinimumSize(preferredSize);
                    panel13.setPreferredSize(preferredSize);
                }
            }
            panel10.add(panel13);
            panel13.setBounds(0, 0, 785, 135);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel10.getComponentCount(); i++) {
                    Rectangle bounds = panel10.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel10.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel10.setMinimumSize(preferredSize);
                panel10.setPreferredSize(preferredSize);
            }
        }
        add(panel10);
        panel10.setBounds(0, 0, 750, 555);

        { // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < getComponentCount(); i++) {
                Rectangle bounds = getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            setMinimumSize(preferredSize);
            setPreferredSize(preferredSize);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel10;
    private JPanel panel12;
    private JPanel panel13;
    private JCheckBox checkKaryoBamAllowed;
    private JCheckBox checkKaryoGeneAllowed;
    private JCheckBox checkKaryoGeneExprAllowed;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
