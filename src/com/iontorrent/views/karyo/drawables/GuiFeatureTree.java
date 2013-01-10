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
package com.iontorrent.views.karyo.drawables;

import com.iontorrent.data.karyo.filter.KaryoFilter;
import com.iontorrent.data.karyo.*;
import com.iontorrent.event.SelectionEvent;
import com.iontorrent.views.basic.DrawingCanvas;
import com.iontorrent.views.basic.GuiCanvas;
import com.iontorrent.views.basic.GuiObject;
import java.awt.*;

import java.awt.event.MouseEvent;
import java.util.List;

public class GuiFeatureTree extends GuiObject { 

    protected int min_nr_pixels_per_feature_to_draw_all = 1;
    protected GuiChromosome chromo;
    protected FeatureTree tree;
    protected KaryoTrack ktrack;
    protected int dx;
    protected boolean toLeft;
    protected int width = 10;
    protected Font tinyfont = new Font("SansSerif", Font.PLAIN, 8);
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
        this.chromo = chromo;
        this.ktrack = ktrack;
        this.dx = dx;
        this.tree = tree;
        setForeground(new Color(255, 235, 100));
        update(tree);
    }

    public void setWidth() {
        this.width = width;
        update(tree);
    }

    public String getType() {
        return "FeatureTree";
    }

    public String getName() {
        return "FeatureTree " + tree.getName();
    }

    public void update(FeatureTree tree) {
        this.tree = tree;
        if (dx < 0) {
            toLeft = true;
        } else {
            toLeft = false;
        }
        setAbsolutePosition(new Point(chromo.getX() + dx - width, chromo.getY()));
        double h = chromo.getHeight();
        setAbsoluteSize(new Dimension(width, (int) h + 20));
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
        y += h;
        //  p("Drawing "+chromo+"  at "+x+"/"+y+", w="+w+", h="+h);

//        g.setColor(new Color(240,240,240));
//        if (!toLeft)g.drawLine(x, y - h, x, y); 
//        else g.drawLine(x - w, y - h, x - w, y);
        
        g.setColor(ktrack.getDefaultColor());
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

        if (this.isSelected()) {
            g.setColor(Color.orange);
            g.drawRect(x - w, y - h, w, h);
        }

        g.setColor(ktrack.getDefaultColor());
        g.setFont(tinyfont);
       // g.drawString(tree.getName(), x - w / 2 - (tree.getName().length()) * 3, y + 15);
        ((GuiCanvas)canvas).drawText((Graphics2D)g, ktrack.getShortName(), x-5, y -h- h/40);

    }

    protected void drawBin(double nrpixelperfeature, boolean drawall, Graphics g, KaryoFilter filter, FeatureTreeNode node, int nr, int w, int maxnr, int s, int e, int x, int y, int h) {
        // depends on filter mode! And wether we draw all or not
        g.setColor(ktrack.getDefaultColor());
        if (drawall) {
            // to see if we are drawing all!
            //g.setColor(Color.red.darker());
            List<KaryoFeature> features = node.getAllFeatures();
            double startx = 0;
            for (int i = 0; i < nr; i++) {
                KaryoFeature f = (KaryoFeature) features.get(i);
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
    protected void p(String s) {
        System.out.println("GuiFeatureTree:" + s);
    }

    /**
     * clear the graphics component
     */
    public void clear(Graphics g) {
        g.setColor(getBackground());
        g.fillRect(0, 0, (int) getViewSize().getWidth(),
                (int) getViewSize().getHeight());
    }

    public int filter() {
        int nr = tree.filter();
       // p("Filtered tree " + tree.getName() + ": " + nr);
        return nr;
    }

    public FeatureTree getTree() {
        return tree;
    }

    /**
     * This method should be overwritten for any specific instance
     */
    public String toString(String nl) {
        String res = tree.getName() + ", " + tree.getTotalNrChildren() + " features";
        return res;
    }

    public String toString() {
        return toString("\n");
    }

    public String toHtml() {
        return toString("<br>");
    }

    @Override
    public String getToolTipText(MouseEvent evt) {

        if (!isVisible()) {
            return null;
        }
        int loc = getEventLocation(evt.getY());
        List<KaryoFeature> features = tree.getFeaturesAt(loc, 1000000);
        String info = "";
        if (features != null) {
            p("Found " + features.size() + " features");
            if (features.size() < 4) {
                for (KaryoFeature f : features) {
                    info += f.toHtml() + "_________________<br>";
                    p(f.toString());
                }
            } else {
                info = "Found " + features.size() + ". Example: <br>";
                info += features.get(0).toHtml();
                p(features.get(0).toString());
            }
        } else {
          //  info = "Found no features there";
        }
        if (tree.getNodeForLocation(loc) != null && tree.hasFilter()) {
            if (tree.getNrFilterPassed() > 0) {
                info += "<br>Got " + tree.getNrFilterPassed() + " filtered features";
            }
           // info += "<br>Filter: " + tree.getFilter().toString();
        }
        return htmlstart + toHtml() + "<br>Position: " + (int) (loc / 1000000) + " MB"
                + "<br>" + info
                + "<br>e.y=" + evt.getY() + ", this.y=" + this.getY() + ", zoom=" + canvas.getYZoomFactor() + htmlend;
    }

    public int getEventLocation(double eventy) {
        return chromo.getEventLocation(eventy);
    }
     @Override
     public void mouseClicked(MouseEvent evt) {
        
        chromo.setCurLocation(chromo.getEventLocation(evt.getY()));
        
        p("mouse clicked at loc "+chromo.getCurLocation());
        // notify selection listeners
        String cmd = "SELECT";
        if (evt.getClickCount() > 1) cmd = "DOUBLE";
        SelectionEvent e = new SelectionEvent(this, this, cmd, evt.getPoint());
        notifySelectionListeners(e);
        
    }
}
