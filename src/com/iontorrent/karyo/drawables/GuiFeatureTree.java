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

import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.FeatureTreeNode;
import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.event.SelectionEvent;
import com.iontorrent.karyo.renderer.RenderType;
import com.iontorrent.views.basic.DrawingCanvas;
import com.iontorrent.views.basic.GuiCanvas;
import com.iontorrent.views.basic.GuiObject;
import com.iontorrent.views.basic.StaticText;
import java.awt.*;

import java.awt.event.MouseEvent;
import java.util.List;

public class GuiFeatureTree extends GuiObject { 

    public static final int WIDTH = 14;
    protected int min_nr_pixels_per_feature_to_draw_all = 1;
    protected GuiChromosome chromo;
    protected FeatureTree tree;
    protected KaryoTrack ktrack;
    protected int dx;
    protected boolean toLeft;
    
    protected int width;
    protected Font tinyfont = new Font("SansSerif", Font.PLAIN, 8);
    protected RenderType renderType;
    protected int nrerrors;
    //protected Font normalfont = new Font("SansSerif", Font.BOLD, 12);
// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************

    public GuiFeatureTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        this(ktrack, canvas, chromo, tree, dx, 1.0);
    }

    public GuiFeatureTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx, double scale) {
        super(chromo, canvas, chromo.getAbsolutePosition());
        //this.scale = scale;
        this.width = WIDTH;
        this.chromo = chromo;
        this.renderType = ktrack.getRenderType();
        this.ktrack = ktrack;
        this.dx = dx;
        this.tree = tree;
        setForeground(renderType.getColor(0));
        update(tree);
    }

    public void setWidth() {
     
        update(tree);
    }

    public String getType() {
        return ktrack.getRenderType().getName();
    }

    public String getName() {
        return "FeatureTree " + tree.getName();
    }

    public void update(FeatureTree tree) {
        this.tree = tree;
        int x = (int) chromo.getX() + dx;
        int y = (int) chromo.getY();
        int h = (int) chromo.getHeight();
        int w = width;
        if (dx < 0) {
            toLeft = true;
            setAbsolutePosition(new Point(x-w, y));
        } else {
            toLeft = false;
            setAbsolutePosition(new Point(x-w, y));
        }           
        setAbsoluteSize(new Dimension(w, (int) h + 20));
    }

    protected double getHeight(long bases) {
        return chromo.getHeight(bases);
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
        return false;
    }

    /**
     * draw the graphics component
     */
    @Override
    public void draw(Graphics g0) {

         Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        
        int x = (int) chromo.getX() + dx;
        int y = (int) chromo.getY();
        int h = (int) chromo.getHeight();
        
        int w = (int) getAbsoluteSize().getWidth();
        setAbsolutePosition(new Point(x-w, y));
        y += h;
       
        g.setColor(new Color(252, 252, 252));
        g.fillRect(x-w, y-h, w, h);
        g.setColor(new Color(250, 250, 250));
        g.drawRect(x-w, y-h, w, h);
       
       // g.setColor(Color.red);
        //g.drawRect((int)this.getAbsolutePosition().getX(), (int)this.getAbsolutePosition().getY(), this.getWidth(), this.getHeight());
       
        
        g.setColor(ktrack.getRenderType().getColor(0));
        int start = tree.getStart();
        int end = tree.getEnd();
        FeatureTreeNode nodes[] = tree.getNodes();
        int delta = (end - start) / tree.getNrbuckets();
        int maxnr = tree.getMaxNodeChildren();
        //maxnr is width

        double nrpixelperfeature = Math.min(width / 5, (double) w / maxnr);

        // if we have at least one pixel per 
        boolean drawall = (nrpixelperfeature > this.min_nr_pixels_per_feature_to_draw_all);

       
        KaryoFilter filter = tree.getFilter();
        drawBuckets(start, delta, nodes, nrpixelperfeature, drawall, g, filter, w, maxnr, x, y, h);

        if (this.isSelected()) {
            g.setColor(Color.orange);
            g.drawRect(x - w-1, y - h, w+2, h);
        }

        g.setColor(Color.black);
        g.setFont(tinyfont);
       // g.drawString(tree.getName(), x - w / 2 - (tree.getName().length()) * 3, y + 15);
        //StaticText t = new StaticText(ktrack.getShortName(),  x-10, y -h- h/40, Color.black);
        
        //((GuiCanvas)canvas).drawStatic((Graphics2D)g,t);

    }

    protected void drawBin(double nrpixelperfeature, boolean drawall, Graphics g, KaryoFilter filter, FeatureTreeNode node, int nr, int w, int maxnr, int s, int e, int x, int y, int h) {
        // depends on filter mode! And wether we draw all or not
       
        
        if (drawall) {
            // to see if we are drawing all!
            //g.setColor(Color.red.darker());
            List<KaryoFeature> features = node.getAllFeatures();
            double startx = 0;
            for (int i = 0; i < nr; i++) {
                KaryoFeature f = (KaryoFeature) features.get(i);
                if (renderType.drawFeature(f)) {
                    Color color = this.renderType.getColor(ktrack.getMetaInfo(), f);
                    g.setColor(color);
                   boolean drawit = true;
                   if (filter != null && filter.isValid()) {
                       if (filter.isHighlightFiltered()) {
                           g.setColor(filter.getFilteredColor(f));
                       } else if (filter.isRemoveFiltered()) {
                           drawit = !filter.filter(f);
                       } else {
                           drawit = filter.filter(f);
                       }
                   }
                   if (drawit) {
                       int wb = (int) nrpixelperfeature;
                       int y1 = (int) getHeight(s);
                       int y2 = (int) getHeight(e);
                       if (toLeft) {
                           g.fillRect(x - wb - (int) startx, y1 + y - h, wb, y2 - y1);
                           g.drawRect(x - wb - (int) startx, y1 + y - h, wb, y2 - y1);
                       } else {
                           g.fillRect(x - w + (int) startx, y1 + y - h, wb, y2 - y1);
                           g.drawRect(x - w + (int) startx, y1 + y - h, wb, y2 - y1);
                       }
                       startx += nrpixelperfeature;
                   }
                   //else p("NOT drawing feature, was filtered OUT");
                }
            }
        } else {
            if (filter != null) {
                if (filter.isHighlightFiltered()) {
                    g.setColor(filter.getFilteredColor(node));
                } else if (filter.isRemoveFiltered()) {
                    nr = nr - node.getNrFilterPassed();
                } else {
                    nr = node.getNrFilterPassed();
                }
            }
            int wb = (int) ((double) w / (double) maxnr * (double) nr);
            int y1 = (int) getHeight(s);
            int y2 = (int) getHeight(e);
            if (toLeft) {
                g.fillRect(x - wb, y1 + y - h, wb, y2 - y1);
                g.drawRect(x - wb, y1 + y - h, wb, y2 - y1);
            } else {
                g.fillRect(x - w, y1 + y - h, wb, y2 - y1);
                g.drawRect(x - w, y1 + y - h, wb, y2 - y1);
            }
        }
    }
    

