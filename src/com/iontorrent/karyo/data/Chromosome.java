/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Chantal Roth
 */
public class Chromosome {

    static Range pary1 = new Range(100001, 2649520);
    static Range pary2 = new Range(59034050, 59363566);
    static Range parx1 = new Range(600001, 2649520);
    static Range parx2 = new Range(154931044, 155260560);

   
    private String name;
    private long length;
    private long center;
    private String genome;
    private String id;
    private String type;
    private HashMap<KaryoTrack, FeatureTree> treemap;
    private ArrayList<Band> bands;

    public Chromosome(String name, String chrid, long length, String type) {
        this.name = name;
        this.id = chrid;
        this.length = length;
        this.center = length / 2;
        this.type = type;
        treemap = new HashMap<KaryoTrack, FeatureTree>();
        bands = new ArrayList<Band>();
    }

    public static boolean isSexChromosome(String name) {
        boolean sex = name.equalsIgnoreCase("x") || name.equalsIgnoreCase("23") || isY(name);
        // if (sex) Logger.getLogger("Chromsome").info("Chr "+name+" is a sex chromosome");
        return sex;
    }

    public static boolean isX(String name) {
        boolean y = name.equalsIgnoreCase("x") || name.equalsIgnoreCase("chrx") || name.equalsIgnoreCase("23");
        //   if (y) Logger.getLogger("Chromsome").info("Chr "+name+" is a y chromosome");
        return y;
    }

    public static boolean isY(String name) {
        boolean y = name.equalsIgnoreCase("y") || name.equalsIgnoreCase("chry") || name.equalsIgnoreCase("24");
        //   if (y) Logger.getLogger("Chromsome").info("Chr "+name+" is a y chromosome");
        return y;
    }

    public void addTree(KaryoTrack kt, FeatureTree tree) {
        treemap.put(kt, tree);
    }

    public FeatureTree getTree(KaryoTrack kt) {
        return treemap.get(kt);
    }

    public long getCenter() {
        return center;
    }

    public void findCenter() {

        for (Band b : bands) {
            if (b.isCentromer()) {
                center = b.getStart();
            }
        }
    }

    public String toString() {
        String s = "Chr " + id + ", " + length + ", " + bands.size() + " bands";
        return s;
    }

    public void add(Band band) {
        getBands().add(band);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * @return the genome
     */
    public String getGenome() {
        return genome;
    }

    /**
     * @param genome the genome to set
     */
    public void setGenome(String genome) {
        this.genome = genome;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the bands
     */
    public ArrayList<Band> getBands() {
        return bands;
    }

    public boolean isSexChromosome() {
        return isSexChromosome(name);
    }

    public boolean isY() {
        return isY(name);
    }
    
     public static boolean isPar(boolean sampleIsMale, KaryoFeature f) {

        // no matter which gender, Y is special
        if (isY(f.getChr())) {
            Range target = new Range(f.getStart(), f.getEnd());
            // y and par region -> so it is par
            if (pary1.overlaps(target) || pary2.overlaps(target)) {
             //   p("Y in par region: "+target+"-> par");
                return true;
            } // y bot not in par region, so not par
            else {
              //  p("Y in NON-par region: "+target+"-> false");
                return false;
            }
        } 
        // not male and not y, so par
        if (!sampleIsMale) return true;
                
        // male and neither X nor Y, so par
        if (!isX(f.getChr())) return true;
        
        // male and X
        Range target = new Range(f.getStart(), f.getEnd());
        // male x and par region -> par
        if (parx1.overlaps(target) || parx2.overlaps(target)) {
           // p("male on x, in par -> par: +"+ target);
            return true;
        } else {
            // male and not par region on x
           // p("male on x, NOT in par -> not par: +"+ target);
            return false;
        }       
    }
    private static void p(String s) {
        Logger.getLogger("Chromosome").info(s);
    }
}
