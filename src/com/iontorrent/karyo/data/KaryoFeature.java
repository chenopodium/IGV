/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import com.iontorrent.karyo.filter.KaryoFilter;
import com.iontorrent.karyo.filter.LocusScoreFilter;
import com.iontorrent.karyo.filter.VariantAttributeFilter;
import com.iontorrent.karyo.filter.VariantFrequencyFilter;
import com.iontorrent.karyo.renderer.RenderManager;
import com.iontorrent.karyo.views.GuiProperties;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.broad.igv.data.seg.Segment;
import org.broad.igv.feature.BasicFeature;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.track.AbstractTrack;
import org.broad.igv.variant.Allele;
import org.broad.igv.variant.Genotype;
import org.broad.igv.variant.Variant;
import org.broad.igv.variant.VariantTrack;
import org.broad.igv.variant.vcf.VCFVariant;
import org.broad.tribble.Feature;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

/**
 *
 * @author Chantal Roth
 */
public class KaryoFeature implements Feature {

    private Feature feature;
    private Range range;
    private DecimalFormat f = new DecimalFormat("#0.00");
    private int msgs = 0;
    private static GuiProperties gui;
    //private FeatureTree tree;
    
    public KaryoFeature(Feature f) {
        this.feature = f;
        //this.tree = tree;
        range = new Range(f.getStart(), f.getEnd());
    }
//    public FeatureTree getTree() {
//        return tree;
//    }

    public boolean overlaps(KaryoFeature f) {
        return f.range.overlaps(range);
    }

    public String getAttribute(Variant var, String rel) {
        String res = var.getAttributeAsString(rel);
        Iterator it = var.getAttributes().keySet().iterator();
        if (it != null) {
            for (; it.hasNext();) {
                String n = "" + it.next();
                if (n.equalsIgnoreCase(rel)) {
                    rel = n;
                    res = var.getAttributeAsString(rel);
                    //    p("Got att "+rel+": "+n);     
                }

            }
        }
        return res;

    }

   
    public boolean isInsertion(double cutoffScore) {
        if (cutoffScore == Integer.MIN_VALUE) {
            cutoffScore = 2;
        }
        if (feature instanceof Variant) {
            Variant var = (Variant) feature;

            String scopynr = getAttribute(var, "COPYNR");
            if (scopynr != null && scopynr.length() > 0) {
                int copynr = -1;
                if (scopynr != null) {
                    try {
                        copynr = Integer.parseInt(scopynr);
                    } catch (Exception e) {
                        p("Could not parse copy nr " + scopynr + " to string");
                    }
                    return copynr > cutoffScore;
                }
            }

            String rel = "INDELTYPE";
            String indeltype = getAttribute(var, rel);

            if (indeltype != null) {
                p("Got indeltype from " + rel + ":" + indeltype);
                if (indeltype.contains("INS")) {
                    return true;
                } else {
                    return false;
                }
            }

            if (var instanceof VCFVariant) {
                VCFVariant v = (VCFVariant) var;
                VariantContext c = v.getVariantContext();
                boolean indel = c.isIndel();
                if (indel) {
                    return c.isSimpleDeletion();
                }
            } else {
                String ref = var.getReference();
                String alt = getAttribute(var, "ALT");
                if (ref != null && alt != null) {
                    return alt.length() < ref.length();
                }
            }
            return false;
        } else if (feature instanceof Segment) {
            Segment s = (Segment) feature;
            if (s.getScore() > cutoffScore) {
                return true;
            }
        }
        return false;
    }

    public double getScore(FeatureMetaInfo info, String relevantName) {
        double score = 0;
        //  p("get score "+this.getRelevantAttName()+" for "+f.getClass().getName());

        Feature f = this.feature;

        if (f instanceof Variant) {
            Variant v = (Variant) f;


            String sscore = getAttribute(v, relevantName);

            if (sscore != null && sscore.length() > 0) {
                //        p("Got v.getAttributeAsString: "+sscore);
                try {
                    score = Double.parseDouble(sscore);
                } catch (Exception e) {
                    // p("Could not parse score "+relevantName+"="+sscore +" to Double");
                }
            } else {
                // check the default values
                //        p("Getting score via info.getValue("+this.getRelevantAttName()+")");
                return info.getValue(relevantName, f);
            }
        } else if (f instanceof Segment) {
            Segment v = (Segment) f;
            score = v.getScore();
            //  pmsg("Score for segment : "+score);

        } else if (f instanceof LocusScore) {
            LocusScore v = (LocusScore) f;
            score = v.getScore();
        }
        return score;
    }

