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

import com.iontorrent.event.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;
import javax.swing.Action;

/**
 * This is a general class that represents any GuiObject. The class knows how to
 * handle zooming, it notifies action listeners if a selection event occurred,
 * and handles simple selection. For any concrete drawable object, such as a
 * rectangle, a feature, a dog, whatever, extend this class and implement the
 * draw() and cleear() methods.
 *
 * @Author Chantal Roth
 */
public abstract class GuiObject implements Drawable, SelectionEventProducer, MouseListener, MouseMotionListener {

    private static boolean DEBUG = false;
    private static double SIN45 = 0.7071;
    protected Object object = null;
    private boolean isstatic = false;
    protected boolean movable = false;
    private ArrayList<Action> actions;
    protected boolean collapsed = false;
    protected static String htmlstart = "<html><font color=blue size=-1>";
    protected static String htmlend = "</font></html>";
    protected static String nl = "<br>";
    private Rectangle bounds = null;
    protected boolean error = false;
    private Text label;
    private Object source = null;
    protected boolean visible = true;
    protected int zoomfactor = 100;
    private int layer = 0;
    protected Color backgroundColor = Color.white;
    protected Color foregroundColor = Color.white;
    protected Color highlightColor = Color.black;
    protected Dimension absoluteSize = new Dimension(0, 0);
    protected Dimension viewSize = new Dimension(0, 0);
    protected Point absolutePosition = new Point(0, 0);
    private String name;
    protected boolean selectable = true;
    protected Rectangle viewrect = null;
    protected boolean selected = false;
    protected Point origin = new Point(0, 0);
    protected boolean highlighted = false;
    private SelectionListener selectionlistener = null;
    protected DrawingCanvas canvas;
    
    protected ArrayList<Drawable>  ds;
    private boolean invalidpos = true;
    private boolean invalidsize = true;
    private int w = -1;
    private int h = -1;
    private static HashMap visible_map = new HashMap();

    // ***************************************************************************
    // CONSTRUCTOR
    // ***************************************************************************
    protected GuiObject() {
        this(null, null, null);
    }

    protected GuiObject(Point origin) {
        this(null, null, origin);
    }

    protected GuiObject(DrawingCanvas canvas, Point origin) {
        this(null, canvas, origin);
    }

    protected GuiObject(Object object, DrawingCanvas canvas, Point origin) {
        //super(object);
        //	p("constructor of GuiObject:"+object.getClass().getName()+", after calling super");
        this.object = object;
        this.origin = origin;
        this.canvas = canvas;
        //	guiinfo = new GuiInfo(getType(), getClass());
    }
    // ***************************************************************************
    //GET SET
    // ***************************************************************************


    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public void setDrawable(Drawable d) {
        setLink(d);
    }

    public Drawable getDrawable() {
        if (ds == null || ds.size() < 1) {
            return null;
        } else {
            return (Drawable) ds.get(0);
        }

    }

    public void addDrawable(Drawable d) {
        addLink(d);
    }

    public void addLink(Drawable d) {
        if (ds == null) {
            ds = new ArrayList<Drawable> ();
        }
        ds.add(d);
    }

    public void setLink(Drawable d) {
        ds = new ArrayList<Drawable> ();
        addLink(d);
    }

    public boolean isError() {
        return error;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean c) {
        this.collapsed = c;
    }

    public void setBaseObject(Object object) {
        this.object = object;
    }

    public Object getBaseObject() {
        return object;
    }

    public void setStatic(boolean s) {
        this.isstatic = s;
    }

    public boolean isStatic() {
        return isstatic;
    }

    // ***************************************************************************
    // ACTIONS
    // ***************************************************************************
    public void setActions(ArrayList<Action> v) {
        this.actions = v;
    }

    public ArrayList<Action> getActions() {

        return actions;
    }

    public void addAction(BasicAction a) {
        if (actions == null) {
            actions = new ArrayList<Action>();
        }
        actions.add(a);
    }

    
    // ***************************************************************************
    // ABSTRACT; FROM DRAWABLE
    // ***************************************************************************
    /**
     * draw the graphics component
     */
    @Override
    public abstract void draw(Graphics g);

    @Override
    public void clear(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(getX(), getY(), getWidth(), getHeight());
    }

    @Override
    public String getToolTipText(MouseEvent evt) {        
        if (!isVisible()) {
            return null;
        }
        return htmlstart + toHtml() + htmlend;
    }

    public String toHtml() {
        return "";
    }

    public boolean isText() {
        return false;
    }

    public ArrayList<Drawable> getDrawables() {
        return ds;
    }

    public boolean drawAbove() {
        return isForward();
    }

    public String getId() {
        return null;
    }

    public void drawBounds(Graphics g2) {
        g2.setColor(Color.green);
        Rectangle r = getBounds();
        g2.drawRect(r.x, r.y, r.width, r.height);
    }

