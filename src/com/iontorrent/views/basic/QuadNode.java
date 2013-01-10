/*
 *
 */
package com.iontorrent.views.basic;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Vector;
import java.util.logging.Logger;


public class QuadNode {
	private QuadNode nodes[]=new QuadNode[4];
	private boolean leaf=true;
	private Rectangle rect;
	private Vector objs;
	private int limit = 30;
	private String name;
	private static boolean DEBUG = false;
	private static final int MINWIDTH = 20;
	private static final int MINHEIGHT = 20;
//	**************************************************************************
//  CONSTRUCTOR
//  **************************************************************************
	public QuadNode(int x1, int y1, int w, int h) {
		this(new Rectangle(x1, y1, w, h));
	}
	public QuadNode(Rectangle rect){
		this.rect=rect;
		leaf=true;
	//	p("created quad "+toString());
	}
//	**************************************************************************
//	GET/SET
//	**************************************************************************
	public String getName() {
		if (name == null) name="1";
		return name;
	}

	public void setName(String string) {
		name = string;
	}

	public boolean isFull(){
		return objs !=null && objs.size() > limit;
	}
	public boolean isEmpty(){
		return objs ==null || objs.size() < 1;
	}
	public Rectangle getBounds(){
		return rect;
	}
	private boolean isLeaf() {
		return leaf;
	}
	public int getLimit() {
		return limit;
	}

/* max number of objects that can be inside before splitting occurs
 */
	public void setLimit(int i) {
		limit = i;
	}
	public int getX() {
		return (int)rect.getX();
	}
	public int getY() {
		return (int)rect.getY();
	}
	public int getWidth() {
		return (int)rect.getWidth();
	}
	public int getHeight() {
		return (int)rect.getHeight();
	}
	public Vector getAllQuads() {
		Vector res = new Vector();
		getAllQuads_r(res);
		return res;
	}
	private void getAllQuads_r(Vector res) {
		if (isLeaf()) res.add(this);
		else {
			for (int i = 0; i < nodes.length; i++) {
				Vector v= nodes[i].getAllQuads();
				res.addAll(v);
			}
		}
	}
//	**************************************************************************
//	OBJECTS
//	**************************************************************************
	
	public void add(Vector objects){
		for (int i = 0; objects != null && i < objects.size(); i++) {
			Drawable d = (Drawable)objects.get(i);
			add(d);
		}
	}
	public void add(Drawable d){
		if (d==null || !d.isVisible() || (d instanceof Text)) return;
		if (!intersects(d)) {
	//		p("Drawable does not intersect with thid quad "+rect+", ignoring it");
			return;
		}			
		if (!isLeaf()) {
			// adding to children
			for (int i = 0; i < nodes.length; i++) {
				nodes[i].add(d);
			}
		}
		else {
			if (objs == null) objs = new Vector();
		//	p("adding drawable to quad "+rect);
			objs.add(d);
			if (objs.size() > limit) {
		//		p("Limit "+limit+" reached in "+toString()+", splitting");
				split();
			}
		}
	}
	public Vector getObjects() {
		if (leaf) return objs;
		else {
			Vector res = new Vector();
			for (int i = 0; i < nodes.length; i++) {
				Vector v= nodes[i].getObjects();
				if (v !=null)res.addAll(v);
			}
			return res;
		}
	}

	public void remove(Drawable d) {
		if (isLeaf()) {
			if (isEmpty()) return;
			if (objs.contains(d)) {
				objs.remove(d);
			}
		}
		else {
			for (int i = 0; i < nodes.length; i++) {
				nodes[i].remove(d);
			}
		}
		
	}
	
	/** return true only if entire object fits into this quadnode
	 * 
	 * @param d
	 * @return
	 */
	public boolean contains(Drawable d) {
		return rect.contains(d.getBounds());
	}
	public boolean contains(Point p) {
		return rect.contains(p);
	}
	public boolean contains(int x, int y) {
		return rect.contains(x, y);
	}
//	**************************************************************************
//	METHODS
//	**************************************************************************
	/** returns a list of quads that intersect with this drawable 
	 * returns null if the drawable does not intersect with this quad
	 */
	public Vector findQuads(Drawable d) {
		if (intersects(d)) {
			Vector quads = new Vector();
			findQuads_r(d, quads);
			return quads;
		} 	
		else return null;
	}
	public void findQuads_r(Drawable d, Vector quads) {
		if (isLeaf()) {
			quads.add(this);
		}
		else { //not a leaf
			for (int i = 0; i < nodes.length; i++) {
				Vector v = nodes[i].findQuads(d);
				if (v != null) {
					quads.addAll(v);
				}
			}
		}
	}
	public QuadNode findQuad(int x, int y) {
			if (contains(x, y)) {
				return findQuad_r(x, y);
			} 	
			else return null;
		}
	public QuadNode findQuad_r(int x, int y) {
		if (isLeaf()) {
			return this;
		}
		else { //not a leaf
			for (int i = 0; i < nodes.length; i++) {
				QuadNode n = nodes[i].findQuad(x, y);
				if (n != null) return n ;
			}
		}
		return null;
	}
	public Vector getOverlapping(Drawable d) {
		Vector quads = findQuads(d);
		Vector res = new Vector();
		for (int i = 0; quads != null && i < quads.size(); i++) {
			QuadNode n= (QuadNode)quads.get(i);
			Vector v = n.getThisOverlapping(d);
			if (v != null)res.addAll(v);
				
		}
		return res;
	}
	public boolean overlaps(Drawable d){
	//	
		if (isLeaf()) {
			p("leaf: overlaps "+getName());
			return getThisOverlapps(d);
		}
		else { 
			for (int i = 0; nodes != null && i < nodes.length; i++) {
				p("node: overlaps "+nodes[i].getName()+"?");
				if (nodes[i].intersects(d)) {
					p("yes, node overlaps "+nodes[i].getName());
					boolean over = nodes[i].overlaps(d);
					if (over) return true;	
				}
			}
		}
		return false;
	}
	private boolean getThisOverlapps(Drawable d) {
			if (objs != null) {
				for (int i = 0; objs != null && i < objs.size(); i++) {
					Drawable target= (Drawable)objs.get(i);
					if (d!=target && d.getBounds().intersects(target.getBounds()))return true;
				}
			}
			return false;
		}
	
