package com.iontorrent.views.basic;

import java.awt.*;
import java.awt.geom.GeneralPath;

/**
  * @author:  Chantal Roth
  */

public class GuiTriangle extends GuiObject
{
   private Point start;     // start point of the line
   private Point A;			// other points of arrow
   private Point B;
   private Point C;       // end point of the line, arrow end
   private Color cborder;
   private Color cfill;
   private boolean fill = true;

   private GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

   public GuiTriangle(Point A, Point B, Point C ) {
	  this(A, B, C, Color.black);
   }

	public GuiTriangle(Point A, Point B, Point C,  Color c ) {
	  this(A, B, C, c, c, true);
   }
   public GuiTriangle(Point A, Point B , Point C,  Color cborder, Color cfill, boolean fill ) {
		this(new Point(0,0), A, B, C, cborder, cfill, fill);
	}

   public GuiTriangle(Point start, Point A, Point B , Point C,  Color cborder, Color cfill, boolean fill )
   {
	  super(start );
	  this.start = start;
	  this.A = A;
	  this.A = A;
	  this.B = B;
	  this.C = C;

	  setForeground( cborder );

	  this.start = start;
	  update();
   }


   public String toHtml(){ return "Triangle"; }

	public boolean drawAbove() { return true; }

	public String getName() { return "Triangle"; }

	public String getDescription() { return "Triangle"; }

	public void update()
   {
		setAbsolutePosition( start );

		float startx = (float)getAbsolutePosition().getX();
		float starty = (float)getAbsolutePosition().getY();

		float cx = (float)C.getX()+startx;
		float cy = (float)C.getY()+starty;

		float ax = (float)A.getX()+startx;
		float ay =(float) A.getY()+starty;

		float bx =(float) B.getX()+startx;
		float by = (float)B.getY()+starty;

		path.moveTo(cx, cy);
		path.lineTo(ax, ay);
		path.lineTo(bx, by);
		path.closePath();

		setAbsoluteSize(path.getBounds().getSize());
	}
   public void move (int dx, int dy) {
	 super.move(dx, dy);
	// A.setLocation(new Point(A.getX()+dx, A.getY()+dy);
	// B.setLocation(new Point(B.getX()+dx, B.getY()+dy);
	// C.setLocation(new Point(C.getX()+dx, C.getY()+dy);
	 update();
   }

   public void draw( Graphics g )
   {
	  Graphics2D gg = (Graphics2D)g;
	  gg.setColor(cborder);
	  gg.draw(path);
  //    p("drawing triangle, path is:"+path.toString()+ ", A:"+A+", B:"+B+" C:"+C);
	  if (fill) {
		 gg.setColor(cfill);
		 gg.fill(path);
		}


   }

   public void clear( Graphics g )
   {
		g.setColor( getBackground() );
		g.fillRect( 0, 0, (int)getViewSize().getWidth(), (int)getViewSize().getHeight() );
   }

	public void p(String s) {
		System.out.println("Arrow: "+s);
	}

}