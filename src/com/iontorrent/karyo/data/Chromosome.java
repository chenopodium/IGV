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

    private static void p(String s) {
        Logger.getLogger("Chromosome").info(s);
    }
}