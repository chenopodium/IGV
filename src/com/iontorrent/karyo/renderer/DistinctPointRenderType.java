/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import java.awt.Color;
import org.apache.log4j.Logger;


/**
 *
 * @author Chantal
 */
public class DistinctPointRenderType extends PointRenderType {
    
    
    public DistinctPointRenderType(KaryoTrack ktrack) {
        super(ktrack, "Distinct Point plot", "Plots the value of an attribute, such as the copy number, in a point plot, but using distinct color and NOT a greadient");
        
        //this.setRelevantAttName("COPYNR");
    }
  
  
    @Override
    public Color getColor(FeatureMetaInfo meta, KaryoFeature f) {        
        return super.getDistinctColor(meta, f);
    }
    
    private void err(String s) {
            Logger.getLogger("DistinctPointRenderType").warn(s);
    }
    private void p(String s) {
            Logger.getLogger("DistinctPointRenderType").info(s);
    }
    
    @Override
     public double getMinPointHeight() {
        return 8;
    }
    @Override
    public double getMinPointWidth() {
        return 6;
    }
    @Override
 public boolean outlineOval(double min, double max, double cutoff, double score) {
      double delta = (max-min)/100.0;
    //  p("DistinctRenderType: outlineOval? min="+min+",max="+max+", cutoff="+cutoff+", score ="+score+", delta="+delta );
        if (Math.abs(cutoff - score) < delta) return false;
        else return true;
    }
   
}
