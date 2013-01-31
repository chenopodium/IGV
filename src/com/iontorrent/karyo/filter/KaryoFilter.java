/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.FeatureTreeNode;
import com.iontorrent.karyo.data.KaryoFeature;
import java.awt.Color;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public abstract class KaryoFilter {

     private boolean initialized;
    private Color filteredColor = Color.red.darker();
    private Color nonfilteredColor = Color.blue.darker();
    
    protected String name;
    protected String fieldname;
    protected double treshold;
    protected boolean not;
    
    protected FilterMode filterMode = FilterMode.HIGHLIGHT_FILTERED;

    /**
     * @return the initialized
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * @param initialized the initialized to set
     */
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public static enum FilterMode {

        REMOVE_FILTERED,
        SHOW_FILTERED,
        HIGHLIGHT_FILTERED;
    }
   

    public abstract KaryoFilter copy();
    
    @Override
    public String toString() {
        String s = "";
        if (this.isHighlightFiltered()) s = "Highlight areas that have features where ";
        else if (this.isRemoveFiltered())s = "Hide features where ";
        else s = "Show features where the ";
        s += fieldname;
        if (not) s += " is smaller or equal to ";
        else s +=" is larger than ";
        s += treshold;
        s += " (filter mode: "+filterMode.name()+")";
        return s;
    }

    public abstract boolean isValid();
    
    public abstract boolean filter(KaryoFeature f);

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    public FilterMode getFilterMode() {
        return filterMode;
    }

    public boolean isHighlightFiltered() {
        return filterMode == FilterMode.HIGHLIGHT_FILTERED;
    }

    public boolean isRemoveFiltered() {
        return filterMode == FilterMode.REMOVE_FILTERED;
    }

    public abstract boolean isForFeature(Feature feature);

    /**
     * @return the description
     */
    public String getDescription() {
        return toString();
    }

    /**
     * @return the fieldname
     */
    public String getFieldname() {
        return fieldname;
    }

    /**
     * @return the treshold
     */
    public double getTreshold() {
        return treshold;
    }

    /**
     * @param treshold the treshold to set
     */
    public void setTreshold(double treshold) {
        this.treshold = treshold;
        this.initialized = true;
    }
     /**
     * @return the not
     */
    public boolean isNot() {
        return not;
    }

    public Color getFilteredColor() {
        return this.filteredColor;
    }
    public Color getNonFilteredColor() {
        return this.nonfilteredColor;
    }
    public Color getFilteredColor(FeatureTreeNode node) {
        if (node.filter(this) > 0) return getFilteredColor();
        else return getNonFilteredColor();
    }
    public Color getFilteredColor(KaryoFeature f) {
        return getFilteredColor();
    }
    /**
     * @param not the not to set
     */
    public void setNot(boolean not) {
        this.not = not;
    }

    /**
     * @param filteredColor the filteredColor to set
     */
    public void setFilteredColor(Color filteredColor) {
        this.filteredColor = filteredColor;
    }

    /**
     * @return the nonfilteredColor
     */
    public Color getNonfilteredColor() {
        return nonfilteredColor;
    }

    /**
     * @param nonfilteredColor the nonfilteredColor to set
     */
    public void setNonfilteredColor(Color nonfilteredColor) {
        this.nonfilteredColor = nonfilteredColor;
    }

  
}
