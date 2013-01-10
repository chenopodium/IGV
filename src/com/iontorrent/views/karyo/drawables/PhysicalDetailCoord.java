/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.karyo.drawables;

import com.iontorrent.views.basic.GuiCanvas;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 *
 * @author Chantal Roth
 */
public class PhysicalDetailCoord extends PhysicalCoord {

    private static final int DIV = 1000;
//    private static final int KB = 1;
//    private static final int KB10 = 10;
//    private static final int KB100 = 100;
    private static final int MB = 1000;
    private static final int MB10 = 10000;
    private static final int MB100 = 100000;
    private static final int SCALE = 1;
    private static final int DY = 5;
    private int startbase;


    public PhysicalDetailCoord(GuiCanvas canvas, int startx, int starty, int height, long bases, int startbase) {
        super(canvas, startx, starty, height, bases);

        
        // in 1 kilobases
        startbase = (int) startbase / 1000;
        bases = (int) bases / 1000;

        this.bases = bases;
        // start is rounded to the nearest 100 KB
        // startbase is in kilo bases
        startbase = (int) (startbase / MB100) * MB100;
        this.startbase = startbase;
    }

// ***************************************************************************
// FROM GRIDIF
// ***************************************************************************
    @Override
    public void draw(Graphics g) {
        if (g == null) {
            return;
        }
        g.setColor(getForeground());
        g.setFont(font);
        double dy = (double) height / (double) bases;

        g.drawLine(startx, starty-height, startx, starty);

        double y = 0;
        //number vertical line
        double zoom = canvas.getYZoomFactor();
        int first = startbase;
        // i is NOT scaled
        boolean shown = false;
        for (int i = startbase; i < bases + startbase; i += 5*MB) {
            y = starty - (i - startbase) * dy;
            String out;

            if (shown == false) {
                out = "" + first + "k";
                canvas.drawText((Graphics2D) g, out, startx + 6, (int) y + DY);
                shown = true;
            } else {
                // divisible by 100 MB, always draw it
                if (i % (MB100) == 0) {
                    out = "" + (i / MB) + "MB";
                    canvas.drawText((Graphics2D) g, out, startx + 6, (int) y + DY);
                    g.drawLine(startx, (int) y, startx + 5, (int) y);
                } else if (i % MB10 == 0) {
                    // almost always draw the 10 MB line
                    if (zoom > 0.1) {
                        g.drawLine(startx, (int) y, startx + 5, (int) y);
                    } else {
                        g.drawLine(startx, (int) y, startx + 2, (int) y);
                    }
                    // only write 10KB text if zoomed in a bit
                    if (zoom > 0.2) {
                        int nr = i - startbase;
                        out = "" + nr / MB + "MB";
                        canvas.drawText((Graphics2D) g, out, startx + 6, (int) y + DY);
                    }
                } else if (zoom > 0.5) { // every 1 MB
                    // only consider if zoomed in
                    // 			p("drawing :"+startx+"/"+y);
                    g.drawLine(startx, (int) y, startx + 2, (int) y);
                    // detail view
//                    if (zoom > 0.8 && i % 5 * MB == 0) {
//                        int nr = i - startbase;
//                        out = "" + ((nr)/MB * SCALE) + " MB";
//                        canvas.drawText((Graphics2D) g, out, startx + 6, (int) y + DY);
//                    }
                }
            }
            y += dy;
        }

    }
}