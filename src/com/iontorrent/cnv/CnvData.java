/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.util.HttpUtils;

/**
 *
 * @author Chantal
 */
public class CnvData {

    private String file;
    private int colChr = 3;
    private int colPos = 4;
    private int colEnd = 4;
    private int colDesc = 1;
    private int colRatio = 2;
    private int colSample = -1;
    private int colControl = -1;
    private boolean log;
    int bad = 0;
    private ArrayList<CnvDataPoint> points;
    private ArrayList<CnvDataPoint> redlinedata;
    private ArrayList<CnvDataPoint> summarydata;
    private String samplefile;
    private String redline;
    private String summaryfile;
    private ArrayList<String> loaded;
    private boolean gotAllData = false;;

    public CnvData(String samplefile, String summaryfile, String redline) {
        this.samplefile = samplefile;
        this.summaryfile = summaryfile;
        this.redline = redline;
        this.log = true;
        parseRedLineFile();
    //    parseSummaryFile();
        loadData("ALL");
    }

    public CnvData(String file) {
        this.file = file;

    }
    //Index	Clone	Log2 Ratio Ch1/Ch2	Chromosome	Position	Type	Banding	Amplitude Ch1	Amplitude Ch2	Ratio	Log2 Product

    public ArrayList<CnvDataPoint> getPoints() {
        return points;
    }

    public ArrayList<CnvDataPoint> getRedLinePoints() {
        return redlinedata;
    }

    public ArrayList<CnvDataPoint> getSummaryPoints() {
        return summarydata;
    }

    public ArrayList<CnvDataPoint> loadData(String chr) {
        p("==== loadData: " + chr);         
     //   p("loaddata trace: "+ErrorHandler.getString(e));
        if (gotAllData && points != null && points.size()>0) {
          //  p("loadData data already loaded, gotAllData is true");
            return points;
        }
        boolean justSummary = false;
       // return ALL data
//        if (justSummary || chr.equalsIgnoreCase("ALL")) {
//            p("loadData ALL or just summary: return summary data if it is there");       
//            if (summarydata == null) {
//                p("loadData ALL: Got no summary data, loading all data");
//                 parseSampleFile(chr);
//            }
//            if (summarydata != null) {
//                p("loadData we DO have summary data ");
//                return summarydata;
//            }
//        }
       // p("loadData ==== Parsing sample data");
        parseSampleFile(chr, true);
      //  p("loadData ==== Parsing sampledata DONE");

        return points;

    }

    private int count(String line, String s) {
        int count = 0;
        int pos = line.indexOf(s);
        while (pos > 0) {
            count++;
            pos = line.indexOf(s, pos + 1);
        }
        return count;
    }

    public void parseRedLineFile() {
    //    p("========= parsing red line file "+redline);
        ArrayList<String> slines = loadContent(redline);

        redlinedata = new ArrayList<CnvDataPoint>();
        int count = 0;

        if (slines == null || slines.size() < 1) {
            p("Got no data in file " + samplefile);
            return;

        }
        String sep = "\t";
        this.setColChr(1);
        this.setColPos(2);
        this.setColEnd(3);
        this.setColRatio(4);
        for (int i = 0; i < slines.size(); i++) {
            String sline = slines.get(i);
            ArrayList<String> sitems = StringTools.parseList(sline, sep);
            if (sitems != null && sitems.size() > 3) {
                String schr = get(sitems, getColChr()).toLowerCase();

                 HashMap<String,String> atts = new HashMap<String,String>();
                int chr = getChr(schr);
                double ratio = getDouble(get(sitems, getColRatio()));

// TODO XXX
                long spos = getLong(get(sitems, getColPos()));
                long send = getLong(get(sitems, this.colEnd));
                if (chr > 0 && spos > -1) {
                    if (ratio < 100) {
                        // if (log) ratio = Math.log(ratio);
                        if (count % 100 == 0) {
                           // p("Got RED line: " + chr + ":" + spos + "-" + send + ", ratio=" + (int) ratio);
                        }
                        CnvDataPoint point = new CnvDataPoint(chr, spos, send, ratio, "", atts);
                        redlinedata.add(point);
                        count++;
                    } else {
                        p("ratio too large, ignoring: " + chr + ":" + spos + "-" + send + ", ratio=" + (int) ratio);
                    }
                } else {
                    if (bad == 0 || bad % 100 == 0 || bad < 10) {
                        p("Ignoring lines: " + sline);
                        bad++;
                    }
                }
            }
        }

    }

