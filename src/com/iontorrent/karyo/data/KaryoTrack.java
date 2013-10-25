/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.karyo.filter.KaryoFilter.FilterMode;
import com.iontorrent.karyo.filter.LocusScoreFilter;
import com.iontorrent.karyo.filter.VariantAttributeFilter;
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
    private boolean male;
    private boolean female;
    private ArrayList<KaryoFilter> possibleFilters;

    public KaryoTrack(AbstractTrack track, int nr) {
        this.track = track;
        visible = true;

        //track.getResourceLocator().getDescription();
        this.trackdispname = track.getDisplayName();
        this.sample = track.getSample();
        p("Track " + trackdispname + " sample: " + sample);
        if (track.getResourceLocator() != null) {
            p("Resource sample: " + track.getResourceLocator().getSampleId());
            sample = track.getResourceLocator().getSampleId();
        }
        int dot = trackdispname.lastIndexOf(".");
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

    /**
     * return file ending
     */
    public String getFileExt() {
        if (track == null || track.getResourceLocator() == null) {
            return "?";
        }
        String path = this.track.getResourceLocator().getPath();
        if (path == null) {
            return "?";
        }
        int dot = path.lastIndexOf(".");
        if (dot < 0) {
            return "?";
        }
        return path.substring(dot + 1);
    }

    public String getTrackName() {
        if (track == null) {
            return "?";
        }
        return track.getName();
    }

    public String getLastPartOfFile() {
        if (track == null || track.getResourceLocator() == null) {
            return "?";
        }
        String name = this.track.getResourceLocator().getFileName();
        if (name == null) {
            return "?";
        }
        int dot = name.lastIndexOf(".");
        if (dot < 0) {
            return name;
        }
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
        return getShortName() + ":" + getTrackDisplayName();
    }

    public String getShortName() {
        if (shortname == null) {
            shortname = trackdispname.substring(0, 1);
        }
        return shortname;
    }

    public ArrayList<KaryoFilter> getPossibleFilters() {
        if (possibleFilters != null) {
            return possibleFilters;
        }

        possibleFilters = FilterManager.getManager().getFiltersFor(getSampleafeture());

        // p("========================= Getting possible filters");
        // currently it should just be one :-)
        if (possibleFilters.size() > 0) {
            p("========================= Getting possible filters - CALL ONLY ONCE FOR EACH NEW TRACK");
            KaryoFilter fil = possibleFilters.get(0);
            String fkey = this.renderType.getKaryoFilterKey();
            String op = renderType.getKaryoFilterOperator();
            double fvalue = renderType.getKaryoFilterValue();
            String mode = renderType.getKaryoFilterMode();

//            p(" =+++ FOR TESTING, USING CONFIDENCE ");
//            fkey = "CONFIDENCE";
//            op = ">";
//            fvalue = 10;
            // p("Got possible filter: "+fil.toString()+", fil is "+fil.getClass().getName());

            if (fkey != null && !fkey.equals("null")) {
                p("========== GOT FILTER SETTINGS FOR " + this.getTrackDisplayName() + ": fkey=" + fkey + ", op=" + op + ", val=" + fvalue + ", mode=" + mode);
                if (op == null) {
                    op = ">";
                }
                if (op.startsWith("gr") || op.equalsIgnoreCase("gt")) {
                    op = (">");
                } else if (op.startsWith("le") || op.equalsIgnoreCase("lt")) {
                    op = ("<");
                } else if (op.startsWith("eq")) {
                    op = ("=");
                } else if (op.startsWith("not")) {
                    op = ("<>");
                } else if (op.startsWith(">")) {
                    op = (">");
                } else {
                    op = (op);
                }

                p("================ getPossibleFilters: Found a filter " + fil.getClass().getName() + ", key for this track " + this.getTrackName() + ":" + this.renderType.getKaryoFilterKey());
                if (fil instanceof LocusScoreFilter) {
                    LocusScoreFilter sfil = (LocusScoreFilter) fil;
                    FeatureMetaInfo.Range r = sfil.getRange();
                    if (op.equals("=")) {
                        r.min = fvalue;
                        r.max = fvalue;
                    } else if (op.equals(">")) {
                        r.min = fvalue;
                        r.max = Double.NaN;
                    } else if (op.equals("<")) {
                        r.min = Double.NaN;;
                        r.max = fvalue;
                    }
                    String scorename = this.renderType.getKaryoScoreName();
                    sfil.setScorename(scorename);
                    p("===================== Got a LocusScoreFilter:" + sfil);
                } else if (fil instanceof VariantAttributeFilter) {
                    VariantAttributeFilter vfil = (VariantAttributeFilter) fil;
                    vfil.setAttname(fkey);
                    vfil.setEnabled(true);
                    vfil.setAttvalue("" + fvalue);
                    vfil.setOperator(op);
                    p("===================== Got an VariantAttributeFilter:" + vfil);
                } else {
                    p("PROBLEM: Got other filter, don't know what to do " + fil);
                }
                fil.setFilterMode(FilterMode.SHOW_FILTERED);
                if (mode != null && mode.length() > 0) {
                    if (mode.equalsIgnoreCase("SHOW_FILTERED")) {
                        fil.setFilterMode(FilterMode.SHOW_FILTERED);
                    } else if (mode.equalsIgnoreCase("REMOVE_FILTERED")) {
                        fil.setFilterMode(FilterMode.REMOVE_FILTERED);
                    } else if (mode.equalsIgnoreCase("HIGHLIGHT_FILTERED")) {
                        fil.setFilterMode(FilterMode.HIGHLIGHT_FILTERED);
                    }
                }
            }
            else p("Got possible filters, but NO predefined settings from gui.properties");
            return possibleFilters;
        } else {
            return null;
        }
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

    public void resetFilter() {
        if (filter != null) {
            filter.resetFilterCount();
        }
    }

    public int getFilterCount() {
        if (filter != null) {
            return filter.getFilterCount();
        } else {
            return 0;
        }
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
            p("renderType is null for " + this.getTrackName() + "getting default render type");
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

    /**
     * @return the male
     */
    public boolean isMale() {
        return male;
    }

    /**
     * @param male the male to set
     */
    public void setMale(boolean male) {
        this.male = male;
    }

    /**
     * @return the female
     */
    public boolean isFemale() {
        return female;
    }

    /**
     * @param female the female to set
     */
    public void setFemale(boolean female) {
        this.female = female;
    }
}
