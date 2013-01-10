package com.iontorrent.views.basic;

import java.awt.*;


public class Line extends GuiObject
{
   protected Point start;     // start point of the line
   protected Point end;       // end point of the line
   private int defaultheight = 1;


   public Line(Point start, Point end ) {
	  this(start, end, Color.blue);
   }

   public Line(Point start, Point end , Color color )
   {
	  super(start );

	  // test dummy color
	  setForeground( color );

	  this.start = start;
	  this.end = end;
	  update();
   }
	public void setStart(Point start) {
		this.start = start;
		update();
	}
	public void setEnd(Point end) {
		this.end = end;
		update();
	}
   public String toHtml(){ return "Line"; }

	public boolean drawAbove() { return true; }

	public String getName() { return "Line"; }

	public String getDescription() { return "Line"; }

	public void update() {
		if (start == null || end == null) return;
		int x = (int)start.getX();
		int y = (int)start.getY();

		 int len_x = (int )( end.getX() - start.getX() );
		 int len_y = (int)( end.getY() - start.getY() );

		setAbsolutePosition( new Point( x, y ) );
		setAbsoluteSize(new Dimension(len_x, len_y));
	}

	public void move(int dx, int dy) {
		int startx = (int)getAbsolutePosition().getX();
		int starty = (int)getAbsolutePosition().getY();
		startx += dx;
		starty += dy;
	//	start = new Point(startx, starty);
		setAbsolutePosition(new Point(startx, starty));
		int endx = (int)end.getX();
		int endy = (int)end.getY();
		endx += dx;
		endy += dy;
		end = new Point(endx, endy);

		int len_x = (int )( endx - startx );
		int len_y = (int)( endy - starty) ;
		setAbsoluteSize(new Dimension(len_x, len_y));

   }

	public void moveStartTo(int x, int y) {
		int startx = (int)getAbsolutePosition().getX();
		int starty = (int)getAbsolutePosition().getY();
		int dx = x - startx;
		int dy = y - starty;
		moveStart(dx, dy);
   }
   public void moveEnd(int dx, int dy) {
		int endx = (int)end.getX();
		int endy = (int)end.getY();
		endx += dx;
		endy += dy;
		end = new Point(endx, endy);
   }
   public void moveStart(int dx, int dy) {
		int startx = (int)getAbsolutePosition().getX();
		int starty = (int)getAbsolutePosition().getY();
		startx += dx;
		starty += dy;
	//	start = new Point(startx, starty);
		int endx = (int)end.getX();
		int endy = (int)end.getY();
		endx += -dx;
		endy += -dy;
		end = new Point(endx, endy);
		setAbsolutePosition(new Point(startx, starty));

		int len_x = (int )( endx - startx );
		int len_y = (int)( endy - starty );
		setAbsoluteSize(new Dimension(len_x, len_y));
   }

	public Point getEndPoint(){ return end; }
   public void moveEnd(Point p) {
		end = p;
   }
   public void moveEndY(int y) {
		end = new Point((int)end.getX(), y);
   }
   public void moveStartY(int y) {
		setAbsolutePosition(new Point((int)getX(), y));
   }
   public void draw( Graphics g )
   {
	  int startx = (int)getAbsolutePosition().getX();
	  int starty = (int)getAbsolutePosition().getY();

	  int len_x = (int )( end.getX() - startx );
	  int len_y = (int)( end.getY() - starty);
	  if (isSelected()) g.setColor(Color.red);
	  else g.setColor(getForeground());

	  g.drawLine( startx, starty, startx + len_x, starty + len_y );
   }

   public void clear( Graphics g )
   {
		g.setColor( getBackground() );
		g.fillRect( 0, 0, (int)getViewSize().getWidth(), (int)getViewSize().getHeight() );
   }

}