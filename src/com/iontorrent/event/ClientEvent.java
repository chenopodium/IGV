package com.iontorrent.event;

import java.awt.event.ActionEvent;

public class ClientEvent extends ActionEvent {
	
	Object target = null;
	 
	/** @param source the component who created the event
	 *  @param target the object to be saved
	 */
	public ClientEvent(Object source, Object target) {
		this(source, target, 0,  "");
	}
	 
	 /** @param source the component who created the event
	 *  Here the object to be saved is equal to the source
	 */
	public ClientEvent(Object source) {
		this(source, source, 0, "");
	}
	
	/** @param source the component who created the event
	 *  @param target the object on which the event is performed, e.g.
	 *   an object to be saved, selected, help requested on, to be deleted, etc.
	 *  @param command any string that specifies the save event more
	 */
	public ClientEvent(Object source, Object target, String command) {
	 	this(source, target, 0, command);
	}	
	
	/** @param source the component who created the event
	 *  @param target the object on which the event is performed, e.g.
	 *   an object to be saved, selected, help requested on, to be deleted, etc.
	 *  @ param and id that identifies the event
	 *  @param command any string that specifies the save event more
	 */
	public ClientEvent(Object source, Object target, int id, String command) {
	 	super(source, 0, command);
	 	this.target = target;
	 	
	}	
	/** return the object upon which an action might be performed, such
	as an object to be saved, selected etc */
	public Object getTarget() {
		return target;
	}
	
}