    private void pmsg(String s) {
        if (msgs < 100) {
            p(s);
            msgs++;
        }
    }

    public boolean isDeletion(double cutoffScore) {
        if (cutoffScore == Integer.MIN_VALUE) {
            cutoffScore = 2;
        }
        if (feature instanceof Variant) {
            Variant var = (Variant) feature;
            String scopynr = getAttribute(var, "COPYNR");
            if (scopynr != null && scopynr.length() > 0) {
                int copynr = -1;
                if (scopynr != null) {
                    try {
                        copynr = Integer.parseInt(scopynr);
                    } catch (Exception e) {
                        p("Could not parse copy nr " + scopynr + " to string");
                    }
                    return copynr < cutoffScore;
                }
            }
            String rel = "INDELTYPE";
            String indeltype = getAttribute(var, rel);

            if (indeltype != null) {
                p("Got indeltype from " + rel + ":" + indeltype);
                if (indeltype.contains("DEL")) {
                    return true;
                } else {
                    return false;
                }
            }
            if (var instanceof VCFVariant) {
                VCFVariant v = (VCFVariant) var;
                VariantContext c = v.getVariantContext();
                boolean indel = c.isIndel();
                if (indel) {
                    return c.isSimpleDeletion();
                }
            } else {
                String ref = var.getReference();
                String alt = getAttribute(var, "ALT");
                if (ref != null && alt != null) {
                    return alt.length() < ref.length();
                }
            }
        } else if (feature instanceof Segment) {
            Segment s = (Segment) feature;
            if (s.getScore() < cutoffScore) {
                return true;
            }
        }
        return false;

    }

