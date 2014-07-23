/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.renderer;

import com.iontorrent.karyo.data.Chromosome;
import com.iontorrent.karyo.data.FeatureMetaInfo;
import com.iontorrent.karyo.data.FeatureTree;
import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.KaryoTrack;
import com.iontorrent.karyo.drawables.GuiChromosome;
import com.iontorrent.karyo.drawables.GuiFeatureTree;
import com.iontorrent.karyo.views.GuiProperties;
import com.iontorrent.views.basic.DrawingCanvas;
import java.awt.Color;
import java.util.HashMap;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.genome.GenderManager;
import org.broad.igv.track.AbstractTrack;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal
 */
public class RenderType {
 public boolean debug = true;
    
    /** Count instances so we know if we need to use different colors :-) */
   // private static HashMap<Class, Integer> instances = new HashMap<Class, Integer>();
    private String name;
    private String description;
    
    protected Color[] colors;
    protected int nrcolors;
    private int nrerrors;
    //  Color color;
    private String type;
    private String relevantAttName;
    private double parcutoffScore;
    private HashMap<String, Double> cutoffmap;
    protected static GuiProperties gui;
    
    protected KaryoTrack ktrack;
    
    public RenderType(KaryoTrack ktrack) {
        this(ktrack, 1);
    }
    public RenderType(KaryoTrack ktrack, int nrcolors) {
        this(ktrack, "Histogram", "Histogram", "Standard histogram type rendering for whole chromosome view for any kind of feature", nrcolors);        
    }
     public RenderType(KaryoTrack ktrack,String name, String desc, int nrcolors) {
        this(ktrack,name, name, desc, nrcolors);
    }
    public RenderType(KaryoTrack ktrack, String type, String name, String desc, int nrcolors) {
        this.ktrack = ktrack;
        this.type = type;
        this.name = name;
        this.description = desc;
        this.nrcolors = nrcolors;
        cutoffmap = new HashMap<String, Double>();
        // check preferences if there are cutoff values?
        colors = new Color[nrcolors];
        gui = RenderManager.getGuiProperties();
        intValues();
//        Integer count = instances.get(this.getClass());
//        if (count == null) count = new Integer(0);
//        else count++;
//        instances.put(this.getClass(), count);
    }

    public double getParCutoffScore() {
         if (parcutoffScore == Integer.MIN_VALUE) parcutoffScore = 2;  
         return parcutoffScore;
    }
    
    public double getCutoffScore(KaryoFeature f, AbstractTrack track) {
        double expected = track.getExpectedValue(f.getChr());
        
        if (expected != 0) {
            // Got expected value from track Ploidy_(vcf) and chr chrY:1.0
            if (f.getChr().equalsIgnoreCase("chrx") || f.getChr().equalsIgnoreCase("chry")) {
        //        p("Got expected value from track "+track.getName()+" and chr "+f.getChr()+":"+expected);
            }
            return expected;
        }
      //  else p("Got no expeded value from track "+track.getName()+", using old way to get cutoff score");
        
        String chr = f.getChr();
        Double score = cutoffmap.get(chr);
        if (score == null) {
            String key = chr+"_CUTOFF".toUpperCase();
            if (PreferenceManager.getInstance().hasTemp(key)) {
                String val = PreferenceManager.getInstance().getTemp(key);
                p("getCutoffScore "+chr+": Found value "+val+" for "+key+" in preferences. Adding to hashmap");
                try {
                    double d = Double.parseDouble(val);
                    score = new Double(d);
                    cutoffmap.put(chr, score); 
                    p("getCutoffScore "+chr+": Got value: "+d);
                    return d;
                }
                catch (Exception e) {}
            }
            else  {
                if (parcutoffScore == Integer.MIN_VALUE) parcutoffScore = 2;  
                return parcutoffScore;
            }
            p("getCutoffScore "+chr+": Getting cutoff score for chromosome "+chr+":"+score);
            return score.doubleValue();            
        }
        else return score.doubleValue();     
        
    }
//    private double getActualCutoffScoreBasedOnChromosome(KaryoFeature f, double cutoff) {
//        // only check for MALE samples        
//        if(cutoff != 2 || GenderManager.isPar(ktrack.isMale(), f.getChr(),f.getRange())) return cutoff;
//        else {
//            //p("Getting getActualCutoffScoreBasedOnChromosome chr "+f.getChr()+", cutoff="+cutoff+", -> GOT NON-PAR REGION ON Y CHROMOSOME: "+f.getStart()+"-"+f.getEnd());
//            return 1.0;
//        }
//    }
    
