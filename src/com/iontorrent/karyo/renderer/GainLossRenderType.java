/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.karyo.drawables.GuiIndelTree;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import java.util.Iterator;
import org.apache.log4j.Logger;
import org.broad.igv.data.seg.Segment;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;


/**
 *
 * @author Chantal
 */
public class GainLossRenderType extends RenderType {
    
    public static final String GAIN = "gain";
    public static final String LOSS = "loss";
    public static final String NEUTRAL = "neutral";
    public static final String UNKNOWN = "UNKNOWN";
    
   
    public GainLossRenderType(KaryoTrack ktrack) {
        super(ktrack, "Gain/Loss histogram", "Two histograms, one for gains, one for losses, such as for INDELS or other similar types", 3);
        this.setRelevantAttName("INDELTYPE");
    }
    
    @Override
    public String getColorName(int nr) {         
        String rel = "";
        if (this.getRelevantAttName() != null) rel = "based on "+rel+" or ";
        
        if (nr == 0) return "Neutral color at "+this.getCutoffScore();
        else if (nr == 1)return "Gain color ("+rel+"score > "+this.getCutoffScore()+")" ;
        
        else if (nr == 2)  return "Loss color ( "+rel+"score < "+this.getCutoffScore()+")";        
        else return null;
    }
    @Override
     public String getColorShortName(int nr) {
        if (nr == 1)return "Gain" ;
        else if (nr == 2)  return "Loss";
        else return null;
    }
   
    @Override
    public boolean isClassSupported(Feature featureClass) {
        return (featureClass instanceof Variant)
                || (featureClass instanceof Segment);
    }
    @Override
    public GuiFeatureTree getGuiTree(DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiIndelTree(ktrack, canvas, chromo, tree, dx);        
    }
    
    public String getGainType(KaryoFeature f) {
        Feature feature = f.getFeature();
     //   p("getGainType: att name="+this.getRelevantAttName()+" for "+feature.getClass().getName());
        if (feature instanceof Variant) {
            Variant var = (Variant)feature;
            String rel = this.getRelevantAttName();
            String indeltype = f.getAttribute(var,rel);
          //  p("Got "+rel+" "+indeltype);
            
            if (indeltype != null) {
           //     p("Got indeltype from "+rel+":"+indeltype);
                if (indeltype.contains("INS")) return GAIN;
                else if (indeltype.contains("DEL")) return LOSS;
                else return NEUTRAL;
            }            
        }
        if (f.isInsertion(this.getCutoffScore())) return GAIN;
        else if (f.isDeletion(this.getCutoffScore())) return LOSS;
        else return NEUTRAL;               
    }
    
    private void err(String s) {
            Logger.getLogger("GainLossRenderType").warn(s);
    }
    private void p(String s) {
            Logger.getLogger("GainLossRenderType").info(s);
    }
}
