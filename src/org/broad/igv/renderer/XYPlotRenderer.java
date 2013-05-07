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

/**
 * @author jrobinso
 */
public abstract class XYPlotRenderer extends DataRenderer {

    private int msgs;
    private double marginFraction = 0.2;

    protected void drawDataPoint(Color graphColor, int dx, int pX, int baseY, int pY,
            RenderContext context) {
        dx = Math.max(5, dx);
       
        context.getGraphic2DForColor(graphColor).fillRect(pX, pY, dx, 4);

    }
    public Color getGradientColor(double MIN, double MAX, double score, Color chigh, Color clow, Color cmid, double middle) {
              
       
       if (cmid == null) cmid = Color.darkGray; 
        if (chigh == null) chigh = cmid;
        if (clow == null) clow = cmid;
        double rangedelta = MAX-MIN;
       
        if (middle == Integer.MIN_VALUE) middle = (MAX-MIN)/2;
        // check if we use the higher or lower scale
        double ds = score - MIN;
                
        Color hi = cmid;
        Color lo = clow;
        
        if ( Math.abs(score - middle)<0.00001) return cmid;
      //  Logger.getLogger("XYPlot").info("Score: "+score+", low="+MIN+", mid=CUTOFF="+middle+", max="+MAX);
        if (score > middle) {
            lo = cmid;
            hi = chigh;
            ds = score - middle;
        }
        
        double dr = (hi.getRed()-lo.getRed()) / (rangedelta);
        double dg = (hi.getGreen()-lo.getGreen()) / (rangedelta);
        double db = (hi.getBlue()-lo.getBlue()) / (rangedelta);
        
       // p("Getting color for "+fieldname +"="+score+", min="+MIN+", max="+MAX);
        int r = Math.min(255, Math.max(0,(int) (lo.getRed()+dr * ds)));
        int g = Math.min(255, Math.max(0,(int) (lo.getGreen()+dg * ds)));
        int b = Math.min(255, Math.max(0,(int) (lo.getBlue()+db * ds)));
        Color c = new Color(r, g, b);
        return c;
    
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
    public synchronized void renderScores(Track track, List<LocusScore> locusScores, RenderContext context, Rectangle arect) {

        boolean showMissingData = PreferenceManager.getInstance().getAsBoolean(PreferenceManager.SHOW_MISSING_DATA_KEY);

        Graphics2D noDataGraphics = context.getGraphic2DForColor(UIConstants.NO_DATA_COLOR);
        Graphics2D tickGraphics = context.getGraphic2DForColor(Color.BLACK);

        Rectangle adjustedRect = calculateDrawingRect(arect);
        double origin = context.getOrigin();
        double locScale = context.getScale();

        Color posColor = track.getColor();
        Color negColor = track.getAltColor();
        Color midColor = track.getMidColor();
        // Get the Y axis definition, consisting of minimum, maximum, and base value.  Often
        // the base value is == min value which is == 0.

        DataRange dataRange = track.getDataRange();
        float maxValue = dataRange.getMaximum();
        float cutOff = (float) track.getCutoffScore();
        float baseValue = 0;
//        if ( msgs <10) {
//            Logger.getLogger("XYPlotRenderer").info("Got cutoffscore/basevalue : " + baseValue +" for "+track.getName());
//        }
        
        if (baseValue == 0) {
            baseValue = dataRange.getBaseline();
        }
        float minValue = dataRange.getMinimum();
        boolean isLog = dataRange.isLog();

        if (isLog) {
            minValue = (float) (minValue == 0 ? 0 : Math.log10(minValue));
            maxValue = (float) Math.log10(maxValue);
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

        int lastPx = 0;
        for (LocusScore score : locusScores) {

            // Note -- don't cast these to an int until the range is checked.
            // could get an overflow.
            double pX = ((score.getStart() - origin) / locScale);
            double dx = Math.ceil((Math.max(1, score.getEnd() - score.getStart())) / locScale) + 1;
            
            if ((pX + dx < 0)) {
                continue;
            } else if (pX > adjustedRect.getMaxX()) {
                break;
            }

            float dataY = score.getScore();
            if (isLog && dataY <= 0) {
                continue;
            }

            if (!Float.isNaN(dataY)) {


                // Compute the pixel y location.  Clip to bounds of rectangle.
                double dy = isLog ? Math.log10(dataY) - baseValue : (dataY - baseValue);
                int pY = baseY - (int) (dy * yScaleFactor);
                if (pY < adjustedRect.y) {
                    pY = adjustedRect.y;
                } else if (pY > adjustedRect.y + adjustedRect.height) {
                    pY = adjustedRect.y + adjustedRect.height;
                }

                if (msgs < 10 && baseValue != 0) {
                    Logger.getLogger("XY: base="+baseValue+", data="+dataY);
                    msgs++;
                }
                //Color color = (dataY >= baseValue) ? posColor : negColor;
                Color color = getGradientColor(minValue, maxValue, dataY, posColor, negColor, midColor, cutOff);
                drawDataPoint(color, (int) dx, (int) pX, baseY, pY, context);

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

    }
    static DecimalFormat formatter = new DecimalFormat();

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
        if (context.getChr().equals(Globals.CHR_ALL)) {
            return;
        }

        super.renderAxis(track, context, arect);

        Rectangle drawingRect = calculateDrawingRect(arect);

        PreferenceManager prefs = PreferenceManager.getInstance();

        Color labelColor = prefs.getAsBoolean(PreferenceManager.CHART_COLOR_TRACK_NAME) ? track.getColor() : Color.black;
        Graphics2D labelGraphics = context.getGraphic2DForColor(labelColor);

        labelGraphics.setFont(FontManager.getFont(8));

        if (prefs.getAsBoolean(PreferenceManager.CHART_DRAW_TRACK_NAME)) {

            // Only attempt if track height is > 25 pixels
            if (arect.getHeight() > 25) {
                Rectangle labelRect = new Rectangle(arect.x, arect.y + 10, arect.width, 10);
                labelGraphics.setFont(FontManager.getFont(10));
                GraphicUtils.drawCenteredText(track.getDisplayName(), labelRect, labelGraphics);
            }
        }

        if (prefs.getAsBoolean(PreferenceManager.CHART_DRAW_Y_AXIS)) {

            Rectangle axisRect = new Rectangle(arect.x, arect.y + 1, AXIS_AREA_WIDTH, arect.height);


            DataRange axisDefinition = track.getDataRange();
            float maxValue = axisDefinition.getMaximum();
            float baseValue = axisDefinition.getBaseline();
            float minValue = axisDefinition.getMinimum();


            // Bottom (minimum tick mark)
            int pY = computeYPixelValue(drawingRect, axisDefinition, minValue);

            labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, pY, axisRect.x + AXIS_AREA_WIDTH - 5, pY);
            GraphicUtils.drawRightJustifiedText(formatter.format(minValue), axisRect.x + AXIS_AREA_WIDTH - 15, pY, labelGraphics);

            // Top (maximum tick mark)
            int topPY = computeYPixelValue(drawingRect, axisDefinition, maxValue);

            labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, topPY,
                    axisRect.x + AXIS_AREA_WIDTH - 5, topPY);
            GraphicUtils.drawRightJustifiedText(formatter.format(maxValue),
                    axisRect.x + AXIS_AREA_WIDTH - 15, topPY + 4, labelGraphics);

            // Connect top and bottom
            labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, topPY,
                    axisRect.x + AXIS_AREA_WIDTH - 10, pY);

            // Middle tick mark.  Draw only if room
            int midPY = computeYPixelValue(drawingRect, axisDefinition, baseValue);

            if ((midPY < pY - 15) && (midPY > topPY + 15)) {
                labelGraphics.drawLine(axisRect.x + AXIS_AREA_WIDTH - 10, midPY,
                        axisRect.x + AXIS_AREA_WIDTH - 5, midPY);
                GraphicUtils.drawRightJustifiedText(formatter.format(baseValue),
                        axisRect.x + AXIS_AREA_WIDTH - 15, midPY + 4, labelGraphics);
            }

        } else if (!FrameManager.isExomeMode() && track.isShowDataRange() && arect.height > 20) {
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
}
