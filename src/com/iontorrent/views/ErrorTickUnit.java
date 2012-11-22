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
public class ErrorTickUnit extends NumberTickUnit{
    private String base;
    int hplen;
    DecimalFormat f = new DecimalFormat("#%");
    
    public ErrorTickUnit(int hplen, String base, double size) {
        super(size);
        this.hplen = hplen;
        this.base = base;
    }
    @Override
    public String toString() {
        return "ErrorTickUnit for "+hplen+base;
    }
    @Override
    public String valueToString(double value) {
        if (value == 0) {
            return "no error"; 
        }
        else {
            return f.format(value/100.0);
        }
    }
}
