/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.karyo.drawables.GuiIndelTree;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import org.apache.log4j.Logger;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;


/**
 *
 * @author Chantal
 */
public class GainLossRenderType extends RenderType {
    
    public static final String GAIN = "gain";
    public static final String LOSS = "loss";
    public static final String UNKNOWN = "UNKNOWN";
    
   
    public GainLossRenderType() {
        super("Gain/Loss histogram", "Two histograms, one for gains, one for losses, such as for INDELS or other similar types");
        this.setRelevantAttName("INDELTYPE");
    }
    
    @Override
    public String getColorName() {
        return "Gain color";
    }
    @Override
    public String getColor1Name() {
        return "Loss color";
    }
    // LOSS color
    @Override
    public Color getDefaultColor1(KaryoTrack ktrack) {
        String type = ktrack.getName().toUpperCase();
        if (type.indexOf("SNP") > -1) {
            return Color.yellow.brighter();
        } else if (type.indexOf("INDEL") > -1) {
            return Color.red.darker();        
        } else if (type.indexOf("CNV") > -1) {
            return Color.yellow.brighter();       
        } else if (type.indexOf("COVERAGE") > -1) {
            return Color.gray;
        } else {
            return Color.red.darker();
        }
    }
    
    /** Gain color */
    @Override
    public Color getDefaultColor(KaryoTrack ktrack) {
        String type = ktrack.getName().toUpperCase();
        if (type.indexOf("SNP") > -1) {
            return Color.blue.darker();
        } else if (type.indexOf("INDEL") > -1) {
            return Color.green.darker();        
        } else if (type.indexOf("CNV") > -1) {
            return new Color(200, 50, 240);       
        } else if (type.indexOf("COVERAGE") > -1) {
            return Color.gray;
        } else {
            return Color.green.darker();
        }
    }
    @Override
    public boolean isClassSupported(Feature featureClass) {
        return (featureClass instanceof Variant);
               // || (featureClass instanceof Variant);
    }
    @Override
    public GuiFeatureTree getGuiTree(KaryoTrack ktrack, DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiIndelTree(ktrack, canvas, chromo, tree, dx);        
    }
    public String getGainType(Feature f) {
        if (f instanceof Variant) {
            Variant v = (Variant) f;
            String att = v.getAttributeAsString(this.getRelevantAttName());
            if (att == null)  {
                String scopynr = v.getAttributeAsString("COPYNR");
                int copynr = -1;
                if (scopynr != null) {
                    try {
                        copynr = Integer.parseInt(scopynr);
                    }
                    catch (Exception e) {
                        err("Could not parse copy nr "+scopynr +" to string");
                    }
                    if (copynr > 1) return GAIN;
                    else if (copynr >-1) return LOSS;
                }                
            }
            else {
                if (att.equalsIgnoreCase("GAIN"))return GAIN;
                else if (att.equalsIgnoreCase("LOSS")) return LOSS;            
            }
        }       
        return UNKNOWN;
    }   
    
    private void err(String s) {
            Logger.getLogger("GainLossRenderType").warn(s);
    }
    private void p(String s) {
            Logger.getLogger("GainLossRenderType").info(s);
    }
}
