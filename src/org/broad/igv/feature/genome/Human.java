/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.feature.genome;

import com.iontorrent.karyo.data.KaryoFeature;
import com.iontorrent.karyo.data.Range;
import java.util.List;
import java.util.logging.Logger;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.Cytoband;

/**
 *
 * @author Chantal
 */
public class Human {
    
      static Cytoband pary1 = new Cytoband("chry", "py1", 100001, 2649520);
      static Cytoband pary2 = new Cytoband("chry", "py2", 59034050, 59363566);
      static Cytoband parx1 = new Cytoband("chrx", "px1", 600001, 2649520);
      static Cytoband parx2 = new Cytoband("chrx", "px2", 154931044, 155260560);
   
      static final Logger log = Logger.getLogger("Human");
      
      
      public static void addParBands(Chromosome chromosome,  List<Cytoband> cytobands) {          
            
            if (isY(chromosome.getName())) {
               // p("Adding par bands to y chromosome: "+chromosome.getName());
                cytobands.add(pary1);
                cytobands.add(pary2);
            //    p("adding "+pary1);
             //   p("adding "+pary2);
            } 
            else if (isSexChromosome(chromosome.getName())) {
              //  p("Adding par bands to X chromosome "+chromosome.getName());
                cytobands.add(parx1);
                cytobands.add(parx2);
              //  p("adding "+parx1);
               // p("adding "+parx2);
            }
            
      }
    public static boolean isSexChromosome(String name) {
        boolean sex = name.equalsIgnoreCase("chrx") || name.equalsIgnoreCase("x") || name.equalsIgnoreCase("23") || isY(name);
        // if (sex) Logger.getLogger("Chromsome").info("Chr "+name+" is a sex chromosome");
        return sex;
    }


      public static boolean isPar(boolean sampleIsMale, String chr, Range target) {

        // no matter which gender, Y is special
        if (isY(chr)) {
            // y and par region -> so it is par
            if (pary1.overlaps(target) || pary2.overlaps(target)) {
             //   p("Y in par region: "+target+"-> par");
                return true;
            } // y bot not in par region, so not par
            else {
              //  p("Y in NON-par region: "+target+"-> false");
                return false;
            }
        } 
        // not male and not y, so par
        if (!sampleIsMale) return true;
                
        // male and neither X nor Y, so par
        if (!isX(chr)) return true;
        
        // male and X
        // male x and par region -> par
        if (parx1.overlaps(target) || parx2.overlaps(target)) {
           // p("male on x, in par -> par: +"+ target);
            return true;
        } else {
            // male and not par region on x
           // p("male on x, NOT in par -> not par: +"+ target);
            return false;
        }       
    }
      public static boolean isX(String name) {
        boolean y = name.equalsIgnoreCase("x") || name.equalsIgnoreCase("chrx") || name.equalsIgnoreCase("23");
        //   if (y) Logger.getLogger("Chromsome").info("Chr "+name+" is a y chromosome");
        return y;
    }

    public static boolean isY(String name) {
        boolean y = name.equalsIgnoreCase("y") || name.equalsIgnoreCase("chry") || name.equalsIgnoreCase("24");
        //   if (y) Logger.getLogger("Chromsome").info("Chr "+name+" is a y chromosome");
        return y;
    }
      private static void p(String s) {
          log.info(s);
      }
}