    public String toString(FeatureMetaInfo info, String nl) {
        
        String spar = getParString(info, nl);
        if (feature instanceof Variant) {
            Variant v = (Variant) feature;
            
            String h = "Variant type: " + v.getType() + nl;
            h += "Position: " + v.getPositionString() + nl;
            h += spar;
            if (v.getAlleleFraction() > 0) {
                h += "Allele fraction: " + f.format(v.getAlleleFraction()) + nl;
            }
            if (v.getCoveredSampleFraction() > 0) {
                h += "Covered sample fraction: " + f.format(v.getCoveredSampleFraction()) + nl;
            }
            if (v.getMethlationRate() > 0) {
                h += "Methylation rate: " + f.format(v.getMethlationRate()) + nl;
            }
            h += "Phred scaled quality: " + f.format(v.getPhredScaledQual()) + nl;
            if (v.getHetCount() > 0) {
                h += "Het count rate: " + v.getHetCount() + nl;
            }
            if (v.getHomRefCount() > 0) {
                h += "Hom ref count: " + v.getHomRefCount() + nl;
            }
            if (v.getHomVarCount() > 0) {
                h += "Hom var count: " + v.getHomVarCount() + nl;
            }
            if (v.getNoCallCount() > 0) {
                h += "No call count: " + v.getNoCallCount() + nl;
            }

            h += "<b>Reference:  " + v.getReference() + "</b>" + nl;

            Collection<String> filters = v.getFilters();
            if (filters != null) {
                int nr = 0;
                for (String f : filters) {
                    nr++;
                    h += "Filter " + nr + ":  " + f + nl;
                }
            }

            Set<Allele> als = v.getAlternateAlleles();
            if (als != null) {
                int nr = 0;
                for (Allele al : als) {
                    nr++;
                    h += "<b>Allele: " + nr + ": " + al.toString() + "</b>" + nl;
                }
            }
            Collection<String> samples = v.getSampleNames();
            if (samples != null) {
                for (String sample : samples) {
                    Genotype gt = v.getGenotype(sample);
                    if (gt != null) {
                        h += "<b>Sample: " + sample + " has genotype " + gt.getGenotypeString() + "</b>" + nl;
                        Map map = gt.getAttributes();
                        if (map != null && map.size() > 0) {
                            Iterator it = map.keySet().iterator();
                            while (it.hasNext()) {
                                String key = (String) it.next();
                                String val = "" + map.get(key);
                                
                                h += key + ": " + val + nl;
                            }
                            h += "</b>";
                        }
                    }
                }
            }
            Map map = v.getAttributes();

            if (v instanceof VCFVariant) {
                VCFVariant var = (VCFVariant) v;
                VariantContext c = var.getVariantContext();
                map = c.getAttributes();

            }

            if (map != null && map.size() > 0) {
                h += "<b>";
                Iterator it = map.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String val = "" + map.get(key);
                    h += key + ": " + val + nl;
                }
                h += "</b>";
            }
           
            return h;
        } else if (feature instanceof LocusScore) {
            LocusScore basic = (LocusScore) feature;
            String scorename= "Score";
            if (info != null) scorename = info.getScoreLabel();
            return "<b>"+scorename+" =" + basic.getScore() + "</b>" + nl + "Position: " + feature.getStart() + "-" + feature.getEnd() + nl+spar;
        } else if (feature instanceof Segment) {
            Segment basic = (Segment) feature;
            String scorename= "Score";
            if (info != null) scorename = info.getScoreLabel();
            return "<b>"+scorename+" =" + basic.getScore() + "</b>" + nl + "Position: " + feature.getStart() + "-" + feature.getEnd() + nl+spar;
        } else if (feature instanceof BasicFeature) {
            BasicFeature basic = (BasicFeature) feature;
            return basic.getName() + ": " + basic.getType() + nl + "Position: " + getChr() + " " + feature.getStart() + "-" + feature.getEnd() + nl + feature.toString()+spar;
        } else {
            return feature.getClass().getName() + nl + "Position: " + getChr() + " " + feature.getStart() + "-" + feature.getEnd() + nl + feature.toString()+spar;
        }
    }

    public List<KaryoFilter> getFilters(Feature f) {
        if (f instanceof Variant) {
            List<KaryoFilter> filters = new ArrayList<KaryoFilter>();
            filters.add(new VariantFrequencyFilter());
            filters.add(new VariantAttributeFilter());
            return filters;
        } else if (f instanceof LocusScore) {
            List<KaryoFilter> filters = new ArrayList<KaryoFilter>();
            filters.add(new LocusScoreFilter());
            return filters;
        } else if (f instanceof Segment) {
            List<KaryoFilter> filters = new ArrayList<KaryoFilter>();
            filters.add(new LocusScoreFilter());
            p("No filter for segment yet");
            return filters;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return toString(null);
    }
    public String toString(FeatureMetaInfo info) {
        if (feature instanceof Variant) {
            return toString(info, "\n");
        } else if (feature instanceof LocusScore) {
            return toString(info, "\n");
        } else if (feature instanceof Segment) {
            return toString(info, "\n");
        } else {
            String spar = getParString(info, "\n")+"\n";
            return spar+feature.toString();
        }
    }

    public String getParString(FeatureMetaInfo info, String nl) {
         boolean par  = Chromosome.isPar(info.getTrack().isMale(),this);
        String spar = nl+"Feature is in ";
        if (par) spar += "par region";
        else spar +="<b>non par region</b>";
        if (Chromosome.isX(getChr())) spar += " on X";
        else if (Chromosome.isY(getChr())) spar += " on Y";
        return spar;
    }
    public String toHtml(FeatureMetaInfo info) {
        String nl = "<br>";
        AbstractTrack igvtrack = info.getTrack().getTrack();
        if (feature instanceof Variant) {
            if (igvtrack != null && igvtrack instanceof VariantTrack) {
                VariantTrack vtrack = (VariantTrack)igvtrack;
                String spar = getParString(info, nl)+nl;
                String h= spar+vtrack.getVariantToolTip((Variant)feature);
                return  h;
            }
            else return toString(info, nl);
        } else {
            return toString(info, nl);
        }
    }

    @Override
    public String getChr() {
        String chr = feature.getChr();
        if (chr == null) {
            chr = "";
        }
        return chr;
    }

    @Override
    public int getStart() {
        return feature.getStart();
    }

    @Override
    public int getEnd() {
        return feature.getEnd();
    }

    public Feature getFeature() {
        return feature;
    }

    private void p(String s) {
        Logger.getLogger("KaryoFeature").info(s);
    }
}
