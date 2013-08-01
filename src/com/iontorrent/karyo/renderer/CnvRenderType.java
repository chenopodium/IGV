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
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;

/** 
 *
 * @author Chantal
 */
public class CnvRenderType extends RenderType{

   

    public CnvRenderType(KaryoTrack ktrack) {
        super(ktrack,"CNV Histogram", "CNV Histogram", "Histogram, but using different colors for score >2, scores <2, and scores =2", 3);
    }

    @Override
    public String getColorName(int nr) {         
        if (nr <= 0) return "Neutral color";
        else if (nr == 1)return "Color for CNV > "+this.getParCutoffScore() ;
        else if (nr == 2)  return "Color for CNV < "+this.getParCutoffScore();
        
        else return null;
    }
    @Override
     public String getColorShortName(int nr) {
        if (nr == 1)return "Gain" ;
        else if (nr == 2)  return "Loss";
        else return null;
    }
   
 
     // TODO: use color gradient with multiple colors
    @Override
    public Color getColor(FeatureMetaInfo meta, KaryoFeature f) {
        if (f.isInsertion(this.getCutoffScore(f))) return this.getColor(1);
        else if (f.isDeletion(this.getCutoffScore(f))) return this.getColor(2);
        return this.getColor(0);
        //return c;
    }
    @Override
    public boolean drawFeature(KaryoFeature f) {
        if (f.isInsertion(this.getCutoffScore(f))) return true;
        else if (f.isDeletion(this.getCutoffScore(f))) return true;
        else return true;
    }
   

    @Override
    public GuiFeatureTree getGuiTree(DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiFeatureTree(ktrack, canvas, chromo, tree, dx);
    }

   
}
