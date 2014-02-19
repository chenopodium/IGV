/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.broad.igv.variant.Allele;
import org.broad.igv.variant.Genotype;
import org.broad.igv.variant.Variant;

/**
 *
 * @author Chantal Roth
 */
public class FakeVariant implements Variant {

    private String id;
    private String type;
    private boolean filtered;
    private double phred;
    private double frac;
    private int hom;
    private int het;
    private int homvar;
    private int start;
    private int end;
    private String chr;
    private int expected;
    
    private HashMap<String, Object> map;
    
    public FakeVariant(String chr, int start, int end) {
        this.chr = chr;
        this.start = start;
        this.end = end;
        type = "CNV";
        frac = Math.random();
        phred = Math.random();
        id = "CNV"+(int)(Math.random()*10000);
        getAttributes().put("COPYNR", (int)(Math.random()*10));
        String type = "GAIN";
        if (Math.random()>0.5) type = "LOSS";
        getAttributes().put("INDELTYPE", type);
    }
    public double getPloidy() {
        return 2;
    }
    public double getScore() {
        return getPloidy();
    }
    @Override
    public String getID() {
        return id;
    }

    @Override
    public String getType() {
       return type;
    }

    @Override
    public boolean isFiltered() {
       return filtered;
    }

    @Override
    public double getPhredScaledQual() {
       return getPhred();
    }

    @Override
    public Map<String, Object> getAttributes() {
       if (map == null) map = new HashMap<String, Object>();
       return map;
    }

    @Override
    public String getAttributeAsString(String key) {
        return ""+getAttributes().get(key);
    }

    @Override
    public String getReference() {
        return null;
    }

    @Override
    public Set<Allele> getAlternateAlleles() {
        return null;
    }

    @Override
    public double[] getAlleleFreqs() {
        return null;
    }

    @Override
    public double getAlleleFraction() {
        return getFrac();
    }

    @Override
    public Collection<String> getSampleNames() {
        return null;
    }

    @Override
    public Genotype getGenotype(String sample) {
        return null;
    }

    @Override
    public Collection<String> getFilters() {
       return null;
    }

    @Override
    public int getHomVarCount() {
        return getHomvar();
    }

    @Override
    public int getHetCount() {
        return getHet();
    }

    @Override
    public int getHomRefCount() {
        return getHom();
    }

    @Override
    public int getNoCallCount() {
        return 0;
    }

    @Override
    public double getMethlationRate() {
        return 0;
    }

    @Override
    public double getCoveredSampleFraction() {
       return 0;
    }

    @Override
    public String getPositionString() {
        return getChr()+":"+getStart()+"-"+getEnd();
    }

    @Override
    public String getChr() {
       return chr;
    }

    @Override
    public int getStart() {
        return start;
    }

    @Override
    public int getEnd() {
       return end;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param filtered the filtered to set
     */
    public void setFiltered(boolean filtered) {
        this.filtered = filtered;
    }

    /**
     * @return the phred
     */
    public double getPhred() {
        return phred;
    }

    /**
     * @param phred the phred to set
     */
    public void setPhred(double phred) {
        this.phred = phred;
    }

    /**
     * @return the frac
     */
    public double getFrac() {
        return frac;
    }

    /**
     * @param frac the frac to set
     */
    public void setFrac(double frac) {
        this.frac = frac;
    }

    /**
     * @return the hom
     */
    public int getHom() {
        return hom;
    }

    /**
     * @param hom the hom to set
     */
    public void setHom(int hom) {
        this.hom = hom;
    }

    /**
     * @return the het
     */
    public int getHet() {
        return het;
    }

    /**
     * @param het the het to set
     */
    public void setHet(int het) {
        this.het = het;
    }

    /**
     * @return the homvar
     */
    public int getHomvar() {
        return homvar;
    }

    /**
     * @param homvar the homvar to set
     */
    public void setHomvar(int homvar) {
        this.homvar = homvar;
    }
    
    public String toString() {
        return "Fake variant @"+ this.getPositionString()+", type="+this.getType();    
    }

    /**
     * @return the expected
     */
    public int getExpected() {
        return expected;
    }

    /**
     * @param expected the expected to set
     */
    public void setExpected(int expected) {
        this.expected = expected;
    }
}
