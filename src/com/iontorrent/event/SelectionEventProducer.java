package com.iontorrent.event;


/**

 * @see lj.client.events.EventProducer
 * Classes that produce SelectionEvents  and want to allow other components
 * to listen for events must implement this interface
  * @Author Chantal Roth
 */
public interface SelectionEventProducer extends EventProducer{
	 
	/** @param listener A listener that likes to register itself
	 * to receive selection events (selectionPerformed)
	 */ 
	public void addSelectionListener(SelectionListener listener);
	
	public void removeSelectionListener(SelectionListener listener);
}