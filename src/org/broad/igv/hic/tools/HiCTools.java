/*
 * Copyright (c) 2007-2011 by The Broad Institute of MIT and Harvard.  All Rights Reserved.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 *
 * THE SOFTWARE IS PROVIDED "AS IS." THE BROAD AND MIT MAKE NO REPRESENTATIONS OR
 * WARRANTES OF ANY KIND CONCERNING THE SOFTWARE, EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, NONINFRINGEMENT, OR THE ABSENCE OF LATENT OR OTHER DEFECTS, WHETHER
 * OR NOT DISCOVERABLE.  IN NO EVENT SHALL THE BROAD OR MIT, OR THEIR RESPECTIVE
 * TRUSTEES, DIRECTORS, OFFICERS, EMPLOYEES, AND AFFILIATES BE LIABLE FOR ANY DAMAGES
 * OF ANY KIND, INCLUDING, WITHOUT LIMITATION, INCIDENTAL OR CONSEQUENTIAL DAMAGES,
 * ECONOMIC DAMAGES OR INJURY TO PROPERTY AND LOST PROFITS, REGARDLESS OF WHETHER
 * THE BROAD OR MIT SHALL BE ADVISED, SHALL HAVE OTHER REASON TO KNOW, OR IN FACT
 * SHALL KNOW OF THE POSSIBILITY OF THE FOREGOING.
 */

package org.broad.igv.hic.tools;

import jargs.gnu.CmdLineParser;
import net.sf.samtools.util.CloseableIterator;
import org.broad.igv.Globals;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.ChromosomeImpl;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.hic.data.*;
import org.broad.igv.sam.Alignment;
import org.broad.igv.sam.ReadMate;
import org.broad.igv.sam.reader.AlignmentReader;
import org.broad.igv.sam.reader.AlignmentReaderFactory;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.util.FileUtils;
import org.broad.igv.util.ParsingUtils;
import org.broad.tribble.util.LittleEndianInputStream;
import org.broad.tribble.util.LittleEndianOutputStream;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Jim Robinson
 * @date 9/16/11
 */
public class HiCTools {

    public static void usage() {
        System.out.println("Usage: hictools sort <infile> <outfile>");
        System.out.println("       hictools pairsToBin <infile> <outfile> <genomeID>");
        System.out.println("       hictools binToPairs <infile> <outfile>");
        System.out.println("       hictools printmatrix <observed/oe/pearson> <hicFile> <chr1> <chr2> <binsize> [outfile]");
        System.out.println("       hictools eigenvector <hicFile> <chr> <binsize>");
        System.out.println("       hictools pre <options> <infile> <outfile> <genomeID>");
        System.out.println("  <options>: -d only calculate intra chromosome (diagonal) [false]");
        System.out.println("           : -f <restriction site file> calculate fragment map");
        System.out.println("           : -m <int> only write cells with count above threshold m [0]");
        System.out.println("           : -c <chromosome ID> only calculate map on specific chromosome");
        System.out.println("           : -h print help");
    }

