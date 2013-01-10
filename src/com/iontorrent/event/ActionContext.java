package com.iontorrent.event;


public class ActionContext {

    private int x;
    private int y;
    private Object obj;

    public ActionContext(int x, int y, Object obj) {
        this.x = x;
        this.y = y;
        this.obj = obj;
    }
     public ActionContext(int x, int y) {
         this(x, y, null);
     }
     public int getX() { return x; }
     public int getY() { return y; }
     public Object getObject(){ return obj; }
}
