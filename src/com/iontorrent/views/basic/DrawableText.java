package com.iontorrent.views.basic;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

public class DrawableText extends Text {


// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************
	public DrawableText(String text, int x, int y, Color color) {
		this(text, x, y, color, null, null);
	}

	public DrawableText(String text, int x, int y) {
		this(text, x, y, null, null, null);
	}

	public DrawableText(String text, int x, int y, Color color, Font font, Drawable d) {
		super(text, x, y, color, font);
		setDrawable(d);

	}

// ***************************************************************************
// GET/SET
// ***************************************************************************
	public boolean isSelectable() { return true; }

	public void setDrawable(Drawable d){
		super.setDrawable(d);
		
	}

// ***************************************************************************
// SELECTION
// ***************************************************************************


}
