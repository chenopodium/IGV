/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.iontorrent.karyo.data;

import java.util.Comparator;

/**
 *
 * @author Chantal
 */
public class KaryoTrackComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
       KaryoTrack k1 = (KaryoTrack)o1;
       KaryoTrack k2 = (KaryoTrack)o2;
       return k1.getOrder() - k1.getOrder();
    }
    
}
