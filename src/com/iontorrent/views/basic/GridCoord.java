package com.iontorrent.views.basic;

import java.awt.Graphics2D;

public class GridCoord extends AbstractCoord{
	
		
	public GridCoord(GuiCanvas canvas) {
		super(canvas);
	}
	
// ***************************************************************************
// FROM GRIDIF 
// ***************************************************************************
	
    @Override
	public void draw(Graphics2D g) {
	 	if (g == null) return;
	 	int nr = 10;
	 	g.setColor(color_fore);
	 	
	 	
	 	double dx = canvas.getStartingWidth()/nr;
	 	double dy = canvas.getStartingHeight()/nr;
	 	double x = 0;
	 	for (int i = 0; i < nr; i++) {
//	 		canvas.drawString(g, ""+(i*nr)+"%",(int)x,30, Color.blue, null);
	 		g.drawLine((int)x, 0, (int)x, (int)canvas.getStartingHeight());
	 		x +=dx;
	 	}
	 	double y = 0;
	 	for (int i = 0; i < nr; i++) {
//	 		canvas.drawString(g, ""+(i*nr)+"%",(int)y,30, Color.blue, null);
	 		g.drawLine(0, (int)y, (int)canvas.getStartingWidth(), (int)y);
	 		y +=dy;
	 	}	
	}
}
