/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.track;

import java.util.Comparator;

/**
 *
 * @author Chantal
 */
public class TrackOrderComparator implements Comparator{
     @Override
    public int compare(Object o1, Object o2) {
        if (o1 == null || o2 == null) return 0;
        Track t1 = (Track)o1;
        Track t2 = (Track)o2;
        return t1.getTrackorder() - t2.getTrackorder();
    }
    
}
