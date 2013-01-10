package com.iontorrent.views.basic;

import java.awt.*;

/**
  * Line.java     Version 0.1
  * @author:   E Li
  * Date:      Oct. 6, 2000
  * Purpose:   Draw a line on the frame word canvas.
  */

public class GuiLine extends GuiObject
{
   private Point start;     // start point of the line
   private Point end;       // end point of the line
   private int defaultheight = 1;
  
   public GuiLine( Point start, Point end ) {
   	  this( start, end, Color.blue);	
   }
   	
   public GuiLine(  Point start, Point end , Color color )
   {
      super( start );

      // test dummy color
      setForeground( color );

      this.start = start;
      this.end = end;
      update();
   }


   public String toHtml(){ return "Line"; }
	
	public boolean drawAbove() { return true; }
	
	public String getName() { return "Line"; }
	
	public String getDescription() { return "Line"; }
	
	public void update()
   {
		int x = (int)start.getX();
		int y = (int)start.getY();
		
		setAbsolutePosition( new Point( x, y ) );
	}


   public void draw( Graphics g )
   {
      int startx = (int)getAbsolutePosition().getX();
      int starty = (int)getAbsolutePosition().getY();

      int len_x = (int )( end.getX() - start.getX() );
      int len_y = (int)( end.getY() - start.getY() );
	  g.setColor(getForeground());
      g.drawLine( startx, starty, startx + len_x, starty + len_y );
   }

   public void clear( Graphics g )
   {
		g.setColor( getBackground() );
		g.fillRect( 0, 0, (int)getViewSize().getWidth(), (int)getViewSize().getHeight() );
   }

}