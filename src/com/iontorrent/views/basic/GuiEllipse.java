package com.iontorrent.views.basic;

import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * @author: Chantal Roth
 */
public class GuiEllipse extends GuiShape {

    private Point start;     // start point of the line
    private Point M;			// other points of arrow
    private float w;
    private float h;
    private Ellipse2D.Float ellipse = null;

// ***************************************************************************
// CONSTRUCTOR
// ***************************************************************************
    public GuiEllipse(Point M, float w) {
        this(M, w, w, Color.black);
    }

    public GuiEllipse(Point M, float w, Color c) {
        this(M, w, w, c);
    }

    public GuiEllipse(Point M, float w, float h, Color c) {
        this(M, w, h, c, c, true);
    }

    public GuiEllipse(Point M, float w, float h, Color cborder, Color cfill, boolean fill) {
        this(new Point(0, 0), M, w, h, cborder, cfill, fill);
    }

    public GuiEllipse(Point start, Point M, float w, float h, Color cborder, Color cfill, boolean fill) {
        super(start);
        this.start = start;
        this.M = M;
        this.w = w;
        this.h = h;
        setForeground(cborder);
        this.cborder = cborder;
        this.cfill = cfill;
        this.fill = fill;
        this.start = start;
        update();
    }

// ***************************************************************************
// GET/SET
// ***************************************************************************
    public Shape getShape() {
        return ellipse;
    }
// ***************************************************************************
// DRAWABLE
// ***************************************************************************

    public void update() {
        setAbsolutePosition(start);
        float startx = (float) getAbsolutePosition().getX();
        float starty = (float) getAbsolutePosition().getY();
        float mx = (float) M.getX() + startx;
        float my = (float) M.getY() + starty;
        ellipse = new Ellipse2D.Float(mx - w, my - h, w * 2, h * 2);
        setAbsoluteSize(ellipse.getBounds().getSize());
    }

    public String toString() {
        int startx = (int) getAbsolutePosition().getX();
        int starty = (int) getAbsolutePosition().getY();

        String res = "Ellipse: M= (" + startx + ", " + starty + "), w=:" + w + ", h=:" + h;
        return res;
    }

    public void p(String s) {
        // System.out.println("Arrow: "+s);
    }
}