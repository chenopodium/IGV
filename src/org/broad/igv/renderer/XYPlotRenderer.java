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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.renderer;

//~--- non-JDK imports --------------------------------------------------------
import com.iontorrent.cnv.CustomCnvDataSourceTrack;
import com.iontorrent.utils.ErrorHandler;
import org.broad.igv.Globals;
import org.broad.igv.PreferenceManager;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.track.RenderContext;
import org.broad.igv.track.Track;
import org.broad.igv.ui.FontManager;
import org.broad.igv.ui.UIConstants;
import org.broad.igv.ui.panel.FrameManager;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Logger;
import org.broad.igv.data.seg.ReferenceSegment;
import org.broad.igv.data.seg.SummarySegment;
import org.broad.igv.track.WindowFunction;

/**
 * @author jrobinso
 */
public abstract class XYPlotRenderer extends DataRenderer {

    private int msgs;
    private double marginFraction = 0.2;
    static final Logger log = Logger.getLogger(XYPlotRenderer.class.getName());

    protected void drawDataPoint(Color graphColor, int dx, int pX, int baseY, int pY,
            RenderContext context) {
        dx = Math.max(5, dx);

        context.getGraphic2DForColor(graphColor).fillRect(pX, pY, dx, 4);

    }

    public Color getGradientColor(double MIN, double MAX, double score, Color chigh, Color clow, Color cmid, double middle) {


        if (cmid == null) {
            cmid = Color.darkGray;
        }
        if (chigh == null) {
            chigh = cmid;
        }
        if (clow == null) {
            clow = cmid;
        }
        double rangedelta = MAX - MIN;

        if (middle == Integer.MIN_VALUE) {
            middle = (MAX - MIN) / 2;
        }
        // check if we use the higher or lower scale
        double ds = score - MIN;

        Color hi = cmid;
        Color lo = clow;

        if (Math.abs(score - middle) < 0.0000001) {
            return cmid;
        }

        if (score > middle) {
            lo = cmid;
            hi = chigh;
            ds = score - middle;
        }

        double dr = (hi.getRed() - lo.getRed()) / (rangedelta * 0.7);
        double dg = (hi.getGreen() - lo.getGreen()) / (rangedelta * 0.7);
        double db = (hi.getBlue() - lo.getBlue()) / (rangedelta * 0.7);

        // p("Getting color for "+fieldname +"="+score+", min="+MIN+", max="+MAX);
        int r = Math.min(255, Math.max(0, (int) (lo.getRed() + dr * ds)));
        int g = Math.min(255, Math.max(0, (int) (lo.getGreen() + dg * ds)));
        int b = Math.min(255, Math.max(0, (int) (lo.getBlue() + db * ds)));
        Color c = new Color(r, g, b);

        if (middle != 2) {
            p("score: " + score + ", low=" + MIN + ", mid=CUTOFF=" + middle + ", max=" + MAX + " => color=" + c + ", rangedelta=" + rangedelta);
            //c = chigh;
        }
        return c;

    }

    public Color getGainLossColor(double MIN, double MAX, double score, Color chigh, Color clow, Color cmid, double middle) {


        if (cmid == null) {
            cmid = Color.darkGray;
        }
        if (chigh == null) {
            chigh = cmid;
        }
        if (clow == null) {
            clow = cmid;
        }

        if (middle == Integer.MIN_VALUE) {
            middle = (MAX - MIN) / 2;
        }

        if (Math.abs(score - middle) < 0.0000001) {
            return cmid;
        }
        if (score > middle) {
            return chigh;
        } else {
            return clow;
        }

    }

