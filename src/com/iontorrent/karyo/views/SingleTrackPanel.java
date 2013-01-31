/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.karyo.data.KaryoTrack;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Chantal Roth
 */
public class SingleTrackPanel extends JPanel {

    KaryoTrack track;
    JCheckBox box;
    KaryoControlPanel control;
    MouseListener list;
    
    public SingleTrackPanel(final KaryoTrack track, boolean showButton, final KaryoControlPanel control, final MouseListener list) {
        this.track = track;
        this.control = control;
        this.list = list;
        setLayout(new BorderLayout());
        String file = "";
        String type = "";
        if (track.getTrack().getResourceLocator() != null) {
            file = track.getTrack().getResourceLocator().getFileName();
            type =  track.getTrack().getResourceLocator().getType();
        }
        if (type == null) type = file;
        String name = track.getShortName() + ": " + track.getName();
        if (type != null && type.length() > 0) name +=", "+type;
        
        box = new JCheckBox(name);
        box.setForeground(track.getColor());
        box.setSelected(track.isVisible());
        String tool = "File: "+file+"<br>";
        
        
        if (track.getSampleafeture()!= null) {
            box.setToolTipText("<html>"+tool+ "Example: "+ track.getSampleafeture().toString()+"</html>");
        }
        else  box.setToolTipText("<html>"+tool+"No data was found for this type</html>");
       
        box.addItemListener(new ItemListener(){

            @Override
            public void itemStateChanged(ItemEvent e) {
                 track.setVisible(box.isSelected());
                 p("+++++++++++++++ Check box clicked on "+track.getName()+". Visible is now = "+track.isVisible());
                 if (control != null) control.recreateView(false);
                 else p("Control is null! Not recreating view");
            }
            
        });
        box.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (list != null) list.mouseClicked(evt);
                else if (evt.getClickCount() >1) getTrackOptions();
            }
        });
        add("West", box);
        
    }

    public void setText(String s) {
        box.setToolTipText(s);
    }
    public void setSelected(boolean b) {
        this.box.setSelected(b);
    }
    public KaryoTrack getTrack() {
        return this.track;
    }
    private void getTrackOptions() {
        TrackOptionsPanel p = new TrackOptionsPanel(track);
        int ans = JOptionPane.showConfirmDialog(SingleTrackPanel.this, p);
        if (ans == JOptionPane.OK_OPTION) {
            p.updateTrack();
            box.setForeground(track.getColor());
            if (control != null) control.recreateView(false);
            else p("Control is null! Not recreating view");
        }
    }
    private void p(String s) {
        //Logger.getLogger("SingleTrackPanel").info(s);
        System.out.println("SingleTrackPanel: "+s);
    }
}
