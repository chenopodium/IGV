/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.filter;

import com.iontorrent.karyo.data.KaryoFeature;
import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public class VariantAttributeFilter extends KaryoFilter{
     
    private String attname;
    private String attvalue;
    private double numvalue;
    private String operator;
    public VariantAttributeFilter() {        
        super();
        name= "Attribute filter";
       
    }
    
  
    // example VT = SNP, INDEL, SV
   @Override
    public String toString() {
        String s = "";
        if (this.isHighlightFiltered()) s = "Highlight areas that have features where ";
        else if (this.isRemoveFiltered())s = "Hide features where ";
        else s = "Show features where the ";
        s += attname;
        if (not) s += " is  ";
        else s +=" is not ";
        s += attvalue;
        s += " (filter mode: "+filterMode.name()+")";
        return s;
    }
    
    @Override
    public boolean filter(KaryoFeature kf) {
        if (attname == null || attvalue == null) return true;
        
        Feature f = kf.getFeature();
        if (!(f instanceof Variant)) return true;
        Variant var = (Variant)f;
        String value = var.getAttributeAsString(attname);
        // if this is  number, operator is not null
        boolean passed = true;
        if (operator != null) {
            try {
                Double d = Double.parseDouble(value);
                if (operator.equalsIgnoreCase("=")) passed = d == numvalue;
                else if (operator.equalsIgnoreCase(">")) passed = d >  numvalue;
                else if (operator.equalsIgnoreCase("<")) passed =  d <   numvalue;
                else  passed =  d !=   numvalue;
            }
            catch (Exception e) {
                p("Could not parse to double: "+value);
            }
        }
        else {
            passed = value != null && value.equalsIgnoreCase(attvalue);
        }
        if (not) passed= !passed;
             
        return passed;
    }
    @Override
    public Color getFilteredColor(KaryoFeature kf) {
        Feature f = kf.getFeature();
        if (!(f instanceof Variant)) return null;
        Variant var = (Variant)f;
        return getColorByValue(var.getAttributeAsString(attname));
    }
    
    protected Color getColorByValue(String value) {
        if (value == null) return Color.black;
        // example VT = SNP, INDEL, SV
        else if (value.equalsIgnoreCase("SNP")) return Color.orange;
        else if (value.equalsIgnoreCase("SV")) return Color.green;        
        else if (value.equalsIgnoreCase("INDEL")) return Color.BLACK;
        else if (value.equalsIgnoreCase("CNV")) return Color.red.darker();
        else if (value.equalsIgnoreCase("MIXED")) return Color.gray;
        else return Color.blue;
    }
    private void p(String s) {
        Logger.getLogger("AttributeFilter").info(s);
    }
    @Override
    public boolean isForFeature(Feature feature) {
        if (feature instanceof KaryoFeature) return isForFeature(((KaryoFeature)feature).getFeature());
        return feature instanceof Variant;
    }

    /**
     * @return the attname
     */
    public String getAttname() {
        return attname;
    }

    /**
     * @param attname the attname to set
     */
    public void setAttname(String attname) {
        this.attname = attname;
    }

    /**
     * @return the attvalue
     */
    public String getAttvalue() {
        return attvalue;
    }

    /**
     * @param attvalue the attvalue to set
     */
    public void setAttvalue(String attvalue) {
        this.attvalue = attvalue;
    }

    @Override
    public KaryoFilter copy() {
        VariantAttributeFilter fil = new VariantAttributeFilter();
        fil.setAttname(attname);
        fil.setAttvalue(attvalue);
        fil.setFilteredColor(super.getFilteredColor());
        fil.setNonfilteredColor(super.getNonfilteredColor());
        return fil;
    }

    @Override
    public boolean isValid() {
        return (this.attname != null && this.attvalue != null);
    }

    public void setOperator(String op) {
        this.operator = op;
        numvalue = Double.parseDouble(attvalue);
    }
}
