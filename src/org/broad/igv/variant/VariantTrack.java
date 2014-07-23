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
//chr2:128,565,093-128,565,156 
package org.broad.igv.variant;

import com.iontorrent.utils.ErrorHandler;
import org.apache.log4j.Logger;
import org.broad.igv.feature.FeatureUtils;
import org.broad.igv.feature.IGVFeature;
import org.broad.igv.renderer.GraphicUtils;
import org.broad.igv.session.IGVSessionReader;
import org.broad.igv.track.*;
import org.broad.igv.ui.FontManager;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.event.TrackGroupEvent;
import org.broad.igv.ui.event.TrackGroupEventListener;
import org.broad.igv.ui.panel.*;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.FileUtils;
import org.broad.igv.util.LongRunningTask;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;
import org.broad.tribble.Feature;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.ui.TooltipTextFrame;
import org.broad.igv.ui.util.UIUtilities;

/**
 * @author Jesse Whitworth, Jim Robinson, Fabien Campagne
 */
public class VariantTrack extends FeatureTrack implements TrackGroupEventListener {

    private static Logger log = Logger.getLogger(VariantTrack.class);
    static final DecimalFormat numFormat = new DecimalFormat("#.###");
    private static final Color OFF_WHITE = new Color(170, 170, 170);
    private static final int GROUP_BORDER_WIDTH = 3;
    private static final Color BAND1_COLOR = new Color(245, 245, 245);
    private static final Color BAND2_COLOR = Color.white;
    private static final Color SELECTED_BAND_COLOR = new Color(210, 210, 210);
    private static final Color borderGray = new Color(200, 200, 200);
    private final static int DEFAULT_EXPANDED_GENOTYPE_HEIGHT = 15;
    private final int DEFAULT_SQUISHED_GENOTYPE_HEIGHT = 4;
    private final static int DEFAULT_VARIANT_BAND_HEIGHT = 25;
    private final static int MAX_FILTER_LINES = 30;
    // TODO -- this needs to be settable
    static DecimalFormat f = new DecimalFormat("#.##");
    public static int METHYLATION_MIN_BASE_COUNT = 10;
    int messages;
    /**
     * The renderer.
     */
    private VariantRenderer renderer = new VariantRenderer(this);
    /**
     * When this flag is true, we have detected that the VCF file contains the
     * FORMAT MR column representing methylation data. This will enable the
     * "Color By/Methylation Rate" menu item.
     */
    private boolean enableMethylationRateSupport;
    /**
     * Top (y) position of this track. This is updated whenever the track is
     * drawn.
     */
    private int top;
    /**
     * The height of a single row in in squished mode
     */
    private int squishedHeight = DEFAULT_SQUISHED_GENOTYPE_HEIGHT;
    /**
     * The height of the top band representing the variant call
     */
    private int variantBandHeight = DEFAULT_VARIANT_BAND_HEIGHT;
    /**
     * List of all samples, in the order they appear in the file.
     */
    List<String> allSamples;
    /**
     * Boolean indicating if samples are grouped.
     */
    private boolean grouped;
    /**
     * The id of the group used to group samples.
     */
    private String groupByAttribute;
    /**
     * Map of group -> samples. Each entry defines a group, the key is the group
     * name and the value the list of samples in the group.
     */
    LinkedHashMap<String, List<String>> samplesByGroups = new LinkedHashMap<String, List<String>>();
    /**
     * Current coloring option
     */
    private ColorMode coloring = ColorMode.GENOTYPE;
    //private ColorMode coloring = ColorMode.TRACK;
    /**
     * When true, variants that are marked filtering are not drawn.
     */
    private boolean hideFiltered = false;
    /**
     * If true the variant ID, when present, will be rendered.
     */
    private boolean renderID = true;
    /**
     * The currently selected variant. This is a transient variable, set only
     * while the popup menu is up.
     */
    private Variant selectedVariant;
    /**
     * Transient list to keep track of the vertical bounds of each sample. Set
     * when rendering names, used to select correct sample for popup text. We
     * use a list and linear lookup for now, some sort of tree structure would
     * be faster.
     */
    private List<SampleBounds> sampleBounds = new ArrayList<SampleBounds>();
    /**
     * List of selected samples.
     */
    private List<String> selectedSamples = new ArrayList<String>();
    /**
     * Experimental "mode" to couple VCF & BAM files
     */
    //private boolean vcfToBamMode = false;
    /**
     * Map of sample name -> associated bam file
     */
    Map<String, String> alignmentFiles;
    private boolean userSetHeight;
    private int max_nr_variants_tt =1 ;
    

    @Override
    public String getDisplayName(boolean withSampleInfo) {
        return super.getDisplayName(false);
    }

    public VariantTrack(ResourceLocator locator, FeatureSource source, List<String> samples,
            boolean enableMethylationRateSupport) {
        super(locator, source);

        this.enableMethylationRateSupport = enableMethylationRateSupport;
        if (enableMethylationRateSupport) {
            // also set the default color mode to Methylation rate:
            coloring = ColorMode.METHYLATION_RATE;
        }


        this.allSamples = samples;

        // this handles the new attribute grouping mechanism:
        setupGroupsFromAttributes();

        setDisplayMode(DisplayMode.EXPANDED);

        setRenderID(false);

        // Estimate visibility window.
        // TODO -- set beta based on available memory
        int cnt = Math.max(1, allSamples.size());
        // int beta = 10000000;
        int mem = (int) (Runtime.getRuntime().totalMemory());
        double p = Math.pow(cnt, 1.5);
        int MB = 1000000;
        int visWindow = (int) Math.min(500 * MB, (mem / p) * 10000);
        //  p("Computing visibility window: nr fatures=" + allSamples.size() + ", mem in MB=" + mem / 1000000 + ", window=" + visWindow);
        setVisibilityWindow(visWindow);

        // Listen for "group by" events.  TODO -- "this" should be removed when track is disposed of
        if (IGV.hasInstance()) {
            IGV.getInstance().addGroupEventListener(this);
        }

        // If sample->bam list file is supplied enable vcfToBamMode.
        String bamListPath = locator.getPath() + ".mapping";
        if (ParsingUtils.pathExists(bamListPath)) {
            loadAlignmentMappings(bamListPath);

        }

    }

