package com.iontorrent.event;

/**
 
 * Any class that implements this interface must implement one method, registerEvents,
 * which registers this class at the CommunicationController. The CommunicationController keeps track of incoming
 * events and passes them on to any listeners. This enables communication between
 * different (yet unknown) components.
 * @Author Chantal Roth
 */
public interface EventProducer{
	 
	/** 
	 * This method should register the class at the CommunicationController as event
	 * producer, in the form:
	 * CommunicationController.registerActionEvents(this);
	 */ 
	public void registerEvents();
	
}