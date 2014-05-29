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

import org.broad.igv.util.StringUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;
import javax.swing.JLabel;

/**
 * @author jrobinso
 */
public class GraphicUtils {

    public static void drawCenteredChar(Graphics g, char[] chars, int x, int y,
            int w, int h) {

        // Get measures needed to center the message
        FontMetrics fm = g.getFontMetrics();

        // How many pixels wide is the string
        int msg_width = fm.charsWidth(chars, 0, 1);

        // How far above the baseline can the font go?
        int ascent = fm.getMaxAscent();

        // How far below the baseline?
        int descent = fm.getMaxDescent();

        // Use the string width to find the starting point
        int msgX = x + w / 2 - msg_width / 2;

        // Use the vertical height of this font to find
        // the vertical starting coordinate
        int msgY = y + h / 2 - descent / 2 + ascent / 2;

        g.drawChars(chars, 0, 1, msgX, msgY);

    }

    /**
     * Draw a block of text centered in or over the rectangle
     *
     * @param text
     * @param rect
     * @param g
     */
    public static void drawCenteredText(String text, Rectangle rect, Graphics g) {
        drawCenteredText(text, rect.x, rect.y, rect.width, rect.height, g);

    }

    public static void drawCenteredText(String text, int x, int y, int w, int h, Graphics g) {
        FontMetrics fontMetrics = g.getFontMetrics();

        String t = text.replace("<b>", "");
        t = t.replace("</b>", "");
        t = t.replace("<font", "");
        t = t.replace("</font>", "");
        Rectangle2D textBounds = fontMetrics.getStringBounds(t, g);
        int xOffset = (int) ((w - textBounds.getWidth()) / 2);
        int yOffset = (int) ((h - textBounds.getHeight()) / 2);
        int ypos = y + h - yOffset - (int) (textBounds.getHeight() / 4);
        if (text.indexOf("<") > 0) {
            paintHtmlString(g, text, x + xOffset, ypos);
        } else {
            g.drawString(text, x + xOffset, ypos);
        }

    }

    public static void drawVerticallyCenteredText(String text, int margin, Rectangle rect, Graphics g2D, boolean rightJustify) {
        drawVerticallyCenteredText(text, margin, rect, g2D, rightJustify, false);
    }

    /**
     * Draw a block of text centered verticallyin the rectangle
     *
     * @param text
     * @param rect
     * @param g2D
     */
    public static void drawVerticallyCenteredText(String text,
            int margin,
            Rectangle rect,
            Graphics g2D,
            boolean rightJustify,
            boolean clear) {
        FontMetrics fontMetrics = g2D.getFontMetrics();
        String t = text.replace("<b>", "");
        t = t.replace("</b>", "");
        t = t.replace("<font", "");
        t = t.replace("</font>", "");
        Rectangle2D textBounds = fontMetrics.getStringBounds(t, g2D);

        int yOffset = (int) ((rect.getHeight() - textBounds.getHeight()) / 2);
        int yPos = (rect.y + rect.height) - yOffset - (int) (textBounds.getHeight() / 4);

        if (clear) {
            int h = 2 * (int) textBounds.getHeight();
            //Color c = g2D.getColor();
            //Globals.isHeadless();
            //g2D.setColor(Globals.VERY_LIGHT_GREY);
            int y = Math.max(rect.y, yPos - h);
            int h2 = Math.min(rect.height, 2 * h);
            g2D.clearRect(rect.x, y, rect.width, h2);
            //g2D.setColor(c);
        }

        if (rightJustify) {
            drawRightJustifiedText(text, rect.x + rect.width - margin, yPos, g2D);
        } else {
            if (text.indexOf("<") > 0) {
                paintHtmlString(g2D, text, margin, yPos);
            } else {
                g2D.drawString(text, margin, yPos);
            }
        }
    }

    private static void p(String s) {
      //  Logger.getLogger("GraphicsUtils").info(s);

    }

