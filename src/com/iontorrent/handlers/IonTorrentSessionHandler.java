/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.handlers;

import org.broad.igv.session.SessionHandler;
import org.broad.igv.session.SessionReader;
import org.broad.igv.ui.IGV;

/**
 *
 * @author Chantal
 */
public class IonTorrentSessionHandler implements SessionHandler {
    @Override
    public SessionReader getSessionReader(IGV igv) {
        return new IonTorrentSessionReader(igv);
    }
}
