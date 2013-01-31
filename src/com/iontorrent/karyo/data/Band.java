/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

/**
 *
 * @author Chantal Roth
 */
public class Band {
    private String name;
    private String chrname;
    private long start;
    private long end;
    private String type;
    
    private Chromosome chr;

    
    public Band(String name, String chrname, long start, long end, String type) {
        this.name = name;
        this.chrname = chrname;
        this.start = start;
        this.end = end;
        this.type = type;
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
        return type.equals("acen");
    }
    public boolean issNeg() {
        return type.equals("gneg");
    }
    public boolean isStalk() {
        return type.equals("stalk");
    }
    public boolean isVar() {
        return type.equals("gvar");
    }
           
}
