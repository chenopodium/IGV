package com.iontorrent.views.basic;

import com.iontorrent.event.CloseableTabbedPaneListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventListener;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;



/**
 * A JTabbedPane which has a close ('X') icon on each tab.
 * 
 * To add a tab, use the method addTab(String, Component)
 * 
 * To have an extra icon on each tab, in addition to the close icon,
 * use the method addTab(String, Component, Icon). Only clicking the 'X'
 * closes the tab. <p/>
 * 
 * dguist: Removed custom UI implementation - not sure of the implications of
 * this, may affect use with scrolling layout policy, but using it requires
 * either subclassing every specific L&F UI that may be used or having tabs that
 * potentially don't conform to the current L&F.
 * 
 * @author fast_ of Java Developer Forums, post dated Nov 24, 2004 7:44 AM
 * @author dguist
 * @see <a
 *      href="http://forum.java.sun.com/thread.jspa?threadID=337070&tstart=165">
 *      Forum Discussion </a>
 */
public class CloseableTabbedPane extends JTabbedPane {

    public static final String CLOSEABLE_PROPERTY_KEY = "isCloseable";
    /**
     * The <code>EventListenerList</code>.
     */
    private EventListenerList listenerList = null;

    /**
     * The viewport of the scrolled tabs.
     */
    private JViewport headerViewport = null;

    /**
     * The normal closeicon.
     */
    private Icon normalCloseIcon = null;

    /**
     * The closeicon when the mouse is over.
     */
    private Icon hooverCloseIcon = null;

    /**
     * The closeicon when the mouse is pressed.
     */
    private Icon pressedCloseIcon = null;

    /**
     * Creates a new instance of <code>CloseableTabbedPane</code>
     */
    public CloseableTabbedPane() {
        listenerList = new EventListenerList();
        setupMouseListener();
    }

    /**
     * Allows setting own closeicons.
     * 
     * @param normal
     *            the normal closeicon
     * @param hoover
     *            the closeicon when the mouse is over
     * @param pressed
     *            the closeicon when the mouse is pressed
     */
    public void setCloseIcons(Icon normal, Icon hoover, Icon pressed) {
        normalCloseIcon = normal;
        hooverCloseIcon = hoover;
        pressedCloseIcon = pressed;
    }

    /**
     * Adds a <code>Component</code> represented by a title and no icon.
     * Whether or not the given component is displayed with a closeable icon is
     * determined by the presence and value of the <code>isCloseable</code>
     * client property. Painting the closeable icon is true by default so in
     * order to suppress this behavior, the component must be an instance of
     * JComponent and must contain a <code>Boolean</code> with the false value
     * as the value of the property obtained via
     * <code>((JComponent)component).getClientProperty("isCloseable")</code>.
     * 
     * @param title
     *            the title to be displayed in this tab
     * @param component
     *            the component to be displayed when this tab is clicked
     */
    public void addTab(String title, Component component) {
        addTab(title, null, component);
    }

