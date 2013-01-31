/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.KaryoTrack;
import java.util.ArrayList;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal
 */
public class RenderManager {
    
    public RenderManager() {
        
    }
    public ArrayList<RenderType> getRenderTypes() {
        ArrayList<RenderType>  res = new ArrayList<RenderType> ();
        res.add(new RenderType());
        res.add(new HeatMapRenderType());
        res.add(new GainLossRenderType());
        res.add(new PointRenderType());
        return res;
    }
    
    public ArrayList<RenderType> getRenderTypes(ArrayList<RenderType> renderers, Feature f) {
        ArrayList<RenderType> res= new ArrayList<RenderType>();
        for (RenderType r: renderers) {
            if (r.isClassSupported(f)) res.add(r);
        }
        return res;
    }
    public ArrayList<RenderType> getRenderTypes(ArrayList<RenderType> renderers, KaryoTrack t) {
        ArrayList<RenderType> res= new ArrayList<RenderType>();
        Feature f=  t.getSampleafeture();
        
        for (RenderType r: renderers) {
            if (f != null && r.isClassSupported(f)) res.add(r);
            else res.add(r);
        }
        return res;
    }
}
