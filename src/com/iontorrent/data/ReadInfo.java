/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data;

import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.wellmodel.WellCoordinate;

/**
 *
 * @author Chantal Roth
 */
public class ReadInfo {
    String readname;
    FlowValue fv;
    
    public ReadInfo(String readname, FlowValue fv) {
        this.readname = readname;
        this.fv = fv;
    }
    public int getFlowPosition() {
        return fv.getFlowPosition();
    }
    public WellCoordinate getCoord() {
         // Y9VO3:844:1030_Y9VO3:38:643
            int col = readname.indexOf(":");
            readname = readname.substring(col + 1);
            readname = readname.replace(":", "_");
            int ul = readname.indexOf("_");
            int x = Integer.parseInt(readname.substring(0, ul));
            int y = Integer.parseInt(readname.substring(ul+1));
            return new WellCoordinate(x, y);
    }
    public char getBase() {
        return fv.getBase();
    }
    //public FlowValue(int flowvalue, int flowposition, char base, int location_in_sequence, boolean empty, char alignmentbase) {
    public FlowValue getFlowValue() {
        return fv;
    }
    public String getReadName() {
        return readname;
    }
    public static String getHeader() {
       return "Read name, flow position, base, flow value";
    }
    public String toCsv() {
        StringBuilder b = new StringBuilder();
        b = b.append(readname).append(",").append(this.getFlowPosition()).append(",").append(fv.getBase()).append(",").append(fv.getFlowvalue());
        return b.toString();
    }
    public String toString() {
        return toCsv();
    }
}
