/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

package org.broad.igv.feature.tribble;

import java.awt.Color;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
import org.broad.igv.exceptions.ParserException;
import org.broad.igv.feature.BasicFeature;
import org.broad.igv.feature.FeatureDB;
import org.broad.igv.feature.Strand;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.color.ColorUtilities;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.StringUtils;
import org.broad.igv.util.collections.CI;
import org.broad.igv.util.collections.MultiMap;
import org.broad.tribble.AsciiFeatureCodec;
import org.broad.tribble.Feature;
import org.broad.tribble.exception.CodecLineParsingException;
import org.broad.tribble.readers.LineReader;

import java.io.IOException;
import java.util.*;

/**
 * Notes from GFF3 spec  http://www.sequenceontology.org/gff3.shtml
 * These tags have predefined meanings (tags are case sensitive):
 * <p/>
 * ID	   Indicates the name of the feature (unique).
 * Name   Display name for the feature.
 * Alias  A secondary name for the feature.
 * Parent Indicates the parent of the feature.
 * <p/>
 * Specs:
 * GFF3  http://www.sequenceontology.org/gff3.shtml
 * GFF2 specification: http://www.sanger.ac.uk/resources/software/gff/spec.html
 * UCSC GFF (GFF "1") http://genome.ucsc.edu/FAQ/FAQformat#format3
 * GTF  http://mblab.wustl.edu/GTF2.html
 * UCSC GTF  http://genome.ucsc.edu/FAQ/FAQformat#format4
 * Feature type definitions http://www.ebi.ac.uk/embl/Documentation/FT_definitions/feature_table.html#7.2
 */
public class DagCodec extends AsciiFeatureCodec<Feature> {

    private static Logger log = Logger.getLogger(DagCodec.class);

    static Color defaultColor = Color.pink.darker();
    static Color complex = new Color(139,69,19);
    static Color loss = new Color(200,0,0);
    
    static Color inv = new Color(200,0,200);
    static Color gain = new Color(0,0,200);
    static Color dup = new Color(0,200,0);
    
    private TrackProperties trackProperties = null;
    CI.CIHashSet featuresToHide = new CI.CIHashSet();


    FeatureFileHeader header;
    Helper helper;
    Genome genome;

    public enum Version {
        DAG
    }


    /**
     * List of know "Name" fields.  Some important fields from the DAG spec are listed below.  
     * <p/>
     * ID	  Indicates the ID of the feature.
     * Name   Display name for the feature.
     * Alias  A secondary name for the feature.
     */
    static String[] nameFields = {"Name", "name", "Alias", "variantaccession",  "locus", "alias", "ID"};
    String[] headerFields;

    public DagCodec(Genome genome) {
        super(Feature.class);
        // Assume GFF2 until shown otherwise
        helper = new DagHelper();
        this.genome = genome;
    }

    public DagCodec(Version version, Genome genome) {
        super(Feature.class);
        this.genome = genome;
        helper = new DagHelper();        
    }

    public void readHeaderLine(String line) {
        if(header == null) {
            header = new FeatureFileHeader();
        }
        if (line.startsWith("#track") || line.startsWith("##track")) {
            trackProperties = new TrackProperties();
            ParsingUtils.parseTrackLine(line, trackProperties);
            header.setTrackProperties(trackProperties);        
        } else if (line.startsWith("#hide") || line.startsWith("##hide")) {
            String[] kv = line.split("=");
            if (kv.length > 1) {
                featuresToHide.addAll(Arrays.asList(kv[1].split(",")));
            }
        } else if (line.startsWith("#displayName") || line.startsWith("##displayName")) {
            String[] nameTokens = line.split("=");
            if (nameTokens.length < 2) {
                helper.setNameFields(null);
            } else {
                String[] fields = nameTokens[1].split(",");
                helper.setNameFields(fields);
            }
        }
        else if (line.startsWith("variantaccession")) {
          headerFields = line.split("\t");  
          log.info("Got header fields: "+Arrays.toString(headerFields));
        } 
    }

    public Object readHeader(LineReader reader) {

        if(header == null) {
            header = new FeatureFileHeader();
        }
        header = new FeatureFileHeader();
        String line;
        int nLines = 0;
        try {
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("#") || line.startsWith("variantaccession") ) {
                    nLines++;
                    log.info("Reading header line: "+line);
                    readHeaderLine(line);
                } else {
                    break;
                }
            }

