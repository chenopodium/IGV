/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.karyo.data.Chromosome;
import com.iontorrent.karyo.views.KaryoManager;
import com.iontorrent.views.basic.ZoomCanvas;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

/**
 *
 * @author Chantal
 */
public class CnvPanel extends JPanel{
     CnvData data;
    
     int WIDTH = 1200;
     int HEIGHT = 800;
     KaryoManager manager;
     int[] startposperchr;
     
     
     int BORDER = 50;
     double MAXY = 1.8;
     long tot;
     int MB = 1000000;
     double actualmax;
     
    public CnvPanel(KaryoManager manager, CnvData data) {
        this.manager = manager;
        this.data = data;
        
        actualmax = 0;
         for (CnvDataPoint point: data.getPoints()) {
             if (Math.abs(point.ratio) > actualmax) actualmax = Math.abs(point.ratio);
         }
       
      //   p("Got actualmax: "+actualmax);
        startposperchr = new int[manager.getChromosomes().size()+1];
        tot = 0;
        int nr = 0;
        for (Chromosome chr: manager.getChromosomes()) {
            startposperchr[nr] = (int)tot;
            nr++;            
            tot += chr.getLength()/MB;
            
        }
        tot += 50;
        
        this.setSize(new Dimension(WIDTH, HEIGHT));
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    }
    
    @Override
    public void paintComponent(Graphics oldg) {
        Graphics2D g = (Graphics2D)oldg;
        int width = getWidth();
        int height = getHeight();
        int W = (int) (width - 2*BORDER);
        int H = (int) (height - 2*BORDER);
        
        double dxperb = W/(double)tot;
        int MY = H/2+BORDER;
        
         g.setColor(Color.white);
                  
         g.fillRect(0,0,width, height);
         
         g.setColor(Color.blue);
         g.setFont(new Font("Arial", Font.BOLD, 20));
         g.drawString("Whole Genome CNV log2ratio plot", width/2-200, BORDER-20);
        // draw grid
         
         g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.black);
        
        g.setStroke(new BasicStroke(2));
        g.drawRect(BORDER, BORDER, W, H);
        g.drawLine(BORDER, MY, W+BORDER, MY);        
        
        int nr = 0;
        int dline=10;
        actualmax = Math.max(actualmax, MAXY);
         double dy = (int)H/2.0/actualmax;
        //p("MY: "+MY+", dxperb="+dxperb+", dy="+dy);
         g.setStroke(new BasicStroke(1));
          
        for (Chromosome chr: manager.getChromosomes()) {
            int xa = BORDER+(int)(startposperchr[nr]*dxperb);
            int xb = (int) (xa+(double)(chr.getLength()/MB)*dxperb);
           // p("Chr "+chr.getName()+", x="+xa+"-"+xb+", size="+chr.getLength());
            g.setColor(new Color(200,200,200));
            g.drawLine(xa, BORDER+1, xa, height-BORDER-2);
            int mx = (xa+xb)/2;
            g.setColor(Color.black);
            int delta = 15;
            if (nr % 2 ==0) delta = 30;
            g.drawString(chr.getName().substring(3), mx-5, height-BORDER+delta);            
            nr++;            
        }
         g.setStroke(new BasicStroke(1));
        Color cgray = new Color(220,220,220);
        
        double delta = Math.pow(10, ((int)Math.log10(actualmax)-0.5))/2.0;
                
        for (double yy = delta; yy < actualmax; yy+=delta) {
             int y = MY - (int)(yy*dy);
             int y1 = MY + (int)(yy*dy);
             g.setColor(cgray);
             g.drawLine(BORDER, y, W+BORDER, y);        
             g.drawLine(BORDER, y1, W+BORDER, y1);        
             g.setColor(cgray.darker());
             g.drawString(""+yy, BORDER-25, y+4);
             g.drawString("-"+yy, BORDER-30, y1+4);
        }
        
        g.setColor(Color.blue.darker());
        int r = 3;
        int count = 0;
        for (CnvDataPoint point: data.getPoints()) {
            double pos = point.pos/MB;
            int chr = point.chr;
            if (chr < startposperchr.length) {
                int startx = BORDER+(int)(startposperchr[chr-1]*dxperb);
                int y = MY - (int)(point.ratio*dy);
                int x = startx+(int)(pos*dxperb);
                if (count % 100000 == 0) {
                    p("DataPoint: chr="+chr+", x="+x+", ratio="+point.ratio+", y="+y+", pos="+pos);
                }
                g.setColor(Color.blue.brighter());
                g.fillOval(x, y, r, r);
                g.setColor(Color.black);
                g.drawOval(x, y, r, r);
                count++;
            }            
        }
        g.setColor(Color.red.darker());
        r = 3;
        count = 0;
        for (CnvDataPoint point: data.getRedLinePoints()) {
            double startpos = point.pos/MB;
            double endpos = point.end/MB;
            int chr = point.chr;
            if (chr < startposperchr.length) {
                int startx = BORDER+(int)(startposperchr[chr-1]*dxperb);
                int y = MY - (int)(point.ratio*dy);
                int x1 = startx+(int)(startpos*dxperb);
                int x2 = startx+(int)(endpos*dxperb);
                if (count % 10 == 0) {
                    p("red DataPoint: chr="+chr+", x="+x1+", ratio="+point.ratio+", y="+y+", pos="+startpos);
                }
                g.setColor(Color.red.brighter());
                g.fill3DRect(x1, y, x2-x1, r, true);
                g.setColor(Color.black);
                g.drawRect(x1, y, x2-x1, r);
                count++;
            }            
        }
    }
    private void p(String s) {
        System.out.println("CnvPanel: "+s);
    }
}
