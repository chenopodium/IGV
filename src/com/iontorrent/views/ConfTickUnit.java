/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views;

import java.text.DecimalFormat;
import org.jfree.chart.axis.NumberTickUnit;

/**
 *
 * @author Chantal Roth
 */
public class ConfTickUnit extends NumberTickUnit{
    private String base;
    int hplen;
    DecimalFormat f = new DecimalFormat("#%");
    
    public ConfTickUnit(int hplen, String base, double size) {
        super(size);
        this.hplen = hplen;
        this.base = base;
    }
    @Override
    public String toString() {
        return "ConfTickUnit for "+hplen+base;
    }
    @Override
    public String valueToString(double v) {
        
        if (v == 100) {
            return "high"; 
        }
        else if (v == 125 || v == 75) {
            return "medium"; 
        }
        else if (v == 50 || v == 150) {
            return "low"; 
        }
        else if (v == 175 || v == 175) {
            return "very low"; 
        }
        else if (v >175 || v < 25) {
            return "none"; 
        }
        else {
            return ""+v;
        }
    }
}