    private static void paintHtmlString(Graphics gg, String html, int x, int y) {
        Graphics2D g = (Graphics2D) gg;
      //  p("paintHtmlString " + html + "  at " + x + "/" + y);

        FontMetrics fontMetrics = g.getFontMetrics();


        String t = html;
        int b = html.indexOf("<b>");

        while (b > 0) {
            int e = html.indexOf("</b>", b + 1);
            if (e > b) {
                t = t.substring(0, b);
                Rectangle2D tb = fontMetrics.getStringBounds(t, g);
                g.drawString(t, x, y);
                x = (int) (x + tb.getWidth());

                Font f = g.getFont();
                Stroke oldstroke = g.getStroke();
                Color c = g.getColor();
                g.setFont(new Font("SansSerif Bold", Font.BOLD, f.getSize() + 2));
                g.setStroke(new BasicStroke(3));
                g.setColor(Color.black);
                g.setRenderingHint(
                        RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                String bold = html.substring(b + 3, e);
                p("bold part: " + bold + ", lenght: " + bold.length());
                tb = fontMetrics.getStringBounds(bold, g);
                g.drawString(bold, x, y);
                x = (int) (x + tb.getWidth());

                g.setStroke(oldstroke);
                g.setFont(f);
                g.setColor(c);
                html = html.substring(e + 4);
                b = html.indexOf("<b>");
            }

        }
        p("Drawing remainder: " + html);
        g.drawString(html, x, y);
    }

    /**
     * Draw a block of text right justified to the given location
     *
     * @param text
     * @param right
     * @param y
     * @param g
     */
    public static void drawRightJustifiedText(String text, int right, int y,
            Graphics g) {
        FontMetrics fontMetrics = g.getFontMetrics();

        String t = text.replace("<b>", "");
        t = t.replace("</b>", "");
        t = t.replace("<font", "");
        t = t.replace("</font>", "");
        Rectangle2D textBounds = fontMetrics.getStringBounds(t, g);
        int x = right - (int) textBounds.getWidth();
        if (text.indexOf("<") > 0) {
            paintHtmlString(g, text, x, y);
        } else {
            g.drawString(text, x, y);
        }

    }

    public static void drawDottedDashLine(Graphics2D g, int x1, int y1, int x2,
            int y2) {
        Stroke thindashed = new BasicStroke(1.0f, // line width
                BasicStroke.CAP_BUTT, // cap style
                BasicStroke.JOIN_BEVEL, 1.0f, // join style, miter limit
                new float[]{8.0f, 3.0f, 2.0f, 3.0f}, // the dash pattern :  on 8, off 3, on 2, off 3
                0.0f);  // the dash phase
        drawDashedLine(g, thindashed, x1, y1, x2, y2);

    }

    public static void drawDashedLine(Graphics2D g, int x1, int y1, int x2,
            int y2) {
        Stroke thindashed = new BasicStroke(1.0f, // line width
                BasicStroke.CAP_BUTT, // cap style
                BasicStroke.JOIN_BEVEL, 1.0f, // join style, miter limit
                new float[]{3.0f, 3.0f}, // the dash pattern :  on 8, off 3, on 2, off 3
                0.0f);  // the dash phase
        drawDashedLine(g, thindashed, x1, y1, x2, y2);

    }
//    private static void p(String s) {
//        Logger.getLogger(GraphicUtils.class).info(s);
//    }

    public static void drawWrappedText(String string, Rectangle rect, Graphics2D g2D, boolean clear) {
        FontMetrics fontMetrics = g2D.getFontMetrics();
        Rectangle2D stringBounds = fontMetrics.getStringBounds(string, g2D);
        final int margin = 2;
        int textHeight = (int) stringBounds.getHeight() + margin;
        double textWidth = stringBounds.getWidth() + 10;
        boolean nl = string.indexOf("<br>") >= 0 || string.indexOf("\n") >= 0;


        if (textWidth < rect.width && !nl) {
            GraphicUtils.drawVerticallyCenteredText(string, margin, rect, g2D, false, clear);
        } else {
            int charWidth = (int) (stringBounds.getWidth() / string.length());
            int charsPerLine = rect.width / charWidth;

            int nStrings = (string.length() / charsPerLine) + 1;
//            if (nl) {                
//                nStrings++;
//            }
            int nlpos = 0;
            if (nl) {
                //   p("drawWrappedText: found nl in " + string + ". nr=" + nStrings + ",  h=" + rect.getHeight() + ", w=" + rect.getWidth() + ", perline:" + charsPerLine);
                int nlpos1 = string.indexOf("<br>");
                int nlpos2 = string.indexOf("\n");
                string = string.replace("<br>", "");
                string = string.replace("\n", "");
                nlpos = Math.max(nlpos1, nlpos2);
                nStrings++;
                //   p("nStrings = " + nStrings + ", textHeight=" + textHeight + ", rect.height=" + rect.height + ", nlpos=" + nlpos);
            }
            if (nStrings * textHeight > rect.height) {
                if (nlpos > 0) {
                    //cutting off after nl

                    string = string.substring(0, nlpos);
                    //   p("Cutting off part after nlpos "+nlpos+": drawing just "+string);
                    GraphicUtils.drawVerticallyCenteredText(string, margin, rect, g2D, false, clear);
                } else {
                    //     p("drawWrappedText: Shortening string " + string + ", nlines=" + nStrings + ", rect.height=" + rect.height);
                    // Shorten string to fit in space.  Try a max of 5 times,  progressivley shortening string
                    int nChars = (rect.width - 2 * margin) / charWidth + 1;
                    int nTries = 0;
                    String shortString;
                    double w;
                    do {
                        shortString = StringUtils.checkLength(string, nChars);
                        w = fontMetrics.getStringBounds(shortString, g2D).getWidth() + 2 * margin;
                        nTries++;
                        nChars--;
                    } while (w > rect.width && nTries <= 5 && nChars > 1);

                    GraphicUtils.drawVerticallyCenteredText(shortString, margin, rect, g2D, false, clear);
                }
            } else {
                //    p("--drawWrappedText: computing breakpointfor "+string+": nStrings=" + nStrings + ", totlen=" + string.length()+", textheight="+textHeight+", charpserline="+charsPerLine);
                int breakPoint = 0;
                Rectangle tmp = new Rectangle(rect);
                tmp.y -= ((nStrings - 1) * textHeight) / 2;
                while (breakPoint < string.length()) {

                    int end = Math.min(string.length(), breakPoint + charsPerLine);

                    if (nlpos > 0 && breakPoint == 0) {
                        end = nlpos;
                    }
                    String sub = string.substring(breakPoint, end);
                    //     p("        got substring " + breakPoint + "-" + end + ":" + string.substring(breakPoint, end));
                    double w = fontMetrics.getStringBounds(sub, g2D).getWidth() + 2 * margin;
                    if (w > rect.width) {
                        //         p("          substring too long - shortening maybe");
                        int nChars = (rect.width - 2 * margin) / charWidth + 1;
                        int nTries = 0;
                        while (w > rect.width && nTries <= 5 && nChars > 1) {
                            sub = StringUtils.checkLength(sub, nChars);
                            w = fontMetrics.getStringBounds(sub, g2D).getWidth() + 2 * margin;
                            nTries++;
                            nChars--;
                        }
                        GraphicUtils.drawVerticallyCenteredText(sub, margin, tmp, g2D, false, clear);
                    } else {
                        //  p("          substring NOT too long");
                        GraphicUtils.drawVerticallyCenteredText(sub, margin, tmp, g2D, false);
                    }
                    breakPoint = end;

                    //       p("        drawWrappedText: new breakopint=" + breakPoint);
                    tmp.y += textHeight;
                }
            }
        }
    }

    private static void sp(String s) {
        Logger.getLogger("GraphicUtils").info(s);
    }

    /**
     * Method description Stroke thindashed = new BasicStroke(thickness, // line
     * width BasicStroke.CAP_BUTT, // cap style BasicStroke.JOIN_BEVEL, 1.0f, //
     * join style, miter limit dashPattern, // the dash pattern : on 8, off 3,
     * on 2, off 3 phase); // the dash phase
     *
     * @param g
     */
    public static void drawDashedLine(Graphics2D g, Stroke stroke,
            int x1, int y1, int x2, int y2) {


        Stroke currentStroke = g.getStroke();
        g.setStroke(stroke);
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(currentStroke);

    }

    public static void drawHorizontalArrow(Graphics g, Rectangle r, boolean direction) {
        int[] x;
        int[] y;

        int dy = r.height / 3;
        int y0 = r.y;
        int y1 = y0 + dy;
        int y3 = y0 + r.height;
        int y2 = y3 - dy;
        int yc = (y1 + y2) / 2;
        int dx = yc - y0;
        if (direction) {
            int x1 = r.x;
            int x3 = x1 + r.width;
            int x2 = x3 - dx;
            x = new int[]{x1, x2, x2, x3, x2, x2, x1};
            y = new int[]{y1, y1, y0, yc, y3, y2, y2};
        } else {
            int x1 = r.x;
            int x3 = x1 + r.width;
            int x2 = x1 + dx;
            x = new int[]{x1, x2, x2, x3, x3, x2, x2};
            y = new int[]{yc, y0, y1, y1, y2, y2, y3};

        }

        g.fillPolygon(x, y, x.length);
    }

    public static void drawCenteredText(Graphics2D g, char[] chars, int x, int y, int w, int h) {

        // Get measures needed to center the message
        FontMetrics fm = g.getFontMetrics();

        // How many pixels wide is the string
        int msg_width = fm.charsWidth(chars, 0, 1);

        // How far above the baseline can the font go?
        int ascent = fm.getMaxAscent();

        // How far below the baseline?
        int descent = fm.getMaxDescent();

        // Use the string width to find the starting point
        int msgX = x + w / 2 - msg_width / 2;

        // Use the vertical height of this font to find
        // the vertical starting coordinate
        int msgY = y + h / 2 - descent / 2 + ascent / 2;

        g.drawChars(chars, 0, 1, msgX, msgY);

    }
}
