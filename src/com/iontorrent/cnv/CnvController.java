/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal
 */
public class CnvController {

    CnvData data;
    static String lastfile;
    String file;
    private boolean isLog;
    private int colChr = -1;
    private int colPos = -1;
    private int colEnd = -1;
    private int colDesc = -1;
    private int colRatio = -1;
    private int colControl = -1;
    private int colSample =-1;
    
    public CnvController(String file) {
        this.file = file;

    }

    public String getCustomProperties() {
        String s = "chr:" + colChr + "_pos:" + colPos +  "_end:" + colEnd + "_desc:" + colDesc 
                + "_ratio:" + colRatio + "_sample:" + colSample + "_control:" + colControl
                + "_log:"+(isLog ? 1 : 0);
        return s;
    }

    public boolean parseCustomProperties(String s) {
        p("Got track prperties with column assignments " + s);
        ArrayList<String> items = StringTools.parseList(s, "_");
        boolean ok = true;
        for (String it : items) {
       //     p("Processing " + it);
            int e = it.indexOf(":");
            if (e < 0) {
            //    p("No : in: " + it);
                ok  = false;
            } else {
                String a = it.substring(0, e).toLowerCase();
                String b = it.substring(e + 1);
                p("a=" + a + ", b=" + b);
                int val = 0;
                try {
                    val = Integer.parseInt(b);
                } catch (Exception ex) {
                    p("Could not parse " + b + " to int");
                    ok  = false;
                }
                if (a.startsWith("chr")) {
                    colChr = val;
                } else if (a.startsWith("pos")) {
                    colPos = val;
                 } else if (a.startsWith("end")) {
                    colEnd = val;
                } else if (a.startsWith("desc")) {
                    colDesc = val;
                } else if (a.startsWith("sample")) {
                    colSample = val;
                } else if (a.startsWith("ratio")) {
                    colRatio = val;
                } else if (a.startsWith("control")) {
                    colControl = val;                
                } else if (a.startsWith("log")) {
                    isLog = val > 0;
                }
            }

        }
        return ok;
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
    public boolean gatherParameters() {
        // wizard that asks for file
        // pick columns
        boolean askforfile = true;
        p("Got file: " + file);
        if (file == null) {
            if (lastfile == null) {
                lastfile = "h:\\data\\cnv.csv";
            }
            file = lastfile;
        } else if (new File(file).exists()) {
            askforfile = false;
        }
        if (askforfile) {
            file = FileTools.getFile("Please pick the fiel with CNV data", ".csv", file);
            lastfile = file;
        }
        ArrayList<String> lines = FileTools.getFileAsArray(file);

        if (lines == null || lines.size()<2) return false;
        
        String first = lines.get(0);
        // now show a few lines in the table
        String sep = ",";
        int comma = count(first, ",");
        int tab = count(first, "\t");
        if (tab > comma) sep = "\t";
        
        // now guess positions
        CnvTableModel model = new CnvTableModel(lines, sep);
        guessColumns(model.getHeader());
        
        CnvTablePanel tablepanel = new CnvTablePanel(model);
        
        tablepanel.setColChr(colChr);
        tablepanel.setLog(isLog);
        tablepanel.setColControl(colControl);
        tablepanel.setColDesc(colDesc);
        tablepanel.setColPos(colPos);
        tablepanel.setColRatio(colRatio);
        tablepanel.setColSample(colSample);
        tablepanel.setColEnd(colEnd);
        
        tablepanel.updateRenderers();
        
        int ans = JOptionPane.showConfirmDialog(IGV.getMainFrame(), tablepanel, "Pick the columns", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (ans != JOptionPane.OK_OPTION) {
            return false;
        } else {
            setColChr(tablepanel.getColChr());
            setColPos(tablepanel.getColPos());
            setColEnd(tablepanel.getColEnd());
            setColDesc(tablepanel.getColDesc());
            setColRatio(tablepanel.getColRatio());
            setColSample(tablepanel.getColSample());
            setColControl(tablepanel.getColControl());
            setIsLog(tablepanel.isLog());
            
            return true;
        }

    }

    private void guessColumns(ArrayList<String> header) {
        for (int i = 0; i < header.size(); i++) {
            String name = header.get(i);
            name = name.toLowerCase().trim();
            if (name.startsWith("chr")) {
                this.colChr = i;
                 p("Got guessed chr: "+i);
            }
            else if (name.startsWith("pos") || name.startsWith("start") ||  name.startsWith("begin")){
                this.colPos = i;
                p("Got guessed pos: "+i);
            }
             else if (name.startsWith("end") ){
                this.colEnd = i;
                p("Got guessed colEnd: "+i);
            }
              else if (name.indexOf("ratio")>-1 && name.indexOf("log")>-1 ){
                 if (colRatio <0) this.colRatio = i;
                p("Got guessed colRatio: "+i);
            }
            else if (name.indexOf("sample")>-1){
                if (colSample <0) this.colSample = i;
                p("Got guessed colSample: "+i);
            }
            else if (name.indexOf("control")>-1){
                if (colControl <0) this.colControl = i;
                p("Got guessed colControl: "+i);
            }
        }
        if (colRatio < 0) {
            for (int i = 0; i < header.size(); i++) {
                String name = header.get(i);
                name = name.toLowerCase().trim();
                if (name.indexOf("ratio")>-1){
                     if (colRatio <0) this.colRatio = i;
                    p("Got guessed colRatio: "+i);
                }               
            }
          }
    }
    public CnvData readData() {
        data = new CnvData(file);
        data.setColChr(getColChr());
        data.setColSample(this.colSample);
        data.setColControl(this.colControl);
        data.setColDesc(getColDesc());
        data.setColRatio(getColRatio());
        data.setColPos(getColPos());
        data.setColEnd(getColEnd());
        data.setLog(this.isLog);
        data.parseCustomFile();
        return data;
    }

    private void p(String s) {
        System.out.println("CnvController: " + s);
    }

    /**
     * @return the colChr
     */
    public int getColChr() {
        return colChr;
    }

    /**
     * @param colChr the colChr to set
     */
    public void setColChr(int colChr) {
        this.colChr = colChr;
    }
    public void setColEnd(int i) {
        this.colEnd = i;
    }

    /**
     * @return the colPos
     */
    public int getColPos() {
        return colPos;
    }
     public int getColEnd() {
        return colEnd;
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
        return colDesc;
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
        return colRatio;
    }

    /**
     * @param colRatio the colRatio to set
     */
    public void setColRatio(int colRatio) {
        this.colRatio = colRatio;
    }

    private void setColSample(int colSample) {
        this.colSample = colSample;
    }
    private void setColControl(int colControl) {
        this.colControl = colControl;
    }

    /**
     * @return the isLog
     */
    public boolean isIsLog() {
        return isLog;
    }

    /**
     * @param isLog the isLog to set
     */
    public void setIsLog(boolean isLog) {
        this.isLog = isLog;
    }
}
