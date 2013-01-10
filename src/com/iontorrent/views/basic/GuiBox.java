package com.iontorrent.views.basic;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

public class GuiBox extends Text {
	private static final int LEN = 12;
	private GuiObject select;
	ZoomCanvas canvas;
// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************
	public GuiBox(String text, GuiObject select, int x, int y, ZoomCanvas canvas) {
		super(text, x, y);
		this.select = select;
		setLayer(10);
		this.canvas=canvas;
	}

// ***************************************************************************
// GET/SET
// ***************************************************************************
// ***************************************************************************
// SELECTION
// ***************************************************************************
	public void setSelected(boolean b) {
		//super.setSelected(b);
		//p("ignoring SELECT "+b);
		//select.setSelected(b);
	}
	
	public void setSelected(boolean b, boolean event) {
		//p("ignoring SELECT "+b);
		//super.super.setSelected(b, event);
	}
	public void mouseClicked(MouseEvent e) {
		//p("MOUSE: SELECTED "+selected+" from "+e.getComponent());
		
		this.selected = !selected;
		p("+++++ setting select of "+select+" to "+selected);
		select.setSelected(selected);
		//draw(canvas.getGraphics());
		canvas.repaint();
	}
	public Drawable getDrawable() {
		return select;
	}
// ***************************************************************************
// DRAW
// ***************************************************************************

   public void calcFontMetrics(Graphics g) {
	   FontMetrics fm = g.getFontMetrics(font);
	   int font_widths[] = fm.getWidths();
	   font_width = -1;
	   for(int i = 0; i < font_widths.length; i++) {
		   if(font_widths[i] > font_width) font_width = font_widths[i];
	   }
	   font_height = fm.getHeight()*3/4;
	   font_width = (int)(font_width*text.length()*0.6)+15;
	   font_base_line = fm.getMaxAscent();

	   setAbsoluteSize(new Dimension(font_width, font_height));
	  // setAbsolutePosition(new Point(x, y-font_height));
   }
   public void draw(Graphics g2) {
		clear(g2);
		if (select != null) {
			this.setSelected(select.isSelected(), false);
		}
		int dy = 0;
		g2.setColor(color);
		if (isSelected()) {
			g2.setColor(Color.red);
			g2.setFont(selected_font);
		}
		else g2.setFont(font);
		g2.drawString(text, x+LEN+3, y-dy+LEN-3);
		
		int bx = x;
		int by = y-dy;
		g2.setColor(Color.gray);
		g2.drawLine(bx, by, bx+LEN, by);
		g2.drawLine(bx, by, bx, by+LEN);
		g2.setColor(Color.darkGray);
		bx++; by++;
		g2.drawLine(bx, by, bx+LEN-2, by);
		g2.drawLine(bx, by, bx, by+LEN-2);
		bx+=LEN-1; by+=LEN-1;
		g2.setColor(Color.white);
		g2.drawLine(bx, by, bx-LEN-1, by);
		g2.drawLine(bx, by, bx, by-LEN-1);
		g2.setColor(Color.lightGray);
		bx--; by--;
		g2.drawLine(bx, by, bx-LEN+1, by);
		g2.drawLine(bx, by, bx, by-LEN+1);
		if (isSelected()) {
			bx = x+2;
			by = y-dy+5;
			g2.setColor(Color.black);
			g2.drawLine(bx, by, bx+2, by+2);
			bx++; by++;
			g2.drawLine(bx, by, bx+2, by+2);
			bx+=2;by+=2;
			g2.drawLine(bx, by, bx+4, by-4);
			bx++; by--;
			g2.drawLine(bx, by, bx+4, by-4);
		}
	//	if (Environment.isTesting()) drawBounds(g2);
	}
	public Rectangle getBounds() {
		return new Rectangle(x, y, font_width, font_height*2);
	}
	public String getDescription(String nl) {
		String s = text;
		if (select != null) {
			String tmp = select.getDescription();
			if (tmp!=null && tmp.length()>0 ) s+=nl+tmp;
		}
		return s;
	}
		public String getDescription() {
			return getDescription("\n");
		}
		public String toHtml() {
			return getDescription("<BR>");
		}
// ***************************************************************************
// OTHER
// ***************************************************************************
	
	protected void p(String s) {
		Logger.getLogger("GuiBox").info("GuiBox:"+s);
		}

}
