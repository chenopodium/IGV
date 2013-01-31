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
package com.iontorrent.karyo.drawables;

import com.iontorrent.karyo.data.Band;
import com.iontorrent.karyo.data.Chromosome;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.event.SelectionEvent;
import com.iontorrent.views.basic.DrawingCanvas;
import com.iontorrent.views.basic.GuiCanvas;
import com.iontorrent.views.basic.GuiObject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

public class GuiChromosome extends GuiObject {
 
    private ArrayList<GuiFeatureTree> trees;
    private boolean movable;
    private Chromosome chromo = null;
    private GuiChromosome linkedView;
    public static int defaultheight = 500;
    public static final long largest = 250000000;
    private static final int maxsingleheight = defaultheight/100;
    private Point start;
    public int width = 30;
    protected Font tinyfont = new Font("SansSerif", Font.PLAIN, 10);
    protected Font normalfont = new Font("SansSerif", Font.BOLD, 14);
    protected Font largefont = new Font("SansSerif", Font.BOLD, 16);
    private double scale;
    private int curLocation;
   
    private Graphics g;
// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************

    public GuiChromosome(DrawingCanvas canvas, Chromosome chromo, Point start) {
        this(canvas, chromo, start, 1.0);
    }

    public GuiChromosome(DrawingCanvas canvas, Chromosome chromo, Point start, double scale) {
        super(chromo, canvas, start);
        this.scale = scale;
        this.chromo = chromo;
        this.start = start;
        trees = new ArrayList<GuiFeatureTree>();
        setForeground(new Color(235, 235, 235));
       
        update(chromo);
    }

   
    public void addTree(GuiFeatureTree t){
        trees.add(t);
    }
    
    public String getType() {
        return "Chromosome";
    }

    public String getName() {
        return "chromosome " + chromo.getName();
    }

    public void update(Chromosome chromo) {
        this.chromo = chromo;

        int x = (int) start.getX();
        int y = (int) start.getY();
        setAbsolutePosition(new Point(x, y));
        double h = getHeight(chromo.getLength());
      //  p("Got chr " + chromo + ", largest=" + largest + ", height=" + h);
        setAbsoluteSize(new Dimension(width, Math.max(20, (int) (h))));
    }

    public static double getDefaultHeight(long bases) {
        return (double) defaultheight * (double) bases / (double) largest;
    }
    public  double getHeight(long bases) {
        return (double) scale*defaultheight * (double) bases / (double) largest;
    }
     public int getLocation(double y) {
         int location = (int) (chromo.getLength()-(int)(y*largest/defaultheight/scale)) ;
         return location;
    }
     public int getEventLocation(double eventy) {
        eventy = eventy/canvas.getYZoomFactor();
        int dy =  (int) (getAbsolutePosition().getY() -  eventy +this.getAbsoluteSize().getHeight());
        int loc = getLocation(dy);
        
        return loc;
     }

