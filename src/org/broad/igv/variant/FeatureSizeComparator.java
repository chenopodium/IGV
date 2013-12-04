/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.broad.igv.variant;

import java.util.Comparator;
import org.broad.tribble.Feature;

/**
 *
 * @author Chantal
 */
public class FeatureSizeComparator implements Comparator{

    @Override
    public int compare(Object o1, Object o2) {
        Feature a = (Feature)o1;
        Feature b = (Feature)o2;
        int s1 = a.getEnd()-a.getStart();
        int s2 = b.getEnd()-b.getStart();
        return s2 - s1;
    }
    
}
