/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

/**
 *
 * @author Chantal
 */
public class CnvDataPoint {
    double pos;
    double ratio;
    String clone;
    int chr;

    CnvDataPoint(int chr, double pos, double ratio, String clone) {
        this.chr = chr;
        this.pos = pos;
        this.ratio = ratio;
        this.clone = clone;
    }
            
}
