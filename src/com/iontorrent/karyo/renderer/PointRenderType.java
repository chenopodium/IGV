/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiPointTree;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.data.seg.Segment;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;


/**
 *
 * @author Chantal
 */
public class PointRenderType extends RenderType {
    
    boolean errorShown = false;
   
    
    public PointRenderType(KaryoTrack ktrack, String name, String desc) {
        super(ktrack, name, desc, 3);
        
        //this.setRelevantAttName("COPYNR");
    }
    public PointRenderType(KaryoTrack ktrack) {
        super(ktrack, "Point plot", "Plots the value of an attribute, such as the copy number, in a point plot", 3);
        
        //this.setRelevantAttName("COPYNR");
    }
     @Override
    public String getColorName(int nr) {         
        if (nr <= 0) return "Neutral color";
        else if (nr == 1)return "High color for values > "+this.getParCutoffScore() ;
        else if (nr == 2)  return "Low color for values < "+this.getParCutoffScore();
        
        else return null;
    }
  @Override
     public String getColorShortName(int nr) {
        if (nr == 1)return ">"+(int)this.getParCutoffScore() ;
        else if (nr == 2)  return "<"+(int)this.getParCutoffScore();
        else return "="+(int)this.getParCutoffScore();
    }
      // TODO: use color gradient with multiple colors
    @Override
    public Color getColor(FeatureMetaInfo meta, KaryoFeature f) {        
        return super.getGradientColor(meta, f);
    }
    @Override
    public boolean isClassSupported(Feature featureClass) {
        return (featureClass instanceof Variant)
                || (featureClass instanceof LocusScore)
                || (featureClass instanceof Segment);
    }
    public boolean outlineOval(double min, double max, double cutoff, double score) {
        //p("PointRenderType: outlineOval is always false" );
         return false;
    }
    @Override
    public GuiFeatureTree getGuiTree(DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiPointTree(ktrack, canvas, chromo, tree, dx);        
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

    public double getMinPointHeight() {
        return 2;
    }
    public double getMinPointWidth() {
        return 1;
    }

    
}