    public void parseSummaryFile() {
        p("=========== parsing summary file: " + summaryfile);
        ArrayList<String> slines = loadContent(summaryfile);
        if (slines == null || slines.size() < 1) {
            p("Got no data in file " + samplefile);
            return;

        }
        summarydata = new ArrayList<CnvDataPoint>();
        int count = 0;


        String sep = "\t";

        for (int i = 0; i < slines.size(); i++) {
            String sline = slines.get(i);
            ArrayList<String> sitems = StringTools.parseList(sline, sep);
            if (sitems != null && sitems.size() > 3) {
                String schr = get(sitems, 0).toLowerCase();
                HashMap<String,String> atts = new HashMap<String,String>();
                // chr	start    end 	min	max	mean	setdev	nrvalues
                //1	2492125	4984250	0	9.69	2.03	34.82	584
                int chr = getInt(schr);
                long spos = getLong(get(sitems, 1));
                long send = getLong(get(sitems, 2));

                if (chr > 0 && spos > -1) {
                    double mean = getDouble(get(sitems, 5));
                    if (mean < 100) {
                        // if (log) ratio = Math.log(ratio);
                        if (count % 100 == 0) {
                            p("Got summary line: " + chr + ":" + spos + "-" + send + ", mean=" + (int) mean);
                        }
                        // XXX TODO add atts
                        CnvDataPoint point = new CnvDataPoint(chr, spos, send, mean, "", atts);
                        summarydata.add(point);
                        count++;
                    } else {
                        p("mean too large, ignoring: " + chr + ":" + spos + "-" + send + ", mean=" + (int) mean);
                    }
                } else {
                    if (bad == 0 || bad % 100 == 0 || bad < 10) {
                        p("Ignoring lines: " + sline);
                        bad++;
                    }
                }
            }
        }

    }

    private int getChr(String chrToLoad) {
        int chr = 0;

        if (chrToLoad.startsWith("chr")) {
            chrToLoad = chrToLoad.substring(3);
        }
        if (chrToLoad.equalsIgnoreCase("x")) {
            chr = 23;
        } else if (chrToLoad.equalsIgnoreCase("y")) {
            chr = 24;
        } else if (chrToLoad.equalsIgnoreCase("m")) {
            chr = 25;
        } else {
            try {
                chr = Integer.parseInt(chrToLoad);
            } catch (Exception e) {
                p("Could not parse " + chrToLoad);
            }
        }
        return chr;
    }

    public void parseSampleFile(String chrToLoad, boolean alsoForAll) {
        ArrayList<String> slines = null;
        int chr = 0;
        if (loaded == null) {
            loaded = new ArrayList<String>();
        }
        if (loaded.contains(chrToLoad)) {
            p("Chr " + chrToLoad + " already loaded, loaded contains "+chrToLoad+": "+loaded.contains(chrToLoad)+", total points: "+points.size());
            return;
        }

        if (!alsoForAll && chrToLoad.equalsIgnoreCase("ALL")) {
            //slines = loadContent(this.samplefile);
            p("not doing ALL, return nothing, we use the summary for that");
            return;
        } else {
            if (chrToLoad.equalsIgnoreCase("ALL")) {
               p("Loading ALL data of "+samplefile);
               slines = loadContent(this.samplefile );
            }
            else {
                chr = getChr(chrToLoad);
                p("Loading chr specific sample file " + this.samplefile + "." + chr);
                slines = loadContent(this.samplefile + "." + chr);
            }
        }
        points = new ArrayList<CnvDataPoint>();
        int count = 0;
        if (slines == null || slines.size() < 1) {
            p("Got no data in file " + samplefile + " for chr " + chrToLoad);
            loaded.add(chrToLoad);
            return;
        }
        String sep = "\t";

        this.setColChr(1);
        this.setColPos(2);
        this.setColEnd(3);
        this.setColRatio(4);
        for (int i = 0; i < slines.size(); i++) {
            //String cline = clines.get(i);
            String sline = slines.get(i);
            //  ArrayList<String> citems = StringTools.parseList(cline, sep);
            ArrayList<String> sitems = StringTools.parseList(sline, sep);
            if (sitems != null && sitems.size() > 3) {
                String schr = get(sitems, getColChr()).toLowerCase();
                chr = getChr(schr);
                double sample = getDouble(get(sitems, getColRatio()));

                double value = sample;// - control;
                long spos = getLong(get(sitems, getColPos()));
                long send = getLong(get(sitems, this.colEnd));
                if (chr > 0 && spos > -1) {
                    HashMap<String,String> atts = new HashMap<String,String>();
                    if (value < 100) {
                        // if (log) ratio = Math.log(ratio);
                        // if (count % 10000 == 0) p("Got data, line: "+chr+":"+spos+"-"+send+", ratio="+(int)ratio);
                        CnvDataPoint point = new CnvDataPoint(chr, spos, send, value, "", atts);
                        points.add(point);
                        count++;
                    } else {
                        p("value too large, ignoring: " + chr + ":" + spos + "-" + send + ", value=" + value);
                    }
                } else {
                    if (bad == 0 || bad % 100 == 0 || bad < 10) {
                        p("Ignoring line: " + sline);
                        bad++;
                    }
                }
            }
        }
        loaded.add(chrToLoad);
    }

