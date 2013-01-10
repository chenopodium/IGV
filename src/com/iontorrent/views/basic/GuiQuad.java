
package  com.iontorrent.views.basic;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Vector;

 
/**
 * @author:  Chantal Roth
 */
public class GuiQuad extends GuiObject {
	private QuadNode quad;
	private Vector quads;
	
	public GuiQuad (QuadNode quad) {
		this.quad = quad;
	}
	public String getName () {
		return  quad.getName();
	}

	public void draw (Graphics g) {
		g.setColor(Color.black);
		quads = quad.getAllQuads();
		for (int i = 0; quads != null && i < quads.size(); i++) {
			QuadNode q= (QuadNode)quads.get(i);
			g.drawRect(q.getX(), q.getY(), q.getWidth(), q.getHeight());
			g.drawString(q.toString(), q.getX(), q.getY()+15);
		}
	}

}