    /**
     * Adds a <code>Component</code> represented by a title and an icon.
     * Whether or not the given component is displayed with a closeable icon is
     * determined by the presence and value of the <code>isCloseable</code>
     * client property. Painting the closeable icon is enabled by default so in
     * order to suppress this behavior, the component must be an instance of
     * JComponent and must contain a <code>Boolean</code> with the false value
     * as the value of the property obtained via
     * <code>((JComponent)component).getClientProperty("isCloseable")</code>.
     * 
     * @param title
     *            the title to be displayed in this tab
     * @param component
     *            the component to be displayed when this tab is clicked
     * @param extraIcon
     *            the icon to be displayed in this tab
     */
    public void addTab(String title, Icon extraIcon, Component component) {
        boolean doPaintCloseIcon = true;
        try {
            Object prop = null;
            if ((prop = ((JComponent) component)
                    .getClientProperty(CLOSEABLE_PROPERTY_KEY)) != null) {
                doPaintCloseIcon = ((Boolean) prop).booleanValue();
            }
        } catch (Exception ignored) {/* Could be a ClassCastException */
        }
        component.addPropertyChangeListener("isClosable",
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent e) {
                    Object newVal = e.getNewValue();
                    int index = -1;
                    if (e.getSource() instanceof Component) {
                        index = indexOfComponent((Component) e.getSource());
                    }
                    if (index != -1 && newVal != null && newVal instanceof Boolean) {
                        setCloseIconVisibleAt(index, ((Boolean)newVal).booleanValue());
                    }
                }
            });

        super.addTab(title, doPaintCloseIcon ? new CloseTabIcon(extraIcon)
                : extraIcon, component);
        if (doPaintCloseIcon && headerViewport == null) {
            Component[] components = getComponents();
            for (int i = 0; i < components.length; i++) {
                Component c = components[i];
                if ("TabbedPane.scrollableViewport".equals(c.getName()))
                    headerViewport = (JViewport) c;
            }
        }
    }
    
    /**
     * Sets the closeicon at <code>index</code>.
     * @param index the tab index where the icon should be set
     * @param icon the icon to be displayed in the tab
     * @throws IndexOutOfBoundsException if index is out of range (index < 0 ||
     * index >= tab count)
     */
    private void setCloseIconVisibleAt(int index, boolean closeIconVisible)
      throws IndexOutOfBoundsException {
        Icon currentIcon = getIconAt(index);
        super.setIconAt(index, closeIconVisible ? new CloseTabIcon(currentIcon) : currentIcon);
    }
    
    /**
     * Return the CloseTabIcon at the given index.  If the given index
     * contains no icon or some other type of icon, return null.
     */
    private CloseTabIcon getCloseIconAt(int index) {
        Icon icon = getIconAt(index);
        if (icon instanceof CloseTabIcon) {
            return (CloseTabIcon)icon;
        }
        return null;
    }


    private void setupMouseListener() {
        MouseInputAdapter listener = new MouseInputAdapter() {

            public void mouseClicked(MouseEvent e) {
                processMouseEvents(e);
            }

            /**
             * Invoked when a mouse button is pressed on a component and then
             * dragged. <code>MOUSE_DRAGGED</code> events will continue to be
             * delivered to the component where the drag originated until the
             * mouse button is released (regardless of whether the mouse
             * position is within the bounds of the component). <br/><br/>Due
             * to platform-dependent Drag&Drop implementations,
             * <code>MOUSE_DRAGGED</code> events may not be delivered during a
             * native Drag&amp;Drop operation.
             * 
             * @param e
             *            the <code>MouseEvent</code>
             */
            public void mouseDragged(MouseEvent e) {
                processMouseEvents(e);
            }

            public void mouseExited(MouseEvent e) {
                for (int i = 0; i < getTabCount(); i++) {
                    CloseTabIcon icon = getCloseIconAt(i);
                    if (icon != null)                        
                        icon.mouseover = false;
                }
                repaint();
            }

            public void mouseMoved(MouseEvent e) {
                processMouseEvents(e);

            }

            public void mousePressed(MouseEvent e) {
                processMouseEvents(e);
            }
        };
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    /**
     * Processes all caught <code>MouseEvent</code>s.
     * 
     * @param e
     *            the <code>MouseEvent</code>
     */
    private void processMouseEvents(MouseEvent e) {
        int tabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());
        if (tabNumber < 0)
            return;
        boolean otherWasOver = false;
        for (int i=0; i<getTabCount(); i++) {
          if (i != tabNumber) {
            CloseTabIcon ic = getCloseIconAt(i);
            if (ic != null) {
              if (ic.mouseover)
                otherWasOver = true;
              ic.mouseover = false;
            }
          }
        }
        if (otherWasOver)
          repaint();
        CloseTabIcon closeIcon = getCloseIconAt(tabNumber);
        if(closeIcon == null)
            return;
        Rectangle rect = closeIcon.getBounds();
        boolean vpIsNull = headerViewport == null;
        Point pos = vpIsNull ? new Point() : headerViewport.getViewPosition();
        int vpDiffX = (vpIsNull ? 0 : headerViewport.getX());
        int vpDiffY = (vpIsNull ? 0 : headerViewport.getY());
        Rectangle drawRect = new Rectangle(rect.x - pos.x + vpDiffX,
          rect.y - pos.y + vpDiffY, rect.width, rect.height);

        if (e.getID() == MouseEvent.MOUSE_PRESSED) {
            closeIcon.mousepressed = e.getModifiers() == MouseEvent.BUTTON1_MASK;
            repaint(drawRect);
            return;
        }
        if (!(e.getID() == MouseEvent.MOUSE_MOVED ||
                e.getID() == MouseEvent.MOUSE_DRAGGED ||
                e.getID() == MouseEvent.MOUSE_CLICKED)) {
            return;
        }
        pos.x += e.getX() - vpDiffX;
        pos.y += e.getY() - vpDiffY;
        if (rect.contains(pos)) {
            if (e.getID() == MouseEvent.MOUSE_CLICKED) {
                int selIndex = getSelectedIndex();
                if (fireCloseTab(selIndex)) {
                    if (selIndex > 0) {
                        // to prevent uncatchable null-pointers
                        Rectangle rec = getUI()
                                .getTabBounds(this, selIndex - 1);
                        MouseEvent event = new MouseEvent((Component) e
                                .getSource(), e.getID() + 1, System
                                .currentTimeMillis(), e.getModifiers(), rec.x,
                                rec.y, e.getClickCount(), e.isPopupTrigger(), e
                                        .getButton());
                        dispatchEvent(event);
                    }
                    //the tab is being closed
                    //removeTabAt(tabNumber);
                    remove(selIndex);
                } else {
                    closeIcon.mouseover = false;
                    closeIcon.mousepressed = false;
                    repaint(drawRect);
                }
            } else {
                closeIcon.mouseover = true;
                closeIcon.mousepressed = e.getModifiers() == MouseEvent.BUTTON1_MASK;
            }
        } else {
            closeIcon.mouseover = false;
        }
        repaint(drawRect);
    }

    /**
     * Adds an <code>CloseableTabbedPaneListener</code> to the tabbedpane.
     * 
     * @param l
     *            the <code>CloseableTabbedPaneListener</code> to be added
     */
    public void addCloseableTabbedPaneListener(CloseableTabbedPaneListener l) {
        listenerList.add(CloseableTabbedPaneListener.class, l);
    }

    /**
     * Removes an <code>CloseableTabbedPaneListener</code> from the
     * tabbedpane.
     * 
     * @param l
     *            the listener to be removed
     */
    public void removeCloseableTabbedPaneListener(CloseableTabbedPaneListener l) {
        listenerList.remove(CloseableTabbedPaneListener.class, l);
    }

    /**
     * Notifies all listeners that have registered interest for notification on
     * this event type.
     * 
     * @param tabIndexToClose
     *            the index of the tab which should be closed
     * @return true if the tab can be closed, false otherwise
     */
    protected boolean fireCloseTab(int tabIndexToClose) {
        boolean closeit = true;
        EventListener[] listeners = 
            listenerList.getListeners(CloseableTabbedPaneListener.class);
        for (int i = 0; i < listeners.length; i++) {
            CloseableTabbedPaneListener listener = 
                (CloseableTabbedPaneListener)listeners[i];
            if (!((CloseableTabbedPaneListener)listener)
                    .closeTab(tabIndexToClose)) {
                closeit = false;
                break;
            }
        }
        return closeit;
    }

    /**
     * The class which generates the 'X' icon for the tabs. The constructor
     * accepts an icon which is extra to the 'X' icon, so you can have tabs like
     * in JBuilder. This value is null if no extra icon is required.
     */
    class CloseTabIcon implements Icon {
        /**
         * the x position of the icon
         */
        private int x_pos;

        /**
         * the y position of the icon
         */
        private int y_pos;

        /**
         * the width the icon
         */
        private int width;

        /**
         * the height the icon
         */
        private int height;

        /**
         * the additional fileicon
         */
        private Icon fileIcon;

        /**
         * true whether the mouse is over this icon, false otherwise
         */
        private boolean mouseover = false;

        /**
         * true whether the mouse is pressed on this icon, false otherwise
         */
        private boolean mousepressed = false;

        /**
         * Creates a new instance of <code>CloseTabIcon</code>
         * 
         * @param fileIcon
         *            the additional fileicon, if there is one set
         */
        public CloseTabIcon(Icon fileIcon) {
            this.fileIcon = fileIcon;
            width = 16;
            height = 16;
        }

        /**
         * Draw the icon at the specified location. Icon implementations may use
         * the Component argument to get properties useful for painting, e.g.
         * the foreground or background color.
         * 
         * @param c
         *            the component which the icon belongs to
         * @param g
         *            the graphic object to draw on
         * @param x
         *            the upper left point of the icon in the x direction
         * @param y
         *            the upper left point of the icon in the y direction
         */
        public void paintIcon(Component c, Graphics g, int x, int y) {
            boolean doPaintCloseIcon = true;
            try {
                // JComponent.putClientProperty("isCloseable", new
                // Boolean(false));
                JTabbedPane tabbedpane = (JTabbedPane) c;
                int tabNumber = tabbedpane.getUI().tabForCoordinate(tabbedpane,
                        x, y);
                JComponent curPanel = (JComponent) tabbedpane
                        .getComponentAt(tabNumber);
                Object prop = null;
                if ((prop = curPanel.getClientProperty(CLOSEABLE_PROPERTY_KEY)) != null) {
                    doPaintCloseIcon = ((Boolean) prop).booleanValue();
                }
            } catch (Exception ignored) {/*
                                          * Could probably be a
                                          * ClassCastException
                                          */
            }
            if (doPaintCloseIcon) {
                x_pos = x;
                y_pos = y;
                int y_p = y + 1;

                if (normalCloseIcon != null && !mouseover) {
                    normalCloseIcon.paintIcon(c, g, x, y_p);
                } else if (hooverCloseIcon != null && mouseover
                        && !mousepressed) {
                    hooverCloseIcon.paintIcon(c, g, x, y_p);
                } else if (pressedCloseIcon != null && mousepressed) {
                    pressedCloseIcon.paintIcon(c, g, x, y_p);
                } else {
                    y_p++;

                    Color col = g.getColor();

                    if (mousepressed && mouseover) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x + 1, y_p, 12, 13);
                    }

                    g.setColor(Color.black);
                    g.drawLine(x + 1, y_p, x + 12, y_p);
                    g.drawLine(x + 1, y_p + 13, x + 12, y_p + 13);
                    g.drawLine(x, y_p + 1, x, y_p + 12);
                    g.drawLine(x + 13, y_p + 1, x + 13, y_p + 12);
                    g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
                    if (mouseover)
                        g.setColor(Color.GRAY);
                    g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
                    g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
                    g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
                    g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
                    g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);
                    g.setColor(col);
                    if (fileIcon != null) {
                        fileIcon.paintIcon(c, g, x + width, y_p);
                    }
                }
            }
        }

        /**
         * Returns the icon's width.
         * 
         * @return an int specifying the fixed width of the icon.
         */
        public int getIconWidth() {
            return width + (fileIcon != null ? fileIcon.getIconWidth() : 0);
        }

        /**
         * Returns the icon's height.
         * 
         * @return an int specifying the fixed height of the icon.
         */
        public int getIconHeight() {
            return height;
        }

        /**
         * Gets the bounds of this icon in the form of a <code>Rectangle<code>
         * object. The bounds specify this icon's width, height, and location
         * relative to its parent.
         * @return a rectangle indicating this icon's bounds
         */
        public Rectangle getBounds() {
            return new Rectangle(x_pos, y_pos, width, height);
        }
    }

    //    /**
    //     * A specific <code>BasicTabbedPaneUI</code>.
    //     */
    //    class CloseableTabbedPaneUI extends BasicTabbedPaneUI {
    //
    //        /**
    //         * the horizontal position of the text
    //         */
    //        private int horizontalTextPosition = SwingUtilities.LEFT;
    //
    //        /**
    //         * Creates a new instance of <code>CloseableTabbedPaneUI</code>
    //         */
    //        public CloseableTabbedPaneUI() {
    //        }
    //
    //        /**
    //         * Creates a new instance of <code>CloseableTabbedPaneUI</code>
    //         *
    //         * @param horizontalTextPosition
    //         * the horizontal position of the text (e.g.
    //         * SwingUtilities.TRAILING or SwingUtilities.LEFT)
    //         */
    //        public CloseableTabbedPaneUI(int horizontalTextPosition) {
    //            this.horizontalTextPosition = horizontalTextPosition;
    //        }
    //
    //        /**
    //         * Layouts the label
    //         *
    //         * @param tabPlacement
    //         * the placement of the tabs
    //         * @param metrics
    //         * the font metrics
    //         * @param tabIndex
    //         * the index of the tab
    //         * @param title
    //         * the title of the tab
    //         * @param icon
    //         * the icon of the tab
    //         * @param tabRect
    //         * the tab boundaries
    //         * @param iconRect
    //         * the icon boundaries
    //         * @param textRect
    //         * the text boundaries
    //         * @param isSelected
    //         * true whether the tab is selected, false otherwise
    //         */
    //        protected void layoutLabel(int tabPlacement, FontMetrics metrics,
    //                int tabIndex, String title, Icon icon, Rectangle tabRect,
    //                Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    //
    //            textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
    //
    //            javax.swing.text.View v = getTextViewForTab(tabIndex);
    //            if (v != null) {
    //                tabPane.putClientProperty("html", v);
    //            }
    //
    //            SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
    //                    title, icon, SwingUtilities.CENTER, SwingUtilities.CENTER,
    //                    SwingUtilities.CENTER,
    //                    //SwingUtilities.TRAILING,
    //                    horizontalTextPosition, tabRect, iconRect, textRect,
    //                    textIconGap + 2);
    //
    //            tabPane.putClientProperty("html", null);
    //
    //            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    //            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    //            iconRect.x += xNudge;
    //            iconRect.y += yNudge;
    //            textRect.x += xNudge;
    //            textRect.y += yNudge;
    //        }
    //    }
    //
    //    /**
    //     * A specific <code>MetalTabbedPaneUI</code>.
    //     */
    //    class CloseableMetalTabbedPaneUI extends MetalTabbedPaneUI {
    //
    //        /**
    //         * the horizontal position of the text
    //         */
    //        private int horizontalTextPosition = SwingUtilities.LEFT;
    //
    //        /**
    //         * Creates a new instance of <code>CloseableMetalTabbedPaneUI</code>
    //         */
    //        public CloseableMetalTabbedPaneUI() {
    //        }
    //
    //        /**
    //         * Creates a new instance of <code>CloseableMetalTabbedPaneUI</code>
    //         *
    //         * @param horizontalTextPosition
    //         * the horizontal position of the text (e.g.
    //         * SwingUtilities.TRAILING or SwingUtilities.LEFT)
    //         */
    //        public CloseableMetalTabbedPaneUI(int horizontalTextPosition) {
    //            this.horizontalTextPosition = horizontalTextPosition;
    //        }
    //
    //        /**
    //         * Layouts the label
    //         *
    //         * @param tabPlacement
    //         * the placement of the tabs
    //         * @param metrics
    //         * the font metrics
    //         * @param tabIndex
    //         * the index of the tab
    //         * @param title
    //         * the title of the tab
    //         * @param icon
    //         * the icon of the tab
    //         * @param tabRect
    //         * the tab boundaries
    //         * @param iconRect
    //         * the icon boundaries
    //         * @param textRect
    //         * the text boundaries
    //         * @param isSelected
    //         * true whether the tab is selected, false otherwise
    //         */
    //        protected void layoutLabel(int tabPlacement, FontMetrics metrics,
    //                int tabIndex, String title, Icon icon, Rectangle tabRect,
    //                Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    //
    //            textRect.x = textRect.y = iconRect.x = iconRect.y = 0;
    //
    //            javax.swing.text.View v = getTextViewForTab(tabIndex);
    //            if (v != null) {
    //                tabPane.putClientProperty("html", v);
    //            }
    //
    //            SwingUtilities.layoutCompoundLabel((JComponent) tabPane, metrics,
    //                    title, icon, SwingUtilities.CENTER, SwingUtilities.CENTER,
    //                    SwingUtilities.CENTER,
    //                    //SwingUtilities.TRAILING,
    //                    horizontalTextPosition, tabRect, iconRect, textRect,
    //                    textIconGap + 2);
    //
    //            tabPane.putClientProperty("html", null);
    //
    //            int xNudge = getTabLabelShiftX(tabPlacement, tabIndex, isSelected);
    //            int yNudge = getTabLabelShiftY(tabPlacement, tabIndex, isSelected);
    //            iconRect.x += xNudge;
    //            iconRect.y += yNudge;
    //            textRect.x += xNudge;
    //            textRect.y += yNudge;
    //        }
    //    }
}