    public static void main(String[] argv) throws IOException, CmdLineParser.UnknownOptionException, CmdLineParser.IllegalOptionValueException {

        Globals.setHeadless(true);

        CommandLineParser parser = new CommandLineParser();
        parser.parse(argv);
        String[] args = parser.getRemainingArgs();

        if (parser.getHelpOption()) {
            usage();
            System.exit(0);
        }

        if (args.length < 1) {
            usage();
            System.exit(1);
        }

        if (args[0].equalsIgnoreCase("db")) {
            String[] tmp = new String[args.length - 1];
            System.arraycopy(args, 1, tmp, 0, args.length - 1);
            try {
                HiCDBUtils.main(tmp);
            } catch (SQLException e) {
                System.err.println("Sql exception: " + e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }
        } else if (args[0].equalsIgnoreCase("fragmentToBed")) {
            if (args.length != 2) {
                System.out.println("Usage: hictools fragmentToBed <fragmentFile>");
                System.exit(1);
            }
            fragmentToBed(args[1]);
        } else if (args[0].equalsIgnoreCase("bpToFrag")) {
            if (args.length != 4) {
                System.out.println("Usage: hictools bpToFrag <fragmentFile> <inputBedFile> <outputFile>");
            }
            bpToFrag(args[1], args[2], args[3]);
        } else if (args[0].equals("sort")) {
            if (args.length != 3) {
                usage();
                System.exit(1);
            }
            AlignmentsSorter.sort(args[1], args[2], null);
        } else if (args[0].equals("pairsToBin")) {
            if (args.length != 4) {
                usage();
                System.exit(1);
            }
            String ifile = args[1];
            String ofile = args[2];
            String genomeId = args[3];
            List<Chromosome> chromosomes = loadChromosomes(genomeId);
            AsciiToBinConverter.convert(ifile, ofile, chromosomes);
        } else if (args[0].equals("binToPairs")) {
            if (args.length != 3) {
                usage();
                System.exit(1);
            }
            String ifile = args[1];
            String ofile = args[2];
            AsciiToBinConverter.convertBack(ifile, ofile);
        } else if (args[0].equals("printmatrix")) {
            if (args.length != 6 && args.length != 7) {
                usage();
                System.exit(1);
            }
            String type = args[1];
            String file = args[2];
            String chr1 = args[3];
            String chr2 = args[4];
            String binSizeSt = args[5];
            int binSize = 0;
            try {
                binSize = Integer.parseInt(binSizeSt);
            } catch (NumberFormatException e) {
                System.err.println("Integer expected for bin size.  Found: " + binSizeSt);
                System.exit(1);
            }
            String ofile = null;
            if (args.length == 7) {
                ofile = args[6];
            }

            dumpMatrix(file, chr1, chr2, binSize, type, ofile);

        } else if (args[0].equals("eigenvector")) {
            if (args.length < 4) {
                System.err.println("Usage: hictools eigenvector hicFile chr binsize");
            }
            String file = args[1];
            String chr = args[2];
            String binSizeSt = args[3];
            int binSize = 0;
            try {
                binSize = Integer.parseInt(binSizeSt);
            } catch (NumberFormatException e) {
                System.err.println("Integer expected.  Found: " + binSizeSt);
                System.exit(-1);
            }
            calculateEigenvector(file, chr, binSize);
        } else if (args[0].equals("pre")) {
            String genomeId = "";
            try {
                genomeId = args[3];
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("No genome ID given");
                System.exit(0);
            }
            List<Chromosome> chromosomes = loadChromosomes(genomeId);

            long genomeLength = 0;
            for (Chromosome c : chromosomes) {
                if (c != null)
                    genomeLength += c.getLength();
            }
            chromosomes.set(0, new ChromosomeImpl(0, "All", (int) (genomeLength / 1000)));

            String[] tokens = args[1].split(",");
            List<String> files = new ArrayList<String>(tokens.length);

            for (String f : tokens) {
                files.add(f);
            }

            Preprocessor preprocessor = new Preprocessor(new File(args[2]), genomeId, chromosomes);

            preprocessor.setIncludedChromosomes(parser.getChromosomeOption());
            preprocessor.setCountThreshold(parser.getCountThresholdOption());
            preprocessor.setNumberOfThreads(parser.getThreadedOption());
            preprocessor.setDiagonalsOnly(parser.getDiagonalsOption());
            preprocessor.setFragmentFile(parser.getFragmentOption());
            preprocessor.preprocess(files);
        } else {
            usage();
            System.exit(1);
        }

    }

    /**
     * Load chromosomes from given ID or file name.
     *
     * @param idOrFile Genome ID or file name where chromosome lengths written
     * @return Chromosome lengths
     * @throws IOException if chromosome length file not found
     */
    public static List<Chromosome> loadChromosomes(String idOrFile) throws IOException {

        InputStream is = null;

        try {
            // Note: to get this to work, had to edit Intellij settings
            // so that "?*.sizes" are considered sources to be copied to class path
            is = HiCTools.class.getResourceAsStream(idOrFile + ".chrom.sizes");

            if (is == null) {
                // Not an ID,  see if its a file
                File file = new File(idOrFile);
                if (file.exists()) {
                    is = new FileInputStream(file);
                } else {
                    throw new FileNotFoundException("Could not find chromosome sizes file for: " + idOrFile);
                }

            }

            List<Chromosome> chromosomes = new ArrayList();
            chromosomes.add(0, null);   // Index 0 reserved for "whole genome" pseudo-chromosome

            Pattern pattern = Pattern.compile("\t");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String nextLine;
            long genomeLength = 0;
            int idx = 1;

            while ((nextLine = reader.readLine()) != null) {
                String[] tokens = pattern.split(nextLine);
                if (tokens.length == 2) {
                    String name = tokens[0];
                    int length = Integer.parseInt(tokens[1]);
                    genomeLength += length;
                    chromosomes.add(idx, new ChromosomeImpl(idx, name, length));
                    idx++;
                } else {
                    System.out.println("Skipping " + nextLine);
                }
            }

            // Add the "pseudo-chromosome" All, representing the whole genome.  Units are in kilo-bases
            chromosomes.set(0, new ChromosomeImpl(0, "All", (int) (genomeLength / 1000)));


            return chromosomes;
        } finally {
            if (is != null) is.close();
        }

    }


    public static void bpToFrag(String fragmentFile, String inputFile, String outputDir) throws IOException {
        BufferedReader fragmentReader = null;
        Pattern pattern = Pattern.compile("\\s");
        Map<String, int[]> fragmentMap = new HashMap<String, int[]>();  // Map of chr -> site positions
        try {
            fragmentReader = new BufferedReader(new FileReader(fragmentFile));

            String nextLine;
            while ((nextLine = fragmentReader.readLine()) != null) {
                String[] tokens = pattern.split(nextLine);

                // A hack, could use IGV's genome alias definitions
                String chr = getChrAlias(tokens[0]);

                int[] sites = new int[tokens.length];
                sites[0] = 0;  // Convenient convention
                for (int i = 1; i < tokens.length; i++) {
                    sites[i] = Integer.parseInt(tokens[i]) - 1;
                }
                fragmentMap.put(chr, sites);
            }
        } finally {
            fragmentReader.close();
        }

        // inputFile contains a list of files or URLs.
        BufferedReader reader = null;
        try {
            File dir = new File(outputDir);
            if (!dir.exists() || !dir.isDirectory()) {
                System.out.println("Output directory does not exist, or is not directory");
                System.exit(1);
            }
            reader = new BufferedReader(new FileReader(inputFile));
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                String path = nextLine.trim();
                int lastSlashIdx = path.lastIndexOf("/");
                if (lastSlashIdx < 0) lastSlashIdx = path.lastIndexOf("\\");  // Windows convention
                String fn = lastSlashIdx < 0 ? path : path.substring(lastSlashIdx);

                File outputFile = new File(dir, fn + ".sites");
                annotateWithSites(fragmentMap, path, outputFile);

            }
        } finally {
            if (reader != null) reader.close();
        }
    }


