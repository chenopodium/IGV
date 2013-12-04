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

import com.iontorrent.utils.ErrorHandler;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.variant.Genotype;
import org.broad.igv.variant.vcf.VCFVariant;
import org.broad.tribble.AsciiFeatureCodec;
import org.broad.tribble.Feature;
import org.broad.tribble.readers.LineReader;
import org.broadinstitute.sting.utils.variantcontext.VariantContext;

/**
 * @author Jim Robinson
 * @date Aug 1, 2011
 */
public class VCFWrapperCodec extends AsciiFeatureCodec<VCFVariant> {

    AsciiFeatureCodec wrappedCodec;
    Genome genome;

    int nrerrors = 0;
    public VCFWrapperCodec(AsciiFeatureCodec wrappedCodec, Genome genome) {
        super(VCFVariant.class);
        this.wrappedCodec = wrappedCodec;
        this.genome = genome;
       // p("Got codec: "+wrappedCodec.getClass().getName());
    }

    @Override
    public Feature decodeLoc(String line) {
        return wrappedCodec.decodeLoc(line);
    }

    private static void p(String s) {
         Logger.getLogger(VCFWrapperCodec.class).info(s);
    }
    @Override
    public VCFVariant decode(String line) {
        //line = line.trim();
      //  Logger.getLogger(VCFWrapperCodec.class).info("VCFWrapperCodec: got line:\n" + line);
        VariantContext vc  = null;
        try {
             vc = (VariantContext) wrappedCodec.decode(line);
            // vc.g
        }
        catch (Throwable e) {
            if (nrerrors < 10) {
               p("Got error in:"+e.getMessage()+" for line: "+line);
            }
//            if (nrerrors < 2) {
//               p("Tracing error: "+ErrorHandler.getString(e)) ;
//               
//            }
            nrerrors++;
             
        }
        if (vc == null) {
            return null;
        }
        String chr = genome == null ? vc.getChr() : genome.getChromosomeAlias(vc.getChr());
        VCFVariant variant = new VCFVariant(vc, chr);
        //Logger.getLogger("VCFWrapperCodec: got variant: " + variant.toString());
                
        return variant;

    }

    @Override
    public Object readHeader(LineReader reader) {
        return wrappedCodec.readHeader(reader);
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
    @Override
    public boolean canDecode(String path) {
        return path.endsWith(".vcf") || path.endsWith(".vcf4") || path.endsWith(".vcf3");
    }
}
