/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import org.broad.igv.variant.Variant;
import org.broad.igv.variant.vcf.VCFVariant;
import org.broad.tribble.Feature;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

/**
 *
 * @author Chantal Roth
 */
public class VariantMetaInfo extends FeatureMetaInfo {

    private String scorefieldname = null;
    int count = 0;

    public VariantMetaInfo(KaryoTrack t, String name) {
        super(t, name);
    }

    @Override
    public void populateMetaInfo(Feature f) {
      //  p("---- populateMetaInfo with feature "+f);
        if (!(f instanceof Variant)) {
            return;
        }
        Variant var = (Variant) f;
        //   boolean show = Math.random()>0.99;
        Map<String, Object> map = var.getAttributes();

        if (map != null && !map.isEmpty()) {
            Iterator<String> it = map.keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();
                super.addAtt(name);

                Object val = map.get(name);
                super.addAtt(name, ""+val);
                
            }
        } else {
            // if (show) p("Variant has no attributes: "+var.getType()+","+var.toString());
        }

        if (var.getPloidy()>-1) addAtt("Ploidy", var.getPloidy());
        addAtt("AlleleFraction", var.getAlleleFraction());
        addAtt("HetCount", var.getHetCount());
        addAtt("HomRefCount", var.getHomRefCount());
        addAtt("HomVarCount", var.getHomVarCount());
        addAtt("NoCallCount", var.getNoCallCount());
        addAtt("MethylationRate", var.getMethlationRate());
        addAtt("PhredScaledQual", var.getPhredScaledQual());
       // addAtt("Score", var.getPhredScaledQual());
        count++;
    }

    @Override
    public double getValue(String att, Feature f) {
       
        if (!(f instanceof Variant)) {
            return -1;
        }
        Variant var = (Variant) f;
        if (att.equalsIgnoreCase("AlleleFraction")) {
            return var.getAlleleFraction();
        }
        if (att.equalsIgnoreCase("HetCount")) {
            return var.getHetCount();
        }
        if (att.equalsIgnoreCase("HomRefCount")) {
            return var.getHomRefCount();
        }
        if (att.equalsIgnoreCase("Ploidy")) {
        //    p("Getting ploidy from var:"+var.getPloidy());
            return var.getPloidy();
        }
        if (att.equalsIgnoreCase("HomVarCount")) {
            return var.getHomVarCount();
        }
        if (att.equalsIgnoreCase("NoCallCount")) {
            return var.getAlleleFraction();
        }

        if (att.equalsIgnoreCase("MethylationRate")) {
            return var.getMethlationRate();
        }
        if (att.equalsIgnoreCase("PhredScaledQual")) {
            return var.getPhredScaledQual();
        }

        if (att.equalsIgnoreCase("Score")) {
            String what = this.getScoreFieldName(f);
          //  p("Getting SCORE from att. what is the score field? "+what);
            if (!what.equalsIgnoreCase("score")) {
                return getValue(what, f);
            }
           // else p("I have no idea");
        }
        return -2;
    }

    public String getScoreFieldName(Feature f) {
        if (scorefieldname != null) {
            return scorefieldname;
        }

        //if (!(f instanceof Variant)) {
        scorefieldname = "PhredScaledQual";

//        }else {
//            Variant var = (Variant)f;
//
//            if (var instanceof VCFVariant) {
//                 VCFVariant v = (VCFVariant)var;
//                 v.getVariantContext().getAttributes();
//            }
//             if (var.getAttributes().containsKey("CONFIDENCE")) {
//             }
//        }
        return scorefieldname;
    }

    public boolean isDeletion(Feature f) {
        if (!(f instanceof Variant)) {
            return false;
        }
        Variant var = (Variant) f;
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
    }

    private String getAttribute(Variant var, String rel) {
        String res = var.getAttributeAsString(rel);
        Iterator it = var.getAttributes().keySet().iterator();
        if (it != null) {
            for (; it.hasNext();) {
                String n = "" + it.next();
                if (n.equalsIgnoreCase(rel)) {
                    rel = n;
                    res = var.getAttributeAsString(rel);
                }
                // p("Got att: "+n);                       
            }
        }
        return res;

    }

    public boolean isInsertion(Feature f) {
        if (!(f instanceof Variant)) {
            return false;
        }
        Variant var = (Variant) f;
        if (var instanceof VCFVariant) {
            VCFVariant v = (VCFVariant) var;
            VariantContext c = v.getVariantContext();
            boolean indel = c.isIndel();
            if (indel) {
                return c.isSimpleInsertion();
            }
        } else {
            String ref = var.getReference();
            String alt = getAttribute(var, "ALT");
            if (ref != null && alt != null) {
                return alt.length() > ref.length();
            }
        }
        return false;
    }

    private void p(String s) {
        Logger.getLogger("VariantMetaInfo").info(s);
    }
}
