/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views;

import com.iontorrent.data.FlowValue;
import com.iontorrent.data.IonogramAlignment;
import com.iontorrent.data.Ionogram;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import org.broad.igv.PreferenceManager;

/**
 * One single line in the ionogram alignment
 *
 * @author Chantal Roth
 */
public class IonogramPanel extends JPanel {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(IonogramPanel.class);
    Ionogram ionogram;
    IonogramAlignment alignment;
    private boolean isHeader;
    private Color cg = new Color(80, 80, 80);
    private Color ca = new Color(110, 245, 120);
    private Color ct = new Color(235, 100, 100);
    private Color cc = new Color(110, 110, 235);
    private Color colors[] = {cg, ca, ct, cc};
    static String GATC = "GATC";
    public static final int BORDER = 0;
    public static final int TOP = 2;
    private Font titleFont = new Font("Helvetica", Font.BOLD, 18);
    private Font medFont = new Font("Helvetica", Font.BOLD, 12);
    private Font gatcFont = new Font("Helvetica", Font.BOLD, 10);
    private Color cline = Color.gray;
    private Color background = Color.white;
    private Color emptycolor = new Color(250, 250, 250);
    private Color flowcolor = new Color(220, 220, 220);
    private Color noflowcolor = Color.white;
    private Color highlight = new Color(255, 255, 180);
    // private boolean NORM;
    private BasicStroke line = new BasicStroke(1);
    boolean raw;
    boolean norm;
    private DecimalFormat f = new DecimalFormat("0.00");
    private BasicStroke dotted = new BasicStroke(
            1f,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_ROUND,
            1f,
            new float[]{4f},
            0f);
    private int slotheight;
    private int slotwidth;

