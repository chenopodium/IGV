/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views;

import org.jfree.chart.axis.NumberTickUnit;

/**
 *
 * @author Chantal Roth
 */
public class BcTickUnit extends NumberTickUnit{
    private String base;
    
    public BcTickUnit(String base, double size) {
        super(size);
        this.base = base;
    }
    @Override
    public String toString() {
        return "BcTickUnit for "+base;
    }
    @Override
    public String valueToString(double value) {
        int count = (int) (value / 100);
        if (value % 100 == 0) {
            return count+base; 
        }
        else {
            int diff =-(int)((value % 100 - 50)); 
            if (diff == 0) return "";
            else return ""+diff;
        }
    }
}
