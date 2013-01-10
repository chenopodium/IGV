/*
*	Copyright (C) 2011 Life Technologies Inc.
*
*   This program is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 2 of the License, or
*   (at your option) any later version.
*
*   This program is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with this program.  If not, see <http://www.gnu.org/licenses/>.
* 
*  @Author Chantal Roth
*/
package com.iontorrent.views.basic;

import com.iontorrent.event.ActionContext;
import com.iontorrent.event.BasicAction;
import com.iontorrent.event.ContextActionHandlerIF;
import com.iontorrent.event.SelectionEvent;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.*;


/**
 * @Author Chantal Roth
 */
public class BasicTabView
    extends JPanel {
    private boolean active = false;
    private Vector actions;
    protected String title = "View";
    private int popup_x = 0;
    private int popup_y = 0;
    protected JPopupMenu popup = null;
    private ContextActionHandlerIF handler = null;
//	protected CloseAction closeAction;
	private ImageIcon icon;

    /**
     * Create a new tab with the given title and no view that is not closeable.
     */
    public BasicTabView(String title) {
        
        this.title = title;
        setCloseable(false);
    }
    
    /**
     * Create a new tab displaying the given view in the center of a
     * BorderLayout that is not closeable.
     * @param title the tab title
     * @param view the view to display
     */
    public BasicTabView(String title, JComponent view) {
        this(title);
        buildGui(view);
    }

    /**
     * Subclasses can override this empty implementation to perform
     * initialization when ever this tab is selected.
     */
    public void init() {
        // no-op
    }

// *****************************************************************
// GET/SET
// *****************************************************************
    public void setContextActionHandler(ContextActionHandlerIF handler) {
        this.handler = handler;
    }

    public ContextActionHandlerIF getContextActionHandler() {
        return handler;
    }

    public int getPopupX() {
        return popup_x;
    }

    public int getPopupY() {
        return popup_y;
    }

   public void setPopup(JPopupMenu popup) {
       this.popup = popup;
    }

    public void addAction(BasicAction action) {
        if (actions == null)
            actions = new Vector();
        actions.add(action);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    /**
     * Return true of this tab is closeable, false if it is not.  Tabs
     * are not closeable by default.  A closeable tab is displayed with an "X"
     * icon that can be clicked to close the tab.
     */
    public boolean isCloseable() {
        Boolean closeable = (Boolean)getClientProperty(
                CloseableTabbedPane.CLOSEABLE_PROPERTY_KEY);
        return closeable == null || closeable.booleanValue();
    }
    
    /**
     * Set whether this tab should be closeable.  Tabs are not closeable by
     * default.  A closeable tab is displayed with an "X" icon that can be
     * clicked to close the tab.
     * @param closeable if true, this tab is closeable.  Otherwise, it is not.
     */
    public void setCloseable(boolean closeable) {
        putClientProperty(
                CloseableTabbedPane.CLOSEABLE_PROPERTY_KEY,
                new Boolean(closeable));
    }

    public String getTitle() {
        return title;
    }

// *****************************************************************
// TOOLTIP TEXT
// *****************************************************************

    public String getToolTipText(MouseEvent event) {
        JComponent comp = (JComponent) getComponentAt(event.getPoint());
        String result;

        if (comp == null) {
        //    System.out.println("BasicView getToolTipText");
            result = super.getToolTipText(event);
        }
        else {
       //     System.out.println(comp.getClass().getName() + " getToolTipText");
            MouseEvent compEvent = SwingUtilities.convertMouseEvent(this, event,
                comp);
            result = ";-)";
//            result = comp.getToolTipText(compEvent);
            
        }
        return result;
    }

// *****************************************************************
// POPUP MENU
// *****************************************************************

    public class PopupListener
        extends MouseAdapter {
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
            requestFocus();
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            JPopupMenu totalpopup = null;
            if (e.isPopupTrigger() && (handler != null || actions != null)) {
                totalpopup = new JPopupMenu("totalactions");
                int total = 0;
                for (int i = 0; actions != null && i < actions.size(); i++) {
                    totalpopup.add( (Action) actions.get(i));
                    total++;
                }

                if (handler != null) {
                    ActionContext ctx = new ActionContext(e.getX(), e.getY());
                    ArrayList<Action> moreactions = handler.getContextSensitiveActions(ctx);
                    if (moreactions != null && moreactions.size() > 0) {
                             totalpopup.addSeparator();
                    }
                    for (int i = 0; moreactions != null && i < moreactions.size();  i++) {
                        BasicAction a = (BasicAction) moreactions.get(i);
                        totalpopup.add( a);
                        total++;
                    }
                }

               if (total >0) {
                   totalpopup.show(e.getComponent(),
                                   e.getX(), e.getY());
                   popup_x = e.getX();
                   popup_y = e.getY();
                   
               }

            }
        }
    }
//	protected class CloseAction
//			extends BasicAction {
//		   public CloseAction() {
//                       
//			   super("close view", new ImageIcon(BasicTabView.class.getResource("delete.png")));
//			   this.setToolTipText("Close this view");
//		   }
//
//		   public void actionPerformed(ActionEvent e) {
//			   SelectionEvent sel = new SelectionEvent(BasicTabView.this,
//			BasicTabView.this, "close", null);
//			 //  notifySelectionListeners(sel);
//		   }
//	   }

// *****************************************************************
// ABSTRACT
// *****************************************************************

    public Object clone() {
        return this;
    }

	public int getPopup_x() {
		return popup_x;
	}

	public int getPopup_y() {
		return popup_y;
	}

    /**
     * Add the given view to the center of a BorderLayout.
     */
    private void buildGui(JComponent view) {
        setLayout(new BorderLayout());
        add(view, BorderLayout.CENTER);
    }

    public void setIcon(ImageIcon icon) {
       this.icon = icon;
        
    }

    public ImageIcon getIcon() {
       
        return icon;
    }
}