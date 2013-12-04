/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.data.seg;

import java.util.HashMap;

/**
 *
 * @author Chantal
 */
public class SummarySegment extends Segment{
    
    public SummarySegment(int start, int end, float score) {
        super(start, end, score);
    }
    
    public SummarySegment(String chr, int start, int origStart, int end, int origEnd, float value, String description, HashMap<String,String> atts) {
        super(chr, start, origStart, end, origEnd, value, description, atts);
    }
    
    @Override
     public Segment copy() {
        
        Segment seg = new  SummarySegment(chr, start, extendedStart, end, extendedEnd, score, description, atts);        
        return seg;
    }
}
