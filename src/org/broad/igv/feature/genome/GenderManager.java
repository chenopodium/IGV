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
public class GenderManager {

    static final Logger log = Logger.getLogger("GenderManager");

    static boolean isHuman;
    
    public static boolean isFemale(Genome genome, List<Chromosome> chromosomesWithData) {
        if (isHuman(genome)) {
            for (Chromosome chr : chromosomesWithData) {
                if (Human.isY(chr.getName())) {
                    return false;
                }
            }
            return true;
        }
        return true;
    }
    public static boolean isPar(boolean male, String chr,Range r) {
        if (isHuman) {
            return Human.isPar(male, chr, r);
        }
        else return true;
    }
    public  static void addParBands(Genome genome, Chromosome chromosome, List<Cytoband> cytobands ) {
         if (isHuman(genome)) {
            Human.addParBands(chromosome, cytobands);
        }
         
    }
    public  static boolean isHuman(Genome genome) {
          boolean h=  genome.getDisplayName().startsWith("hg") || genome.getId().startsWith("hg");
          if (!h) {
               p("================ Genome is NOT human: "+genome.getId()+"/"+genome.getDisplayName());
          }
          isHuman = h;
          return h;
    }
    
    public static boolean isSexChromosome(String chr) {
        if (!isHuman){
            p("================ Genome is NOT human");
            return false;
        }
        return Human.isSexChromosome(chr);
    }
     public static boolean isY(String chr) {
        if (!isHuman) {
            p("================ Genome is NOT human");
            return false;
        }
        return Human.isY(chr);
    }
      public static boolean isX(String chr) {
        if (!isHuman){
            p("================ Genome is NOT human");
            return false;
        }
        return Human.isX(chr);
    }
    private static void p(String s) {
        log.info(s);
    }
}
