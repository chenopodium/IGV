package com.iontorrent.karyo.drawables;

import com.iontorrent.views.basic.GuiObject;
import com.iontorrent.views.basic.TextLine;
import java.awt.*;

public class MarkerTextLine extends TextLine {

    private int dx = 10;
    private GuiObject gui;
    private static final int MAX = 300000;

    public MarkerTextLine(Point start, Point end) {
        this(start, end, Color.blue, 10);
    }

    public MarkerTextLine(Point start, Point end, Color color, int dx) {
        this(start, end, color, dx, null);
    }

    public MarkerTextLine(Point start, Point end, Color color, int dx, GuiObject gui) {
        super(start, end, color, dx, gui);
        this.dx = dx;
        this.gui = gui;
    }

    public void draw(Graphics g) {
        int startx = gui.getX() + gui.getWidth();//(int)getAbsolutePosition().getX();

        int starty = gui.getY();//(int)getAbsolutePosition().getY();

        int endx = (int) (getEndPoint().getX());
        int endy = (int) (getEndPoint().getY());

        if (startx < 0 || starty < 0 || endx < 0 || endy < 0) {
            //		err("TextLine: Cords < 0!");
            //		Exception e = new Exception("TextLine: coords < 0:"+startx+"/"+starty+", "+endx+"/"+endy);
            //		e.printStackTrace();
            return;
        } else if (startx > MAX || starty > MAX || endx > MAX || endy > MAX) {
            err("TextLine: Coords too large");
            Exception e = new Exception();
            e.printStackTrace();
            return;
        }
        if (gui != null) {
            if (gui.isSelected()) {
                g.setColor(Color.red);
            } else {
                g.setColor(gui.getForeground());
            }
        } else {
            g.setColor(getForeground());
        }
        g.drawLine(startx, starty, startx + dx, starty);
        g.drawLine(startx + dx, starty, endx - dx, endy);
        g.drawLine(endx - dx, endy, endx, endy);
    }

    public String toString() {
        return "MarkerTextLine";
    }
}