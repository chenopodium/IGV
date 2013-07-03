/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.track;

import java.util.List;
import org.broad.igv.feature.genome.Genome;
import org.broad.igv.util.ResourceLocator;

/**
 *
 * @author Chantal
 */
public interface TrackHandler {
    
    public List<Track> loadCustomFile(ResourceLocator locator, List<Track> allTracks, Genome genome);

}
