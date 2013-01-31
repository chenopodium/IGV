package com.iontorrent.views.basic;

import java.awt.*;

public class GeneticCoord extends GuiObject{

	private int starty;
	private int height;
	private double totalcm;
	private int startx;
	private int startcm;
	private GuiCanvas canvas;

	private static final int DY = 15;

	public GeneticCoord(GuiCanvas canvas, int startx, int starty, int height, double totalcm) {
		this(canvas, startx, starty, height, totalcm, 0.0d);
	}

	public GeneticCoord(GuiCanvas canvas, int startx, int starty, int height, double totalcm, double startcm) {
		super();
		this.height = height;
		this.starty = starty;
		this.startcm = (int)startcm;
		this.startx = startx;
		this.canvas = canvas;
		this.totalcm = totalcm;
		setAbsolutePosition(new Point(startx, starty));
		setAbsoluteSize(new Dimension(40, height));
		setForeground(Color.black);
		//p("height:"+height);
	}

// ***************************************************************************
// FROM GRIDIF
// ***************************************************************************
	public boolean isMovable() {
		return true;
	}
	public boolean isSelectable() {
		return true;
	}
	public void draw(Graphics g) {
		if (g == null) return;
		startx = getX();
		starty = getY();
		g.setColor(getForeground());
		if (isSelected()) g.setColor(Color.red);
		double dy = height /totalcm;
	//	p("dy:"+dy);
		g.drawLine(startx, starty, startx, starty+height);

		double y = 0;
		//number vertical line
		double zoom = canvas.getYZoomFactor();
		for (int i = startcm; i < totalcm; i++) {
			y = starty+i*dy;
			if (i % 10 == 0) {
				g.drawLine(startx - 5, (int)y, startx+5, (int)y);
				if (zoom > 0.05 || i % 20 == 0) canvas.drawText((Graphics2D)g, ""+i+" cm", startx +5, (int)y+DY);
			}
			else {
		//		p("drawing :"+startx+"/"+y);		       
				g.drawLine(startx - 2, (int)y, startx+2, (int)y);
				if (zoom > 0.3) canvas.drawText((Graphics2D)g, ""+i+" cm", startx +5, (int)y+DY);
			}

			y +=dy;
		}

	}

}
