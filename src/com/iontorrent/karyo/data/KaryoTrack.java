/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.karyo.renderer.GainLossRenderType;
import com.iontorrent.karyo.renderer.HeatMapRenderType;
import com.iontorrent.karyo.renderer.PointRenderType;
import com.iontorrent.karyo.renderer.RenderType;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import java.util.ArrayList;
import org.broad.igv.track.AbstractTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class KaryoTrack {

    private AbstractTrack track;
    private Color color;
    private Feature sampleafeture;
    private KaryoFilter filter;
    private FeatureMetaInfo metainfo;
    private String shortname;
    private String name;
    private boolean visible;
    private RenderType renderType;
    private RenderType defaultRenderType;
    public KaryoTrack(AbstractTrack track) {
        this.track = track;
        visible = true;

        //track.getResourceLocator().getDescription();
        this.name = track.getName();
        int dot = name.indexOf(".");
        if (dot > 0) {
            name = name.substring(0, dot - 1);
        }
        String sname = null;
        if (sname == null && track.getName() != null) {
            sname = "" + Character.toUpperCase(track.getName().charAt(0));
        }
        this.shortname = sname;
    }

    public RenderType getDefaultRenderer() {
        if (track instanceof FakeCnvTrack) {
            defaultRenderType = new PointRenderType();
        } else if (track instanceof FakeIndelTrack) {
            defaultRenderType = new GainLossRenderType();       
        } else if (getMetaInfo() instanceof LocusScoreMetaInfo) {
            defaultRenderType = new PointRenderType();
        } else if (getMetaInfo() instanceof SegmentMetaInfo) {
            defaultRenderType = new HeatMapRenderType();
        } else {
            defaultRenderType = new RenderType();
        }
        return defaultRenderType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getShortName() {
        if (shortname == null) {
            shortname = name.substring(0, 1);
        }
        return shortname;
    }

//    public String getFilterString(String nl) {
//        String s = "";
//        ArrayList<KaryoFilter> filters = getPossibleFilters();
//        if (filters != null) {
//            for (KaryoFilter f : filters) {
//                if (f.isInitialized() && f.isValid()) {
//                    s += f.toString() + nl;
//                }
//            }
//        }
//        return s;
//    }
    public ArrayList<KaryoFilter> getPossibleFilters() {
        return FilterManager.getManager().getFiltersFor(getSampleafeture());
    }

    public FeatureMetaInfo getMetaInfo() {
        return this.getMetainfo();
    }

    /**
     * @return the filter
     */
    public KaryoFilter getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(KaryoFilter filter) {
        this.filter = filter;
    }

    /**
     * @return the track
     */
    public AbstractTrack getTrack() {
        return track;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        if (color == null) {
            color = this.getRenderType().getDefaultColor(this);
        }
        return color;
    }

    public void setRenderType(RenderType rtype) {
        this.renderType = rtype;
        color = renderType.getDefaultColor(this);
    }

    /**
     * @return the renderType
     */
    public RenderType getRenderType() {
        if (renderType == null) renderType = this.getDefaultRenderer();
        return renderType;
    }
    
    /**
     * @return the sampleafeture
     */
    public Feature getSampleafeture() {
        return sampleafeture;
    }

    /**
     * @param sampleafeture the sampleafeture to set
     */
    public void setSampleafeture(Feature sampleafeture) {
        this.sampleafeture = sampleafeture;
    }

    /**
     * @return the metainfo
     */
    public FeatureMetaInfo getMetainfo() {
        return metainfo;
    }

    /**
     * @param metainfo the metainfo to set
     */
    public void setMetainfo(FeatureMetaInfo metainfo) {
        this.metainfo = metainfo;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setVisible(boolean selected) {
        visible = selected;
    }
}
