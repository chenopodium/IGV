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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.ui.panel;

//~--- non-JDK imports --------------------------------------------------------
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.FeatureUtils;
import org.broad.igv.feature.exome.ExomeBlock;
import org.broad.igv.feature.exome.ExomeReferenceFrame;
import org.broad.igv.sam.CoverageTrack;
import org.broad.igv.track.RenderContext;
import org.broad.igv.track.RenderContextImpl;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackGroup;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.UIConstants;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.broad.igv.Globals;
import org.broad.igv.feature.Chromosome;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.feature.genome.GenomeImpl;
import org.broad.igv.feature.genome.GenomeManager;
import org.broad.igv.track.AbstractTrack;
import org.broad.igv.track.DataTrack;

/**
 * @author jrobinso
 *
 * Apart from what you wrote, here are two things I'm encountering: 1. 'Show
 * data range' does not seem to work in exome view (it doesn't display the range
 * on the side). 2. In terms of the display - the lines running from the top
 * track (showing the gene name) go all the way through, and make it difficult
 * to tell whether the lines are coming from an exon, or from this top line. It
 * should ideally be more intutitve exactly what the exon-exon structure is.
 * Maybe this top line should just be removed altogether - it doesn't really
 * provide any additional information, and it does actually get quite confusing
 * when you have two genes overlapping each other (in which case it's unclear to
 * me based on what the top track decides whether to display a section as the
 * first gene or the other, but it's often confusing). 3. Something I suggested
 * in the past: It would be nice if I could toggle back and forth more rapidly,
 * without having to select each time the same bullet (based on which introns
 * are removed). Thanks a lot!
 *
 */
public class DataPanelPainter {

    private static Logger log = Logger.getLogger(DataPanelPainter.class);
    private static Color exomeBorderColor = new Color(190, 190, 255);
    /**
     * Hacky field to keep scales from drawing multiple times in Exome view
     */
    private boolean scalesDrawn;

