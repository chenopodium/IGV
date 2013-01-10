/*
 * Created on Apr 20, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.iontorrent.views.basic;
import java.util.Comparator;

public class LayerComparator implements Comparator{

	public int compare(Object o1, Object o2) {
		if (!(o1 instanceof GuiObject ) || !(o2 instanceof GuiObject )) return 0;
		GuiObject g1= (GuiObject)o1;
		GuiObject g2= (GuiObject )o2;
		if (g1 == null || g2 == null) return 0;
		return (int)(g2.getLayer() - g1.getLayer());
	}

}






