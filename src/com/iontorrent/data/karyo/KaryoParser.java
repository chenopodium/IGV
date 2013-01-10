/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.data.karyo;

import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author Chantal Roth
 */
public class KaryoParser {

    private ArrayList<Band> bands;
    private ArrayList<Chromosome> chromosomes;
    private HashMap<String,Chromosome> chrmap;
    
    
    /**
     * chr - hs18 18 0 76117153 green
chr - hs19 19 0 63811651 green
chr - hs20 20 0 62435964 green
chr - hsX X 0 154913754 green
chr - hsY Y 0 57772954 green
band hs1 p36.33 p36.33 0 2300000 gneg
band hs1 p36.23 p36.23 7100000 9200000 gpos25
     */
    
    public KaryoParser(String filename) {
        chromosomes = new ArrayList<Chromosome> ();
        bands  = new ArrayList<Band>();
        chrmap = new HashMap<String,Chromosome>();
        parseFile(filename);
    }
    
    private void parseFile(String filename) {
        p("Parsing file "+filename);
        InputStream is = this.getClass().getResourceAsStream(filename);
        String content = FileTools.getIsAsString(is);
        ArrayList<String> lines = StringTools.parseList(content, "\n");
        for (String line: lines) {
            if (line != null) {
                processLine(line);
            }
        }
    }
    private void processLine(String line) {
        ArrayList<String> items = StringTools.parseList(line, " ");    
     //   p("Got line: "+line+", items: "+items);
        if (items != null && items.size()>1) {
            if (line.startsWith("chr")) {
                processChr(items);
            }
            else if (line.startsWith("band")) {
                processBand(items);
            }
        }
    }
    private void processChr(ArrayList<String> items ) {
       
        if (items.size()<7) {
            err("Not enough items in line, need 7: "+items);
            return;                    
        }
        String name = items.get(3);
        String chrid = items.get(2);        
        long length = Long.parseLong(items.get(5));
        String type = items.get(6);
         
        Chromosome chr = new  Chromosome( name,  chrid,  length,  type);
        chromosomes.add(chr);
        chrmap.put(chrid, chr);
      //   p("Got a chr: "+chr);
        
    }
    private void processBand(ArrayList<String> items ) {
       
        if (items.size()<7) {
            err("Not enough items in line, need 7: "+items);
            return;                    
        }
        String name = items.get(3);
        String chrid = items.get(1);
        long start = Integer.parseInt(items.get(4));
        long end = Integer.parseInt(items.get(5));
        String type = items.get(6);
        Band band = new  Band( name,  chrid,  start,  end,  type);
        bands.add(band);
        
        Chromosome chr =  chrmap.get(chrid);
        if (chr == null) {
            err("Could not find parent chr "+chrid);
        }
        else {
            chr.add(band);        
        }
    //     p("Got a band: "+band);
    }
    
    private void p(String msg) {
        Logger.getLogger("KaryoParser").info(msg);
    }
     private void err(String msg) {
        Logger.getLogger("KaryoParser").warning(msg);
    }
    public static void main(String[] args) {
        KaryoParser parser = new KaryoParser("karyotype.human.txt");
    }


    /**
     * @return the bands
     */
    public ArrayList<Band> getBands() {
        return bands;
    }

    /**
     * @return the chromosomes
     */
    public ArrayList<Chromosome> getChromosomes() {
        return chromosomes;
    }

   
}