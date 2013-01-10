/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import com.iontorrent.data.karyo.filter.KaryoFilter;
import java.awt.Color;
import java.util.ArrayList;
import org.broad.igv.track.FeatureTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class KaryoTrack {
    private FeatureTrack track;
    private Color color;
    private String renderType;
    private Feature sampleafeture;
    private KaryoFilter filter;
    private FeatureMetaInfo metainfo;
    private String shortname;
    private String name;
    boolean visible = true;
    
    public KaryoTrack(FeatureTrack track, String shortname) {
        this.track = track;
        this.color = getDefaultColor();
        this.name = track.getName();
        int dot = name.indexOf(".");
        if (dot > 0) {
            name = name.substring(0, dot-1);
        }
        this.shortname = shortname;
    }
    public String getName() {
        return name;
    }
    public String toString() {
        return getName();
    }
    public String getShortName() {
        if (shortname == null) shortname = name.substring(0,1);
        return shortname;
    }
    public String getFilterString(String nl) {
        String s = "";
        ArrayList<KaryoFilter>  filters = getPossibleFilters();
        if (filters != null) {
            for (KaryoFilter f: filters) {
                if (f.isInitialized() && f.isValid()) {
                    s += f.toString()+nl;
                }
            }
        }
        return s;
    }
    public ArrayList<KaryoFilter> getPossibleFilters() {
        
        return FilterManager.getManager().getFiltersFor(getSampleafeture());
    }
     public FeatureMetaInfo getMetaInfo() {
        return this.getMetainfo();
    }
    public Color getDefaultColor() { 
        String type = getTrack().getName().toUpperCase();
        if (type.indexOf("SNP")>-1) {
            return Color.blue.darker();
        } else if (type.indexOf("INDEL")>-1) {
            return Color.red.darker(); 
         } else if (type.indexOf("INSERTION")>-1) {
            return Color.green.darker(); 
         } else if (type.indexOf("DELETION")>-1) {
            return Color.red.darker(); 
        } else if (type.indexOf("CNV")>-1) {
            return new Color(200,50,200);  
        } else if (type.indexOf("EXOME")>-1) {
            return Color.green.darker();    
            
        } else {
            return Color.orange;
        }
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
    public FeatureTrack getTrack() {
        return track;
    }

    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

    /**
     * @return the renderType
     */
    public String getRenderType() {
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
    
}
