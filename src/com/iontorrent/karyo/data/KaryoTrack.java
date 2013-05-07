/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.karyo.renderer.RenderManager;
import com.iontorrent.karyo.renderer.RenderType;
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
    private String trackdispname;
    private boolean visible;
    private RenderType renderType;
    private RenderType defaultRenderType;
    private ArrayList<RenderType> rendererTypes;
    private String sample;
    
    
    public KaryoTrack(AbstractTrack track, int nr) {
        this.track = track;
        visible = true;

        //track.getResourceLocator().getDescription();
        this.trackdispname = track.getDisplayName();
        this.sample = track.getSample();
        p("Track "+trackdispname+" sample: "+sample);
        if (track.getResourceLocator() != null) {
            p("Resource sample: "+track.getResourceLocator().getSampleId());
            sample = track.getResourceLocator().getSampleId();
        }
        int dot = trackdispname.indexOf(".");
        if (dot > 0) {
            trackdispname = trackdispname.substring(0, dot - 1);
        }
      //  if (sample != null) trackname= sample+" "+trackname;
        String sname = null;
        if (sname == null && track.getDisplayName() != null) {
            sname = "" + nr;
            //sname = "" + Character.toUpperCase(track.getName().charAt(0));
        }
        this.shortname = sname;
    }
    public String getSample() {
        return sample;
    }
     public String getGuiKey() {
       // return ktrack.getLastPartOfFile();
         return getTrackName();
    }
    public String getGuiSample() {
       // return ktrack.getLastPartOfFile();
         return getSample();
    }
   
    /** return file ending */
    public String getFileExt() {
        if (track == null || track.getResourceLocator() == null) return "?";
        String path = this.track.getResourceLocator().getPath();
        if (path == null) return "?";
        int dot = path.lastIndexOf(".");
        if (dot<0) return "?";
        return path.substring(dot+1);
    }
    public String getTrackName() {
        if (track == null) return "?";
        return track.getName();
    }
    public String getLastPartOfFile() {
        if (track == null || track.getResourceLocator() == null) return "?";
        String name = this.track.getResourceLocator().getFileName();
        if (name == null) return "?";
        int dot = name.lastIndexOf(".");
        if (dot<0) return name;
        return name.substring(0, dot);
    }
    
    public void setTrackDisplayName(String name) {
        this.trackdispname = name;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getTrackDisplayName() {
        return trackdispname;
    }

    @Override
    public String toString() {
        return getTrackDisplayName();
    }

    public String getShortName() {
        if (shortname == null) {
            shortname = trackdispname.substring(0, 1);
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
        return color;
    }

    public void setRenderType(RenderType rtype) {
        this.renderType = rtype;
        p("Render type of track " + this.getTrackDisplayName() + " is now " + rtype.getName());
        color = renderType.getDefaultColor(0);
    }

    public void p(String s) {
        System.out.println("KaryoTrack: " + s);
    }

    /**
     * @return the renderType
     */
    public RenderType getRenderType() {
        if (renderType == null) {
            p("renderType is null for "+this.getTrackName()+"getting default render type");
            renderType = getDefaultRenderer();
        }
        return renderType;
    }
    public RenderType getDefaultRenderer() {
         renderType = RenderManager.getDefaultRenderer(this, track);
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

    /**
     * @return the rendererTypes
     */
    public ArrayList<RenderType> getRendererTypes() {
        return rendererTypes;
    }

    /**
     * @param rendererTypes the rendererTypes to set
     */
    public void setRendererTypes(ArrayList<RenderType> rendererTypes) {
        this.rendererTypes = rendererTypes;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return track.getTrackorder();
    }

    /**
     * @param order the order to set
     */
    public void setOrder(int order) {
        track.setTrackorder(order);
    }
}
