package com.iontorrent.views.basic;

import java.awt.*;

/**
  * Line.java     Version 0.1
  * @author:   Chantal Roth
  * Date:      Oct. 6, 2000
  */

public class GuiRectangle extends GuiObject
{
   private int defaultheight = 1;
   private Color cfill = Color.orange;
   private boolean fill;
   private boolean is3d = true;
   private String description;

   public GuiRectangle( Point start, Point end ) {
	  this( start, end, Color.black, Color.orange, true);
   }

   public GuiRectangle( Point start, Point end, Color border ) {
	  this( start, end, border, border, true);
   }

   public GuiRectangle( Point start, Point end, Color border, Color fill ) {
	  this( start, end, border, fill, true);
   }

   public GuiRectangle(  Point start, Point end , Color border, Color cfill, boolean fill )
   {
	  super( start );
	  setForeground( border );
	  this.cfill = cfill;
	  this.fill = fill;
	  int x = (int)start.getX();
	int y = (int)start.getY();

	setAbsolutePosition( new Point( x, y ) );
	setAbsoluteSize(new Dimension((int)Math.abs(end.getX()-x), (int)Math.abs(end.getY() - y)));
   }

	public void setDescription(String desc) {
		description = desc;
	}

   public String toHtml(){
		return getDescription();
	}

	public boolean drawAbove() { return true; }

	public String getName() { return "Rectangle"; }

	public String getDescription() {
		return description;

	}

   public void draw( Graphics gg )
   {
	  Graphics2D g = (Graphics2D)gg;
	  int startx = (int)getAbsolutePosition().getX();
	  int starty = (int)getAbsolutePosition().getY();
	  int len_x = getWidth();
	  int len_y = getHeight();
	  if (len_x > 100000 || len_y > 100000) {
		err("Bad lengths: "+len_x+", "+len_y);
		return;
	  }
	   if (startx > 100000 || starty > 100000) {
		err("Bad coordinates: "+startx+", "+starty);
		return;
	  }
	  g.setColor(getForeground());

	  if (fill) {
		 g.setColor(cfill);
		 if (is3d) g.fill3DRect( startx, starty, len_x, len_y, true);
                else g.fillRect( startx, starty, len_x, len_y);
	  }
	  else g.drawRect( startx, starty, len_x, len_y );


   }
   public void set3D(boolean b) {
       this.is3d = b;
   }
   public boolean is3D() { return is3d; }
   public void clear( Graphics g )
   {
	//	System.out.println("clearing");
		g.setColor( getBackground() );
		int startx = (int)getAbsolutePosition().getX();
		int starty = (int)getAbsolutePosition().getY();
		int len_x = getWidth();
		int len_y = getHeight();
		g.fillRect( startx-10, starty-1, len_x+20, len_y+2 );
   }
   public String toString() {
       return "GuiRectangle ("+this.getX()+", "+this.getY()+")";
   }

}