     public void useMouseLocation(int eventy) {
         this.setCurLocation(getEventLocation(eventy));
//         if (g != null) {
//             p("redrawing view");
//             draw(g);
//         }
     }
    public Chromosome getChromosome() {
        return chromo;
    }
    @Override
    public String getToolTipText(MouseEvent evt) {
        
        if (!isVisible()) {
            return null;
        }        
        int loc = getEventLocation(evt.getY());
        return htmlstart + toHtml() +"<br>Position: "+(int)(loc/1000000)+" MB"+ 
                "<br>e.y="+evt.getY()+", this.y="+this.getY()+", zoom="+canvas.getYZoomFactor()+htmlend;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************
    @Override
    public boolean isMovable() {
        return movable;
    }

    /**
     * draw the graphics component
     */
    @Override
    public void draw(Graphics g) {

        int x = (int) getAbsolutePosition().getX();
        int y = (int) getAbsolutePosition().getY();
        int h = (int) getAbsoluteSize().getHeight();
        int w = (int) getAbsoluteSize().getWidth();
        y += h;
        //  p("Drawing "+chromo+"  at "+x+"/"+y+", w="+w+", h="+h);

        int half1 = (int) getHeight(chromo.getCenter());
        int half2 = h - half1;
        g.setColor(getForeground());
        //   p("Trying to draw: ("+x+", "+y+"), ("+w+", "+h+")");
        //	g.fill3DRect(x,y, w,h, true);

        g.fillRoundRect(x, y - h, width, half1, width, width);
        g.fillRoundRect(x, y - half2, width, half2, width, width);
//        g.fillOval(x, y - h, width, half1);
//        g.fillOval(x, y - half2, width, half2);
        

        if (chromo.getBands() != null) {
            drawBands(g, x, y, w, h);
        }
        if (this.isSelected()) {
            g.setColor(Color.orange);
            g.drawRect(x-1, y - h-1, w+2, h+2);
            //p("Current location is: "+this.curLocation);
            if (this.getCurLocation()>0 && this.getCurLocation() < chromo.getLength()) {
                g.setColor(Color.red.darker());
                
                int sely = (int)getHeight(getCurLocation()); 
                p("Drawing selection at "+sely+", y="+(y-h+sely));
                g.fill3DRect(x-2, y-h+sely, w+4, (int)(3.0/this.getZoomFactor()), true);
                //g.draw3DRect(x, y-sely, w, 3, true);
            }
        }

        g.setColor(Color.darkGray);
//        g.drawOval(x, y - h, width, half1);
//        g.drawOval(x, y - half2, width, half2);
        g.drawRoundRect(x, y - h, width, half1, width, width);
        g.drawRoundRect(x, y - half2, width, half2, width, width);

        
        g.setFont(normalfont);
        ((GuiCanvas)canvas).drawText((Graphics2D)g, chromo.getName(), x + w/2 - (chromo.getName().length())*5, y -h - Math.min(50, h/20));
      //  g.drawString(chromo.getName(), x + w/2 - (chromo.getName().length())*5, y + 30);

    }

    private void drawBands(Graphics g0, int x, int y, int w, int h) {

        Graphics2D g = (Graphics2D)g0;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
//        Shape ellipse = new Ellipse2D.Double(x, y, w , h/2);
//        ellipse.

        long center = chromo.getCenter();
        double hcenter = getHeight(center);

        for (Band band : chromo.getBands()) {
            double h1 = getHeight(band.getStart());
            double h2 = getHeight(band.getEnd());
            Paint paint = getPaint(band.getType());
            
            g.setPaint(paint);
            

            double dh = h2 - h1;
            double stepsize = dh;
            if (dh > maxsingleheight) {
                // ONLY IF AT BORDERS!
                if (this.isAtEnd(w, hcenter, h-hcenter, h1) || this.isAtEnd(w, hcenter, h-hcenter, h2)) {
                    stepsize = maxsingleheight;
                    int nr = (int) (dh / stepsize + 1);
                    stepsize = dh / nr;
                }
               // p("dh " + dh + " larger than " + (maxsingleheight) + ", splitting into " + nr + "  of size " + stepsize);
            }
            
            int xm = x + w / 2;
            for (double curh1 = h1; curh1 < h2; curh1 += stepsize) {
                double curh2 = curh1 + stepsize;
                int bw1 = (int)getXForY(w, hcenter, h-hcenter, curh1);
                int bw2 = (int)getXForY(w, hcenter, h-hcenter, curh2);
                Polygon p = new Polygon();
                
                p.addPoint(xm - bw1, (int) (y - h + curh1));
                p.addPoint(xm + bw1, (int) (y - h + curh1));
                p.addPoint(xm + bw2, (int) (y - h + curh2));
                p.addPoint(xm - bw2, (int) (y - h + curh2));
                g.fillPolygon(p);
                g.drawPolygon(p);
            }
            int w1 = (int)getXForY(w, hcenter, h-hcenter, (h1+h2)/2);
            if (h2 - h1 > 10.0/this.getZoomFactor() && w1>w/3 && band.getName().length()<7) {
                g.setFont(tinyfont);                
                
                Color c = this.getInvertedColor(getColor(band.getType()));
                g.setColor(c);
              //  p("Drawing band name at "+(xm-(band.getName().length()-1)*3)+"/"+ (y-h+(int)(h1+h2)/2+2));
                ((GuiCanvas)canvas).drawText((Graphics2D)g, band.getName(), xm-(band.getName().length()-1)*3, y-h+(int)(h1+h2)/2+3);
                //.g.drawString(band.getName(), xm-(band.getName().length()-1)*3, y-h+(int)(h1+h2)/2+3);
            }        
            
        }
    }

    private boolean isAtEnd(double w, double h1, double h2, double y) {
        double h = h1;
        
        if (y > h1) {
            y = y - h1-1;
            h = h2;
        }
        if (y > h / 2) {
            y = h-y;
        }       
        if (y >= w/2) {
            return false;
        }
        return true;
    }
    private double getXForY(double w, double h1, double h2, double y) {
        double h = h1;
        double origy = y;
        if (y > h1+h2+1) {
          //  err("heihgt too large: y="+origy+"  "+h+","+h1+","+h2);
        }
        if (y > h1) {
            y = y - h1-1;
            h = h2;
        }
        double r = w / 2;
        if (y > h / 2) {
            y = h-y;
        }
       // if (y <0) err("heihgt too small: y="+y+", origy="+origy+  ", "+h+","+h1+","+h2);
        if (y >= r) {
            return r;
        }
        y =  r-y;
        double bw = Math.sqrt(r*r - y*y);
        return bw;
    }
    
    private Color getInvertedColor(Color c) {
        int r = c.getRed();
        int g = c.getGreen();
        int b = c.getBlue();
        if (r < 200 && g < 200 && b < 200) return Color.white;
        else return new Color(10,10,10);
        
    }
    private Paint getPaint(String type) {
        Color c = getColor(type);
        Paint p = c;
        if (type.equalsIgnoreCase("acen" )) p =  getStripedPaint(2, 1);
        else if (type.equalsIgnoreCase("stalk" ))p =  getStripedPaint(2, -1);
        
        return p;
    }
    private Color getColor(String type) {
        if (type.equals("gneg")) {
            return new Color(250,250,250);
        } else if (type.equals("gpos25")) {
            return new Color(210,210,210);
        } else if (type.equals("gpos50")) {
            return  new Color(170,170,170);
        } else if (type.equals("gpos75")) {
            return  new Color(120,120,120);
        } else if (type.equals("gvar")) {
            return new Color(140,140,140);
        } else if (type.equals("stalk")) {
            return Color.yellow;
        } else if (type.equals("acen")) {
            return Color.GREEN;
        } else {
            return  new Color(80,80,80);
        }
    }

    protected void p(String s) {
        System.out.println("GuiChromosome:" + s);
    }

    /**
     * clear the graphics component
     */
    public void clear(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, (int) getViewSize().getWidth(),
                (int) getViewSize().getHeight());
    }