    /**
     * Render the track in the given rectangle.
     *
     * @param track
     * @param locusScores
     * @param context
     * @param arect
     */
    @Override
    // XXX was synchronized
    public synchronized void renderScores(Track track, List<LocusScore> locusScores, RenderContext context, Rectangle arect) {
        try {
            //  log.info("renderScores: "+locusScores.size()+", scale="+context.getScale()+",chr="+context.getChr()+", origin="+context.getOrigin());
            boolean show = locusScores.size() < 200;

            boolean showMissingData = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SHOW_MISSING_DATA_KEY);

            Graphics2D noDataGraphics = context.getGraphic2DForColor(UIConstants.NO_DATA_COLOR);
            Graphics2D tickGraphics = context.getGraphic2DForColor(Color.BLACK);

            Rectangle adjustedRect = calculateDrawingRect(arect);
            // for debugging
            Graphics2D g = context.getGraphics();
            // g.setColor(Color.blue);
            // g.drawRect(arect.x, arect.y, arect.width, arect.height);
            //  g.setColor(Color.green);
            //  g.fillRect(adjustedRect.x, adjustedRect.y, adjustedRect.width, adjustedRect.height);

            double origin = context.getOrigin();
            double locScale = context.getScale();

            Color posColor = track.getColor();
            Color negColor = track.getAltColor();
            Color midColor = track.getMidColor();
            //  p(track.getName()+": high color: "+posColor+", midColor: "+midColor+", low color="+negColor);
            // Get the Y axis definition, consisting of minimum, maximum, and base value.  Often
            // the base value is == min value which is == 0.

            DataRange dataRange = track.getDataRange();
            float maxValue = dataRange.getMaximum();

            float cutOff = 0;
            if (track instanceof CustomCnvDataSourceTrack) {
                // CHANGE METHOD GETCUTOFF SCORE TO INCLUDE POSITION!                    
                cutOff = (float) ((CustomCnvDataSourceTrack) track).getExpectedValue(context.getChr());
                //  p("XYPlotRenderer: Got CustomCnvDataSourceTrack: cutoff at " + context.getChr() + " is " + cutOff);
            } else {
                cutOff = (float) track.getCutoffScore();
                //  p("XYPlotRenderer: NOT a CustomCnvDataSourceTrack track: " + track.getClass().getName());
            }

            float baseValue = 0;


            if (baseValue == 0) {
                baseValue = dataRange.getBaseline();
            }
            float minValue = dataRange.getMinimum();

            //  p(track.getName() + ": high color: " + posColor + ", midColor: " + midColor + ", low color=" + negColor + ", min=" + minValue + ", max=" + maxValue);
            boolean isLog = dataRange.isLog();

            if (isLog) {
                minValue = (float) (minValue == 0 ? 0 : Math.log10(minValue));
                maxValue = (float) Math.log10(maxValue);
                try {
                    cutOff = (float) Math.log10(cutOff);
                } catch (Exception e) {
                }
            }


            // Calculate the Y scale factor.

            double delta = (maxValue - minValue);
            double yScaleFactor = adjustedRect.getHeight() / delta;

            // Calculate the Y position in pixels of the base value.  Clip to bounds of rectangle
            double baseDelta = maxValue - baseValue;
            int baseY = (int) (adjustedRect.getY() + baseDelta * yScaleFactor);
            if (baseY < adjustedRect.y) {
                baseY = adjustedRect.y;
            } else if (baseY > adjustedRect.y + adjustedRect.height) {
                baseY = adjustedRect.y + adjustedRect.height;
            }


//        int midY = getY(isLog, cutOff, baseValue, baseY, yScaleFactor, adjustedRect);
//        Graphics2D midG = context.getGraphic2DForColor(Color.lightGray);
//        midG.drawLine(midY, midY, midY, midY);

            int lastPx = 0;
            boolean first = true;

            for (LocusScore score : locusScores) {
                show = false;// score instanceof ReferenceSegment;


                first = false;
                // Note -- don't cast these to an int until the range is checked.
                // could get an overflow.

                double pX = ((score.getStart() - origin) / locScale);
                double pEnd = ((score.getEnd() - origin) / locScale);

                if (show) {
                    p("Maybe Drawing score: " + score.getStart() + "-" + score.getEnd() + ", x1, x2=" + pX + "-" + pEnd);
                }
                if (pX < 0 && pEnd < 0) {
                    if (show) {
                        p("start/end < 0");
                    }
                    continue;
                } else if (pX > adjustedRect.getMaxX() && pEnd > adjustedRect.getMaxX()) {
                    if (show) {
                        p("start/end too large: max is " + adjustedRect.getMaxX());
                    }
                    continue;
                }
                double dx = Math.ceil((Math.max(1, score.getEnd() - score.getStart())) / locScale) + 1;


                //  dx = Math.min(dx, adjustedRect.getWidth());
                // dx = Math.max(dx, 0);
                if ((pX + dx < 0)) {
                    p("continue 1");
                    continue;
                } else if (pX > adjustedRect.getMaxX()) {
                    p("break 1, px too large:" + pX + ">  " + adjustedRect.getMaxX());
                    continue;
                }

                float dataY = score.getScore();
                if (isLog && dataY <= 0) {
                    p("continue 2: dataY <=0");
                    continue;
                }


                if (pX < adjustedRect.getMinX()) {
                    double d = adjustedRect.getMinX() - pX;
                    dx = dx - d;
                    pX = Math.max(pX, adjustedRect.getMinX());
                }


                dx = Math.min(dx, adjustedRect.getWidth() - pX);
                if (!Float.isNaN(dataY)) {

                    int pY = getY(isLog, dataY, baseValue, baseY, yScaleFactor, adjustedRect);
                    if (show) {
                        p("pX=" + pX + ", dx=" + dx + ", py=" + pY + ", rect y=" + adjustedRect.getY() + "-" + adjustedRect.getMaxY());
                    }
//                    if (msgs < 10 && baseValue != 0) {
//                        Logger.getLogger("XY: base=" + baseValue + ", data=" + dataY);
//                        msgs++;
//                    }
                    //Color color = (dataY >= baseValue) ? posColor : negColor;
                    // if sepecial segment?
                    Color color = Color.black;
                    boolean draw = true;
                    if (score instanceof SummarySegment) {
                        color = Color.green.darker();
                    } else if (score instanceof ReferenceSegment) {
                        if (track.getWindowFunction() != null && track.getWindowFunction() == WindowFunction.noRefLine) {
                            draw = false;
                            if (show) {
                                p("orange line toggled off");
                            }
                        } else {
                            if (show) {
                                p("Got ref segment");
                            }
                            color = Color.orange;
                            // if ()
                        }
                    } else {
                        if (track instanceof CustomCnvDataSourceTrack) {
                            color = getGainLossColor(minValue, maxValue, dataY, posColor, negColor, midColor, cutOff);
                            //p("Got color "+color+" for "+dataY+" and mid "+cutOff+", chigh="+posColor+", clow="+negColor);
                        } else {
                            color = getGradientColor(minValue, maxValue, dataY, posColor, negColor, midColor, cutOff);
                        }
                    }
                    if (draw) {
                        if (show) {
                            p("really drawing data point from pX=" + pX + ", dx=" + dx);
                        }
                        drawDataPoint(color, (int) dx, (int) pX, baseY, pY, context);
                    }

                } else if (show) {
                    p("Y is nan");
                }

                if (showMissingData) {
                    if (msgs < 100) {
                        Logger.getLogger("XYPLot").info("Drawing missing data");
                        msgs++;
                    }
                    // Draw from lastPx + 1  to pX - 1;
                    int w = (int) pX - lastPx - 4;
                    if (w > 0) {
                        noDataGraphics.fillRect(lastPx + 2, (int) arect.getY(), w, (int) arect.getHeight());
                    }
                }
                if (!Float.isNaN(dataY)) {

                    lastPx = (int) pX + (int) dx;

                }
            }
            if (showMissingData) {
                int w = (int) arect.getMaxX() - lastPx - 4;
                if (w > 0) {
                    noDataGraphics.fillRect(lastPx + 2, (int) arect.getY(), w, (int) arect.getHeight());
                }
            }

        } catch (Exception e) {
            log.warning(ErrorHandler.getString(e));
        }
    }

    private void p(String s) {
        // System.out.println("XYRENDERER: " + s);
    }
    static DecimalFormat formatter = new DecimalFormat("#.##");

    /**
     * Method description
     *
     * @param track
     * @param context
     * @param arect
     */
    @Override
    public void renderAxis(Track track, RenderContext context, Rectangle arect) {

        // For now disable axes for all chromosome view
//        if (context.getChr().equals(Globals.CHR_ALL)) {
//            return;
//        }

        super.renderAxis(track, context, arect);

        Rectangle drawingRect = calculateDrawingRect(arect);

        PreferenceManager prefs = PreferenceManager.getInstance();

        Color labelColor = prefs.getAsBoolean(PreferenceManager.CHART_COLOR_TRACK_NAME) ? track.getColor() : Color.black;
        Graphics2D lg = context.getGraphic2DForColor(labelColor);

        lg.setFont(FontManager.getFont(8));

        if (prefs.getAsBoolean(PreferenceManager.CHART_DRAW_TRACK_NAME)) {

            // Only attempt if track height is > 25 pixels
            if (arect.getHeight() > 25) {
                Rectangle labelRect = new Rectangle(arect.x, arect.y + 10, arect.width, 10);
                lg.setFont(FontManager.getFont(10));
                GraphicUtils.drawCenteredText(track.getDisplayName(), labelRect, lg);
            }
        }

        if (prefs.getAsBoolean(PreferenceManager.CHART_DRAW_Y_AXIS)) {
            Font smallFont = FontManager.getFont(8);
            lg.setFont(smallFont);

            Rectangle axisRect = new Rectangle(arect.x, arect.y + 1, AXIS_AREA_WIDTH, arect.height);


            DataRange axisDefinition = track.getDataRange();
            float maxValue = axisDefinition.getMaximum();
            float baseValue = axisDefinition.getBaseline();
            float minValue = axisDefinition.getMinimum();


            // draw small ticks first.
            // Bottom (minimum tick mark)
            int minY = computeYPixelValue(drawingRect, axisDefinition, minValue);
            int ax = axisRect.x + 1;

            int lx = ax + 2;
            int tx = lx + 6;
            // Top (maximum tick mark)
            int maxY = computeYPixelValue(drawingRect, axisDefinition, maxValue);
            int midY = computeYPixelValue(drawingRect, axisDefinition, baseValue);

        //    log.info("Drawing Y axis: min = " + minValue + ", max=" + maxValue + ", base=" + baseValue);
            int nrsteps = 4;
            double dy = minY - maxY;
            if (dy > 40) {
                nrsteps = 10;
            } else if (dy > 16) {
                nrsteps = 4;
            } else {
                nrsteps = 1;
            }
            double step = getStepSize(maxValue - minValue, nrsteps);
            if (nrsteps > 0 && step > 0 && (maxValue - minValue)>0) {
                lg.setColor(Color.lightGray);                
                nrsteps = (int) ((maxValue - minValue) / step);
             //   log.info(nrsteps+ " steps, range=" + (maxValue - minValue) + ", stepsize=" + step);
                // where to start? Divide max to step size. Say step size is 2, and max is 11. Then 11/2 -> 10 is start
                double smax = Math.floor(maxValue / step) * step;
                if (nrsteps > 0) {
                    boolean drawAllTickLabels = -(maxY - minY) / nrsteps > 8;
                    boolean drawOddTickLabels = -(maxY - minY) / nrsteps > 4;
                    for (int s = 0; s <= nrsteps; s++) {
                        double sv = (smax - s * step);
                        int sy = computeYPixelValue(drawingRect, axisDefinition, sv);
                        if (sy + 2 > minY) {
                            break;
                        }
                        // not too close to min or max
                        if (sy > maxY + 2) {
                            lg.drawLine(lx, sy, lx + 2, sy);
                            if (drawAllTickLabels || (drawOddTickLabels && s % 2 == 1)) {
                                // not too close to other labels
                                if (sy + 12 < minY && sy > maxY + 8 && Math.abs(sy - midY) > 8) {
                                    lg.drawString(formatter.format(sv), tx, sy + 4);
                                }
                            }
                        }
                    }
                }
            }

            Color axisLabelColor = Color.darkGray;
            lg.setColor(axisLabelColor);

            //labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, pY, axisRect.x + AXIS_AREA_WIDTH - 5, pY);
            lg.drawLine(lx, minY - 1, lx + 5, minY - 1);
            //GraphicUtils.drawRightJustifiedText(formatter.format(minValue), axisRect.x + AXIS_AREA_WIDTH - 15, pY, lg);
            lg.drawString(formatter.format(minValue), tx, minY - 1);


            //labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, topPY,axisRect.x + AXIS_AREA_WIDTH - 5, topPY);
            lg.drawLine(lx, maxY, lx + 5, maxY);
            //GraphicUtils.drawRightJustifiedText(formatter.format(maxValue), axisRect.x + AXIS_AREA_WIDTH - 15, topPY + 4, lg);
            lg.drawString(formatter.format(maxValue), tx, maxY + 4);

            // Connect top and bottom
            // labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, topPY,axisRect.x + AXIS_AREA_WIDTH - 10, pY);
            lg.drawLine(lx, maxY, lx, minY);
            // Middle tick mark.  Draw only if room

            if ((midY < minY - 3) && (midY > maxY + 3)) {
                //labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, midPY,axisRect.x + AXIS_AREA_WIDTH - 5, midPY);
                lg.drawLine(lx, midY, lx + 5, midY);
            }

            if ((midY < minY - 10) && (midY > maxY + 10)) {
                //GraphicUtils.drawRightJustifiedText(formatter.format(baseValue), axisRect.x + AXIS_AREA_WIDTH - 15, midPY + 4, labelGraphics);
                lg.drawString(formatter.format(baseValue), tx, midY + 4);
            }
        } else if (track.isShowDataRange() && arect.height > 20) {
            //} else if (!FrameManager.isExomeMode() && track.isShowDataRange() && arect.height > 20) {
            DataRange range = track.getDataRange();
            if (range != null) {
                Graphics2D g = context.getGraphic2DForColor(Color.black);
                Font font = g.getFont();
                Font smallFont = FontManager.getFont(8);
                try {
                    g.setFont(smallFont);
                    String minString = range.getMinimum() == 0f ? "0" : String.format("%.3f", range.getMinimum());
                    String fmtString = range.getMaximum() > 10 ? "%.0f" : "%.2f";
                    String maxString = String.format(fmtString, range.getMaximum());
                    String scale = "[" + minString + " - " + maxString + "]";
                    g.drawString(scale, arect.x + 5, arect.y + 10);

                } finally {
                    g.setFont(font);
                }
            }
        }
    }

    private double getStepSize(double range, int nrsteps) {
        // calculate an initial guess at step size
        if (range ==0 || nrsteps <1) return 0;
        double tempStep = range / nrsteps;

        // get the magnitude of the step size
        double mag =  Math.floor(Math.log10(tempStep));
        double magPow =  Math.pow(10.0, mag);

        // calculate most significant digit of the new step size
        int magMsd = (int) (tempStep / magPow + 0.5);

        // promote the MSD to either 1, 2, or 5
        if (magMsd > 5.0) {
            magMsd = 10;
        } else if (magMsd > 2.0) {
            magMsd = 5;
        } else if (magMsd > 1.0) {
            magMsd = 2;
        }
        else magMsd = 1;

     //      log.info("tempStep="+tempStep+", mag="+mag+", magPow="+magPow+", msd="+magMsd);
         
        double size = magMsd * magPow;
        if (range / size > nrsteps) {
            size = size * 2;
        }
        if (range / size > nrsteps) {
            size = size * 2;
        }
        if (range / size < nrsteps / 2) {
            size = size / 2;
        }
        return size;
    }

    @Override
    public void renderBorder(Track track, RenderContext context, Rectangle arect) {

        Rectangle adjustedRect = calculateDrawingRect(arect);

        // Draw boundaries if there is room
        if (adjustedRect.getHeight() >= 10) {

            ///TrackProperties pros = track.getProperties();


            // midline

            DataRange axisDefinition = track.getDataRange();
            float maxValue = axisDefinition.getMaximum();
            float baseValue = axisDefinition.getBaseline();
            float minValue = axisDefinition.getMinimum();


            double maxX = adjustedRect.getMaxX();
            double x = adjustedRect.getX();
            double y = adjustedRect.getY();

            if ((baseValue > minValue) && (baseValue < maxValue)) {
                int baseY = computeYPixelValue(adjustedRect, axisDefinition, baseValue);

                getBaselineGraphics(context).drawLine((int) x, baseY, (int) maxX, baseY);
            }

            PreferenceManager prefs = PreferenceManager.getInstance();

            Color borderColor = (prefs.getAsBoolean(PreferenceManager.CHART_COLOR_BORDERS) && track.getAltColor() == track.getColor())
                    ? track.getColor() : Color.lightGray;
            Graphics2D borderGraphics = context.getGraphic2DForColor(borderColor);

            // Draw the baseline -- todo, this is a wig track option?
            double zeroValue = axisDefinition.getBaseline();
            int zeroY = computeYPixelValue(adjustedRect, axisDefinition, zeroValue);
            borderGraphics.drawLine(adjustedRect.x, zeroY, adjustedRect.x + adjustedRect.width, zeroY);

            // Optionally draw "Y" line  (UCSC track line option)
            if (track.isDrawYLine()) {
                Graphics2D yLineGraphics = context.getGraphic2DForColor(Color.gray);
                int yLine = computeYPixelValue(adjustedRect, axisDefinition, track.getYLine());
                GraphicUtils.drawDashedLine(borderGraphics, adjustedRect.x, yLine, adjustedRect.x + adjustedRect.width, yLine);
            }


            // If the chart has + and - numbers draw both borders or none. This
            // needs documented somewhere.
            boolean drawBorders = true;

            if (minValue * maxValue < 0) {
                drawBorders = prefs.getAsBoolean(PreferenceManager.CHART_DRAW_BOTTOM_BORDER)
                        && prefs.getAsBoolean(PreferenceManager.CHART_DRAW_TOP_BORDER);
            }

            if (drawBorders && prefs.getAsBoolean(PreferenceManager.CHART_DRAW_TOP_BORDER)) {
                borderGraphics.drawLine(adjustedRect.x, adjustedRect.y,
                        adjustedRect.x + adjustedRect.width, adjustedRect.y);
            }

            if (drawBorders && prefs.getAsBoolean(PreferenceManager.CHART_DRAW_BOTTOM_BORDER)) {
                borderGraphics.drawLine(adjustedRect.x, adjustedRect.y + adjustedRect.height,
                        adjustedRect.x + adjustedRect.width,
                        adjustedRect.y + adjustedRect.height);
            }
        }
        /*
         (CHART_DRAW_TOP_BORDER));
         prefs.setDrawBottomBorder(getBooleanPreference(CHART_DRAW_BOTTOM_BORDER));
         prefs.setColorBorders(getBooleanPreference(CHART_COLOR_BORDERS));
         prefs.setDrawAxis(getBooleanPreference(CHART_DRAW_Y_AXIS));
         prefs.setDrawTrackName(getBooleanPreference(CHART_DRAW_TRACK_NAME));
         prefs.setColorTrackName(getBooleanPreference(CHART_COLOR_TRACK_NAME));
         prefs.setAutoscale(getBooleanPreference(CHART_AUTOSCALE));
         prefs.setShowDataRange(getBooleanPreference(CHART_SHOW_DATA_RANGE));
         */
    }

    /**
     * Get a grapphics object for the baseline. TODO -- make the line style
     * settable by the user
     *
     * @param context
     * @return
     */
    private static Graphics2D getBaselineGraphics(RenderContext context) {
        Graphics2D baselineGraphics;
        baselineGraphics = (Graphics2D) context.getGraphic2DForColor(Color.lightGray).create();
        baselineGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return baselineGraphics;
    }

    /**
     * Method description
     *
     * @return
     */
    public String getDisplayName() {
        return "Scatter Plot";
    }

    protected int computeYPixelValue(Rectangle drawingRect, DataRange axisDefinition, double dataY) {

        double maxValue = axisDefinition.getMaximum();
        double minValue = axisDefinition.getMinimum();

        double yScaleFactor = drawingRect.getHeight() / (maxValue - minValue);

        // Compute the pixel y location.  Clip to bounds of rectangle.
        // The distince in pixels frmo the data value to the axis maximum
        double delta = (maxValue - dataY) * yScaleFactor;
        double pY = drawingRect.getY() + delta;

        return (int) Math.max(drawingRect.getMinY(), Math.min(drawingRect.getMaxY(), pY));
    }

    protected Rectangle calculateDrawingRect(Rectangle arect) {

        double buffer = Math.min(arect.getHeight() * marginFraction, 10);
        Rectangle adjustedRect = new Rectangle(arect);
        adjustedRect.y = (int) (arect.getY() + buffer);
        adjustedRect.height = (int) (arect.height - (adjustedRect.y - arect.getY()));


        return adjustedRect;
    }

    public void setMarginFraction(double marginFraction) {
        this.marginFraction = marginFraction;
    }

    public int getY(boolean isLog, float dataY, float baseValue, int baseY, double yScaleFactor, Rectangle adjustedRect) {
        // Compute the pixel y location.  Clip to bounds of rectangle.
        double dy = isLog ? Math.log10(dataY) - baseValue : (dataY - baseValue);
        int pY = baseY - (int) (dy * yScaleFactor);
        if (pY < adjustedRect.y) {
            pY = adjustedRect.y;
        } else if (pY >= adjustedRect.y + adjustedRect.height) {
            pY = adjustedRect.y + adjustedRect.height - 1;
        }
        return pY;
    }
}
