/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import com.iontorrent.data.karyo.filter.KaryoFilter;
import com.iontorrent.data.karyo.filter.VariantFrequencyFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class KaryoFeature implements Feature {

    private Feature feature;
    private Range range;
    private DecimalFormat f = new DecimalFormat("#0.00");

    public KaryoFeature(Feature f) {
        this.feature = f;
        range = new Range(f.getStart(), f.getEnd());
    }
    
    public boolean overlaps(KaryoFeature f) {
        return f.range.overlaps(range);
    }

    public String toString(String nl) {
         if (feature instanceof Variant) {
            Variant v = (Variant) feature;
            String h = "Variant type: " + v.getType() + nl;
            h += "Position: " + v.getPositionString() + nl;
            h += "Allele fraction: " + f.format(v.getAlleleFraction()) + nl;
            h += "Covered sample fraction: " + f.format(v.getCoveredSampleFraction()) + nl;
            h += "Methylation rate: " + f.format(v.getMethlationRate()) + nl;
            h += "Phred scaled quality: " + f.format(v.getPhredScaledQual()) + nl;
            h += "Het count rate: " + v.getHetCount() + nl;
            h += "Hem ref count: " + v.getHomRefCount() + nl;
            h += "Hem var count: " + v.getHomVarCount() + nl;
            Map map = v.getAttributes();
            if (map != null && map.size()>0) {
                h+="<b>";
                Iterator it = map.keySet().iterator();
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String val = ""+map.get(key);
                    h += key+": " + val + nl;
                }
                h+="</b>";
            }
            return h;
        } else {
            return feature.getClass().getName()+"@"+feature.getChr()+":"+feature.getStart()+"-"+feature.getEnd()+nl+feature.toString();
        }
    }
    public List<KaryoFilter> getFilters(Feature f) {
        if (f instanceof Variant) {
            List<KaryoFilter> filters = new ArrayList<KaryoFilter>();
            filters.add(new VariantFrequencyFilter());
            return filters;
        }
        else return null;
    }
    @Override
    public String toString() {
        if (feature instanceof Variant) {
            return toString("\n");
        } else {
            return feature.toString();
        }
    }

    public String toHtml() {
        String nl = "<br>";
        if (feature instanceof Variant) {
            return toString(nl);
        } else {
            return toString();
        }
    }

    @Override
    public String getChr() {
        return feature.getChr();
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
}
