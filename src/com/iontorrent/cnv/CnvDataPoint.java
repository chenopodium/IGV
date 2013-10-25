/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import java.util.HashMap;

/**
 *
 * @author Chantal
 */
public class CnvDataPoint {
    long pos;
    double ratio;
    String clone;
    long end;
    int chr;
    private PlotType type;
    HashMap<String,String> atts;
    /**
     * @return the type
     */
    public PlotType getType() {
        return type;
    }

    public HashMap<String,String> getAttributes() {
        return atts;
    }
    /**
     * @param type the type to set
     */
    public void setType(PlotType type) {
        this.type = type;
    }
    
   public static enum PlotType {
        SAMPLE,
        REDLINE,       
    }
   
   CnvDataPoint(int chr, long pos, long end, double ratio, String clone, HashMap<String,String> atts) {
        this.chr = chr;
        this.pos = pos;
        this.atts = atts;
        this.end = Math.max(pos+1, end);
        this.ratio = ratio;
        this.clone = clone;
    }
            
}
