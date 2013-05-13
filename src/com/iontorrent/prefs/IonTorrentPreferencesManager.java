/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.prefs;

import java.util.Map;

/**
 *
 * @author Chantal
 */
public class IonTorrentPreferencesManager {
        
     /** Added by Chantal Roth, June 25th 2012 */
    public static final String IONTORRENT_FLOWDIST_HIDE_FIRST_HP = "IONTORRENT.FLOWDIST_HIDE_FIRST_HP";
    public static final String IONTORRENT_FLOWDIST_BINSIZE = "IONTORRENT.FLOWDIST_BINSIZE";
    public static final String IONTORRENT_FLOWDIST_CHARTTYPE = "IONTORRENT.FLOWDIST_CHARTTYPE";
    public static final String IONTORRENT_SERVER = "IONTORRENT.SERVER";
    public static final String IONTORRENT_RESULTS = "IONTORRENT.RESULTS";
    public static final String STARTUP_AUTOLOAD_GENOME = "STARTUP.AUTOLOAD_GENOME";
    public static final String BAM_FILE = "BAM.FILENAME";
    /** the number of bases to the left and right of the current location we wish to include in the ionogram alignment view */
    public static final String IONTORRENT_NRBASES_IONOGRAM_ALIGN= "IONTORRENT.NRBASES_IONOGRAM_ALIGN";
    /** the currently preferred height of one line in the ionogram alignment (can be zoomed, and we want to remember the setting */
    public static final String IONTORRENT_HEIGHT_IONOGRAM_ALIGN= "IONTORRENT.HEIGHT_IONOGRAM_ALIGN";
    /** the maximum number of reads we want to include in the ionogram alignment (showing hundreds may not make sense  :-)*/
    public static final String IONTORRENT_MAXNREADS_IONOGRAM_ALIGN= "IONTORRENT.MAXNRREADS_IONOGRAM_ALIGN";
    public static final String IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE= "IONTORRENT.ONOGRAM_ALIGN_DRAWTYPE";
    public static final String IONTORRENT_BAM_HAS_FLOWVALUES= "IONTORRENT.BAM_HAS_FLOWVALUES";
    
    
    /** Added by Chantal Roth for whole genome Karyo views */ 
    public static final String KARYO_ALLOW_BAMFILES= "KARYO.ALLOW_BAMFILES";
    public static final String KARYO_ALLOW_GENEFILES= "KARYO.ALLOW_GENEFILES";
    public static final String KARYO_ALLOW_EXPFILES= "KARYO.ALLOW_EXPFILES";
    
    public IonTorrentPreferencesManager(Map defaultValues) {       
        initDefaultValues(defaultValues);
    }
    private void initDefaultValues(Map defaultValues) {
        defaultValues.put(IONTORRENT_FLOWDIST_HIDE_FIRST_HP, "true");
        defaultValues.put(IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE, "peak");
        defaultValues.put(IONTORRENT_BAM_HAS_FLOWVALUES, "false");
        defaultValues.put(IONTORRENT_FLOWDIST_BINSIZE, "15");
        defaultValues.put(IONTORRENT_FLOWDIST_CHARTTYPE, "LINE");
        defaultValues.put(IONTORRENT_SERVER, "ioneast.ite");
        defaultValues.put(IONTORRENT_RESULTS, "/results/analysis/output/Home/");
        defaultValues.put(IONTORRENT_NRBASES_IONOGRAM_ALIGN, "5");
        defaultValues.put(IONTORRENT_HEIGHT_IONOGRAM_ALIGN, "50");
        defaultValues.put(IONTORRENT_MAXNREADS_IONOGRAM_ALIGN, "100");
        defaultValues.put(KARYO_ALLOW_BAMFILES, "false");
        defaultValues.put(KARYO_ALLOW_GENEFILES, "false");
        defaultValues.put(KARYO_ALLOW_EXPFILES, "false");
    }
}
