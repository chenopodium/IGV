/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.broad.igv.track.FeatureTrack;
import org.broad.igv.variant.Variant;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal Roth
 */
public abstract class FakeTrack extends FeatureTrack {
    
     
    protected static final double MB = 1000000;
    
    protected HashMap<String, List<Feature>> map;
    
    public FakeTrack(String name) {
        super(null, "fake", name);
        map = new HashMap<String, List<Feature>>();
    }
    
    
     @Override
     public List<Feature> getFeatures(String chr, int start, int end) {
          List<Feature>  features = map.get(chr);
          if (features == null) {
              features = createFeatures(chr, start, end);
              map.put(chr, features);
          }
          List<Feature> res = new ArrayList<Feature>();
          for (Feature f: features) {
              int s = f.getStart();
              int e = f.getEnd();
              if (new Range(s, e).overlaps(new Range(start, end))) {
                  res.add(f);
              }
          }
          return res;
     }
     
    protected abstract double getExpectedPerMb();
    
    protected abstract double getMaxSize();
   
    protected abstract void addFeaturesToFake(FakeVariant v);
    
    private List<Feature> createFeatures(String chr, int start, int end) {
        
        int expected = (int) ((end-start)/MB*getExpectedPerMb());
        List<Feature> res = new ArrayList<Feature>();
        
        int g = 0;
        int l = 0;
        for (int i = 0; i < expected; i++) {
            int delta = end-start;
            
            if (Math.random()>0.5) {
                int size =(int)( Math.random()*getMaxSize());
                int s = (int) (Math.random()*delta+start);
                int e = Math.min(s+size, end);
                FakeVariant v = new FakeVariant(chr, s, e);
                addFeaturesToFake(v);
                res.add(v);     
                
                if (s < start || e > end || end < start) {
                   err("Features coords out of bounds") ;
                }
            }
        }
        if (g+l > expected) {
            err("Too many features");
        }
        p("--- Creating fake CNVs between "+start+"-"+end+", expected: "+expected+", gains: "+g+", losses: "+l);
        if (g+l > expected) {
            err("Too many features, expected was "+expected);
        }
        return res;
    }
    
     protected void p(String s) {
        //Logger.getLogger("FakeCnvTrack").info(s);
         System.out.println("FakeCnvTrack:"+s);
    }
     
     protected void err(String s) {
        //Logger.getLogger("FakeCnvTrack").warning(s);
         System.out.println("ERROR IN FakeCnvTrack:"+s);
         System.exit(0);
    }
}