    // ***************************************************************************
    // MOUSE STUFF
    // ***************************************************************************
    public void mouseClicked(MouseEvent e) {
        //p("mouse clicked");
    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    // ***************************************************************************
    // IMPLEMENTED; FROM DRAWABLE
    // ***************************************************************************
    public boolean isHighlighted() {
        return highlighted;
    }

    public void setHighlighted(boolean high) {
        this.highlighted = high;
    }

    public boolean isMovable() {
        return movable;
    }

    public void setMovable(boolean b) {
        this.movable = b;
    }

    public void setSelectable(boolean b) {
        this.selectable = b;
    }

    public boolean isSelectable() {
        return selectable;
    }

    public boolean isForward() {
        return true;
    }

    public boolean isClassVisible() {
        Boolean B = (Boolean) visible_map.get(getClass().getName());
        if (B == null || B.booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isClassVisible(Class clazz) {
        Boolean B = (Boolean) visible_map.get(clazz.getName());
        if (B == null || B.booleanValue()) {
            return true;
        } else {
            return false;
        }
    }

    public void setClassVisible(boolean b) {
        if (visible_map == null) {
            visible_map = new HashMap();
        }
        visible_map.put(getClass().getName(), new Boolean(b));
    }

    public static void setClassVisible(Class c, boolean b) {
        if (visible_map == null) {
            visible_map = new HashMap();
        }
        visible_map.put(c.getName(), new Boolean(b));
    }
//	public static void setClassVisible(GuiInfo info, boolean b) {
//		if (visible_map == null)
//			visible_map = new HashMap();
//		visible_map.put(info.getGuiClass(), new Boolean(b));
//	}

    public boolean isVisible() {
        return visible && isClassVisible();
    }

    public void setVisible(boolean vis) {
        //if (this instanceof GuiChromoQtl) {
        //	if (vis !=visible) {
        //		err("Qtl is set to visible: "+vis);
        //		Exception e = new Exception("Qtl is set to "+vis);
        //		e.printStackTrace();
        //	}
        //}
        this.visible = vis;

    }

    /**
     * returns true, if the point p (a absolute position) is contained in this
     * object (determinded using the absolute position and absolute size
     */
    public boolean containsPoint(Point p) {
        if (getBounds() == null) {
            return false;
        }
        return getBounds().contains(p);
    }

    /**
     * returns true, if the point p (a absolute position) is contained in this
     * object (determinded using the absolute position and absolute size
     */
    public boolean contains(int px, int py) {
        if (getBounds() == null) {
            return false;
        }
        return getBounds().contains(px, py);
    }

    private boolean contains(Point p) {
        return containsPoint(p);
    }

    /**
     * returns true, if the drawable draw overlaps with this drawable.
     */
    public boolean overlaps(Drawable draw) {
        if (getBounds() == null) {
            return false;
        } else {
            return getBounds().intersects(draw.getBounds());
        }
    }

    /**
     * returns true, if the drawable draw overlaps with this drawable.
     */
    public boolean overlaps(Rectangle rect) {
        return getBounds().intersects(rect);
    }
    // ***************************************************************************
    // GET/SET
    // ***************************************************************************

    /**
     * set the rectangle that is currently viewed
     */
    protected void setViewRect(Rectangle rect) {
        this.viewrect = rect;
    }

    /**
     * get the currently viewed rectangle
     */
    public Rectangle getViewRect() {
        return viewrect;
    }

    /**
     * Creates a new SelectionEvent and notifies any listeners
     */
    @Override
    public void setSelected(boolean b, boolean sendevent) {
        this.selected = b;
        // notify listeners
        if (sendevent) {
            SelectionEvent evt = new SelectionEvent(this, this, "SELECT", null);
            notifySelectionListeners(evt);
        }

    }

    public void setSelected(boolean b) {
        setSelected(b, true);
    }

    /**
     * returs true if the current object is selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * A zoomfactor of 1 = normal size (default), 2 would be twice the size (you
     * see only half of it with 0=nimimum size and 100 = maximum size
     */
    public double getZoomFactor() {
        if (canvas == null) {
            return 1;
        }
        return canvas.getZoomFactor();
    }

    public double getXZoomFactor() {
        if (canvas == null) {
            return 1;
        }
        return canvas.getXZoomFactor();
    }

    public double getYZoomFactor() {
        if (canvas == null) {
            return 1;
        }
        return canvas.getYZoomFactor();
    }

    public int getX() {
        if (invalidpos) {
            updateBounds();
        }
        return (int) getAbsolutePosition().getX();
    }

    public int getY() {
        if (invalidpos) {
            updateBounds();
        }
        return (int) getAbsolutePosition().getY();
    }

    public void setHeight(int h) {
        int w = getWidth();
        setAbsoluteSize(new Dimension(w, h));
    }

    public void setWidth(int w) {
        int h = getHeight();
        setAbsoluteSize(new Dimension(w, h));
    }

    public Dimension getAbsoluteSize() {
        return absoluteSize;
    }

    /**
     * Calculates the actual viewing size as a function of the zoomfactor and
     * absoluteSize and sets the viewSize accordingly
     */
    public void setAbsoluteSize(Dimension size) {
        absoluteSize = size;
        bounds = null;
        invalidsize = true;

    }

    public Point getAbsolutePosition() {
        return absolutePosition;
    }

    public void rotate90(Point m) {
        rotate90(true, m);
    }

    public void rotate180(Point m) {
        double x = 2 * m.getX() - getX();
        double y = 2 * m.getY() - getY();
        setAbsolutePosition(new Point((int) x, (int) y));
    }

    public void rotate90(boolean plus, Point m) {
        setAbsoluteSize(new Dimension(getHeight(), getWidth()));

        double x = 0;
        double y = 0;
        if (plus) {
            x = 2 * m.getY() - getY();
            y = getX();
        } else {
            x = getY();
            y = 2 * m.getX() - getX();
        }

        setAbsolutePosition(new Point((int) x, (int) y));
    }

    // ***************************************************************************
    // NEW MOVEMENT/POSITIONS
    // ***************************************************************************
    public void setPosition(Point p) {
        setAbsolutePosition(p);
    }

    public Point getPosition() {
        return getAbsolutePosition();
    }

    public Dimension getSize() {
        return getAbsoluteSize();
    }

    public void setSize(Dimension d) {
        setAbsoluteSize(d);
    }

    public Rectangle getBounds() {
        if (bounds == null) {
            updateBounds();
        }
        return bounds;
    }

    public void updateBounds() {
        if (getPosition() == null) {
            err("No position for guiobject " + toString());
            bounds = null;;
            invalidpos = true;
        } else if (getSize() == null) {
            err("No size for guiobject " + toString());
            bounds = null;
            invalidsize = true;
        } else {
            bounds = new Rectangle(getPosition(), getSize());
            w = (int) bounds.getWidth();
            h = (int) bounds.getHeight();
            invalidpos = false;
            invalidsize = false;
        }
    }

    // ***************************************************************************
    // MOVEMENT/POSITIONS
    // ***************************************************************************
    public void move(int dx, int dy) {
        setAbsolutePosition(new Point(getX() + dx, getY() + dy));
    }

    public int getWidth() {
        if (invalidsize) {
            updateBounds();
        }
        return w;
    }

    public int getHeight() {
        if (invalidsize) {
            updateBounds();
        }
        return h;
    }

    public void moveTo(int x, int y) {

        setAbsolutePosition(new Point(x, y));
    }

    public void setLocation(Point p) {
        setAbsolutePosition(p);
    }

    /**
     * Calculates the actual viewing position
     */
    public void setAbsolutePosition(Point p) {
        this.absolutePosition = p;
        bounds = null;
        invalidpos = true;
    }

    public void setOrigin(Point p) {
        origin = p;
    }

    public Point getOrigin() {
        return origin;
    }

    public Color getForeground() {
        return foregroundColor;
    }

    public Color getHighlight() {
        return highlightColor;
    }

    public Color getBackground() {
        return backgroundColor;
    }

    public void setForeground(Color c) {
        foregroundColor = c;
    }

    public void setBackground(Color c) {
        backgroundColor = c;
    }

    public Dimension getViewSize() {
        return viewSize;
    }

    // *****************************************************************
    // HELPER METHODS
    // *****************************************************************
    // ***************************************************************************
    // REGISTER AT CENTRAL MANAGER
    // ***************************************************************************
    /**
     * From interface EventProducer
     */
    public void registerEvents() {
        //CommunicationController.registerSelectionEvents(this);
    }

    // *****************************************************************
    // NOTIFY SELECTION LISTENERS
    // *****************************************************************
    public void addSelectionListener(SelectionListener list) {
        selectionlistener = EventMulticaster.add(selectionlistener, list);

    }

    public void removeSelectionListener(SelectionListener list) {
        selectionlistener = EventMulticaster.remove(selectionlistener, list);
    }

    /**
     * whenever a new event is produced, the event producer must call this
     * method in order to notify any listeners
     */
    protected synchronized void notifySelectionListeners(SelectionEvent evt) {
        if (selectionlistener != null) {
            selectionlistener.selectionPerformed(evt);
        }
    }

    /**
     * This method should be overwritten for any specific instance
     */
    public String toString() {
        return name;
    }

    public Text getLabel() {
        return label;
    }

    public void setLabel(Text text) {
        label = text;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int i) {
        layer = i;
    }

    protected void pp(String s) {
        Logger.getLogger("GuiObject").info(s);
    }

    protected void p(String s) {
        Logger.getLogger("GuiObject").info(s);
    }

    protected void err(String s) {
        Logger.getLogger("GuiObject").warning(s);
    }

    public String getDescription() {
        return toString();
    }

    protected BufferedImage createStripedImage(int thickness, int direction) {
        int w = 50;
        int h = 50;        
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) img.getGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(thickness));
        g2.setColor(Color.gray);        
        double y = -h;
        while (y < h+h) {
            g2.draw(new Line2D.Double(0.0, y, w, y + w * SIN45*direction));
            y += thickness*2.0;
        }
        return img;
    }

    protected Paint getStripedPaint(int thickness, int direction) {
        BufferedImage img = createStripedImage(thickness, direction);
        Paint paint = new TexturePaint(img, new Rectangle2D.Double(0.0, 0.0, (double) img.getWidth(), (double) img.getHeight()));
        return paint;
    }
}
