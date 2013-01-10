package com.iontorrent.views.basic;

import java.awt.Color;
import java.awt.Font;


public class StaticText extends Text{
	
// ***************************************************************************
// FROM DRAWABLE 
// ***************************************************************************
	public StaticText(String text, int x, int y, Color color) {
		super(text, x, y, color, null);
	}
	
	public StaticText(String text, int x, int y) {
		super(text, x, y, null, null);
	}
	
	public StaticText(String text, int x, int y, Color color, Font font) {
		super(text, x, y, color, font);
	}
	public boolean isStatic() { return true; }
}
