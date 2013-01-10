package com.iontorrent.event;

import java.awt.Point;
import java.awt.Rectangle;

public class SelectionEvent extends ClientEvent {
	 
	private Rectangle rect;
        private Point point;

	/** @param source component that created the event, e.g. a viewer
	 *  This constructor can be used if the selectee is also the source
	 */ 
	public SelectionEvent(Object source, Object selectee, Point point) {
		super(source, selectee);
                this.point = point;
	}
	public int getX() {
            if (point != null) return point.x;
            else return 0;
                    
        }
        public int getY() {
            if (point != null) return point.y;
            else return 0;
        }
	/** @param source component that created the event, e.g. a viewer
	 *  @param selectee the object that was selected, e.g. a drawable
	 *  @msg any message that should accompany the event
	 */
	public SelectionEvent(Object source, Point point) {
		super(source);
                this.point = point;
	}
	
		 
	/** @param source component that created the event, e.g. a viewer
	 *  @param selectee the object that was selected, e.g. a drawable
	 *  @msg any message that should accompany the event
	 */ 
	public SelectionEvent(Object source, Object selectee, String command,  Point point) {
	 	super(source, selectee, command);
                this.point = point;
	}

	public void setArea(Rectangle rect) {
		this.rect = rect;
		
	}
	public Rectangle getArea() {
		return rect;
	}
	
}