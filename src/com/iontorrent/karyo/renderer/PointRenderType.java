/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiPointTree;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.views.basic.DrawingCanvas;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;


/**
 *
 * @author Chantal
 */
public class PointRenderType extends RenderType {
    
    boolean errorShown = false;
    
    public PointRenderType() {
        super("Point plot", "Plots the value of an attribute, such as the copy number, in a point plot");
        this.setRelevantAttName("COPYNR");
    }
    
    @Override
    public boolean isClassSupported(Feature featureClass) {
        return (featureClass instanceof Variant)
                || (featureClass instanceof LocusScore);
    }
    @Override
    public GuiFeatureTree getGuiTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiPointTree(ktrack, canvas, chromo, tree, dx);        
    }
    public double getScore(Feature f) {
        if (f instanceof Variant) {
            Variant v = (Variant) f;
            int copynr = -1;
            String scopynr = v.getAttributeAsString(getAttname());
            if (scopynr != null && scopynr.length()>0 && !scopynr.equals("null")) {
                try {
                    copynr = Integer.parseInt(scopynr);
                }
                catch (Exception e) {
                    err("Could not parse copy nr "+scopynr +" to string");
                }
                return copynr;
            }  
            else {
                if (!errorShown) {
                    errorShown = true;
                    p("Variant "+v+" has no "+getAttname()+" attribute");
                    Map<String, Object> map = v.getAttributes();
                    Iterator it = map.keySet().iterator();
                    p("Got attributes: ");
                    for (; it.hasNext(); ) {
                        String key = (String) it.next();
                        p(key+"="+map.get(key));
                    }
                }
            }
                       
        }    
        else if (f instanceof LocusScore) {
            LocusScore v = (LocusScore) f;
            return v.getScore();                           
        }    
        return 0;
    }   
    
    private void err(String s) {
            Logger.getLogger("PointRenderType").warn(s);
    }
    private void p(String s) {
            Logger.getLogger("PointRenderType").info(s);
    }

    /**
     * @return the attname
     */
    public String getAttname() {
        return super.getRelevantAttName();
    }

    
}
