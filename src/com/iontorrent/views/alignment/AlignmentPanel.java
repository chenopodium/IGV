/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.alignment;

import com.iontorrent.data.IonogramAlignment;
import com.iontorrent.data.Ionogram;
import com.iontorrent.data.PeakFunction;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import org.broad.igv.PreferenceManager;

/**
 * One single line in the ionogram alignment
 *
 * @author Chantal Roth
 */
public class AlignmentPanel extends JPanel {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlignmentPanel.class);
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
    private Color selectedcolor = new Color(255, 180, 180);
    // private boolean NORM;
    private BasicStroke line = new BasicStroke(1);
    private BasicStroke fatline = new BasicStroke(2);
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
    private static final int NRPOINTS = 40;
    private PeakFunction peak = new PeakFunction(NRPOINTS);
    private boolean drawPeak;
    private PreferenceManager prefs;

    public AlignmentPanel(Ionogram ionogram, IonogramAlignment alignment, boolean isHeader) {
        this.ionogram = ionogram;
        this.alignment = alignment;
        this.isHeader = isHeader;

        this.setBackground(Color.white);
        prefs = PreferenceManager.getInstance();
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
        this.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int slot = getSlot(e);
                if (slot >= 0 && slot < AlignmentPanel.this.alignment.getNrslots()) {
                    if (!AlignmentPanel.this.isHeader) {
                        p("Selecting slot " + slot);
                        AlignmentPanel.this.ionogram.toggleSelect(slot);
                        repaint();
                    }
                }
            }
        });
    }

    public int getSlot(MouseEvent e) {
        int slot = e.getX() / slotwidth;
        return slot;
    }

    public Ionogram getIonogram() {

        return ionogram;
    }

    //btnRaw.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/iontorrent/views/raw.png")))
    @Override
    public String getToolTipText(MouseEvent evt) {
        //  p("Get Ionopanel tool tip");
        int slot = evt.getX() / this.slotwidth;
        String nl = "<br>";
        String s = "read: " + ionogram.getReadname() + nl;
        if (slot >= 0 && slot < alignment.getNrslots()) {
            if (this.isHeader) {
                String bases = alignment.getAlignmentBase(slot);
                if (bases.charAt(0) == ' ') {
                    s += alignment.getEmptyBasesInfo(slot);
                } else {
                    s += bases;
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
        s += "<br><b>Double click (or right click) to load raw trace ";
        return "<html>" + s + "</html>";
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        String type = prefs.get(PreferenceManager.IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE);
        drawPeak = type.equalsIgnoreCase("PEAK");
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
        boolean showRawFlowValue = slotwidth > 50;
        g.drawLine(BORDER, y0, width, y0);
        for (int slot = 0; slot < slotrow.length; slot++) {
            FlowValue fv = slotrow[slot];
            boolean hasRaw = fv != null&& fv.getTimeseries() != null;
            int x = (int) (slot * dx) + x0;
            if (!isHeader && ionogram.isSelected(slot)) {
                if (hasRaw) {
                    g.setColor(Color.blue.darker());
                    
                }
                else g.setColor(selectedcolor);
                g.fillRect(x, y0 - h, slotwidth, h);

            } else if (slot == alignment.getCenterSlot()) {
                if (hasRaw) {
                    g.setColor(Color.black);
                }
                else  g.setColor(highlight);
                g.fillRect(x, y0 - h, slotwidth, h);
            }
            gg.setStroke(line);
            g.setColor(Color.black);
            g.drawLine(x, y0, x, y0 - h);
            if (this.isHeader) {

                String bases = alignment.getAlignmentBase(slot);
                if (bases.charAt(0) != ' ') {
                    Color color = colors[GATC.indexOf(bases.charAt(0))].darker();
                    g.setColor(color);
                    g.setFont(this.titleFont);
                    g.drawString("" + bases, x + 5, 15);
                } else {
                    g.setColor(Color.gray);
                    g.setFont(this.gatcFont);
                    g.drawString("" + alignment.getEmptyBases(slot), x + 5, 17);
                }
                g.setColor(Color.lightGray);
                g.setFont(this.gatcFont);
                g.drawString("slot " + slot, x + 5, y0 - 2);
            } else {
                if (fv == null) {
                } else {
                    if (slot != alignment.getCenterSlot() && !ionogram.isSelected(slot)) {
                        //if (!isHeader) {
                        if (!fv.isEmpty()) {
                            if (hasRaw) {
                                g.setColor(Color.black);
                            } else {
                                g.setColor(flowcolor);
                            }
                        } else {
                            if (hasRaw) {
                                g.setColor(Color.darkGray);
                            } else {
                                g.setColor(emptycolor);
                            }
                        }
                        g.fillRect(x, y0 - h, slotwidth, h);
                        // }
                    }
                    char base = fv.getBase();
                    double value = fv.getRawFlowvalue();
                    int y = y0 - (int) (value * dy);
                    int mx = x + (int) (dx / 2);
                    int barwidth = slotwidth / 3;

                    gg.setStroke(line);
                    g.setColor(Color.black);
                    g.drawLine(x, y0, x, y0 - h);
                    Color color = colors[GATC.indexOf(base)];
                    int nr = 0;
                    g.setColor(color.darker());
                    int maxy = (int) (value * dy);
// DRAW PEAK FUNCTION
                    if (hasRaw) {
                      //  p("Drawing RAW timeseries data");
                        // g.fill3DRect(mx - barwidth / 2, y, barwidth, maxy, true);
                        // 1. normalize
                        g.setColor(Color.yellow);
                        double data[] = fv.getTimeseries().getData();
                        WellFlowDataResult res = fv.getTimeseries();
                        double rawmin = res.getMin();
                        double rawmax = res.getMax();
                        double rawdv = rawmax - rawmin;
                        double rawdy = maxy / rawdv;
                        double sx = x + 1.0;
                        double prevx = sx;
                        double prevy = y0;
                        double rawdx = (double)slotwidth/(double)data.length;
                      //  p("rawdx: "+rawdx);
                        gg.setStroke(fatline);
                        for (int i = 0; i < data.length; i++) {
                            double v = data[i];                          
                            double offy = (double) (v * rawdy);
                            double nextx = sx + i*rawdx;
                            double nexty = y0 - offy;
                            g.drawLine((int)prevx, (int)prevy, (int)nextx, (int)nexty);
                            prevx = nextx;
                            prevy = nexty;

                        }
                    } else if (drawPeak) {
                        int sx = x + slotwidth / 3;
                        gg.setColor(Color.gray);
                        for (int i = 100; i + 50 < value; i += 100) {
                            int liney = (int) (y0 - i * dy);
                            g.drawLine(sx - 1, liney, sx + slotwidth / 3, liney);
                        }
                        g.setColor(Color.DARK_GRAY);
                        gg.setStroke(fatline);
                        peak.draw(g, sx + 1, y0 - 1, slotwidth / 2, -maxy);
                        g.setColor(color.darker());
                        //gg.setStroke(fatline);
                        peak.draw(g, sx, y0 - 2, slotwidth / 2, -maxy);
                    } else {
                        g.fill3DRect(mx - barwidth / 2, y, barwidth, maxy, true);
                    }
                    g.setFont(gatcFont);
                    nr = (int) Math.round(value / 100.0);
                    if (showText) {
                        g.drawString("" + nr + base, x + 2, y0 - 5);
                    } else if (fv.isEmpty()) {
                        g.drawString("" + (char) base, x + 2, y0 - 5);
                    }
                    if (showRawFlowValue) {
                        g.drawString("" + fv.getRawFlowvalue(), x + slotwidth - 20, y0 - 5);
                    }
                    if (showText) {
                        if (hasRaw) g.setColor(Color.orange);
                        else g.setColor(Color.darkGray);
                        g.drawString("" + fv.getFlowPosition(), x + 2, y0 - h + 10);
                    }



                    if (!drawPeak) {
                        gg.setStroke(line);
                        if (base == 'G') {
                            gg.setColor(Color.lightGray);
                        } else {
                            gg.setColor(Color.darkGray);
                        }

                        for (int i = 100; i + 50 < value; i += 100) {
                            int liney = (int) (y0 - i * dy);
                            g.drawLine(mx - barwidth / 2 + 1, liney, mx + barwidth / 2 - 1, liney);
                        }
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
