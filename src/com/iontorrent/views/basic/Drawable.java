/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.basic;

import java.awt.*;
import java.util.ArrayList;

public interface Drawable {

// ***************************************************************************
// DRAWING
// ***************************************************************************

	public void draw(Graphics g);

	public void clear(Graphics g);

	public Color getForeground();

	public void setForeground(Color c);

// ***************************************************************************
// NEW POSITIONS
// ***************************************************************************
	public Point getPosition();

	public void setPosition(Point p);

	public Rectangle getBounds();

	public Dimension getSize();

// ***************************************************************************
// POSITIONS
// ***************************************************************************
	/** @deprecated */
	public Dimension getAbsoluteSize();

	/** @deprecated */
	public Point getAbsolutePosition();

	/** @deprecated */
	public void setAbsolutePosition(Point p);

	public void move(int dx, int dy);

	public int getX();

	public int getY();

	public int getWidth();

	public int getHeight();
// ***************************************************************************
// ATTRIBUTES
// ***************************************************************************
	public boolean isMovable();

	public boolean isSelectable();

	public boolean isStatic();

	public boolean isText();

	public ArrayList<Drawable> getDrawables();

	public boolean isVisible();

	public void setVisible(boolean vis);

// ***************************************************************************
// SELECTIONS
// ***************************************************************************


	public void setSelected(boolean b);

	public void setSelected(boolean b, boolean sendevent);

	public boolean isSelected();

// ***************************************************************************
// OTHER
// ***************************************************************************

	/** returns true, if the point p (a absolute position) is contained in this object (determinded
	 * using the absolute position and absolute size
	 */
	public boolean containsPoint(Point p);

	public boolean overlaps(Rectangle rect);

    @Override
	public String toString();

	public String toHtml();

	public String getToolTipText(java.awt.event.MouseEvent evt);
}