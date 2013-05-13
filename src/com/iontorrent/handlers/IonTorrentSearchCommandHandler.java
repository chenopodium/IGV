/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.PreferenceManager;
import org.broad.igv.batch.CommandExecutor;
import org.broad.igv.track.Track;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.action.SearchCommand;
import org.broad.igv.ui.action.SearchCommandHandler;
import org.broad.igv.util.LongRunningTask;

/**
 *
 * @author Chantal
 */
public class IonTorrentSearchCommandHandler implements SearchCommandHandler {
    
    private static Logger log = Logger.getLogger(IonTorrentSearchCommandHandler.class);
    
    
    @Override
     public void execute(SearchCommand cmd) {

        String searchString = cmd.getSearchString();
        log.info("Run search: " + searchString);
        
        String oldchr = cmd.getReferenceFrame().getChrName();
        List<SearchCommand.SearchResult> results = cmd.runSearch(searchString);
        if (cmd.isAskUser()) {
            results = cmd.askUserFeature(results);
            if (results == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Multiple results, show cancelled: " + searchString);
                }
                return;
            }
        }

        cmd.showSearchResult(results);

        log.info("IonTorrentSearchCommandHandler.End search: " + searchString);
        
        // we might have defined a variable chr1=location to file, to speed up loading of IGV
        // in this case, we want to load the next chromosome now....
        String newchr = cmd.getReferenceFrame().getChrName();
        log.info("IonTorrentSearchCommandHandler.execute: old: " + oldchr + ", newchr: " + newchr);
        if (newchr != null && oldchr != null && !(newchr.equalsIgnoreCase(oldchr))) {
            String locationtofile = PreferenceManager.getInstance().getTemp(newchr);
            if (locationtofile != null) {
                log.info("User moved to new chr " + newchr + ", and we found a map to a file " + locationtofile + ", will now use load command");
                // first remove other tracks
                String name = newchr;
                Collection<Track> remove = new ArrayList<Track>();
                log.info("Trying to remove other tracks with name chr");
                Collection<Track> alltracks = IGV.getInstance().getAllTracks();

                for (Track other : alltracks) {
                    if (other.getName().startsWith("chr")) {
                        log.info("Found other track " + other.getName());
                        remove.add(other);
                    } else {
                        log.info("NOT removing track " + other.getName());
                    }
                }
                if (remove.size() > 0) {
                    log.info("Removing other tracks");
                    IGV.getInstance().removeTracks(remove);
                }
                CommandExecutor cmdExe = new CommandExecutor();

                Map<String, String> params = new HashMap<String, String>();
                LongRunningTask.submit(new CommandExecutor.LoadRunnable(locationtofile, searchString, true, name, params, cmdExe));
            }
        }

    }
}
