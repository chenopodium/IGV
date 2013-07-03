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
    private String file;
    
    private int colChr = 3;
    private int colPos=4;
    private int colEnd=4;
    private int colDesc = 1;
    private int colRatio = 2;
    private int colSample = -1;
    private int colControl = -1;
    int bad = 0;
    
    private ArrayList<CnvDataPoint> points;
    
    
    public CnvData() {
        this("h:\\data\\cnv.csv");
    }
    public CnvData(String file) {
        this.file = file;
        
    }
    //Index	Clone	Log2 Ratio Ch1/Ch2	Chromosome	Position	Type	Banding	Amplitude Ch1	Amplitude Ch2	Ratio	Log2 Product

    public ArrayList<CnvDataPoint> getPoints(){
        return points;
    }
    private int count(String line, String s ) {
        int count = 0;
        int pos = line.indexOf(s);
        while (pos > 0) {
            count++;
            pos = line.indexOf(s, pos+1);
        }
        return count;
    }
    public void parse() {
        ArrayList<String> lines = FileTools.getFileAsArray(getFile());
        
        
        points = new ArrayList<CnvDataPoint>();
        int count = 0;
        
        if (lines == null || lines.size()<1){
           p("Got no data in file "+file);
           return;
           
        }
         String first = lines.get(0);
        // now show a few lines in the table
        String sep = ",";
        int comma = count(first, ",");
        int tab = count(first, "\t");
        if (tab > comma) sep = "\t";
        
        for (String line: lines) {
            ArrayList<String> items = StringTools.parseList(line, sep);
            if (items != null && items.size()>3) {
                String desc = "";
                
                desc = get(items, getColDesc());
                double ratio = 0;
                if( getColRatio()>0) {
                    ratio = getDouble(get(items, getColRatio()));
                }
                else {
                    // compute it
                    double cont = getDouble(get(items, this.colControl));
                    double sample = getDouble(get(items, this.colSample));
                    if (cont > 0 && sample > 0) {
                     //   p("Computing ratio from sample "+sample+" and control "+cont);
                        ratio = sample / cont;
                    }
                }
                
                String schr = get(items, getColChr()).toLowerCase();
                if (schr.startsWith("chr")) schr = schr.substring(3);
                int chr = getInt(schr);
                long pos = getLong(get(items, getColPos()));
                long end = pos;
                if (colEnd > -1) end = getLong(get(items,this.colEnd));
                if (chr > 0 && pos > -1 ) {
                    //if (ratio > 10) ratio = ratio / 1000;
                    //if (ratio <-10) ratio = ratio / 1000;
                    if (count % 100 == 0) p("Got data, line: "+chr+":"+pos+"-"+end+", ratio="+ratio+", "+desc);
                    CnvDataPoint point = new CnvDataPoint(chr, pos, end, ratio, desc);
                    points.add(point);
                    count++;
                }
                else {
                    if (bad == 0 || bad % 100 == 0 || bad <10) {
                        p("Ignoring line: "+line);
                        bad++;
                    }
                }
            }
        }
        
    }
    private String get( ArrayList<String> items, int col) {
        if (col < 0 || col >= items.size()) {
            return null;
        }
        else return items.get(col);
    }
    private void p(String s) {
        System.out.println("CnvData: "+s);
    }
    private int getInt(String s) {
        if (s == null) return -1;
        int  d = -1;
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
        if (s == null) return -1;
        double  d = -1;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
             d = Double.parseDouble(s);
        }
        catch (Exception e) {
            
        }        
        return d;                
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(String file) {
        this.file = file;
    }

    /**
     * @return the colChr
     */
    public int getColChr() {
        if (colChr<0) return 3;
        return colChr;
    }

    /**
     * @param colChr the colChr to set
     */
    public void setColChr(int colChr) {
        this.colChr = colChr;
    }

    /**
     * @return the colPos
     */
    public int getColPos() {
         if (colPos<0) return 4;
        return colPos;
    }

    /**
     * @param colPos the colPos to set
     */
    public void setColPos(int colPos) {
       
        this.colPos = colPos;
    }

    /**
     * @return the colDesc
     */
    public int getColDesc() {
        if (colDesc<0) return 1;
        else return colDesc;
    }

    /**
     * @param colDesc the colDesc to set
     */
    public void setColDesc(int colDesc) {
        this.colDesc = colDesc;
    }

    /**
     * @return the colRatio
     */
    public int getColRatio() {
         if (colRatio<0 && this.colControl<0 && this.colSample <0) return 2;
        return colRatio;
    }

    /**
     * @param colRatio the colRatio to set
     */
    public void setColRatio(int colRatio) {
        this.colRatio = colRatio;
    }

    void setColEnd(int colEnd) {
        this.colEnd = colEnd;
    }

    private long getLong(String s) {
        if (s == null) return -1;
        long  d = -1;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
             d = Long.parseLong(s);
        }
        catch (Exception e) {
            // maybe it is a double?
            d = (long)getDouble(s);
        }        
        return d;                
    }

    public void setColSample(int colSample) {
        this.colSample = colSample;
    }
    public void setColControl(int colControl) {
        this.colControl = colControl;
    }
}
