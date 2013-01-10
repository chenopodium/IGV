package com.iontorrent.views.basic;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Vector;


/**
  * @author:  Chantal Roth
  */

public abstract class GuiGeneralPath extends GuiShape
{

   protected GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
   protected Vector pts;
   private boolean forward;

   private GuiObject gstart;
   private GuiObject gend;
// ***************************************************************************
// CONSTRUCTOR
// ***************************************************************************

	public GuiGeneralPath(Point start, Color cborder, Color cfill, boolean fill )
   {
      super(start );
      setForeground( cborder );
      this.cborder = cborder;
      this.cfill = cfill;
      this.fill = fill;
      update();
   }

// ***************************************************************************
// GET/SET
// ***************************************************************************
	public Shape getShape() {
		update();
		return path;
	}
	public void setArrowStart(GuiObject g) { this.gstart = g; }
	public void setArrowEnd(GuiObject g) { this.gend = g; }

	public void drawArrow(Graphics g) {
		if (gstart== null || gend == null) return;

	   Point s = gstart.getPosition();
	   Point e = gend.getPosition();
	    Vector2d line = new Vector2d(s, e);
	   int headlen = 6;
	   int headwidth = 10;

	   int len = (int)line.getLength();

	  	// its all relative to this line (nstart is like 0)
	  Vector2d head = new Vector2d(line);

	  int startx = (int)s.getX();
	  int starty = (int)s.getY();

	  line.subtract(7);
	  Point E = line.getPoint();
	  line.subtract(headlen);
	  Point M = line.getPoint();
	  head.rotate90();
	  head.normalize();
	  head.scale(headwidth/2);
	  Point A = head.getPoint(M);
	  head.scale(-1);
	  Point B = head.getPoint(M);

	  g.drawLine( (int)A.getX()+startx,(int)A.getY()+starty,(int) E.getX()+startx,(int) E.getY()+starty);
	  g.drawLine((int) B.getX()+startx, (int)B.getY()+starty,(int) E.getX()+startx, (int)E.getY()+starty);
	}
// ***************************************************************************
// DRAWABLE
// ***************************************************************************
	public abstract void update() ;

}