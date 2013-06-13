/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.views.alignment;

import com.iontorrent.data.IonogramAlignment;
import com.iontorrent.data.Ionogram;
import com.iontorrent.data.PeakFunction;
import com.iontorrent.prefs.IonTorrentPreferencesManager;
import com.iontorrent.rawdataaccess.FlowValue;
import com.iontorrent.rawdataaccess.FlowValue.BasePer;
import com.iontorrent.wellmodel.WellFlowDataResult;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import org.broad.igv.PreferenceManager;

/**
 * One single line in the ivoid ponogram alignment
 *
 * @author Chantal Roth
 */
public class AlignmentPanel extends JPanel {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AlignmentPanel.class);
    Ionogram ionogram;
    IonogramAlignment alignment;
    private boolean isHeader;
    private DecimalFormat format = new DecimalFormat("#.#");
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
    private Font smallFont = new Font("Helvetica", Font.BOLD, 10);
    private Font tinyFont = new Font("Helvetica", Font.BOLD, 8);
    private Font gatcFont = new Font("Helvetica", Font.BOLD, 10);
    private Color cline = Color.gray;
    private Color background = Color.white;
    public static Color selected_background = new Color(255, 210, 210);
    private Color emptycolor = new Color(250, 250, 250);
    private Color flowcolor = new Color(220, 220, 220);
    private Color highlightflowcolor = new Color(230, 230, 220);
    private Color differentflowcolor = new Color(220, 230, 230);
    private Color noflowcolor = Color.white;
    private Color highlight = new Color(255, 255, 180);
    private Color highlightrow = new Color(255, 255, 230);
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
        slotheight = prefs.getAsInt(IonTorrentPreferencesManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN);
        slotwidth = prefs.getAsInt(IonTorrentPreferencesManager.IONTORRENT_HEIGHT_IONOGRAM_ALIGN);
        if (this.isHeader) {
            slotheight = 120;
        }
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
                FlowValue hv = alignment.getHeaderFlowValue(slot);
                if (hv == null || hv.isEmpty()) {
                    s += alignment.getEmptyBasesInfo(slot);
                } else {
                    s += alignment.getReference(hv.getBasecall_location());
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


        String type = prefs.get(IonTorrentPreferencesManager.IONTORRENT_IONOGRAM_ALIGN_DRAWTYPE);
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
            boolean hasRaw = fv != null && fv.getTimeseries() != null;
            int x = (int) (slot * dx) + x0;
            gg.setStroke(line);
            g.setColor(Color.black);
            g.drawLine(x, y0, x, y0 - h);
            if (this.isHeader) {
                drawHeaderSlot(slot, g, x, y0, h, gg);
                g.setColor(Color.black);
             //   g.drawLine(0, y0-1, width, y0-1);
                g.drawLine(0, 0, width, 0);
            } else {
                drawIonogramSlot(fv, slot, hasRaw, g, x, y0, h, dy, dx, gg, showText, showRawFlowValue);
            }

        }
    }

    private String getPosString(String pos) {
        if (pos.length() <= 6) {
            return pos;
        } else {
            int len = pos.length();
            int cutoff = len - 4;
            pos = "..." + pos.substring(cutoff);
        } 
        return pos;
    }

    private void p(String msg) {
        log.info(msg);
        System.out.println("AlignmentPanel: " + msg);
    }

    private void err(String msg) {
        log.error(msg);
    }

    private void drawHeaderSlot(int slot, Graphics g, int x, int y0, int h, Graphics2D gg) {
        FlowValue headerfv = alignment.getHeaderFlowValue(slot);
        // p("Drawing header slot");
      //  String bases = alignment.getAlignmentBase(slot);

        if (headerfv == null || headerfv.isEmpty()) {
            // EMPTY FLOW
            g.setColor(Color.gray);
            g.setFont(this.gatcFont);
            g.drawString("" + alignment.getEmptyBases(slot), x + 5, y0 - 20);
            return;
        }

        //char ref = headerfv.getAlignmentBase();
        String ref = alignment.getReference(headerfv.getBasecall_location());
        // how many?
        //  p("Header FV for slot " + slot + " is: " + headerfv + ", bases='" + bases + "', ref base is: " + ref);

        g.setColor(Color.red);
        if (slot == alignment.getCenterSlot()) {
            g.setColor(highlight);
            //  p("highlighting center slot");
        } else {
            g.setColor(this.flowcolor);
            //   p("header,inc flow, using flow color "+flowcolor+", h="+h);
        }
        g.fillRect(x, y0 - h, slotwidth, h);
        gg.setStroke(line);
        g.setColor(Color.black);
        g.drawLine(x, y0, x, y0 - h);

        BasePer[] basepers = headerfv.getSortedNucPercentages();
        // p("Got sorted nuc per: " + Arrays.toString(basepers));
        //alignment.getB
        int starty = 20;
        int basedy = 12;
        g.setFont(this.titleFont);
        // first draw reference
        Color color = this.getBaseColor(ref);
        g.setColor(color);
        g.drawString("" + ref, x + 5, starty);
        g.setColor(Color.black);
        g.drawLine(x, starty + 3, x + slotwidth, starty + 3);
        starty += 25;
//        color = colors[GATC.indexOf("" + bases.charAt(bases.length() - 1))].darker();
//        g.setColor(color);
//        g.drawString(bases, x + 5, starty);
//        starty += 20;
        for (int i = 0; i < basepers.length; i++) {
            String base = basepers[i].getBase();
            double per = (int) basepers[i].getPercent();

            if (per > 1 || (per > 0 && i < 3)) {
                String sper = "" + (int) per;
                if (per < 10) {
                    sper = format.format(per);
                }
                color = getBaseColor(base);
                g.setColor(color);
                if (i == 0) {
                    g.setFont(this.medFont);
                } else {
                    if (per > 30) {
                        g.setFont(this.medFont);
                    } else if (per > 10) {
                        g.setFont(this.smallFont);
                    } else {
                        g.setFont(this.smallFont);
                    }
                }
                g.drawString(base + " " + sper + "%", x + 5, starty + i * basedy);
            }
        }
        g.setColor(Color.lightGray);
        g.setFont(this.gatcFont);
        int basecallloc = headerfv.getBasecall_location();
        int pos = alignment.getChromosome_center_location() + basecallloc - alignment.getNrbases_left_right();
        String posb = "" + (pos);
        posb = getPosString(posb);
        int hplen = headerfv.getHpLen();
        if (hplen == 1) {
            g.drawString(posb, x + 5, y0 - 2);
        } else {
            String posa = "" + (pos - hplen + 1);
            posa = getPosString(posa);
            g.drawString(posa + " -", x + 5, y0 - 12);
            g.drawString(posb, x + 5, y0 - 2);
        }

    }

    private void drawIonogramSlot(FlowValue fv, int slot, boolean hasRaw, Graphics g, int x, int y0, int h, double dy, float dx, Graphics2D gg, boolean showText, boolean showRawFlowValue) {

        if (fv == null) {
            return;
        }


        if (ionogram.isSelected(slot)) {
            if (hasRaw) {
                g.setColor(Color.blue.darker());

            } else {
                g.setColor(selectedcolor);
            }
            g.fillRect(x, y0 - h, slotwidth, h);
        } else if (ionogram.isIonoSelected() && slot != alignment.getCenterSlot()) {
            if (hasRaw) {
                g.setColor(Color.black);
            } else {
                g.setColor(highlightrow);
            }
            g.fillRect(x, y0 - h, slotwidth, h);

        }
        if (slot == alignment.getCenterSlot()) {
            if (hasRaw) {
                g.setColor(Color.black);
            } else {
                g.setColor(highlight);
            }
            g.fillRect(x, y0 - h, slotwidth, h);
        }
        //     p("drawing slot "+slot);
        if (slot != alignment.getCenterSlot() && !ionogram.isSelected(slot)) {

            if (!fv.isEmpty()) {
                if (hasRaw) {
                    g.setColor(Color.black);
                } else {
                    if (ionogram.isIonoSelected()) {
                        g.setColor(this.highlightflowcolor);
                    } else {
                        g.setColor(flowcolor);
                    }
                }
            } else {
                if (hasRaw) {
                    g.setColor(Color.darkGray);
                } else {
                    if (ionogram.isIonoSelected()) {
                        g.setColor(this.highlightrow);
                    } else {
                        g.setColor(emptycolor);
                    }
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
            double rawdx = (double) slotwidth / (double) data.length;
            //  p("rawdx: "+rawdx);
            gg.setStroke(fatline);
            for (int i = 0; i < data.length; i++) {
                double v = data[i];
                double offy = (double) (v * rawdy);
                double nextx = sx + i * rawdx;
                double nexty = y0 - offy;
                g.drawLine((int) prevx, (int) prevy, (int) nextx, (int) nexty);
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
        nr = fv.getHpLen();
        // check if this flow is different from all ohers!
        boolean different = alignment.isDifferent(slot, fv);
        if (different) {
            g.setFont(medFont);
        }
        if (showText) {
            g.drawString("" + nr + base, x + 2, y0 - 5);

        } else if (fv.isEmpty()) {
            g.drawString("" + (char) base, x + 2, y0 - 5);

        }
        if (showRawFlowValue) {
            g.drawString("" + fv.getRawFlowvalue(), x + slotwidth - 20, y0 - 5);
        }
        if (showText) {
            if (hasRaw) {
                g.setColor(Color.orange);
            } else {
                g.setColor(Color.darkGray);
            }
            String fpos = "" + fv.getFlowPosition();
            if (different) {
                fpos += "(*)";
            }
            g.drawString(fpos, x + 2, y0 - h + 10);
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

    private Color getBaseColor(String bases) {
        if (bases == null || bases.length()<1) return Color.black;
        int len = bases.length();
              
        Color color = colors[GATC.indexOf(bases.charAt(len-1))].darker();
        return color;
    }
}
