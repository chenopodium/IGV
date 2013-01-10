package com.iontorrent.event;

import java.awt.event.ActionListener;

/**
 
 * Classes that produce ActionEvents  and want to allow other components
 * to listen for events must implement this interface
 * @Author Chantal Roth
 * @see lj.client.events.EventProducer
 */
public interface ActionEventProducer extends EventProducer {
	 
	/** @param listener A listener that likes to register itself
	 * to receive action events (actionPerformed)
	 */ 
	public void addActionListener(ActionListener listener);
	
	public void removeActionListener(ActionListener listener);
	
	
}