/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import java.awt.Graphics;

/**
 *
 * @author Chantal Roth
 */
public class PeakFunction {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PeakFunction.class);
    
    double m = 0.005;
    double k = 0.006;
    double scaley = 1.5;
    double dt = 30.0;
    
    private double curvedata[];
    private int nrpoints;
    
    public PeakFunction(int nrpoints) {
        this.nrpoints = nrpoints;
        
    }
    public void draw(Graphics g, int startx, int starty, int width, int height) {        
        double dx = (double)width/(double)nrpoints;
        // height must correspond to 100
        double scale = (double)height/100.0;
        int prevx = startx;
        int prevy = starty;
      //  p("Peak Function: dx="+dx+", maxx = "+dx*nrpoints+", scale="+scale+", 100*scale = "+100*scale+", width="+width+", height="+height);
        
        if (curvedata == null) curvedata =  compute(nrpoints);
        for (int i = 0; i < nrpoints; i++) {
            int offx =(int)(dx*i);
            int offy = (int) (curvedata[i]*scale);  
            int x = startx + offx;
            int y = starty + offy;
       //     p((x-startx)+"/"+(y-starty));
            g.drawLine(prevx, prevy, x, y);            
            prevx = x;
            prevy = y;
        }
    }
    
    private double[] compute(int nrpoints) {
        double[] res = new double[nrpoints];
        double max = 0;
        for (int f = 1; f < nrpoints; f++) {
            res[f] = compute(f * dt);
            if (res[f] > max) max = res[f];
        }
    //    p("Peak Function: Got max peak: "+max);
        return res;
    }

    private void p(String msg) {
        log.info(msg);
    }

    private void err(String msg) {
        log.error(msg);
    }
    private double compute(double t) {
        if (t <= 0 || m == k) {
            return 0;
        }        
        double y = scaley * (Math.exp(-t * k) - Math.exp(-t * m)) / (m - k);
        return y;
    }
}
