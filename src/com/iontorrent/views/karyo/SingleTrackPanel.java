/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.karyo;

import com.iontorrent.data.karyo.KaryoTrack;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class SingleTrackPanel extends JPanel {
    KaryoTrack track;
    JCheckBox box;
    public SingleTrackPanel(KaryoTrack track) {
        this.track = track;
        
        setLayout(new BorderLayout());
        box = new JCheckBox(track.getShortName()+") "+ track.getName());
        box.setForeground(track.getDefaultColor());
        if (track.isVisible()) box.setSelected(true);
        add("West", box);
                
    }
    
    public boolean isSelected() {
        return box.isSelected();
    }
}
