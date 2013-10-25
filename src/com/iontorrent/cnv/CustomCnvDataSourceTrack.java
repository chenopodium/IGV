/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.cnv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import org.apache.log4j.Logger;
import org.broad.igv.data.DataSource;
import org.broad.igv.renderer.PointsRenderer;
import org.broad.igv.track.DataSourceTrack;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackClickEvent;
import org.broad.igv.track.TrackMenuUtils;
import org.broad.igv.track.WindowFunction;
import org.broad.igv.ui.panel.IGVPopupMenu;
import org.broad.igv.util.ResourceLocator;

/**
 *
 * @author Chantal
 */
public class CustomCnvDataSourceTrack extends DataSourceTrack {

    CnvData data;
    private boolean toggleRedLine;
    private static Logger log = Logger.getLogger(CustomCnvDataSourceTrack.class);
    
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

    private void toggleLine() {
        toggleRedLine();
        if (toggleRedLine) {
            setWindowFunction(WindowFunction.noRefLine);
            p("================= removing red line");
        }
        else {
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
        JLabel popupTitle = new JLabel("  " + track.getDisplayName(), JLabel.CENTER);


        if (popupTitle != null) {
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

        
         final JMenuItem toggleLine = new JCheckBoxMenuItem("<html>Toggle orange <b>ploidy line</b></html>");
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
