/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import com.iontorrent.cnv.CnvController;
import com.iontorrent.cnv.CnvData;
import com.iontorrent.cnv.CustomCNVDataSource;
import com.iontorrent.cnv.CustomCnvDataSourceTrack;
import com.iontorrent.cnv.SegmentedCustomCnvDataSet;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.StringTools;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.broad.igv.data.seg.SegmentedDataSource;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.renderer.PointsRenderer;
import org.broad.igv.track.DataSourceTrack;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackHandler;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;

/**
 *
 * @author Chantal
 */
public class IonTorrentTrackHandler implements TrackHandler {

    Logger log = Logger.getLogger("IonTorrentTrackHandler");

    @Override
    public List<Track> loadCustomFile(ResourceLocator locator, List<Track> allTracks, Genome genome) {
        List<Track> newTracks = new ArrayList<Track>();

        String file = locator.getPath();
        p("Procesing custom file: " + file);
        if (file.endsWith(".csv") || file.endsWith(".tsv")) {
            loadCustomCnv(locator, newTracks);

        } else {
            p("Not sure what to do with this file");
            MessageUtils.showMessage("Don't know how to process this file: " + file);
        }
        allTracks.addAll(newTracks);
        return newTracks;
    }

    public void loadCustomCnv(ResourceLocator locator, List<Track> newTracks) {
        p("Loading custom cnv file - check properties: "+locator.getTrackLine());
       
        TrackProperties tp = null;
        String trackLine = locator.getTrackLine();
        if (trackLine != null) {
            tp = new TrackProperties();
            ParsingUtils.parseTrackLine(trackLine, tp);
            p("Got trackline: "+trackLine+", and tp: "+tp+", tp"+tp.getCustomProperties());
        }
        else p("Got no trackline from locator");

        CnvController cont = new CnvController(locator.getPath());
        boolean ok = true;
        if (tp == null || tp.getCustomProperties() == null) {
            ok = cont.gatherParameters();
        } else {
            String props = tp.getCustomProperties();
            ok = cont.parseCustomProperties(props);
            if (!ok) {
                p("need to ask for parameters");
                ok = cont.gatherParameters();
            }
        }
        if (!ok) {
            p("NOT ok, returning");
            return;
        }

        p("About to parse data");
        CnvData data = cont.readData();
        String id = locator.getPath();

        SegmentedCustomCnvDataSet ds = new SegmentedCustomCnvDataSet(id, data);
        CustomCNVDataSource source = new CustomCNVDataSource(id, ds);
        CustomCnvDataSourceTrack track = new CustomCnvDataSourceTrack(data, locator, id, "Custom cnv", source);

        track.setName(locator.getFileName());
        track.setAltColor(Color.red.darker());
        track.setColor(Color.blue);
        track.setMidColor(Color.lightGray);
        

        track.setCustomProperties(cont.getCustomProperties());
       
         p("Got custom cnv data source track: " + track.getName()+", customproperties="+track.getCustomProperties());
        // Set attributes.        
        track.setHeight(80);
        newTracks.add(track);

    }

    private void p(String s) {
        log.info(s);
    }
}
