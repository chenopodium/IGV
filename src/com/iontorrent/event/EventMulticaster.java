
package com.iontorrent.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;
/**
 * A class which implements efficient and thread-safe multi-cast event
 * dispatching for the events (@see java.awt.AWTEventMulticaster)
 * This class will manage an immutable structure consisting of a chain of
 * event listeners and will dispatch events to those listeners.  Because
 * the structure is immutable, it is safe to use this API to add/remove
 * listeners during the process of an event dispatch operation.
 * @author      Chantal Roth
 */
public class EventMulticaster extends AWTEventMulticaster
	implements SelectionListener{

	protected EventMulticaster(EventListener a, EventListener b) {
		super(a, b);
	}

	
   /**
	 * Handles the selectionPerformed event by invoking the
	 * selectionPerformed methods on listener-a and listener-b.
	 * @param e the selection event
	 */
	public void selectionPerformed(SelectionEvent e) {
		((SelectionListener)a).selectionPerformed(e);
		((SelectionListener)b).selectionPerformed(e);
	}

	
	/** Adds selection-listener-a with selection-listener-b and
	 * returns the resulting multicast listener.
	 * @param a selection-listener-a
	 * @param b selection-listener-b
	 */
	public static SelectionListener add(SelectionListener a, SelectionListener b) {
		return (SelectionListener)addMyInternal(a, b);
	}

	
	/**
	 * Removes the old selection-listener from selection-listener-l and
	 * returns the resulting multicast listener.
	 * @param l selection-listener-l
	 * @param oldl the selection-listener being removed
	 */
	public static SelectionListener remove(SelectionListener l, SelectionListener oldl) {
	return (SelectionListener) removeMyInternal(l, oldl);
	}

	protected static EventListener addMyInternal(EventListener eventlistener, EventListener eventlistener1)
	{
		if(eventlistener == null)
			return eventlistener1;
		if(eventlistener1 == null)
			return eventlistener;
		else
			return new EventMulticaster(eventlistener, eventlistener1);
	}
	protected static EventListener removeMyInternal(EventListener eventlistener, EventListener eventlistener1)
	{
		if(eventlistener == eventlistener1 || eventlistener == null)
			return null;
		if(eventlistener instanceof EventMulticaster)
			return ((EventMulticaster)eventlistener).remove(eventlistener1);
		else
			return eventlistener;
	}

}
