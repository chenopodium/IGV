/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

package org.broad.igv.variant.vcf;

import org.apache.log4j.Logger;
import org.broad.igv.variant.Allele;
import org.broad.igv.variant.Genotype;
import org.broad.igv.variant.Variant;
import org.broad.igv.variant.VariantTrack;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

import java.util.*;

//import org.broadinstitute.sting.utils.variantcontext.Allele;

/**
 * @author Jim Robinson
 * @date Aug 1, 2011
 */
public class VCFVariant implements Variant {

    private static Logger log = Logger.getLogger(Variant.class);

    VariantContext variantContext;
    Set<Allele> alternateAlleles;
    private ZygosityCount zygosityCount;
    private boolean isIndel;

    String chr;
    private double[] alleleFreqs;
    private double methylationRate = Double.NaN;  // <= signals unknown / not applicable
    private double coveredSampleFraction = Double.NaN;

    public VCFVariant(VariantContext variantContext, String chr) {
        this.variantContext = variantContext;
        this.chr = chr;
        isIndel = variantContext.getType() == VariantContext.Type.INDEL;
        init();
    }

    private void init() {
        zygosityCount = new ZygosityCount();
        for (String sample : getSampleNames()) {
            Genotype genotype = getGenotype(sample);
            zygosityCount.incrementCount(genotype);
        }

        // TODO -- deal with multiple value allele freq, e.g. [0.01,0.001]
        String afString = null;
        String[] alleleFreqKeys = {"AF", "GMAF"};
        try {
            for (String alleleFreqKey : alleleFreqKeys) {
                afString = variantContext.getAttributeAsString(alleleFreqKey, "-1");
                alleleFreqs = parseAFString(afString);
                if (alleleFreqs[0] >= 0) break;
            }
        } catch (NumberFormatException e) {
            alleleFreqs = new double[]{-1};
            log.error("Error parsing allele frequency: " + afString);
        }

    }

    private int tryToGetPloidy(String key) {
         int p = -1;
        String s =  this.getAttributeAsString(key);
        if (s != null && s.length()>0) {
            try {
                p = Integer.parseInt(s);
            }
            catch (Exception e)  {
                log.info("Could not get ploidy from "+s+" for key "+key);
            }
        }
        return p;
    }
    @Override
    public double getPloidy() {
        int p = tryToGetPloidy("CNV_Ploidy");
        if (p > -1) return p;
        p = tryToGetPloidy("LongDel_CNV_Ploidy");
        if (p > -1) return p;
        p = tryToGetPloidy("Ploidy");
        if (p > -1) return p;
        
        
        if (this.getSampleNames() == null || getSampleNames().size()<1) {
            log.info("getPloidy: no sample info, no CNV_ploidy, no LongDel_CNV_Ploidy, no Ploidy key -> no ploidy");
            return -1;
        }
        
        for (String s: this.getSampleNames()) {
            p = getPloidy(s);
            if (p != -1) {
               // log.info("Got Ploidy "+p+" for sample "+s);
            
            }
        }
        return p;
    }
    
    public double getScore() {
        return getPloidy();
    }
    
    public int getPloidy(String sample) {
        Genotype genotype = getGenotype(sample);
        if (genotype != null && genotype.getAttributes() != null) {
            Set<String> keys = genotype.getAttributes().keySet();
            if (keys.size() > 0) {
                String key = "CN";
                //for (String key : keys) {
                    try {
                        String sp = genotype.getAttributeAsString(key);
                        if (sp == null) return -1;
                        else return Integer.parseInt(sp);
                    } catch (Exception e) {
                        log.info("getPloidy: Could not get "+key+"  from sample "+sample);
                    }
               // }
            }
            //else log.info("Sample "+sample+" has no keys at "+this.getPositionString());
        }
        else log.info("Sample "+sample+" has no genotype at "+this.getPositionString());
        return -1;
    }
    /**
     * Allele frequency is a comma separated list of doubles
     * We strip away brackets and parentheses
     *
     * @param afString
     * @return
     */
    private double[] parseAFString(String afString) {
        afString = afString.replaceAll("[\\[\\]\\(\\)]", "");
        String[] tokens = afString.split(",");
        double[] result = new double[tokens.length];
        for (int ii = 0; ii < tokens.length; ii++) {
            result[ii] = Double.parseDouble(tokens[ii]);
        }
        return result;
    }

    /**
     * Compute the average methylation rate for those samples with data (i.e. with methylation rate recorded).
     */
    private void computeMethylationRate() {

        double methTotal = 0;
        int samplesWithData = 0;
        final int size = getSampleNames().size();
        if (size > 0) {
            for (String sample : getSampleNames()) {
                Genotype genotype = getGenotype(sample);
                double mr = genotype.getAttributeAsDouble("MR");
                double goodBaseCount = genotype.getAttributeAsDouble("MR");
                if (!Double.isNaN(mr) && !Double.isNaN(goodBaseCount) && goodBaseCount > VariantTrack.METHYLATION_MIN_BASE_COUNT) {
                    methTotal += mr;
                    samplesWithData++;
                }
            }
            methylationRate = samplesWithData == 0 ? 0 : methTotal / samplesWithData;
            coveredSampleFraction = ((double) samplesWithData) / size;
        }
    }


