/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.basic;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

/**
 *
 * @author Chantal Roth
 */

public interface DrawIF {
	public void draw(Graphics g);
	public void setPosition(Point p);
	public void setDimension(Dimension d);
}
