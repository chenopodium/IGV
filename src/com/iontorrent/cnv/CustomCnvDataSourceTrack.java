/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.data.DataSource;
import org.broad.igv.feature.LocusScore;
import org.broad.igv.renderer.PointsRenderer;
import org.broad.igv.renderer.XYPlotRenderer;
import org.broad.igv.track.DataSourceTrack;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackClickEvent;
import org.broad.igv.track.TrackMenuUtils;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.ui.UIConstants;
import org.broad.igv.ui.panel.IGVPopupMenu;
import org.broad.igv.ui.panel.ReferenceFrame;
import org.broad.igv.util.ResourceLocator;

/**
 *
 * @author Chantal
 */
public class CustomCnvDataSourceTrack extends DataSourceTrack {

    CnvData data;
    private boolean toggleRedLine;
    private static Logger log = Logger.getLogger(CustomCnvDataSourceTrack.class);
    private HashMap<String, Double> cutoffvalues = new HashMap<String, Double>();
    private static DecimalFormat format = new DecimalFormat("#.##");
    public CustomCnvDataSourceTrack(CnvData data, ResourceLocator locator, String id, String name, DataSource dataSource) {
        super(locator, id, name, dataSource);
        this.data = data;
        setName(id);
        setShowDataRange(true);
        setCutoffScore(0);


        setUseScore(true);
        this.getDataRange().setDrawBaseline(true);
        this.getDataRange().setBaseline(0);
        setRendererClass(PointsRenderer.class);
        setDisplayMode(Track.DisplayMode.EXPANDED);

    }

    @Override
    protected String getBaselineName() {
        return "Expected value";
    }
    /**
     * @return the cutoffScore
     */
    @Override
    public double getCutoffScore() {
        //  p("Get expected value!");
        //super.gete

        return super.getCutoffScore();
    }

    @Override
    public Color getAltColor() {
        return new Color(185, 0, 0);
    }
    @Override
    public Color getColor() {
        return new Color(0, 0, 255);
    }
    public double getExpectedValue(String chr) {
        Double ex = cutoffvalues.get(chr);
        if (ex != null && ex.doubleValue() != 0) {
        //    p("Found expected value for " + chr + " in map");
            return ex.doubleValue();
        }
        double expected = this.getCutoffScore();

        if (this.getSample() != null) {
            String key = chr + "_CUTOFF_" + this.getSample();
            p(" getExpectedValue ===================  Getting expected value cnv track for chr " + chr + "-  store in map. KEY: "+key);
            key = key.toUpperCase();
            // CHR1_CUTOFF_SELF=4
            // CHR2_CUTOFF_SELF=3
            // CHR23_CUTOFF_SELF=4
            String val = PreferenceManager.getInstance().getTemp(key);
            if (val != null) {
                try {
                    expected = Integer.parseInt(val);
                } catch (Exception e) {
                    p("getExpectedValue Could not parse expected from " + val);
                }
            } else {
                p("getExpectedValueFound no value for expected value for key  " + key);
            }
            p("getExpectedValue Expected value for " + key + ":" + expected);
        } else {
            p("getExpectedValue no Sample Info. Got no expected value (using 2), and have no sample info for track " + this.getName());
        }
        cutoffvalues.put(chr, expected);
        return expected;
    }

    public String getValueStringAt(String chr, double position, int y, ReferenceFrame frame) {
        StringBuilder buf = new StringBuilder();
        LocusScore score = super.getLocusScoreAt(chr, position, frame);
        // If there is no value here, return null to signal no popup
        if (score == null) {
            return null;
        }
        buf.append(getName() + "<br>");
        if ((getDataRange() != null) && (getRenderer() instanceof XYPlotRenderer)) {
            buf.append("Data scale: " + getDataRange().getMinimum() + " - " + getDataRange().getMaximum() + "<br>");
        }
        if (this.getLinkedTrack() != null) {
            buf.append("<b>Data scale is linked to track: " + getLinkedTrack() + "</b><br>");
        }

        double expected = this.getExpectedValue(chr);
        if (expected != 0 && this.getAltColor() != this.getColor() && this.getDataRange() != null) {
            buf.append("Expected value: <b>" + expected + "</b><br>");
        }
        double p = score.getScore();
        String pl = "" + format.format(p);


        if (p > expected) {
            pl = "<font color='000099'>" + pl + "</font>";
        } else if (p < expected) {
            pl = "<font color='990000'>" + pl + "</font>";
        }
        String valueString = "Value @ " + chr + ":" + score.getStart() + "-" + score.getEnd() + ": <b>" + pl + "</b>";
        buf.append(valueString);
        return buf.toString();
    }

    private void toggleLine() {
        toggleRedLine();
        if (toggleRedLine) {
            setWindowFunction(WindowFunction.noRefLine);
            p("================= removing red line");
        } else {
            setWindowFunction(null);
            p(" ===============Enabling red line");
        }
        //CnvControlPanel.show(data);
    }

    private void p(String s) {
        log.info(s);
    }

    void toggleRedLine() {
        toggleRedLine = !toggleRedLine;
    }

    @Override
    public IGVPopupMenu getPopupMenu(TrackClickEvent te) {


        IGVPopupMenu menu = new IGVPopupMenu();

        List<Track> tracks = Arrays.asList((Track) this);

        CustomCnvDataSourceTrack track = (CustomCnvDataSourceTrack) CustomCnvDataSourceTrack.this;
        String title = track.getDisplayName();
        title = title.replace("<br>", " ");
        JLabel popupTitle = new JLabel("<html>" + title+"</html>");

        
        if (popupTitle != null) {
            popupTitle.setFont(UIConstants.boldFont);
            menu.add(popupTitle);
        }

//        final JMenuItem showInWindow = new JCheckBoxMenuItem("<html>Show track in <b>separate</b> window</html>");
//        showInWindow.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                showInWindow();
//            }
//        });
        //   menu.add(showInWindow);


        final JMenuItem toggleLine = new JCheckBoxMenuItem("<html>Toggle <b>ploidy line</b></html>");
        toggleLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLine();
            }
        });
        menu.add(toggleLine);
        //addSeparator();
        TrackMenuUtils.addStandardItems(menu, tracks, te);


        menu.addSeparator();
        menu.add(TrackMenuUtils.getRemoveMenuItem(tracks));
        return menu;

    }
}
