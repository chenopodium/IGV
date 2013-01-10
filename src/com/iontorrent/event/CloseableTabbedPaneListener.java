package com.iontorrent.event;

import java.util.EventListener;

public interface CloseableTabbedPaneListener extends EventListener {
    /**
     * Informs all <code>CloseableTabbedPaneListener</code> s when a tab
     * should be closed
     * 
     * @param tabIndexToClose
     *            the index of the tab which should be closed
     * @return true if the tab can be closed, false otherwise
     */
    public boolean closeTab(int tabIndexToClose);
}