    /**
     * Find fragments that overlap the input bed file.  Its assumed the bed file is sorted by start position, otherwise
     * an exception is thrown.  If a fragment overlaps 2 or more bed featuers it is only outputted once.
     *
     * @param fragmentMap
     * @param bedFile
     * @param outputBedFile
     * @throws IOException
     */

    static void annotateWithSites(Map<String, int[]> fragmentMap, String bedFile, File outputBedFile) throws IOException {


        BufferedReader bedReader = null;
        PrintWriter bedWriter = null;
        try {

            bedReader = ParsingUtils.openBufferedReader(bedFile);
            bedWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputBedFile)));

            String nextLine;
            while ((nextLine = bedReader.readLine()) != null) {
                if (nextLine.startsWith("track") || nextLine.startsWith("browser") || nextLine.startsWith("#"))
                    continue;

                BedLikeFeature feature = new BedLikeFeature(nextLine);

                String[] tokens = Globals.whitespacePattern.split(nextLine);
                String chr = tokens[0];
                int start = Integer.parseInt(tokens[1]);
                int end = Integer.parseInt(tokens[2]);

                int[] sites = fragmentMap.get(feature.getChr());
                if (sites == null) continue;

                int firstSite = FragmentCalculation.binarySearch(sites, feature.getStart());
                int lastSite = FragmentCalculation.binarySearch(sites, feature.getEnd());

                bedWriter.print(chr + "\t" + start + "\t" + end + "\t" + firstSite + "\t" + lastSite);
                for (int i = 3; i < tokens.length; i++) {
                    bedWriter.print("\t" + tokens[i]);
                }
                bedWriter.println();


            }
        } finally {
            if (bedReader != null) bedReader.close();
            if (bedWriter != null) bedWriter.close();
        }


    }

    private static String getChrAlias(String token) {
        if (token.equals("MT")) {
            return "chrM";
        } else if (!token.startsWith("chr")) {
            return "chr" + token;
        } else {
            return token;
        }
    }

    /**
     * Convert a fragment site file to a "bed" file
     *
     * @param filename
     * @throws IOException
     */
    public static void fragmentToBed(String filename) throws IOException {
        BufferedReader reader = null;
        PrintWriter writer = null;
        try {
            File inputFile = new File(filename);
            reader = new BufferedReader(new FileReader(inputFile));

            writer = new PrintWriter(new BufferedWriter(new FileWriter(filename + ".bed")));

            Pattern pattern = Pattern.compile("\\s");
            String nextLine;
            while ((nextLine = reader.readLine()) != null) {
                String[] tokens = pattern.split(nextLine);
                String chr = tokens[0];
                int beg = Integer.parseInt(tokens[1]) - 1;  // 1 vs 0 based coords
                for (int i = 2; i < tokens.length; i++) {
                    int end = Integer.parseInt(tokens[i]) - 1;
                    writer.println(chr + "\t" + beg + "\t" + end);
                    beg = end;
                }

            }
        } finally {
            reader.close();
        }

    }


    /**
     * Convert a BAM file containing paried-end tags to the ascii "pair" format used for HiC.
     *
     * @param inputBam
     * @param outputFile
     * @throws IOException
     */
    public static void filterBam(String inputBam, String outputFile, List<Chromosome> chromosomes) throws IOException {

        CloseableIterator<Alignment> iter = null;
        AlignmentReader reader = null;
        PrintWriter pw = null;

        HashSet allChroms = new HashSet(chromosomes);

        try {
            pw = new PrintWriter(new FileWriter(outputFile));
            reader = AlignmentReaderFactory.getReader(inputBam, false);
            iter = reader.iterator();
            while (iter.hasNext()) {

                Alignment alignment = iter.next();
                ReadMate mate = alignment.getMate();

                // Filter unpaired and "normal" pairs.  Only interested in abnormals
                if (alignment.isPaired() &&
                        alignment.isMapped() &&
                        alignment.getMappingQuality() > 10 &&
                        mate != null &&
                        mate.isMapped() &&
                        allChroms.contains(alignment.getChr()) &&
                        allChroms.contains(mate.getChr()) &&
                        (!alignment.getChr().equals(mate.getChr()) || alignment.getInferredInsertSize() > 1000)) {

                    // Each pair is represented twice in the file,  keep the record with the "leftmost" coordinate
                    if (alignment.getStart() < mate.getStart()) {
                        String strand = alignment.isNegativeStrand() ? "-" : "+";
                        String mateStrand = mate.isNegativeStrand() ? "-" : "+";
                        pw.println(alignment.getReadName() + "\t" + alignment.getChr() + "\t" + alignment.getStart() +
                                "\t" + strand + "\t.\t" + mate.getChr() + "\t" + mate.getStart() + "\t" + mateStrand);
                    }
                }

            }
        } finally {
            pw.close();
            iter.close();
            reader.close();
        }
    }

    static void calculateEigenvector(String file, String chr, int binsize) throws IOException {
        if (!file.endsWith("hic")) {
            System.err.println("Only 'hic' files are supported");
            System.exit(-1);

        }
        Dataset dataset = (new DatasetReaderV1(file)).read();

        // Load the expected density function, if it exists.
        //Map<Integer, DensityFunction> zoomToDensityMap = null;

        if (dataset.getVersion() <= 1) {

            throw new RuntimeException("Unsupported dataset version");
//            String densityFile = file + ".densities";
//            if (FileUtils.resourceExists(densityFile)) {
//                InputStream is = null;
//                try {
//                    is = ParsingUtils.openInputStream(densityFile);
//                    zoomToDensityMap = DatasetReaderV1.readDensities(new LittleEndianInputStream(new BufferedInputStream(is)));
//                    dataset.setZoomToDensity();
//                } finally {
//                    if (is != null) is.close();
//                }
//            } else {
//                System.err.println("Densities file doesn't exist");
//                System.exit(-1);
//            }

        }

        Chromosome[] tmp = dataset.getChromosomes();

        Map<String, Chromosome> chromosomeMap = new HashMap<String, Chromosome>();
        for (Chromosome c : tmp) {
            chromosomeMap.put(c.getName(), c);
        }

        if (!chromosomeMap.containsKey(chr)) {
            System.err.println("Unknown chromosome: " + chr);
            System.exit(-1);
        }
        int zoomIdx = 0;
        boolean found = false;
        for (; zoomIdx < dataset.getNumberZooms(); zoomIdx++) {
            if (dataset.getZoom(zoomIdx) == binsize) {
                found = true;
                break;
            }
        }

        if (!found) {
            System.err.println("Unknown bin size: " + binsize);
            System.exit(-1);
        }

        Matrix matrix = dataset.getMatrix(chromosomeMap.get(chr), chromosomeMap.get(chr));
        MatrixZoomData zd = matrix.getObservedMatrix(zoomIdx);
        final DensityFunction df = dataset.getDensityFunction(zd.getZoom()); // zoomToDensityMap.get(zd.getZoom());
        double[] eigenvector = zd.computeEigenvector(df, 0);
        for (double ev : eigenvector)
            System.out.print(ev + " ");
        System.out.println();
    }

    static void dumpMatrix(String file, String chr1, String chr2, int binsize, String type, String ofile) throws IOException {

        if (!file.endsWith("hic")) {
            System.err.println("Only 'hic' files are supported");
            System.exit(-1);
        }
        // Load the expected density function, if it exists.

        LittleEndianOutputStream les = null;
        BufferedOutputStream bos = null;
        Dataset dataset = (new DatasetReaderV1(file)).read();

        if (ofile != null) {
            bos = new BufferedOutputStream(new FileOutputStream(ofile));
            les = new LittleEndianOutputStream(bos);
        }

        Chromosome[] tmp = dataset.getChromosomes();

        Map<String, Chromosome> chromosomeMap = new HashMap<String, Chromosome>();
        for (Chromosome c : tmp) {
            chromosomeMap.put(c.getName(), c);
        }

        if (!chromosomeMap.containsKey(chr1)) {
            System.err.println("Unknown chromosome: " + chr1);
            System.exit(-1);
        } else if (!chromosomeMap.containsKey(chr2)) {
            System.err.println("Unknown chromosome: " + chr2);
            System.exit(-1);
        }
        if (type.equals("oe") || type.equals("pearson")) {
            if (!chr1.equals(chr2)) {
                System.err.println("Chromosome " + chr1 + " not equal to Chromosome " + chr2);
                System.err.println("Currently only intrachromosomal O/E and Pearson's are supported.");
                System.exit(-1);
            }
        }

        int zoomIdx = 0;
        boolean found = false;
        for (; zoomIdx < dataset.getNumberZooms(); zoomIdx++) {
            if (dataset.getZoom(zoomIdx) == binsize) {
                found = true;
                break;
            }
        }
        if (!found) {
            System.err.println("Unknown bin size: " + binsize);
        }

        Matrix matrix = dataset.getMatrix(chromosomeMap.get(chr1), chromosomeMap.get(chr2));
        MatrixZoomData zd = matrix.getObservedMatrix(zoomIdx);
        if (type.equals("oe") || type.equals("pearson")) {
            final DensityFunction df = dataset.getDensityFunction(zd.getZoom());
            if (df == null) {
                System.err.println("Densities not calculated to this resolution.");
                System.exit(-1);
            }
            try {
                zd.dumpOE(df, type.equals("oe"), les);
            } finally {
                if (les != null)
                    les.close();
                if (bos != null)
                    bos.close();
            }
        } else {
            try {
                zd.dump(les);
            } finally {
                if (les != null)
                    les.close();
                if (bos != null)
                    bos.close();
            }
        }
    }


    static class CommandLineParser extends CmdLineParser {
        private Option diagonalsOption = null;
        private Option chromosomeOption = null;
        private Option countThresholdOption = null;
        private Option threadedOption = null;
        private Option helpOption = null;
        private Option fragmentOption = null;

        CommandLineParser() {
            diagonalsOption = addBooleanOption('d', "diagonals");
            chromosomeOption = addStringOption('c', "chromosomes");
            countThresholdOption = addIntegerOption('m', "minCountThreshold");
            threadedOption = addIntegerOption('t', "threads");
            fragmentOption = addStringOption('f', "restriction fragment site file");
            helpOption = addBooleanOption('h', "help");
        }

        boolean getHelpOption() {
            Object opt = getOptionValue(helpOption);
            return opt == null ? false : ((Boolean) opt).booleanValue();
        }

        boolean getDiagonalsOption() {
            Object opt = getOptionValue(diagonalsOption);
            return opt == null ? false : ((Boolean) opt).booleanValue();
        }

        Set<String> getChromosomeOption() {
            Object opt = getOptionValue(chromosomeOption);
            if (opt != null) {
                String[] tokens = opt.toString().split(",");
                return new HashSet<String>(Arrays.asList(tokens));
            } else {
                return null;
            }
        }

        String getFragmentOption() {
            Object opt = getOptionValue(fragmentOption);
            if (opt != null) {
                return opt.toString();
            } else {
                return null;
            }
        }


        int getCountThresholdOption() {
            Object opt = getOptionValue(countThresholdOption);
            return opt == null ? 0 : ((Number) opt).intValue();
        }

        int getThreadedOption() {
            Object opt = getOptionValue(threadedOption);
            return opt == null ? 0 : ((Number) opt).intValue();

        }
    }


    static class BedLikeFeature implements LocusScore {

        String chr;
        int start;
        int end;
        String name;
        String line;

        BedLikeFeature(String line) {
            this.line = line;
            String[] tokens = Globals.whitespacePattern.split(line);
            this.chr = tokens[0];
            this.start = Integer.parseInt(tokens[1]);
            this.end = Integer.parseInt(tokens[2]);
            if (tokens.length > 3) {
                this.name = name;
            }

        }

        @Override
        public String getValueString(double position, WindowFunction windowFunction) {
            return line;
        }

        public String getChr() {
            return chr;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public float getScore() {
            return 0;
        }
    }
}
