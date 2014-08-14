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

import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.FeatureTreeNode;
import com.iontorrent.karyo.data.Range;
import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.karyo.renderer.PointRenderType;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.*;

import java.util.List;

public class GuiPointTree extends GuiFeatureTree {

    PointRenderType pointType;
    private int times;
// ***************************************************************************
// FROM DRAWABLE
// ***************************************************************************

    public GuiPointTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        this(ktrack, canvas, chromo, tree, dx, 1.0);
    }

    public GuiPointTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx, double scale) {
        super(ktrack, canvas, chromo, tree, dx, scale);
        pointType = (PointRenderType) ktrack.getRenderType();
        setWidth(Math.max(WIDTH, 2*((int) pointType.getMinPointWidth()+2)));
         update(tree);
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
        g.setColor(new Color(252, 252, 252));
        g.fillRect(x-w, y-h, w, h);
        g.setColor(new Color(250, 250, 250));
        g.drawRect(x-w, y-h, w, h);
        //g.drawLine(x - w, y - h, x - w, y);
        //g.drawLine(x, y - h, x, y);
        g.setColor(new Color(245, 245, 245));
        g.drawLine(x - w / 2, y - h, x - w / 2, y);
        super.draw(g0);

    }
    @Override
    protected void drawBuckets(int start, int delta, FeatureTreeNode[] nodes, double nrpixelperfeature, boolean drawall, Graphics2D g, KaryoFilter filter, int w, int maxnr, int x, int y, int h) {
        for (times = 0; times < 3; times++) {
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
    @Override
    protected void drawBin(double nrpixelperfeature, boolean drawall, Graphics g, KaryoFilter filter, FeatureTreeNode node, int nr, 
        int trackwidth, int maxnr, int starty, int endy, int trackstartx, int y, int h) {
        // depends on filter mode! And wether we draw all or not
        List<KaryoFeature> features = null;
        if (filter != null && filter.isValid()  && filter.isEnabled()) {
            features = node.getFilteredFeatures(filter, true);
           // p("Got "+features.size()+" FILTERED features");
        } else {
            features = node.getAllFeatures(true);
           // p("Got "+features.size()+" UNFILTERED features");
        }
        FeatureMetaInfo.Range r = super.ktrack.getMetaInfo().getRangeForAttribute(this.renderType.getRelevantAttName(), renderType.getKaryoScoreLabel());
        if (r == null && nrerrors < 100){
            p("=====Got no range for point plot and track "+ktrack.getTrackDisplayName()+", relevant field: "+this.renderType.getRelevantAttName()+", scorelabel="+renderType.getKaryoScoreLabel());
            nrerrors++;
            ktrack.getMetaInfo().showRanges();
            
        }

        boolean toleft = this.getX() < this.chromo.getX();
        int thistrackstartx = trackstartx- trackwidth;
        if (toleft) {
            thistrackstartx = trackstartx;
        }
       // to left if on left side of chromsome, to right otherwise
        drawVars(toleft, features, filter, g, nrpixelperfeature, starty, endy, thistrackstartx, y, h, trackwidth , node, r);

    }

    protected void drawVars(boolean toLeft, List<KaryoFeature> vars, KaryoFilter filter, Graphics g, 
            double nrpixelperfeature, int starty, int endy, int trackstartx, int y, int h, int trackwidth, FeatureTreeNode node, FeatureMetaInfo.Range range) {
        int nr = vars.size();
     //   int wb = 2;
        Graphics2D gg = (Graphics2D)g;
        double min = 0; 
        double max = 100;
        
    //    p("drawVars. PoinType = "+pointType.getClass().getName());
                
        if (range != null) {
            min = range.min;
            max = range.max;
        }
        
        
        
        if (max == min) {
//           if (nrerrors < 10) {
//               p("=== Max = min.... Setting max to min+1");
//               nrerrors++;
//           }         
            max = min+1;
            min = min-1;
        }
        double dxpixels = Math.max(0.0000001, (double)trackwidth/2/(double)(max-min+1));
          
        int minheight = (int) pointType.getMinPointHeight();
        int minwidth = (int) pointType.getMinPointWidth();
        for (int times = 0; times < 3; times++) {
            for (int i = 0; i < nr; i++) {
                KaryoFeature f = vars.get(i);
                double score = f.getScore(super.ktrack.getMetaInfo(), this.renderType.getRelevantAttName());
                if (score < min || score > max) {
                    p(this.ktrack.getTrackName()+": SCORE OUTSIDE OF ATT RANGE: "+score+", min="+min+", max="+max);
                    if (range == null) {
                        range = new FeatureMetaInfo.Range();
                    }
                    if (range != null) {
                        range.add(score);
                        min = range.min;
                        max = range.max;
                    }
                    
                }
                double cutoff = renderType.getCutoffScore(f, this.getTree().getSource());
                if ( ( times == 0 && score == cutoff ) || 
                        (times ==1 && score != cutoff ) || 
                        ( times ==2 &&  pointType.outlineOval(min, max, cutoff, score))) {
                    score = score - min;

                    Color c = pointType.getColor(ktrack.getMetaInfo(), f);

                    gg.setPaint(c);

                  //  p("Got color: "+c+" for feature "+f+", times="+times+", cutoff="+cutoff+", meta="+ktrack.getMetaInfo()+", score="+f.getScore(ktrack.getMetaInfo(), ktrack.getMetaInfo().getScoreFieldName(f))+", rendertype ="+renderType.getClass().getName());
                    
                  //  pointType.debug = true; 
                    
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
                        int y1 = (int) getHeight(f.getStart())+y-h;
                        int y2 = (int) getHeight(f.getEnd())+y-h;

                        int dx = (int)(dxpixels*score);
                        if (nrerrors < 50 && times == 2) {
                       //     p(this.ktrack.getTrackName()+"/"+this.renderType.getClass().getName()+": f="+f.toString()+", y1-y2="+y1+"-"+y2);
                            nrerrors++;
                        }
                        //int dy = Math.max(wb, y2 - y1);
                        int r = minwidth;
                        int rh = Math.max(minheight, (y2-y1));
                        
                        if (times == 2) {
                            r = minwidth+2;
                            rh = Math.max(minheight+2, (y2-y1));
                        }
                        int my = y1-r/4;
                         if (toLeft) {
                             int thisx = trackstartx -r;
                             
                             g.fillRoundRect(thisx - (int) dx, my, r,rh, r, r/2);
                           // g.fillOval(trackstartx - (int) dx, my, r,rh);
                           if (times == 2 && r>3)  {
                                gg.setPaint(c.darker());
                                g.drawRoundRect(thisx - (int) dx,my, r,rh, r, r/2);
                               // g.drawOval(trackstartx - (int) dx,my, r,rh);
                            }

                        } else {
                              g.fillRoundRect(trackstartx + (int) dx, my, r,rh, r, r/2);
                            //g.fillOval(trackstartx + (int) dx,my, r,rh);
                            if (times == 2 && r>3)  {
                                gg.setPaint(c.darker());
                                //g.drawOval(trackstartx +(int) dx,my, r,rh);
                                g.drawRoundRect(trackstartx + (int) dx,my, r,rh, r, r/2);
                            }
                        }
                    }
                   // else p("Not drawing feature, was filtered OUT");
                }
            }
        }
    }

    @Override
    protected void p(String s) {
        System.out.println("GuiPointTree:" + s);
    }
}
