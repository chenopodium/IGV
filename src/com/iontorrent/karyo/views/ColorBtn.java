/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.views;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;

/**
 *
 * @author Chantal
 */
public class ColorBtn extends JButton{
    
    Font f = new Font("Helvetica", Font.PLAIN, 9);
    
    public ColorBtn(String tinyname, Color c, boolean border) {
        super();
        super.setText(tinyname);
        super.setIcon(null);
        this.setFont(f);
        this.setBackground(c);
        
        this.setForeground(Color.black);
        super.setOpaque(true);
        super.setPressedIcon(null);
        super.setSelectedIcon(null);
        super.setRolloverIcon(null);
        if (border) super.setBorder(BorderFactory.createEtchedBorder());
        super.setRolloverEnabled(false);
    }
    
    public String getColorString() {
        Color c = this.getBackground();
        int rgb = c.getRGB();
        String hex = Integer.toHexString(rgb);
        return "("+c.getRed()+","+c.getGreen()+","+c.getBlue()+"), "+hex;
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D gg = (Graphics2D)g;
        gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        Color c = this.getBackground();
        g.setColor(c);
        Rectangle r = this.getBounds();     
        g.fill3DRect(0,0, r.width, r.height, true);        
        g.setColor(this.getBackground().darker());
        g.draw3DRect(0,0, r.width, r.height, true);
        
        if (this.getText() != null && r.width>20) {
            FontMetrics fm = g.getFontMetrics(f);
            int fw = fm.stringWidth(this.getText());
            if (fw+4<r.width) {
                int dx = (r.width - fw)/2;
                Color tf = this.getForeground();
                int avg = (c.getRed()+c.getBlue()+c.getGreen())/3;
                if (avg < 110) {
                    tf = Color.white;
                }
                g.setColor(tf);
                g.drawString(this.getText(), dx, r.height/2+f.getSize()/2);
            }
        }
        
    }
    private void p(String s) {
        System.out.println("ColorBtn: "+s);
    }
}
