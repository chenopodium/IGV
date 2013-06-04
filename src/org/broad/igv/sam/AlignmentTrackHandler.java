/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.sam;

import org.broad.igv.track.TrackClickEvent;
import org.broad.igv.ui.panel.IGVPopupMenu;

/**
 *
 * @author Chantal
 */
public interface AlignmentTrackHandler {
    public  void addCustomMenusAndActions(AlignmentTrack track, IGVPopupMenu popup, TrackClickEvent e);
}
