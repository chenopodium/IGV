package org.broad.igv.util.ucsc;


import org.broad.igv.feature.BasicFeature;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.feature.tribble.PSLCodec;
import org.broad.igv.ui.IGV;
import org.broad.igv.util.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.broad.igv.Globals;
import org.broad.tribble.Feature;

/**
 * Port of perl script blatPlot.pl   http://genomewiki.cse.ucsc.edu/index.php/Blat_Scripts
 *
 * @author jrobinso
 *         Date: 11/21/12
 *         Time: 8:28 AM
 */
public class BlatClient {

    static int sleepTime = 120 * 1000;  //	#	milli seconds to sleep after request returned

    static String hgsid;  // cached, not sure what this is for but apparently its best to reuse it.
    static long lastQueryTime = 0;

    static void Usage() {
        System.out.println("usage: BlatBot <organism> <db> <searchType> <sortOrder>");
        System.out.println(" <outputType> <querySequence>");
        System.out.println("\tSpecify organism using the common name with first letter");
        System.out.println("capitalized.");
        System.out.println("\te.g. Human, Mouse, Rat etc.");
        System.out.println("\tDb is database or assembly name e.g hg17, mm5, rn3 etc.");
        System.out.println("\tsearchType can be BLATGuess, DNA, RNA, transDNA or transRNA");
        System.out.println("\tsortOrder can be query,score; query,start; chrom,score");
        System.out.println("\tchrom,start; score.");
        System.out.println("\toutputType can be pslNoHeader, psl or hyperlink.");
        System.out.println("\tblats will be run in groups of $batchCount sequences, all");
    }


    public static void main(String[] args) throws IOException {

        if (args.length != 6) {
            Usage();
            System.exit(255);
        }

        String org = args[0];
        String db = args[1];
        String searchType = args[2];
        String sortOrder = args[3];
        String outputType = args[4];
        String userSeq = args[5];


        blat(org, db, searchType, sortOrder, outputType, userSeq);



    }

    public static List<Feature>  blat(String org, String db, String userSeq) throws IOException {
        String searchType = "DNA";
        String sortOrder = "query,score";
        String outputType = "psl";
        return blat(org, db, searchType, sortOrder, outputType, userSeq);
    }

    public static List<Feature>  blat(String org, String db, String searchType, String sortOrder, String outputType, String userSeq) throws IOException {
        if (searchType.equals("BLATGuess")) {
            searchType = "Blat's Guess";
        } else if (searchType.equals("transDNA")) {
            searchType = "translated DNA";
        } else if (searchType.equals("transRNA")) {
            searchType = "translated RNA";
        } else if (searchType.equals("DNA") || (searchType.equals("RNA"))) {
        } else {
            System.out.println("ERROR: have not specified an acceptable search type - it should be BLATGuess, transDNA, transRNA, DNA or RNA.");
            Usage();
            System.exit(255);
        }
        if (outputType.equals("pslNoHeader")) {
            outputType = "psl no header";
        } else if (outputType.equals("psl") || outputType.equals("hyperlink")) {
        } else {
            System.out.println("ERROR: have not specified an acceptable output type - it should be pslNoHeader, psl or hyperlink.");
            Usage();
            System.exit(255);
        }

        //$response;
        String $url = "http://genome.cse.ucsc.edu/cgi-bin/hgBlat";

        //if an hgsid was obtained from the output of the first batch
        //then use this.

        String urlString = ($url + "?org=" + org + "&db=" + db + "&type=" + searchType + "&sort=" + sortOrder +
                "&output=" + outputType + "&userSeq=" + userSeq); // + "&hgsid=" + hgsid);
        if (hgsid != null) {
            urlString += "&hgsid=" + hgsid;
        }

        long dt = System.currentTimeMillis() - lastQueryTime;
        if (dt < sleepTime) {
            try {
                Thread.sleep(dt);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        lastQueryTime = System.currentTimeMillis();

        String result = HttpUtils.getInstance().getContentsAsString(new URL(urlString));

        return parseResult(result);
    }

    static List<Feature> parseResult(String result) throws IOException {

        ArrayList<Feature> features = new ArrayList<Feature>();

        Genome genome = null;
        if(IGV.hasInstance()) {
            genome = GenomeManager.getInstance().getCurrentGenome();
        }

        PSLCodec codec = new PSLCodec(genome);

        BufferedReader br = new BufferedReader(new StringReader(result));
        String line;
        int headerLineCount = 0;
        boolean header = false;
        boolean hgsidFound = false;
        boolean pslSectionFound = false;
        while ((line = br.readLine()) != null) {

            if (line.contains("hgsid=") && !hgsidFound) {
                int startIDX = line.indexOf("hgsid=") + 6;
                String sub = line.substring(startIDX);
                int endIDX = sub.indexOf("\"");
                if(endIDX < 0) endIDX = sub.indexOf("&");
                if(endIDX > 0) {
                    hgsid = sub.substring(0, endIDX);
                    System.out.println("hgsid = " + hgsid);
                    hgsidFound = true;
                }
            }
            if (line.trim().startsWith("<TT><PRE>")) {
                pslSectionFound = true;
                if (line.contains("psLayout") && line.contains("version")) {
                    header = true;
                    headerLineCount++;
                }
            } else if (line.trim().startsWith("</PRE></TT>")) {
                break;
            }

            if (pslSectionFound) {
                if (header && headerLineCount < 6) {
                    headerLineCount++;
                    continue;
                }

                String[] tokens = Globals.whitespacePattern.split(line);
                if (tokens.length != 21) {
                    System.err.println("Unexpected number of fields (" + tokens.length + ")");
                    System.err.println(line);
                } else {
                    features.add(codec.decode(tokens));
                }
            }
        }
        return features;
    }
}

