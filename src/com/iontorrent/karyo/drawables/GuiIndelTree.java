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
import com.iontorrent.karyo.renderer.GainLossRenderType;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.*;

import java.util.ArrayList;
import java.util.List;

public class GuiIndelTree extends GuiFeatureTree {

    GainLossRenderType gltype;
// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************
    public GuiIndelTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        this(ktrack, canvas, chromo, tree, dx, 1.0);
    }

    public GuiIndelTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx, double scale) {
        super(ktrack, canvas, chromo, tree, dx, scale);
        this.gltype = (GainLossRenderType)ktrack.getRenderType();
    }

    @Override
    public String getType() {
        return gltype.getName();
    }

    @Override
    public String getName() {
        return "IndelTree " + tree.getName();
    }

// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************
    /**
     * draw the graphics component
     */
    @Override
    public void draw(Graphics g0) {

        Graphics2D g = (Graphics2D) g0;

        int x = (int) chromo.getX() + dx;
        int y = (int) chromo.getY();
        int h = (int) chromo.getHeight();
        int w = (int) getAbsoluteSize().getWidth();
        y += h;

        g.setColor(Color.white);
        g.drawLine(x - w, y - h, x - w, y);
        g.drawLine(x , y - h, x, y);
        g.setColor(new Color(240,240,240));
        g.drawLine(x - w / 2, y - h, x - w / 2, y);

        super.draw(g0);


    }

    @Override
    protected void drawBin(double nrpixelperfeature, boolean drawall, Graphics g, KaryoFilter filter, FeatureTreeNode node, int nr, int w, int maxnr, int s, int e, int x, int y, int h) {
        // depends on filter mode! And wether we draw all or not
        g.setColor(ktrack.getColor());
        List<KaryoFeature> features = null;
        if (filter != null && filter.isValid())
        {
            features = node.getFilteredFeatures(filter, true);
        } else {
            features = node.getAllFeatures(true);
        }

        ArrayList<KaryoFeature> gains = new ArrayList<KaryoFeature>();
        ArrayList<KaryoFeature> losses = new ArrayList<KaryoFeature>();
        ArrayList<KaryoFeature> other = new ArrayList<KaryoFeature>();
        
        for (KaryoFeature f : features) {
            String type = gltype.getGainType(f.getFeature());
            if (type.equals(gltype.GAIN)) {
                gains.add(f);
            } 
            else if (type.equals(gltype.LOSS)) {
                losses.add(f);
            }             
            else {
                other.add(f);
            }
        }
//        nrpixelperfeature = Math.min(w / 5, (double) w / Math.max(gains.size(), losses.size()));
//        nrpixelperfeature = nrpixelperfeature/2;
        // if we have at least one pixel per 
        drawall = (nrpixelperfeature > this.min_nr_pixels_per_feature_to_draw_all);

       // p("chr "+chromo.getName()+", "+node.getStart()+", total: "+features.size()+", gains: "+gains.size()+", losses: "+losses.size()+", nrpix/f="+nrpixelperfeature+", drawall: "+drawall+", w="+w+", maxnr="+maxnr);

        
        double startx = 0;
        drawVars(Color.green.darker(), false, drawall, gains, filter, g, nrpixelperfeature, s, e, x-w/2, startx, y, h, w/2, node, maxnr);        
        drawVars(Color.red.darker(), true, drawall, losses, filter, g, nrpixelperfeature, s, e, x-w/2, startx, y, h, w/2, node, maxnr);
        drawVars(Color.gray, true, drawall, other, filter, g, nrpixelperfeature, s, e, x-w/2, startx, y, h, w/2, node, maxnr);
    }

    protected void drawVars(Color color, boolean toLeft, boolean drawall, ArrayList<KaryoFeature> vars, KaryoFilter filter, Graphics g, double nrpixelperfeature, int s, int e, int x, double startx, int y, int h, int w, FeatureTreeNode node, int maxnr) {
        int nr = vars.size();
        g.setColor(color);
        if (drawall) {
            for (int i = 0; i < nr; i++) {
                KaryoFeature f = vars.get(i);
                boolean drawit = true;
                if (filter != null) {
                    if (filter.isHighlightFiltered()) {
                       // g.setColor(filter.getFilteredColor(f));
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
                        g.fillRect(x + (int) startx+1, y1 + y - h, wb, y2 - y1);
                        g.drawRect(x + (int) startx+1, y1 + y - h, wb, y2 - y1);
                    }
                    startx += nrpixelperfeature;
                }
            }

        } else {
            if (filter != null) {                
                if (filter.isHighlightFiltered()) {
                  //  g.setColor(filter.getFilteredColor(node));
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
   

    @Override
      protected void p(String s) {
        System.out.println("GuiIndelTree:" + s);
    }

}
