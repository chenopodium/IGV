package com.iontorrent.views.basic;

import java.awt.Point;

public class Vector2d {
	private double dx;
	private double dy;
	private double len = 0;

	private boolean didlen = false;
	private boolean didnorm = false;

// ***************************************************************************
// CONSTRUCTORS
// ***************************************************************************

	public Vector2d(Point a, Point b) {
		dx = b.getX() - a.getX();
	    dy = b.getY() - a.getY();
	}
	public Vector2d (Vector2d v) {
		this.dx = v.dx;
		this.dy = v.dy;
	}

	public Vector2d (double dx, double dy) {
		this.dx = dx;
		this.dy = dy;
	}
// ***************************************************************************
// GET/SET
// ***************************************************************************
	public double getX() { return dx; }
	public double getY() { return dy; }
	public double getAngle() {
		double deg = 0;
		if (getX() == 0) {
			if ( getY() > 0) return 90;
			else return -90;
		}
		else deg = Math.toDegrees(Math.atan(getY()/getX()));
		if (getX() < 0) deg = deg - 180;
		if (deg > 180 && deg > 0) deg = 360-deg;
		else if (deg < -180) deg = 360+deg;
		else if (deg > 360) deg = deg - 360;
		else if (deg < -360) deg = deg + 360;
		return deg;
	}

// ***************************************************************************
// METHODS
// ***************************************************************************

	public void scale(double factor) {
		dx = factor*dx;
		dy = factor*dy;
		didlen = false;
	}
	public void subtract(Vector2d v) {
		dx = dx - v.dx;
		dy = dy - v.dy;
	}
	public void add(Vector2d v) {
		dx = dx + v.dx;
		dy = dy + v.dy;
	}
	/** Make vector shorter by this distance */
	public void subtract(double dist) {
		double len = getLength();
		normalize();
		scale(len - dist);
	}
	/** Make vector longer by this distance */
	public void add(double dist) {
		Vector2d v = new Vector2d(this);
		v.normalize();
		v.scale(dist);
		add(v);
	}
	public Point getPoint(Point a) {
		return new Point((int)(dx + a.getX()), (int)(dy + a.getY()));
	}

	public Point getPoint() {
		return new Point((int)dx, (int)dy);
	}

	public double getLength() {
		if (didlen) return len;

		if (dx == 0 && dy == 0) len = 0;
		else {
		  	len = Math.sqrt(dx*dx + dy*dy);
		  	didlen = true;
		}
		return len;
	}

	public void normalize() {
		if (didnorm) return;
		if (getLength() == 0) {
			return;
		}
	  	dx = dx / getLength();
	  	dy = dy / getLength();
	  	didlen = false;
	}

	public void rotate90() {
		double temp = dx;
		dx = -dy;
		dy = temp;
		didnorm = false;
	}

// ***************************************************************************
// DEBUG
// ***************************************************************************
	public String toString() {
		return "("+(int)dx+", "+dy+")";
	}


}