    public IonogramPanel(Ionogram ionogram, IonogramAlignment alignment, boolean isHeader) {
        this.ionogram = ionogram;
        this.alignment = alignment;
        this.isHeader = isHeader;
        this.setBackground(Color.white);
        PreferenceManager prefs = PreferenceManager.getInstance();
        slotheight = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN);
        slotwidth = prefs.getAsInt(PreferenceManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN);
        int totalwidth = slotwidth * alignment.getNrslots() + BORDER;
        this.setSize(totalwidth, slotheight + TOP);
        //  p("Got slot height: " + slotheight);
        this.setMinimumSize(new Dimension(totalwidth, slotheight + TOP));
        this.setPreferredSize(new Dimension(totalwidth, slotheight + TOP));
        this.setMaximumSize(new Dimension(totalwidth, slotheight + TOP));
        this.setFocusable(true);
        setFocusTraversalKeysEnabled(false);
        ToolTipManager.sharedInstance().registerComponent(this);
    }

    @Override
    public String getToolTipText(MouseEvent evt) {
        //  p("Get Ionopanel tool tip");
        int slot = evt.getX() / this.slotwidth;
        String nl = "<br>";
        String s = "read: " + ionogram.getReadname() + nl;
        if (slot >= 0 && slot < alignment.getNrslots()) {
            if (this.isHeader) {
                char base = alignment.getAlignmentBase(slot);
                if (base == ' ') {
                    s += alignment.getEmptyBasesInfo(slot);
                } else {
                    s += base;
                }
            } else {
                FlowValue fv = ionogram.getSlotrow()[slot];
                if (fv != null) {
                    String fo = ionogram.getFloworder();
                    //   if (fo != null) {
                    fo = fo + fo + fo + fo;// in case the flow order is just GATC, we want to get just one substring for those 11 bases :-)
                    int pos = fv.getFlowPosition();
                    int start = Math.max(0, pos - 5);
                    start = start % fo.length();
                    int end = pos + 5;
                    end = end % fo.length();
                    pos = pos % fo.length();
                    String left = "";
                    String right = "";
                    char base = fv.getBase();
                    try {
                        if (start < end) {

                            left = fo.substring(start, pos);
                            if (pos + 1 <= end) {
                                right = fo.substring(pos + 1, end);
                            }
                        } else {
                            left = fo.substring(start);
                            if (end > 0) {
                                right = fo.substring(1, end);
                            }
                        }
                    } catch (Exception e) {
                        p("Got an error: " + e.getMessage());
                        p("start=" + start + ", end=" + end + ", pos=" + pos + "fo=" + fo);

                    }


                    s += fv.toHtml();
                    if (ionogram.isReverse()) {
                        StringBuilder sleft = new StringBuilder(right);
                        StringBuilder sright = new StringBuilder(left);
                        left = sleft.reverse().toString();
                        right = sright.reverse().toString();
                    }
                    String order = left + "<font color='000088'><b>" + base + "</b></font>" + right;

                    s += "<br>Flow order around " + pos + ": " + order;
                } else {
                    s += ionogram.toHtml();
                }
            }
        } else {
            s += ionogram.toHtml();
        }

        return "<html>" + s + "</html>";
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D gg = (Graphics2D) g;
        int width = this.getWidth();
        int height = slotheight + TOP;
        int w = width - BORDER;
        int h = height - TOP;

        int y0 = height - TOP;
        int x0 = BORDER;

        gg.setStroke(line);
        g.setColor(background);
        //  }
        g.fillRect(x0, TOP, w, h);
        g.setColor(Color.black);

        // draw graphics rectangle
        int slots = alignment.getNrslots();
        //     p("Got nr slots from ionogram:" + slots);
        float dx = slotwidth;

        // find max value
        int globalMaxFlowValue = alignment.getMaxValue();
        globalMaxFlowValue = Math.max(globalMaxFlowValue, 100);
        double dy = (double) h / (double) globalMaxFlowValue;

        FlowValue slotrow[] = ionogram.getSlotrow();
        if (slotrow == null) {
            p("Got no slotrow");
            return;
        }

        boolean showText = slotheight > 40;
        boolean showFlowValue = slotwidth > 50;
        g.drawLine(BORDER, y0, width, y0);
        for (int slot = 0; slot < slotrow.length; slot++) {
            FlowValue fv = slotrow[slot];
            int x = (int) (slot * dx) + x0;
            if (slot == alignment.getCenterSlot()) {
                g.setColor(highlight);
                g.fillRect(x, y0 - h, slotwidth, h);
            }
            gg.setStroke(line);
            g.setColor(Color.black);
            g.drawLine(x, y0, x, y0 - h);
            if (this.isHeader) {

                char base = alignment.getAlignmentBase(slot);
                if (base != ' ') {
                    Color color = colors[GATC.indexOf(base)].darker();
                    g.setColor(color);
                    g.setFont(this.titleFont);
                    g.drawString("" + base, x + 5, 15);
                } else {
                    g.setColor(Color.gray);
                    g.setFont(this.gatcFont);
                    g.drawString("" + alignment.getEmptyBases(slot), x + 5, 17);
                }
            } else {
                if (fv == null) {
                } else {
                    if (slot != alignment.getCenterSlot()) {
                        //if (!isHeader) {
                        if (!fv.isEmpty()) {
                            g.setColor(flowcolor);
                        } else {
                            g.setColor(emptycolor);
                        }
                        g.fillRect(x, y0 - h, slotwidth, h);
                        // }
                    }
                    char base = fv.getBase();
                    int value = fv.getFlowvalue();
                    int y = y0 - (int) (value * dy);
                    int mx = x + (int) (dx / 2);
                    int barwidth = slotwidth / 3;

                    gg.setStroke(line);
                    g.setColor(Color.black);
                    g.drawLine(x, y0, x, y0 - h);
                    Color color = colors[GATC.indexOf(base)];
                    int nr = 0;
                    g.setColor(color.darker());

                    g.fill3DRect(mx - barwidth / 2, y, barwidth, (int) (value * dy), true);
                    g.setFont(gatcFont);
                    nr = (int) Math.round(value / 100.0);
                    if (showText) {
                        g.drawString("" + nr + base, x + 2, y0 - 5);
                    } else if (fv.isEmpty()) {
                        g.drawString("" + (char) base, x + 2, y0 - 5);
                    }
                    if (showFlowValue) {
                        g.drawString("" + fv.getFlowvalue(), x + slotwidth - 20, y0 - 5);
                    }
                    if (showText) {
                        g.setColor(Color.darkGray);
                        g.drawString("" + fv.getFlowPosition(), x + 2, y0 - h + 10);
                    }

                    if (base == 'G') {
                        gg.setColor(Color.lightGray);
                    } else {
                        gg.setColor(Color.darkGray);
                    }
                    gg.setStroke(line);
                    for (int i = 100; i + 2 < value; i += 100) {
                        int liney = (int) (y0 - i * dy);
                        g.drawLine(mx - barwidth / 2 + 1, liney, mx + barwidth / 2 - 1, liney);
                    }
                }
            }
        }
    }

    private void p(String msg) {
        log.info(msg);
    }

    private void err(String msg) {
        log.error(msg);
    }
}