    public synchronized void paint(Collection<TrackGroup> groups,
            RenderContext context,
            int width,
            Color background,
            Rectangle visibleRect) {

        Graphics2D graphics2D = null;

        try {
            graphics2D = (Graphics2D) context.getGraphics().create();
            graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, PreferenceManager.getInstance().getAntiAliasingHint());
            graphics2D.setBackground(background);
            graphics2D.clearRect(visibleRect.x, visibleRect.y, visibleRect.width, visibleRect.height);
            graphics2D.setColor(Color.BLACK);

            ReferenceFrame frame = context.getReferenceFrame();

            if (frame.isExomeMode()) {

                ExomeReferenceFrame exomeFrame = (ExomeReferenceFrame) frame;
                int blockGap = exomeFrame.getBlockGap();

                Rectangle panelClip = visibleRect;

                List<ExomeBlock> blocks = exomeFrame.getBlocks();
                int idx = exomeFrame.getFirstBlockIdx();

                RenderContext exomeContext = new RenderContextImpl(null, null, frame, visibleRect);
                preloadTracks(groups, exomeContext, visibleRect);

                ExomeBlock b;
                int lastPStart = 0;
                int pStart;
                int pEnd;
                int exomeOrigin = ((ExomeReferenceFrame) frame).getExomeOrigin();
                int visibleBlockCount = 0;
                scalesDrawn = false;

                do {
                    b = blocks.get(idx);

                    pStart = (int) ((b.getExomeStart() - exomeOrigin) / frame.getScale()) + visibleBlockCount * blockGap;
                    pEnd = (int) ((b.getExomeEnd() - exomeOrigin) / frame.getScale()) + visibleBlockCount * blockGap;

                    if (pEnd > lastPStart) {

                        lastPStart = pStart;
                        // Don't draw over previously drawn region -- can happen when zoomed out.


                        if (pEnd == pStart) {
                            pEnd++;
                        }


                        b.setScreenBounds(pStart, pEnd);

                        Rectangle rect = new Rectangle(pStart, visibleRect.y, pEnd - pStart, visibleRect.height);

                        Graphics2D exomeGraphics = (Graphics2D) context.getGraphics().create();
                        exomeGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, PreferenceManager.getInstance().getAntiAliasingHint());

                        //Shape clip = exomeGraphics.getClip();

//                         Color c = ColorUtilities.randomColor(idx);
//                         exomeGraphics.setColor(c);
//                         exomeGraphics.fill(rect);
//                         exomeGraphics.setColor(Color.black);
//                         GraphicUtils.drawCenteredText(String.valueOf(idx), rect, exomeGraphics);

                        exomeGraphics.setClip(rect.intersection(panelClip));
                        exomeGraphics.translate(pStart, 0);

                        ReferenceFrame tmpFrame = new ReferenceFrame(frame);
                        tmpFrame.setOrigin(b.getGenomeStart());


                        RenderContext tmpContext = new RenderContextImpl(null, exomeGraphics, tmpFrame, rect);
                        paintFrame(groups, tmpContext, rect.width, rect);

                        tmpContext.dispose();
                        exomeGraphics.dispose();
                        visibleBlockCount++;
                    }
                    idx++;

                } while ((pStart < visibleRect.x + visibleRect.width) && idx < blocks.size());

                // Draw lines @ gene boundaries
                String chr = frame.getChrName();
                List<ExomeReferenceFrame.Gene> genes = exomeFrame.getGenes(chr);

                idx = FeatureUtils.getIndexBefore(frame.getOrigin(), genes);

                exomeOrigin = ((ExomeReferenceFrame) frame).getExomeOrigin();
                int top = visibleRect.y;

                int lastXDrawn = -1;
                Graphics2D lineGraphics = context.getGraphic2DForColor(exomeBorderColor);
                do {
                    ExomeReferenceFrame.Gene gene = genes.get(idx);
                    double exomeStart = exomeFrame.genomeToExomePosition(gene.getStart());
                    double exomeEnd = exomeFrame.genomeToExomePosition(gene.getEnd());
                    pStart = (int) ((exomeStart - exomeOrigin) / frame.getScale()) + visibleBlockCount * blockGap;
                    pEnd = (int) ((exomeEnd - exomeOrigin) / frame.getScale()) + visibleBlockCount * blockGap;

                    if (pStart != lastXDrawn) {
                        lineGraphics.drawLine(pStart, top, pStart, top + visibleRect.height);
                    }
                    lineGraphics.drawLine(pEnd, top, pEnd, top + visibleRect.height);
                    lastXDrawn = pEnd;
                    idx++;

                } while ((pStart < visibleRect.x + visibleRect.width) && idx < genes.size());


            } else {
                paintFrame(groups, context, width, visibleRect);
            }


        } finally {
            graphics2D.dispose();
        }
    }

    private void paintFrame(Collection<TrackGroup> groups,
            RenderContext context,
            int width,
            Rectangle visibleRect) {


        int trackX = 0;
        int trackY = 0;
        boolean anyScaleDrawn = false;

      //  log.info("======================== DataPanelPainter. paintFrame");

        for (Iterator<TrackGroup> groupIter = groups.iterator(); groupIter.hasNext();) {
            TrackGroup group = groupIter.next();

            if (visibleRect != null && (trackY > visibleRect.y + visibleRect.height)) {
                //     log.info("NOT darwing troup "+group.getName()+", y="+trackY+" > visible rect height "+(visibleRect.y + visibleRect.height));
                break;
            }
            if (group.isVisible()) {

                if (groups.size() > 1) {
                    final Graphics2D greyGraphics = context.getGraphic2DForColor(UIConstants.LIGHT_GREY);
                    greyGraphics.fillRect(0, trackY + 1, width, UIConstants.groupGap - 1);
                    trackY += UIConstants.groupGap;
                }
                //   log.info("=== Drawing group " + group.getName());
                // Draw a line just above group.
                if (group.isDrawBorder()) {
                    //  log.info("Drawing border");
                    Graphics2D graphics2D = context.getGraphic2DForColor(Color.black);
                    graphics2D.drawLine(0, trackY - 1, width, trackY - 1);
                }
                //  else log.info("NOT drawing border");

                List<Track> trackList = group.getTracks();
                HashMap<Track, Integer> trackymap = new HashMap<Track, Integer>();
                synchronized (trackList) {
                    ArrayList<Track> visibleTracks = new ArrayList<Track>();

                    for (Track track : trackList) {
                        if (track == null) {
                            continue;
                        }
                        int trackHeight = track.getHeight();
                        // log.info(track.getDisplayName()+", h="+trackHeight);
                        if (visibleRect != null) {
                            if (trackY > visibleRect.y + visibleRect.height) {
                                break;
                            } else if (trackY + trackHeight < visibleRect.y) {
                                if (track.isVisible()) {
                                    trackY += trackHeight;
                                    //       log.info("          visible, h="+trackHeight+", tracky is now = "+trackY);
                                }
                                //      log.info("         "+(trackY + trackHeight)+"<"+visibleRect.y+" => not drawing, continue");
                                continue;
                            }


                            //  log.info("     a) " + track.getDisplayName() + " tracky=" + trackY);
                            if (track.isVisible()) {
                                visibleTracks.add(track);
                                trackymap.put(track, trackY);
                                if (track instanceof DataTrack) {
                                    DataTrack atrack = (DataTrack) track;
                                    atrack.setDataRangeComputed(false);
                                }
                            }
                            trackY += trackHeight;
                        }
                        // else log.info("Got no visible rect");
                    }
                    linkTracks(visibleTracks, context);
                    for (Track track : visibleTracks) {
                        int trackHeight = track.getHeight();
                        int curY = trackymap.get(track);

                        Rectangle rect = new Rectangle(trackX, curY, width, trackHeight);
                        //   log.info("     Drawing " + track.getDisplayName() + " tracky=" + curY +  "-" + rect.getMaxY()+", x="+trackX);

                        // DEBUGGING
//                        Graphics2D g = context.getGraphics();
//                        g.setColor(Color.yellow);
//                        g.fillRect(rect.x, rect.y, rect.width, rect.height);
//                        g.setColor(Color.RED);                        
//                        g.drawRect(rect.x, rect.y, rect.width, rect.height);
//                        g.drawString(track.getDisplayName(), rect.x+5, rect.y+20);
                        draw(track, rect, context);

                        //TODO Hack to keep from rendering scale multiple times in Exome View
                        if (track instanceof CoverageTrack && FrameManager.isExomeMode() && !scalesDrawn) {
                            int x = context.getGraphics().getClipBounds().x;
                            Rectangle scaleRect = new Rectangle(x, rect.y, rect.width, rect.height);
                            ((CoverageTrack) track).drawScale(context, scaleRect);
                            anyScaleDrawn = true;
                            //  log.info("             drawing scales");
                        }
                        //    else log.info("             NOT drawing scales");
                    }
                }

                // Draw a line just below group.
                if (group.isDrawBorder()) {
                    Graphics2D graphics2D = context.getGraphic2DForColor(Color.black);
                    graphics2D.drawLine(0, trackY, width, trackY);
                }
            }
        }
        ReferenceFrame frame = context.getReferenceFrame();
        if (isWholeGenomeView(frame)) {
            Color blueline = new Color(180, 180, 255, 150);
            Graphics2D g = context.getGraphic2DForColor(blueline);
            float dash1[] = {5.0f};
            Stroke s = new BasicStroke(1.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, dash1, 0.0f);
            g.setStroke(s);
            g.drawLine(100, 0, 100, context.getPanel().getHeight());

            Genome genome = GenomeManager.getInstance().getCurrentGenome();
            List<String> chrNames = genome.getLongChromosomeNames();


            int locationUnit = 1000;

            int height = context.getPanel().getHeight();
            long offset = 0;
            for (String chrName : chrNames) {
                Chromosome c = genome.getChromosome(chrName);
                if (c == null) {
                    continue;
                }
                int chrLength = c.getLength();

                double scale = frame.getScale();
                int gStart = genome.getGenomeCoordinate(chrName, 0);
                int x = (int) (gStart / scale)+1;

                if (offset > 0) g.drawLine(x, 2, x, height - 2);
                offset += chrLength;
            }
        } 
        scalesDrawn |= anyScaleDrawn;
    }

    private boolean isWholeGenomeView(ReferenceFrame frame) {
        return frame.getChrName().equals(Globals.CHR_ALL);
    }

    final private void draw(Track track, Rectangle rect, RenderContext context) {

        //   Logger.getLogger("DataPanelPainter").info("========= draw " + track.getName());
        track.render(context, rect);

        // Get overlays

        List<Track> overlayTracks = IGV.getInstance().getOverlayTracks(track);
        if (overlayTracks != null) {
            for (Track overlayTrack : overlayTracks) {

                // Don't overlay on self
                if (overlayTrack != track) {
                    overlayTrack.overlay(context, rect);
                }
            }
        }

    }

    private List<Track> getVisibleTracks(final Collection<TrackGroup> groups) {
        // Find the tracks that need loaded, we go to this bother to avoid loading tracks scrolled out of view
        final List<Track> visibleTracks = new ArrayList<Track>();
        for (Iterator<TrackGroup> groupIter = groups.iterator(); groupIter.hasNext();) {
            TrackGroup group = groupIter.next();
            List<Track> trackList = new ArrayList(group.getTracks());
            for (Track track : trackList) {
                if (track != null && track.isVisible()) {
                    visibleTracks.add(track);
                }
            }
        }
        return visibleTracks;
    }

    private void preloadTracks(final Collection<TrackGroup> groups,
            final RenderContext context,
            final Rectangle visibleRect) {


        final List<Track> visibleTracks = getVisibleTracks(groups);

        Runnable runnable = new Runnable() {
            public void run() {
                for (Track track : visibleTracks) {
                    RenderContextImpl newContext = new RenderContextImpl(null, null, context.getReferenceFrame(), visibleRect);
                    track.preload(newContext);
                }
            }
        };

        runnable.run();

//        Future future = LongRunningTask.submit(runnable);
//        try {
//            future.get();
//        } catch (InterruptedException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (ExecutionException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

//        Thread workerThread = new Thread(runnable);
//        workerThread.start();
//        try {
//            workerThread.join();
//        } catch (InterruptedException e) {
//            log.error("Preload thread was interrupted", e);
//        }
    }

    public void linkTracks(ArrayList<Track> visibleTracks, RenderContext context) {
        for (Track track : visibleTracks) {
            if (track instanceof DataTrack) {
                DataTrack atrack = (DataTrack) track;
                if (!atrack.isDataRangeComputed()) {
                    ArrayList<DataTrack> linked = atrack.getLinkedTracks();
                    if (linked != null && linked.size() > 0) {
                        atrack.computeVisibleDataRange(context);
                        //  log.info("--- Now computing data ranges, linking and then painting: "+atrack.getName());
                        for (DataTrack dt : linked) {
                            //      log.info("             computeVisibleDataRange: "+dt.getName());
                            dt.computeVisibleDataRange(context);
                        }
                        //  log.info("--- Now calling linkDataRanges for: "+atrack.getName());
                        atrack.linkDataRanges(linked);
                    }
                }
            }
        }
    }
}
