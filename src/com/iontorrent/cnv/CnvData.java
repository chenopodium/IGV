/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import java.util.ArrayList;
import org.junit.experimental.theories.DataPoints;

/**
 *
 * @author Chantal
 */
public class CnvData {
    String file = "h:\\data\\cnv.csv";
    
    ArrayList<CnvDataPoint> points;
    
    public CnvData() {
        parse();
    }
    //Index	Clone	Log2 Ratio Ch1/Ch2	Chromosome	Position	Type	Banding	Amplitude Ch1	Amplitude Ch2	Ratio	Log2 Product

    public ArrayList<CnvDataPoint> getPoints(){
        return points;
    }
    public void parse() {
        ArrayList<String> lines = FileTools.getFileAsArray(file);
        
        points = new ArrayList<CnvDataPoint>();
        for (String line: lines) {
            ArrayList<String> items = StringTools.parseList(line);
            if (items != null && items.size()>9) {
                String clone = items.get(1);
                double ratio = getDouble(items.get(2));
                int chr = getInt(items.get(3));
                double pos = getDouble(items.get(4));
                if (chr > 0 && pos > 0 ) {
                    //if (ratio > 10) ratio = ratio / 1000;
                    //if (ratio <-10) ratio = ratio / 1000;
                    // p("Got data, line: "+chr+":"+pos+", "+ratio+", "+clone);
                    CnvDataPoint point = new CnvDataPoint(chr, pos, ratio, clone);
                    points.add(point)  ;
                }
                else p("Got bad data, line: "+line);
            }
        }
        
    }
    private void p(String s) {
        System.out.println("CnvData: "+s);
    }
    private int getInt(String s) {
        int  d = -1000;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
             d = Integer.parseInt(s);
        }
        catch (Exception e) {
          
        }
          
          return d;
    }
    private double getDouble(String s) {
        double  d = -1000;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
             d = Double.parseDouble(s);
        }
        catch (Exception e) {
            
        }        
        return d;                
    }
}