    // XXX set cutoffscore per chromosome
    public void setCutoffScore(double d){
        this.parcutoffScore = d;
    }
    public String getColorName(int colornr) {
       if (colornr <=0) return "Track color";
       else return null;
    }
    public String getColorShortName(int colornr) {
       return null;
    }
    
    public boolean drawFeature(FeatureMetaInfo meta, KaryoFeature f) {
        return true;
    }
    
    public String getGuiKey() {
       // return ktrack.getLastPartOfFile();
         return ktrack.getTrackName();
    }
    public String getGuiSample() {
       // return ktrack.getLastPartOfFile();
         return ktrack.getSample();
    }
    public String getKaryoDisplayName() {
         return gui.getDisplayName(getGuiSample(), getGuiKey(), ktrack.getFileExt());
    }
    public String getKaryoFilterKey() {
         return gui.getFilterKey(getGuiSample(), getGuiKey(), ktrack.getFileExt());
    }
    public String getKaryoFilterOperator() {
         return gui.getFilterOperator(getGuiSample(), getGuiKey(), ktrack.getFileExt());
    }
    
    public String getKaryoFilterMode() {
        return gui.getFilterMode(getGuiSample(), getGuiKey(), ktrack.getFileExt());
    }
    public double getKaryoFilterValue() {
         return gui.getFilterValue(getGuiSample(), getGuiKey(), ktrack.getFileExt());
    }
    public double getKaryoCutoffScore(){
        return gui.getKaryoCutoffScore(getGuiSample(), getGuiKey(), ktrack.getFileExt());
    }
     public String getKaryoScoreName(){
        String rel= gui.getKaryoScoreName(getGuiSample(), getGuiKey(), ktrack.getFileExt());
        if (rel == null) rel = "PLOIDY";
        return rel;
    }
      public String getKaryoFilterScoreName(){
        String rel= gui.getKaryoFilterScoreName(getGuiSample(), getGuiKey(), ktrack.getFileExt());
        if (rel == null) rel = "PRECISION";
        return rel;
    }
      public String getKaryoScoreLabel(){
        String rel= gui.getScoreLabel(getGuiSample(), getGuiKey(), ktrack.getFileExt());
        if (rel == null || rel.equals("null")) rel = "Score";
        return rel;
    }
    public Color getKaryoColorGain(){
       // p("Got color gain: sample="+getGuiSample()+",key="+getGuiKey()+", ext="+ktrack.getFileExt());
        Color c= gui.getKaryoColorGain(getGuiSample(), getGuiKey(), ktrack.getFileExt());
        if (c == null) this.getDefaultColor(1);
        return c;
    }
    public Color getKaryoColorLoss(){
     //   p("Got color loss: sample="+getGuiSample()+",key="+getGuiKey()+", ext="+ktrack.getFileExt());
        Color c=  gui.getKaryoColorLoss(getGuiSample(), getGuiKey(), ktrack.getFileExt());
        if (c == null) this.getDefaultColor(2);
        return c;
    }
    public Color getKaryoColorNeutral(){
     //   p("Got color neutral: sample="+getGuiSample()+",key="+getGuiKey()+", ext="+ktrack.getFileExt());
        Color c=  gui.getKaryoColorNeutral(getGuiSample(), getGuiKey(), ktrack.getFileExt());
        if (c == null) {
           // p("Got no neutral color for "+getGuiKey()+"/"+ ktrack.getFileExt()+", using defaultcolor of track");
            this.getDefaultColor(0);
        }
        return c;
    }
    
     public void setKaryoDisplayName(String value) {
         gui.setDisplayName( getGuiSample(),getGuiKey(), ktrack.getFileExt(), value);
    }
    public void setKaryoCutoffScore(double d){
        gui.setKaryoCutoffScore(getGuiSample(),getGuiKey(), ktrack.getFileExt(),d);
    }
     public void setKaryoScoreName(String value){
        gui.setKaryoScoreName(getGuiSample(),getGuiKey(), ktrack.getFileExt(), value);
        
    }
    public void setKaryoColorGain(Color c){
         gui.setKaryoColorGain(getGuiSample(),getGuiKey(), ktrack.getFileExt(), c);
        
    }
    public void setKaryoColorLoss(Color c){
        gui.setKaryoColorLoss(getGuiSample(),getGuiKey(), ktrack.getFileExt(),c );
       
    }
    public void setKaryoColorNeutral(Color c){
        gui.setKaryoColorNeutral(getGuiSample(),getGuiKey(), ktrack.getFileExt(),c);        
    }
    
