/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import org.broad.igv.feature.Cytoband;

/**
 *
 * @author Chantal Roth
 */
public class Band {
    private String name;
    private String chrname;
    private long start;
    private long end;
    private char type;
    private short stain;
    private Chromosome chr;

     public Band(String name, Chromosome chr, Range r) {
        this.name = name;
        this.type = 'x';
        this.stain = 50;
        this.start =r.a;
        this.end = r.b;
        this.chrname = chr.getName();
    }
    public Band(Cytoband cyto) {
        this.name = cyto.getName();
        this.type = cyto.getType();
        this.stain = cyto.getStain();
        this.start =cyto.getStart();
        this.end = cyto.getEnd();
        this.chrname = cyto.getChr();
    }
    public Band(String name, String chrname, long start, long end, char type, short stain) {
        this.name = name;
        this.chrname = chrname;
        this.start = start;
        this.end = end;
        this.type = type;
        this.stain = stain;
    }
    public String toString() {
        return "Band @ "+chrname+", "+start+"-"+end;
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
     * @return the chrname
     */
    public String getChrname() {
        return chrname;
    }

    /**
     * @param chrname the chrname to set
     */
    public void setChrname(String chrname) {
        this.chrname = chrname;
    }

    /**
     * @return the start
     */
    public long getStart() {
        return start;
    }

    /**
     * @param start the start to set
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * @return the end
     */
    public long getEnd() {
        return end;
    }

    /**
     * @param end the end to set
     */
    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * @return the type
     */
    public char getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(char type) {
        this.type = type;
    }
    public short getStain() {
        return stain;
    }

    /**
     * @return the chr
     */
    public Chromosome getChr() {
        return chr;
    }

    /**
     * @param chr the chr to set
     */
    public void setChr(Chromosome chr) {
        this.chr = chr;
    }

    public double getHeight() {
        return getEnd() - getStart();
    }
    public boolean isCentromer() {
        return type == 'c';
    }
     public boolean isPar() {
        return type == 'x';
    }
    public boolean issNeg() {
        return type == 'n';
    }
    public boolean isStalk() {
        return type == 's';
    }
    public boolean isVar() {
        return type == 'v';
    }
           
}
