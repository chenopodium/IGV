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
import com.iontorrent.cnv.SegmentedRedLineCnvDataSet;
import com.iontorrent.utils.ErrorHandler;
import com.iontorrent.utils.StringTools;
import com.iontorrent.utils.io.FileTools;
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
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.openide.util.Exceptions;

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

        } else if (file.endsWith(".json")) {
            try {
                loadCustomCnvJson(locator, newTracks);
            } catch (JSONException ex) {
               p("Could not read json file: "+file);
                MessageUtils.showMessage("I could not read this file: " + file);
            }

        } else {
            p("Not sure what to do with this file");
            MessageUtils.showMessage("Don't know how to process this file: " + file);
        }
        allTracks.addAll(newTracks);
        return newTracks;
    }

    public void loadCustomCnvJson(ResourceLocator locator, List<Track> newTracks) throws JSONException {
        p("Loading custom cnv file via JSON - check properties: " + locator.getTrackLine());
        TrackProperties tp = getTrackLine(locator);
        String path = locator.getPath();
        
       String json = FileTools.getFileAsString(path);
         p("About to parse json "+json);
          JSONObject JSON = null;
        try {
            JSON = new JSONObject(new JSONTokener(json));
        } catch (JSONException ex) {
            p("Could not read json "+json+":"+ErrorHandler.getString(ex));
            return;
        }
         String controlname = JSON.getString("control");
         String samplename = JSON.getString("sample");
         String redlinename = JSON.getString("redline");
         
         // extract fron path name
         int slash = path.lastIndexOf("/");
         slash = Math.max(slash,path.lastIndexOf("\\")); 
         
         String jsonname = path.substring(slash+1);
         p("JSON name: "+jsonname);
         
         String controlfile = path.replace(jsonname, controlname);
         String samplefile = path.replace(jsonname, samplename);
         String redfile = path.replace(jsonname, redlinename);
         
         p("Got controlfile: "+controlfile);
         p("Got samplefile: "+samplefile);
         p("Got redfile: "+redfile);
         
         // xxx todo 
        CnvData data = new CnvData(samplefile, controlfile, redfile);
       // data.parseSampleAndControlFile();
        // also read red file
        String id = path;
        addCustomJsonTrack(id, data, locator, newTracks);  
    }

    public void loadCustomCnv(ResourceLocator locator, List<Track> newTracks) {
        p("Loading custom cnv file - check properties: " + locator.getTrackLine());
        TrackProperties tp = getTrackLine(locator);
        String id = locator.getPath();
        
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
        addCustomTrack(id, data, locator, cont, newTracks);        

    }

    private void p(String s) {
        log.info(s);
    }

    public TrackProperties getTrackLine(ResourceLocator locator) throws NumberFormatException {
        TrackProperties tp = null;
        String trackLine = locator.getTrackLine();
        if (trackLine != null) {
            tp = new TrackProperties();
            ParsingUtils.parseTrackLine(trackLine, tp);
            p("Got trackline: " + trackLine + ", and tp: " + tp + ", tp" + tp.getCustomProperties());
        } else {
            p("Got no trackline from locator");
        }
        return tp;
    }
     public void addCustomJsonTrack(String id, CnvData data, ResourceLocator locator, List<Track> newTracks) {
        SegmentedRedLineCnvDataSet ds = new SegmentedRedLineCnvDataSet(FileTools.isUrl(locator.getPath()), id, data);
        CustomCNVDataSource source = new CustomCNVDataSource(id, ds);
        CustomCnvDataSourceTrack track = new CustomCnvDataSourceTrack(data, locator, id, "Custom Json Cnv", source);

        track.setName(locator.getFileName());
        track.setAltColor(Color.red.darker());
        track.setColor(Color.blue);
        track.setMidColor(Color.lightGray);

/// XXX TODO
        //track.setCustomProperties(cont.getCustomProperties());

        p("Got custom cnv data source track: " + track.getName() + ", customproperties=" + track.getCustomProperties());
        // Set attributes.        
        track.setHeight(80);
        newTracks.add(track);
    }

    public void addCustomTrack(String id, CnvData data, ResourceLocator locator, CnvController cont, List<Track> newTracks) {
        SegmentedCustomCnvDataSet ds = new SegmentedCustomCnvDataSet(id, data);
        CustomCNVDataSource source = new CustomCNVDataSource(id, ds);
        CustomCnvDataSourceTrack track = new CustomCnvDataSourceTrack(data, locator, id, "Custom cnv", source);

        track.setName(locator.getFileName());
        track.setAltColor(Color.red.darker());
        track.setColor(Color.blue);
        track.setMidColor(Color.lightGray);

        track.setCustomProperties(cont.getCustomProperties());

        p("Got custom cnv data source track: " + track.getName() + ", customproperties=" + track.getCustomProperties());
        // Set attributes.        
        track.setHeight(80);
        newTracks.add(track);
    }
}
