/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.karyo.drawables;

import com.iontorrent.views.basic.GuiCanvas;
import com.iontorrent.views.basic.GuiObject;
import java.awt.*;

/**
 *
 * @author Chantal Roth
 */

public class PhysicalCoord extends GuiObject{

	protected int starty;
	protected int height;
	protected long bases;
	protected int startx;
	protected GuiCanvas canvas;
	
	protected static final int KB100 = 100;
	protected static final int KB10 = 10;
        private static final int MB = 1000;
        private static final int MB10 = 10000;
        private static final int MB100 = 100000;
	protected static final int DIV = MB;
	protected static final int SCALE = 1;

        protected Font font = new Font("SansSerif", Font.PLAIN, 8);
	
	public PhysicalCoord(GuiCanvas canvas, int startx, int starty, int height, long bases) {
		super(null);
		this.height = height;
		this.starty = starty;
		this.startx = startx;
		this.canvas = canvas;
		this.bases = bases / 1000;
		setAbsolutePosition(new Point(startx, starty));
		setAbsoluteSize(new Dimension(10, height));
		setForeground(Color.gray);
		p("height:"+height);
	}

// ***************************************************************************
// FROM GRIDIF
// ***************************************************************************

	public void draw(Graphics g) {
		if (g == null) return;
		g.setColor(getForeground());
		if (bases == 0) return;
                g.setFont(font);
		double dy = (double)height /(double)bases;
		g.drawLine(startx, starty, startx, starty+height);

		double y = 0;
		//number vertical line
		double zoom = canvas.getYZoomFactor();

	 //	System.out.println("zoom:"+zoom);

		for (int i = MB; i < bases; i+=MB) {
			y = starty+i*dy;
			if (i % (MB10) == 0) {
				g.drawLine(startx - 5, (int)y, startx+5, (int)y);
				canvas.drawText((Graphics2D)g, ""+(i*SCALE/MB)+" M", startx +7, (int)y);
			}
//			else if (i % (5*MB) == 0) {
//				g.drawLine(startx - 5, (int)y, startx+5, (int)y);
//				canvas.drawText((Graphics2D)g, ""+(i*SCALE/MB)+"M", startx +5, (int)y+DY);
//			}
//			else if (i % (KB100) == 0) {
//				if (zoom > 0.1) g.drawLine(startx - 5, (int)y, startx+5, (int)y);
//				float f = (float)(i*SCALE)/10.0f;
//
//				if (zoom > 0.3) canvas.drawText((Graphics2D)g, ""+(f)+"M", startx +5, (int)y+DY);
//			}
                        else if (i % (2*MB) == 0) {
				//p("drawing :"+startx+"/"+y);
				g.drawLine(startx - 2, (int)y, startx+2, (int)y);
				//if (zoom > 0.3) canvas.drawText((Graphics2D)g, ""+(i*SCALE)+"M", startx +5, (int)y+DY);
			}
			
			y +=dy;
		}

	}

}
