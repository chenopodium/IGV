package com.iontorrent.views.basic;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class AbstractCoord implements CoordIF {
	
	
	protected Color color_fore = Color.blue;
	protected Color color_back = Color.white;
	protected GuiCanvas canvas;
	
	public AbstractCoord(GuiCanvas canvas) {
		this.canvas = canvas;
	}
	
// ***************************************************************************
// FROM COORDDIF 
// ***************************************************************************
	
	public abstract void draw(Graphics2D g);
	 	
}