//    private void drawFeatures(Graphics g0, int x, int y, int w, int h) {
//
//        Graphics2D g = (Graphics2D) g0;
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//                RenderingHints.VALUE_ANTIALIAS_ON);
//
//    }
    @Override
    protected void p(String s) {
        if (nrerrors < 10) System.out.println("GuiFeatureTree:" + s);
        nrerrors++;
    }

    /**
     * clear the graphics component
     */
    @Override
    public void clear(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, (int) getViewSize().getWidth(),
                (int) getViewSize().getHeight());
    }

    public int filter() {
        int nr = tree.filter();
        
        if (tree.hasEnabledFilter()) {
            p("Tree "+tree.getName()+"has filter. " + tree.getName() + " with filter "+tree.getFilter().toHtml()+": found " + nr+"/"+tree.getFilter().getFilterCount()+"  of "+(tree.getNrFeatures()+tree.getTotalNrChildren()));
        }
       // else p("Tree has NO enabled filters");
        return nr;
    }

    public FeatureTree getTree() {
        return tree;
    }

    /**
     * This method should be overwritten for any specific instance
     */
    public String toString(String nl) {
        String res = "Track <b>"+ tree.getSource().getDisplayName()+"</b>";// + "', " + tree.getTotalNrChildren() + " features</b><br>";
        //if (DEBUG) res += "(Rendering type: "+this.renderType.getName()+")<br>";
        return res;
    }

    @Override
    public String toString() {
        return toString("\n");
    }

    @Override
    public String toHtml() {
        return toString("<br>");
    }

    @Override
    public String getToolTipText(MouseEvent evt) {

        if (!isVisible()) {
            return null;
        }
        int loc = getEventLocation(evt.getY());
        int MB = 5;
        int MILLION = 1000000;
        // only features that are drawn
        KaryoFilter fil = tree.getFilter();
        if (fil == null || !fil.isEnabled() || !fil.isValid()) fil = null;
        List<KaryoFeature> features = tree.getFeaturesAt(loc, MB*1000000, fil);
        String info = "";
        if (features != null && features.size()>0) {
            
            p("Found " + features.size() + " items at "+loc/MILLION+"  (+- "+MB+" MB): ");
            
            info = "Found " + features.size() +" at "+loc/MILLION+" MB (+- "+MB+" MB):<br>";
            if (features.size() < 5) {
                boolean table = features.size()>1;
                // table?
                if (table)info += "<table border='1'><tr>";
                for (KaryoFeature f : features) {                    
                    if (table) info += "<td>";
                    info +=f.toHtml(this.ktrack.getMetaInfo());
                    if (table) info += "</td>";
                  //  p(f.toString());
                }
                if (table)info += "</tr></table>";
            }
            else {
                int nr = 1;
                // table?
                int MAX = 12;
                if (features.size()> MAX) {
                     info += "Showing just <b>first "+MAX+" of "+features.size()+"</b>: <br>";
                }
                info += "<table border='1'><tr>";                
                for (KaryoFeature f : features) {
                    
                    info += "<td>"+f.toShortHtml(this.ktrack.getMetaInfo())+"</td>";
                  //  p(f.toString());
                  if (nr % 4 == 0)   {
                      info += "</tr><tr>";
                  }
                  nr++;
                  if(nr > MAX) break;
                }
                info += "</tr></table>";            
            }
        } else {
        //  p("Found no features at "+loc);
          //tree.getAllFeatures().toString();
         // tree.getb
          
        }
        
        return htmlstart + toHtml() + "<br>Position: " + (int) (loc / 1000000) + " MB"
                + "<br>" + info
                +  htmlend;
    }

    public int getEventLocation(double eventy) {
        return chromo.getEventLocation(eventy);
    }
     @Override
     public void mouseClicked(MouseEvent evt) {
        
        chromo.setCurLocation(chromo.getEventLocation(evt.getY()));
        
        p("Tree: mouse clicked at loc "+chromo.getCurLocation()+" of chr "+chromo.getName());
        // notify selection listeners
        String cmd = "SELECT";
        if (evt.getClickCount() > 1) cmd = "DOUBLE";
        SelectionEvent e = new SelectionEvent(this, this, cmd, evt.getPoint());
        notifySelectionListeners(e);
        
    
        
    }

    protected void drawBuckets(int start, int delta, FeatureTreeNode[] nodes, double nrpixelperfeature, boolean drawall, Graphics2D g, KaryoFilter filter, int w, int maxnr, int x, int y, int h) {
        for (int b = 0; b < tree.getNrbuckets(); b++) {
            int s = start + delta * b;
            int e = s + delta;
            FeatureTreeNode node = nodes[b];
            if (node != null) {
                int nr = node.getTotalNrChildren();
                if (nr > 0) {
                    drawBin(nrpixelperfeature, drawall, g, filter, node, nr, w, maxnr, s, e, x, y, h);
                }
            
            }
        }
    }
}
