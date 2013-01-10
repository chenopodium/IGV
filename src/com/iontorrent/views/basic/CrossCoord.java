package com.iontorrent.views.basic;

import java.awt.Graphics2D;

public class CrossCoord extends AbstractCoord{
	
		
	public CrossCoord(GuiCanvas canvas) {
		super(canvas);
	}
	
// ***************************************************************************
// FROM GRIDIF 
// ***************************************************************************
	
	public void draw(Graphics2D g) {
		if (g == null) return;
	 	int nr = 10;
	 	g.setColor(color_fore);
	 	double dx = canvas.getStartingWidth()/nr;
	 	double dy = canvas.getStartingHeight()/nr;
	 	double x = 0;
	 	int mx = (int)canvas.getMiddleX();
	 	int my = (int)canvas.getMiddleY();
	 	g.drawLine(0, my, (int)canvas.getStartingWidth(), (int)my);
	 	g.drawLine(mx, 0, mx, (int)canvas.getStartingHeight());
	
		// number horizontal line
	 	for (int i = 0; i < nr; i++) {
	 		
	 		if (i != 5) {
//	 			canvas.drawString(g, ""+(i*nr)+"%",(int)(x-5/canvas.getFactorX()),(int)(my-20/canvas.getFactorY()), null, null);
	 		}
	 		
	 		g.drawLine((int)x, (int)my-2, (int)x, (int)my+2);
	 		x +=dx;
	 	}
	 	double y = 0;
	 	//number vertical line
	 	for (int i = 0; i < nr; i++) {
	 		if (i != 5) {
	// 			canvas.drawString(g, ""+(i*nr)+"%", (int)(mx+5/canvas.getFactorX()), (int)y, null, null);
	 		}
	 		
	 		g.drawLine((int)mx-2, (int)y, (int)mx+2, (int)y);
	 		y +=dy;
	 	}	
		
	}

}
