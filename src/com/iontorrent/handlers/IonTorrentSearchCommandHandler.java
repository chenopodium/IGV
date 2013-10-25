/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.broad.igv.Globals;
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
        //log.info("Run search: " + searchString);

        String oldchr = cmd.getReferenceFrame().getChrName();
        log.info("Old chromosome: "+oldchr);
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

        log.info("======================= IonTorrentSearchCommandHandler.Search: " + searchString);

        // we might have defined a variable chr1=location to file, to speed up loading of IGV
        // in this case, we want to load the next chromosome now....
        String newchr = cmd.getReferenceFrame().getChrName();
        if (searchString.equalsIgnoreCase(Globals.CHR_ALL)) {
            newchr = Globals.CHR_ALL;
            cmd.getReferenceFrame().setChromosomeName(Globals.CHR_ALL);
        }
        
        log.info("IonTorrentSearchCommandHandler.execute: old: " + oldchr + ", newchr: " + newchr+", frame chr: "+cmd.getReferenceFrame().getChrName());
        if (newchr != null && oldchr != null && !(newchr.equalsIgnoreCase(oldchr))) {
              
           
            // could be MULTIPLE locations, depending on samples. Need to get all keys that look similar
            Iterator<String> tempkeys = PreferenceManager.getInstance().getTemplValues().keySet().iterator();
            boolean found = false;
            for (; tempkeys.hasNext();) {
                String tempkey = tempkeys.next().toUpperCase();
                if (tempkey.startsWith(newchr.toUpperCase())) {
                    log.info("Found key "+tempkey+" for "+newchr+" and sample " + tempkey);
                    found = true;                    
                }
            }
           if (newchr.equalsIgnoreCase(Globals.CHR_ALL)) {
                log.info("Going to all chromosomes, removing all chromsome tracks");
                found = true;
            }
            if (found) {
                Collection<Track> remove = new ArrayList<Track>();
                log.info("====== Removing other tracks with name chr ");
                Collection<Track> alltracks = IGV.getInstance().getAllTracks();
                for (Track other : alltracks) {
                    if (other.getName().toUpperCase().startsWith("CHR")) {
                     //   log.info("Found other track to remove: " + other.getName());
                        remove.add(other);
                    } else {
                        //log.info("NOT removing track " + other.getName());
                    }
                }
                if (remove.size() > 0) {
                 //   log.info("Removing other tracks");
                    IGV.getInstance().removeTracks(remove);
                }
            }
            else {
                log.info("Could NOT find link to new chromosome "+newchr+", listing keys");
                tempkeys = PreferenceManager.getInstance().getTemplValues().keySet().iterator();
                for (; tempkeys.hasNext();) {
                    String tempkey = tempkeys.next().toUpperCase();
                    log.info("Got key "+tempkey);
                }
            }
            if (!newchr.equalsIgnoreCase(Globals.CHR_ALL)) {
                // now load the new tracks for ALL samples
                tempkeys = PreferenceManager.getInstance().getTemplValues().keySet().iterator();
                found = false;
                for (; tempkeys.hasNext();) {
                    String tempkey = tempkeys.next();
                    String TEMPKEY = tempkey.toUpperCase();
                    
                    if (TEMPKEY.startsWith(newchr.toUpperCase())) {
                        log.info("Checking "+newchr+" for TMPKEY="+TEMPKEY);
                        if (tempkey.length() == newchr.length() || TEMPKEY.startsWith(newchr.toUpperCase()+"_")) {
                            found = true;
                            String locationtofile = PreferenceManager.getInstance().getTemp(tempkey);
                            log.info("Found  "+tempkey+" for  "+newchr+" to locationtofile");
                            if (locationtofile != null) {
                                
                                // first remove other tracks
                                String name = tempkey.toLowerCase();
                                CommandExecutor cmdExe = new CommandExecutor();
                                log.info("User moved to new chr " + name + ", and we found a map to a file " + locationtofile + ", will now use load command");
                                Map<String, String> params = new HashMap<String, String>();
                                LongRunningTask.submit(new CommandExecutor.LoadRunnable(locationtofile, searchString, true, name, params, cmdExe));
                            }
                            else {
                                log.info("Found no tempPref "+tempkey+", got temp keys: "+PreferenceManager.getInstance().getTempKeys());                                
                            }                        
                        }
                        else log.info("Not using "+tempkey+" doesn't match "+newchr);
                    }
                }
                if (!found ) {
                    log.info("!!!!!!Could NOT find "+newchr.toUpperCase()+" anymore in tmpkeys!");
                }
            }
        }
        log.info("======================= IonTorrentSearchCommandHandler.done: " + searchString);
    }
}
