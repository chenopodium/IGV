/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FakeCnvTrack;
import com.iontorrent.karyo.data.FakeIndelTrack;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.data.LocusScoreMetaInfo;
import com.iontorrent.karyo.data.SegmentMetaInfo;
import com.iontorrent.karyo.views.GuiProperties;
import com.iontorrent.utils.GuiUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.track.AbstractTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal
 */
public class RenderManager {

    static GuiProperties gui;
    static final boolean STORE_PREFERENCES = false;

   static RenderManager manager;
    
    public static RenderManager getManager() {
        if (manager == null) manager = new RenderManager();
        return manager;
    }

    public static void saveGuiProperties() {
        if (STORE_PREFERENCES) {
            GuiUtils.showNonModalMsg("Saving gui preferences ", false, 5);
            getGuiProperties();
            Map<String,String> userprop = PreferenceManager.getInstance().getProperties();
            for (Iterator<String> it = gui.keys(); it != null && it.hasNext();){
                 String key = it.next().toString().toUpperCase();
                 String value = gui.get(key);
                 p("Overwriting USER PROP "+key+" with GUI preference "+value);
                 userprop.put(key, value);             
            }        
        }
    }
    
    private RenderManager() {
    }

    public static GuiProperties getGuiProperties() {
         if (gui == null) {
             gui = new GuiProperties(PreferenceManager.getInstance().getTempProperties());
             if (STORE_PREFERENCES) {
             // now add any stored properties!
             Map<String,String> userprop = PreferenceManager.getInstance().getProperties();
             for (Iterator it = userprop.keySet().iterator(); it != null && it.hasNext();) {
                 String key = it.next().toString().toUpperCase();
                 if (gui.containsKey(key)) {                     
                     String value = userprop.get(key);
                     p("Overwriting GUI key "+key+" with user preference "+value);
                     gui.put(key, value);
                 }
             }
             }
        }
         return gui;
    }
    public ArrayList<RenderType> getRenderTypes(KaryoTrack t) {
        ArrayList<RenderType> res = new ArrayList<RenderType>();
        res.add(new CnvRenderType(t));
        res.add(new RenderType(t));
        res.add(new HeatMapRenderType(t));
        res.add(new GainLossRenderType(t));
        res.add(new PointRenderType(t));
        res.add(new DistinctPointRenderType(t));
        return res;
    }

    private static void p(String s) {
        Logger.getLogger("RenderManager").info(s);;
    }
    public static RenderType getDefaultRenderer(KaryoTrack ktrack, AbstractTrack track) {
        String ftype = ktrack.getFileExt();
        String fname = ktrack.getLastPartOfFile();
        // first check gui properties!
        RenderType r = null;
        getGuiProperties();
        String R = gui.getKaryoRenderer(ktrack.getSample(), fname, ftype);

      
        
        if (R == null) {
            if (ftype.equalsIgnoreCase("bed")) {
                R = "HISTOGRAM";
            }
        }
        String n = ktrack.getTrackDisplayName().toLowerCase();
        if (R != null) {
            if (R.equalsIgnoreCase("SCATTER_PLOT")) {
                r = new PointRenderType(ktrack);
            } else if (R.startsWith("DISTINCT")) {
                if (n.contains("coverage")) r = new PointRenderType(ktrack);
                else r = new DistinctPointRenderType(ktrack);
            } else if (R.equalsIgnoreCase("HEATMAP")) {
                r = new HeatMapRenderType(ktrack);
            } else if (R.equalsIgnoreCase("CNV")) { 
                r = new CnvRenderType(ktrack);
            } else if (R.equalsIgnoreCase("HISTOGRAM")) {
                r = new RenderType(ktrack);
            } else if (R.equalsIgnoreCase("GAIN_LOSS")) {
                r = new GainLossRenderType(ktrack);
            }
        }

        if (r == null) {

            if (track instanceof FakeCnvTrack) {
                r = new CnvRenderType(ktrack);
            } else if (track instanceof FakeIndelTrack) {
                r = new GainLossRenderType(ktrack);

            } else if (ktrack.getMetaInfo() instanceof LocusScoreMetaInfo) {
                r = new HeatMapRenderType(ktrack);
            } else if (ktrack.getMetaInfo() instanceof SegmentMetaInfo) {
                if (ktrack.getLastPartOfFile() != null && ktrack.getLastPartOfFile().contains("cover")) {
                    r = new HeatMapRenderType(ktrack);  
                }
                else {
                    r = new CnvRenderType(ktrack);
                }
            } else {
                
                if (n.contains("cover")) {
                    r = new HeatMapRenderType(ktrack);
                } else if (n.contains("cnv")) {
                    r = new CnvRenderType(ktrack);
                } else {
                    r = new RenderType(ktrack);
                }
            }
        }

        if (ktrack.getRendererTypes() != null) {
            for (RenderType tr : ktrack.getRendererTypes()) {
                if (tr.getClass() == r.getClass()) {
                    r = tr;
                }
            }
        }
        for (int i = 0; i < r.getNrColors(); i++) {
            if (r.getColor(i) == null) {
                r.setColor(r.getDefaultColor(i), i);
            }
        }
         p("Getting default renderer for "+fname +" and type "+ftype+", R="+R+"  => "+r.getClass().getName()+", nrcolors="+r.getNrColors()+", color(0)="+r.getColor(0));
        return r;
    }

    public ArrayList<RenderType> getRenderTypes(ArrayList<RenderType> renderers, KaryoTrack t) {
        ArrayList<RenderType> res = new ArrayList<RenderType>();
        Feature f = t.getSampleafeture();

        for (RenderType r : renderers) {
            if (f != null && r.isClassSupported(f)) {
                res.add(r);
            } else {
                res.add(r);
            }
            for (int i = 0; i < r.getNrColors(); i++) {
                if (r.getColor(i) == null) {
                    r.setColor(r.getDefaultColor(i), i);
                }
            }
        }
        return res;
    }
}