     // TODO: use color gradient with multiple colors
    public Color getColor(FeatureMetaInfo meta, KaryoFeature f) {
        Color c = this.getColor(0);
        if (c == null) c = getKaryoColorNeutral();
        if (f.isInsertion(this.getCutoffScore(f, meta.getTrack().getTrack()))) {
            c = this.getColor(1);
            if (c == null) c = getKaryoColorGain();
        }
        else if (f.isDeletion(this.getCutoffScore(f,meta.getTrack().getTrack()))) {
            c = this.getColor(2);
            if (c == null) c = getKaryoColorLoss();
        }
        return c;
    }
    public Color getDistinctColor(FeatureMetaInfo meta, KaryoFeature f) {
        return getDistinctColor(meta, f, false);
    }
    public Color getDistinctColor(FeatureMetaInfo meta, KaryoFeature f, boolean debug) {
        String fieldname =this.getRelevantAttName();
        //debug = true;
        if(debug)  p("Getting color for "+fieldname);
        if (fieldname == null) {
             fieldname = meta.getScoreFieldName(f.getFeature());
             this.setRelevantAttName(fieldname);
              if(debug)  p("Fieldname was null, it is now "+fieldname);
        }
        
        FeatureMetaInfo.Range range = meta.getRangeForAttribute(fieldname, getKaryoScoreLabel());
      
        if (range == null) {
            if (this.nrerrors < 5)  {
                if (debug) p("Found no range for field "+fieldname+" in meta info: "+meta.getTrackname()+", "+meta.getClass().getName());
                nrerrors++;
            }
            return this.getColor(0);
        }
        
        //if(debug)  p("Getting color for "+this.getRelevantAttName());
        double score = f.getScore(meta, this.getRelevantAttName());
        
        double MAX = range.max;
        double MIN = range.min;
        double middle = getCutoffScore(f, meta.getTrack().getTrack());
        
        int which = 0;
        if (Math.abs(score - middle) <= 0.001) which=0;
        else if (score > middle) which =1;
        else  which = 2;
        if (debug) {
            String type = "neutral";
            if (which ==1) type = "GAIN";
            else if (which ==2) type = "LOSS";
           //  p("Color for "+this.getRelevantAttName()+":cutoff="+middle+", score="+score+", resulting color: "+which+"="+type );
        }
        return getColor(which);
        
    }
   
    public Color getGradientColor(FeatureMetaInfo meta, KaryoFeature f) {
        String fieldname =this.getRelevantAttName();
        
        if (fieldname == null) {
             fieldname = meta.getScoreFieldName(f.getFeature());
             this.setRelevantAttName(fieldname);
        }
        
        FeatureMetaInfo.Range range = meta.getRangeForAttribute(fieldname, this.getKaryoScoreLabel());
      
        
        if (range == null) {
            if (debug || this.nrerrors < 5)  {
                p("Found no range for field "+fieldname+" in meta info: "+meta.getTrackname()+", "+meta.getClass().getName());
                nrerrors++;
            }
            
            return this.getColor(0);
        }
        
        
        double score = f.getScore(meta, this.getRelevantAttName());
        
        double MAX = Math.max(range.max, score);
        double MIN = Math.min(range.min, score);
        if (MAX== MIN) MAX = MIN+1;
       return getGradientColor(f, MIN, MAX, score, meta.getTrack().getTrack());
        
    }
  
    public Color getGradientColor(KaryoFeature f, double MIN, double MAX, double score, AbstractTrack t) {
              
        
        
        Color chigh = this.getColor(1);
        Color clow = this.getColor(2);
        Color cmid = this.getColor(0);
        
        if (clow == null) clow = cmid;
        if (chigh == null) chigh = cmid;
        double rangedelta = MAX-MIN;
        double middle = getCutoffScore(f, t);
        
//        if (debug) {
//            //MIN=1.0, MAX=1.0, score=1.0, middle=2.0
//            p("getGradientColor, MIN="+MIN+", MAX="+MAX+", score="+score+", middle="+middle);
//            p("getGradientColor, clow="+clow+", cmid="+cmid+", chigh="+chigh);
//        }
        if (middle == Integer.MIN_VALUE) middle = (MAX-MIN)/2;
        // check if we use the higher or lower scale
        double ds = score - MIN;
                
        Color hi = cmid;
        Color lo = clow;
        
        if (Math.abs(score-middle) < 0.01) return cmid;
        
        if (score > middle) {
            lo = cmid;
            hi = chigh;
            ds = score - middle;
        }
        
        double dr = (hi.getRed()-lo.getRed()) / (rangedelta);
        double dg = (hi.getGreen()-lo.getGreen()) / (rangedelta);
        double db = (hi.getBlue()-lo.getBlue()) / (rangedelta);
        
       // p("Getting color for "+fieldname +"="+score+", min="+MIN+", max="+MAX);
        int r = Math.min(255, Math.max(0,(int) (lo.getRed()+dr * ds)));
        int g = Math.min(255, Math.max(0,(int) (lo.getGreen()+dg * ds)));
        int b = Math.min(255, Math.max(0,(int) (lo.getBlue()+db * ds)));
        
       
        Color c = new Color(r, g, b);
//         if (debug) {
//            p("getGradientColor, res color="+c);
//        }
        return c;
    
    }
    public int getNrColors() {
        return nrcolors;
    }
    
