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
import com.iontorrent.karyo.renderer.HeatMapRenderType;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.*;

import java.util.List;

public class GuiHeatMapTree extends GuiFeatureTree {

    HeatMapRenderType heattype;
// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************

    public GuiHeatMapTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        this(ktrack, canvas, chromo, tree, dx, 1.0);
    }

    public GuiHeatMapTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx, double scale) {
        super(ktrack, canvas, chromo, tree, dx, scale);
        heattype = (HeatMapRenderType) ktrack.getRenderType();
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
        g.drawLine(x, y - h, x, y);
        g.setColor(new Color(240, 240, 240));
        g.drawLine(x - w / 2, y - h, x - w / 2, y);

        super.draw(g0);
    }

    @Override
    protected void drawBin(double nrpixelperfeature, boolean drawall, Graphics g, KaryoFilter filter, FeatureTreeNode node, int nr, int w, int maxnr, int s, int e, int x, int y, int h) {
        // depends on filter mode! And wether we draw all or not
        g.setColor(ktrack.getColor());
        List<KaryoFeature> features = null;
        if (filter != null && filter.isValid()) {
            features = node.getFilteredFeatures(filter, true);
        } else {
            features = node.getAllFeatures(true);
        }
        drawHeatRects(features, filter, g, nrpixelperfeature, s, e, x - w / 2, y, h, w / 2, node, maxnr);

        //  drawVars(sneg, true, drawall, losses, filter, g, nrpixelperfeature, s, e, x - w / 2, startx, y, h, w / 2, node, maxnr);
    }

    protected void drawHeatRects(List<KaryoFeature> features, KaryoFilter filter, Graphics g, double nrpixelperfeature, int s, int e, int x, int y, int h, int w, FeatureTreeNode node, int maxnr) {
        int nr = features.size();

        for (int i = 0; i < nr; i++) {
            KaryoFeature f = features.get(i);

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
                Color heatcolor = heattype.getColor(ktrack.getMetaInfo(), f);
                g.setColor(heatcolor);
                int wb = (int) nrpixelperfeature;
                int y1 = (int) getHeight(s);
                int y2 = (int) getHeight(e);
                g.fillRect(x, y1 + y - h, wb, y2 - y1);
                g.drawRect(x, y1 + y - h, wb, y2 - y1);
            }
        }
    }

    @Override
    protected void p(String s) {
        System.out.println("GuiHeatMapTree:" + s);
    }
}
