/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.karyo.drawables.GuiHeatMapTree;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import org.apache.log4j.Logger;
import org.broad.igv.data.seg.Segment;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;


/**
 *
 * @author Chantal
 */
public class HeatMapRenderType extends RenderType {
    
    static boolean ERR_SHOWN = false;
   
    int msgs = 0;
    public HeatMapRenderType(KaryoTrack ktrack) {
        super(ktrack, "HeatMap plot", "Plots the value of an attribute in varying colors, such as a locus score", 3);
        this.setRelevantAttName("Score");
    }
     @Override
    public String getColorName(int nr) {         
        if (nr <= 0) return "Neutral color";
        else if (nr == 1)return "High color for values > "+this.getCutoffScore() ;
        else if (nr == 2)  return "Low color for values < "+this.getCutoffScore();
        
        else return null;
    }
  @Override
     public String getColorShortName(int nr) {
        if (nr == 1)return ">"+(int)this.getCutoffScore() ;
        else if (nr == 2)  return "<"+(int)this.getCutoffScore();
        else return "="+(int)this.getCutoffScore();
    }
    @Override
    public boolean isClassSupported(Feature featureClass) {
        boolean ok = (featureClass instanceof Variant)
                || (featureClass instanceof Segment)
                || (featureClass instanceof LocusScore);
        
        if (!ok) p("HeatMap not supported for class "+featureClass.getClass().getName());
        return ok;
    }
    
   
    @Override
    public GuiFeatureTree getGuiTree( DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
       
        FeatureMetaInfo.Range range = ktrack.getMetaInfo().getRangeForAttribute("Score");
        if (range == null) {
               err("Got no range for meta "+ ktrack.getMetaInfo()+"  and ktrack "+ktrack);
           
        }
        return new GuiHeatMapTree(ktrack, canvas, chromo, tree, dx);        
    }
    
    @Override
    public Color getColor(FeatureMetaInfo meta, KaryoFeature f) {        
        return super.getGradientColor(meta, f);
    }
    
    
    private void err(String s) {
            Logger.getLogger("HeatMapRenderType").warn(s);
    }
    private void p(String s) {
        if (msgs < 100) {
            Logger.getLogger("HeatMapRenderType").info(s);
            msgs ++;
        }
    }


   
}