    public String getID() {
        return variantContext.getID();
    }

    public boolean isFiltered() {
        return variantContext.isFiltered();
    }

    public String getAttributeAsString(String key) {
        return variantContext.getAttributeAsString(key, null);
    }

    public String getReference() {
        return variantContext.getReference().toString();
    }

    public Set<Allele> getAlternateAlleles() {
        if (alternateAlleles == null) {
            List<org.broadinstitute.sting.utils.variantcontext.Allele> tmp = variantContext.getAlternateAlleles();
            alternateAlleles = new HashSet(tmp.size());
            for (org.broadinstitute.sting.utils.variantcontext.Allele a : tmp) {
                alternateAlleles.add(new VCFAllele(a.getBases()));
            }
        }
        return alternateAlleles;
    }

    public double getPhredScaledQual() {
        return variantContext.getPhredScaledQual();
    }

    public String getType() {
        return variantContext.getType().toString();
    }


    /**
     * Return the allele frequency as annotated with an AF or GMAF attribute.  A value of -1 indicates
     * no annotation (unknown allele frequency).
     */
    public double[] getAlleleFreqs() {
        return alleleFreqs;
    }

    /**
     * Return the allele fraction for this variant.  The allele fraction is similiar to allele frequency, but is based
     * on the samples in this VCF as opposed to an AF or GMAF annotation.
     * <p/>
     * A value of -1 indicates unknown
     */
    public double getAlleleFraction() {

        int total = getHomVarCount() + getHetCount() + getHomRefCount();
        return total == 0 ? -1 : (((double) getHomVarCount() + ((double) getHetCount()) / 2) / total);
    }

    /**
     * Return the methylation rate as annoted with a MR attribute.  A value of -1 indicates
     * no annotation (unknown methylation rate).  This option is only applicable for dna methylation data.
     */
    public double getMethlationRate() {
        if (Double.isNaN(methylationRate)) {
            computeMethylationRate();
        }
        return methylationRate;
    }

    public double getCoveredSampleFraction() {
        if (Double.isNaN(coveredSampleFraction)) {
            computeMethylationRate();
        }
        return coveredSampleFraction;
    }

    public Collection<String> getSampleNames() {
        return variantContext.getSampleNames();
    }

    public Map<String, Object> getAttributes() {
        return variantContext.getAttributes();
    }

    @Override
    public Genotype getGenotype(String sample) {
        // TODO -- cache these rather than make a new object each call?
        return new VCFGenotype(variantContext.getGenotype(sample));
    }

    public Collection<String> getFilters() {
        return variantContext.getFilters();
    }

    @Override
    public int getHomVarCount() {
        return zygosityCount.getHomVar();
    }

    @Override
    public int getHetCount() {
        return zygosityCount.getHet();
    }

    @Override
    public int getHomRefCount() {
        return zygosityCount.getHomRef();
    }

    @Override
    public int getNoCallCount() {
        return zygosityCount.getNoCall();
    }

    @Override
    public String getChr() {
        return chr;
    }

    @Override
    public int getStart() {
        return isIndel ? variantContext.getStart() : variantContext.getStart() - 1;
    }

    @Override
    public int getEnd() {
        return variantContext.getEnd();
    }

    @Override
    public String toString() {
        return String.format("VCFVariant[%s:%d-%d]", getChr(), getStart(), getEnd());
    }

    @Override
    public String getPositionString() {
        if (variantContext.getStart() == variantContext.getEnd()) {
            return String.valueOf(variantContext.getStart());
        } else {
            return String.format("%d-%d", variantContext.getStart(), variantContext.getEnd());
        }

    }


    public VariantContext getVariantContext() {
        return variantContext;
    }

    /**
     * @author Jim Robinson
     * @date Aug 1, 2011
     */
    public static class ZygosityCount {
        private int homVar = 0;
        private int het = 0;
        private int homRef = 0;
        private int noCall = 0;

        public void incrementCount(Genotype genotype) {
            if (genotype != null) {
                if (genotype.isHomVar()) {
                    homVar++;
                } else if (genotype.isHet()) {
                    het++;
                } else if (genotype.isHomRef()) {
                    homRef++;
                } else {
                    noCall++;
                }
            }
        }

        public int getHomVar() {
            return homVar;
        }

        public int getHet() {
            return het;
        }

        public int getHomRef() {
            return homRef;
        }

        public int getNoCall() {
            return noCall;
        }

    }
}
