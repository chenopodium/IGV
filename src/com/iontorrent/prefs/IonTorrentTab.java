/*
 * Created by JFormDesigner on Wed May 08 16:21:15 CEST 2013
 */
package com.iontorrent.prefs;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.PreferencesTabPanel;
import org.broad.igv.ui.util.MessageUtils;

/**
 * @author Chantal Roth
 */
public class IonTorrentTab extends PreferencesTabPanel {  
    
    private ButtonGroup groupType = new ButtonGroup();

    public IonTorrentTab() {
        super();
        initComponents();
    } 

    @Override
    public void initValues() {
        /**
         * Ion Torrent (Chantal Roth)
         */
        this.binSizeText.setText(prefMgr.get(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_BINSIZE));
        boolean hideHp = prefMgr.getAsBoolean(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_HIDE_FIRST_HP);
        this.hideFirstHP.setSelected(hideHp);
        String type = PreferenceManager.getInstance().get(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_CHARTTYPE);
        if (type == null) {
            type = "LINE";
        }
        if (type != null) {
            type = type.trim().toUpperCase();
            if (type.equals("LINE")) {
                this.radioLine.setSelected(true);
            } else if (type.equals("AREA")) {
                this.radioArea.setSelected(true);
            } else if (type.equals("BAR")) {
                this.radioBar.setSelected(true);
            } else if (type.equals("STACKED")) {
                this.radioStacked.setSelected(true);
            }
        }
        String server = PreferenceManager.getInstance().get(IonTorrentPreferencesManager.IONTORRENT_SERVER);
        if (server != null) {
            this.textServer2.setText(server);
        }
        this.textNrBases.setText(prefMgr.get(IonTorrentPreferencesManager.IONTORRENT_NRBASES_IONOGRAM_ALIGN));
        this.textNrIonograms.setText(prefMgr.get(IonTorrentPreferencesManager.IONTORRENT_MAXNREADS_IONOGRAM_ALIGN));
        groupType.add(this.radioFlowBar);
        groupType.add(this.radioPeak);
        type = PreferenceManager.getInstance().get(IonTorrentPreferencesManager.IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE);
        if (type == null) {
            type = "PEAK";
        }
        if (type != null) {
            type = type.trim().toUpperCase();
            if (type.equals("PEAK")) {
                this.radioPeak.setSelected(true);
            } else {
                this.radioFlowBar.setSelected(true);
            }
        }

    }

    private void checkBAMHasFlowValuesActionPerformed(ActionEvent e) {
        boolean sel = this.checkBAMHasFlowValues.isSelected();
        updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_BAM_HAS_FLOWVALUES, "" + sel);
    }

    private void extractBinSize() {
        String sbin = this.binSizeText.getText();
        if (sbin != null) {
            sbin = sbin.trim();
            try {
                Integer.parseInt(sbin);
                updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_BINSIZE, sbin);
            } catch (NumberFormatException numberFormatException) {
                inputValidated = false;
                MessageUtils.showMessage(
                        "Bin size must be an integer.");
            }
        }
    }

    private void extractNrBasesLeftRight() {
        String snr = this.textNrIonograms.getText();
        if (snr != null) {
            try {
                int bases = Integer.parseInt(snr);
                updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_NRBASES_IONOGRAM_ALIGN, snr);
            } catch (Exception ex) {
            }
        }
    }

    private void extractNrIonograms() {
        String siono = this.textNrIonograms.getText();
        if (siono != null) {
            try {
                int iono = Integer.parseInt(siono);
                updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_MAXNREADS_IONOGRAM_ALIGN, siono);
            } catch (Exception ex) {
            }
        }
    }

    private void extractServerInfo() {
        String server = this.textServer2.getText();
        if (server != null) {
            server = server.trim();
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_SERVER, server);
        }
    }

    private void textServerActionPerformed(ActionEvent e) {
        extractServerInfo();
    }

    private void binSizeTextFocusLost(FocusEvent e) {
        extractBinSize();
    }

    private void textNrIonogramsActionPerformed(ActionEvent e) {
        extractNrIonograms();
    }

    private void textNrBasesActionPerformed(ActionEvent e) {
        extractNrBasesLeftRight();
    }

    private void textNrIonogramsFocusLost(FocusEvent e) {
        extractNrIonograms();
    }

    private void textNrBasesFocusLost(FocusEvent e) {
        extractNrBasesLeftRight();
    }

    private void binSizeTextActionPerformed(ActionEvent e) {
        extractBinSize();
    }

    private void radioPeakActionPerformed(ActionEvent e) {
        storeSelectedIonoAlignDrawType();
    }

    private void radioFlowBarActionPerformed(ActionEvent e) {
        storeSelectedIonoAlignDrawType();
    }

