package com.iontorrent.views.basic;
import java.awt.*;
import java.util.Vector;
public class GuiSpline extends GuiGeneralPath {


    private GuiObject[] objs;

// ****************************************************************
// *** CONSTRUCTOR
// ****************************************************************
	public GuiSpline(Vector pts) {
		this(pts, Color.black, Color.gray, false);
	}
	public GuiSpline(Vector pts, Color cborder) {
		this(pts, cborder, Color.gray, false);
	}
	public GuiSpline(Vector pts, Color cborder, Color cfill, boolean fill) {
		   super((Point)pts.get(0), cborder, cfill, fill);
		    objs = new GuiObject[pts.size()];
			this.pts = pts;
			update();
	}


// ****************************************************************
// *** GET/SET
// ****************************************************************
	 public void set(GuiObject obj, int i) {
		objs[i]=obj;
	 }

	public void draw( Graphics g ) {
	  super.draw(g);
	  drawArrow(g);
   }

// ****************************************************************
// *** UPDATE
// ****************************************************************


   public void update() {
	    if (pts == null) {
		//	err("NO POINTS");
			return;
	    }
		for(int i = 0; i < pts.size(); i++)  {
			GuiObject o = objs[i];
			if (o != null) {
				pts.set(i, new Point(o.getX(), o.getY()));
			}
		}

		Point S = (Point)pts.get(0);
		setPosition(S);

		float startx = (float)getX();
     	float starty = (float)getY();
		path.reset();
		path.moveTo(startx, starty);

     	for (int i = 1; i < pts.size(); i+=3) {
     		Point A = (Point)pts.get(i);
			Point B = (Point)pts.get(i+1);
			Point C = (Point)pts.get(i+2);
     		float ax = (float)A.getX();
	    	float ay =(float) A.getY();
			float bx = (float)B.getX();
	    	float by =(float) B.getY();
			float cx = (float)C.getX();
	    	float cy =(float) C.getY();
	    	path.curveTo(ax, ay, bx, by, cx, cy);
     	}
		setSize(path.getBounds().getSize());
	}


}