    @Override
    public String toString() {
        return name;
    }
    public String geType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public boolean isClassSupported(Feature featureClass) {
        return featureClass instanceof Feature;
    }

    public GuiFeatureTree getGuiTree(DrawingCanvas canvas, GuiChromosome chromo, FeatureTree tree, int dx) {
        return new GuiFeatureTree(ktrack, canvas, chromo, tree, dx);
    }

     public Color getDefaultColor(int nr) {
         Color c= null;
         //Integer count = instances.get(this.getClass());
         int count = 2;
         if (nr == 1) {
             c = this.ktrack.getTrack().getColor();
             // just one blue track :-)
             if (c == null ||(c == Color.blue && count>1)) c= getDefaultColor(0).brighter().brighter();
         }
         else if (nr == 2) {
              c = this.ktrack.getTrack().getAltColor();
              // just one blue track :-)
             if (c == null || (c == Color.blue && count>1) ) c= getDefaultColor(0).darker().darker();
         }
         else {            
              
             if (c == null ) {
                  c = this.ktrack.getTrack().getMidColor();                  
             }
             // just one blue track :-)
             if (c == null || (c == Color.blue && count>1)) {
                 c = getDefaultColor();               
             }
              ktrack.setColor(c);
         }
         return c;
     }
   
    private Color getDefaultColor() {
        String type = ktrack.getLastPartOfFile().toUpperCase();
        
        p("getDefaultColor: GETTING DEFAULT COLOR FOR "+type);
        
        Color c =   this.randColor(80);
        if (type.indexOf("SNP") > -1) {
            c= new Color(119,221,198);     
        } else if (type.indexOf("EXOME") > -1) {
            c= new Color(240,142,100);
        } else if (type.indexOf("GENE") > -1) {
            c=  new Color(248, 165, 177);       
        } else {
            p("getDefaultColor: Not sure what type of track it is: "+type+", returning random color");
            //c= new Color(202, 225, 187);
            c = this.randColor(80);
        }
       
        return c;
    }

    private void err(String s) {
        Logger.getLogger("RenderType").warn(s);
    }

    private void p(String s) {
        Logger.getLogger("RenderType").info(s);
    }

    /**
     * @return the relevantAttName
     */
    public String getRelevantAttName() {
        if (relevantAttName == null ) {
            this.getKaryoScoreName();
        }
        return relevantAttName;
    }

    /**
     * @param relevantAttName the relevantAttName to set
     */
    public void setRelevantAttName(String relevantAttName) {
        this.relevantAttName = relevantAttName;
      //  p("The relevant att name of "+this.getName()+"  is: "+relevantAttName);
    }

    /**
     * @return the color
     */
    public Color getColor(int nr) {
        if (nr >= colors.length || nr >= this.nrcolors) {
           // err("Colors out of range: "+nr+", "+nrcolors);
            return null;
        }
        Color c = colors[nr];
        if (c == null) {
            p("Got no color for "+nr);
           
        }
        return c;
    }

    /**
     * @param color the color to set
     */
    public void setColor(Color color, int nr) {
        this.colors[nr] = color;
        // also update GUI
        if (nr == 0) this.setKaryoColorNeutral(color);
        else if (nr == 1) this.setKaryoColorGain(color);
        else if (nr == 2) this.setKaryoColorLoss(color);
    }

    private void intValues() {         
        parcutoffScore = getKaryoCutoffScore();        
        this.relevantAttName = this.getKaryoScoreName();
        initColors();
    }

    private void initColors() {
        for (int i = 0; i < nrcolors; i++) {
            Color c = null;
            if (i == 0) c = this.getKaryoColorNeutral();
            else if (i == 1) c = this.getKaryoColorGain();
            else if (i == 2) c = this.getKaryoColorLoss();
            else  c = this.getDefaultColor();
            colors[i] = c;
        }
    }

    private Color randColor(int min) {
        int avg = 0;
        int r=0;
        int g=0;
        int b=0;
        while (avg < min) {
            r = (int)(Math.random()*255);
            g = (int)(Math.random()*255);
            b = (int)(Math.random()*255);
            avg = (g+r+b)/3;
        }
        return new Color(r, g, b);
    }   

}