	private Vector getThisOverlapping(Drawable d) {
		Vector res = new Vector();
		if (objs != null) {
			for (int i = 0; objs != null && i < objs.size(); i++) {
				Drawable target= (Drawable)objs.get(i);
				if (d.getBounds().intersects(target.getBounds())) res.add(target);
			}
		}
		return res;
	}
	private Vector getThisOverlapping(int x, int y) {
		Vector res = new Vector();
		if (objs != null) {
			for (int i = 0; objs != null && i < objs.size(); i++) {
				Drawable target= (Drawable)objs.get(i);
				if (target.getBounds().contains(x, y)) res.add(target);
			}
		}
		return res;
	}
	public Vector getDrawablesAt(int x, int y) {
		QuadNode n = findQuad(x, y);
		Vector res = new Vector();
		Vector v = n.getThisOverlapping(x, y);
		if (v != null)res.addAll(v);
		return res;
	}
	private void checkLimit() {
		int nr = count();
		
		if (!isLeaf()) {
			if (nr < limit) merge();
			else {
				// check Children
				for (int i = 0; i < nodes.length; i++) {
					nodes[i].checkLimit();
				}
			}
			return;
		}
		else if (nr > limit) {
		//	p(objs.size()+" > "+limit+", splitting");
			split();
		}
	}
	/** returns nr of objects cointaind in quad or subquads
	 * 
	 * @return
	 */
	public int count() {
		int nr = 0;
		if (isLeaf()) {
			if (!isEmpty()) nr=objs.size();
		}
		else {
			for (int i = 0; i < nodes.length; i++) {
				nr += nodes[i].count();
			}
		}
	//	p("count in "+rect+": "+nr);
		return nr;
	}
	public boolean intersects (Drawable d) {
		Rectangle dr = d.getBounds();
		if (dr.intersects(rect)) return true;
		else return false;
	}
	private boolean split(){
		if (!isFull()) return false;
		p("Splitting quad "+toString());
		int x1 = (int) rect.getX();
		int y1 = (int) rect.getY();
		int w = (int) rect.getWidth()/2;
		int h = (int) rect.getHeight()/2;
		if (w < MINWIDTH|| h< MINHEIGHT) return false;
		
		nodes[0] = new QuadNode(x1, y1, w, h);
		nodes[1] = new QuadNode(x1+w, y1, w, h);
		nodes[2] = new QuadNode(x1, y1+h, w, h);
		nodes[3] = new QuadNode(x1+w, y1+h, w, h);
		for (int i = 0; i < nodes.length; i++) {
			nodes[i].add(objs);
			nodes[i].setName(getName()+"."+(i+1));
		}
		leaf = false;
		p("leaf? "+getName()+":"+isLeaf());
		objs = null;
		return true;
	}
	private boolean merge() {
		if (isLeaf()) return false;
		p("Merging quad "+rect);
		objs = getObjects();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = null;
		}
		leaf = true;
		return true;
	}

	public String toString() {
		int nr = 0;
		if (objs != null) nr = objs.size();
		return getName()+" objs: "+nr+"; pos=("+getX()+"/"+getY()+"; "+getWidth()+"/"+getHeight()+")";
	}
//	*****************************************************************
//	TEST/DEBUG
//	*****************************************************************
	protected void p(String s) {
		if (DEBUG) {
			Logger.getLogger("QuadNode").info("QuadNode: " + s);
		}
	}
	
	protected void err(String s) {
		Logger.getLogger("QuadNode").warning("QuadNode: " + s);
	}
	protected void err(Exception e) {
		Logger.getLogger("QuadNode").warning("QuadNode: " + e.toString());
	}
	public static void main(String[] args) {
	
	}
		
	
}