            header.setTrackProperties(trackProperties);
            return header;
        } catch (IOException e) {
            throw new CodecLineParsingException("Error parsing header", e);
        }
    }

    /**
     * This function returns true iff the File potentialInput can be parsed by this
     * codec.
     * <p/>
     * There is an assumption that there's never a situation where two different Codecs
     * return true for the same file.  If this occurs, the recommendation would be to error out.
     * <p/>
     * Note this function must never throw an error.  All errors should be trapped
     * and false returned.
     *
     * @param path the file to test for parsability with this codec
     * @return true if potentialInput can be parsed, false otherwise
     */
    public boolean canDecode(String path) {
        final String pathLowerCase = path.toLowerCase();
        return pathLowerCase.endsWith(".dag");
    }

    public BasicFeature decodeLoc(String line) {
        return decode(line);
    }

    public BasicFeature decode(String line) {

        if (line.startsWith("#")) {
            // This should not be possible as this line would be parsed as a header.  But just in case
            return null;
        }

        String[] tokens = Globals.tabPattern.split(line, -1);
        int nTokens = tokens.length;

        // DAG files have 20 tokens,
        // TODO -- the attribute column is optional for GFF 2 and earlier (8 tokens required)
      //  log.info("Got "+nTokens+" tokens: "+Arrays.toString(tokens));
        if (nTokens < 5) {
            return null;
        }
//              0               1       2          3        4               5           6               7               8       9               10
//        variantaccession	chr	start	end	varianttype	variantsubtype	reference	pubmedid	method	platform	mergedvariants	supportingvariants	
  //      mergedorsample	frequency	samplesize	observedgains	observedlosses	cohortdescription	genes	samples
        String chrToken = tokens[1].trim();
        String featureType = StringUtils.intern(tokens[4].trim());
        String subType = StringUtils.intern(tokens[5].trim());
        String chromosome = genome == null ? StringUtils.intern(chrToken) : genome.getChromosomeAlias(chrToken);

        // GFF coordinates are 1-based inclusive (length = end - start + 1)
        // IGV (UCSC) coordinates are 0-based exclusive.  Adjust start and end accordingly
        int start;
        int end;
        int col = 2;
        try {
            start = Integer.parseInt(tokens[col]) - 1;
            if(start < 0) throw new ParserException("Start index must be 1 or larger; DAG is 1-based", -1, line);
            col++;
            end = Integer.parseInt(tokens[col]);
        } catch (NumberFormatException ne) {
            String msg = String.format("Column %d must contain a numeric value. %s", col + 1, ne.getMessage());
            throw new ParserException(msg, -1, line);
        }

        
        //CI.CILinkedHashMap<String> attributes = new CI.CILinkedHashMap();
        MultiMap<String, String> attributes = new MultiMap<String, String>();
        for (int c = 0; c < nTokens; c++) {
           // log.info("Adding att:"+headerFields[c]+"="+ tokens[c]);
            if (c < 1 || c > 3) { 
                String val = tokens[c].trim();
                if (val.length() > 0) attributes.put(headerFields[c], val);
            }
        }
        
        String description = tokens[nTokens-3];//getDescription(attributes, featureType);
        String id = helper.getID(attributes);
       
        Strand strand = Strand.NONE;
        BasicFeature f = new BasicFeature(chromosome, start, end, strand);

        f.setName(getName(attributes));
        f.setType(featureType);
        f.setDescription(description);

        id = id != null ? id : "igv_" + UUID.randomUUID().toString();
        f.setIdentifier(id);

        f.setAttributes(attributes);

        String typename = this.headerFields[5];
        String[] colorNames = new String[]{"color", "Color", "colour", "Colour"};
        
        for(String colorName: colorNames){
            Color c = defaultColor;
            if (attributes.containsKey(colorName)) {
                f.setColor(ColorUtilities.stringToColor(attributes.get(colorName)));
                break;
            }
            else if (subType.equalsIgnoreCase("Gain") || subType.equalsIgnoreCase("insertion")) {
                 c=gain;
            }
            else if (subType.equalsIgnoreCase("Loss") || subType.equalsIgnoreCase("deletion") ) {
                 c= loss;
            }
            else if (subType.equalsIgnoreCase("inversion")) {
                 c=inv;
            }
            else if (subType.equalsIgnoreCase("duplication")) {
                 c=Color.black; ;
            }
            else if (subType.equalsIgnoreCase("complex")) {
                 c=complex;
            }
            else if (subType.equalsIgnoreCase("gain+loss")) {
                 c=complex;
            }
            else if (subType.indexOf('+')>-1 ) {
                 c=complex;
            }
            else if (subType.equalsIgnoreCase("unknown")) {
                 c= Color.black;             
            }
            else  {
                 c= Color.gray;             
            }
            
            f.addHighlightColor(typename, c);
            f.setColor(c);
        }

        if (featuresToHide.contains(featureType)) {
            if (IGV.hasInstance()) FeatureDB.addFeature(f);
            return null;
        }

        return f;

    }

    public Object getHeader() {
        return header;
    }

    private Strand convertStrand(String strandString) {
        Strand strand = Strand.NONE;
        if (strandString.equals("-")) {
            strand = Strand.NEGATIVE;
        } else if (strandString.equals("+")) {
            strand = Strand.POSITIVE;
        }

        return strand;
    }


    String getName(MultiMap<String, String> attributes) {

        if (attributes == null || attributes.size() == 0) {
            return null;
        }
        for (String nf : nameFields) {
            if (attributes.containsKey(nf)) {
                return attributes.get(nf);
            }
        }
        return "";
    }

    static StringBuffer buf = new StringBuffer();

    static String getDescription(MultiMap<String, String> attributes, String type) {
        buf.setLength(0);
        buf.append(type);
        buf.append("<br>");
        attributes.printHtml(buf, 100);
        return buf.toString();
    }


    protected interface Helper {

        String[] getParentIds(MultiMap<String, String> attributes, String attributeString);

        void parseAttributes(String attributeString, MultiMap<String, String> map);

        String getID(MultiMap<String, String> attributes);

        void setUrlDecoding(boolean b);

        String getName(MultiMap<String, String> attributes);

        void setNameFields(String[] fields);

    }

    public static class DagHelper implements Helper {

        //TODO Almost identical
        static String[] idFields = {"variantaccession", "ID", "pubmedid", "name", "primary_name", "gene", "locus", "alias"};
        static String[] DEFAULT_NAME_FIELDS = {"gene", "name", "primary_name", "locus", "alias", "systematic_id", "ID"};
        static String[] possParentNames = new String[]{"id", "mRna", "systematic_id", "transcript_id", "gene", "transcriptId", "Parent", "proteinId"};

        private String[] nameFields;

        DagHelper() {
            this(DEFAULT_NAME_FIELDS);
        }

        DagHelper(String[] nameFields) {
            if (nameFields != null) {
                this.nameFields = nameFields;
            }

        }

        public void setUrlDecoding(boolean b) {
            // Ignored,  DAG files are never url DECODED
        }


        public void parseAttributes(String description, MultiMap<String, String> kvalues) {

            List<String> kvPairs = StringUtils.breakQuotedString(description.trim(), ';');
            for (String kv : kvPairs) {
                String[] tokens = kv.split(" ");
                if (tokens.length == 1) {
                    //Not space delimited, check =
                    tokens = kv.split("=");
                }
                if (tokens.length >= 2) {
                    String key = tokens[0].trim().replaceAll("\"", "");
                    String value = tokens[1].trim().replaceAll("\"", "");
                    kvalues.put(StringUtils.intern(key), value);
                }
            }
        }

        /**
         * parentIds[0] = attributes.get("id");
         * if (parentIds[0] == null) {
         * parentIds[0] = attributes.get("mRNA");
         * }
         * if (parentIds[0] == null) {
         * parentIds[0] = attributes.get("systematic_id");
         * }
         * if (parentIds[0] == null) {
         * parentIds[0] = attributes.get("transcript_id");
         * }
         * if (parentIds[0] == null) {
         * parentIds[0] = attributes.get("gene");
         * }
         * if (parentIds[0] == null) {
         * parentIds[0] = attributes.get("transcriptId");
         * }
         * if (parentIds[0] == null) {
         * parentIds[0] = attributes.get("proteinId");
         * }
         *
         * @param attributes
         * @param attributeString
         * @return
         */

        public String[] getParentIds(MultiMap<String, String> attributes, String attributeString) {

            String[] parentIds = new String[1];
            if (attributes.size() == 0) {
                parentIds[0] = attributeString;
            } else {
                for (String possName : possParentNames) {
                    if (attributes.containsKey(possName)) {
                        parentIds[0] = attributes.get(possName);
                        break;
                    }
                }
            }
            return parentIds;
        }


        public String getID(MultiMap<String, String> attributes) {
            for (String nf : idFields) {
                if (attributes.containsKey(nf)) {
                    return attributes.get(nf);
                }
            }
            return getName(attributes);
        }

        public String getName(MultiMap<String, String> attributes) {

            if (attributes.size() > 0 && nameFields != null) {
                for (String nf : nameFields) {
                    if (attributes.containsKey(nf)) {
                        return attributes.get(nf);
                    }
                }
            }

            return null;
        }

        public void setNameFields(String[] nameFields) {
            this.nameFields = nameFields;
        }

    }
 
}