    /**
     * This method should be overwritten for any specific instance
     */
    public String toString(String nl) {
        String res = "chr " + chromo.getName() + ", " + (int) chromo.getLength()/1000000+" MB "+nl+chromo.getBands().size()+"  bands";
        return res;
    }

    public String toString() {
        return toString("\n");
    }

    public String toHtml() {
        return toString("<br>");
    }
    
    @Override
     public void mouseClicked(MouseEvent evt) {
        
        this.setCurLocation(getEventLocation(evt.getY()));
        
        p("mouse clicked at loc "+getCurLocation());
        // notify selection listeners
        String cmd = "SELECT";
        if (evt.getClickCount() > 1) cmd = "DOUBLE";
        SelectionEvent e = new SelectionEvent(this, this, cmd, evt.getPoint());
        notifySelectionListeners(e);
        
    }
     public int getCurrentLocation() {
         return getCurLocation();
     }

    /**
     * @return the linkedView
     */
    public GuiChromosome getLinkedView() {
        return linkedView;
    }

    /**
     * @param linkedView the linkedView to set
     */
    public void setLinkedView(GuiChromosome linkedView) {
        this.linkedView = linkedView;
    }

    /**
     * @return the curLocation
     */
    public int getCurLocation() {
        return curLocation;
    }

    /**
     * @param curLocation the curLocation to set
     */
    public void setCurLocation(int curLocation) {
        this.curLocation = curLocation;
    }

    /**
     * @return the trees
     */
    public ArrayList<GuiFeatureTree> getTrees() {
        return trees;
    }

    /**
     * @param trees the trees to set
     */
    public void setTrees(ArrayList<GuiFeatureTree> trees) {
        this.trees = trees;
    }

    /**
     * @param movable the movable to set
     */
    public void setMovable(boolean movable) {
        this.movable = movable;
    }
}