//    private void defaultUsernameFieldFocusLost(FocusEvent e) {
//        updateDefaultUsername();
//    }
//
//    private void updateDefaultUsername() {
//        this.proxySettingsChanged = true;
//        updatedPreferenceMap.put(PreferenceManager.AUTHENTICATION_DEFAULT_USER, this.defaultUsernameField.getText());
//    }
//    
//    private void updateDefaultPw() {
//        this.proxySettingsChanged = true;
//        String pw = defaultPasswordField.getText();
//        String pwEncoded = Utilities.base64Encode(pw);
//        updatedPreferenceMap.put(PreferenceManager.AUTHENTICATION_DEFAULT_PW, pwEncoded);        
//        
//        
//    }
    private void hideFirstHPActionPerformed(ActionEvent e) {
        updatedPreferenceMap.put(
                IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_HIDE_FIRST_HP,
                String.valueOf(this.hideFirstHP.isSelected()));
    }

    private void storeSelectedIonoAlignDrawType() {
        if (this.radioPeak.isSelected()) {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE, "PEAK");
        } else {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE, "BAR");
        }
    }

//    private void defaultUsernameFieldActionPerformed(ActionEvent e) {
//        updateDefaultUsername();
//    }
//
//    private void defaultPasswordFieldActionPerformed(ActionEvent e) {
//      updateDefaultPw();
//    }
//
//    private void defaultPasswordFieldFocusLost(FocusEvent e) {
//       updateDefaultPw();
//    }
//    private void checkBAMHasFlowValuesActionPerformed(ActionEvent e) {
//        boolean sel = this.checkBAMHasFlowValues.isSelected();
//        updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_BAM_HAS_FLOWVALUES, "" + sel);
//    }

    private void radioAreaActionPerformed(ActionEvent e) {
        storeSelectedFlowDistChartType();
    }

    private void radioBarActionPerformed(ActionEvent e) {
        storeSelectedFlowDistChartType();
    }

    private void radioStackedActionPerformed(ActionEvent e) {
        storeSelectedFlowDistChartType();
    }

    private void storeSelectedFlowDistChartType() {
        if (this.radioBar.isSelected()) {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_CHARTTYPE, "BAR");
        } else if (this.radioStacked.isSelected()) {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_CHARTTYPE, "STACKED");
        } else if (this.radioArea.isSelected()) {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_CHARTTYPE, "AREA");
        } else {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_CHARTTYPE, "LINE");
        }
    }

    private void radioLineActionPerformed(ActionEvent e) {
        if (this.radioLine.isSelected()) {
            updatedPreferenceMap.put(IonTorrentPreferencesManager.IONTORRENT_FLOWDIST_CHARTTYPE, "LINE");
        }
    }

    private void showJunctionTrackCBActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void junctionFlankingTextFieldActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void junctionFlankingTextFieldFocusLost(FocusEvent e) {
        // TODO add your code here
    }

    private void radioButton1ActionPerformed(ActionEvent e) {
        storeSelectedFlowDistChartType();
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        panel6 = new JPanel();
        hideFirstHP = new JCheckBox();
        binSizeText = new JTextField();
        label28 = new JLabel();
        label29 = new JLabel();
        radioLine = new JRadioButton();
        radioArea = new JRadioButton();
        radioBar = new JRadioButton();
        radioStacked = new JRadioButton();
        panel7 = new JPanel();
        label30 = new JLabel();
        textNrIonograms = new JTextField();
        label27 = new JLabel();
        textNrBases = new JTextField();
        label32 = new JLabel();
        radioPeak = new JRadioButton();
        radioFlowBar = new JRadioButton();
        panel8 = new JPanel();
        label31 = new JLabel();
        textServer2 = new JTextField();
        panel9 = new JPanel();
        checkBAMHasFlowValues = new JCheckBox();

        //======== this ========
        setLayout(null);

        //======== panel6 ========
        {
            panel6.setBorder(new TitledBorder("Confidence Distribution Chart Options"));
            panel6.setLayout(null);

            //---- hideFirstHP ----
            hideFirstHP.setText("skip data for homo polymers at start or end of read (including HP of size 1)");
            hideFirstHP.setToolTipText("discards flow signals from HP at beginning or end of reads (including HP of size 1), in order to not skew the results due to short reads");
            hideFirstHP.setSelected(true);
            hideFirstHP.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showJunctionTrackCBActionPerformed(e);
                    hideFirstHPActionPerformed(e);
                }
            });
            panel6.add(hideFirstHP);
            hideFirstHP.setBounds(new Rectangle(new Point(5, 25), hideFirstHP.getPreferredSize()));

            //---- binSizeText ----
            binSizeText.setToolTipText("The size of the bins by which the data in the chart is grouped. Small bin size means small granularity, large bin size means smoother chart");
            binSizeText.setText("15");
            binSizeText.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    junctionFlankingTextFieldActionPerformed(e);
                    binSizeTextActionPerformed(e);
                    binSizeTextActionPerformed(e);
                    binSizeTextActionPerformed(e);
                }
            });
            binSizeText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    junctionFlankingTextFieldFocusLost(e);
                    binSizeTextFocusLost(e);
                    binSizeTextFocusLost(e);
                }
            });
            panel6.add(binSizeText);
            binSizeText.setBounds(135, 55, 95, 25);

            //---- label28 ----
            label28.setText("Bin size in chart:");
            panel6.add(label28);
            label28.setBounds(10, 55, 125, label28.getPreferredSize().height);

            //---- label29 ----
            label29.setText("Default chart type:");
            panel6.add(label29);
            label29.setBounds(new Rectangle(new Point(10, 90), label29.getPreferredSize()));

            //---- radioLine ----
            radioLine.setText("line chart");
            radioLine.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioButton1ActionPerformed(e);
                    radioLineActionPerformed(e);
                    radioLineActionPerformed(e);
                }
            });
            panel6.add(radioLine);
            radioLine.setBounds(new Rectangle(new Point(135, 90), radioLine.getPreferredSize()));

            //---- radioArea ----
            radioArea.setText("area chart");
            radioArea.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioAreaActionPerformed(e);
                }
            });
            panel6.add(radioArea);
            radioArea.setBounds(new Rectangle(new Point(135, 115), radioArea.getPreferredSize()));

            //---- radioBar ----
            radioBar.setText("bar chart");
            radioBar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioBarActionPerformed(e);
                }
            });
            panel6.add(radioBar);
            radioBar.setBounds(new Rectangle(new Point(135, 140), radioBar.getPreferredSize()));

            //---- radioStacked ----
            radioStacked.setText("(stacked bar chart)");
            radioStacked.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioStackedActionPerformed(e);
                }
            });
            panel6.add(radioStacked);
            radioStacked.setBounds(new Rectangle(new Point(135, 165), radioStacked.getPreferredSize()));

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel6.getComponentCount(); i++) {
                    Rectangle bounds = panel6.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel6.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel6.setMinimumSize(preferredSize);
                panel6.setPreferredSize(preferredSize);
            }
        }
        add(panel6);
        panel6.setBounds(0, 0, 784, 243);

        //======== panel7 ========
        {
            panel7.setBorder(new TitledBorder("Ionogram Alignment Settings"));
            panel7.setLayout(null);

            //---- label30 ----
            label30.setText("Max. number of reads  in alignment");
            panel7.add(label30);
            label30.setBounds(new Rectangle(new Point(15, 25), label30.getPreferredSize()));

            //---- textNrIonograms ----
            textNrIonograms.setText("50");
            textNrIonograms.setToolTipText("Used to launch other applications (such as Torrent Scout Light)");
            textNrIonograms.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    textServerActionPerformed(e);
                    textNrIonogramsActionPerformed(e);
                }
            });
            textNrIonograms.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    textNrIonogramsFocusLost(e);
                }
            });
            panel7.add(textNrIonograms);
            textNrIonograms.setBounds(390, 20, 105, textNrIonograms.getPreferredSize().height);

            //---- label27 ----
            label27.setText("Number of base calls around main location to include in alignment");
            panel7.add(label27);
            label27.setBounds(15, 45, 365, label27.getPreferredSize().height);

            //---- textNrBases ----
            textNrBases.setText("5");
            textNrBases.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    textNrBasesActionPerformed(e);
                }
            });
            textNrBases.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    textNrBasesFocusLost(e);
                }
            });
            panel7.add(textNrBases);
            textNrBases.setBounds(390, 45, 105, textNrBases.getPreferredSize().height);

            //---- label32 ----
            label32.setText("Alignmentl drawing type:");
            panel7.add(label32);
            label32.setBounds(15, 65, 150, 14);

            //---- radioPeak ----
            radioPeak.setText("peak function");
            radioPeak.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioPeakActionPerformed(e);
                }
            });
            panel7.add(radioPeak);
            radioPeak.setBounds(390, 70, 105, radioPeak.getPreferredSize().height);

            //---- radioFlowBar ----
            radioFlowBar.setText("bar");
            radioFlowBar.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    radioFlowBarActionPerformed(e);
                }
            });
            panel7.add(radioFlowBar);
            radioFlowBar.setBounds(390, 90, 95, radioFlowBar.getPreferredSize().height);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel7.getComponentCount(); i++) {
                    Rectangle bounds = panel7.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel7.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel7.setMinimumSize(preferredSize);
                panel7.setPreferredSize(preferredSize);
            }
        }
        add(panel7);
        panel7.setBounds(0, 243, 784, 168);

        //======== panel8 ========
        {
            panel8.setBorder(new TitledBorder("Server Settings"));
            panel8.setLayout(null);

            //---- label31 ----
            label31.setText("Default Ion Torrent Server:");
            panel8.add(label31);
            label31.setBounds(new Rectangle(new Point(15, 25), label31.getPreferredSize()));

            //---- textServer2 ----
            textServer2.setText("ioneast.ite");
            textServer2.setToolTipText("Used to launch other applications (such as Torrent Scout Light)");
            textServer2.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    textServerActionPerformed(e);
                }
            });
            panel8.add(textServer2);
            textServer2.setBounds(220, 20, 475, textServer2.getPreferredSize().height);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel8.getComponentCount(); i++) {
                    Rectangle bounds = panel8.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel8.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel8.setMinimumSize(preferredSize);
                panel8.setPreferredSize(preferredSize);
            }
        }
        add(panel8);
        panel8.setBounds(0, 411, 784, 96);

        //======== panel9 ========
        {
            panel9.setBorder(new TitledBorder("BAM File Information"));
            panel9.setLayout(null);

            //---- checkBAMHasFlowValues ----
            checkBAMHasFlowValues.setText("The current BAM file contains raw flow information (select to show flow menu items)");
            checkBAMHasFlowValues.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    checkBAMHasFlowValuesActionPerformed(e);
                }
            });
            panel9.add(checkBAMHasFlowValues);
            checkBAMHasFlowValues.setBounds(15, 20, 420, checkBAMHasFlowValues.getPreferredSize().height);

            { // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < panel9.getComponentCount(); i++) {
                    Rectangle bounds = panel9.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = panel9.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                panel9.setMinimumSize(preferredSize);
                panel9.setPreferredSize(preferredSize);
            }
        }
        add(panel9);
        panel9.setBounds(0, 507, 784, 99);

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

        //---- buttonGroup1 ----
        ButtonGroup buttonGroup1 = new ButtonGroup();
        buttonGroup1.add(radioLine);
        buttonGroup1.add(radioArea);
        buttonGroup1.add(radioBar);
        buttonGroup1.add(radioStacked);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }
    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    private JPanel panel6;
    private JCheckBox hideFirstHP;
    private JTextField binSizeText;
    private JLabel label28;
    private JLabel label29;
    private JRadioButton radioLine;
    private JRadioButton radioArea;
    private JRadioButton radioBar;
    private JRadioButton radioStacked;
    private JPanel panel7;
    private JLabel label30;
    private JTextField textNrIonograms;
    private JLabel label27;
    private JTextField textNrBases;
    private JLabel label32;
    private JRadioButton radioPeak;
    private JRadioButton radioFlowBar;
    private JPanel panel8;
    private JLabel label31;
    private JTextField textServer2;
    private JPanel panel9;
    private JCheckBox checkBAMHasFlowValues;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
