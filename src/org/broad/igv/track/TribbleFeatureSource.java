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

package org.broad.igv.track;

import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.io.FileTools;
import java.io.File;
import org.broad.igv.data.DataSource;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.feature.tribble.CachingFeatureReader;
import org.broad.igv.feature.tribble.CodecFactory;
import org.broad.igv.feature.tribble.VCFWrapperCodec;
import org.broad.igv.tdf.TDFDataSource;
import org.broad.igv.tdf.TDFReader;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.RuntimeUtils;
import org.broad.tribble.*;
import org.broad.tribble.AbstractFeatureReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.HttpUtils;
import org.openide.util.Exceptions;

/**
 * @author jrobinso
 * @date Jun 27, 2010
 */
public class TribbleFeatureSource implements org.broad.igv.track.FeatureSource {

    CachingFeatureReader reader;
    DataSource coverageSource;
    boolean isVCF;
    Genome genome;

    /**
     * Map of IGV chromosome name -> source name
     */
    Map<String, String> chrNameMap = new HashMap();
    private int featureWindowSize;
    Object header;
    Class featureClass;

    public TribbleFeatureSource(String path, Genome genome) throws IOException {
        this.genome = genome;
        init(path);

    }

    private void initCoverageSource(String covPath) {
        if (ParsingUtils.pathExists(covPath)) {
            TDFReader reader = TDFReader.getReader(covPath);
            coverageSource = new TDFDataSource(reader, 0, "", genome);
        }
    }

    private void log(String s) {
        Logger.getLogger("TribbleFeatureSource").info(s);
        System.out.println("TribbleFeatureSource: "+s);
    }
    protected void init(String path) {
    //    log("TribbleFeatureSource init");
        FeatureCodec codec = CodecFactory.getCodec(path, genome);
        isVCF = codec.getClass() == VCFWrapperCodec.class;
        featureClass = codec.getFeatureType();
       // log("Feature class="+featureClass);
        AbstractFeatureReader basicReader = null;
        try {
            basicReader = AbstractFeatureReader.getFeatureReader(path, codec, true);
        }
        catch (Exception e) {           
           String msg = e.getMessage();
           p("Could not read file "+ path+" because: "+ErrorHandler.getString(e));
           if (msg != null && msg.indexOf("parse") > 0) {
               basicReader = AbstractFeatureReader.getFeatureReader(path, codec, true);
           }
           else {                      
                boolean yes = MessageUtils.confirm("<html>I had trouble reading the remote file<br>"+path+".<br><b>Would you like me to download it and read it locally?</b></html>");
                if (yes) {
                 String f = this.savePath(path);
                 if (path.endsWith(".gz")) {
                      // also try to save the file withouyt .gz
                      savePath(path.substring(0, path.length()-3));
                      savePath(path+".tbi");
                 }
                 else if (path.endsWith(".vcf")) {
                      // also try to save the file withouyt .gz
                      savePath(path+".gz");
                      savePath(path+".gz.tbi");
                 }
                 if (f != null) {
                     p("Trying again with downloaded file "+f);
                     path = f;
                     basicReader = AbstractFeatureReader.getFeatureReader(f, codec, true);
                 }
                }
                else basicReader = AbstractFeatureReader.getFeatureReader(path, codec, true);
           }
           // generate same exception again
           
        }
        header = basicReader.getHeader();
      //  log("header  = "+header.toString());
        initFeatureWindowSize(basicReader);
        reader = new CachingFeatureReader(basicReader, 5, getFeatureWindowSize());

        if (genome != null) {
            Collection<String> seqNames = reader.getSequenceNames();
            if (seqNames != null)
                for (String seqName : seqNames) {
                    String igvChr = genome.getChromosomeAlias(seqName);
                    if (igvChr != null && !igvChr.equals(seqName)) {
                        chrNameMap.put(igvChr, seqName);
                    }
                }
        }

        initCoverageSource(path + ".tdf");
    }

    private void p(String s) {
        Logger.getLogger("TribbleFeatureSource").info(s);
    }
   private String savePath(String path) {
        File f = null;
        byte[] content = null;
        p("SavePath: "+path);
        if (FileTools.isUrl(path)) {
            try {
                p("SavePath is url: " + path);
                int sl = path.lastIndexOf("/");
                f = new File(path.substring(sl));
                File output = new File(PreferenceManager.getInstance().getLastSessionDirectory()+"/"+ f.getName());
                HttpUtils.getInstance().downloadFile(path, output);
                return output.getAbsolutePath();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
       
        } else {
            return path;
        }
        
    }
    public Class getFeatureClass() {
        return featureClass;
    }

    public CloseableTribbleIterator<Feature> getFeatures(String chr, int start, int end) throws IOException {

        String seqName = chrNameMap.get(chr);
        if (seqName == null) seqName = chr;
        return reader.query(seqName, start, end);
    }

    /**
     * Return coverage values overlapping the query interval.   At this time Tribble sources do not provide
     * coverage values
     *
     * @param chr
     * @param start
     * @param end
     * @param zoom
     * @return
     */
    public List<LocusScore> getCoverageScores(String chr, int start, int end, int zoom) {
        return coverageSource == null ? null :
                coverageSource.getSummaryScoresForRange(chr, start, end, zoom);
    }

    public int getFeatureWindowSize() {
        return featureWindowSize;
    }

    public void setFeatureWindowSize(int size) {
        this.featureWindowSize = size;
        reader.setBinSize(size);
    }

    public Object getHeader() {
        return header;
    }


    /**
     * Estimate an appropriate feature window size.
     *
     * @param reader
     */
    private void initFeatureWindowSize(FeatureReader reader) {

        CloseableTribbleIterator<org.broad.tribble.Feature> iter = null;

        try {
            double mem = RuntimeUtils.getAvailableMemory();
            iter = reader.iterator();
            if (iter.hasNext()) {

                int nSamples = isVCF ? 100 : 1000;
                org.broad.tribble.Feature firstFeature = iter.next();
                org.broad.tribble.Feature lastFeature = firstFeature;
                String chr = firstFeature.getChr();
                int n = 1;
                long len = 0;
                while (iter.hasNext() && n < nSamples) {
                    org.broad.tribble.Feature f = iter.next();
                    if (f != null) {
                        n++;
                        if (f.getChr().equals(chr)) {
                            lastFeature = f;
                        } else {
                            len += lastFeature.getEnd() - firstFeature.getStart() + 1;
                            firstFeature = f;
                            lastFeature = f;
                            chr = f.getChr();
                        }

                    }
                }
                double dMem = mem - RuntimeUtils.getAvailableMemory();
                double bytesPerFeature = Math.max(100, dMem / n);

                len += lastFeature.getEnd() - firstFeature.getStart() + 1;
                double featuresPerBase = ((double) n) / len;

                double targetBinMemory = 20000000;  // 20  mega bytes
                int maxBinSize = isVCF ? 1000000 : Integer.MAX_VALUE;
                int bs = Math.min(maxBinSize, (int) (targetBinMemory / (bytesPerFeature * featuresPerBase)));
                featureWindowSize = Math.max(1000000, bs);
            } else {
                featureWindowSize = Integer.MAX_VALUE;
            }
        } catch (IOException e) {
            featureWindowSize = 1000000;
        }
    }

    public Collection<String> getChrNames() {
        return chrNameMap.keySet();
    }
}