    public void parseCustomFile() {
        ArrayList<String> lines = FileTools.getFileAsArray(getFile());

        points = new ArrayList<CnvDataPoint>();
        int count = 0;

        if (lines == null || lines.size() < 1) {
            p("Got no data in file " + file);
            return;

        }
        String first = lines.get(0);
        // now show a few lines in the table
        String sep = ",";
        int comma = count(first, ",");
        int tab = count(first, "\t");
        if (tab > comma) {
            sep = "\t";
        }

        for (String line : lines) {
            ArrayList<String> items = StringTools.parseList(line, sep);
            if (items != null && items.size() > 3) {
                String desc = "";

                desc = get(items, getColDesc());
                double ratio = 0;
                if (getColRatio() > 0) {
                    ratio = getDouble(get(items, getColRatio()));
                } else {
                    // compute it
                    double cont = getDouble(get(items, this.colControl));
                    double sample = getDouble(get(items, this.colSample));
                    if (cont > 0 && sample > 0) {
                        //   p("Computing ratio from sample "+sample+" and control "+cont);
                        ratio = sample / cont;
                    }
                }

                if (log) {
                    //p("Computing log")
                    if (ratio >= 0) {
                        ratio = Math.log(ratio);
                    } else {
                        ratio = 0;
                    }
                }
                String schr = get(items, getColChr()).toLowerCase();
                if (schr.startsWith("chr")) {
                    schr = schr.substring(3);
                }
                int chr = getInt(schr);
                long pos = getLong(get(items, getColPos()));
                long end = pos;
                if (colEnd > -1) {
                    end = getLong(get(items, this.colEnd));
                }
                if (chr > 0 && pos > -1) {
                    //if (ratio > 10) ratio = ratio / 1000;
                    //if (ratio <-10) ratio = ratio / 1000;
                    HashMap<String,String> atts = new HashMap<String,String>();
                    if (count % 1000 == 0) {
                        p("Got data, line: " + chr + ":" + pos + "-" + end + ", ratio=" + ratio + ", " + desc);
                    }
                    /// todo xxx 
                    CnvDataPoint point = new CnvDataPoint(chr, pos, end, ratio, desc, atts);
                    points.add(point);
                    count++;
                } else {
                    if (bad == 0 || bad % 100 == 0 || bad < 10) {
                        p("Ignoring line: " + line);
                        bad++;
                    }
                }
            }
        }
        gotAllData = true;

    }

    private String get(ArrayList<String> items, int col) {
        if (col < 0 || col >= items.size()) {
            return null;
        } else {
            return items.get(col);
        }
    }

    private void p(String s) {
        Logger.getLogger("CnvData").info(s);
        System.out.println("CnvData: " + s);
    }

    private int getInt(String s) {
        if (s == null) {
            return -1;
        }
        int d = -1;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
            d = Integer.parseInt(s);
        } catch (Exception e) {
        }

        return d;
    }

    private double getDouble(String s) {
        if (s == null) {
            return -1;
        }
        double d = -1;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
            d = Double.parseDouble(s);
        } catch (Exception e) {
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
        if (colChr < 0) {
            return 3;
        }
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
        if (colPos < 0) {
            return 4;
        }
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
        if (colDesc < 0) {
            return 1;
        } else {
            return colDesc;
        }
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
        if (colRatio < 0 && this.colControl < 0 && this.colSample < 0) {
            return 2;
        }
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
        if (s == null) {
            return -1;
        }
        long d = -1;
        s = s.replace(":", ".");
        s = s.replace(",", ".");
        try {
            d = Long.parseLong(s);
        } catch (Exception e) {
            // maybe it is a double?
            d = (long) getDouble(s);
        }
        return d;
    }

    public void setColSample(int colSample) {
        this.colSample = colSample;
    }

    public void setColControl(int colControl) {
        this.colControl = colControl;
    }

    /**
     * @return the log
     */
    public boolean isLog() {
        return log;
    }

    /**
     * @param log the log to set
     */
    public void setLog(boolean log) {
        this.log = log;
    }

    public ArrayList<String> loadContent(String path) {
        String content = null;
        p("Loading cnv data:" + path);
        if (FileTools.isUrl(path)) {
            p("Path is url: " + path);
            try {
                content = HttpUtils.getInstance().getContentsAsString(new URL(path));
            } catch (Exception ex) {
                p("Unable to load data for " + path + ":" + ErrorHandler.getString(ex));
            }
        } else {
            if (path != null && new File(path).exists()) {
                content = FileTools.getFileAsString(path);
            }
        }

        if (content == null) {
            p("GOT NO CONTENT FOR PATH " + path);
            return null;
        }
        ArrayList<String> slines = StringTools.parseList(content, "\n");
        p("Got " + slines.size() + " lines for " + path + ", content size=" + content.length());
        return slines;
    }
}
