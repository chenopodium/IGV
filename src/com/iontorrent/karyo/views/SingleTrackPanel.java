/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import com.iontorrent.guiutils.FlowPanel;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.renderer.RenderType;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Chantal Roth
 */
public class SingleTrackPanel extends JPanel {

    KaryoTrack track;
    JCheckBox box;
    JLabel lbl;
    JPanel panColors;
    KaryoControlPanel control;
    MouseListener list;
    RenderType render;

    public SingleTrackPanel( final KaryoTrack track, boolean showButton, final KaryoControlPanel control, final MouseListener list) {
        this(false, null, null, true, track, showButton, control, list);
    }
    public SingleTrackPanel(boolean selected, Color bgcolor, Color fg, boolean check, final KaryoTrack track, boolean showButton, final KaryoControlPanel control, final MouseListener list) {
        this.track = track;
        this.control = control;
        this.list = list;
        setLayout(new BorderLayout());
        if (bgcolor != null) {
            this.setBackground(bgcolor);
        }
        String file = "";
       
        if (track.getTrack().getResourceLocator() != null) {
            file = track.getTrack().getResourceLocator().getFileName();       
        }
        
         String tname = track.getTrackDisplayName();
       
        tname = getShorterName(tname, 5, 40);
        String g = track.getTrack().getGenderSymbol();
          p("Track name is: "+tname+", gender="+g);
        if (g != null) {
          tname += " <font size='4'><b>"+g+"</b></font>" ;
        }
        String name = track.getShortName() + ": " + tname;

       
        String tool = "File: " + file ;
        if (track.getSampleafeture() != null) {
            tool = "<html>" + tool + "<br>Example: " + track.getSampleafeture().toString() + "</html>";
        }
        if (check) {
            box = new JCheckBox("<html>"+ name+"</html>");
            // box.setForeground(track.getRenderType().getColor(0));              
            box.setSelected(track.isVisible());
            box.setToolTipText(tool);
            box.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    track.setVisible(box.isSelected());
                    if (control != null) {
                        control.recreateView(false);
                    }
                }
            });
            box.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
                    if (list != null) {
                        list.mouseClicked(evt);
                    } else if (evt.getClickCount() > 1) {
                        getTrackOptions();
                    }
                }
            });
            if (bgcolor != null) {
                box.setBackground(bgcolor);
            }
        } else {
            if (selected ) {
                name = "<b>"+name+"</b>";
            }
            else {
                
            }
            lbl = new JLabel("<html><b>-</b> "+name+"</html>");
            lbl.setToolTipText(tool);
            if (bgcolor != null) {
                lbl.setBackground(bgcolor);
                lbl.setForeground(fg);
            }
        }

        // ad colors
        render = track.getRenderType();
        if (render == null) {
            render = track.getDefaultRenderer();
        }
        int nrcolors = render.getNrColors();
        panColors = new JPanel();
        
        boolean NEWLINES = false;
        if (!NEWLINES) panColors.setLayout(new FlowLayout());
        else panColors.setLayout(new GridLayout(nrcolors,3));
        if (bgcolor != null) {
                panColors.setBackground(bgcolor);
            }
        
        int dims = 26;
        if (!check) dims = 10;
        for (int i = 0; i < nrcolors; i++) {
            String s = render.getColorShortName(i);
            Color c = render.getColor(i);
            ColorBtn b = null;
            if (NEWLINES) b=new ColorBtn(null, c, check);
            else b=new ColorBtn(s, c, check);
            Dimension d = new Dimension(dims, dims);
            b.setSize(d);
            b.setMaximumSize(d);
            b.setPreferredSize(d);
            if (s == null) s = "";
            b.setToolTipText("<html>"+render.getColorName(i)+ "<br> "+b.getColorString()+"/<html>");
            if (NEWLINES) {
                panColors.add(new JPanel());
                panColors.add(b);
                panColors.add(new JLabel(s));
            }
            else {
                panColors.add(new FlowPanel(b));
            }
            b.addActionListener(new BtnListener(b, i));
        }
        if (check) {
            add("Center", box);
        } else {
            add("Center", lbl);
        }
        add("South", panColors);

    }

    private class BtnListener implements ActionListener {

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
                if (nr == 0) {
                    track.setColor(c);
                }
            }
        }
    }

    private String getShorterName(String n, int minlen, int maxlen) {
        if (n != null && n.length() > maxlen) {
            n = n.substring(0, maxlen);
            int dot = n.lastIndexOf(".");
            int under = n.indexOf("_");
            int hy = n.indexOf("-");
            int col = n.indexOf(":");
            int p = Math.max(dot, under);
            p = Math.max(p, hy);
            p = Math.max(p, col);
            if (p > minlen && p < maxlen) {
                n = n.substring(0, minlen);
            }
        }
        return n;
    }

    public void setText(String s) {
        box.setToolTipText(s);
    }

    public void setSelected(boolean b) {
        this.box.setSelected(b);
    }
    public boolean isSelected() {
        return box.isSelected();
    }

    public KaryoTrack getTrack() {
        return this.track;
    }

    private void getTrackOptions() {
        TrackOptionsPanel p = new TrackOptionsPanel(track);
        int ans = JOptionPane.showConfirmDialog(SingleTrackPanel.this, p);
        if (ans == JOptionPane.OK_OPTION) {
            p.updateTrack();
            // box.setForeground(track.getColor());

            if (control != null) {
                control.recreateView(false);
            } else {
                p("Control is null! Not recreating view");
            }
        }
    }

    private void p(String s) {
        //Logger.getLogger("SingleTrackPanel").info(s);
        System.out.println("SingleTrackPanel: " + s);
    }
}
