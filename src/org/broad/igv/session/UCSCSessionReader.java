/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

package org.broad.igv.session;

import java.io.BufferedInputStream;
import org.apache.log4j.Logger;
import org.broad.igv.track.Track;
import org.broad.igv.track.TrackProperties;
import org.broad.igv.ui.IGV;
import org.broad.igv.ui.panel.TrackPanel;
import org.broad.igv.ui.util.MessageUtils;
import org.broad.igv.util.ParsingUtils;
import org.broad.igv.util.ResourceLocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to parse a UCSC session file
 *
 * @author Jim Robinson
 * @date 1/12/12
 */
public class UCSCSessionReader implements SessionReader {

    private static Logger log = Logger.getLogger(UCSCSessionReader.class);

    IGV igv;

    public UCSCSessionReader(IGV igv) {
        this.igv = igv;
    } 

     @Override
    public boolean loadSession(Session session, String sessionPath, int tries) throws IOException {
            InputStream inputStream = new BufferedInputStream(ParsingUtils.openInputStreamGZ(new ResourceLocator(sessionPath)));
            return loadSession(inputStream, session, sessionPath);
    }
    /**
     * Load a UCSC session from the given stream.
     *
     * @param inputStream
     * @param session
     * @param sessionName
     * @throws IOException
     */
    public boolean loadSession(InputStream inputStream, Session session, String sessionName) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String trackLine = null;
        String nextLine;

        final List<String> errors = new ArrayList<String>();
        final HashMap<String, List<Track>> loadedTracks = new HashMap();
        List<ResourceLocator> aSync = new ArrayList();

        while ((nextLine = reader.readLine()) != null) {
            ResourceLocator locator = null;
            try {

                if (nextLine.startsWith("#")) {
                    continue;
                } else if (nextLine.startsWith("browser")) {
                    parseBrowserLine(nextLine, session);
                } else if (nextLine.startsWith("track")) {
                    trackLine = nextLine;
                    String dataURL = getDataURL(trackLine);
                    if (dataURL != null) {
                        locator = new ResourceLocator(dataURL);
                        loadedTracks.put(dataURL, igv.load(locator));
                    }
                } else {
                    locator = new ResourceLocator(nextLine);
                }

                if (locator != null) {
                    locator.setTrackLine(trackLine);
                    // Alignment tracks must be loaded synchronously
                    if (isAlignmentFile(locator.getPath())) {
                        TrackPanel panel = igv.getPanelFor(locator);
                        panel.addTracks(igv.load(locator));
                    } else {
                        aSync.add(locator);
                    }
                    trackLine = null; // Reset for next time
                    locator = null;
                }
            } catch (Exception e) {
                log.error("Error loading resource " + locator.getPath(), e);
                String ms = "<b>" + locator.getPath() + "</b><br>&nbs;p&nbsp;" + e.toString() + "<br>";
                errors.add(ms);
            }
        }

        loadAsynchronous(aSync, loadedTracks, errors);

        if (errors.size() > 0) {
            displayErrors(errors);
        }
        return true;

    }

    private void loadAsynchronous(List<ResourceLocator> aSync, final HashMap<String, List<Track>> loadedTracks,
                                  final List<String> errors) {
        List<Thread> threads = new ArrayList(aSync.size());
        for (final ResourceLocator locator : aSync) {
            Runnable runnable = new Runnable() {
                public void run() {
                    // TODO handle errors
                    try {
                        loadedTracks.put(locator.getPath(), igv.load(locator));
                    } catch (Exception e) {
                        log.error("Error loading resource " + locator.getPath(), e);
                        String ms = "<b>" + locator.getPath() + "</b><br>&nbs;p&nbsp;" + e.toString() + "<br>";
                        errors.add(ms);
                    }
                }
            };
            Thread t = new Thread(runnable);
            threads.add(t);
            t.start();
        }
        // Wait for all threads to complete
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignore) {
            }
        }
        placeTracksInPanels(aSync, loadedTracks);

    }

    private String getDataURL(String nextLine) {
        TrackProperties props = new TrackProperties();
        ParsingUtils.parseTrackLine(nextLine, props);
        return props.getDataURL();
    }


    private void placeTracksInPanels(List<ResourceLocator> locatorPaths, Map<String, List<Track>> loadedTracks) {
        for (ResourceLocator loc : locatorPaths) {
            //TrackPanel panel = IGV.getInstance().getPanelFor(new ResourceLocator(path));
            // If loading from UCSC use a single panel
            TrackPanel panel = igv.getTrackPanel(IGV.DATA_PANEL_NAME);
            String path = loc.getPath();
            if (loadedTracks.containsKey(path)) {
                panel.addTracks(loadedTracks.get(path));
            }
        }
    }

    private boolean isAlignmentFile(String path) {
        return path.endsWith(".bam") || path.endsWith(".entries") || path.endsWith(".sam");
    }


    /**
     * Browser lines have 2 or 3 tokens,  e.g.
     * browser position chr19:11500000-12000000
     *
     * @param line
     */
    private void parseBrowserLine(String line, Session session) {

        String[] tokens = line.split("\\s+");
        if (tokens.length >= 3 && tokens[1].equals("position")) {
            session.setLocus(tokens[2]);
        }
    }


    private void displayErrors(List<String> errors) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>Errors were encountered while loading session:<br>");
        for (String e : errors) {
            buffer.append(e);
        }
        MessageUtils.showMessage(buffer.toString());
    }

}
