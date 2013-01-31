/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.FeatureTree;
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
   
    public HeatMapRenderType() {
        super("HeatMap plot", "Plots the value of an attribute in varying colors, such as a locus score");
        this.setRelevantAttName("Score");
    }
    @Override
    public String getColorName() {
        return "High value color";
    }
    @Override
    public String getColor1Name() {
        return "Low value color";
    }
     // Low value color
    @Override
    public Color getDefaultColor1(KaryoTrack ktrack) {        
        return Color.white;
    }
    
    /** Gain color */
    @Override
    public Color getDefaultColor(KaryoTrack ktrack) {
        return Color.blue.darker();
    }
    @Override
    public boolean isClassSupported(Feature featureClass) {
        return (featureClass instanceof Variant)
                || (featureClass instanceof Segment)
                || (featureClass instanceof LocusScore);
    }
    @Override
    public GuiFeatureTree getGuiTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        if (getStartcolor() == null) setColor(this.getDefaultColor(ktrack).brighter());
        if (getEndcolor() == null) setColor1(this.getDefaultColor(ktrack).darker());
        FeatureMetaInfo.Range range = ktrack.getMetaInfo().getRangeForAttribute("Score");
        if (range == null) {
               err("Got no range for meta "+ ktrack.getMetaInfo()+"  and ktrack "+ktrack);
           
        }
        return new GuiHeatMapTree(ktrack, canvas, chromo, tree, dx);        
    }
    
    // TODO: use color gradient with multiple colors
    public Color getColor(FeatureMetaInfo meta, Feature f) {
       
        FeatureMetaInfo.Range range = meta.getRangeForAttribute("Score");
        if (range == null) {
           if (!ERR_SHOWN) {
               err("Got no range for meta "+ meta+"  and f "+f);
               ERR_SHOWN = true;
           }
            return Color.gray;
        }
        
        double score = getScore(f);
        
        double MAX = range.max;
        double MIN = range.min;
        
        double rangedelta = MAX-MIN;
        
        double dr = (color1.getRed()-color.getRed()) / (rangedelta);
        double dg = (color1.getGreen()-color.getGreen()) / (rangedelta);
        double db = (color1.getBlue()-color.getBlue()) / (rangedelta);
        
        double ds = score - MIN;
        
        int r = Math.min(255, Math.max(0,(int) (color.getRed()+dr * ds)));
        int g = Math.min(255, Math.max(0,(int) (color.getGreen()+dg * ds)));
        int b = Math.min(255, Math.max(0,(int) (color.getBlue()+db * ds)));
        Color c = new Color(r, g, b);
        return c;
    
    }
    public double getScore(Feature f) {
        double score = 0;
        if (f instanceof Variant) {
            Variant v = (Variant) f;            
            
            String sscore = v.getAttributeAsString(this.getRelevantAttName());
                 
            if (sscore != null) {
                try {
                    score = Double.parseDouble(sscore);
                }
                catch (Exception e) {
                    err("Could not parse score "+getRelevantAttName()+"="+sscore +" to string");
                }               
            }                                       
        }  
        else if (f instanceof Segment) {
            Segment v = (Segment) f;
            score = v.getScore();  
            
        } 
        else if (f instanceof LocusScore) {
            LocusScore v = (LocusScore) f;
            score = v.getScore();                           
        }    
        return score;
    }   
    
    private void err(String s) {
            Logger.getLogger("HeatMapRenderType").warn(s);
    }
    private void p(String s) {
            Logger.getLogger("HeatMapRenderType").info(s);
    }


    /**
     * @return the startcolor
     */
    public Color getStartcolor() {
        return color;
    }

   

    /**
     * @return the endcolor
     */
    public Color getEndcolor() {
        return color1;
    }

   
}