    private void loadAlignmentMappings(String bamListPath) {
        alignmentFiles = new HashMap<String, String>();
        BufferedReader br = null;

        try {
            br = ParsingUtils.openBufferedReader(bamListPath);
            String nextLine;
            while ((nextLine = br.readLine()) != null) {
                String[] tokens = ParsingUtils.TAB_PATTERN.split(nextLine);
                if (tokens.length < 2) {
                    log.info("Skipping bam mapping file line: " + nextLine);
                } else {

                    String alignmentPath = tokens[1];
                    boolean isAbsolute;
                    if (alignmentPath.startsWith("http://") || alignmentPath.startsWith("ftp:")) {
                        isAbsolute = true;
                    } else {
                        String absolutePath = (new File(alignmentPath)).getAbsolutePath();
                        String prefix = absolutePath.substring(0, 3);
                        isAbsolute = alignmentPath.startsWith(prefix);
                    }
                    if (!isAbsolute) {
                        alignmentPath = FileUtils.getAbsolutePath(alignmentPath, bamListPath);
                    }


                    alignmentFiles.put(tokens[0], alignmentPath);
                }
            }
        } catch (IOException e) {
            MessageUtils.showMessage("<html>Error loading bam mapping file: " + bamListPath + "<br>" + e.getMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                }
            }
        }
    }

    String getBamFileForSample(String sample) {
        return alignmentFiles == null ? null : alignmentFiles.get(sample);
    }

    /**
     * Set groups from global sample information attributes.
     */
    private void setupGroupsFromAttributes() {
        // setup groups according to the attribute used for sorting (loaded from a sample information file):

        AttributeManager manager = AttributeManager.getInstance();
        String newGroupByAttribute = !IGV.hasInstance() ? null : IGV.getInstance().getGroupByAttribute();

        // The first equality handles the case where both are null
        if ((newGroupByAttribute == groupByAttribute)
                || (newGroupByAttribute != null && newGroupByAttribute.equals(groupByAttribute))) {
            // Nothing to do
            return;
        }


        samplesByGroups.clear();

        groupByAttribute = newGroupByAttribute;

        if (groupByAttribute == null) {
            grouped = false;
            return;
        }

        for (String sample : allSamples) {

            String sampleGroup = manager.getAttribute(sample, newGroupByAttribute);

            List<String> sampleList = samplesByGroups.get(sampleGroup);
            if (sampleList == null) {
                sampleList = new ArrayList<String>();
                samplesByGroups.put(sampleGroup, sampleList);
            }
            sampleList.add(sample);
        }

        grouped = samplesByGroups.size() > 1;
        groupByAttribute = newGroupByAttribute;
    }

    /**
     * Sort samples. Sort both the master list and groups, if any.
     *
     * @param comparator the comparator to sort by
     */
    public void sortSamples(Comparator<String> comparator) {
        Collections.sort(allSamples, comparator);
        for (List<String> samples : samplesByGroups.values()) {
            Collections.sort(samples, comparator);
        }
    }

    public boolean isEnableMethylationRateSupport() {
        return enableMethylationRateSupport;
    }

    /**
     * Returns the height of a single sample (genotype) band
     *
     * @return
     */
    public int getGenotypeBandHeight() {
        switch (getDisplayMode()) {
            case SQUISHED:
                return getSquishedHeight();
            case COLLAPSED:
                return 0;
            default:
                return DEFAULT_EXPANDED_GENOTYPE_HEIGHT;

        }
    }

    /**
     * Returns the total height of the track (including all sample/genotypes)
     *
     * @return
     */
    @Override
    public int getHeight() {
        if (this.isUserSetHeight()) {
            // log.info("returning user defined height "+height);
            return height;
        }
        int sampleCount = allSamples.size();
        if (getDisplayMode() == DisplayMode.COLLAPSED || sampleCount == 0) {
            return variantBandHeight;
        } else {
            final int groupCount = samplesByGroups.size();
            int margins = groupCount * 3;
            return variantBandHeight + margins + (sampleCount * getGenotypeBandHeight());
        }
    }

    /**
     * Set the height of the track.
     *
     * @param height
     */
    @Override
    public void setHeight(int height) {

        this.height = height;
        log.info("Variant track " + this.getName() + ", height is now: " + height + "/" + this.getHeight());
        final DisplayMode displayMode = getDisplayMode();

        // If collapsed there's nothing we can do to affect height
        if (displayMode == DisplayMode.COLLAPSED) {
            return;
        }

        // If height is < expanded height try "squishing" track, otherwise expand it
        final int groupCount = samplesByGroups.size();
        final int margins = (groupCount - 1) * 3;
        int sampleCount = allSamples.size();
        final int expandedHeight = variantBandHeight + margins + (sampleCount * getGenotypeBandHeight());
        if (height < expandedHeight) {
            setDisplayMode(DisplayMode.SQUISHED);
            squishedHeight = Math.max(1, (height - variantBandHeight - margins) / sampleCount);
        } else {
            if (displayMode != DisplayMode.EXPANDED) {
                setDisplayMode(DisplayMode.EXPANDED);
            }
        }
    }

    private void p(String s) {
        log.info("VariantTrack: " + s);
        System.out.println("VT: "+s);
    }

    /**
     * Render the features in the supplied rectangle.
     *
     * @param context
     * @param trackRectangle
     * @param packedFeatures
     */
    @Override
    protected void renderFeatureImpl(RenderContext context, Rectangle trackRectangle, PackedFeatures packedFeatures) {

        Graphics2D g2D = context.getGraphics();

        top = trackRectangle.y;
        final int left = trackRectangle.x;
        final int right = (int) trackRectangle.getMaxX();

        Rectangle visibleRectangle = context.getVisibleRect();

        // A disposable rect -- note this gets modified all over the place, bad practice
        Rectangle rect = new Rectangle(trackRectangle);
        rect.height = getGenotypeBandHeight();
        rect.y = trackRectangle.y + variantBandHeight;
        drawBackground(g2D, rect, visibleRectangle, BackgroundType.DATA);

        if (top > visibleRectangle.y && top < visibleRectangle.getMaxY()) {
            g2D.drawLine(left, top + 1, right, top + 1);
        }

        List<Feature> features = packedFeatures.getFeatures();

        boolean show = false;//features.size() < 10;
        if (features.size() > 0) {
            p("Rendering "+features.size()+ " variants from "+left+" to "+right);
            
            int[] sizelimits = { 1000000, 100000, 10000, 1000, 100, 10, 0};
            int nrtimes = sizelimits.length;

            for (int times = 0; times < nrtimes; times++) {
                final double locScale = context.getScale();
                final double origin = context.getOrigin();

                int lastPX = -1;
                final double pXMin = rect.getMinX();
                final double pXMax = rect.getMaxX();

                for (Feature feature : features) {
                    Variant variant = (Variant) feature; 
                    if (hideFiltered && variant.isFiltered()) {
                        continue;
                    }
                    int start = variant.getStart();
                    int end = variant.getEnd();

                    int size = end - start;
                   // if (show) p(" v "+start+"-"+end+", size="+size+", limit="+sizelimits[times]);
                    if (size < sizelimits[times]) continue;
                    else if (times > 0 && size > sizelimits[times-1]) continue;
                        if (show) p("     drawing v "+variant.getID()+" at "+start+",size="+size);

                        int pX = (int) ((start - origin) / locScale);
                        int dX = (int) Math.max(2, (end - start) / locScale);

                        //if (show)   p("Maybe drawing variant :"+variant+", pX="+pX+",  dx="+dX+", locScale="+locScale+", pxmin="+pXMin+", (px+dx)="+(int)(pX+dX));
                        if (pX + (long) dX < (long) pXMin && pX < pXMin) {
                            if (show)p("NOT drawing variant :"+variant+" because px+dx < pxmin");
                            continue;
                        }
                        if (pX > pXMax) {
                            if (show) p("NOT drawing variant and BREAKING :"+variant+" because px (start) > pXMax: "+pX+">"+pXMax);
                            break;
                        }
                        int w = (int) dX;
                        int x = pX;
                        if (w < 3) {
                            w = 3;
                            x--;
                        }


                        if ((long) pX + (long) dX > (long) lastPX) {
                            if (show) p("Yes, rendering variant");
                            rect.y = top;
                            rect.height = variantBandHeight;
                            if (rect.intersects(visibleRectangle)) {
                                renderer.renderSiteBand(variant, rect, x, w, context);
                            }

                            if (getDisplayMode() != DisplayMode.COLLAPSED) {
                                rect.y += rect.height;
                                rect.height = getGenotypeBandHeight();

                                // Loop through groups
                                if (grouped) {
                                    for (Map.Entry<String, List<String>> entry : samplesByGroups.entrySet()) {
                                        for (String sample : entry.getValue()) {
                                            if (rect.intersects(visibleRectangle)) {
                                                renderer.renderGenotypeBandSNP(variant, context, rect, x, w, sample, coloring,
                                                        hideFiltered);
                                            }
                                            rect.y += rect.height;
                                        }
                                        g2D.setColor(OFF_WHITE);
                                        g2D.fillRect(rect.x, rect.y, rect.width, GROUP_BORDER_WIDTH);
                                        rect.y += GROUP_BORDER_WIDTH;
                                    }
                                } else {
                                    for (String sample : allSamples) {
                                        if (rect.intersects(visibleRectangle)) {
                                            renderer.renderGenotypeBandSNP(variant, context, rect, x, w, sample, coloring,
                                                    hideFiltered);
                                        }
                                        rect.y += rect.height;
                                    }

                                }
                            }


                            boolean isSelected = selectedVariant != null && selectedVariant == variant;
                            if (isSelected) {
                                Graphics2D selectionGraphics = context.getGraphic2DForColor(Color.black);
                                selectionGraphics.drawRect(x, top, w, getHeight());
                            }
                            // ONLY FOR SMALL VARIANTS!                        
                           // if (size < sizelimit) {
                                lastPX = pX + dX;
                           // } else {
                           ////     dX = (int) Math.max(1, 2 / locScale);
                           //     lastPX = pX + dX;
                           // }
                        }
                    
                }
            }
        } else {
            rect.height = variantBandHeight;
            rect.y = trackRectangle.y;
            g2D.setColor(Color.gray);
            GraphicUtils.drawCenteredText("No Variants Found", trackRectangle, g2D);
        }

        // Variant band border
        if (allSamples.size() > 0) {
            int variantBandY = trackRectangle.y + variantBandHeight;
            if (variantBandY >= visibleRectangle.y && variantBandY <= visibleRectangle.getMaxY()) {
                Graphics2D borderGraphics = context.getGraphic2DForColor(Color.black);
                borderGraphics.drawLine(left, variantBandY, right, variantBandY);
            }
        }

        // Bottom border
        int bottomY = trackRectangle.y + trackRectangle.height;
        if (bottomY >= visibleRectangle.y && bottomY <= visibleRectangle.getMaxY()) {
            g2D.drawLine(left, bottomY, right, bottomY);
        }


    }

    /**
     * Render the name panel.
     * <p/>
     * NOTE: The sample names are actually drawn in the drawBackground method!
     *
     * @param g2D
     * @param trackRectangle
     * @param visibleRectangle
     */
    @Override
    public void renderName(Graphics2D g2D, Rectangle trackRectangle, Rectangle visibleRectangle) {

        top = trackRectangle.y;
        final int left = trackRectangle.x;
        final int right = (int) trackRectangle.getMaxX();

        Rectangle rect = new Rectangle(trackRectangle);
        g2D.setFont(FontManager.getFont(fontSize));
        g2D.setColor(BAND2_COLOR);

        if (top > visibleRectangle.y && top < visibleRectangle.getMaxY()) {
            g2D.setColor(Color.black);
            g2D.drawLine(left, top + 1, right, top + 1);
        }

        g2D.setColor(Color.black);
        rect.height = variantBandHeight;
        if (rect.intersects(visibleRectangle)) {
            GraphicUtils.drawWrappedText(getDisplayName(), rect, g2D, false);
        }

        rect.y += rect.height;
        rect.height = getGenotypeBandHeight();
        if (getDisplayMode() != DisplayMode.COLLAPSED) {
            // The sample bounds list will get reset when  the names are drawn.
            sampleBounds.clear();
            drawBackground(g2D, rect, visibleRectangle, BackgroundType.NAME);

        }

        // Bottom border
        int bottomY = trackRectangle.y + trackRectangle.height;
        if (bottomY >= visibleRectangle.y && bottomY <= visibleRectangle.getMaxY()) {
            g2D.setColor(borderGray);
            g2D.drawLine(left, bottomY, right, bottomY);
        }

        // Variant / Genotype border
        if (allSamples.size() > 0) {
            int variantBandY = trackRectangle.y + variantBandHeight;
            if (variantBandY >= visibleRectangle.y && variantBandY <= visibleRectangle.getMaxY()) {
                g2D.setColor(Color.black);
                g2D.drawLine(left, variantBandY, right, variantBandY);
            }
        }

    }

    /**
     * Render sample attributes, if any.
     *
     * @param g2D
     * @param trackRectangle
     * @param visibleRectangle
     * @param attributeNames
     * @param mouseRegions
     */
    public void renderAttributes(Graphics2D g2D, Rectangle trackRectangle, Rectangle visibleRectangle,
            List<String> attributeNames, List<MouseableRegion> mouseRegions) {

        top = trackRectangle.y;
        final int left = trackRectangle.x;
        final int right = (int) trackRectangle.getMaxX();
        Rectangle rect = new Rectangle(trackRectangle);

        g2D.setColor(Color.black);
        if (top > visibleRectangle.y && top < visibleRectangle.getMaxY()) {
            g2D.drawLine(left, top + 1, right, top + 1);
        }

        rect.height = variantBandHeight;
        if (rect.intersects(visibleRectangle)) {
            super.renderAttributes(g2D, rect, visibleRectangle, attributeNames, mouseRegions);
        }

        if (getDisplayMode() == DisplayMode.COLLAPSED) {
            return;
        }

        rect.y += rect.height;
        rect.height = getGenotypeBandHeight();
        Rectangle bandRectangle = new Rectangle(rect);  // Make copy for later use

        drawBackground(g2D, rect, visibleRectangle, BackgroundType.ATTRIBUTE);

        if (grouped) {
            for (List<String> sampleList : samplesByGroups.values()) {
                renderAttibuteBand(g2D, bandRectangle, visibleRectangle, attributeNames, sampleList, mouseRegions);
                bandRectangle.y += GROUP_BORDER_WIDTH;

            }
        } else {
            renderAttibuteBand(g2D, bandRectangle, visibleRectangle, attributeNames, allSamples, mouseRegions);

        }

        // Bottom border
        int bottomY = trackRectangle.y + trackRectangle.height;
        if (bottomY >= visibleRectangle.y && bottomY <= visibleRectangle.getMaxY()) {
            g2D.setColor(borderGray);
            g2D.drawLine(left, bottomY, right, bottomY);
        }

        // Variant / Genotype border
        if (allSamples.size() > 0) {
            int variantBandY = trackRectangle.y + variantBandHeight;
            if (variantBandY >= visibleRectangle.y && variantBandY <= visibleRectangle.getMaxY()) {
                g2D.setColor(Color.black);
                g2D.drawLine(left, variantBandY, right, variantBandY);
            }
        }

    }

    /**
     * Render attribues for a sample. This is mostly a copy of
     * AbstractTrack.renderAttibutes(). TODO -- refactor to eliminate duplicate
     * code from AbstractTrack
     *
     * @param g2D
     * @param bandRectangle
     * @param visibleRectangle
     * @param attributeNames
     * @param sampleList
     * @param mouseRegions
     * @return
     */
    private void renderAttibuteBand(Graphics2D g2D, Rectangle bandRectangle, Rectangle visibleRectangle,
            List<String> attributeNames, List<String> sampleList, List<MouseableRegion> mouseRegions) {


        for (String sample : sampleList) {

            if (bandRectangle.intersects(visibleRectangle)) {

                int x = bandRectangle.x;

                for (String name : attributeNames) {

                    String key = name.toUpperCase();
                    String attributeValue = AttributeManager.getInstance().getAttribute(sample, key);
                    if (attributeValue != null) {
                        Rectangle rect = new Rectangle(x, bandRectangle.y, AttributeHeaderPanel.ATTRIBUTE_COLUMN_WIDTH,
                                bandRectangle.height);
                        g2D.setColor(AttributeManager.getInstance().getColor(key, attributeValue));
                        g2D.fill(rect);
                        mouseRegions.add(new MouseableRegion(rect, key, attributeValue));
                    }
                    x += AttributeHeaderPanel.ATTRIBUTE_COLUMN_WIDTH + AttributeHeaderPanel.COLUMN_BORDER_WIDTH;
                }

            }
            bandRectangle.y += bandRectangle.height;

        }
    }

    /**
     * Draws the "greenbar" type background. Also, rather bizzarely, draws the
     * sample names.
     *
     * @param g2D
     * @param bandRectangle
     * @param visibleRectangle
     * @param type
     */
    private void drawBackground(Graphics2D g2D, Rectangle bandRectangle, Rectangle visibleRectangle,
            BackgroundType type) {


        if (getDisplayMode() == DisplayMode.COLLAPSED) {
            return;
        }

        boolean coloredLast = true;
        Rectangle textRectangle = new Rectangle(bandRectangle);
        textRectangle.height--;

        int bandFontSize = Math.min(fontSize, (int) bandRectangle.getHeight() - 1);
        Font font = FontManager.getFont(bandFontSize);
        Font oldFont = g2D.getFont();
        g2D.setFont(font);

        if (grouped) {
            for (Map.Entry<String, List<String>> sampleGroup : samplesByGroups.entrySet()) {
                int y0 = bandRectangle.y;

                List<String> sampleList = sampleGroup.getValue();
                coloredLast = colorBand(g2D, bandRectangle, visibleRectangle, coloredLast, textRectangle, sampleList, type);

                g2D.setColor(OFF_WHITE);
                g2D.fillRect(bandRectangle.x, bandRectangle.y, bandRectangle.width, GROUP_BORDER_WIDTH);
                bandRectangle.y += GROUP_BORDER_WIDTH;

                if (type == BackgroundType.NAME && bandRectangle.height < 3) {
                    String group = sampleGroup.getKey();
                    if (group != null) {
                        g2D.setColor(Color.black);
                        g2D.setFont(oldFont);
                        int y2 = bandRectangle.y;
                        Rectangle textRect = new Rectangle(bandRectangle.x, y0, bandRectangle.width, y2 - y0);
                        GraphicUtils.drawWrappedText(group, textRect, g2D, true);
                    }
                }

            }

        } else {
            coloredLast = colorBand(g2D, bandRectangle, visibleRectangle, coloredLast, textRectangle, allSamples, type);
        }
        g2D.setFont(oldFont);
    }

    private boolean colorBand(Graphics2D g2D, Rectangle bandRectangle, Rectangle visibleRectangle,
            boolean coloredLast, Rectangle textRectangle, List<String> sampleList,
            BackgroundType type) {

        boolean supressFill = (getDisplayMode() == DisplayMode.SQUISHED && squishedHeight < 4);

        for (String sample : sampleList) {

            if (coloredLast) {
                g2D.setColor(BAND1_COLOR);
                coloredLast = false;
            } else {
                g2D.setColor(BAND2_COLOR);
                coloredLast = true;
            }

            if (bandRectangle.intersects(visibleRectangle)) {
                if (!supressFill) {
                    if (selectedSamples.contains(sample) && hasAlignmentFiles()) {
                        g2D.setColor(SELECTED_BAND_COLOR);
                    }
                    g2D.fillRect(bandRectangle.x, bandRectangle.y, bandRectangle.width, bandRectangle.height);
                }

                if (type == BackgroundType.NAME) {
                    sampleBounds.add(new SampleBounds(bandRectangle.y, bandRectangle.y + bandRectangle.height, sample));
                    if (bandRectangle.height >= 3) {
                        String printName = sample;
                        textRectangle.y = bandRectangle.y + 1;
                        g2D.setColor(Color.black);
                        GraphicUtils.drawWrappedText(printName, bandRectangle, g2D, false);
                    }


                } else if (type == BackgroundType.ATTRIBUTE) {
                }
            }
            bandRectangle.y += bandRectangle.height;

        }
        return coloredLast;
    }

    public void setRenderID(boolean value) {
        this.renderID = value;
    }

    public boolean getHideFiltered() {
        return hideFiltered;
    }

    public void setHideFiltered(boolean value) {
        this.hideFiltered = value;
    }

    public ColorMode getColorMode() {
        return coloring;
    }

    public void setColorMode(ColorMode mode) {
        this.coloring = mode;
    }

    public String getNameValueString(int y) {
        if (y < top + variantBandHeight) {
            return "<html>" + getDisplayName(true) + "</html>";
        } else {
            String sample = getSampleAtPosition(y);
            return sample;
        }
    }

    /**
     * Return popup text for the given position
     *
     * @param chr
     * @param position - position in UCSC "0 based" genomic coordinates
     * @param y - pixel position in panel coordinates (i.e. not track
     * coordinates)
     * @param frame
     * @return
     */
    @Override
    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {


        if (chr.equalsIgnoreCase(Globals.CHR_ALL)) {
            return null;
        }
        try {
            double maxDistance = 10 * frame.getScale();
            List<Variant> variants = getFeatureClosests(position, maxDistance, frame);
            if (variants == null) {
                return null;
            } else {
                
                if (y < top + variantBandHeight) {
                    return getVariantToolTip(variants);
                } else {
                    if (sampleBounds == null && sampleBounds.isEmpty()) {
                        return null;
                    }
                    String sample = getSampleAtPosition(y);
                    if (sample != null) {
                        return getSampleToolTip(sample, variants);
                    } else {
                        return null;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

    /**
     * Return the sample at the give pixel position
     *
     * @param y - screen position in pixels
     * @return
     */
    private String getSampleAtPosition(int y) {

        if (sampleBounds.isEmpty()) {
            return null;
        }
        String sample = null;

        // Estimate the index of the sample, then do a linear search
        final int sampleCount = sampleBounds.size();

        int firstSampleY = sampleBounds.get(0).top;
        int idx = Math.max(0, Math.min((y - firstSampleY) / Math.max(1, getGenotypeBandHeight()), sampleCount - 1));

        SampleBounds bounds = sampleBounds.get(idx);
        if (bounds.contains(y)) {
            sample = bounds.sample;
        } else if (bounds.top > y) {
            while (idx > 0) {
                idx--;
                bounds = sampleBounds.get(idx);
                if (bounds.contains(y)) {
                    sample = bounds.sample;
                }
            }
        } else {
            while (idx < sampleCount - 1) {
                idx++;
                bounds = sampleBounds.get(idx);
                if (bounds.contains(y)) {
                    sample = bounds.sample;
                }
            }
        }
        return sample;
    }

    /**
     * Return the variant closest to the genomic position in the given reference
     * frame, within the prescribed tolerance
     *
     * @param position
     * @param frame
     * @return
     */
    protected List<Variant> getFeatureClosests(double position, double maxDistance, ReferenceFrame frame) {

        List<Variant> res = new ArrayList<Variant>();
        PackedFeatures<IGVFeature> packedFeatures = packedFeaturesMap.get(frame.getName());

        if (packedFeatures == null) {
            return null;
        }

    
        List<Feature> foundfeatures = null;
        // Note that we use the full features to search here because (1) we expect to retrieve one element at most
        // (2) searching packed features would miss the features we are looking for despite them being in the full set.
        List<IGVFeature> features = packedFeatures.getFeatures();
        
      //  p("Getting features closest to position " + position);
        if (features != null) {
            foundfeatures = FeatureUtils.getFeatureClosests(position, features);
        }
        if (foundfeatures == null) return null;
        else {
            for (Feature f: foundfeatures) {
                if (f instanceof Variant) {
                    if  ( (position < f.getStart() - maxDistance) || (position > f.getEnd() + maxDistance)) {
                        // ignore
                    }
                    else res.add((Variant)f);
                }
                else p("Got NON variant feature: "+f);
            }
            
        }
        return res;
    }

    public String getVariantToolTip(List<Variant> variants) {
        boolean isShort = variants.size() > max_nr_variants_tt;
        
        return getVariantToolTip(variants, isShort, variants.size() > 2*max_nr_variants_tt);
    }
    public String getVariantToolTip(List<Variant> variants, boolean isShort, boolean isVeryShort) {
        String s = "";
        for (int i = 0; i < variants.size(); i++) {
            Variant v = variants.get(i);
            s += getVariantToolTip(v, isShort, isVeryShort);
            if (i+1 < variants.size()) s +="<hr>";
        }
        return s;
    }
    public String getVariantToolTip(Variant variant, boolean isShort) {
        return getVariantToolTip(variant, isShort, isShort);
    }
    public String getVariantToolTip(Variant variant) {
        return getVariantToolTip(variant, false, false);
    }

    public String getVariantToolTip(Variant variant, boolean isShort, boolean isVeryShort) {
        String id = variant.getID();

        StringBuffer toolTip = new StringBuffer();
        toolTip.append("Position: " + variant.getChr());
        
         toolTip.append(", " + variant.getPositionString());
        if (!isVeryShort) {
            toolTip.append("<br>ID: " + id);
            toolTip.append("<br>Reference: " + variant.getReference());
        }

        double p = variant.getPloidy();
        
        if (p >= 0) {
            String pl = "Ploidy: " + f.format(variant.getPloidy());
            // USE EXPECTED PLOIDY
            
            int expected = variant.getExpected();
            if (expected <=0) {
                expected = 2;
                if (this.getSample() != null) {
                    String key = variant.getChr()+"_CUTOFF_"+this.getSample();
                    key = key.toUpperCase();
                    // CHR1_CUTOFF_SELF=4
                    // CHR2_CUTOFF_SELF=3
                    // CHR23_CUTOFF_SELF=4
                    String val = PreferenceManager.getInstance().getTemp(key);
                    if (val != null) {
                        try {
                            expected = Integer.parseInt(val);
                        }
                        catch (Exception e) {
                            p("Could not parse expected from "+val);
                        }
                    }
                    else p("Found no value for expected value for key  "+key);
                   // p("Expected value for "+key+":"+expected);
                }
                else p("Got no expected value (using 2), and have no sample info for track "+this.getName());
                
                variant.setExpected(expected);
            }
            String ex = ""+expected;
            if (p > expected) {
                pl = "<font color='000099'>" + pl + "</font>";
                ex = "<b>"+ex+"</b>";
            } else if (p <expected) {
                pl = "<font color='990000'>" + pl + "</font>";
                ex = "<b>"+ex+"</b>";
            }
            toolTip.append("<br><b>" + pl + "</b>");
            toolTip.append("<br>Expected ploidy: " + ex + "");
        } else {
            // toolTip.append("<br>Ploidy: unknown");
        }
        toolTip.append("<br>Type: " + variant.getType());
        if (!isShort) {
            Set<Allele> alternates = variant.getAlternateAlleles();
            if (alternates.size() > 0) {
                toolTip.append("<br>Alternate: " + alternates.toString());
            }
        }
        if (!isVeryShort) {
            toolTip.append("<br>Qual: " + numFormat.format(variant.getPhredScaledQual()));
            
            if (!isShort) {
                if (variant.isFiltered()) {
                    toolTip.append("<br>Is Filtered Out: Yes</b>");
                    toolTip = toolTip.append(getFilterTooltip(variant));
                } else {
                    toolTip.append("<br>Is Filtered Out: No</b><br>");
                }
                addBrIfMissing(toolTip);
                toolTip.append("<b>Alleles:</b>");
                toolTip.append(getAlleleToolTip(variant));


                double[] af = variant.getAlleleFreqs();
                if (af[0] < 0 && variant.getSampleNames().size() > 0) {
                    af = new double[]{variant.getAlleleFraction()};
                }
                String afMsg = "Unknown";
                if (af[0] >= 0) {
                    afMsg = numFormat.format(af[0]);
                    for (int ii = 1; ii < af.length; ii++) {
                        afMsg += ", " + numFormat.format(af[ii]);
                    }
                }
                toolTip.append("<br>Allele Frequency: " + afMsg + "<br>");

                if (variant.getSampleNames().size() > 0) {
                    toolTip = toolTip.append(getSampleToolTip(null, variant));
                    double afrac = variant.getAlleleFraction();
                    toolTip = toolTip.append("Minor Allele Fraction: " + numFormat.format(afrac) + "<br>");
                }

                toolTip.append("<b>Genotypes:</b>");
                // for all samples!
                addBrIfMissing(toolTip);
                toolTip.append(getGenotypeToolTip(variant) + "<br>");
            }
        }
        addBrIfMissing(toolTip);
        String vi = getVariantInfo(variant, isShort, isVeryShort);
        if (vi.length()>0) toolTip.append(vi + "<br>");

        return toolTip.toString();
    }

    private void addBrIfMissing(StringBuffer buf) {
        int pos = buf.lastIndexOf("<br>");
        if (pos < 0 || pos < buf.length() - 6) {
            buf.append("<br>");
        }
    }

    protected String getVariantInfo(Variant variant) {
        return getVariantInfo(variant, false, false);
    }

    protected String getVariantInfo(Variant variant, boolean isShort, boolean isVeryShort) {
        Set<String> keys = variant.getAttributes().keySet();
        if (keys.size() > 0) {

            if (isVeryShort) {
                String res = "";
                String key = "CONFIDENCE";
                String value = variant.getAttributeAsString(key);               
                if (value != null && value.length() > 0) {
                    String name = getFullName(key);                   
                    res = "<br>" + name + ": " + variant.getAttributeAsString(key);
                }
                key = "SVTYPE";
                value = variant.getAttributeAsString(key);
              
                if (value != null && value.length() > 0) {
                    String name = getFullName(key);
                    res +="<br>" + name + ": " + variant.getAttributeAsString(key);
                }
                return res;
            } else {
                String toolTip = "<b>Variant Attributes:</b>";
                int count = 0;
                boolean skipped = false;
                // Put AF and GMAF and put at the top, if present
                String k = "AF";
                String afValue = variant.getAttributeAsString(k);
                if (afValue != null && afValue.length() > 0 && !afValue.equals("null")) {
                    toolTip = toolTip.concat("<br>" + getFullName(k) + ": " + variant.getAttributeAsString(k));
                }
                k = "GMAF";
                afValue = variant.getAttributeAsString(k);
                if (afValue != null && afValue.length() > 0 && !afValue.equals("null")) {
                    toolTip = toolTip.concat("<br>" + getFullName(k) + ": " + variant.getAttributeAsString(k));
                }
                
                int COLS = 2;
                boolean usetable = keys.size() > 10 && !isShort;
                if (keys.size() > 0) {
                    if (usetable) {
                        toolTip = toolTip.concat("<table border='0' cellspacing='0' cellpadding='0'><tr>");
                    } else {
                        toolTip = toolTip.concat("<br>");
                    }
                    int col = 1;
                    int nr = 0;
                    for (String key : keys) {

                        if (key.length() < 1 || key.equals("AF") || key.equals("GMAF")) {
                            continue;
                        }
                        
                        String t = "";
                        String name = getFullName(key);
                        if (name.equalsIgnoreCase("CONFIDENCE")) {
                            name = "<b><font color='000099'>" + name + "</font></b>";
                        }
                        else if (name.equalsIgnoreCase("SVTYPE")) {
                            name = "<b>" + name + "</font></b>";
                        }
                        else {
                            if (this.max_nr_variants_tt < 5 && !key.equalsIgnoreCase("PRECISION") && 
                                    (count > MAX_FILTER_LINES || (isShort && count > MAX_FILTER_LINES/3 )) )  {
                                skipped = true;
                                continue;
                            }                        
                        }
                        if (name.length() > 0) {
                            t = name + ": " + variant.getAttributeAsString(key);
                            if (this.max_nr_variants_tt < 5 && t.length() > 60) {
                                t = t.substring(0, 60) + "...";
                            }
                        } else {
                            continue;
                        }
                        count++;
                        if (usetable) {
                            toolTip = toolTip.concat("<td>" + t + "</td>");
                            if (col % COLS == 0 || t.length() > 30) {
                                toolTip = toolTip.concat("</tr><tr>");
                                col = 0;
                            }
                        } else {
                            toolTip = toolTip.concat(t + "<br>");
                        }
                        col++;
                    }
                    if (usetable) {
                        toolTip = toolTip.concat("</tr></table>");
                    }
                    if (skipped) {
                        toolTip = toolTip.concat("...");
                    }

                }


                return toolTip;
            }
        }
        return " ";
    }

    private String getSampleInfo(Genotype genotype) {
        Set<String> keys = genotype.getAttributes().keySet();
        if (keys.size() > 0) {
            String tooltip = "<br><b>Sample Attributes</b>";
            for (String key : keys) {
                try {
                    tooltip = tooltip.concat("<br>" + getFullName(key) + ": " + genotype.getAttributeAsString(key));
                } catch (IllegalArgumentException iae) {
                    tooltip = tooltip.concat("<br>" + key + ": " + genotype.getAttributeAsString(key));
                }
            }
            return tooltip;
        }
        return null;
    }

    public void clearSelectedVariant() {
        selectedVariant = null;
    }

    public List<String> getAllSamples() {
        return allSamples;
    }

    public int getSquishedHeight() {
        return squishedHeight;
    }

    public void setSquishedHeight(int squishedHeight) {
        this.squishedHeight = squishedHeight;
    }

    public void onTrackGroupEvent(TrackGroupEvent e) {
        setupGroupsFromAttributes();
    }

    public boolean hasAlignmentFiles() {
        return alignmentFiles != null && !alignmentFiles.isEmpty();
    }

    public Collection<String> getSelectedSamples() {
        return selectedSamples;
    }

    /**
     * @return the userUserHeight
     */
    public boolean isUserSetHeight() {
        return userSetHeight;
    }

    /**
     * @param userUserHeight the userUserHeight to set
     */
    public void setUserSetHeight(boolean b) {
        this.userSetHeight = b;
    }

    public static enum ColorMode {

        GENOTYPE, METHYLATION_RATE, ALLELE, TRACK
    }

    public static enum BackgroundType {

        NAME, ATTRIBUTE, DATA;
    }
    static Map<String, String> fullNames = new HashMap();

    static {
        fullNames.put("AA", "Ancestral Allele");
        fullNames.put("AC", "Allele Count in Genotypes");
        fullNames.put("AN", "Total Alleles in Genotypes");
        fullNames.put("AF", "Allele Frequency");
        fullNames.put("CN", "Copy Number");
        fullNames.put("DP", "Depth");
        fullNames.put("MQ", "Mapping Quality");
        fullNames.put("NS", "Number of Samples with Data");
        fullNames.put("BQ", "RMS Base Quality");
        fullNames.put("SB", "Strand Bias");
        fullNames.put("DB", "dbSNP Membership");
        fullNames.put("GQ", "Genotype Quality");
        fullNames.put("GL", "Genotype Likelihoods");  //Hom-ref, het, hom-var break this down into a group
    }

    static String getFullName(String key) {
        return fullNames.containsKey(key) ? fullNames.get(key) : key;
    }

    private String getSampleToolTip(String sample, List<Variant>  variants) {
        String res = "";
        for (Variant v: variants) {
            res += getSampleToolTip(sample, v)+"<br>";
        }
        return res;
    }
    private String getSampleToolTip(String sample, Variant variant) {
        if (sample == null) {
            if (variant.getSampleNames() != null && variant.getSampleNames().size() > 0) {
                sample = (String) variant.getSampleNames().iterator().next();
            }
        }
        if (sample == null) {
            return "";
        }
        double goodBaseCount = variant.getGenotype(sample).getAttributeAsDouble("GB");
        if (Double.isNaN(goodBaseCount)) {
            goodBaseCount = 0;
        }
        if (sample != null && isEnableMethylationRateSupport() && goodBaseCount < 10) {
            return sample;
        }
        String id = variant.getID();
        StringBuffer toolTip = new StringBuffer();
        toolTip = toolTip.append("Position: " + variant.getChr());
        toolTip.append(", " + variant.getPositionString());
        toolTip = toolTip.append("<br>ID: " + id + "<br>");
        toolTip = toolTip.append("<b>Sample Information:</b>");

       
        Genotype genotype = variant.getGenotype(sample);
        if (genotype != null && genotype.getAttributes() != null) {
            Set<String> keys = genotype.getAttributes().keySet();
            int COLS = 3;
            boolean usetable = keys.size() > 3;
            if (keys.size() > 0) {
                if (usetable) {
                    toolTip = toolTip.append("<table border='0'  cellspacing='0' cellpadding='0'><tr>");
                } else {
                    toolTip = toolTip.append("");
                }
                int col = 1;
                int nr = 0;
                for (String key : keys) {
                    String t = "";
                    try {
                        t = getFullName(key) + ": " + genotype.getAttributeAsString(key);
                    } catch (IllegalArgumentException iae) {
                        t = key + ":" + genotype.getAttributeAsString(key);
                    }
                    if (usetable) {
                        toolTip = toolTip.append("<td height='18'>" + t + "</td>");

                        if (col % COLS == 0) {
                            toolTip = toolTip.append("</tr><tr>");
                            col = 1;
                        }
                    } else {
                        toolTip = toolTip.append("<br>");
                    }
                    nr++;
                    col++;
                }
                if (usetable) {
                    toolTip = toolTip.append("</tr></table>");
                }
            } else {
                toolTip = toolTip.append("<br>");
            }
            toolTip = toolTip.append("Bases: " + genotype.getGenotypeString());
            toolTip = toolTip.append("<br>Quality: " + numFormat.format(genotype.getPhredScaledQual()));
            toolTip = toolTip.append("<br>Type: " + genotype.getType());
        }
        if (variant.isFiltered()) {
            toolTip = toolTip.append("<br>Is Filtered Out: Yes</b>");
            toolTip = toolTip.append(getFilterTooltip(variant));
        } else {
            toolTip = toolTip.append("<br>Is Filtered Out: No</b><br>");
        }
        return toolTip.toString();
    }

    private String getFilterTooltip(Variant variant) {
        Collection filters = variant.getFilters();
        String toolTip = "<br>";
        for (Object filter : filters) {
            toolTip = toolTip.concat("- " + (String) filter + "<br>");
        }

        return toolTip;
    }

    private String getAlleleToolTip(Variant counts) {
        double noCall = counts.getNoCallCount() * 2;
        double aNum = (counts.getHetCount() + counts.getHomRefCount() + counts.getHomVarCount()) * 2;
        double aCount = (counts.getHomVarCount() * 2 + counts.getHetCount()) * 2;

        String toolTip = "<br>No Call: " + (int) noCall;
        toolTip = toolTip.concat("<br>Allele Num: " + (int) aNum).concat("; count: " + (int) aCount);
        return toolTip;
    }

    private String getGenotypeToolTip(Variant counts) {
        int noCall = counts.getNoCallCount();
        int homRef = counts.getHomRefCount();
        int nonVar = noCall + homRef;
        int het = counts.getHetCount();
        int homVar = counts.getHomVarCount();
        int var = het + homVar;

        String toolTip = "Non Variant: " + nonVar;
        toolTip = toolTip.concat("<br> - No Call: " + noCall);
        toolTip = toolTip.concat("<br> - Hom Ref: " + homRef);
        toolTip = toolTip.concat("<br>Variant: " + var);
        toolTip = toolTip.concat("<br> - Het: " + het);
        toolTip = toolTip.concat("<br> - Hom Var: " + homVar);
        return toolTip;
    }

    public IGVPopupMenu getPopupMenu(final TrackClickEvent te) {

        final ReferenceFrame referenceFrame = te.getFrame();
        selectedVariant = null;
        if (referenceFrame != null && referenceFrame.getName() != null) {
            final double position = te.getChromosomePosition();
            double maxDistance = 10 * referenceFrame.getScale();
            List<Variant> list = getFeatureClosests(position, maxDistance, referenceFrame);
            // If more than ~ 20 pixels distance reject
            if (list != null && list.size() > 0) {
                selectedVariant = list.get(0);
                IGV.getInstance().doRefresh();
            }
        }

        return new VariantMenu(this, selectedVariant);
    }

    /**
     * Handle a mouse click from the name panel.
     * <p/>
     * if (e.isMetaDown() || e.isControlDown()) { toggleTrackSelections(e); }
     * else if (e.isShiftDown()) { shiftSelectTracks(e); } else if
     * (!isTrackSelected(e)) { clearTrackSelections(); selectTracks(e); }
     *
     * @param e
     */
    @Override
    public void handleNameClick(MouseEvent e) {
        String sampleAtPosition = getSampleAtPosition(e.getY());

        if (e.isPopupTrigger()) {
            return;
        }

        if (e.isMetaDown() || e.isControlDown()) {
            if (sampleAtPosition != null) {
                if (selectedSamples.contains(sampleAtPosition)) {
                    //    selectedSamples.remove(sampleAtPosition);
                } else {
                    selectedSamples.add(sampleAtPosition);
                }
            }
        } else if (e.isShiftDown() && !selectedSamples.isEmpty()) {
            int idx = getSampleIndex(sampleAtPosition);
            int lastIDX = getSampleIndex(selectedSamples.get(selectedSamples.size() - 1));
            if (idx >= 0 && lastIDX >= 0) {
                selectedSamples.clear();
                for (int i = Math.min(idx, lastIDX); i <= (Math.max(idx, lastIDX)); i++) {
                    String s = sampleBounds.get(i).sample;
                    selectedSamples.add(s);
                }
            }

        } else {
            if (sampleAtPosition != null) {
                if (selectedSamples.size() == 1 && selectedSamples.contains(sampleAtPosition)) {
                    selectedSamples.clear();
                    IGV.getInstance().repaint();
                    return;
                } else {
                    selectedSamples.clear();
                }
                selectedSamples.add(sampleAtPosition);
            }
        }
        IGV.getInstance().repaint();

    }

    /**
     * Return the index for the sample. This is a very inefficient
     * implementation, but we don't care because these lists are tiny.
     *
     * @param sample
     * @return
     */
    private int getSampleIndex(String sample) {
        for (int i = 0; i < sampleBounds.size(); i++) {
            if (sample.equals(sampleBounds.get(i).sample)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handle a mouse click from the data panel.
     *
     * @param te - wraps the MouseClickEvent and reference frame.
     * @return true if the click is handled, false otherwise
     */
    @Override
    public boolean handleDataClick(TrackClickEvent te) {

        boolean ignore = false;
        if (getResourceLocator() != null) {
            ignore = !getResourceLocator().isAutoLoad();
        }
        
        if (ignore) {
            boolean done =  super.handleDataClick(te);
            log.info("Event already handled in superclass, stopping");
            if (done) return done;
        }
        if (hasAlignmentFiles()) {
            final ReferenceFrame referenceFrame = te.getFrame();
            final double position = te.getChromosomePosition();
            double maxDistance = 10 * referenceFrame.getScale();

            List<Variant> variants = getFeatureClosests(position, maxDistance, te.getFrame());
            Variant f = null;
            if (variants != null && variants.size()>0) f = variants.get(0);
            selectedSamples.clear();
            if (f != null) {
                String selectedSample = getSampleAtPosition(te.getMouseEvent().getY());
                if (selectedSample != null) {
                    // Select clicked sample and all other adjacent with the same genotype
                    Genotype genotype = f.getGenotype(selectedSample);
                    String type = genotype.getType();

                    int idx = getSampleIndex(selectedSample);
                    for (int i = idx; i < sampleBounds.size(); i++) {
                        String s = sampleBounds.get(i).sample;
                        Genotype gt = f.getGenotype(s);
                        if (gt != null && type.equals(gt.getType())) {
                            selectedSamples.add(s);
                        } else {
                            break;
                        }
                    }
                    for (int i = idx - 1; i >= 0; i--) {
                        String s = sampleBounds.get(i).sample;
                        Genotype gt = f.getGenotype(s);
                        if (gt != null && type.equals(gt.getType())) {
                            selectedSamples.add(s);
                        } else {
                            break;
                        }
                    }
                }
            }
            IGV.getInstance().doRefresh();
        }

        if (te.getMouseEvent().getClickCount()>0 || IGV.getInstance().isShowDetailsOnClick()) {            
            openTooltipWindow(te);
        }

        return true;
    }
    protected boolean openTooltipWindow(TrackClickEvent e) {
        ReferenceFrame frame = e.getFrame();
        final MouseEvent me = e.getMouseEvent();
        this.max_nr_variants_tt = 10;
        String popupText = getValueStringAt(frame.getChrName(), e.getChromosomePosition(), e.getMouseEvent().getY(), frame);
        this.max_nr_variants_tt = 1;
        if (popupText != null) {

            final TooltipTextFrame tf = new TooltipTextFrame(getDisplayName(false), popupText);
            Point p = me.getComponent().getLocationOnScreen();
            tf.setLocation(Math.max(0, p.x + me.getX() - 150), Math.max(0, p.y + me.getY() - 150));

            UIUtilities.invokeOnEventThread(new Runnable() {
                public void run() {
                    tf.setVisible(true);
                }
            });
            return true;
        }
        return false;
    }
    @Override
    protected boolean isShowFeatures(RenderContext context) {
        boolean show = super.isShowFeatures(context);
//        if (show) {
//            return show;
//        }
//        // check nr of features
//        if (source == null) {
//            log.info("isShowFeature: no source");
//            return false;
//        }
//        log.info("isShowFeatures: we always show variants");
        return show;
    }

    /**
     * Return the current state of this object as map of key-value pairs. Used
     * to store session state.
     * <p/>
     * // TODO -- this whole scheme could probably be more elegantly handled
     * with annotations.d
     *
     * @return
     */
    public Map<String, String> getPersistentState() {

        Map<String, String> attributes = super.getPersistentState();
        attributes.put(IGVSessionReader.SessionAttribute.RENDER_NAME.getText(), String.valueOf(renderID));

        ColorMode mode = getColorMode();
        if (mode != null) {
            attributes.put(IGVSessionReader.SessionAttribute.COLOR_MODE.getText(), mode.toString());
        }

        if (squishedHeight != DEFAULT_SQUISHED_GENOTYPE_HEIGHT) {
            attributes.put("SQUISHED_ROW_HEIGHT", String.valueOf(squishedHeight));
        }

        return attributes;
    }

    public void restorePersistentState(Map<String, String> attributes) {
        super.restorePersistentState(attributes);

        String rendername = attributes.get(IGVSessionReader.SessionAttribute.RENDER_NAME.getText());
        if (rendername != null) {
            setRenderID(rendername.equalsIgnoreCase("true"));
        }

        String colorModeText = attributes.get(IGVSessionReader.SessionAttribute.COLOR_MODE.getText());
        if (colorModeText != null) {
            try {
                setColorMode(ColorMode.valueOf(colorModeText));
                log.info("Got color mode: " + colorModeText);
            } catch (Exception e) {
                log.error("Error interpreting color mode: " + colorModeText);
            }
        }

        String squishedHeightText = attributes.get("SQUISHED_ROW_HEIGHT");
        if (squishedHeightText != null) {
            try {
                squishedHeight = Integer.parseInt(squishedHeightText);
            } catch (Exception e) {
                log.error("Error restoring squished height: " + squishedHeightText);
            }
        }
    }

    public void loadSelectedBams() {
        Runnable runnable = new Runnable() {
            public void run() {
                // Use a set to enforce uniqueness
                final int nSamples = selectedSamples.size();
                if (nSamples == 0) {
                    return;
                }

                Set<String> bams = new HashSet<String>(nSamples);
                String name = "";
                int n = 0;
                for (String sample : selectedSamples) {
                    bams.add(getBamFileForSample(sample));
                    n++;
                    if (n < 7) {
                        if (n == 6) {
                            name += "...";
                        } else {
                            name += sample;
                            if (n < nSamples) {
                                name += ", ";
                            }
                        }
                    }
                }

                if (bams.size() > 20) {
                    boolean proceed = MessageUtils.confirm("Are you sure you want to load " + nSamples + " bams?");
                    if (!proceed) {
                        return;
                    }
                }

                String bamList = "";
                for (String bam : bams) {
                    bamList += bam + ",";

                }
                ResourceLocator loc = new ResourceLocator(bamList);
                loc.setType("alist");
                loc.setName(name);
                List<Track> tracks = IGV.getInstance().load(loc);

                TrackPanel panel = IGV.getInstance().getVcfBamPanel();
                panel.clearTracks();
                panel.addTracks(tracks);
            }
        };

        LongRunningTask.submit(runnable);
    }

    /**
     * Return the nextLine or previous feature relative to the center location.
     * <p/>
     * Loop through "next feature from super implementation until first
     * non-filtered variant is found.
     *
     * @param chr
     * @param center
     * @param forward
     * @return
     * @throws java.io.IOException
     */
    @Override
    public Feature nextFeature(String chr, double center, boolean forward, ReferenceFrame frame) throws IOException {

        if (getHideFiltered()) {
            Feature f;
            while ((f = super.nextFeature(chr, center, forward, frame)) != null) {
                if (!(f instanceof Variant) || !((Variant) f).isFiltered()) {
                    return f;
                }
            }
            return null;
        } else {
            return super.nextFeature(chr, center, forward, frame);
        }
    }

    static class SampleBounds {

        int top;
        int bottom;
        String sample;

        SampleBounds(int top, int bottom, String sample) {
            this.top = top;
            this.bottom = bottom;
            this.sample = sample;
        }

        boolean contains(int y) {
            return y >= top && y <= bottom;
        }
    }
}
