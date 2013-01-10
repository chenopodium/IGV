package com.iontorrent.views.basic;

import java.awt.*;


public abstract class GuiShape extends GuiObject {
	
   protected boolean fill = true;
   protected Color cborder;
   protected Color cfill;
   
// ***************************************************************************
// CONSTRUCTOR
// ***************************************************************************
  
   public GuiShape(Point start ) {
   	  super(start);
   }
   
// ***************************************************************************
// GET/SET
// ***************************************************************************
	public void setFill(boolean fill) { this.fill = fill; }
	public void setBorderColor(Color c) { this.cborder = c; }
	public void setFillColor(Color c) { this.cfill = c; }

	public abstract Shape getShape();
	
// ***************************************************************************
// DRAWABLE
// ***************************************************************************


   public void draw( Graphics g )
   {
      Graphics2D gg = (Graphics2D)g;
      Shape shape = getShape();
      gg.setColor(cborder);
  //    gg.setStroke(new BasicStroke(0.1f));
      gg.draw(shape);
     
      if (fill) {
      	 gg.setColor(cfill);
      	 gg.fill(shape);
      }
   }

   public void clear( Graphics g )
   {
		g.setColor( getBackground() );
		g.fillRect( 0, 0, (int)getViewSize().getWidth(), (int)getViewSize().getHeight() );
   }

	
}
